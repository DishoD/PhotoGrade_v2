package photograde.tools;

import org.opencv.core.Mat;
import photograde.PhotoGrade;
import photograde.util.IntervalValueConverter;
import photograde.util.OpenCVUtility;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ToolUnsharpMask extends JFrame {
    private static final int RADIUS_MIN_SLIDER = 2;
    private static final int RADIUS_MAX_SLIDER = 60;
    private static final int RADIUS_INITIAL_SLIDER = 7;

    private static final int STRENGTH_MIN_SLIDER = 0;
    private static final int STRENGTH_MAX_SLIDER = 1000;
    private static final int STRENGTH_INITIAL_SLIDER = 0;

    private static final int STRENGTH_MIN_ACTUAL = 0;
    private static final int STRENGTH_MAX_ACTUAL = 15;

    private static final IntervalValueConverter converterStrength = new IntervalValueConverter(STRENGTH_MIN_SLIDER, STRENGTH_MAX_SLIDER, STRENGTH_MIN_ACTUAL, STRENGTH_MAX_ACTUAL);

    private PhotoGrade pgref;
    private Mat baseImage;

    private JSlider slRadius = new JSlider(RADIUS_MIN_SLIDER, RADIUS_MAX_SLIDER, RADIUS_INITIAL_SLIDER);
    private JSlider slStrength = new JSlider(STRENGTH_MIN_SLIDER, STRENGTH_MAX_SLIDER, STRENGTH_INITIAL_SLIDER);

    private int radius = RADIUS_INITIAL_SLIDER;
    private double strength = converterStrength.convert(STRENGTH_INITIAL_SLIDER);

    private boolean isDisabled;

    public ToolUnsharpMask(PhotoGrade pgref) {
        this.pgref = pgref;
        this.isDisabled = true;
        this.setAlwaysOnTop(true);
        //this.setUndecorated(true);

        this.setTitle("Sharpen");
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cancel();
            }
        });

        JLabel l1 = new JLabel("Radius");
        JLabel l2 = new JLabel("Strength");

        l1.setHorizontalAlignment(JLabel.CENTER);
        l2.setHorizontalAlignment(JLabel.CENTER);

        JButton btnReset = new JButton("Reset");
        JButton btnApply = new JButton("Apply");
        JButton btnCancel = new JButton("Cancel");

        Container cp = this.getContentPane();
        JPanel p1 = new JPanel(new GridLayout(5, 1, 5, 5));
        JPanel p2 = new JPanel(new FlowLayout());

        p1.add(l1);
        p1.add(slRadius);
        p1.add(l2);
        p1.add(slStrength);
        p2.add(btnReset);
        p2.add(btnApply);
        p2.add(btnCancel);
        p1.add(p2);

        cp.add(p1);
        this.pack();

        //slRadius.setSnapToTicks(true);

        slRadius.addChangeListener(e -> {
            radius = slRadius.getValue();
            applyTool();
        });

        slStrength.addChangeListener(e -> {
            strength = converterStrength.convert(slStrength.getValue());
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
            pgref.setCurrentWorkingImage(OpenCVUtility.unsharpMask(baseImage, radius, strength));
        }
    }

    private void resetSliders() {
        slRadius.setValue(RADIUS_INITIAL_SLIDER);
        slStrength.setValue(STRENGTH_INITIAL_SLIDER);
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
