package uk.ac.rdg.resc.ncwms.graphics.plot;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;

/**
 * Base class for plot markers with some default implementations.
 * 
 * A marker instance can be drawn and/or filled in a graphics context,
 * possibly scaled and/or oriented.
 * 
 * This defines a null marker. It does nothing when drawing or filling.
 */
public abstract class Marker
{
    /**
     * Check whether the marker might be oriented.
     * @return whether the marker may be rotated or not.
     */
    public abstract boolean orientable();
    
    /**
     * Check whether the marker might be scaled.
     * @return whether the marker may be scaled or not.
     */
    public abstract boolean scalable();
    
    /**
     * Check whether the marker might be drawn (outlined).
     * @return whether the marker draw operation is non-trivial.
     */
    public abstract boolean drawable();
    
    /**
     * Check whether the marker might be filled (outlined).
     * @return whether the marker fill operation is non-trivial.
     */
    public abstract boolean fillable();
    
    /**
     * Draw the marker in a graphics context,
     * @param g2 the graphics context to draw the marker in.
     */
    public void draw(Graphics2D g2)
    {}

    /**
     * Draw the marker at the given position in a graphics context,
     * @param g2 the graphics context to draw the marker in.
     * @param x the horizontal coordinate of the marker location.
     * @param y the vertical coordinate of the marker location.
     */
    public void draw(Graphics2D g2, double x, double y)
    {}

    /**
     * Draw the marker at the given position in a graphics context,
     * @param g2 the graphics context to draw the marker in.
     * @param x the horizontal coordinate of the marker location.
     * @param y the vertical coordinate of the marker location.
     */
    public void draw(Graphics2D g2, int x, int y)
    {}

    /**
     * Fill the marker in a graphics context,
     * @param g2 the graphics context to fill the marker in.
     */
    public void fill(Graphics2D g2)
    {}

    /**
     * Fill the marker at the given position in a graphics context,
     * @param g2 the graphics context to fill the marker in.
     * @param x the horizontal coordinate of the marker location.
     * @param y the vertical coordinate of the marker location.
     */
    public void fill(Graphics2D g2, double x, double y)
    {}

    /**
     * Fill the marker at the given position in a graphics context,
     * @param g2 the graphics context to fill the marker in.
     * @param x the horizontal coordinate of the marker location.
     * @param y the vertical coordinate of the marker location.
     */
    public void fill(Graphics2D g2, int x, int y)
    {}

    /**
     * Get the outline of a marker in a transformed coordinate space.
     * @param transform the transform from marker space coordinates to user space coordinates.
     * @return the outline of this marker in the transformed space.
     */
    public Shape getOutline(AffineTransform transform)
    {
        return null;
    }
    

}
