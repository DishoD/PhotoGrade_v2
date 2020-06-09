package photograde.tools;

import org.opencv.core.Mat;
import photograde.PhotoGrade;
import photograde.util.OpenCVUtility;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ToolShadowsAndHighlights extends JFrame {
    private static final int SHADOWS_MIN_SLIDER = -20;
    private static final int SHADOWS_MAX_SLIDER = 20;
    private static final int SHADOWS_INITIAL_SLIDER = 0;


    private static final int MIDS_MIN_SLIDER = -20;
    private static final int MIDS_MAX_SLIDER = 20;
    private static final int MIDS_INITIAL_SLIDER = 0;


    private static final int HIGHLIGHTS_MIN_SLIDER = -20;
    private static final int HIGHLIGHTS_MAX_SLIDER = 20;
    private static final int HIGHLIGHTS_INITIAL_SLIDER = 0;


    private PhotoGrade pgref;
    private Mat baseImage;

    private JSlider slShadows = new JSlider(SHADOWS_MIN_SLIDER, SHADOWS_MAX_SLIDER, SHADOWS_INITIAL_SLIDER);
    private JSlider slMids = new JSlider(MIDS_MIN_SLIDER, MIDS_MAX_SLIDER, MIDS_INITIAL_SLIDER);
    private JSlider slHighlights = new JSlider(HIGHLIGHTS_MIN_SLIDER, HIGHLIGHTS_MAX_SLIDER, HIGHLIGHTS_INITIAL_SLIDER);

    private int alfa = 0;
    private int beta = 0;
    private int gamma = 0;

    private boolean isDisabled;

    public ToolShadowsAndHighlights(PhotoGrade pgref) {
        this.pgref = pgref;
        this.isDisabled = true;
        this.setAlwaysOnTop(true);
        //this.setUndecorated(true);

        this.setTitle("Shadows & Highlights");
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cancel();
            }
        });

        JLabel l1 = new JLabel("Shadows");
        JLabel l2 = new JLabel("Midtones");
        JLabel l3 = new JLabel("Highlights");

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
        p1.add(slShadows);
        p1.add(l2);
        p1.add(slMids);
        p1.add(l3);
        p1.add(slHighlights);
        p2.add(btnReset);
        p2.add(btnApply);
        p2.add(btnCancel);
        p1.add(p2);

        cp.add(p1);
        this.pack();

        slShadows.addChangeListener(e -> {
            alfa = slShadows.getValue();
            applyTool();
        });

        slMids.addChangeListener(e -> {
            beta = slMids.getValue();
            applyTool();
        });

        slHighlights.addChangeListener(e -> {
            gamma = slHighlights.getValue();
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
            pgref.setCurrentWorkingImage(OpenCVUtility.shadowsMidsHighlights(baseImage, alfa, beta, gamma));
        }
    }

    private void resetSliders() {
        slShadows.setValue(SHADOWS_INITIAL_SLIDER);
        slMids.setValue(MIDS_INITIAL_SLIDER);
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
