package photograde.tools;

import org.opencv.core.Mat;
import photograde.PhotoGrade;
import photograde.util.OpenCVUtility;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ToolCrop extends JFrame {
    private PhotoGrade pgref;
    private Mat baseImage;

    private JSlider slTop = new JSlider();
    private JSlider slBottom = new JSlider();
    private JSlider slLeft = new JSlider();
    private JSlider slRight = new JSlider();

    private int top;
    private int bottom;
    private int left;
    private int right;

    private int width;
    private int height;

    private boolean isDisabled;

    public ToolCrop(PhotoGrade pgref) {
        this.pgref = pgref;
        this.isDisabled = true;
        this.setAlwaysOnTop(true);
        //this.setUndecorated(true);

        this.setTitle("Crop");
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cancel();
            }
        });

        JLabel l1 = new JLabel("Top");
        JLabel l2 = new JLabel("Bottom");
        JLabel l3 = new JLabel("Left");
        JLabel l4 = new JLabel("Right");

        l1.setHorizontalAlignment(JLabel.CENTER);
        l2.setHorizontalAlignment(JLabel.CENTER);
        l3.setHorizontalAlignment(JLabel.CENTER);
        l4.setHorizontalAlignment(JLabel.CENTER);

        JButton btnReset = new JButton("Reset");
        JButton btnApply = new JButton("Apply");
        JButton btnCancel = new JButton("Cancel");

        Container cp = this.getContentPane();
        JPanel p1 = new JPanel(new GridLayout(9, 1, 5, 5));
        JPanel p2 = new JPanel(new FlowLayout());

        p1.add(l1);
        p1.add(slTop);
        p1.add(l2);
        p1.add(slBottom);
        p1.add(l3);
        p1.add(slLeft);
        p1.add(l4);
        p1.add(slRight);
        p2.add(btnReset);
        p2.add(btnApply);
        p2.add(btnCancel);
        p1.add(p2);

        cp.add(p1);
        this.pack();

        slTop.addChangeListener(e -> {
            if(slTop.getValue() >= bottom) {
                slTop.setValue(top);
                return;
            }

            top = slTop.getValue();
            applyTool();
        });

        slBottom.addChangeListener(e -> {
            if(slBottom.getValue() <= top) {
                slBottom.setValue(bottom);
                return;
            }

            bottom = slBottom.getValue();
            applyTool();
        });

        slLeft.addChangeListener(e -> {
            if(slLeft.getValue() >= right) {
                slLeft.setValue(left);
                return;
            }

            left = slLeft.getValue();
            applyTool();
        });

        slRight.addChangeListener(e -> {
            if(slRight.getValue() <= left) {
                slRight.setValue(right);
                return;
            }

            right = slRight.getValue();
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
            pgref.setCurrentWorkingImage(OpenCVUtility.cropImagePreview(baseImage, left, top, right, bottom));
        }
    }

    private void resetSliders() {
        slTop.setValue(0);
        slBottom.setValue(height);
        slLeft.setValue(0);
        slRight.setValue(width);
    }

    private void apply() {
        isDisabled = true;
        setVisible(false);
        pgref.setCurrentWorkingImage(OpenCVUtility.cropImage(baseImage, left, top, right, bottom));
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
        initializeSliders();
        resetSliders();
        this.isDisabled = false;
        this.setVisible(true);
        pgref.disableActions();
    }

    private void initializeSliders() {
        width = baseImage.cols() - 1;
        height = baseImage.rows() - 1;

        slTop.setMinimum(0);
        slTop.setMaximum(height);

        slBottom.setMinimum(0);
        slBottom.setMaximum(height);

        slLeft.setMinimum(0);
        slLeft.setMaximum(width);

        slRight.setMinimum(0);
        slRight.setMaximum(width);

    }
}
