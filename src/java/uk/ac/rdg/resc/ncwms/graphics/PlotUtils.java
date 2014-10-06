package uk.ac.rdg.resc.ncwms.graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

/**
 * Plotting functions utility class.
 */
public class PlotUtils {
    /**
     * Plot a label composed of a text inside a box.
     * @param g2 graphics context to plot in.
     * @param text text of the label.
     * @param xtext horizontal position of the text.
     * @param ytext vertical position of the text.
     * @param ctext color of the text.
     * @param xbox horizontal position of the box.
     * @param ybox vertical position of the box.
     * @param wbox width of the box.
     * @param hbox height of the box.
     * @param cbox color of the box.
     * @return the graphics context g2.
     */
    public static Graphics2D plotLabel(Graphics2D g2, String text,
                                       float xtext, float ytext, Color ctext,
                                       float xbox, float ybox,
                                       float wbox, float hbox, Color cbox)
    {
        g2.setPaint(cbox);
        Rectangle2D box = new Rectangle2D.Float(xbox, ybox, wbox, hbox);
        g2.draw(box);
        TextLayout layout = new TextLayout(text,
                                           g2.getFont(),
                                           g2.getFontRenderContext());
        layout.draw(g2, xtext, ytext);
        return g2;
    }

    public static void plotBoxfill(Graphics2D g2, ColorMap colormap,
                                   float[] xpnts, float[] ypnts, float[] cpnts,
                                   int rows, int cols)
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
    }


}
