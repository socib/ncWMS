package uk.ac.rdg.resc.ncwms.graphics.plot;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * Class to draw text labels in a color box.
 */
public class Label {

    private String text;
    private float xtext;
    private float ytext;
    private Color ctext;
    private Font ftext;
    private float xbox;
    private float ybox;
    private float wbox;
    private float hbox;
    private Color cbox;
    
    
    /**
     * Create a label composed of a text inside a box.
     * 
     * @param text text of the label.
     * @param xtext horizontal position of the text.
     * @param ytext vertical position of the text.
     * @param ctext color of the text.
     * @param ftext font of the text.
     * @param xbox horizontal position of the box.
     * @param ybox vertical position of the box.
     * @param wbox width of the box.
     * @param hbox height of the box.
     * @param cbox color of the box.
     */
    public Label(String text,
                 float xtext, float ytext, Color ctext, Font ftext,
                 float xbox, float ybox, float wbox, float hbox, Color cbox)
    {
        this.text = text;
        this.xtext = xtext;
        this.ytext = ytext;
        this.ctext = ctext;
        this.ftext = ftext;
        this.xbox = xbox;
        this.ybox = ybox;
        this.hbox = hbox;
        this.wbox = wbox;
        this.cbox = cbox;
    }
    
    /**
     * Draw the label in a graphics context.
     * 
     * @param g2 graphics context to plot in.
     * @param transform the transform defining the axis placement in the context.
     */
    public void draw(Graphics2D g2, AffineTransform transform)
    {
        AffineTransform g2transform = g2.getTransform();
        if (transform != null)
            g2.transform(transform);
        if (cbox != null)
            g2.setColor(cbox);
        Rectangle2D box = new Rectangle2D.Float(xbox, ybox, wbox, hbox);
        g2.fill(box);
        g2.setTransform(g2transform);
        if (ctext != null)
            g2.setColor(ctext);
        if (ftext != null)
            g2.setFont(ftext);
        Point2D.Float ptext = new Point2D.Float(xtext, ytext);
        if (transform != null)
            transform.transform(ptext, ptext);
        g2.drawString(text, ptext.x, ptext.y);
    }

}
