package photograde;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import photograde.actions.ActionOpen;
import photograde.actions.MyAction;
import photograde.tools.*;
import photograde.util.OpenCVUtility;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class PhotoGrade extends JFrame {
    private Mat originalImage;
    private Mat currentWorkingImage;
    private JLabel imageDisplay = new JLabel();

    private boolean isShowngOriginal = true;

    private PhotoChangeTracker photoChangeTracker = new PhotoChangeTracker();

    private ToolLinearBrightnessContrast toolLinearBrightnessContrast = new ToolLinearBrightnessContrast(this);
    private ToolGammaCorrection toolGammaCorrection = new ToolGammaCorrection(this);
    private ToolHistogramRGB toolHistogramRGB = new ToolHistogramRGB(this);
    private ToolHistogramLuma toolHistogramLuma = new ToolHistogramLuma(this);
    private ToolSmartBrightnesContrast toolSmartBrightnesContrast = new ToolSmartBrightnesContrast(this);
    private ToolHSV toolHSV = new ToolHSV(this);
    private ToolGaussianBlur toolGaussianBlur = new ToolGaussianBlur(this);
    private ToolUnsharpMask toolUnsharpMask = new ToolUnsharpMask(this);
    private ToolShadowsAndHighlights toolShadowsAndHighlights = new ToolShadowsAndHighlights(this);
    private ToolLevels toolLevels = new ToolLevels(this);
    private ToolCrop toolCrop = new ToolCrop(this);
    private ToolResize toolResize = new ToolResize(this);


    private Action actionOpen = new ActionOpen("Open", "O", this);
    private Action actionUndo = new MyAction("Undo", "control Z", this) {
        @Override
        public void actionPerformed(ActionEvent e) {
            photoChangeTracker.undo();
            showCurrentImge();
        }
    };
    private Action actionRedo = new MyAction("Redo", "control Y", this) {
        @Override
        public void actionPerformed(ActionEvent e) {
            photoChangeTracker.redo();
            showCurrentImge();
        }
    };
    private Action actionGrayscale = new MyAction("to Grayscale", "control G", this) {
        @Override
        public void actionPerformed(ActionEvent e) {
            if(getCurrentImage().channels() == 1) return;
            setCurrentWorkingImage(OpenCVUtility.toGrayscale(getCurrentImage()));
            pushImage();
        }
    };
    private Action actionShowOriginal = new MyAction("Toggle original/edited image", "Q", this) {
        @Override
        public void actionPerformed(ActionEvent e) {
            if(pgref.isShowngOriginal) {
                pgref.isShowngOriginal = false;
                pgref.repaintImage(pgref.currentWorkingImage);
            } else {
                pgref.isShowngOriginal = true;
                pgref.repaintImage(pgref.originalImage);
            }
        }
    };
    private Action actionLinearBrightnessAndContrast = new MyAction("Linear Brightness & Contrast", "control B", this) {
        @Override
        public void actionPerformed(ActionEvent e) {
            toolLinearBrightnessContrast.start();
        }
    };
    private Action actionGammaCorrection = new MyAction("Gamma Correction", "control W", this) {
        @Override
        public void actionPerformed(ActionEvent e) {
            toolGammaCorrection.start();
        }
    };
    private Action actionHistogramRGB = new MyAction("RGB Histogram", "control H", this) {
        @Override
        public void actionPerformed(ActionEvent e) {
            toolHistogramRGB.start();
        }
    };
    private Action actionHistogramLuma = new MyAction("Luminance Histogram", "control L", this) {
        @Override
        public void actionPerformed(ActionEvent e) {
            toolHistogramLuma.start();
        }
    };
    private Action actionSmartContrast = new MyAction("Smart Brightness & Contrast", "C", this) {
        @Override
        public void actionPerformed(ActionEvent e) {
            toolSmartBrightnesContrast.start();
        }
    };
    private Action actionHSV = new MyAction("HSV", "H", this) {
        @Override
        public void actionPerformed(ActionEvent e) {
            toolHSV.start();
        }
    };
    private Action actionSepia = new MyAction("Sepia", "S", this) {
        @Override
        public void actionPerformed(ActionEvent e) {
            setCurrentWorkingImage(OpenCVUtility.sepiaEffect(currentWorkingImage));
            pushImage();
        }
    };
    private Action actionHalfStep = new MyAction("Half step", "P", this) {
        @Override
        public void actionPerformed(ActionEvent e) {
            setCurrentWorkingImage(OpenCVUtility.halfStepEffect(currentWorkingImage));
            pushImage();
        }
    };
    private Action actionDeepBlue = new MyAction("Deep blue", "D", this) {
        @Override
        public void actionPerformed(ActionEvent e) {
            setCurrentWorkingImage(OpenCVUtility.deepBlueEffect(currentWorkingImage));
            pushImage();
        }
    };
    private Action actionInvert = new MyAction("Invert", "I", this) {
        @Override
        public void actionPerformed(ActionEvent e) {
            setCurrentWorkingImage(OpenCVUtility.invertEffect(currentWorkingImage));
            pushImage();
        }
    };
    private Action actionFlipHorizontal = new MyAction("Horizontal", null, this) {
        @Override
        public void actionPerformed(ActionEvent e) {
            setCurrentWorkingImage(OpenCVUtility.flipHorizontal(currentWorkingImage));
            pushImage();
        }
    };
    private Action actionFlipVertical = new MyAction("Vertical", null, this) {
        @Override
        public void actionPerformed(ActionEvent e) {
            setCurrentWorkingImage(OpenCVUtility.flipVertical(currentWorkingImage));
            pushImage();
        }
    };
    private Action actionTint = new MyAction("Tint", "T", this) {
        @Override
        public void actionPerformed(ActionEvent e) {
            Color color =  JColorChooser.showDialog(PhotoGrade.this, "Choose color", null);
            if(color == null) return;
            setCurrentWorkingImage(OpenCVUtility.tint(currentWorkingImage, color));
            pushImage();
        }
    };
    private Action actionBlur = new MyAction("Blur", "B", this) {
        @Override
        public void actionPerformed(ActionEvent e) {
            toolGaussianBlur.start();
        }
    };
    private Action actionSharpen = new MyAction("Sharpen", "U", this) {
        @Override
        public void actionPerformed(ActionEvent e) {
            toolUnsharpMask.start();
        }
    };
    private Action actionRotateCounterClockwise = new MyAction("Counter clockwise", "LEFT", this) {
        @Override
        public void actionPerformed(ActionEvent e) {
            setCurrentWorkingImage(OpenCVUtility.rotateCCW90(currentWorkingImage));
            pushImage();
        }
    };
    private Action actionRotateClockwise = new MyAction("Clockwise", "RIGHT", this) {
        @Override
        public void actionPerformed(ActionEvent e) {
            setCurrentWorkingImage(OpenCVUtility.rotateCW90(currentWorkingImage));
            pushImage();
        }
    };
    private Action actionShadowsAndHighlights = new MyAction("Shadows & Highlights", "control A", this) {
        @Override
        public void actionPerformed(ActionEvent e) {
            toolShadowsAndHighlights.start();
        }
    };
    private Action actionLevels = new MyAction("Levels", "L", this) {
        @Override
        public void actionPerformed(ActionEvent e) {
            toolLevels.start();
        }
    };
    private Action actionCrop = new MyAction("Crop", "R", this) {
        @Override
        public void actionPerformed(ActionEvent e) {
            toolCrop.start();
        }
    };
    private Action actionResize = new MyAction("Resize", "X", this) {
        @Override
        public void actionPerformed(ActionEvent e) {
            toolResize.start();
        }
    };

    private List<Action> exportActions = new ArrayList<>();
    private List<Action> actions = new ArrayList<>();

    public PhotoGrade() {
        setUpWindow();
        initializeComponents();
        initializeMenues();
        initializeListeners();

        collectActions();
        disableActions();
        actionShowOriginal.setEnabled(false);
        actionHistogramRGB.setEnabled(false);
        actionHistogramLuma.setEnabled(false);
    }

    private void collectActions() {
        actions.add(actionUndo);
        actions.add(actionRedo);
        actions.add(actionGrayscale);
        actions.add(actionLinearBrightnessAndContrast);
        actions.add(actionGammaCorrection);
        actions.add(actionSmartContrast);
        actions.add(actionHSV);
        actions.add(actionSepia);
        actions.add(actionHalfStep);
        actions.add(actionDeepBlue);
        actions.add(actionInvert);
        actions.add(actionFlipHorizontal);
        actions.add(actionFlipVertical);
        actions.add(actionTint);
        actions.add(actionBlur);
        actions.add(actionSharpen);
        actions.add(actionShadowsAndHighlights);
        actions.add(actionRotateCounterClockwise);
        actions.add(actionRotateClockwise);
        actions.add(actionLevels);
        actions.add(actionCrop);
        actions.add(actionResize);
        actions.addAll(exportActions);
    }

    public void disableActions() {
        actions.forEach(a -> a.setEnabled(false));
    }

    public void enableActions() {
        actions.forEach(a -> a.setEnabled(true));
    }

    private void initializeListeners() {
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if(currentWorkingImage == null) return;
                repaintImage(currentWorkingImage);
            }
        });
    }

    private void initializeMenues() {
        JMenuBar menuBar = new JMenuBar();

        JMenu menuFile = new JMenu("File");
        JMenu menuEdit = new JMenu("Edit");
        JMenu menuLuma = new JMenu("Luma");
        JMenu menuColor = new JMenu("Color");
        JMenu menuFilter = new JMenu("Filter");
        JMenu menuEffects = new JMenu("Effects");
        JMenu menuAnalyze = new JMenu("Analyze");


        JMenu menuSave = new JMenu("Save");
        JMenu menuFlip = new JMenu("Flip");
        JMenu menuRotate = new JMenu("Rotate");

        menuBar.add(menuFile);
        menuBar.add(menuEdit);
        menuBar.add(menuLuma);
        menuBar.add(menuColor);
        menuBar.add(menuFilter);
        menuBar.add(menuEffects);
        menuBar.add(menuAnalyze);

        addExportAction("png");
        addExportAction("jpg");
        addExportAction("jpeg");
        addExportAction("gif");
        addExportAction("bmp");

        //file
        menuFile.add(actionOpen);
        menuFile.add(menuSave);
            //save
            exportActions.forEach(menuSave::add);

        //edit
        menuEdit.add(actionUndo);
        menuEdit.add(actionRedo);
        menuEdit.add(actionResize);
        menuEdit.add(actionCrop);
        menuEdit.add(menuFlip);
            //flip
            menuFlip.add(actionFlipHorizontal);
            menuFlip.add(actionFlipVertical);
        menuEdit.add(menuRotate);
            //rotate
            menuRotate.add(actionRotateCounterClockwise);
            menuRotate.add(actionRotateClockwise);

        //luma
        menuLuma.add(actionLinearBrightnessAndContrast);
        menuLuma.add(actionSmartContrast);
        menuLuma.add(actionShadowsAndHighlights);
        menuLuma.add(actionLevels);
        menuLuma.add(actionGammaCorrection);

        //color
        menuColor.add(actionHSV);
        menuColor.add(actionTint);
        menuColor.add(actionGrayscale);

        //filter
        menuFilter.add(actionSharpen);
        menuFilter.add(actionBlur);

        //effects
        menuEffects.add(actionSepia);
        //menuEffects.add(actionHalfStep);
        menuEffects.add(actionDeepBlue);
        menuEffects.add(actionInvert);

        //analyze
        menuAnalyze.add(actionShowOriginal);
        menuAnalyze.add(actionHistogramRGB);
        menuAnalyze.add(actionHistogramLuma);

        this.setJMenuBar(menuBar);
    }

    private void initializeComponents() {
        Container cp = this.getContentPane();
        cp.add(imageDisplay, BorderLayout.CENTER);
        imageDisplay.setHorizontalAlignment(JLabel.CENTER);
        imageDisplay.setVerticalAlignment(JLabel.CENTER);
    }

    public void pushImage() {
        photoChangeTracker.pushImage(getCurrentWorkingImage());
        showCurrentImge();
    }

    private void showCurrentImge() {
        this.setCurrentWorkingImage(photoChangeTracker.getCurrentImage());
    }

    public void setCurrentWorkingImage(Mat currentWorkingImage) {
        this.currentWorkingImage = currentWorkingImage;
        this.repaintImage(currentWorkingImage);
        this.isShowngOriginal = false;
    }

    public void loadNewImage(Mat image) {
        this.originalImage = image;
        photoChangeTracker.initialize(image);
        showCurrentImge();
        enableActions();
        actionShowOriginal.setEnabled(true);
        actionHistogramRGB.setEnabled(true);
        actionHistogramLuma.setEnabled(true);
    }

    private void repaintImage(Mat img) {
        img = resizeToFitWindow(img);
        imageDisplay.setIcon(new ImageIcon(OpenCVUtility.MatToBufferedImage(img)));
        repaintHistograms();
    }

    private void repaintHistograms() {
        toolHistogramRGB.repaintHist();
        toolHistogramLuma.repaintHist();
    }

    private Mat resizeToFitWindow(Mat mat) {
        int width1 = this.getWidth();
        int height1 = this.getHeight();
        int width2 = mat.cols();
        int height2 = mat.rows();

        double factor = 1;
        if(width2 > width1) factor *= (double)width1/width2;
        height2 *= factor;
        if(height2 > height1) factor *= (double)height1/height2;

        return OpenCVUtility.scale(mat, factor);
    }

    private void setUpWindow() {
        this.setTitle("PhotoGrade");
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setSize(400, 400);
        this.setLocation(400, 200);
    }

    public Mat getOriginalImage() {
        return originalImage;
    }

    public boolean isShowngOriginal() {
        return isShowngOriginal;
    }

    public Mat getCurrentImage() {
        return photoChangeTracker.getCurrentImage();
    }

    public Mat getCurrentWorkingImage() {
        return currentWorkingImage;
    }

    private void saveAs(String extension) {
        Path savePath;

        JFileChooser fc = new JFileChooser("../rektorova nagrada/slike");
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
            return;

        savePath = Paths.get(fc.getSelectedFile().getAbsolutePath());
        if(Files.exists(savePath)) {
            int res = JOptionPane.showConfirmDialog(this,
                    "Do you wish to overwrite the choosen file??");
            if (res != JOptionPane.YES_OPTION) {
                return;
            }
        }
        if (!savePath.toString().endsWith("." + extension)) {
            savePath = Paths.get(savePath.toString() + "." + extension);
        }

        File file = savePath.toFile();
        try {
            ImageIO.write(OpenCVUtility.MatToBufferedImage(currentWorkingImage), extension, file);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error occured while saving the file.");
        }
    }

    private class ExportAction extends MyAction {
        private String exstension;

        public ExportAction(String exstension, PhotoGrade pgref) {
            super(exstension, null, pgref);
            this.exstension = exstension;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            PhotoGrade.this.saveAs(exstension);
        }
    }

    private void addExportAction(String exstension) {
        exportActions.add(new ExportAction(exstension, this));
    }

    public static void main(String[] args){
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        SwingUtilities.invokeLater(() -> {
            new PhotoGrade().setVisible(true);
        });

    }


}
