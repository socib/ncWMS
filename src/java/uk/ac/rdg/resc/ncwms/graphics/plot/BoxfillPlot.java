package uk.ac.rdg.resc.ncwms.graphics.plot;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;

import uk.ac.rdg.resc.ncwms.graphics.ColorMap;

/**
 * Class to draw raster plots with cells centered at the vertices of a grid.
 * 
 * This class implements an algorithm for creating a raster plot of a grid with
 * a given color map. This is sometimes called a heat map or a pseudocolor plot.
 * The plot is composed of cells centered at the vertices of the grid colored
 * according to the value at the corresponding vertex.
  */
public class BoxfillPlot {
    
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
     */
    public BoxfillPlot(float[] xpnts, float[] ypnts, float[] cpnts, int rows, int cols,
                       ColorMap colormap)
    {
        this.colormap = colormap;
        this.xpnts = xpnts;
        this.ypnts = ypnts;
        this.cpnts = cpnts;
        this.rows = rows;
        this.cols = cols;
    }
    
    
    /**
     * Draw the raster plot in the given graphics context.
     *
     * Each cell of the grid is divided into four trapezoidal regions delimited
     * by a vertex, the mid points of adjacent sides, and the barycenter
     * of the cell. Each region is colored according to the vertex value.
     * This creates an illusion of a plot made of cells centered at each vertex
     * of the of the grid and colored according to the value at that vertex.
     * 
     * @param g2 graphics context to plot in.
     * @param transform the transform defining the axis placement in the context.
     */
    public void draw(Graphics2D g2, AffineTransform transform)
    {
        int nw, ne, sw, se;
        float xsw, xse, xnw, xne, xss, xnn, xee, xww, xoo;
        float ysw, yse, ynw, yne, yss, ynn, yee, yww, yoo;
        float csw, cse, cnw, cne;
        Color c;
        Path2D.Float p = new Path2D.Float(Path2D.WIND_NON_ZERO, 4);
        AffineTransform g2transform = g2.getTransform();
        if (transform != null)
          g2.transform(transform);
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
                xoo = 0.25f * (xnw + xne + xsw + xse);
                yoo = 0.25f * (ynw + yne + ysw + yse);
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
        g2.setTransform(g2transform);
    }

}
