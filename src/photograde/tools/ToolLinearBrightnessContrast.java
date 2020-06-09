package photograde.tools;

import org.opencv.core.Mat;
import photograde.PhotoGrade;
import photograde.util.IntervalValueConverter;
import photograde.util.OpenCVUtility;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ToolLinearBrightnessContrast extends JFrame {
    private static final int CONTRAST_MIN_SLIDER = 0;
    private static final int CONTRAST_MAX_SLIDER = 300;
    private static final int CONTRAST_INITIAL_SLIDER = 100;

    private static final double CONTRAST_MIN_ACTUAL = 0;
    private static final double CONTRAST_MAX_ACTUAL = 3;

    private static final int BRIGHTNESS_MIN_SLIDER = -100;
    private static final int BRIGHTNESS_MAX_SLIDER = 100;
    private static final int BRIGHTNESS_INITIAL_SLIDER = 0;

    private static final int BRIGHTNESS_MIN_ACTUAL = -100;
    private static final int BRIGHTNESS_MAX_ACTUAL = 100;

    private static final IntervalValueConverter converterContrast = new IntervalValueConverter(CONTRAST_MIN_SLIDER, CONTRAST_MAX_SLIDER, CONTRAST_MIN_ACTUAL, CONTRAST_MAX_ACTUAL);
    private static final IntervalValueConverter converterBrightness = new IntervalValueConverter(BRIGHTNESS_MIN_SLIDER, BRIGHTNESS_MAX_SLIDER, BRIGHTNESS_MIN_ACTUAL, BRIGHTNESS_MAX_ACTUAL);

    private PhotoGrade pgref;
    private Mat baseImage;

    private JSlider slContrast = new JSlider(CONTRAST_MIN_SLIDER, CONTRAST_MAX_SLIDER, CONTRAST_INITIAL_SLIDER);
    private JSlider slBrightness = new JSlider(BRIGHTNESS_MIN_SLIDER, BRIGHTNESS_MAX_SLIDER, BRIGHTNESS_INITIAL_SLIDER);

    private double alpha = 1;
    private double beta = 0;

    private boolean isDisabled;

    public ToolLinearBrightnessContrast(PhotoGrade pgref) {
        this.pgref = pgref;
        this.isDisabled = true;
        this.setAlwaysOnTop(true);
        //this.setUndecorated(true);

        this.setTitle("Linear Brightness & Contrast");
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cancel();
            }
        });

        JLabel l1 = new JLabel("Contrast");
        JLabel l2 = new JLabel("Brightness");

        l1.setHorizontalAlignment(JLabel.CENTER);
        l2.setHorizontalAlignment(JLabel.CENTER);

        JButton btnReset = new JButton("Reset");
        JButton btnApply = new JButton("Apply");
        JButton btnCancel = new JButton("Cancel");

        Container cp = this.getContentPane();
        JPanel p1 = new JPanel(new GridLayout(5, 1, 5, 5));
        JPanel p2 = new JPanel(new FlowLayout());

        p1.add(l1);
        p1.add(slContrast);
        p1.add(l2);
        p1.add(slBrightness);
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
        if(!isDisabled) {
            pgref.setCurrentWorkingImage(OpenCVUtility.linearBrightnessAndContrast(baseImage, alpha, beta));
        }
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
