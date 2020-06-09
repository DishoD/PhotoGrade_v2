package photograde.util;

public class IntervalValueConverter {
    private double amin, amax;
    private double bmin, bmax;

    private double arange, brange;
    private double ashift, bshift;

    public IntervalValueConverter(double amin, double amax, double bmin, double bmax) {
        this.amin = amin;
        this.amax = amax;
        this.bmin = bmin;
        this.bmax = bmax;

        arange = amax - amin;
        brange = bmax - bmin;

        ashift = -amin;
        bshift = bmin;
    }

    public double convert(double value) {
        if(value < amin || value > amax)
            throw new IllegalArgumentException("Value " + value + "is not in [" + amin + ", " + amax + "]");

        return ((value+ashift)/arange)*brange + bshift;
    }

    public double convertInverted(double value) {
        return bmax - convert(value) + bmin;
    }

    public static void main(String[] args) {
        IntervalValueConverter c = new IntervalValueConverter(-100, 100, 0, 4);

        System.out.println(c.convert(0));
        System.out.println(c.convert(-100));
        System.out.println(c.convert(100));
        System.out.println(c.convert(50));
        System.out.println(c.convert(-75));
        System.out.println(c.convert(33));
    }
}
