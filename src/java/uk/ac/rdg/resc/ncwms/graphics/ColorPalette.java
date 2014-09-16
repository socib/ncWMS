/*
 * Copyright (c) 2008 The University of Reading
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

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A palette of colours that is used by an {@link ImageProducer} to render 
 * data into a BufferedImage
 * @author Jon
 */
public class ColorPalette
{
    private static final Logger logger = LoggerFactory.getLogger(ColorPalette.class);
    
    private static final Map<String, ColorPalette> palettes =
        new HashMap<String, ColorPalette>();
    
    /**
     * The name of the default palette that will be used if the user doesn't 
     * request a specific palette.
     * @see DEFAULT_PALETTE
     */
    public static final String DEFAULT_PALETTE_NAME = "rainbow";
    
    /**
     * This is the palette that will be used if no specific palette has been
     * chosen.  This palette is taken from the SGT graphics toolkit.
     * @see DEFAULT_PALETTE_NAME
     */
    private static final ColorPalette DEFAULT_PALETTE = new ColorPalette(DEFAULT_PALETTE_NAME,
        new Color[] {
        new Color(0,0,143), new Color(0,0,159), new Color(0,0,175),
        new Color(0,0,191), new Color(0,0,207), new Color(0,0,223),
        new Color(0,0,239), new Color(0,0,255), new Color(0,11,255),
        new Color(0,27,255), new Color(0,43,255), new Color(0,59,255),
        new Color(0,75,255), new Color(0,91,255), new Color(0,107,255),
        new Color(0,123,255), new Color(0,139,255), new Color(0,155,255),
        new Color(0,171,255), new Color(0,187,255), new Color(0,203,255),
        new Color(0,219,255), new Color(0,235,255), new Color(0,251,255),
        new Color(7,255,247), new Color(23,255,231), new Color(39,255,215),
        new Color(55,255,199), new Color(71,255,183), new Color(87,255,167),
        new Color(103,255,151), new Color(119,255,135), new Color(135,255,119),
        new Color(151,255,103), new Color(167,255,87), new Color(183,255,71),
        new Color(199,255,55), new Color(215,255,39), new Color(231,255,23),
        new Color(247,255,7), new Color(255,247,0), new Color(255,231,0),
        new Color(255,215,0), new Color(255,199,0), new Color(255,183,0),
        new Color(255,167,0), new Color(255,151,0), new Color(255,135,0),
        new Color(255,119,0), new Color(255,103,0), new Color(255,87,0),
        new Color(255,71,0), new Color(255,55,0), new Color(255,39,0),
        new Color(255,23,0), new Color(255,7,0), new Color(246,0,0),
        new Color(228,0,0), new Color(211,0,0), new Color(193,0,0),
        new Color(175,0,0), new Color(158,0,0), new Color(140,0,0)
    });
    
    private final Color[] palette;
    private final String name;

    static
    {
        palettes.put(DEFAULT_PALETTE_NAME, DEFAULT_PALETTE);
    }
    
    private ColorPalette(String name, Color[] palette)
    {
        this.name = name;
        this.palette = palette;
    }
    
    /**
     * Gets the number of colours in this palette
     * @return the number of colours in this palette
     */
    public int getSize()
    {
        return this.palette.length;
    }
    
    /**
     * Gets the names of the supported palettes.
     * @return the names of the palettes as a Set of Strings.  All Strings
     * will be in lower case.
     */
    public static final Set<String> getAvailablePaletteNames()
    {
        return palettes.keySet();
    }
    
    /**
     * This is called by WmsController on initialization to load all the palettes
     * in the WEB-INF/conf/palettes directory.  This will attempt to load all files
     * with the file extension ".pal".
     * @param paletteLocationDir Directory containing the palette files.  This
     * has already been checked to exist and be a directory
     */
    public static final void loadPalettes(File paletteLocationDir)
    {
        for (File file : paletteLocationDir.listFiles())
        {
            if (file.getName().endsWith(".pal"))
            {
                try
                {
                    String paletteName = file.getName().substring(0, file.getName().lastIndexOf("."));
                    ColorPalette palette = new ColorPalette(paletteName, readColorPalette(new FileReader(file)));
                    logger.debug("Read palette with name {}", paletteName);
                    palettes.put(palette.getName().trim().toLowerCase(), palette);
                }
                catch(Exception e)
                {
                    logger.error("Error reading from palette file {}", file.getName(), e);
                }
            }
        }
    }
    
    /**
     * Gets the palette with the given name.
     * @param name Name of the palette, corresponding with the name of the
     * palette file in WEB-INF/conf/palettes. Case insensitive.
     * @return the ColorPalette object, or null if there is no palette with
     * the given name.  If name is null or the empty string this will return
     * the default palette.
     */
    public static ColorPalette get(String name) throws IllegalArgumentException
    {
        ColorPalette palette;
        if (name == null || name.trim().equals(""))
            palette = palettes.get(DEFAULT_PALETTE_NAME);
        else
            palette = palettes.get(name.trim().toLowerCase());
        if(palette == null)
            throw new IllegalArgumentException("unknown palette " + name);
        return palette;
    }

    /** Gets the name of this palette */
    public String getName() { return this.name; }
    
    /** Gets the sequence of colors of the palette in an array.
     * @return array of colors of the palette.
     */
    public Color[] getColors() {
        return palette;
    }
    
    /**
     * Reads a color palette (as an array of Color object) from the given File.
     * Each line in the file contains a single colour, expressed as space-separated
     * RGB or ARGB values.  These values can be integers in the range 0->255 
     * or floats in the range 0->1.  If the palette cannot be read, no exception
     * is thrown but an event is logged to the error log.
     * @throws Exception if the palette file could not be read or contains a
     * format error
     */
    private static Color[] readColorPalette(Reader paletteReader) throws Exception
    {
        BufferedReader reader = new BufferedReader(paletteReader);
        List<Color> colors = new ArrayList<Color>();
        String line;
        int lineno = 0;
        try
        {
            while((line = reader.readLine()) != null)
            {
                lineno++;
                Color color;
                Float a, r, g, b;
                float[] components = {-1.0f, -1.0f, -1.0f, -1.0f};
                int ncomponents = 0;
                boolean comment = false;
                StringTokenizer tok = new StringTokenizer(line.trim());
                while (ncomponents < 4 && tok.hasMoreTokens() && ! comment)
                {
                    String token = tok.nextToken();
                    if (token.startsWith("#")) {
                       comment = true;
                    } else {
                       components[ncomponents++] = Float.valueOf(token);
                    }
                }
                if (ncomponents == 0)
                   continue;
                if (ncomponents < 3)
                   throw new Exception("missing components");
                b = components[ncomponents-1];
                g = components[ncomponents-2];
                r = components[ncomponents-3];
                a = ncomponents == 4 ? components[0] : 1.0f;
                if (r <   0.0f || g <   0.0f || b <   0.0f || a <   0.0f ||
                    r > 255.0f || g > 255.0f || b > 255.0f || a > 255.0f) {
                    throw new Exception("invalid component value");
                }
                if (r > 1.0f || g > 1.0f || b > 1.0f || a > 1.0f)
                {
                    // color expressed in the range 0->255
                    color = new Color(r.intValue(), g.intValue(), b.intValue(),
                                      ncomponents == 4 ? a.intValue() : 255);
                } else {
                    // color expressed in the range 0->1
                    color = new Color(r, g, b, a);
                }
                colors.add(color);
            }
        }
        catch(Exception e)
        {
            throw new RuntimeException("File format error at line " + lineno +": "
                + " each line must contain 3 or 4 numbers (R G B) or (A R G B)"
                + " between 0 and 255 or 0.0 and 1.0 (" + e.getMessage() + ").");
        }
        finally
        {
            if (reader != null) reader.close();
        }
        return colors.toArray(new Color[0]);
    }
    
    public static void addPalette(String name, Reader reader){
        try {
            ColorPalette palette = new ColorPalette(name, readColorPalette(reader));
            if(!palettes.containsKey(palette.getName()))
                palettes.put(palette.getName(), palette);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
