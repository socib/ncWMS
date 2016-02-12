package uk.ac.rdg.resc.ncwms.graphics.plot;

public abstract class SimpleMarker extends ShapeMarker
{

    private double angle = 0.0;
    private double scale = 1.0;

    protected SimpleMarker()
    {}
    
    protected SimpleMarker(double angle)
    {
        this.angle = angle;
    }

    protected SimpleMarker(double angle, double scale)
    {
        this.angle = angle;
        this.scale = scale;
    }

    @Override
    public double scale()
    {
        return scale;
    }

    @Override
    public double angle()
    {
        return angle;
    }
    

}
