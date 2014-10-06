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
import uk.ac.rdg.resc.ncwms.graphics.ColorMap;

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
    private String[] styles;
    private String imageFormat;           // Image format's MIME type.
    private boolean transparent;          // Use transparent background.
    private Color backgroundColor;        // Background color.
    private Color aboveMaxColor;          // Color for values above the scale range.
    private Color belowMinColor;          // Color for values below the scale range.
    private int opacity;                  // Opacity of the image in the range [0,100]
    private int numColorBands;            // Number of color bands to use in the image
    private int numContours;              // Number of contours to use in the image
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
        this.colorScaleRange = getColorScaleRange(params);
        this.numColorBands = getNumColorBands(params);
        this.numContours = getNumContours(params);
        this.logarithmic = isLogScale(params);
    }
    
    /**
     * Gets the list of styles requested by the client
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
    
    public static String getImageFormat(RequestParams params) throws WmsException
    {
        return params.getMandatoryString("format").replaceAll(" ", "+");
    }
    
    public static boolean isTransparent(RequestParams params) throws WmsException
    {
        return params.getBoolean("transparent", false);
    }
    
    public static Color getBackgroundColor(RequestParams params) throws WmsException
    {
        return params.getColor("bgcolor", Color.WHITE);
    }
    
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
    
    public static int getOpacity(RequestParams params) throws WmsException
    {
        int opacity = params.getPositiveInt("opacity", 100);
        if (opacity > 100) opacity = 100;
        return opacity;
    }

    /**
     * Gets the ColorScaleRange object requested by the client
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
     * Gets the number of color bands requested by the client, or {@link ColorPalette#MAX_NUM_COLOURS} if none
     * has been set or the requested number was bigger than {@link ColorPalette#MAX_NUM_COLOURS}.
     * @throws WmsException if the client requested a negative number of color
     * bands
     */
    public static int getNumColorBands(RequestParams params) throws WmsException
    {
        int numColorBands = params.getPositiveInt("numcolorbands", ColorMap.MAX_NUM_COLORS);
        if (numColorBands > ColorMap.MAX_NUM_COLORS)
            numColorBands = ColorMap.MAX_NUM_COLORS;
        return numColorBands;
    }
    
    /**
     * Gets the number of contours requested by the client, or 10 if none
     * has been set or the requested number was less than 2.
     * @param params The RequestParams object from the client.
     * @return the requested number of contours
     * @throws WmsException if the client requested a negative number of contours
     */
    public static int getNumContours(RequestParams params) throws WmsException
    {
        int numContours = params.getPositiveInt("numcontours", 10);
        if (numContours < 2)
            numContours = 10;
        return numContours;
    }
    
    /**
     * Returns {@link Boolean#TRUE} if the client has requested a logarithmic scale,
     * {@link Boolean#FALSE} if the client has requested a linear scale,
     * or null if the client did not specify.
     * @throws WmsException if the client specified a value that is not
     * "true" or "false" (case not important).
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
     * Returns the array of style names in comma-separated-list parameter STYLES,
     * or an empty array if the user specified "STYLES=".
     */
    public String[] getStyles()
    {
        return styles;
    }

    /**
     * Returns the MIME type given in parameters.
     */
    public String getImageFormat()
    {
        return imageFormat;
    }

    /**
     * Returns {@link Boolean#TRUE} if the client has requested a logarithmic scale,
     * {@link Boolean#FALSE} if the client has requested a linear scale,
     * or null if the client did not specify.
     */
    public Boolean isScaleLogarithmic()
    {
        return logarithmic;
    }

    public boolean isTransparent()
    {
        return transparent;
    }

    public Color getBackgroundColor()
    {
        return backgroundColor;
    }
    

    public Color getBelowMinColor() {
        return belowMinColor;
    }

    public Color getAboveMaxColor() {
        return aboveMaxColor;
    }

    public int getOpacity()
    {
        return opacity;
    }

    /**
     * Gets the values that will correspond with the extremes of the colour 
     * scale.  Returns null if the client has not specified a scale range or
     * if the default scale range is to be used.  Returns an empty Range if
     * the client wants the image to be auto-scaled according to the image's own
     * min and max values.
     */
    public Range<Float> getColorScaleRange()
    {
        return colorScaleRange;
    }

    public int getNumColorBands()
    {
        return numColorBands;
    }
    
    public int getNumContours() {
        return numContours;
    }
}
