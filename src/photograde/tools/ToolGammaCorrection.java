package photograde.tools;

import org.opencv.core.Mat;
import photograde.PhotoGrade;
import photograde.util.IntervalValueConverter;
import photograde.util.OpenCVUtility;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import static java.lang.Math.exp;

public class ToolGammaCorrection extends JFrame {
    private static final int SHADOWS_MIN_SLIDER = -100;
    private static final int SHADOWS_MAX_SLIDER = 100;
    private static final int SHADOWS_INITIAL_SLIDER = 0;

    private static final double SHADOWS_MIN_ACTUAL = -3.5;
    private static final double SHADOWS_MAX_ACTUAL = +3.5;

    private static final int HIGHLIGHTS_MIN_SLIDER = -100;
    private static final int HIGHLIGHTS_MAX_SLIDER = 100;
    private static final int HIGHLIGHTS_INITIAL_SLIDER = 0;

    private static final double HIGHLIGHTS_MIN_ACTUAL = -3.5;
    private static final double HIGHLIGHTS_MAX_ACTUAL = +3.5;

    private static final IntervalValueConverter converterShadows = new IntervalValueConverter(SHADOWS_MIN_SLIDER, SHADOWS_MAX_SLIDER, SHADOWS_MIN_ACTUAL, SHADOWS_MAX_ACTUAL);
    private static final IntervalValueConverter converterHighlights = new IntervalValueConverter(HIGHLIGHTS_MIN_SLIDER, HIGHLIGHTS_MAX_SLIDER, HIGHLIGHTS_MIN_ACTUAL, HIGHLIGHTS_MAX_ACTUAL);

    private PhotoGrade pgref;
    private Mat baseImage;

    private JSlider slShadows = new JSlider(SHADOWS_MIN_SLIDER, SHADOWS_MAX_SLIDER, SHADOWS_INITIAL_SLIDER);
    private JSlider slHighlights = new JSlider(HIGHLIGHTS_MIN_SLIDER, HIGHLIGHTS_MAX_SLIDER, HIGHLIGHTS_INITIAL_SLIDER);

    private double shadowsGammaValue = 1;
    private double highlightsGammaValue = 1;

    private boolean isDisabled;

    public ToolGammaCorrection(PhotoGrade pgref) {
        this.pgref = pgref;
        this.isDisabled = true;
        this.setAlwaysOnTop(true);
        //this.setUndecorated(true);

        this.setTitle("Gamma Correction");
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cancel();
            }
        });

        JLabel l1 = new JLabel("Gamma");
        //JLabel l2 = new JLabel("Highlights");

        l1.setHorizontalAlignment(JLabel.CENTER);
        //l2.setHorizontalAlignment(JLabel.CENTER);

        JButton btnReset = new JButton("Reset");
        JButton btnApply = new JButton("Apply");
        JButton btnCancel = new JButton("Cancel");

        Container cp = this.getContentPane();
        JPanel p1 = new JPanel(new GridLayout(3, 1, 5, 5));
        JPanel p2 = new JPanel(new FlowLayout());

        p1.add(l1);
        p1.add(slShadows);
        //p1.add(l2);
        //p1.add(slHighlights);
        p2.add(btnReset);
        p2.add(btnApply);
        p2.add(btnCancel);
        p1.add(p2);

        cp.add(p1);
        this.pack();

        slShadows.addChangeListener(e -> {
            shadowsGammaValue = exp(-converterShadows.convert(slShadows.getValue()));
            applyTool();
        });

        slHighlights.addChangeListener(e -> {
            highlightsGammaValue = exp(converterHighlights.convert(slHighlights.getValue()));
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
            Mat s = OpenCVUtility.shadowsGammaCorrection(baseImage, shadowsGammaValue);
            pgref.setCurrentWorkingImage(s);
        }
    }


    private void resetSliders() {
        slShadows.setValue(SHADOWS_INITIAL_SLIDER);
        slHighlights.setValue(HIGHLIGHTS_INITIAL_SLIDER);
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
