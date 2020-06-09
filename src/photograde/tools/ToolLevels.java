package photograde.tools;

import org.opencv.core.Mat;
import photograde.PhotoGrade;
import photograde.util.OpenCVUtility;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ToolLevels extends JFrame {
    private static final int BLACKS_MIN_SLIDER = 0;
    private static final int BLACKS_MAX_SLIDER = 255;
    private static final int BLACKS_INITIAL_SLIDER = 0;

    private static final int WHITES_MIN_SLIDER = 0;
    private static final int WHITES_MAX_SLIDER = 255;
    private static final int WHITES_INITIAL_SLIDER = 255;

    private PhotoGrade pgref;
    private Mat baseImage;

    private JSlider slBlacks = new JSlider(BLACKS_MIN_SLIDER, BLACKS_MAX_SLIDER, BLACKS_INITIAL_SLIDER);
    private JSlider slWhites = new JSlider(WHITES_MIN_SLIDER, WHITES_MAX_SLIDER, WHITES_INITIAL_SLIDER);

    private int blackLevel = BLACKS_INITIAL_SLIDER;
    private int whiteLevel = WHITES_INITIAL_SLIDER;

    private boolean isDisabled;

    public ToolLevels(PhotoGrade pgref) {
        this.pgref = pgref;
        this.isDisabled = true;
        this.setAlwaysOnTop(true);
        //this.setUndecorated(true);

        this.setTitle("Levels");
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cancel();
            }
        });

        JLabel l1 = new JLabel("Blacks");
        JLabel l2 = new JLabel("Whites");

        l1.setHorizontalAlignment(JLabel.CENTER);
        l2.setHorizontalAlignment(JLabel.CENTER);

        JButton btnReset = new JButton("Reset");
        JButton btnApply = new JButton("Apply");
        JButton btnCancel = new JButton("Cancel");

        Container cp = this.getContentPane();
        JPanel p1 = new JPanel(new GridLayout(5, 1, 5, 5));
        JPanel p2 = new JPanel(new FlowLayout());

        p1.add(l1);
        p1.add(slBlacks);
        p1.add(l2);
        p1.add(slWhites);
        p2.add(btnReset);
        p2.add(btnApply);
        p2.add(btnCancel);
        p1.add(p2);

        cp.add(p1);
        this.pack();

        slBlacks.addChangeListener(e -> {
            blackLevel = slBlacks.getValue();
            applyTool();
        });

        slWhites.addChangeListener(e -> {
            whiteLevel = slWhites.getValue();
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
            pgref.setCurrentWorkingImage(OpenCVUtility.luminanceRange(baseImage, blackLevel, whiteLevel));
        }
    }

    private void resetSliders() {
        slBlacks.setValue(BLACKS_INITIAL_SLIDER);
        slWhites.setValue(WHITES_INITIAL_SLIDER);
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
