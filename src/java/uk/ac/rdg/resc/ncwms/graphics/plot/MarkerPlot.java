package uk.ac.rdg.resc.ncwms.graphics.plot;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;


/**
 * Class to draw marker plots using scaled and/or oriented markers at the vertices of a grid.
 * 
 * This class produces plots of multidimensional quantities such as velocity 
 * vectors. Markers are placed at the vertices of the grid,
 * and they are scaled, oriented and/or colored according to the data values.
 * Only non-overlapping vectors are plot.
 */
public class MarkerPlot {
    
    private ColorMap colormap;
    private Color color;
    private MarkerStyle style;
    private float scale;
    private float xpnts[];
    private float ypnts[];
    private float cpnts[];
    private float apnts[];
    private float spnts[];
    private int rows;
    private int cols;
    
    
    /**
     * Create a marker plot with scaled and oriented vectors at the vertices of a grid.
     * @param xpnts horizontal position of the grid vertices.
     * @param ypnts vertical position of the grid vertices.
     * @param cpnts color data values at the grid vertices.
     * @param apnts angle data values at the grid vertices.
     * @param spnts scale data values at the grid vertices.
     * @param rows number of rows in the grid.
     * @param cols number of columns in the grid.
     * @param colormap the color map to use, or null.
     * @param color the color to use when there is no color map, or null.
     * @param style the style of marker to use.
     * @param scale the scale factor of the marker.
     */
    public MarkerPlot(float[] xpnts, float[] ypnts,
                      float[] cpnts, float[] apnts, float[] spnts,
                      int rows, int cols,
                      ColorMap colormap, Color color,
                      MarkerStyle style, float scale)
    {
        this.colormap = colormap;
        this.color = color;
        this.style = style;
        this.scale = scale;
        this.xpnts = xpnts;
        this.ypnts = ypnts;
        this.cpnts = cpnts;
        this.apnts = apnts;
        this.spnts = spnts;
        this.rows = rows;
        this.cols = cols;
    }
    
    
    /**
     * Create a marker plot with scaled and oriented vectors at the vertices of a grid.
     * @param xpnts horizontal position of the grid vertices.
     * @param ypnts vertical position of the grid vertices.
     * @param ccnst constant color data value for all the grid vertices.
     * @param apnts angle data values at the grid vertices.
     * @param spnts scale data values at the grid vertices.
     * @param rows number of rows in the grid.
     * @param cols number of columns in the grid.
     * @param colormap the color map to use, or null.
     * @param color the color to use when there is no color map, or null.
     * @param style the style of marker to use.
     * @param scale the scale factor of the marker.
     */
    public MarkerPlot(float[] xpnts, float[] ypnts,
                      float ccnst, float[] apnts, float[] spnts,
                      int rows, int cols,
                      ColorMap colormap, Color color,
                      MarkerStyle style, float scale)
    {
        this.colormap = colormap;
        this.color = color;
        this.style = style;
        this.scale = scale;
        this.xpnts = xpnts;
        this.ypnts = ypnts;
        this.cpnts = new float[xpnts.length]; Arrays.fill(this.cpnts, ccnst);
        this.apnts = apnts;
        this.spnts = spnts;
        this.rows = rows;
        this.cols = cols;
    }

    
    /**
     * Create a marker plot with scaled and oriented vectors at the vertices of a grid.
     * @param xpnts horizontal position of the grid vertices.
     * @param ypnts vertical position of the grid vertices.
     * @param cpnts color data values at the grid vertices.
     * @param acnst constant angle data value for all the grid vertices.
     * @param spnts scale data values at the grid vertices.
     * @param rows number of rows in the grid.
     * @param cols number of columns in the grid.
     * @param colormap the color map to use, or null.
     * @param color the color to use when there is no color map, or null.
     * @param style the style of marker to use.
     * @param scale the scale factor of the marker.
     */
    public MarkerPlot(float[] xpnts, float[] ypnts,
                      float[] cpnts, float acnst, float[] spnts,
                      int rows, int cols,
                      ColorMap colormap, Color color,
                      MarkerStyle style, float scale)
    {
        this.colormap = colormap;
        this.color = color;
        this.style = style;
        this.scale = scale;
        this.xpnts = xpnts;
        this.ypnts = ypnts;
        this.cpnts = cpnts;
        this.apnts = new float[xpnts.length]; Arrays.fill(this.apnts, acnst);
        this.spnts = spnts;
        this.rows = rows;
        this.cols = cols;
    }
    
    
    /**
     * Create a marker plot with scaled and oriented vectors at the vertices of a grid.
     * @param xpnts horizontal position of the grid vertices.
     * @param ypnts vertical position of the grid vertices.
     * @param cpnts color data values at the grid vertices.
     * @param apnts angle data values at the grid vertices.
     * @param scnst constant scale data value for all the grid vertices.
     * @param rows number of rows in the grid.
     * @param cols number of columns in the grid.
     * @param colormap the color map to use, or null.
     * @param color the color to use when there is no color map, or null.
     * @param style the style of marker to use.
     * @param scale the scale factor of the marker.
     */
    public MarkerPlot(float[] xpnts, float[] ypnts,
                      float[] cpnts, float[] apnts, float scnst,
                      int rows, int cols,
                      ColorMap colormap, Color color,
                      MarkerStyle style, float scale)
    {
        this.colormap = colormap;
        this.color = color;
        this.style = style;
        this.scale = scale;
        this.xpnts = xpnts;
        this.ypnts = ypnts;
        this.cpnts = cpnts;
        this.apnts = apnts;
        this.spnts = new float[xpnts.length]; Arrays.fill(this.spnts, scnst);
        this.rows = rows;
        this.cols = cols;
    }
    
    
    /**
     * Create a marker plot with scaled and oriented vectors at the vertices of a grid.
     * @param xpnts horizontal position of the grid vertices.
     * @param ypnts vertical position of the grid vertices.
     * @param ccnst constant color data value for all the grid vertices.
     * @param acnst constant angle data value for all the grid vertices.
     * @param spnts scale data values at the grid vertices.
     * @param rows number of rows in the grid.
     * @param cols number of columns in the grid.
     * @param colormap the color map to use, or null.
     * @param color the color to use when there is no color map, or null.
     * @param style the style of marker to use.
     * @param scale the scale factor of the marker.
     */
    public MarkerPlot(float[] xpnts, float[] ypnts,
                      float ccnst, float acnst, float[] spnts,
                      int rows, int cols,
                      ColorMap colormap, Color color,
                      MarkerStyle style, float scale)
    {
        this.colormap = colormap;
        this.color = color;
        this.style = style;
        this.scale = scale;
        this.xpnts = xpnts;
        this.ypnts = ypnts;
        this.cpnts = new float[xpnts.length]; Arrays.fill(this.cpnts, ccnst);
        this.apnts = new float[xpnts.length]; Arrays.fill(this.apnts, acnst);
        this.spnts = spnts;
        this.rows = rows;
        this.cols = cols;
    }
    
    
    /**
     * Create a marker plot with scaled and oriented vectors at the vertices of a grid.
     * @param xpnts horizontal position of the grid vertices.
     * @param ypnts vertical position of the grid vertices.
     * @param cpnts color data values at the grid vertices.
     * @param acnst constant angle data value for all the grid vertices.
     * @param scnst constant scale data value for all the grid vertices.
     * @param rows number of rows in the grid.
     * @param cols number of columns in the grid.
     * @param colormap the color map to use, or null.
     * @param color the color to use when there is no color map, or null.
     * @param style the style of marker to use.
     * @param scale the scale factor of the marker.
     * 
     */
    public MarkerPlot(float[] xpnts, float[] ypnts,
                      float[] cpnts, float acnst, float scnst,
                      int rows, int cols,
                      ColorMap colormap, Color color,
                      MarkerStyle style, float scale)
    {
        this.colormap = colormap;
        this.color = color;
        this.style = style;
        this.scale = scale;
        this.xpnts = xpnts;
        this.ypnts = ypnts;
        this.cpnts = cpnts;
        this.apnts = new float[xpnts.length]; Arrays.fill(this.apnts, acnst);
        this.spnts = new float[xpnts.length]; Arrays.fill(this.spnts, scnst);
        this.rows = rows;
        this.cols = cols;
    }
    
    
    /**
     * Create a marker plot with scaled and oriented vectors at the vertices of a grid.
     * @param xpnts horizontal position of the grid vertices.
     * @param ypnts vertical position of the grid vertices.
     * @param ccnst constant color data value for all the grid vertices.
     * @param apnts angle data values at the grid vertices.
     * @param scnst constant scale data value for all the grid vertices.
     * @param rows number of rows in the grid.
     * @param cols number of columns in the grid.
     * @param colormap the color map to use, or null.
     * @param color the color to use when there is no color map, or null.
     * @param style the style of marker to use.
     * @param scale the scale factor of the marker.
     */
    public MarkerPlot(float[] xpnts, float[] ypnts,
                      float ccnst, float[] apnts, float scnst,
                      int rows, int cols,
                      ColorMap colormap, Color color,
                      MarkerStyle style, float scale)
    {
        this.colormap = colormap;
        this.color = color;
        this.style = style;
        this.scale = scale;
        this.xpnts = xpnts;
        this.ypnts = ypnts;
        this.cpnts = new float[xpnts.length]; Arrays.fill(this.cpnts, ccnst);
        this.apnts = apnts;
        this.spnts = new float[xpnts.length]; Arrays.fill(this.spnts, scnst);
        this.rows = rows;
        this.cols = cols;
    }
    
    
    /**
     * Create a marker plot with scaled and oriented vectors at the vertices of a grid.
     * @param xpnts horizontal position of the grid vertices.
     * @param ypnts vertical position of the grid vertices.
     * @param ccnst constant color data value for all the grid vertices.
     * @param acnst constant angle data value for all the grid vertices.
     * @param scnst constant scale data value for all the grid vertices.
     * @param rows number of rows in the grid.
     * @param cols number of columns in the grid.
     * @param colormap the color map to use, or null.
     * @param color the color to use when there is no color map, or null.
     * @param style the style of marker to use.
     * @param scale the scale factor of the marker.
     */
    public MarkerPlot(float[] xpnts, float[] ypnts,
                      float ccnst, float acnst, float scnst,
                      int rows, int cols,
                      ColorMap colormap, Color color,
                      MarkerStyle style, float scale)
    {
        this.colormap = colormap;
        this.color = color;
        this.style = style;
        this.scale = scale;
        this.xpnts = xpnts;
        this.ypnts = ypnts;
        this.cpnts = new float[xpnts.length]; Arrays.fill(this.cpnts, ccnst);
        this.apnts = new float[xpnts.length]; Arrays.fill(this.apnts, acnst);
        this.spnts = new float[xpnts.length]; Arrays.fill(this.spnts, scnst);
        this.rows = rows;
        this.cols = cols;
    }
    
    
    /**
     * Draw the marker plot in the given graphics context.
     * @param g2 graphics context to plot in.
     * @param transform the transform defining the axis placement in the context.
     */
    public void draw(Graphics2D g2, AffineTransform transform)
    {
        int k;
        float x, y;
        float c, a, s;
        Marker marker;
        Shape outline;
        Rectangle2D bounds = new Rectangle2D.Float();
        Point2D point = new Point2D.Float();
        Area covering = new Area();
        AffineTransform axes = new AffineTransform();
        Color mc = color;
        
        axes.scale(scale, scale);
        bounds.add(style.marker(-0.5 * Math.PI, 1.0).getOutline(axes).getBounds2D());
        bounds.add(style.marker( 0.0          , 1.0).getOutline(axes).getBounds2D());
        bounds.add(style.marker( 0.5 * Math.PI, 1.0).getOutline(axes).getBounds2D());
        bounds.add(style.marker(       Math.PI, 1.0).getOutline(axes).getBounds2D());

        for (int i = 0; i < rows; i++)
            for(int j = 0; j < cols; j++)
            {
                k = cols * i + j;
                x = xpnts[k];
                y = ypnts[k];
                c = cpnts[k];
                a = apnts[k];
                s = spnts[k];
                if (! Float.isNaN(c) && ! Float.isNaN(a) && ! Float.isNaN(s))
                {
                    point.setLocation(x, y);
                    if (transform != null)
                        transform.transform(point, point);   
                    if (! covering.contains(point))
                    {
                        if (transform == null)
                            axes.setToIdentity();
                        else
                            axes.setTransform(transform);
                        axes.translate(x, y);
                        covering.add(new Area(axes.createTransformedShape(bounds)));
                        marker = style.marker(a, s);
                        axes.scale(scale, scale);
                        outline = marker.getOutline(axes);
                        if (colormap != null)
                            mc = colormap.getColorValue(c);
                        if (mc != null)
                            g2.setColor(mc);
                        if (marker.fillable())
                            g2.fill(outline);
                        else if (marker.drawable())
                            g2.draw(outline);
                    }
                }
            }
    }

}
