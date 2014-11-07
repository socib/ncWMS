package uk.ac.rdg.resc.ncwms.graphics.plot;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;

public abstract class ShapeMarker extends Marker
{
    public abstract double scale();
    public abstract double angle();
    public abstract Shape shape();

    @Override
    public void draw(Graphics2D g2)
    {
        AffineTransform g2transform = g2.getTransform();
        if (orientable())
            g2.rotate(angle());
        if (scalable())
            g2.scale(scale(), scale());
        g2.draw(shape());
        g2.setTransform(g2transform);
    }

    @Override
    public void draw(Graphics2D g2, double x, double y)
    {
        AffineTransform g2transform = g2.getTransform();
        g2.translate(x, y);
        if (orientable())
            g2.rotate(angle());
        if (scalable())
            g2.scale(scale(), scale());
        g2.draw(shape());
        g2.setTransform(g2transform);
    }

    @Override
    public void draw(Graphics2D g2, int x, int y)
    {
        AffineTransform g2transform = g2.getTransform();
        g2.translate(x, y);
        if (orientable())
            g2.rotate(angle());
        if (scalable())
            g2.scale(scale(), scale());
        g2.draw(shape());
        g2.setTransform(g2transform);
    }

    @Override
    public void fill(Graphics2D g2)
    {
        AffineTransform g2transform = g2.getTransform();
        if (orientable())
            g2.rotate(angle());
        if (scalable())
            g2.scale(scale(), scale());
        g2.fill(shape());
        g2.setTransform(g2transform);
    }

    @Override
    public void fill(Graphics2D g2, double x, double y)
    {
        AffineTransform g2transform = g2.getTransform();
        g2.translate(x, y);
        if (orientable())
            g2.rotate(angle());
        if (scalable())
            g2.scale(scale(), scale());
        g2.fill(shape());
        g2.setTransform(g2transform);
    }

    @Override
    public void fill(Graphics2D g2, int x, int y)
    {
        AffineTransform g2transform = g2.getTransform();
        g2.translate(x, y);
        if (orientable())
            g2.rotate(angle());
        if (scalable())
            g2.scale(scale(), scale());
        g2.fill(shape());
        g2.setTransform(g2transform);
    }
    
    @Override
    public Shape getOutline(AffineTransform transform)
    {
        AffineTransform tf = new AffineTransform(transform);
        if (orientable())
            tf.rotate(angle());
        if (scalable())
            tf.scale(scale(), scale());
        return tf.createTransformedShape(shape());
    }

}
