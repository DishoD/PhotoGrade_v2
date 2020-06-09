package photograde.tools;

import org.opencv.core.Mat;
import photograde.PhotoGrade;
import photograde.util.OpenCVUtility;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ToolResize extends JFrame {
    private PhotoGrade pgref;
    private Mat baseImage;

    private int width;
    private int height;
    private double aspectRatio;
    private double aspectRationInverse;

    private JLabel info = new JLabel();
    private JTextField tWidth = new JTextField();
    private JTextField tHeight = new JTextField();
    private JCheckBox checkBox = new JCheckBox("Keep aspect ratio", true);

    private boolean isDisabled;

    public ToolResize(PhotoGrade pgref) {
        this.pgref = pgref;
        this.isDisabled = true;
        this.setAlwaysOnTop(true);

        this.setTitle("Resize");
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cancel();
            }
        });


        JButton btnReset = new JButton("Reset");
        JButton btnApply = new JButton("Apply");
        JButton btnCancel = new JButton("Cancel");

        Container cp = this.getContentPane();
        JPanel p1 = new JPanel(new GridLayout(5, 1, 5, 5));
        JPanel p2 = new JPanel(new FlowLayout());
        JPanel widthPanel = new JPanel(new FlowLayout());
        JPanel heightPanel = new JPanel(new FlowLayout());

        widthPanel.add(new JLabel("Width: "));
        widthPanel.add(tWidth);

        heightPanel.add(new JLabel("Height: "));
        heightPanel.add(tHeight);

        tWidth.setPreferredSize(new Dimension(100, tWidth.getPreferredSize().height));
        tHeight.setPreferredSize(new Dimension(100, tHeight.getPreferredSize().height));


        p1.add(info);
        p1.add(widthPanel);
        p1.add(heightPanel);
        p1.add(checkBox);
        p2.add(btnReset);
        p2.add(btnApply);
        p2.add(btnCancel);
        p1.add(p2);

        cp.add(p1);
        this.pack();

        tWidth.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                widthUpdated();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                widthUpdated();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                widthUpdated();
            }
        });

        tHeight.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                heightUpdated();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                heightUpdated();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                heightUpdated();
            }
        });

        checkBox.addActionListener(e -> {
            if(checkBox.isSelected()) {
                widthUpdated();
                heightUpdated();
            }
        });

        btnReset.addActionListener(e -> {
            resetFields();
        });

        btnApply.addActionListener(e -> {
            apply();
        });

        btnCancel.addActionListener(e -> {
            cancel();
        });
    }

    private void widthUpdated() {
        if(!checkBox.isSelected()) return;

        int width;
        try {
            width = Integer.parseInt(tWidth.getText().trim());
        } catch (NumberFormatException e) {
            return;
        }

        int height = (int)Math.round(aspectRationInverse*width);
        boolean flag = checkBox.isSelected();
        checkBox.setSelected(false);

        tHeight.setText(""+height);

        checkBox.setSelected(flag);
    }

    private void heightUpdated() {
        if(!checkBox.isSelected()) return;

        int height;
        try {
            height = Integer.parseInt(tHeight.getText().trim());
        } catch (NumberFormatException e) {
            return;
        }

        int width = (int)Math.round(aspectRatio*height);
        boolean flag = checkBox.isSelected();
        checkBox.setSelected(false);

        tWidth.setText(""+width);

        checkBox.setSelected(flag);
    }

    private void resetFields() {
        boolean flag = checkBox.isSelected();
        checkBox.setSelected(false);
        tWidth.setText(""+width);
        tHeight.setText(""+height);
        checkBox.setSelected(flag);
    }

    private void apply() {
        int width, height;
        try {
            width = Integer.parseInt(tWidth.getText().trim());
            height = Integer.parseInt(tHeight.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid width or height", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        pgref.setCurrentWorkingImage(OpenCVUtility.resize(baseImage, width, height));
        pgref.pushImage();

        isDisabled = true;
        setVisible(false);
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

        width = baseImage.cols();
        height = baseImage.rows();

        aspectRatio = (double)width/height;
        aspectRationInverse = (double)height/width;

        info.setText(String.format("Original size: %dpx × %dpx", width, height));

        resetFields();
        this.isDisabled = false;
        this.setVisible(true);
        pgref.disableActions();
    }
}
