package photograde.tools;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.statistics.HistogramDataset;
import org.opencv.core.Mat;
import photograde.PhotoGrade;
import photograde.util.OpenCVUtility;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ToolHistogramLuma extends JFrame {
    private PhotoGrade pgref;

    private boolean disabled = true;

    public ToolHistogramLuma(PhotoGrade pgref) {
        this.pgref = pgref;
        this.setAlwaysOnTop(true);
        this.setTitle("Luminance Histogram");


        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                disabled = true;
                setVisible(false);
            }
        });

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                setPreferredSize(getSize());
            }
        });

    }

    public void repaintHist() {
        if(disabled) return;

        Mat mat = pgref.getCurrentWorkingImage();
        if(pgref.isShowngOriginal()) mat = pgref.getOriginalImage();

        JFreeChart hist = getLumaHistogram(mat);

        JPanel p = new ChartPanel(hist);
        this.setContentPane(p);
        this.pack();
    }

    private JFreeChart getLumaHistogram(Mat mat) {
        double[] luma = OpenCVUtility.getFloats(mat);

        HistogramDataset dataset = new HistogramDataset();
        dataset.addSeries("Luminance", luma, 256, 0, 255);

        JFreeChart hist = ChartFactory.createHistogram(null, null, null, dataset, PlotOrientation.VERTICAL, true, true, false);

        XYPlot plot = (XYPlot) hist.getPlot();
        XYBarRenderer renderer = (XYBarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardXYBarPainter());

        Paint[] paintArray = {
                new Color(0x70000000, true)
        };
        plot.setDrawingSupplier(new DefaultDrawingSupplier(
                paintArray,
                DefaultDrawingSupplier.DEFAULT_FILL_PAINT_SEQUENCE,
                DefaultDrawingSupplier.DEFAULT_OUTLINE_PAINT_SEQUENCE,
                DefaultDrawingSupplier.DEFAULT_STROKE_SEQUENCE,
                DefaultDrawingSupplier.DEFAULT_OUTLINE_STROKE_SEQUENCE,
                DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE));

        return hist;
    }

    public void start() {
        if(pgref.getCurrentWorkingImage() == null) return;
        disabled = false;
        this.setVisible(true);
        this.repaintHist();
    }
}
