package uk.ac.rdg.resc.ncwms.graphics;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

import uk.ac.rdg.resc.ncwms.graphics.plot.MarkerStyle;
import uk.ac.rdg.resc.ncwms.graphics.plot.RasterStyle;

/**
 * Enumeration class to represent supported map image styles.
 */
public enum ImageStyle {
    
    CONTOUR    (null, null, null),
    BOXFILL    (RasterStyle.BOXFILL,   null, null),
    AREAFILL   (RasterStyle.AREAFILL,  null, null),
    SHADEFILL  (RasterStyle.SHADEFILL, null, null),
    VECTOR     (RasterStyle.BOXFILL,   MarkerStyle.PRETTY,   Color.BLACK),
    // BARB       (null,                  MarkerStyle.BARB,   null),
    PRETTYVEC  (null,                  MarkerStyle.PRETTY,   null),
    STUMPVEC   (null,                  MarkerStyle.STUMPY,   null),
    TRIVEC     (null,                  MarkerStyle.TRIANGLE, null),
    LINEVEC    (null,                  MarkerStyle.LINE,     null),
    FANCYVEC   (null,                  MarkerStyle.FANCY,    null);
    
    private RasterStyle rasterStyle;
    private MarkerStyle markerStyle;
    private Color markerColor;
    private ImageStyle(RasterStyle rasterStyle,
                       MarkerStyle markerStyle,
                       Color markerColor)
    {
        this.rasterStyle = rasterStyle;
        this.markerStyle = markerStyle;
        this.markerColor = markerColor;
    }
    
    public static final ImageStyle DEFAULT_STYLE = BOXFILL;
    
    /**
     * Get the image style with the given name.
     * @param name the name of the image style.
     * @return the style with given name, or the default style if name is null or empty.
     * @throws IllegalArgumentException if there is no style with given name.
     */
    public static ImageStyle get(String name) throws IllegalArgumentException
    {
        if (name == null || name.trim().equals(""))
            return DEFAULT_STYLE;
        else
            return ImageStyle.valueOf(name.trim().toUpperCase());
    }
    
    /**
     * Get the names of the supported styles.
     * @return the set of the names of the supported styles.
     */
    public static Set<String> getAvailableStyleNames()
    {
        ImageStyle[] styles = ImageStyle.values();
        Set<String> names = new HashSet<String>(styles.length);
        for (ImageStyle style : styles)
            names.add(style.getName());
        return names;
    }
    
    /**
     * Check if the style of a map plot is raster.
     * @return whether the style of the plot is raster or not.
     */
    public boolean isRaster()
    {
        return this.rasterStyle != null;
    }
    
    /**
     * Check if the style of a map plot is marker (some kind of vector marker).
     * @return whether the style of the plot is marker.
     */
    public boolean isMarker()
    {
        return this.markerStyle != null;
    }

    /**
     * Check if the style of a map plot is a constant color marker.
     * @return whether the style of the plot is a constant color marker.
     */
    public boolean isConstantColorMarker()
    {
        return this.markerColor != null;
    }

    /**
     * Check if the style of the map plot is contour.
     * @return whether the style of the plot is contour.
     */
    public boolean isContour()
    {
        return this == CONTOUR;
    }

    /**
     * Get the fill style for a raster plot from a map image style.
     * @return the proper raster fill style, or null if not a raster style.
     */
    public RasterStyle getRasterStyle()
    {
        return this.rasterStyle;
    }

    /**
     * Get the marker style for a vector plot from a map images style.
     * @return the proper marker style, or null if not a vector style.
     */
    public MarkerStyle getMarkerStyle()
    {
        return this.markerStyle;
    }

    /**
     * Get the marker color for a vector plot from a map image style.
     * @return the marker color, or null if marker should not use a color.
     */
    public Color getMarkerColor()
    {
        return this.markerColor;
    }
    
    /**
     * Get the name of a map image style.
     * @return
     */
    public String getName()
    {
        return toString().toLowerCase();
    }
}
