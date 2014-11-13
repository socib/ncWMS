package uk.ac.rdg.resc.ncwms.graphics.plot;

import java.awt.Shape;
import java.awt.geom.Path2D;


public enum MarkerStyle
{
    PRETTY
    {
        public PrettyVector marker() {return new PrettyVector();}
        public PrettyVector marker(double angle) {return new PrettyVector(angle);}
        public PrettyVector marker(double angle, double scale) {return new PrettyVector(angle, scale);}
    },
    STUMPY
    {
        public StumpyVector marker() {return new StumpyVector();}
        public StumpyVector marker(double angle) {return new StumpyVector(angle);}
        public StumpyVector marker(double angle, double scale) {return new StumpyVector(angle, scale);}
    },
    TRIANGLE
    {
        public TriangleVector marker() {return new TriangleVector();}
        public TriangleVector marker(double angle) {return new TriangleVector(angle);}
        public TriangleVector marker(double angle, double scale) {return new TriangleVector(angle, scale);}
    }, 
    LINE 
    {
        public LineVector marker() {return new LineVector();}
        public LineVector marker(double angle) {return new LineVector(angle);}
        public LineVector marker(double angle, double scale) {return new LineVector(angle, scale);}
    },
    FANCY
    {
        public FancyVector marker() {return new FancyVector();}
        public FancyVector marker(double angle) {return new FancyVector(angle);}
        public FancyVector marker(double angle, double scale) {return new FancyVector(angle, scale);}
    };

    abstract public Marker marker();
    abstract public Marker marker(double angle);
    abstract public Marker marker(double angle, double scale);

    public Marker marker(float angle)
    {
        return marker((double) angle);
    }

    public Marker marker(float angle, float scale)
    {
        return marker((double) angle, (double) scale);
    }

}

class PrettyVector extends SimpleMarker
{
    public PrettyVector() {super();}
    public PrettyVector(double angle) {super(angle);}
    public PrettyVector(double angle, double scale) {super(angle, scale);}
    @Override public boolean orientable() {return true;}
    @Override public boolean scalable() {return false;}
    @Override public boolean drawable() {return true;}
    @Override public boolean fillable() {return true;}
    @Override public Shape shape() {return shape;}
    private static final Shape shape = prettyvec();
    private static Shape prettyvec()
    {
        Path2D.Double path = new Path2D.Double();
        path.moveTo(0.000, -0.045);
        path.lineTo(0.625, -0.045);
        path.lineTo(0.500, -0.217);
        path.lineTo(1.000,  0.000);
        path.lineTo(0.500,  0.217);
        path.lineTo(0.625,  0.045);
        path.lineTo(0.000,  0.045);
        path.closePath();
        return path;
    }
}

class StumpyVector extends SimpleMarker
{
    public StumpyVector() {super();}
    public StumpyVector(double angle) {super(angle);}
    public StumpyVector(double angle, double scale) {super(angle, scale);}
    @Override public boolean orientable() {return true;}
    @Override public boolean scalable() {return false;}
    @Override public boolean drawable() {return true;}
    @Override public boolean fillable() {return true;}
    @Override public Shape shape() {return shape;}
    private static final Shape shape = stumpvec();
    private static Shape stumpvec()
    {
        Path2D.Double path = new Path2D.Double();
        path.moveTo(0.00,  0.05);
        path.lineTo(0.00, -0.05);
        path.lineTo(0.70, -0.05);
        path.lineTo(0.40, -0.40);
        path.lineTo(1.00,  0.00);
        path.lineTo(0.40,  0.40);
        path.lineTo(0.70,  0.05);
        path.closePath();
        return path;
    }
}

class TriangleVector extends SimpleMarker
{
    public TriangleVector() {super();}
    public TriangleVector(double angle) {super(angle);}
    public TriangleVector(double angle, double scale) {super(angle, scale);}
    @Override public boolean orientable() {return true;}
    @Override public boolean scalable() {return false;}
    @Override public boolean drawable() {return true;}
    @Override public boolean fillable() {return true;}
    @Override public Shape shape() {return shape;}
    private static final Shape shape = trivec();
    private static Path2D trivec()
    {
        Path2D.Double path = new Path2D.Double();
        path.moveTo(0.0,  0.4);
        path.lineTo(0.0, -0.4);
        path.lineTo(1.0,  0.0);
        path.closePath();
        return path;
    }
}

class LineVector extends SimpleMarker
{
    public LineVector() {super();}
    public LineVector(double angle) {super(angle);}
    public LineVector(double angle, double scale) {super(angle, scale);}
    @Override public boolean orientable() {return true;}
    @Override public boolean scalable() {return false;}
    @Override public boolean drawable() {return true;}
    @Override public boolean fillable() {return false;}
    @Override public Shape shape() {return shape;}
    private static final Shape shape = linevec();
    private static Path2D linevec()
    {
        Path2D.Double path = new Path2D.Double();
        path.moveTo(0.0,  0.0);
        path.lineTo(1.0,  0.0);
        path.moveTo(1.0,  0.0);
        path.lineTo(0.6,  0.3);
        path.moveTo(1.0,  0.0);
        path.lineTo(0.6, -0.3);
        path.closePath();
        return path;
    }
}

class FancyVector extends SimpleMarker
{
    public FancyVector() {super();}
    public FancyVector(double angle) {super(angle);}
    public FancyVector(double angle, double scale) {super(angle, scale);}
    @Override public boolean orientable() {return true;}
    @Override public boolean scalable() {return false;}
    @Override public boolean drawable() {return true;}
    @Override public boolean fillable() {return true;}
    @Override public Shape shape() {return shape;}
    private static final Shape shape = fancyvec();
    private static Path2D fancyvec()
    {
        Path2D.Double path = new Path2D.Double();
        path.moveTo(      0.0,       0.0);
        path.lineTo(      0.0, -3.0/11.0);
        path.lineTo( 5.0/11.0, -2.0/11.0);
        path.lineTo( 3.0/11.0, -5.0/11.0);
        path.lineTo(11.0/11.0, -1.5/11.0);
        path.lineTo( 3.0/11.0,  2.0/11.0);
        path.lineTo( 5.0/11.0, -1.0/11.0);
        path.closePath();
        return path;
    }
}
