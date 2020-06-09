package photograde;

import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;

public class PhotoChangeTracker {
    private List<Mat> stack = new ArrayList<>();
    private int index = -1;

    public void pushImage(Mat mat) {
        removeAfter(index);
        stack.add(mat);
        index++;
    }

    public void setCurrentImage(Mat mat) {
        removeAfter(index-1);
        stack.add(mat);
    }

    public boolean undo() {
        if(index == 0) return false;
        index--;
        return true;
    }

    public boolean redo() {
        if(index == stack.size() - 1) return false;
        index++;
        return true;
    }

    public Mat getCurrentImage() {
        return stack.get(index);
    }

    public void initialize(Mat mat) {
        stack.clear();
        stack.add(mat);
        index = 0;
    }

    private void removeAfter(int a) {
        for(int i = stack.size() -1; i > a; --i) {
            stack.remove(i);
        }
    }
}
