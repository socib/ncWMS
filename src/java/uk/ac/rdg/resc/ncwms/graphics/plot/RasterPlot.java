package uk.ac.rdg.resc.ncwms.graphics.plot;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;


/**
 * Class to draw raster plots with cells centered at the vertices of a grid.
 * 
 * This class implements an algorithm for creating a raster plot of a grid with
 * a given color map. This is sometimes called a heat map or a pseudocolor plot.
 * The plot is composed of cells centered at the vertices of the grid colored
 * according to the value at the corresponding vertex.
  */
public class RasterPlot {

    private RasterStyle style;
    private ColorMap colormap;
    private float xpnts[];
    private float ypnts[];
    private float cpnts[];
    private int rows;
    private int cols;

    /**
     * Create a raster plot composed of cells around the vertices of a grid.
     * 
     * @param xpnts horizontal position of the grid vertices.
     * @param ypnts vertical position of the grid vertices.
     * @param cpnts data values at the grid vertices.
     * @param rows number of rows in the grid.
     * @param cols number of columns in the grid.
     * @param colormap the color map to use.
     * @param style the fill style to use.
     */
    public RasterPlot(float[] xpnts, float[] ypnts, float[] cpnts, int rows, int cols,
                      ColorMap colormap, RasterStyle style)
    {
        this.colormap = colormap;
        this.xpnts = xpnts;
        this.ypnts = ypnts;
        this.cpnts = cpnts;
        this.rows = rows;
        this.cols = cols;
        this.style = style;
    }

    /**
     * Draw the raster plot in the given graphics context using the proper style.
     *
     * @param g2 graphics context to plot in.
     * @param transform the transform defining the axis placement in the context.
     */
    public void draw(Graphics2D g2, AffineTransform transform)
    {
        AffineTransform g2transform = g2.getTransform();
        if (transform != null)
            g2.transform(transform);
        if (style == RasterStyle.BOXFILL)
            boxfill(g2, xpnts, ypnts, cpnts, rows, cols, colormap);
        else if (style == RasterStyle.SHADEFILL)
            shadefill(g2, xpnts, ypnts, cpnts, rows, cols, colormap);
        else if (style == RasterStyle.AREAFILL)
            areafill(g2, xpnts, ypnts, cpnts, rows, cols, colormap);
        if (transform != null)
            g2.setTransform(g2transform);
    }

    /**
     * Draw a boxfill raster plot in the given graphics context.
     *
     * Each cell of the grid is divided into four trapezoidal regions delimited
     * by a vertex, the mid points of adjacent sides, and the barycenter
     * of the cell. Each region is colored according to the vertex value.
     * This creates an illusion of a plot made of cells centered at each vertex
     * of the grid and colored according to the value at that vertex,
     * sometimes called a pseudocolor plot.
     * 
     * @param g2 graphics context to plot in.
     * @param xpnts horizontal position of the grid vertices.
     * @param ypnts vertical position of the grid vertices.
     * @param cpnts data values at the grid vertices.
     * @param rows number of rows in the grid.
     * @param cols number of columns in the grid.
     * @param colormap the color map to use.
     */
    public static void boxfill(Graphics2D g2,
                               float[] xpnts, float[] ypnts, float[] cpnts,
                               int rows, int cols, ColorMap colormap)
    {
        int nw, ne, sw, se;
        float xsw, xse, xnw, xne, xss, xnn, xee, xww, xoo;
        float ysw, yse, ynw, yne, yss, ynn, yee, yww, yoo;
        float csw, cse, cnw, cne;
        Color c;
        Path2D.Float p = new Path2D.Float(Path2D.WIND_NON_ZERO, 4);
        for (int i = 0; i < rows - 1; i++)
            for(int j = 0; j < cols - 1; j++)
            {
                sw = i * cols + j;
                se = i * cols + j + 1;
                nw = i * cols + j + cols;
                ne = i * cols + j + cols + 1;
                csw = cpnts[sw]; cse = cpnts[se]; cnw = cpnts[nw]; cne = cpnts[ne];
                xsw = xpnts[sw]; xse = xpnts[se]; xnw = xpnts[nw]; xne = xpnts[ne];
                ysw = ypnts[sw]; yse = ypnts[se]; ynw = ypnts[nw]; yne = ypnts[ne];
                xss = 0.5f * (xsw + xse); xnn = 0.5f * (xnw + xne);
                yss = 0.5f * (ysw + yse); ynn = 0.5f * (ynw + yne);
                xww = 0.5f * (xnw + xsw); xee = 0.5f * (xne + xse);
                yww = 0.5f * (ynw + ysw); yee = 0.5f * (yne + yse);
                xoo = 0.25f * (xsw + xse + xnw + xne);
                yoo = 0.25f * (ysw + yse + ynw + yne);
                c = colormap.getColorValue(csw);
                if (c != null)
                {
                    p.reset();
                    p.moveTo(xoo, yoo);
                    p.lineTo(xww, yww);
                    p.lineTo(xsw, ysw);
                    p.lineTo(xss, yss);
                    p.closePath();
                    g2.setColor(c);
                    g2.fill(p);
                }
                c = colormap.getColorValue(cse);
                if (c != null)
                {
                    p.reset();
                    p.moveTo(xoo, yoo);
                    p.lineTo(xss, yss);
                    p.lineTo(xse, yse);
                    p.lineTo(xee, yee);
                    p.closePath();
                    g2.setColor(c);
                    g2.fill(p);
                }
                c = colormap.getColorValue(cnw);
                if (c != null)
                {
                    p.reset();
                    p.moveTo(xoo, yoo);
                    p.lineTo(xnn, ynn);
                    p.lineTo(xnw, ynw);
                    p.lineTo(xww, yww);
                    p.closePath();
                    g2.setColor(c);
                    g2.fill(p);
                }
                c = colormap.getColorValue(cne);
                if (c != null)
                {
                    p.reset();
                    p.moveTo(xoo, yoo);
                    p.lineTo(xee, yee);
                    p.lineTo(xne, yne);
                    p.lineTo(xnn, ynn);
                    p.closePath();
                    g2.setColor(c);
                    g2.fill(p);
                }
            }
    }

    /**
     * Draw an areafill raster plot in the given graphics context.
     *
     * Each cell of the grid is divided into regions limited by the isolines
     * corresponding to the levels at the boundaries of each color band,
     * and each region is filled using the corresponding color band.
     * This creates an illusion of a plot made by filling the interior of
     * contour lines, sometimes called a choropleth plot.
     * 
     * @param g2 graphics context to plot in.
     * @param xpnts horizontal position of the grid vertices.
     * @param ypnts vertical position of the grid vertices.
     * @param cpnts data values at the grid vertices.
     * @param rows number of rows in the grid.
     * @param cols number of columns in the grid.
     * @param colormap the color map to use.
     */
    public static void areafill(Graphics2D g2,
                                float[] xpnts, float[] ypnts, float[] cpnts,
                                int rows, int cols, ColorMap colormap)
    {
        final int n = colormap.getNumColorBands() + 2;
        double[] levels = new double[n + 1];
        levels[0] = Double.NEGATIVE_INFINITY;
        for (int k = 0; k <= n - 2; k++)
            levels[k + 1] = colormap.getContinuousIndexedValue(k);
        levels[n] = Double.POSITIVE_INFINITY;
        
        int sw, se, nw, ne;
        float[] xcrns = {0, 0, 0, 0, 0};
        float[] ycrns = {0, 0, 0, 0, 0};
        float[] ccrns = {0, 0, 0, 0, 0};
        double ll, lu;
        double t0, t1;
        float x0, x1, y0, y1, c0, c1;
        Color c;
        Path2D.Float p = new Path2D.Float(Path2D.WIND_NON_ZERO, 9);
        double[] px = new double[8];
        double[] py = new double[8];
        int pn;
        
        for (int i = 0; i < rows - 1; i++)
            for(int j = 0; j < cols - 1; j++)
            {
                sw = i * cols + j;
                se = i * cols + j + 1;
                nw = i * cols + j + cols;
                ne = i * cols + j + cols + 1;
                xcrns[0] = xpnts[sw]; ycrns[0] = ypnts[sw]; ccrns[0] = cpnts[sw];
                xcrns[1] = xpnts[se]; ycrns[1] = ypnts[se]; ccrns[1] = cpnts[se];
                xcrns[2] = xpnts[ne]; ycrns[2] = ypnts[ne]; ccrns[2] = cpnts[ne];
                xcrns[3] = xpnts[nw]; ycrns[3] = ypnts[nw]; ccrns[3] = cpnts[nw];
                xcrns[4] = xpnts[sw]; ycrns[4] = ypnts[sw]; ccrns[4] = cpnts[sw];
                for (int k = 0; k < n; k++)
                {
                    c = colormap.getColorValue(0.5*(levels[k] + levels[k+1]));
                    ll = levels[k];
                    lu = levels[k+1];
                    if (c != null)
                    {
                        g2.setColor(c);
                        pn = 0;
                        for (int l = 0; l < 4; l++)
                        {
                            x0 = xcrns[l]; x1 = xcrns[l+1];
                            y0 = ycrns[l]; y1 = ycrns[l+1];
                            c0 = ccrns[l]; c1 = ccrns[l+1];
                            if (ll <= c0 && c0 <= lu) {
                                t0 = Double.POSITIVE_INFINITY;
                                if      (c1 > lu) t1 = (c1 - lu) / (c0 - lu);
                                else if (c1 < ll) t1 = (c1 - ll) / (c0 - ll);
                                else              t1 = Double.NaN;
                            } else if (c0 > lu) {
                                t0 = (c1 < lu) ? (c1 - lu) / (c0 - lu) : Double.NaN;
                                t1 = (c1 < ll) ? (c1 - ll) / (c0 - ll) : Double.NaN;
                            } else if (c0 < ll) {
                                t0 = (c1 > ll) ? (c1 - ll) / (c0 - ll) : Double.NaN;
                                t1 = (c1 > lu) ? (c1 - lu) / (c0 - lu) : Double.NaN;
                            } else {
                                t0 = Double.NaN;
                                t1 = Double.NaN;
                            }
                            if (! Double.isNaN(t0))
                            {
                                px[pn] = x0 + (x1 - x0) / (1.0 - t0);
                                py[pn] = y0 + (y1 - y0) / (1.0 - t0);
                                pn++;
                            }
                            if (! Double.isNaN(t1))
                            {
                                px[pn] = x0 + (x1 - x0) / (1.0 - t1);
                                py[pn] = y0 + (y1 - y0) / (1.0 - t1);
                                pn++;
                            }
                        }
                        if (pn > 2)
                        {
                            p.reset();
                            p.moveTo(px[0], py[0]);
                            for (int pi = 1; pi < pn; pi++)
                                p.lineTo(px[pi], py[pi]);
                            p.closePath();
                            g2.fill(p);
                        }
                    }
                }
            }
    }

    /**
     * Draw an shadefill raster plot in the given graphics context.
     *
     * Each cell of the grid is divided into triangles filled by a shading
     * varying the color from one vertex to another according to the data value
     * at each vertex. If all the vertices of the cell have a valid value (not
     * NaN), the barycenter of the cell is computed and assigned the mean value,
     * and a triangle is generated from each edge of the cell and the center.
     * Otherwise if a single vertex has an invalid value (NaN),
     * a triangle is generated from the other three vertices.
     * 
     * @param g2 graphics context to plot in.
     * @param xpnts horizontal position of the grid vertices.
     * @param ypnts vertical position of the grid vertices.
     * @param cpnts data values at the grid vertices.
     * @param rows number of rows in the grid.
     * @param cols number of columns in the grid.
     * @param colormap the color map to use.
     */
    public static void shadefill(Graphics2D g2,
                                 float[] xpnts, float[] ypnts, float[] cpnts,
                                 int rows, int cols, ColorMap colormap)
    {
        int nw, ne, sw, se;
        float xsw, xse, xnw, xne, xoo;
        float ysw, yse, ynw, yne, yoo;
        float csw, cse, cnw, cne, coo;
        Color colsw, colse, colnw, colne, coloo;
        TriangularGradientPaint t;
        Path2D.Float p = new Path2D.Float(Path2D.WIND_NON_ZERO, 4);
        for (int i = 0; i < rows - 1; i++)
            for(int j = 0; j < cols - 1; j++)
            {
                sw = i * cols + j;
                se = i * cols + j + 1;
                nw = i * cols + j + cols;
                ne = i * cols + j + cols + 1;
                csw = cpnts[sw]; cse = cpnts[se]; cnw = cpnts[nw]; cne = cpnts[ne];
                xsw = xpnts[sw]; xse = xpnts[se]; xnw = xpnts[nw]; xne = xpnts[ne];
                ysw = ypnts[sw]; yse = ypnts[se]; ynw = ypnts[nw]; yne = ypnts[ne];
                xoo = 0.25f * (xsw + xse + xnw + xne);
                yoo = 0.25f * (ysw + yse + ynw + yne);
                coo = 0.25f * (csw + cse + cnw + cne);
                colsw = colormap.getColorValue(csw);
                colse = colormap.getColorValue(cse);
                colnw = colormap.getColorValue(cnw);
                colne = colormap.getColorValue(cne);
                coloo = colormap.getColorValue(coo);
                if (! Float.isNaN(coo)) {
                    if (coloo != null && colsw != null & colse != null) {
                        t = new TriangularGradientPaint(xoo, yoo, xsw, ysw, xse, yse,
                                                        coo, csw, cse, colormap);
                        p.reset();
                        p.moveTo(xoo, yoo);
                        p.lineTo(xsw, ysw);
                        p.lineTo(xse, yse);
                        p.closePath();
                        g2.setPaint(t);
                        g2.fill(p);
                    }
                    if (coloo != null && colse != null & colne != null) {
                        t = new TriangularGradientPaint(xoo, yoo, xse, yse, xne, yne,
                                                        coo, cse, cne, colormap);
                        p.reset();
                        p.moveTo(xoo, yoo);
                        p.lineTo(xse, yse);
                        p.lineTo(xne, yne);
                        p.closePath();
                        g2.setPaint(t);
                        g2.fill(p);
                    }
                    if (coloo != null && colne != null & colnw != null) {
                        t = new TriangularGradientPaint(xoo, yoo, xne, yne, xnw, ynw,
                                                        coo, cne, cnw, colormap);
                        p.reset();
                        p.moveTo(xoo, yoo);
                        p.lineTo(xne, yne);
                        p.lineTo(xnw, ynw);
                        p.closePath();
                        g2.setPaint(t);
                        g2.fill(p);
                    }
                    if (coloo != null && colnw != null & colsw != null) {
                        t = new TriangularGradientPaint(xoo, yoo, xnw, ynw, xsw, ysw,
                                                        coo, cnw, csw, colormap);
                        p.reset();
                        p.moveTo(xoo, yoo);
                        p.lineTo(xnw, ynw);
                        p.lineTo(xsw, ysw);
                        p.closePath();
                        g2.setPaint(t);
                        g2.fill(p);
                    }
                } else if (! Double.isNaN(csw) && colsw != null &&
                           ! Double.isNaN(cse) && colse != null &&
                           ! Double.isNaN(cnw) && colnw != null) {
                    t = new TriangularGradientPaint(xsw, ysw, xse, yse, xnw, ynw,
                                                    csw, cse, cnw, colormap);
                    p.reset();
                    p.moveTo(xsw, ysw);
                    p.lineTo(xse, yse);
                    p.lineTo(xnw, ynw);
                    p.closePath();
                    g2.setPaint(t);
                    g2.fill(p);
                } else if (! Double.isNaN(cse) && colse != null &&
                           ! Double.isNaN(cne) && colne != null &&
                           ! Double.isNaN(csw) && colsw != null) {
                    t = new TriangularGradientPaint(xse, yse, xne, yne, xsw, ysw,
                                                    cse, cne, csw, colormap);
                    p.reset();
                    p.moveTo(xse, yse);
                    p.lineTo(xne, yne);
                    p.lineTo(xsw, ysw);
                    p.closePath();
                    g2.setPaint(t);
                    g2.fill(p);
                } else if (! Double.isNaN(cne) && colne != null &&
                           ! Double.isNaN(cnw) && colnw != null &&
                           ! Double.isNaN(cse) && colse != null) {
                    t = new TriangularGradientPaint(xne, yne, xnw, ynw, xse, yse,
                                                    cne, cnw, cse, colormap);
                    p.reset();
                    p.moveTo(xne, yne);
                    p.lineTo(xnw, ynw);
                    p.lineTo(xse, yse);
                    p.closePath();
                    g2.setPaint(t);
                    g2.fill(p);
                } else if (! Double.isNaN(cnw) && colnw != null &&
                           ! Double.isNaN(csw) && colsw != null &&
                           ! Double.isNaN(cne) && colne != null) {
                    t = new TriangularGradientPaint(xnw, ynw, xsw, ysw, xne, yne,
                                                    cnw, csw, cne, colormap);
                    p.reset();
                    p.moveTo(xnw, ynw);
                    p.lineTo(xsw, ysw);
                    p.lineTo(xne, yne);
                    p.closePath();
                    g2.setPaint(t);
                    g2.fill(p);
                }
            }
    }


}
