package uk.ac.rdg.resc.ncwms.graphics.plot;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;


/**
 * Class to draw marker plots using scaled and/or oriented markers at the vertices of a grid.
 * 
 * This class produces plots of multidimensional quantities such as velocity 
 * vectors. Markers are placed at the vertices of the grid,
 * and they are scaled, oriented and/or colored according to the data values.
 * The size and spacing of the vectors might be controlled by two factors,
 * that will prevent the drawing of vectors with overlapped origins.
 */
public class MarkerPlot {
    
    private ColorMap colormap;
    private Color color;
    private MarkerStyle style;
    private float scale;
    private float space;
    private boolean inner;
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
     * @param space the space factor of the marker.
     * @param inner draw only markers contained in the area of the grid.
     */
    public MarkerPlot(float[] xpnts, float[] ypnts,
                      float[] cpnts, float[] apnts, float[] spnts,
                      int rows, int cols,
                      ColorMap colormap, Color color,
                      MarkerStyle style, float scale, float space, boolean inner)
    {
        this.colormap = colormap;
        this.color = color;
        this.style = style;
        this.scale = scale;
        this.space = space;
        this.inner = inner;
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
     * @param space the space factor of the marker.
     * @param inner draw only markers contained in the area of the grid.
     */
    public MarkerPlot(float[] xpnts, float[] ypnts,
                      float ccnst, float[] apnts, float[] spnts,
                      int rows, int cols,
                      ColorMap colormap, Color color,
                      MarkerStyle style, float scale, float space, boolean inner)
    {
        this.colormap = colormap;
        this.color = color;
        this.style = style;
        this.scale = scale;
        this.space = space;
        this.inner = inner;
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
     * @param space the space factor of the marker.
     * @param inner draw only markers contained in the area of the grid.
     */
    public MarkerPlot(float[] xpnts, float[] ypnts,
                      float[] cpnts, float acnst, float[] spnts,
                      int rows, int cols,
                      ColorMap colormap, Color color,
                      MarkerStyle style, float scale, float space, boolean inner)
    {
        this.colormap = colormap;
        this.color = color;
        this.style = style;
        this.scale = scale;
        this.space = space;
        this.inner = inner;
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
     * @param space the space factor of the marker.
     * @param inner draw only markers contained in the area of the grid.
     */
    public MarkerPlot(float[] xpnts, float[] ypnts,
                      float[] cpnts, float[] apnts, float scnst,
                      int rows, int cols,
                      ColorMap colormap, Color color,
                      MarkerStyle style, float scale, float space, boolean inner)
    {
        this.colormap = colormap;
        this.color = color;
        this.style = style;
        this.scale = scale;
        this.space = space;
        this.inner = inner;
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
     * @param space the space factor of the marker.
     * @param inner draw only markers contained in the area of the grid.
     */
    public MarkerPlot(float[] xpnts, float[] ypnts,
                      float ccnst, float acnst, float[] spnts,
                      int rows, int cols,
                      ColorMap colormap, Color color,
                      MarkerStyle style, float scale, float space, boolean inner)
    {
        this.colormap = colormap;
        this.color = color;
        this.style = style;
        this.scale = scale;
        this.space = space;
        this.inner = inner;
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
     * @param space the space factor of the marker.
     * @param inner draw only markers contained in the area of the grid.
     */
    public MarkerPlot(float[] xpnts, float[] ypnts,
                      float[] cpnts, float acnst, float scnst,
                      int rows, int cols,
                      ColorMap colormap, Color color,
                      MarkerStyle style, float scale, float space, boolean inner)
    {
        this.colormap = colormap;
        this.color = color;
        this.style = style;
        this.scale = scale;
        this.space = space;
        this.inner = inner;
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
     * @param space the space factor of the marker.
     * @param inner draw only markers contained in the area of the grid.
     */
    public MarkerPlot(float[] xpnts, float[] ypnts,
                      float ccnst, float[] apnts, float scnst,
                      int rows, int cols,
                      ColorMap colormap, Color color,
                      MarkerStyle style, float scale, float space, boolean inner)
    {
        this.colormap = colormap;
        this.color = color;
        this.style = style;
        this.scale = scale;
        this.space = space;
        this.inner = inner;
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
     * @param space the space factor of the marker.
     * @param inner draw only markers contained in the area of the grid.
     */
    public MarkerPlot(float[] xpnts, float[] ypnts,
                      float ccnst, float acnst, float scnst,
                      int rows, int cols,
                      ColorMap colormap, Color color,
                      MarkerStyle style, float scale, float space, boolean inner)
    {
        this.colormap = colormap;
        this.color = color;
        this.style = style;
        this.scale = scale;
        this.space = space;
        this.inner = inner;
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
        final Path2D boundary;
        final Shape clip;
        final AffineTransform axes = new AffineTransform();
        final Rectangle2D bounds = (space != 0) ? new Rectangle2D.Float() : null;
        final Area coverage = inner ? new Area() : null;
        final boolean[] mask = new boolean[rows*cols];
        Color mc = color;
        
        if (space != 0)
        {
            axes.setToScale(space, space);
            bounds.add(style.marker(-0.5 * Math.PI, 1.0).getOutline(axes).getBounds2D());
            bounds.add(style.marker( 0.0          , 1.0).getOutline(axes).getBounds2D());
            bounds.add(style.marker( 0.5 * Math.PI, 1.0).getOutline(axes).getBounds2D());
            bounds.add(style.marker(       Math.PI, 1.0).getOutline(axes).getBounds2D());
        }
        
        if (inner)
        {
            boundary = boundary(xpnts, ypnts, rows, cols);
            if (transform != null)
                boundary.transform(transform);
            coverage.add(new Area(boundary));
            clip = g2.getClip();
            if (clip != null)
                coverage.intersect(new Area(clip));
        }
        
        if (space != 0 || inner)
            layout(mask, xpnts, ypnts, apnts, spnts, rows, cols, style, scale,
                   coverage, bounds, transform);
        else
            Arrays.fill(mask, true);
        
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
            {
                k = cols * i + j;
                x = xpnts[k];
                y = ypnts[k];
                c = cpnts[k];
                a = apnts[k];
                s = spnts[k];
                if (mask[k] && ! Float.isNaN(a) && ! Float.isNaN(s))
                {
                    if (transform == null)
                        axes.setToIdentity();
                    else
                        axes.setTransform(transform);
                    axes.translate(x, y);
                    axes.scale(scale, scale);
                    marker = style.marker(a, s);
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
    
    
    /**
     * Build the path of the boundary of a grid.
     * @param xpnts horizontal position of the grid vertices.
     * @param ypnts vertical position of the grid vertices.
     * @param rows number of rows in the grid.
     * @param cols number of columns in the grid.
     * @return a path following the boundary of the grid.
     */
    private static Path2D boundary(float[] xpnts, float[] ypnts,
                                   int rows, int cols)
    {
        final int n = rows * cols - (rows > 1 ? rows - 2 : 0) * (cols > 1 ? cols - 2 : 0);
        final Path2D.Float boundary = new Path2D.Float(Path2D.WIND_NON_ZERO, n);
        if (n > 0)
        {
            final float[] xy = new float[2 * n];
            int i, j, k;
            i = j = k = 0;
            for (; j < cols - 1; j++)
            {
                xy[k++] = xpnts[cols * i + j];
                xy[k++] = ypnts[cols * i + j];
            }
            for (; i < rows - 1; i++)
            {
                xy[k++] = xpnts[cols * i + j];
                xy[k++] = ypnts[cols * i + j];
            }
            for (; j > 0; j--)
            {
                xy[k++] = xpnts[cols * i + j];
                xy[k++] = ypnts[cols * i + j];
            }
            for (; i > 0; i--)
            {
                xy[k++] = xpnts[cols * i + j];
                xy[k++] = ypnts[cols * i + j];
            }
            boundary.moveTo(xy[0], xy[1]);
            for (int m = 1; m < n; m++)
                boundary.lineTo(xy[2 * m], xy[2 * m + 1]);
            boundary.closePath();
        }
        return boundary;
    }
    
    
    /**
     * Decide which markers to render to avoid overlapping and/or clipping.
     * @param xpnts horizontal position of the grid vertices.
     * @param ypnts vertical position of the grid vertices.
     * @param apnts angle data values at the grid vertices.
     * @param spnts scale data values at the grid vertices.
     * @param rows number of rows in the grid.
     * @param cols number of columns in the grid.
     * @param style the style of marker to use.
     * @param scale the scale factor of the marker.
     * @param coverage the area containing the entire markers to render, or null.
     * @param bounds the bounds of non overlapping regions around markers.
     * @param transform the transform defining the axis placement in the context.
     * @param dst the mask to use, or null to create a new one.
     * @return a boolean mask setting whether a marker should be plot at each vertex.
     */
    private static boolean[] layout(
            boolean[] dst,
            float[] xpnts, float[] ypnts, float[] apnts, float[] spnts,
            int rows, int cols, MarkerStyle style, float scale,
            Area coverage, Shape bounds, AffineTransform transform)
    {
        int k;
        float x, y, a, s;
        Marker marker;
        Shape outline;
        final AffineTransform axes = new AffineTransform();
        final Rectangle2D inset = new Rectangle2D.Float();
        final Area[] roverlap = new Area[rows];
        final Area[] coverlap = new Area[cols];
        final int[] rfst = new int[rows]; Arrays.fill(rfst, cols);
        final int[] rlst = new int[rows]; Arrays.fill(rlst, -1);
        final int[] rcnt = new int[rows]; Arrays.fill(rcnt, 0);
        final int[] cfst = new int[cols]; Arrays.fill(cfst, rows);
        final int[] clst = new int[cols]; Arrays.fill(clst, -1);
        final int[] ccnt = new int[cols]; Arrays.fill(ccnt, 0);
        final boolean[] mask = (dst == null) ? new boolean[rows*cols] : dst;
        final boolean[] rask = new boolean[mask.length];
        final boolean[] cask = new boolean[mask.length];
        for (int i = 0; i < rows; i++)
            roverlap[i] = new Area();
        for (int j = 0; j < cols; j++)
            coverlap[j] = new Area();
        for (int i = 0, m = 0; m < rows; m++, i = rows - i - m % 2)
            for (int j = 0, n = 0; n < cols; n++, j = cols - j - n % 2)
            {
                k = cols * i + j;
                x = xpnts[k];
                y = ypnts[k];
                a = apnts[k];
                s = spnts[k];
                if (transform == null)
                    axes.setToIdentity();
                else
                    axes.setTransform(transform);
                axes.translate(x, y);
                if (bounds != null) {
                    inset.setRect(axes.createTransformedShape(bounds).getBounds2D());
                } else if (coverage != null) {
                    axes.scale(scale, scale);
                    marker = style.marker(Float.isNaN(a) ? 0.0 : a ,
                                          Float.isNaN(s) ? 1.0 : s);
                    outline = marker.getOutline(axes);
                    inset.setRect(outline.getBounds2D());
                }
                if (coverage == null || coverage.contains(inset)) {
                    if (bounds == null || ! roverlap[i].intersects(inset)) {
                        if (rfst[i] > j) rfst[i] = j;
                        if (rlst[i] < j) rlst[i] = j;
                        rcnt[i]++;
                        roverlap[i].add(new Area(inset));
                    }
                    if (bounds == null || ! coverlap[j].intersects(inset)) {
                        if (cfst[j] > i) cfst[j] = i;
                        if (clst[j] < i) clst[j] = i;
                        ccnt[j]++;
                        coverlap[j].add(new Area(inset));
                    }
                }
            }
        Arrays.fill(rask, false);
        for (int i = 0; i < rows; i++)
            for (int o = rfst[i], l = (rlst[i] - rfst[i]), m = 0, n = rcnt[i], j = o;
                 m < n;
                 m++, j = (int)((float) o + (float) l / (float) (n - 1) * (float) m))
                rask[cols * i + j] = true;
        Arrays.fill(cask, false);
        for (int j = 0; j < cols; j++)
            for (int o = cfst[j], l = (clst[j] - cfst[j]), m = 0, n = ccnt[j], i = o;
                 m < n;
                 m++, i = (int)((float) o + (float) l / (float) (n - 1) * (float) m ))
                cask[cols * i + j] = true;
        for (int m = 0; m < mask.length; m++)
            mask[m] = rask[m] & cask[m];
        return mask;
    }

}
