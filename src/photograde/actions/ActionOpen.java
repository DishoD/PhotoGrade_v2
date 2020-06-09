package photograde.actions;

import photograde.PhotoGrade;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.io.IOException;

import static photograde.util.OpenCVUtility.loadImage;

public class ActionOpen extends MyAction {

    public ActionOpen(String name, String accelerator, PhotoGrade pgref) {
        super(name, accelerator, pgref);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JFileChooser jfc = new JFileChooser("./resources/sample-images");
        jfc.setFileFilter(new FileNameExtensionFilter("Image file", "png", "jpg", "jpeg", "bmp", "wbmp", "gif", "tif"));
        if(jfc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            try {
                pgref.loadNewImage(loadImage(jfc.getSelectedFile()));
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(
                        pgref,
                        "Error loading image.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
}
