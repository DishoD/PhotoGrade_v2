package photograde.util;

import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

import static java.lang.Math.abs;
import static java.lang.Math.round;

public final class OpenCVUtility {
    private static double EPS = 1e-6;

    private static final SplineInterpolator SPLINE_INTERPOLATOR = new SplineInterpolator();
    private static final LinearInterpolator LINEAR_INTERPOLATOR = new LinearInterpolator();

    private static Mat sepiaKernel = new Mat(3, 3, CvType.CV_64F);
    private static Mat halfStepKernel;
    private static Mat invertLut;

    static {
        sepiaKernel.put(0,0,
                0.272, 0.534, 0.131,
                        0.349, 0.686, 0.168,
                        0.393, 0.769, 0.189
        );

        halfStepKernel = sepiaKernel.inv(2);

        invertLut = getLUT(x -> 255 - x);
    }

    private OpenCVUtility() {}

    public static Mat loadImage(File file) throws IOException {
        BufferedImage bfImg = ImageIO.read(file);
        try {
            return BufferedImageToMat(bfImg);
        } catch(RuntimeException e) {
            throw new IOException();
        }
    }

    public static BufferedImage MatToBufferedImage(Mat mat) {
        int type = (mat.channels() > 1) ? BufferedImage.TYPE_3BYTE_BGR : BufferedImage.TYPE_BYTE_GRAY;

        BufferedImage bfImg = new BufferedImage(mat.cols(), mat.rows(), type);
        byte[] targetPixels = ((DataBufferByte) bfImg.getRaster().getDataBuffer()).getData();

        mat.get(0, 0, targetPixels);

        return bfImg;
    }

    public BufferedImage BufferedImageToRGB(Image img) {
        BufferedImage rgb = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_RGB);
        rgb.createGraphics().drawImage(img, 0, 0, null);
        return rgb;
    }

    public static Mat BufferedImageToMat(BufferedImage bfImg) {
        int type;
        boolean isRGB = false;

        switch (bfImg.getType()){
            case BufferedImage.TYPE_3BYTE_BGR:
                type = CvType.CV_8UC3;
                break;
            case BufferedImage.TYPE_4BYTE_ABGR:
                type = CvType.CV_8UC4;
                break;

            case BufferedImage.TYPE_BYTE_GRAY:
                type = CvType.CV_8UC1;
                break;

            case BufferedImage.TYPE_CUSTOM:
                if(bfImg.getColorModel().getNumComponents() == 2) {
                    type = CvType.CV_8UC2;
                } else if (bfImg.getColorModel().getNumComponents() == 3) {
                    type = CvType.CV_8UC3;
                    isRGB = true;
                } else {
                    type = CvType.CV_8UC4;
                }
                break;

                default:
                    throw new UnsupportedOperationException("Unsuportted image type: " + bfImg.getType());
        }

        byte[] buffer = ((DataBufferByte) bfImg.getRaster().getDataBuffer()).getData();

        Mat mat = new Mat(bfImg.getHeight(), bfImg.getWidth(), type);

        mat.put(0, 0, buffer);

        if(type == CvType.CV_8UC4) {
            Mat nmat = new Mat(bfImg.getHeight(), bfImg.getWidth(), CvType.CV_8UC4);
            Mat rmat = new Mat(bfImg.getHeight(), bfImg.getWidth(), CvType.CV_8UC3);

            ArrayList<Mat> imgSrc = new ArrayList<Mat>();
            imgSrc.add(mat);

            ArrayList<Mat> imgDest = new ArrayList<Mat>();
            imgDest.add(nmat);

            int[] fromTo = { 0, 3, 1, 0, 2, 1, 3, 2 };
            Core.mixChannels(imgSrc, imgDest, new MatOfInt(fromTo));
            Imgproc.cvtColor(nmat, rmat, Imgproc.COLOR_BGRA2BGR);
            return rmat;
        }

        if(type == CvType.CV_8UC2) {
            List<Mat> channels = new ArrayList<>();
            Core.split(mat, channels);
            return channels.get(0);
        }

        if(isRGB) {
            Mat rmat = new Mat(bfImg.getHeight(), bfImg.getWidth(), CvType.CV_8UC3);
            Imgproc.cvtColor(mat, rmat, Imgproc.COLOR_RGB2BGR);
            return rmat;
        }

        return mat;
    }

    public static Mat toGrayscale(Mat mat) {
        Mat rmat = new Mat(mat.size(), CvType.CV_8UC1);
        Imgproc.cvtColor(mat, rmat, Imgproc.COLOR_BGR2GRAY);
        return rmat;
    }

    private static byte saturate(double val) {
        int iVal = (int) Math.round(val);
        iVal = iVal > 255 ? 255 : (iVal < 0 ? 0 : iVal);
        return (byte) iVal;
    }

    public static Mat linearBrightnessAndContrast(Mat mat, double alpha, double beta) {
        Mat lookUpTable = getLUT(i -> alpha*i + beta);
        return applyLUT(mat, lookUpTable);
    }

    public static Mat shadowsGammaCorrection(Mat mat, double gammaValue) {
        if(areEqual(gammaValue, 1)) return mat;

        Mat lookUpTable = getLUT(i -> Math.pow(i / 255.0, gammaValue) * 255.0);
        return applyLUT(mat, lookUpTable);
    }

    public static Mat highlightsGammaCorrection(Mat mat, double gammaValue) {
        if(areEqual(gammaValue, 1)) return mat;

        Mat lookUpTable = getLUT((i) -> -Math.pow((255-i)/255.0, gammaValue)*255.0 + 250);
        return applyLUT(mat, lookUpTable);
    }

    public static Mat applyLUT(Mat mat, Mat lut) {
        Mat img = new Mat(mat.size(), mat.type());
        Core.LUT(mat, lut, img);
        return img;
    }

    public static Mat applyLUTtoChannel(Mat mat, Mat lut, int channel) {
        List<Mat> channels = splitIntoChannels(mat);
        Mat target = channels.get(channel);
        Mat luted = new Mat(target.size(), target.type());
        Core.LUT(target, lut, luted);

        channels.remove(channel);
        channels.add(channel, luted);

        Mat rez = new Mat(mat.size(), mat.type());
        Core.merge(channels, rez);
        return rez;
    }

    public static Mat getLUT(DoubleUnaryOperator f) {
        Mat lookUpTable = new Mat(1, 256, CvType.CV_8U);
        byte[] lookUpTableData = new byte[(int) (lookUpTable.total()*lookUpTable.channels())];
        for (int i = 0; i < lookUpTable.cols(); i++) {
            lookUpTableData[i] = saturate(f.applyAsDouble(i));
        }
        lookUpTable.put(0, 0, lookUpTableData);
        return lookUpTable;
    }

    public static Mat resize(Mat mat, int width, int height) {
        int originalSize = mat.rows() * mat.cols();
        int newSize = width * height;

        int interpolation = newSize < originalSize ? Imgproc.INTER_AREA : Imgproc.INTER_CUBIC;
        Mat rmat = new Mat(height, width, mat.type());
        Imgproc.resize(mat, rmat, rmat.size(), 0, 0, interpolation);

        return rmat;
    }

    public static Mat scale(Mat mat, double factor) {
        int interpolation = factor < 1 ? Imgproc.INTER_AREA : Imgproc.INTER_CUBIC;
        Mat rmat = new Mat((int) round(mat.rows() * factor), (int) round(mat.cols() * factor), mat.type());
        Imgproc.resize(mat, rmat, new Size(), factor, factor, interpolation);
        return rmat;
    }

    public static List<Mat> splitIntoChannels(Mat mat) {
        List<Mat> channels = new ArrayList<>();
        for(int i = 0; i < mat.channels(); ++i) {
            channels.add(new Mat(mat.size(), mat.type()));
        }

        Core.split(mat, channels);
        return  channels;
    }

    public static List<double[]> splitInFloats(Mat mat) {
        List<Mat> channels = splitIntoChannels(mat);
        List<double[]> ret = new ArrayList<>();

        for(Mat m : channels) {
            ret.add(getFloats(m));
        }

        return  ret;
    }

    public static double[] getFloats(Mat mat) {
        if(mat.channels() > 1) mat = OpenCVUtility.toGrayscale(mat);
        byte[] datab = new byte[mat.cols()*mat.rows()*mat.channels()];
        double[] data = new double[mat.cols()*mat.rows()*mat.channels()];
        mat.get(0, 0, datab);

        for(int i = 0; i < datab.length; ++i) {
            data[i] = datab[i];
            if(data[i] < 0) data[i] += 256;
        }

        return data;
    }

    public static double[] getFloats2(Mat mat) {
        byte[] datab = new byte[mat.cols()*mat.rows()*mat.channels()];
        double[] data = new double[mat.cols()*mat.rows()*mat.channels()];
        mat.get(0, 0, datab);

        for(int i = 0; i < datab.length; ++i) {
            data[i] = datab[i];
            if(data[i] < 0) data[i] += 256;
        }

        return data;
    }

    public static double[] getFloatsOfChannel(Mat mat, int channel) {
        List<Mat> channels = splitIntoChannels(mat);

        return getFloats(channels.get(channel));
    }

    public static Mat HSVshift(Mat mat, double dh, double ds, double dv) {
        Mat ret = BGR2HSV(mat);
        Mat ret2 = new Mat(ret.size(), ret.type());

        Mat shift = new Mat(1, 3, CvType.CV_64F);
        shift.put(0, 0, dh, ds, dv);
        Core.add(ret, shift, ret2);

        return HSV2BGR(ret2);
    }

    public static Mat sepiaEffect(Mat mat) {
        Mat rez = new Mat(mat.size(), mat.type());
        Mat mat2 = mat;

        if(mat.channels() == 1) {
            mat2 = GRAY2BGR(mat);
        }

        Core.transform(mat2, rez, sepiaKernel);
        return rez;
    }

    public static Mat halfStepEffect(Mat mat) {
        Mat rez = new Mat(mat.size(), mat.type());
        Mat mat2 = mat;

        if(mat.channels() == 1) {
            mat2 = GRAY2BGR(mat);
        }

        Core.transform(mat2, rez, halfStepKernel);
        return rez;
    }

    public static Mat deepBlueEffect(Mat mat) {
        Mat rez = invertEffect(mat);
        rez = sepiaEffect(rez);
        rez = invertEffect(rez);
        return rez;
    }

    public static Mat invertEffect(Mat mat) {
        return applyLUT(mat, invertLut);
    }

    public static Mat tint(Mat mat, Color color) {
        if(mat.channels() == 1) {
            mat = GRAY2BGR(mat);
        }

        double[] colorHSV = getHSV(color);
        List<Mat> channels = splitIntoChannels(BGR2HSV(mat));

        channels.get(0).setTo(new Scalar(colorHSV[0]));
        channels.get(1).setTo(new Scalar(colorHSV[1]));

        Mat rez = new Mat(mat.size(), mat.type());
        Core.merge(channels, rez);

        return HSV2BGR(rez);
    }

    public static Mat gaussianBlur(Mat mat, int s) {
        if(s == 0) return mat;

        Mat rez = new Mat(mat.size(), mat.type());
        Size size = new Size(2*s-1, 2*s-1);
        Imgproc.GaussianBlur(mat, rez, size, 0, 0);
        return rez;
    }

    public static Mat cropImage(Mat mat, int xs, int ys, int xe, int ye) {
        Mat rez = mat.submat(ys, ye, xs, xe);
        return rez;
    }

    public static Mat cropImagePreview(Mat mat, int xs, int ys, int xe, int ye) {
        Mat rez = mat.clone();

        int width = mat.cols() - 1;
        int height = mat.rows() - 1;

        Rect top = new Rect(0, 0, width, ys);
        Rect bottom = new Rect(0, ye, width, height - ye+1);
        Rect left = new Rect(0, 0, xs, height);
        Rect right = new Rect(xe, 0, width - xe+1, height);

        Scalar color = new Scalar(127+10, 127+25, 127-10);
        Imgproc.rectangle(rez, top, color, Imgproc.FILLED);
        Imgproc.rectangle(rez, bottom, color, Imgproc.FILLED);
        Imgproc.rectangle(rez, left, color, Imgproc.FILLED);
        Imgproc.rectangle(rez, right, color, Imgproc.FILLED);

        return rez;
    }

    public static Mat luminanceRange(Mat mat, int d, int g){
        return applyLUT(mat, getLUT(x -> 255.0/(g-d)*x-d*255.0/(g-d)));
    }

    public static Mat shadowsMidsHighlights(Mat mat, int alfa, int beta, int gamma) {
        double[] xs = new double[] {0, 42-alfa, 85, 127-beta, 170, 212-gamma, 255};
        double[] ys = new double[] {0, 42+alfa, 85, 127+beta, 170, 212+gamma, 255};

        PolynomialSplineFunction f = SPLINE_INTERPOLATOR.interpolate(xs, ys);
        return applyLUT(mat, getLUT(f::value));
    }

    public static Mat rotateCCW90(Mat mat) {
        Size nsize = new Size(mat.rows(), mat.cols());
        Mat rez = new Mat(nsize, mat.type());
        Core.rotate(mat, rez, Core.ROTATE_90_COUNTERCLOCKWISE);
        return rez;
    }

    public static Mat rotateCW90(Mat mat) {
        Size nsize = new Size(mat.rows(), mat.cols());
        Mat rez = new Mat(nsize, mat.type());
        Core.rotate(mat, rez, Core.ROTATE_90_CLOCKWISE);
        return rez;
    }

    public static Mat unsharpMask(Mat mat, int r, double s)  {
        Mat rez = new Mat(mat.size(), mat.type());
        Mat blurred = gaussianBlur(mat, r);
        Core.addWeighted(mat, s+1, blurred, -s, 0, rez);
        return rez;
    }

    public static double[] getHSV(Color color) {
        Mat mat = new Mat(1, 1, CvType.CV_8UC3);
        mat.put(0,0, color.getBlue(), color.getGreen(), color.getRed());
        return getFloats2(BGR2HSV(mat));
    }

    public static Mat flipHorizontal(Mat mat) {
        Mat rez = new Mat(mat.size(), mat.type());
        Core.flip(mat, rez, 1);
        return rez;
    }

    public static Mat flipVertical(Mat mat) {
        Mat rez = new Mat(mat.size(), mat.type());
        Core.flip(mat, rez, 0);
        return rez;
    }

    public static Mat GRAY2BGR(Mat mat) {
        Mat mat2 = new Mat(mat.size(), CvType.CV_8UC3);
        Imgproc.cvtColor(mat, mat2, Imgproc.COLOR_GRAY2BGR);
        return mat2;
    }

    public static Mat GRAY2BGRA(Mat mat) {
        Mat mat2 = new Mat(mat.size(), CvType.CV_8UC3);
        Imgproc.cvtColor(mat, mat2, Imgproc.COLOR_GRAY2BGRA);
        return mat2;
    }

    public static Mat BGRA2GRAY(Mat mat) {
        Mat mat2 = new Mat(mat.size(), CvType.CV_8UC3);
        Imgproc.cvtColor(mat, mat2, Imgproc.COLOR_BGRA2GRAY);
        return mat2;
    }

    public static Mat BGR2HSV(Mat mat) {
        Mat ret = new Mat(mat.rows(), mat.cols(), CvType.CV_8UC3);
        Imgproc.cvtColor(mat, ret, Imgproc.COLOR_BGR2HSV);
        return ret;
    }

    public static Mat BGR2BGRA(Mat mat) {
        Mat ret = new Mat(mat.rows(), mat.cols(), CvType.CV_8UC3);
        Imgproc.cvtColor(mat, ret, Imgproc.COLOR_BGR2BGRA);
        return ret;
    }

    public static Mat BGRA2BGR(Mat mat) {
        Mat ret = new Mat(mat.rows(), mat.cols(), CvType.CV_8UC3);
        Imgproc.cvtColor(mat, ret, Imgproc.COLOR_BGRA2BGR);
        return ret;
    }

    public static Mat HSV2BGR(Mat mat) {
        Mat ret = new Mat(mat.rows(), mat.cols(), CvType.CV_8UC3);
        Imgproc.cvtColor(mat, ret, Imgproc.COLOR_HSV2BGR);
        return ret;
    }

    private static boolean areEqual(double v1, double v2) {
        return abs(v1 - v2) < EPS;
    }
}
