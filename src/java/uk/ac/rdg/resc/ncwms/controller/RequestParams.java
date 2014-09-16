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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.awt.Color;

import uk.ac.rdg.resc.edal.util.Range;
import uk.ac.rdg.resc.edal.util.Ranges;
import uk.ac.rdg.resc.ncwms.exceptions.WmsException;

/**
 * Class that contains the parameters of the user's request.  Parameter names
 * are not case sensitive.
 *
 * @author Jon Blower
 */
public class RequestParams
{
    private Map<String, String> paramMap = new HashMap<String, String>();

    /**
     * Creates a new RequestParams object from the given Map of parameter names
     * and values (normally gained from HttpServletRequest.getParameterMap()).
     * The Map matches parameter names (Strings) to parameter values (String
     * arrays).
     */
    public RequestParams(Map<String, String[]> httpRequestParamMap)
    {
        for (String name : httpRequestParamMap.keySet())
        {
            String[] values = httpRequestParamMap.get(name);
            assert values.length >= 1;
            try
            {
                String key = URLDecoder.decode(name.trim(), "UTF-8").toLowerCase();
                String value = URLDecoder.decode(values[0].trim(), "UTF-8");
                this.paramMap.put(key, value);
            }
            catch(UnsupportedEncodingException uee)
            {
                // Shouldn't happen: UTF-8 should always be supported
                throw new AssertionError(uee);
            }
        }
    }

    /**
     * Returns the value of the parameter with the given name as a String, or null if the
     * parameter does not have a value.  This method is not sensitive to the case
     * of the parameter name.  Use getWmsVersion() to get the requested WMS version.
     */
    public String getString(String paramName)
    {
        return this.paramMap.get(paramName.toLowerCase());
    }

    /**
     * Returns the value of the parameter with the given name, throwing a
     * WmsException if the parameter does not exist.  Use getMandatoryWmsVersion()
     * to get the requested WMS version.
     */
    public String getMandatoryString(String paramName) throws WmsException
    {
        String value = this.getString(paramName);
        if (value == null)
            throw new WmsException("Must provide a value for parameter " +
                                   paramName.toUpperCase());
        return value;
    }

    /**
     * Finds the WMS version that the user has requested.  This looks for both
     * WMTVER and VERSION, the latter taking precedence.  WMTVER is used by
     * older versions of WMS and older clients may use this in version negotiation.
     * @return The request WMS version as a string, or null if not set
     */
    public String getWmsVersion()
    {
        String version = this.getString("version");
        if (version == null)
            version = this.getString("wmtver");
        return version; // might be null
    }

    /**
     * Finds the WMS version that the user has requested, throwing a WmsException
     * if a version has not been set.
     * @return The request WMS version as a string
     * @throws WmsException if neither VERSION nor WMTVER have been requested
     */
    public String getMandatoryWmsVersion() throws WmsException
    {
        String version = this.getWmsVersion();
        if (version == null)
            throw new WmsException("Must provide a value for VERSION");
        return version;
    }

    /**
     * Returns the value of the parameter with the given name as a positive integer,
     * or the provided default if no parameter with the given name has been supplied.
     * Throws a WmsException if the parameter does not exist or if the value
     * is not a valid positive integer.  Zero is counted as a positive integer.
     */
    public int getPositiveInt(String paramName, int defaultValue) throws WmsException
    {
        String value = this.getString(paramName);
        if (value == null) return defaultValue;
        return parsePositiveInt(paramName, value);
    }

    /**
     * Returns the value of the parameter with the given name as a positive integer,
     * throwing a WmsException if the parameter does not exist or if the value
     * is not a valid positive integer.  Zero is counted as a positive integer.
     */
    public int getMandatoryPositiveInt(String paramName) throws WmsException
    {
        String value = this.getString(paramName);
        if (value == null)
            throw new WmsException("Must provide a value for parameter " +
                                   paramName.toUpperCase());
        return parsePositiveInt(paramName, value);
    }

    private static int parsePositiveInt(String paramName, String value) throws WmsException
    {
        try
        {
            int i = Integer.parseInt(value);
            if (i < 0)
                throw new WmsException("Parameter " + paramName.toUpperCase() +
                                       " must be a valid positive integer");
            return i;
        }
        catch(NumberFormatException nfe)
        {
            throw new WmsException("Parameter " + paramName.toUpperCase() +
                " must be a valid positive integer");
        }
    }

    /**
     * Returns the value of the parameter with the given name, or the supplied
     * default value if the parameter does not exist.
     */
    public String getString(String paramName, String defaultValue)
    {
        String value = this.getString(paramName);
        if (value == null) return defaultValue;
        return value;
    }

    /**
     * Returns the value of the parameter with the given name as a boolean value,
     * or the provided default if no parameter with the given name has been supplied.
     * @throws WmsException if the value is not a valid boolean string.
     */
    public boolean getBoolean(String paramName, boolean defaultValue) throws WmsException
    {
        String value = this.getString(paramName);
        if (value == null) return defaultValue;
        value = value.trim();
        if ("true".equalsIgnoreCase(value)) return true;
        if ("false".equalsIgnoreCase(value)) return false;
        throw new WmsException("Invalid boolean value for parameter " + paramName);
    }

    /**
     * Returns the value of the parameter with the given name as a boolean value,
     * throwing a WmsException if the parameter does not exist or if the value
     * is not a valid positive integer.  Zero is counted as a positive integer.
     * Throws a WmsException if the parameter does not exist or if the value
     * is not a valid boolean string  ("true" or "false", case-insensitive).
     * @throws WmsException 
     */
    public boolean getMandatoryBoolean(String paramName) throws WmsException
    {
        String value = this.getString(paramName);
        if (value == null)
            throw new WmsException("Parameter " + paramName.toUpperCase() +
                                   " must be a valid positive integer");
        value = value.trim();
        if ("true".equalsIgnoreCase(value)) return true;
        if ("false".equalsIgnoreCase(value)) return false;
        throw new WmsException("Invalid boolean value for parameter " + paramName);
    }

    /**
     * Returns the value of the parameter with the given name, or the supplied
     * default value if the parameter does not exist.
     * @throws WmsException if the value of the parameter is not a valid
     * floating-point number
     */
    public float getFloat(String paramName, float defaultValue) throws WmsException
    {
        String value = this.getString(paramName);
        if (value == null)
        {
            return defaultValue;
        }
        try
        {
            return Float.parseFloat(value);
        }
        catch(NumberFormatException nfe)
        {
            throw new WmsException("Parameter " + paramName.toUpperCase() +
                " must be a valid floating-point number");
        }
    }

    /**
     * Returns the value of the parameter with the given name as a Color value,
     * or the supplied default value if the parameter does not exist.
     * Color codes should have the format 0xRRGGBB or #RRGGBB.
     * Throws a WmsException if the parameter is not a valid color code.
     * @throws WmsException
     */ 
    public Color getColor(String paramName, Color defaultValue) throws WmsException
    {
       String value = this.getString(paramName);
       if (value == null)
           return defaultValue;
       try
       {
           return Color.decode(value);
       }
       catch(NumberFormatException nfe)
       {
           throw new WmsException("Parameter " + paramName.toUpperCase() +
               " must be a valid color code (0xXXXXXX or #000000).");
       }
    }

    /**
     * Returns the value of the parameter with the given name as a range.
     * or the supplied default value if the parameter does not exist.
     * The parameter should be comma-separated-pair of float numbers.
     * Throws a WmsException if the parameter is not a valid range.
     * @throws WmsException 
     */ 
    public Range<Float> getFloatRange(String paramName, Range<Float> defaultValue) throws WmsException
    {
        String value = this.getString(paramName);
        if (value == null)
            return defaultValue;
        String[] tokens = value.split(",");
        if (tokens.length != 2)
            throw new WmsException("Invalid range format for parameter " + paramName.toUpperCase() + 
                                   " (not a comma-separated-pair of numbers)");
        float rangeMin = Float.parseFloat(tokens[0]);
        float rangeMax = Float.parseFloat(tokens[1]);
        if (Float.compare(rangeMin, rangeMax) > 0)
            throw new WmsException("Invalid range format for parameter " + paramName.toUpperCase() +
                                   "(min is greater than max)");
        return Ranges.newRange(rangeMin, rangeMax);
    }
}
