package photograde.tools;

import org.opencv.core.Mat;
import photograde.PhotoGrade;
import photograde.util.OpenCVUtility;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ToolHSV extends JFrame {
    private static final int HUE_MIN_SLIDER = -180;
    private static final int HUE_MAX_SLIDER = 180;
    private static final int HUE_INITIAL_SLIDER = 0;


    private static final int SATURATION_MIN_SLIDER = -180;
    private static final int SATURATION_MAX_SLIDER = 180;
    private static final int SATURATION_INITIAL_SLIDER = 0;


    private static final int VALUE_MIN_SLIDER = -180;
    private static final int VALUE_MAX_SLIDER = 180;
    private static final int VALUE_INITIAL_SLIDER = 0;

    private PhotoGrade pgref;
    private Mat baseImage;

    private JSlider slHue = new JSlider(HUE_MIN_SLIDER, HUE_MAX_SLIDER, HUE_INITIAL_SLIDER);
    private JSlider slSaturation = new JSlider(SATURATION_MIN_SLIDER, SATURATION_MAX_SLIDER, SATURATION_INITIAL_SLIDER);
    private JSlider slValue = new JSlider(VALUE_MIN_SLIDER, VALUE_MAX_SLIDER, VALUE_INITIAL_SLIDER);

    private int deltaHue = 0;
    private int deltaSaturation = 0;
    private int deltaValue = 0;

    private boolean isDisabled;

    public ToolHSV(PhotoGrade pgref) {
        this.pgref = pgref;
        this.isDisabled = true;
        this.setAlwaysOnTop(true);

        this.setTitle("HSV");
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cancel();
            }
        });

        JLabel l1 = new JLabel("Hue");
        JLabel l2 = new JLabel("Saturation");
        JLabel l3 = new JLabel("Value");

        l1.setHorizontalAlignment(JLabel.CENTER);
        l2.setHorizontalAlignment(JLabel.CENTER);
        l3.setHorizontalAlignment(JLabel.CENTER);

        JButton btnReset = new JButton("Reset");
        JButton btnApply = new JButton("Apply");
        JButton btnCancel = new JButton("Cancel");

        Container cp = this.getContentPane();
        JPanel p1 = new JPanel(new GridLayout(7, 1, 5, 5));
        JPanel p2 = new JPanel(new FlowLayout());

        p1.add(l1);
        p1.add(slHue);
        p1.add(l2);
        p1.add(slSaturation);
        p1.add(l3);
        p1.add(slValue);
        p2.add(btnReset);
        p2.add(btnApply);
        p2.add(btnCancel);
        p1.add(p2);

        cp.add(p1);
        this.pack();

        slHue.addChangeListener(e -> {
            deltaHue = slHue.getValue();
            applyTool();
        });

        slSaturation.addChangeListener(e -> {
            deltaSaturation = slSaturation.getValue();
            applyTool();
        });

        slValue.addChangeListener(e -> {
            deltaValue = slValue.getValue();
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
            if(baseImage.channels() == 1) return;
            pgref.setCurrentWorkingImage(OpenCVUtility.HSVshift(baseImage, deltaHue, deltaSaturation, deltaValue));
        }
    }

    private void resetSliders() {
        slHue.setValue(HUE_INITIAL_SLIDER);
        slSaturation.setValue(SATURATION_INITIAL_SLIDER);
        slValue.setValue(VALUE_INITIAL_SLIDER);
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
