/*
 * Copyright (c) 2009 The University of Reading
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

package uk.ac.rdg.resc.edal.cdm;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates;
import uk.ac.rdg.resc.edal.geometry.LonLatPosition;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDatatype;
import uk.ac.rdg.resc.edal.cdm.CurvilinearGrid.Cell;
import uk.ac.rdg.resc.edal.cdm.kdtree.KDTree;
import uk.ac.rdg.resc.edal.cdm.kdtree.Point;
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.coverage.grid.impl.GridCoordinatesImpl;
import uk.ac.rdg.resc.edal.coverage.grid.impl.RegularGridImpl;
import uk.ac.rdg.resc.edal.geometry.impl.LonLatPositionImpl;
import uk.ac.rdg.resc.edal.util.CollectionUtils;
import uk.ac.rdg.resc.edal.util.Range;
import uk.ac.rdg.resc.edal.util.Ranges;
import uk.ac.rdg.resc.ncwms.graphics.ColorPalette;
import uk.ac.rdg.resc.ncwms.graphics.ImageProducer;
import uk.ac.rdg.resc.ncwms.graphics.ImageProducer.Style;

/**
 * A HorizontalGrid that uses an KdTree to look up the nearest neighbour of a point.
 */
final class KdTreeGridTest
{
    public static void main(String[] args) throws Exception
    {
    NetcdfDataset nc = NetcdfDataset.openDataset(//"C:\\Godiva2_data\\UCA25D\\UCA25D.20101118.04.nc");
//               "C:\\Godiva2_data\\ORCA025-R07-MEAN\\Exp4-Annual\\ORCA025-R07_y2004_ANNUAL_gridT2.nc");
                 "C:\\Godiva2_data\\EUMETSAT_TEST\\xc_yc\\W_XX-EUMETSAT-Darmstadt,VIS+IR+IMAGERY,MET7+MVIRI_C_EUMS_20091110120000.nc");
        GridDatatype grid = CdmUtils.getGridDatatype(nc, "ch1"); ///*"sossheig_sqd"); //*/"sea_level");
    int size = 256;

        CurvilinearGrid cv = new CurvilinearGrid(grid.getCoordinateSystem());
        int nans = 0;
        int nonnans = 0;
        for (Cell c : cv.getCells()) {
            if (Double.isNaN(c.getCentre().getLongitude()))
            {
                nans++;
            }
            else
            {
                nonnans++;
            }
        }
        System.out.println("nans: " + nans + ", nonnans: " + nonnans);

        if (1 == 1) return;

    // Read the data from the source
    long start = System.nanoTime();
    float[] data = (float[])grid.readDataSlice(0, 0, -1, -1).get1DJavaArray(float.class);
    List<Float> dataList = CollectionUtils.listFromFloatArray(data, null);
    Range<Float> dataRange = Ranges.findMinMax(dataList);
    System.out.println("Read whole data in " + getTimingMs(start) + " ms");

    ImageProducer ip = new ImageProducer.Builder()
    .palette(ColorPalette.get(null))
    .style(Style.BOXFILL)
    .height(size)
    .width(size)
    .colourScaleRange(dataRange)
    .build();

//        start = System.nanoTime();
//        CurvilinearGrid curvGrid = new CurvilinearGrid(grid.getCoordinateSystem());
//        BufferedImage vg = vectorGraphic(dataList, curvGrid, ip);
//        System.out.println("Created vector graphic in " + getTimingMs(start) + " ms");
//        ImageIO.write(vg, "png", new File("c:\\vector.png"));

    start = System.nanoTime();
        AbstractCurvilinearGrid kdTreeGrid = LookUpTableGrid.generate(grid.getCoordinateSystem());
        System.out.println("Generated index in " + getTimingMs(start) + " ms");
    HorizontalGrid targetDomain = new RegularGridImpl(kdTreeGrid.getExtent(), size, size);

        start = System.nanoTime();
        PixelMap pixelMap = new PixelMap(kdTreeGrid, targetDomain);
        System.out.println("Created PixelMap in " + getTimingMs(start) + " ms");

        start = System.nanoTime();

        // It's very unlikely that the target domain will be bigger than
        // Integer.MAX_VALUE
        List<Float> newData = CdmUtils.readHorizontalPoints(nc, grid, 0, 0, pixelMap, (int)targetDomain.size());
        System.out.println("Read data in " + getTimingMs(start) + " ms");
        nc.close();

    start = System.nanoTime();
    ip.addFrame(newData, null);
    List<BufferedImage> ims = ip.getRenderedFrames();
    ImageIO.write(ims.get(0), "png", new File("C:\\lookup.png"));
    System.out.println("Created and wrote image in " + getTimingMs(start) + " ms");

    }

    private static BufferedImage vectorGraphic(List<Float> data, CurvilinearGrid curvGrid, ImageProducer ip)
    throws IOException
    {
    int width = ip.getImageWidth();
    int height = ip.getImageHeight();
    IndexColorModel cm = ip.getColorModel();
    int[] rgbs = new int[cm.getMapSize()];
    cm.getRGBs(rgbs);
    BufferedImage im = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_INDEXED, cm);
    Graphics2D g2d = im.createGraphics();

    GeographicBoundingBox bbox = curvGrid.getBoundingBox();

    double lonDiff = bbox.getEastBoundLongitude() - bbox.getWestBoundLongitude();
    double latDiff = bbox.getNorthBoundLatitude() - bbox.getSouthBoundLatitude();

    // This ensures that the highest value of longitude (corresponding
    // with nLon - 1) is getLonMax()
    double lonStride = lonDiff / (width - 1);
    double latStride = latDiff / (height - 1);

    // Create the transform.  We scale by the inverse of the stride length
    AffineTransform transform = new AffineTransform();
    transform.scale(1.0/lonStride, 1.0/latStride);
    // Then we translate by the minimum coordinate values
    transform.translate(-bbox.getWestBoundLongitude(), -bbox.getSouthBoundLatitude());

    g2d.setTransform(transform);

    // Populate the BufferedImages using the information from the curvilinear grid
    // Iterate over all the cells in the grid, painting the i and j indices of the
    // cell onto the BufferedImage
    for (Cell cell : curvGrid.getCells())
    {
    int index = cell.getI() + curvGrid.getNi() * cell.getJ();
    Float val = data.get(index);
    int colourIndex = ip.getColourIndex(val);

    // Get a Path representing the boundary of the cell
    Path2D path = cell.getBoundaryPath();
    //g2d.setPaint(new Color(rgbs[colourIndex]));
    g2d.setPaint(new Color(cm.getRGB(colourIndex)));
    g2d.fill(path);

    // We paint a second copy of the cell, shifted by 360 degrees, to handle
    // the anti-meridian
    // TODO: this bit increases runtime by 30%
    double shiftLon = cell.getCentre().getLongitude() > 0.0
    ? -360.0
    : 360.0;
    path.transform(AffineTransform.getTranslateInstance(shiftLon, 0.0));
    g2d.fill(path);
    }

    return im;
    }

    private static double getTimingMs(long startNs)
    {
        long finishNs = System.nanoTime();
        return (finishNs - startNs) / 1.e6;
    }
}
