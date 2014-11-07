package uk.ac.rdg.resc.ncwms.graphics.plot;

import java.awt.Paint;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

/**
 * Gradient paint interpolating the values at three points according to a color map.
 */
public class TriangularGradientPaint implements Paint {

    final Point2D.Float p0, p1, p2;
    final float c0, c1, c2;
    final ColorMap colormap;
    
    /**
     * Create a triangular gradient paint from given points, values and color map.
     * 
     * @param x0 horizontal position of the first point.
     * @param y0 vertical position of the first point.
     * @param x1 horizontal position of the second point.
     * @param y1 vertical position of the second point.
     * @param x2 horizontal position of the third point point.
     * @param y2 vertical position of the third point.
     * @param c0 value at the first point.
     * @param c1 value at the second point.
     * @param c2 value at the third point.
     * @param colormap the color map.
     */
    public TriangularGradientPaint(float x0, float y0,
                                   float x1, float y1,
                                   float x2, float y2,
                                   float c0, float c1, float c2,
                                   ColorMap colormap)
    {
        this.p0 = new Point2D.Float(x0, y0);
        this.p1 = new Point2D.Float(x1, y1);
        this.p2 = new Point2D.Float(x2, y2);
        this.c0 = c0;
        this.c1 = c1;
        this.c2 = c2;
        this.colormap = colormap;
    }
    
    /**
     * Create a triangular gradient paint from given points, values and color map.
     * 
     * @param p0 the first point.
     * @param p1 the second point.
     * @param p2 the third point.
     * @param c0 value at the first point.
     * @param c1 value at the second point.
     * @param c2 value at the third point.
     * @param colormap the color map.
     */
    public TriangularGradientPaint(Point2D p0, Point2D p1, Point2D p2,
                                   float c0, float c1, float c2,
                                   ColorMap colormap)
    {
        this.p0 = new Point2D.Float(); this.p0.setLocation(p0);
        this.p1 = new Point2D.Float(); this.p1.setLocation(p1);
        this.p2 = new Point2D.Float(); this.p2.setLocation(p2);
        this.c0 = c0;
        this.c1 = c1;
        this.c2 = c2;
        this.colormap = colormap;
    }
    
    @Override
    public PaintContext createContext(ColorModel cm,
                                      Rectangle deviceBounds,
                                      Rectangle2D userBounds,
                                      AffineTransform transform,
                                      RenderingHints hints)
    {
        Point2D.Float q0 = new Point2D.Float();
        Point2D.Float q1 = new Point2D.Float();
        Point2D.Float q2 = new Point2D.Float();
        transform.transform(p0, q0);
        transform.transform(p1, q1);
        transform.transform(p2, q2);
        return new TriangularGradientPaintContext(q0, q1, q2, c0, c1, c2, colormap);
    }

    @Override
    public int getTransparency()
    {
        return colormap.getColorModel().getTransparency();
    }

}


/**
 * Paint context returned by triangular gradient paint.
 * 
 * Given a raster to fill, this gradient paint fills each pixel according to
 * the values at three given points, linearly interpolated according to the
 * coordinates of the center of the pixel in the barycentric coordinate system
 * defined by those points.
 */
class TriangularGradientPaintContext implements PaintContext
{
    final float m00, m10, m01, m11, m02, m12;
    final float c0, c1, c2;
    final ColorMap colormap;
    final ColorModel cm;

    /**
     * Create a triangular gradient paint context from given points and values.
     * 
     * 
     * @param q0 the first point in device coordinates.
     * @param q1 the second point in device coordinates.
     * @param q2 the third point in device coordinates.
     * @param c0 the value at the first point.
     * @param c1 the value at the second point.
     * @param c2 the value at the third point.
     * @param colormap the color map.
     */
    public TriangularGradientPaintContext(Point2D.Float q0,
                                          Point2D.Float q1,
                                          Point2D.Float q2,
                                          float c0, float c1, float c2,
                                          ColorMap colormap)
    {
        double[] mij = new double[6];
        try
        {
            AffineTransform ft = new AffineTransform(q1.x - q0.x, q1.y - q0.y,
                                                     q2.x - q0.x, q2.y - q0.y,
                                                     q0.x, q0.y);
            AffineTransform tf = ft.createInverse();
            tf.getMatrix(mij); // mij = {m00 m10 m01 m11 m02 m12}
        } 
        catch (NoninvertibleTransformException e)
        {
            throw new IllegalArgumentException("points should not be colinear");
        }
        this.m00 = (float) mij[0];
        this.m10 = (float) mij[1];
        this.m01 = (float) mij[2];
        this.m11 = (float) mij[3];
        this.m02 = (float) mij[4];
        this.m12 = (float) mij[5];
        this.c0 = c0;
        this.c1 = c1;
        this.c2 = c2;
        this.colormap = colormap;
        this.cm = colormap.getColorModel();
    }

    @Override
    public void dispose()
    {}

    @Override
    public ColorModel getColorModel()
    {
        return cm;
    }

    @Override
    public Raster getRaster(int x, int y, int w, int h) {
        WritableRaster raster = cm.createCompatibleWritableRaster(w, h);
        float u0, v0, u, v, t, c;
        int[] p = {0};
        for (int i = 0; i < h; i++)
        {
            v0 = y + i + 0.5f;
            for (int j = 0; j < w; j++)
            {
                u0 = x + j + 0.5f;
                u = m00 * u0 + m01 * v0 + m02;
                v = m10 * u0 + m11 * v0 + m12;
                t = 1.0f - u - v;
                c = t * c0 + u * c1 + v * c2;
                p[0] = colormap.getColorIndex(c);
                if (p[0] >= 0)
                    raster.setPixel(j, i, p);
            }
        }
        return raster;
    }

}
