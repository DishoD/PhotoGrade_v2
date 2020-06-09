package photograde.tools;

import org.opencv.core.Mat;
import photograde.PhotoGrade;
import photograde.util.OpenCVUtility;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ToolGaussianBlur extends JFrame {
    private static final int BLUR_MIN_SLIDER = 0;
    private static final int BLUR_MAX_SLIDER = 30;
    private static final int BLUR_INITIAL_SLIDER = 0;

    private PhotoGrade pgref;
    private Mat baseImage;

    private JSlider slBlur = new JSlider(BLUR_MIN_SLIDER, BLUR_MAX_SLIDER, BLUR_INITIAL_SLIDER);

    private int blurStrength = 0;

    private boolean isDisabled;

    public ToolGaussianBlur(PhotoGrade pgref) {
        this.pgref = pgref;
        this.isDisabled = true;
        this.setAlwaysOnTop(true);
        //this.setUndecorated(true);

        this.setTitle("Gaussian Blur");
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cancel();
            }
        });

        JLabel l1 = new JLabel("Strength");

        l1.setHorizontalAlignment(JLabel.CENTER);

        JButton btnReset = new JButton("Reset");
        JButton btnApply = new JButton("Apply");
        JButton btnCancel = new JButton("Cancel");

        Container cp = this.getContentPane();
        JPanel p1 = new JPanel(new GridLayout(3, 1, 5, 5));
        JPanel p2 = new JPanel(new FlowLayout());

        p1.add(l1);
        p1.add(slBlur);
        p2.add(btnReset);
        p2.add(btnApply);
        p2.add(btnCancel);
        p1.add(p2);

        cp.add(p1);
        this.pack();

        slBlur.addChangeListener(e -> {
            blurStrength = slBlur.getValue();
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
            pgref.setCurrentWorkingImage(OpenCVUtility.gaussianBlur(baseImage, blurStrength));
        }
    }

    private void resetSliders() {
        slBlur.setValue(BLUR_INITIAL_SLIDER);
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
