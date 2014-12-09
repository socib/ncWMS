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

package uk.ac.rdg.resc.ncwms.controller;

import java.awt.Color;

import uk.ac.rdg.resc.ncwms.exceptions.WmsException;
import uk.ac.rdg.resc.edal.util.Range;
import uk.ac.rdg.resc.edal.util.Ranges;
import uk.ac.rdg.resc.ncwms.graphics.plot.ColorMap;

/**
 * Contains those portions of a GetMap request that pertain to styling and
 * image generation.
 *
 * @author Jon Blower
 * $Revision$
 * $Date$
 * $Log$
 */
public class GetMapStyleRequest
{
    private String[] styles;              // Image style and palette.
    private String imageFormat;           // Image format's MIME type.
    private boolean transparent;          // Use transparent background.
    private Color backgroundColor;        // Background color.
    private Color aboveMaxColor;          // Color for values above the scale range.
    private Color belowMinColor;          // Color for values below the scale range.
    private int opacity;                  // Opacity of the image in the range [0,100]
    private int numColorBands;            // Number of color bands to use in the image
    private int numContours;              // Number of contours to use in the image
    private float markerScale;            // The scale of the markers for vector plots.
    private float markerSpacing;          // The space of the markers for vector plots.
    private Boolean logarithmic;          // True if we're using a log scale, false if linear and null if not specified
    private Range<Float> colorScaleRange; // The limits of the color scale. 
    
    /**
     * Creates a new instance of GetMapStyleRequest from the given parameters
     * @throws WmsException if the request is invalid
     */
    public GetMapStyleRequest(RequestParams params) throws WmsException
    {
        this.styles = getStyles(params);
        this.imageFormat = getImageFormat(params);
        this.transparent = isTransparent(params);
        this.backgroundColor = getBackgroundColor(params);
        this.belowMinColor = getBelowMinColor(params);
        this.aboveMaxColor = getAboveMaxColor(params);
        this.opacity = getOpacity(params);
        this.numColorBands = getNumColorBands(params);
        this.numContours = getNumContours(params);
        this.markerScale = getMarkerScale(params);
        this.markerSpacing = getMarkerSpacing(params);
        this.colorScaleRange = getColorScaleRange(params);
        this.logarithmic = isLogScale(params);
    }

    /**
     * Extract the list of styles from the request parameters.
     * The expected value is a comma-separated-list of names of styles.
     * @param params the request parameters.
     * @return the list of the names of the styles in the request.
     * @throws WmsException if there is no style parameter.
     */
    public static String[] getStyles(RequestParams params) throws WmsException
    {
        String stylesStr = params.getMandatoryString("styles");
        String[] styles;
        if (stylesStr.trim().isEmpty())
            styles = new String[0];
        else
            styles = stylesStr.split(",");
        return styles;
    }

    /**
     * Extract the MIME type of the image format from the request parameters.
     * @param params the request parameters.
     * @return the name of MIME type of the image format.
     * @throws WmsException if there is no format parameter.
     */
    public static String getImageFormat(RequestParams params) throws WmsException
    {
        return params.getMandatoryString("format").replaceAll(" ", "+");
    }

    /**
     * Extract the transparent flag from the request parameters (default false).
     * @param params the request parameters.
     * @return whether the background should be transparent.
     * @throws WmsException if the parameter value is invalid.
     */
    public static boolean isTransparent(RequestParams params) throws WmsException
    {
        return params.getBoolean("transparent", false);
    }

    /**
     * Extract the background color from the request parameters (default white).
     * @param params the request parameters.
     * @return whether the background should be transparent.
     * @throws WmsException if the parameter value is invalid.
     */
    public static Color getBackgroundColor(RequestParams params) throws WmsException
    {
        return params.getColor("bgcolor", Color.WHITE);
    }

    /**
     * Extract the color for low data from the request parameters (default black).
     * The expected values is a color name or code, or one of these values:
     *   "extend": return null (the lowest value in the color map will be used).
     *   "transparent": return a fully transparent black color instance.
     * @param params the request parameters.
     * @return the color to use for low data, or null for an extended color map.
     * @throws WmsException if the parameter value is invalid.
     */
    public static Color getBelowMinColor(RequestParams params) throws WmsException
    {
        String belowMinColorStr = params.getString("belowmincolor");
        if ("extend".equalsIgnoreCase(belowMinColorStr)) {
            return null;
        } else if ("transparent".equalsIgnoreCase(belowMinColorStr)) {
            return new Color(0, 0, 0, 0);
        } else {
            return params.getColor("belowmincolor", Color.black);
        }
    }

    /**
     * Extract the color for high data from the request parameters (default black).
     * The expected values is a color name or code, or one of these values:
     *   "extend": return null (the lowest value in the color map will be used).
     *   "transparent": return a fully transparent black color instance.
     * @param params the request parameters.
     * @return the color to use for high data, or null for an extended color map.
     * @throws WmsException if the parameter value is invalid.
     */
    public static Color getAboveMaxColor(RequestParams params) throws WmsException
    {
        String aboveMaxColorStr = params.getString("abovemaxcolor");
        if ("extend".equalsIgnoreCase(aboveMaxColorStr)) {
            return null;
        } else if ("transparent".equalsIgnoreCase(aboveMaxColorStr)) {
            return new Color(0, 0, 0, 0);
        } else {
            return params.getColor("abovemaxcolor", Color.black);
        }
    }

    /**
     * Extract the opacity factor from the request parameters (default 100, fully opaque).
     * @param params the request parameters.
     * @return the opacity factor truncated to the range [0, 100].
     * @throws WmsException if the parameter value is invalid.
     */
    public static int getOpacity(RequestParams params) throws WmsException
    {
        int opacity = params.getPositiveInt("opacity", 100);
        if (opacity > 100) opacity = 100;
        return opacity;
    }

    /**
     * Extract the range of the color scale from the request parameters.
     * The expected value is a range, or one of these values:
     *   "default": use the default scale range for the layer.
     *   "auto": adjust the color scale to the range of the data in the image.
     * @param params the request parameters.
     * @return the color scale range, an empty range for auto-scale or null for default range for the layer.
     * @throws WmsException if the parameter value is invalid.
     */
    public static Range<Float> getColorScaleRange(RequestParams params) throws WmsException
    {
        String colorScaleRangeStr = params.getString("colorscalerange");
        if ("default".equalsIgnoreCase(colorScaleRangeStr))
        {
            // The client wants the layer's default scale range to be used
            return null;
        }
        else if ("auto".equalsIgnoreCase(colorScaleRangeStr))
        {
            // The client wants the image to be scaled according to the image's
            // own min and max values (giving maximum contrast)
            return Ranges.emptyRange();
        }
        else
        {
            return params.getFloatRange("colorscalerange", null);
        }
    }

    /**
     * Extract the number of bands of the color map from the request parameters.
     * The requested number of bands is truncated to the maximum number allowed.
     * @param params the request parameters.
     * @return the number of color bands (default to maximum number allowed).
     * @throws WmsException if the parameter value is invalid.
     */
    public static int getNumColorBands(RequestParams params) throws WmsException
    {
        int numColorBands = params.getPositiveInt("numcolorbands", ColorMap.MAX_NUM_COLORS);
        if (numColorBands > ColorMap.MAX_NUM_COLORS)
            numColorBands = ColorMap.MAX_NUM_COLORS;
        return numColorBands;
    }

    /**
     * Extract the number of contours from the request parameters.
     * @param params the request parameters.
     * @return the number of contour levels (default 10).
     * @throws WmsException if the parameter value is invalid.
     */
    public static int getNumContours(RequestParams params) throws WmsException
    {
        int numContours = params.getPositiveInt("numcontours", 10);
        if (numContours < 2)
            numContours = 10;
        return numContours;
    }

    /**
     * Extract the logarithmic flag from the request parameters.
     * @param params the request parameters.
     * @return whether the scale is logarithmic or not, or null to use the default value for the layer.
     * @throws WmsException if the parameter value is invalid.
     */
    public static Boolean isLogScale(RequestParams params) throws WmsException
    {
        String logScaleStr = params.getString("logscale");
        if (logScaleStr == null)
            return null;
        else
            return params.getMandatoryBoolean("logscale");
    }

    /**
     * Extract the scale of vector markers from the request parameters.
     * @param params the request parameters.
     * @return the requested vector scale, or the default value 14.0.
     * @throws WmsException if the parameter value is invalid.
     */
    public static float getMarkerScale(RequestParams params) throws WmsException
    {
        return params.getFloat("markerscale", 14.0f);
    }

    /**
     * Extract the spacing of vector markers from the request parameters.
     * @param params the request parameters.
     * @return the requested vector spacing, or the marker scale value.
     * @throws WmsException if the parameter value is invalid.
     */
    public static float getMarkerSpacing(RequestParams params) throws WmsException
    {
        return params.getFloat("markerspacing", getMarkerScale(params));
    }

    /**
     * Get the array of style names requested by the client.
     */
    public String[] getStyles()
    {
        return styles;
    }

    /**
     * Get the MIME type of the image format requested by the client.
     */
    public String getImageFormat()
    {
        return imageFormat;
    }

    /**
     * Get the scale type requested by the client.
     */
    public Boolean isScaleLogarithmic()
    {
        return logarithmic;
    }

    /**
     * Get the transparent background choice requested by the client.
     */
    public boolean isTransparent()
    {
        return transparent;
    }

    /**
     * Get the background color choice requested by the client.
     */
    public Color getBackgroundColor()
    {
        return backgroundColor;
    }

    /**
     * Get the low color choice requested by the client.
     */
    public Color getBelowMinColor()
    {
        return belowMinColor;
    }

    /**
     * Get the high color choice requested by the client.
     */
    public Color getAboveMaxColor()
    {
        return aboveMaxColor;
    }

    /**
     * Get the opacity factor requested by the client.
     */
    public int getOpacity()
    {
        return opacity;
    }

    /**
     * Get the color scale range requested by the client.
     */
    public Range<Float> getColorScaleRange()
    {
        return colorScaleRange;
    }

    /**
     * Get the number of color bands requested by the client.
     */
    public int getNumColorBands()
    {
        return numColorBands;
    }

    /**
     * Get the number of contour levels requested by the client.
     */
    public int getNumContours() {
        return numContours;
    }

    /**
     * Get the scale of vector markers requested by the client.
     */
    public float getMarkerScale()
    {
        return markerScale;
    }

    /**
     * Get the spacing of vector markers requested by the client.
     */
    public float getMarkerSpacing()
    {
        return markerSpacing;
    }

}
