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
import java.util.List;

public class ToolHistogramRGB extends JFrame {
    private PhotoGrade pgref;

    private boolean disabled = true;

    public ToolHistogramRGB(PhotoGrade pgref) {
        this.pgref = pgref;
        this.setAlwaysOnTop(true);
        this.setTitle("RGB Histogram");

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

        JFreeChart hist = mat.channels() == 1 ? getLumaHistogram(mat) : getRGBHistogram(mat);

        JPanel p = new ChartPanel(hist);
        this.setContentPane(p);
        this.pack();
    }

    private JFreeChart getRGBHistogram(Mat mat) {
        double maxRange = mat.total()/32;
        List<double[]> channels = OpenCVUtility.splitInFloats(mat);

        HistogramDataset dataset = new HistogramDataset();
        dataset.addSeries("Red", channels.get(2), 256, 0, 255);
        dataset.addSeries("Green", channels.get(1), 256, 0, 255);
        dataset.addSeries("Blue", channels.get(0), 256, 0, 255);

        JFreeChart hist = ChartFactory.createHistogram(null, null, null, dataset, PlotOrientation.VERTICAL, true, true, false);

        XYPlot plot = (XYPlot) hist.getPlot();
        XYBarRenderer renderer = (XYBarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardXYBarPainter());

        Paint[] paintArray = {
                new Color(0x70FF0000, true),
                new Color(0x7000FF00, true),
                new Color(0x700000FF, true)
        };
        plot.setDrawingSupplier(new DefaultDrawingSupplier(
                paintArray,
                DefaultDrawingSupplier.DEFAULT_FILL_PAINT_SEQUENCE,
                DefaultDrawingSupplier.DEFAULT_OUTLINE_PAINT_SEQUENCE,
                DefaultDrawingSupplier.DEFAULT_STROKE_SEQUENCE,
                DefaultDrawingSupplier.DEFAULT_OUTLINE_STROKE_SEQUENCE,
                DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE));

        plot.getRangeAxis().setRange(0, maxRange);

        return hist;
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
