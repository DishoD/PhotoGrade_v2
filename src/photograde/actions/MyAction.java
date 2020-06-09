package photograde.actions;

import photograde.PhotoGrade;

import javax.swing.*;

public abstract class MyAction extends AbstractAction {
    protected PhotoGrade pgref;

    public MyAction(String name, String accelerator, PhotoGrade pgref) {
        super(name);
        this.pgref = pgref;

        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(accelerator));
    }
}
