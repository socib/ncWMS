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
        final Path2D boundary = new Path2D.Float(Path2D.WIND_NON_ZERO, n);
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
        boolean done;
        int m, n;
        int count;
        int k;
        float x, y, a, s;
        final AffineTransform axes = new AffineTransform();
        final Rectangle2D inset = new Rectangle2D.Float();
        final Area[] roverlap = new Area[rows];
        final Area[] coverlap = new Area[cols];
        final boolean[] mask = (dst == null) ? new boolean[rows*cols] : dst;
        final boolean[] iask = new boolean[rows*cols];
        final boolean[] oask = new boolean[rows*cols];
        for (int i = 0; i < rows; i++)
            roverlap[i] = new Area();
        for (int j = 0; j < cols; j++)
            coverlap[j] = new Area();
        m = 1;
        n = 1;
        count = 0;
        done = false;
        for (int ll = 2, l0 = 1, l1 = 1;
             ll <= rows + cols && ! done;
             ll++, l0 = rows < ll ? ll - rows : 1, l1 = cols < ll ? cols : ll -  1) {
            for (int l = l0; l <= l1 & ! done; l++) {
                m = l;
                n = ll - l;
                count = 0;
                done = true;
                for (int i = 0; i < rows; i += m)
                    roverlap[i].reset();
                for (int j = 0; j < cols; j += n)
                    coverlap[j].reset();
                for (int i = 0; i < rows && done; i += m) {
                    for (int j = 0; j < cols && done; j += n) {
                        k = cols * i + j;
                        x = xpnts[k];
                        y = ypnts[k];
                        a = apnts[k];
                        s = spnts[k];
                        if (! oask[k]) {
                            if (transform == null)
                                axes.setToIdentity();
                            else
                                axes.setTransform(transform);
                            axes.translate(x, y);
                            if (bounds != null) {
                                inset.setRect(axes.createTransformedShape(bounds).getBounds2D());
                            } else if (coverage != null) {
                                axes.scale(scale, scale);
                                if (Float.isNaN(a) || Float.isNaN(s))
                                    inset.setRect(0, 0, 0, 0);
                                else
                                    inset.setRect(style.marker(a, s).getOutline(axes).getBounds2D());
                            }
                            if (iask[k] || coverage == null || coverage.contains(inset)) {
                                iask[k] = true;
                                if (bounds != null) {
                                    for (int ii = i; ii >= 0 && done; ii -= m)
                                        done = ! roverlap[ii].intersects(inset);
                                    for (int jj = j; jj >= 0 && done; jj -= n)
                                        done = ! coverlap[jj].intersects(inset);
                                    if (done) {
                                        roverlap[i].add(new Area(inset));
                                        coverlap[j].add(new Area(inset));
                                        count++;
                                    }
                                }
                            } else {
                                oask[k] = true;
                            }
                        }
                    }
                }
            }
        }
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                mask[cols * i + j] = iask[cols * i + j] & (i % m == 0) & (j % n == 0);
        return mask;
    }

}
