/*
 * Copyright (c) 2007 The University of Reading
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the University of Reading, nor the names of the
 *    authors or contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package uk.ac.rdg.resc.ncwms.graphics;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.geotoolkit.referencing.CRS;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.util.Range;
import uk.ac.rdg.resc.edal.util.Ranges;
import uk.ac.rdg.resc.ncwms.exceptions.WmsException;
import uk.ac.rdg.resc.ncwms.graphics.plot.BoxfillPlot;
import uk.ac.rdg.resc.ncwms.graphics.plot.ContourPlot;
import uk.ac.rdg.resc.ncwms.graphics.plot.Label;
import uk.ac.rdg.resc.ncwms.util.WmsUtils;
import uk.ac.rdg.resc.ncwms.wms.Layer;

/**
 * An object that is used to render data into images.  Instances of this class
 * must be created through the {@link Builder}.
 *
 * @author Jon Blower
 */
public final class ImageProducer
{
    /**
     * The width of the legend in pixels that will be created by getLegend()
     */
    public static final int LEGEND_WIDTH = 110;
    /**
     * The height of the legend in pixels that will be created by getLegend()
     */
    public static final int LEGEND_HEIGHT = 264;
    
    private static final Logger logger = LoggerFactory.getLogger(ImageProducer.class);

    public static enum Style {BOXFILL, VECTOR, CONTOUR, BARB, STUMPVEC, TRIVEC, LINEVEC, FANCYVEC};
    
    private Style style;
    private HorizontalGrid layerGrid;
    private HorizontalGrid imageGrid;
    private ColorMap colorMap;
    private boolean autoScale;
    private int numContours;
    private float arrowLength = 14.0f;
    private float barbLength = 28.0f;
    private int equator_y_index;
    public float vectorScale;
    
    // set of rendered images, ready to be turned into a picture
    private List<BufferedImage> renderedFrames = new ArrayList<BufferedImage>();
    
    // If we need to cache the frame data and associated labels (we do this if
    // we have to auto-scale the image) this is where we put them.
    private static final class Components {
        private final List<Float> x;
        private final List<Float> y;
        public Components(List<Float> x, List<Float> y) {
            this.x = x;
            this.y = y;
        }
        public List<Float> getMagnitudes() {
            return this.y == null ? this.x : WmsUtils.getMagnitudes(this.x, this.y);
        }
    }
    private List<Components> frameData;
    
    private List<String> labels;

    /** Prevents direct instantiation */
    private ImageProducer() {}

    public BufferedImage getLegend(Layer layer)
    {
        Color backgroundColor = colorMap.getExtendedUndefValColor();
        float[] backgroundHSB = Color.RGBtoHSB(backgroundColor.getRed(),
                backgroundColor.getGreen(),
                backgroundColor.getBlue(), null);
        Color textColor =  backgroundHSB[2] < 0.5 ? Color.WHITE : Color.BLACK;
        
        // Create the level image itself and get the craphics context.
        BufferedImage legend = new BufferedImage(LEGEND_WIDTH, LEGEND_HEIGHT,
                                                 BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g2 = legend.createGraphics();
        
        // Enable text antialiasing.
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
                            RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
        
        // Fill the background.
        g2.setColor(backgroundColor);
        g2.fillRect(0, 0, LEGEND_WIDTH, LEGEND_HEIGHT);

        // Create the color bar itself.
        BufferedImage colorBar = getColorBar(24, ColorMap.MAX_NUM_COLORS);
        g2.drawImage(colorBar, null, 2, 5);
        
        // Set the text color for the scale tick values and the title.
        g2.setColor(textColor);
        
        // Add the scale tick values
        int numColorBands = colorMap.getNumColorBands();
        double q0 = colorMap.getContinuousIndexedValue(0.00f);
        double q1 = colorMap.getContinuousIndexedValue(0.25f * numColorBands);
        double q2 = colorMap.getContinuousIndexedValue(0.50f * numColorBands);
        double q3 = colorMap.getContinuousIndexedValue(0.75f * numColorBands);
        double q4 = colorMap.getContinuousIndexedValue(numColorBands);
        NumberFormat formatter = getNumberFormat(4);
        g2.drawString(formatter.format(q4), 27,  10);
        g2.drawString(formatter.format(q3), 27,  73);
        g2.drawString(formatter.format(q2), 27, 137);
        g2.drawString(formatter.format(q1), 27, 201);
        g2.drawString(formatter.format(q0), 27, 264);
        
        // Add the title as rotated text
        String title = layer.getTitle();
        String units = layer.getUnits();
        String label = (units == null || units.trim().equals("")) 
                     ? title
                     : (title + " (" + units + ")");
        AffineTransform transform = g2.getTransform();
        g2.translate(90, 0);
        g2.rotate(0.5 * Math.PI);
        g2.drawString(label, 5, 0);
        g2.setTransform(transform);
        
        return legend;
    }
    
    /**
     * Get a color bar for the current color map
     * 
     * The bar is a rectangle with the color bands of the inner colors of 
     * the current color map (without labels or ticks).
     * @param width width of the returned image.
     * @param height height of the returned image.
     * @return the image with the color bar.
     */
    public BufferedImage getColorBar(int width, int height)
    {
        final int numColorBands = colorMap.getNumColorBands();
        BufferedImage colorBar = new BufferedImage(
                width, height, BufferedImage.TYPE_BYTE_INDEXED,
                colorMap.getColorModel());
        Graphics2D g2 = colorBar.createGraphics();
        AffineTransform transform = g2.getTransform();
        g2.translate(0.0f, (double) height);
        g2.scale((double) width, - (double) height / (double) numColorBands);
        for (int i = 0; i < numColorBands; i++)
        {
            Rectangle2D.Float band = new Rectangle2D.Float(0.0f, (float) i, 1.0f, 1.0f);
            g2.setColor(colorMap.getIndexedColor(i));
            g2.fill(band);
        }
        g2.setTransform(transform);
        return colorBar;
    }
    
    public int getImageWidth()
    {
        return this.imageGrid.getGridExtent().getSpan(0);
    }
    
    public int getImageHeight()
    {
        return this.imageGrid.getGridExtent().getSpan(1);
    }
    
    public double getMapWidth()
    {
        return this.imageGrid.getExtent().getSpan(0);
    }
    
    public double getMapHeight()
    {
        return this.imageGrid.getExtent().getSpan(1);
    }
    
    public double getMapOriginX()
    {
        return this.imageGrid.getExtent().getMinX();
    }
    
    public double getMapOriginY()
    {
        return this.imageGrid.getExtent().getMinY();
    }
    
    /**
     * Adds a frame of scalar data to this ImageProducer.  If the data cannot yet be rendered
     * into a BufferedImage, the data and label are stored.
     * @throws WmsException 
     */
    public void addFrame(List<Float> data, String label) throws WmsException
    {
        this.addFrame(data, null, label);
    }
    
    /**
     * Adds a frame of vector data to this ImageProducer.  If the data cannot yet be rendered
     * into a BufferedImage, the data and label are stored.
     * @throws WmsException 
     */
    public void addFrame(List<Float> xData, List<Float> yData, String label) throws WmsException
    {
        logger.debug("Adding frame with label {}", label);
        Components comps = new Components(xData, yData);
        if (autoScale)
        {
            logger.debug("Auto-scaling, so caching frame");
            if (this.frameData == null)
            {
                this.frameData = new ArrayList<Components>();
                this.labels = new ArrayList<String>();
            }
            this.frameData.add(comps);
            this.labels.add(label);
        }
        else
        {
            logger.debug("Scale is set, so rendering image");
            this.renderedFrames.add(this.createImage(comps, label));
        }
    }
    
    /**
     * Creates and returns a single frame as an Image, based on the given data.
     * Adds the label if one has been set.  The scale must be set before
     * calling this method.
     * @throws WmsException 
     */
    private BufferedImage createImage(Components comps, String label)
    throws WmsException 
    {
        AffineTransform mapGraphicsTransform = getMapImageGraphicsTransform();
        BufferedImage image = getMapImage();
        Graphics2D graphics = getMapImageGraphics(image);
        
        List<List<Float>> cmps = new ArrayList<List<Float>>();
        List<float[]> crds = new ArrayList<float[]>();
        List<float[]> data = new ArrayList<float[]>();
        int size[] = {0, 0};
        if (style == Style.BOXFILL) {
            cmps.add(comps.getMagnitudes());
            extractPlotData(cmps, crds, data, size);
            BoxfillPlot plot = new BoxfillPlot(crds.get(0), crds.get(1),
                                               data.get(0), size[0], size[1], 
                                               colorMap);
            plot.draw(graphics, mapGraphicsTransform);
        } else if (style == Style.CONTOUR) {
            float levels[] = getContourLevels();
            cmps.add(comps.getMagnitudes());
            extractPlotData(cmps, crds, data, size);
            NumberFormat formatter = getNumberFormat(4);
            Font font = new Font(graphics.getFont().getName(), Font.BOLD,
                                 Math.round(7.0f * 96.0f / 72.0f));
            Stroke lineStroke = new BasicStroke(0.75f * 96.0f / 72.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
            Stroke textStroke = new BasicStroke(1.5f * 96.0f / 72.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
            ContourPlot plot = new ContourPlot(
                    crds.get(0), crds.get(1), data.get(0), size[0], size[1],
                    levels, Color.BLACK, lineStroke,
                    formatter, font, Color.WHITE, textStroke,
                    4.0f, 3.0f);
            plot.draw(graphics, mapGraphicsTransform);
        }
        if (label != null && !label.isEmpty()) {
            Label caption = new Label(
                    label, 10, image.getHeight() - 5, new Color(255, 151, 0), graphics.getFont(),
                    1, image.getHeight() - 19, image.getWidth() - 1, 18, new Color(0, 0, 143));
            caption.draw(graphics, null);
        }
        return image;
    }

    /**
     * Create an image for the current dimensions and style.
     * @return the image to plot in.
     */
    private BufferedImage getMapImage()
    {
        if (isContourStyle(style))
            return new BufferedImage(getImageWidth(), getImageHeight(),
                                     BufferedImage.TYPE_INT_ARGB);
        else
            return new BufferedImage(getImageWidth(), getImageHeight(),
                                     BufferedImage.TYPE_BYTE_INDEXED,
                                     colorMap.getColorModel());
    }

    /**     * Initialize the graphics context for the current dimensions and style.     * @param image the image to plot in.
     * @return the graphics context of the image to plot in.
     */    private Graphics2D getMapImageGraphics(Image image)
    {
        Color backgroundColor = colorMap.getExtendedUndefValColor();
        int width = getImageWidth();
        int height = getImageHeight();
        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setComposite(AlphaComposite.SrcOver);
        graphics.setBackground(backgroundColor);
        graphics.setColor(backgroundColor);
        graphics.clearRect(0, 0, width, height);
        graphics.clipRect(0, 0, width, height);
        if (isContourStyle(style))
        {
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
        }
        return graphics;
    }
    
    /**
     * Compute the transform to align the plot axes with the edges of the image.
     * @return the transform from map coordinates to image coordinates.
     */
    private AffineTransform getMapImageGraphicsTransform()
    {
        double iw = getImageWidth();
        double ih = getImageHeight();
        double mw = getMapWidth();
        double mh = getMapHeight();
        double mx = getMapOriginX();
        double my = getMapOriginY();
        AffineTransform transform = new AffineTransform();
        transform.translate(0, ih);
        transform.scale(iw/mw, -ih/mh);
        transform.translate(-mx, -my);
        return transform;
    }
    
    
    /**
     * Compute the contour levels from the number of contours and the color scale.
     * @return the contour levels to plot.
     */
    private float[] getContourLevels()
    {
        float[] levels = new float[numContours];
        float factor = (float) colorMap.getNumColorBands() / (float) (numContours - 1);
        for (int i = 0; i < numContours; i++)
            levels[i] = (float) colorMap.getContinuousIndexedValue(i * factor);
        return levels;
    }
    
    /**
     * Get a formatter for the values in the current scale range.
     * 
     * The 
     * @param digits the number of significant digits in short form
     * @return a decimal format using fixed precision or scientific notation.
     */
    private NumberFormat getNumberFormat(int digits)
    {
        double absa = Math.abs(colorMap.getScaleMin());
        double absb = Math.abs(colorMap.getScaleMax());
        double absmax = Math.max(absa, absb);
        int ordmax = (int) Math.floor(Math.log10(absmax));
        String integer, fraction, exponent, pattern;
        if (-digits < ordmax - 1 && ordmax + 1 < digits) {
            integer = "0";
            fraction = "." + new String(new char[digits - ordmax - 1]).replace("\0", "#");
            exponent = "";
        } else if (ordmax + 1 == digits) {
            integer = "0";
            fraction = "";
            exponent = "";
        } else {
            integer = "0";
            fraction = "." + new String(new char[digits-1]).replace("\0", "#");
            exponent = "E0";
        }
        pattern = integer + fraction + exponent;
        return new DecimalFormat(" " + pattern + ";" + "-" + pattern);
    }
    
    /**
     * Gets the frames as BufferedImages, ready to be turned into a picture or
     * animation.  This is called just before the picture is due to be created,
     * so subclasses can delay creating the BufferedImages until all the data
     * has been extracted (for example, if we are auto-scaling an animation,
     * we can't create each individual frame until we have data for all the frames)
     * @return List of BufferedImages
     * @throws WmsException 
     */
    public List<BufferedImage> getRenderedFrames() throws WmsException
    {
        this.setScale(); // Make sure the colour scale is set before proceeding
        // We render the frames if we have not done so already
        if (this.frameData != null)
        {
            logger.debug("Rendering image frames...");
            for (int i = 0; i < this.frameData.size(); i++)
            {
                logger.debug("    ... rendering frame {}", i);
                Components comps = this.frameData.get(i);
                this.renderedFrames.add(this.createImage(comps, this.labels.get(i)));
            }
        }
        return this.renderedFrames;
    }
    
    /**
     * Makes sure that the scale is set: if we are auto-scaling, this reads all
     * of the data we have stored to find the extremes.  If the scale has
     * already been set, this does nothing.
     */
    private void setScale()
    {
        if (autoScale)
        {
            Float scaleMin = null;
            Float scaleMax = null;
            logger.debug("Setting the scale automatically");
            // We have a cache of image data, which we use to generate the colour scale
            for (Components comps : this.frameData)
            {
                // We only use the first component if this is a vector quantity
                Range<Float> range = Ranges.findMinMax(comps.x);
                // TODO: could move this logic to the Range/Ranges class
                if (!range.isEmpty())
                {
                    if (scaleMin == null || range.getMinimum().compareTo(scaleMin) < 0)
                    {
                        scaleMin = range.getMinimum();
                    }
                    if (scaleMax == null || range.getMaximum().compareTo(scaleMax) > 0)
                    {
                        scaleMax = range.getMaximum();
                    }
                }
            }
            colorMap.setScaleRange(scaleMin, scaleMax);
        }
    }
    
    /**
     * Check if the style of the map plot is arrow (some kind of vector marker).
     * @param style the style of the plot     * @return whether the style of the plot is arrow.
     */
    private boolean isArrowStyle(Style style)
    {
        return style == Style.BARB || style == Style.FANCYVEC || style == Style.STUMPVEC || style == Style.TRIVEC || style == Style.LINEVEC;
    }

    /**
     * Check if the style of the map plot is contour.
     * @param style the style of the plot
     * @return whether the style of the plot is contour.
     */
    private boolean isContourStyle(Style style)
    {
        return style == Style.CONTOUR;
    }

    /**
     * Extract the subgrid with the data to plot.
     * 
     * This is the counterpart of the hack in PixelMap to extract the values
     * of the layer at the vertices of the subgrid that covers the bounding box
     * of the map.
     * @param cmps the values of the layer components as extracted by the PixelMap hack.
     * @param crds the coordinates of the vertices of the subgrid in the requested map reference system.
     * @param data the values of the layer at the vertices of the subgrid.
     * @param shape the shape of the subgrid.
     * @throws WmsException if there is no transformation between the layer's and the map reference systems. 
     */
    private void extractPlotData(List<List<Float>> cmps,
                                 List<float[]> crds, List<float[]> data,
                                 int[] shape) throws WmsException
    {
        final int step = layerGrid.getGridExtent().getSpan(0);
        final int ibeg = layerGrid.getGridExtent().getLow(0);
        final int iend = layerGrid.getGridExtent().getHigh(0);
        final int jbeg = layerGrid.getGridExtent().getLow(1);
        final int jend = layerGrid.getGridExtent().getHigh(1);
        int imin = iend;
        int imax = ibeg - 1;
        int jmin = jend;
        int jmax = jbeg - 1;
        for (List<Float> c : cmps)
            for (int i = ibeg; i <= iend; i++)
                for (int j = jbeg; j <= jend; j++)
                    if (i < imin || i > imax || i < jmin || j > jmax)
                    {
                        Float v = c.get(i + j * step);
                        if (v != null && ! Float.isNaN(v))
                        {
                            imin = Math.min(imin, i);
                            imax = Math.max(imax, i);
                            jmin = Math.min(jmin, j);
                            jmax = Math.max(jmax, j);
                        }
                    }
        data.clear();
        crds.clear();
        shape[0] = Math.max(imax - imin + 1, 0);
        shape[1] = Math.max(jmax - jmin + 1, 0);
        final int total = shape[0] * shape[1];
        for (List<Float> c : cmps)
        {
            float[] vals = new float[total];
            int k = 0;
            for (int i = imin; i <= imax; i++)
                for (int j = jmin; j <= jmax; j++) {
                    Float v = c.get(i + j * step);
                    vals[k++] = (v == null) ? Float.NaN : v;
                }
            data.add(vals);
        }
        MathTransform transform;
        try {
            transform = CRS.findMathTransform(
                    layerGrid.getCoordinateReferenceSystem(),
                    imageGrid.getCoordinateReferenceSystem(),
                    true);
        } catch (FactoryException e) {
            throw new WmsException("Could not get transform from layer reference system to map reference system");
        }
        float[] xcrd = new float[total];
        float[] ycrd = new float[total];
        int k = 0;
        for (int i = imin; i <= imax; i++)
            for (int j = jmin; j <= jmax; j++)
            {
                double[] point = layerGrid.transformCoordinates(i, j).getCoordinate();
                try {
                    transform.transform(point, 0, point, 0, 1);
                } catch (TransformException e) {
                    throw new WmsException("Could not convert coordinates from layer reference system to map reference system");
                }
                xcrd[k] = (float) point[0];
                ycrd[k] = (float) point[1];
                k++;
            }
        crds.add(xcrd);
        crds.add(ycrd);
    }

    /**
     * Builds an ImageProducer
     * @todo make error handling and validity-checking more consistent
     */
    public static final class Builder
    {
        private HorizontalGrid imageGrid;
        private HorizontalGrid layerGrid;
        private ColorPalette colorPalette = null;
        private int numColorBands = ColorMap.MAX_NUM_COLORS;
        private Color bgColor = Color.WHITE;
        private Color lowColor = null;
        private Color highColor = null;
        private boolean transparent = false;
        private int opacity = 100;
        private Range<Float> scaleRange = null;
        private Boolean logarithmic = null;
        private Style style = null;
        private float vectorScale = 1;
        private int numContours = 10;
        private int equator_y_index = 0;

        /** Sets map grid (contains the size of the picture and the CRS) */
        public Builder imageGrid(HorizontalGrid imageGrid) {
            this.imageGrid = imageGrid;
            return this;
        }

        /** Sets the layer grid */
        public Builder layerGrid(HorizontalGrid layerGrid) {
            this.layerGrid = layerGrid;
            return this;
        }

        /** Sets the style to be used.  If not set or if the parameter is null,
         * {@link Style#BOXFILL} will be used
         */
        public Builder style(Style style)  {
            this.style = style;
            return this;
        }

        /** Sets the colour palette.  If not set or if the parameter is null,
         * the default colour palette will be used.
         * {@see ColorPalette}
         */
        public Builder palette(ColorPalette palette) {
            this.colorPalette = palette;
            return this;
        }

        /** Sets the number of colour bands to use in the image, from 0 to 253
         * (default 254) */
        public Builder numColorBands(int numColorBands) {
            if (numColorBands < 0 || numColorBands > ColorMap.MAX_NUM_COLORS)
                throw new IllegalArgumentException("invalid number of color bands " + numColorBands);
            this.numColorBands = numColorBands;
            return this;
        }

        /** Sets the colour scale range.  If not set (or if set to null),
         * the min and max values of the data will be used.
         */
        public Builder colorScaleRange(Range<Float> scaleRange) {
            this.scaleRange = scaleRange;
            return this;
        }

        /**
         * Sets whether or not the colour scale is to be spaced logarithmically
         * (default is false)
         */
        public Builder logarithmic(Boolean logarithmic) {
            this.logarithmic = logarithmic;
            return this;
        }

        /** Sets whether or not background pixels should be transparent
         * (defaults to false) */
        public Builder transparent(boolean transparent) {
            this.transparent = transparent;
            return this;
        }

        /** Sets the opacity of the picture, from 0 to 100 (default 100) */
        public Builder opacity(int opacity) {
            if (opacity < 0 || opacity > 100) throw new IllegalArgumentException();
            this.opacity = opacity;
            return this;
        }
        
        /** Sets the vectorScale (defaults to 1.0) */
        public Builder vectorScale(float scale) {
            if (scale <= 0) throw new IllegalArgumentException();
            this.vectorScale = scale;
            return this;
        }

        /** Sets the yindex that the hemisphere switches to southern value is 0
         * if it does not touch the southern hemisphere.
         */
        public Builder equator_y_index(int equator_y_index) {
            this.equator_y_index = equator_y_index;
            return this;
        }

        /** Sets the number of contours to use in the image, from 2 (default 10) */
        public Builder numContours(int numContours) {
            if (numContours < 2) {
                throw new IllegalArgumentException();
            }
            this.numContours = numContours;
            return this;
        }

        /** Sets the background colour, which is used only if transparent==false,
         * for background pixels.  Defaults to white.  If the passed-in color
         * is null, it is ignored.
         */
        public Builder backgroundColor(Color bgColor) {
            if (bgColor != null) this.bgColor = bgColor;
            return this;
        }
        
        /** Sets the colour associated to values below the sale range. If the
         * passed-in colour is null, the lowest color in the palette is used.
         */
        public Builder belowMinColor(Color lowColor) {
            this.lowColor = lowColor;
            return this;
        }
        
        /** Sets the colour associated to values below the sale range. If the
         * passed-in colour is null, the lowest color in the palette is used.
         */
        public Builder aboveMaxColor(Color highColor) {
            this.highColor = highColor;
            return this;
        }

        /**
         * Checks the fields for internal consistency, then creates and returns
         * a new ImageProducer object.
         * @throws IllegalStateException if the builder cannot create a valid
         * ImageProducer object
         */
        public ImageProducer build()
        {
            ImageProducer ip = new ImageProducer();
            ip.layerGrid = this.layerGrid;
            ip.imageGrid = this.imageGrid;
            ip.vectorScale = this.vectorScale;
            ip.equator_y_index = this.equator_y_index;
            ip.numContours = this.numContours;
            ip.style = this.style == null
                ? Style.BOXFILL
                : this.style;
            ip.colorMap = new ColorMap(
                    colorPalette, numColorBands,
                    bgColor, lowColor, highColor,
                    transparent, lowColor == null, highColor == null,
                    0.01f * opacity);
            if (scaleRange == null || scaleRange.isEmpty())
            {
                ip.autoScale = true;
                ip.colorMap.setScaleType((logarithmic == null) ? false : logarithmic);
            }
            else
            {
                ip.autoScale = false;
                ip.colorMap.setScale(scaleRange.getMinimum(), scaleRange.getMaximum(),
                                     (logarithmic == null) ? false : logarithmic);
            }
            return ip;
        }
    }
}
