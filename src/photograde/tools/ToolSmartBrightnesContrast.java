package photograde.tools;

import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.opencv.core.Mat;
import photograde.PhotoGrade;
import photograde.util.IntervalValueConverter;
import photograde.util.OpenCVUtility;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import static java.lang.Math.abs;
import static java.lang.Math.min;

public class ToolSmartBrightnesContrast extends JFrame {
    private static final int CONTRAST_MIN_SLIDER = -640;
    private static final int CONTRAST_MAX_SLIDER = 640;
    private static final int CONTRAST_INITIAL_SLIDER = 0;

    private static final double CONTRAST_MIN_ACTUAL = -64;
    private static final double CONTRAST_MAX_ACTUAL = 64;

    private static final int BRIGHTNESS_MIN_SLIDER = -100;
    private static final int BRIGHTNESS_MAX_SLIDER = 100;
    private static final int BRIGHTNESS_INITIAL_SLIDER = 0;

    private static final int BRIGHTNESS_MIN_ACTUAL = -64;
    private static final int BRIGHTNESS_MAX_ACTUAL = 64;

    private static final IntervalValueConverter converterContrast = new IntervalValueConverter(CONTRAST_MIN_SLIDER, CONTRAST_MAX_SLIDER, CONTRAST_MIN_ACTUAL, CONTRAST_MAX_ACTUAL);
    private static final IntervalValueConverter converterBrightness = new IntervalValueConverter(BRIGHTNESS_MIN_SLIDER, BRIGHTNESS_MAX_SLIDER, BRIGHTNESS_MIN_ACTUAL, BRIGHTNESS_MAX_ACTUAL);

    private static final SplineInterpolator SPLINE_INTERPOLATOR = new SplineInterpolator();
    private static final LinearInterpolator LINEAR_INTERPOLATOR = new LinearInterpolator();

    private PhotoGrade pgref;
    private Mat baseImage;

    private JSlider slContrast = new JSlider(CONTRAST_MIN_SLIDER, CONTRAST_MAX_SLIDER, CONTRAST_INITIAL_SLIDER);
    private JSlider slBrightness = new JSlider(BRIGHTNESS_MIN_SLIDER, BRIGHTNESS_MAX_SLIDER, BRIGHTNESS_INITIAL_SLIDER);

    private boolean isDisabled;

    double alpha = 0;
    double beta = 0;

    public ToolSmartBrightnesContrast(PhotoGrade pgref) {
        this.pgref = pgref;
        this.isDisabled = true;
        this.setAlwaysOnTop(true);

        this.setTitle("Smart Brightness & Contrast");
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cancel();
            }
        });

        JLabel l1 = new JLabel("Brightness");
        JLabel l2 = new JLabel("Contrast");

        l1.setHorizontalAlignment(JLabel.CENTER);
        l2.setHorizontalAlignment(JLabel.CENTER);

        JButton btnReset = new JButton("Reset");
        JButton btnApply = new JButton("Apply");
        JButton btnCancel = new JButton("Cancel");

        Container cp = this.getContentPane();
        JPanel p1 = new JPanel(new GridLayout(5, 1, 5, 5));
        JPanel p2 = new JPanel(new FlowLayout());

        p1.add(l1);
        p1.add(slBrightness);
        p1.add(l2);
        p1.add(slContrast);
        p2.add(btnReset);
        p2.add(btnApply);
        p2.add(btnCancel);
        p1.add(p2);

        cp.add(p1);
        this.pack();

        slContrast.addChangeListener(e -> {
            alpha = converterContrast.convert(slContrast.getValue());
            applyTool();
        });

        slBrightness.addChangeListener(e -> {
            beta = converterBrightness.convert(slBrightness.getValue());
            applyTool();
        });

        btnReset.addActionListener(e -> {
            resetSliders();
        });

        btnApply.addActionListener(e -> {
            apply();
        });

        btnCancel.addActionListener(e -> {
            cancel();
        });
    }

    private void applyTool() {
        if(isDisabled) return;

        if(alpha > 0)
            increaseContrast();
        else
            decreaseContrast();
    }

    private void increaseContrast() {
        double alpha = min(abs(this.alpha), 62);
        double[] x = {0, 64 + alpha, 127, 191 - alpha, 255};
        double[] y = {0, 64 - alpha, 127, 191 + alpha, 255};

        PolynomialSplineFunction fc = SPLINE_INTERPOLATOR.interpolate(x, y);
        PolynomialSplineFunction fb = getBrightnessFunction();
        Mat lut = OpenCVUtility.getLUT(fc::value);
        lut = OpenCVUtility.applyLUT(lut, OpenCVUtility.getLUT(fb::value));
        pgref.setCurrentWorkingImage(OpenCVUtility.applyLUT(baseImage, lut));
    }

    private void decreaseContrast() {
        double alpha = abs(this.alpha);
        double[] x = {0        , 255        };
        double[] y = {0 + alpha, 255 - alpha};

        PolynomialSplineFunction fc = LINEAR_INTERPOLATOR.interpolate(x, y);
        PolynomialSplineFunction fb = getBrightnessFunction();
        Mat lut = OpenCVUtility.getLUT(fc::value);
        lut = OpenCVUtility.applyLUT(lut, OpenCVUtility.getLUT(fb::value));
        pgref.setCurrentWorkingImage(OpenCVUtility.applyLUT(baseImage, lut));
    }

    private PolynomialSplineFunction getBrightnessFunction() {
        double[] x = {-50, 0, 127 - beta, 255, 300};
        double[] y = {-50, 0, 127 + beta, 255, 300};
        return SPLINE_INTERPOLATOR.interpolate(x, y);
    }

    private void resetSliders() {
        slContrast.setValue(CONTRAST_INITIAL_SLIDER);
        slBrightness.setValue(BRIGHTNESS_INITIAL_SLIDER);
    }

    private void apply() {
        isDisabled = true;
        setVisible(false);
        pgref.pushImage();
        pgref.enableActions();
    }

    private void cancel() {
        isDisabled = true;
        setVisible(false);
        pgref.setCurrentWorkingImage(baseImage);
        pgref.enableActions();
    }

    public void start() {
        if(!isDisabled) return;
        this.baseImage = pgref.getCurrentWorkingImage().clone();
        resetSliders();
        this.isDisabled = false;
        this.setVisible(true);
        pgref.disableActions();
    }
}
