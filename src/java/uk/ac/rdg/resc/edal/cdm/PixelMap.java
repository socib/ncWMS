/*
 * Copyright (c) 2010 The University of Reading
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.geotoolkit.metadata.iso.extent.DefaultGeographicBoundingBox;
import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.opengis.coverage.grid.GridCoordinates;
import org.opengis.coverage.grid.GridEnvelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.rdg.resc.edal.coverage.domain.Domain;
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.coverage.grid.RectilinearGrid;
import uk.ac.rdg.resc.edal.coverage.grid.ReferenceableAxis;
import uk.ac.rdg.resc.edal.coverage.grid.RegularAxis;
import uk.ac.rdg.resc.edal.coverage.grid.RegularGrid;
import uk.ac.rdg.resc.edal.coverage.grid.impl.RegularAxisImpl;
import uk.ac.rdg.resc.edal.coverage.grid.impl.RegularGridImpl;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.geometry.HorizontalPosition;
import uk.ac.rdg.resc.edal.util.RArray;
import uk.ac.rdg.resc.edal.util.RUIntArray;
import uk.ac.rdg.resc.edal.util.RLongArray;
import uk.ac.rdg.resc.edal.util.RUByteArray;
import uk.ac.rdg.resc.edal.util.RUShortArray;
import uk.ac.rdg.resc.edal.util.Utils;
import static java.lang.Math.min;
import static java.lang.Math.max;

/**
 *<p>Maps real-world points to i and j indices of corresponding
 * points within the source data.  This is a very important class in ncWMS.  A
 * PixelMap is constructed using the following general algorithm:</p>
 *
 * <pre>
 * For each point in the given {@link PointList}:
 *    1. Find the x-y coordinates of this point in the CRS of the PointList
 *    2. Transform these x-y coordinates into latitude and longitude
 *    3. Use the given {@link HorizontalCoordSys} to transform lat-lon into the
 *       index values (i and j) of the nearest cell in the source grid
 *    4. Add the mapping (point -> i,j) to the pixel map
 * </pre>
 *
 * <p>(A more efficient algorithm is used for the special case in which both the
 * requested CRS and the CRS of the data are lat-lon.)</p>
 *
 * <p>The resulting PixelMap is then used by {@link DataReadingStrategy}s to work out what
 * data to read from the source data files.  A variety of strategies are possible
 * for reading these data points, each of which may be optimal in a certain
 * situation.</p>
 *
 * @author Jon Blower
 * @todo Perhaps we can think of a more appropriate name for this class?
 * @todo equals() and hashCode(), particularly if we're going to cache instances
 * of this class.
 * @todo It may be possible to create an alternative version of this class for
 * cases where both source and target grids are lat-lon.  In this case, the
 * pixelmap should also be a RectilinearGrid, meaning that there would be no need
 * to store mapping information in HashMaps etc.  (Profiling shows that getting
 * and putting data from/to the HashMaps is a bottleneck.)
 * @see DataReadingStrategy
 */
public final class PixelMap implements Iterable<PixelMap.PixelMapEntry>
{
    private static final Logger logger = LoggerFactory.getLogger(PixelMap.class);

    /** Stores the source grid indices */
    private final RArray sourceGridIndices;
    /** Stores the target grid indices */
    private final RArray targetGridIndices;
    
    /**
     * Maps a point in the source grid to corresponding points in the target grid.
     */
    public static interface PixelMapEntry
    {
        /** Gets the i index of this point in the source grid */
        public int getSourceGridIIndex();
        /** Gets the j index of this point in the source grid */
        public int getSourceGridJIndex();
        /** Gets the array of all target grid points that correspond with this
         * source grid point.  Each grid point is expressed as a single integer
         * {@code j * width + i}.*/
        public List<Integer> getTargetGridPoints();
    }

    private final int sourceGridISize;

    // These define the bounding box (in terms of axis indices) of the data
    // to extract from the source files
    private int minIIndex = Integer.MAX_VALUE;
    private int minJIndex = Integer.MAX_VALUE;
    private int maxIIndex = -1;
    private int maxJIndex = -1;

    /**
     * Creates a PixelMap that maps from points within the grid of source
     * data ({@code sourceGrid}) to points within the required target domain.
     */
    public PixelMap(HorizontalGrid sourceGrid, Domain<HorizontalPosition> targetDomain)
    {
        logger.debug("Creating PixelMap: Source CRS: {}, Target CRS: {}",
                sourceGrid.getCoordinateReferenceSystem().getName(),
                targetDomain.getCoordinateReferenceSystem().getName());
        logger.debug("SourceGrid class: {}, targetDomain class: {}",
                sourceGrid.getClass(), targetDomain.getClass());

        if (targetDomain.size() > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Cannot handle target domains" +
                    " greater than Integer.MAX_VALUE in size");
            // This is essentially because PixelMapEntry.getTargetGridPoints()
            // returns a List of Integers.  Also because the results of extracting
            // data are usually held in a primitive array, which can only be
            // indexed by integer values.
        }

        this.sourceGridISize = sourceGrid.getGridExtent().getSpan(0);

        // Create an estimate of a suitable chunk size.  We don't want this to
        // be too small because we would have to do many array copy operations
        // to grow resizeable arrays.  Conversely we don't want it to be too
        // large and lead to wasted space.
        int chunkSize = (int)(targetDomain.size() < 1000
            ? targetDomain.size()
            : targetDomain.size() / 10);
        
        // Choose storage for the mappings appropriate to the sizes of the domains
        long maxSourceGridIndex = sourceGrid.size() - 1;
        this.sourceGridIndices = chooseRArray(maxSourceGridIndex, chunkSize);
        logger.debug("Source grid indices (max: {}) stored in a {}",
                maxSourceGridIndex, this.sourceGridIndices.getClass());

        long maxTargetGridIndex = targetDomain.size() - 1;
        this.targetGridIndices = chooseRArray(maxTargetGridIndex, chunkSize);
        logger.debug("Target grid indices (max: {}) stored in a {}",
                maxTargetGridIndex, this.targetGridIndices.getClass());
        // This is just a double-check: shouldn't happen
        if (this.targetGridIndices instanceof RLongArray) {
            throw new IllegalStateException("Can't store target grid indices as" +
                 " longs: must be integers or smaller");
        }

        long start = System.currentTimeMillis();
        if (targetDomain instanceof MapGrid)
        {
            this.initFromMapGrid(sourceGrid, (MapGrid) targetDomain);
        }
        else if (sourceGrid instanceof RectilinearGrid && targetDomain instanceof RectilinearGrid &&
            Utils.isWgs84LonLat(sourceGrid.getCoordinateReferenceSystem()) &&
            Utils.isWgs84LonLat(targetDomain.getCoordinateReferenceSystem()))
        {
            // We can gain efficiency if the source and target grids are both
            // rectilinear lat-lon grids (i.e. they have separable latitude and
            // longitude axes).

            // TODO: could also be efficient for any matching CRS?  But how test
            // for CRS equality, when one CRS will have been created from an EPSG code
            // and the other will have been inferred from the source data file (e.g. NetCDF)
            this.initFromGrid((RectilinearGrid)sourceGrid, (RectilinearGrid)targetDomain);
        }
        else
        {
            try
            {
                this.initFromPointList(sourceGrid, targetDomain);
            }
            catch(TransformException te)
            {
                // Shouldn't happen, and there's nothing we can do about it if it
                // does (except perhaps to log the exception).
                throw new RuntimeException(te);
            }
        }

        this.sortIndices();

        logger.debug("Built pixel map in {} ms", System.currentTimeMillis() - start);
    }

    /**
     * Creates and returns a resizable array for holding values up to and including
     * maxElementValue.  For example, an unsigned short array may be used if
     * the array will only hold values up to 65535.
     */
    private static RArray chooseRArray(long maxElementValue, int chunkSize) {
        if (maxElementValue <= RUByteArray.MAX_VALUE) return new RUByteArray(chunkSize);
        if (maxElementValue <= RUShortArray.MAX_VALUE) return new RUShortArray(chunkSize);
        if (maxElementValue <= RUIntArray.MAX_VALUE) return new RUIntArray(chunkSize);
        return new RLongArray(chunkSize);
    }

    /**
     * Sorts the arrays of source and target indices so that the arrays are in
     * order of increasing source grid index, then increasing target grid index.
     * Uses an in-place quicksort algorithm adapted from
     * http://www.vogella.de/articles/JavaAlgorithmsQuicksort/article.html.
     */
    private void sortIndices()
    {
        int numElements = this.sourceGridIndices.size();
        // Nothing to do if there are only zero or one elements
        if (numElements < 2) return;
        this.quicksort(0, numElements - 1);
    }

    private void quicksort(final int low, final int high)
    {
        int i = low;
        int j = high;
        // The elements to be sorted are pairs of longs: the first is the
        // source grid index, the second is the target grid index.
        final long[] pivot = getPair(low + (high-low) / 2);

        // Divide into two lists
        while (i <= j) {
            while(comparePairs(getPair(i), pivot) < 0) {
                i++;
            }
            while(comparePairs(getPair(j), pivot) > 0) {
                j--;
            }
            if (i <= j) {
                exchange(i, j);
                i++;
                j--;
            }
        }
        // Recursion
        if (low < j)  quicksort(low, j);
        if (i < high) quicksort(i, high);
    }

    /** Gets the pair of [source, target] grid indices at the given index */
    private long[] getPair(int index) {
        return new long[] {
            sourceGridIndices.getLong(index),
            targetGridIndices.getLong(index)
        };
    }

    /**
     * Returns <0 if pair1 < pair2, 0 if pair1 == pair2, >0 otherwise.
     * Comparisons are performed first on the source grid index, then on the
     * target grid index.
     */
    private int comparePairs(long[] pair1, long[] pair2) {
        if (pair1[0] < pair2[0]) return -1;
        if (pair1[0] > pair2[0]) return 1;
        // Source grid indices must be equal, so compare target grid indices
        if (pair1[1] < pair2[1]) return -1;
        if (pair1[1] > pair2[1]) return 1;
        // Both equal
        return 0;
    }

    /**
     * Exchanges the values in the source and target grid arrays with indices
     * i1 and i2
     */
    private void exchange(int i1, int i2) {
        this.sourceGridIndices.swapElements(i1, i2);
        this.targetGridIndices.swapElements(i1, i2);
    }

    private void initFromPointList(HorizontalGrid sourceGrid, Domain<HorizontalPosition> targetDomain)
            throws TransformException
    {
        logger.debug("Using generic method based on iterating over the domain");
        int pixelIndex = 0;
        // Find the nearest grid coordinates to all the points in the domain
        for (GridCoordinates gridCoords : sourceGrid.findNearestGridPoints(targetDomain))
        {
            if (gridCoords != null)
            {
                this.put(
                    gridCoords.getCoordinateValue(0),
                    gridCoords.getCoordinateValue(1),
                    pixelIndex
                );
            }
            pixelIndex++;
        }
    }

    /**
     * Generates a PixelMap for reading data from the given source grid and
     * projecting onto the target grid.
     * @param sourceGrid The source grid in WGS84 lat-lon coordinates
     * @param targetGrid The target grid in WGS84 lat-lon coordinates
     */
    private void initFromGrid(RectilinearGrid sourceGrid, RectilinearGrid targetGrid)
    {
        logger.debug("Using optimized method for lat-lon coordinates with 1D axes");

        ReferenceableAxis sourceGridXAxis = sourceGrid.getXAxis();
        ReferenceableAxis sourceGridYAxis = sourceGrid.getYAxis();

        ReferenceableAxis targetGridXAxis = targetGrid.getXAxis();
        ReferenceableAxis targetGridYAxis = targetGrid.getYAxis();

        // Calculate the indices along the x axis
        int[] xIndices = new int[targetGridXAxis.getSize()];
        List<Double> targetGridLons = targetGridXAxis.getCoordinateValues();
        for (int i = 0; i < targetGridLons.size(); i++)
        {
            double lon = targetGridLons.get(i);
            xIndices[i] = sourceGridXAxis.getNearestCoordinateIndex(lon);
        }

        // Now cycle through the latitude values in the target grid
        int pixelIndex = 0;
        for (double lat : targetGridYAxis.getCoordinateValues())
        {
            if (lat >= -90.0 && lat <= 90.0)
            {
                int yIndex = sourceGridYAxis.getNearestCoordinateIndex(lat);
                for (int xIndex : xIndices)
                {
                    this.put(xIndex, yIndex, pixelIndex);
                    pixelIndex++;
                }
            }
            else
            {
                // We still need to increment the pixel index value
                pixelIndex += xIndices.length;
            }
        }
    }

    /**
     * Generates a PixelMap for reading data from the given source grid such 
     * that the subgrid covering the target grid is extracted.
     * @param sourceGrid The source grid
     * @param targetGrid The target grid
     */
    public void initFromMapGrid(HorizontalGrid sourceGrid, MapGrid targetGrid)
    {
        BoundingBox mapBBox = targetGrid.getExtent();
        CoordinateReferenceSystem targetCRS = mapBBox.getCoordinateReferenceSystem();
        double[] minCoordinates = mapBBox.getLowerCorner().getCoordinate();
        double[] maxCoordinates = mapBBox.getUpperCorner().getCoordinate();
        int[] minIndices = sourceGrid.getGridExtent().getLow().getCoordinateValues();
        int[] maxIndices = sourceGrid.getGridExtent().getHigh().getCoordinateValues(); 
        int[] minBounds = {maxIndices[0] + 1, maxIndices[1] + 1};
        int[] maxBounds = {minIndices[0] - 1, minIndices[1] - 1};
        for (int i = minIndices[0]; i <= maxIndices[0]; i++)
        {
            for (int j = minIndices[1]; j <= maxIndices[1]; j++)
            {
                if (i < minBounds[0] || i > maxBounds[0] ||
                    j < minBounds[1] || j > maxBounds[1])
                {
                    HorizontalPosition sourcePosition = sourceGrid.transformCoordinates(i, j);
                    HorizontalPosition targetPosition = Utils.transformPosition(sourcePosition, targetCRS);
                    double x = targetPosition.getX();
                    double y = targetPosition.getY();
                    if (minCoordinates[0] <= x && x <= maxCoordinates[0] &&
                        minCoordinates[1] <= y && y <= maxCoordinates[1])
                    {
                        minBounds[0] = min(minBounds[0], i);
                        maxBounds[0] = max(maxBounds[0], i);
                        minBounds[1] = min(minBounds[1], j);
                        maxBounds[1] = max(maxBounds[1], j);
                    }
                }
            }
        }
        minBounds[0] = max(minBounds[0] - 1, minIndices[0]);
        maxBounds[0] = min(maxBounds[0] + 1, maxIndices[0]);
        minBounds[1] = max(minBounds[1] - 1, minIndices[1]);
        maxBounds[1] = min(maxBounds[1] + 1, maxIndices[1]);
        
        for (int i = minBounds[0]; i <= maxBounds[0]; i++)
        {
            for (int j = minBounds[1]; j <= maxBounds[1]; j++)
            {
                this.sourceGridIndices.append(j*sourceGridISize + i);
                this.targetGridIndices.append(j*sourceGridISize + i);
                /*
                HorizontalPosition sourcePosition = sourceGrid.transformCoordinates(i, j);
                HorizontalPosition targetPosition = Utils.transformPosition(sourcePosition, targetCRS);
                GridCoordinates targetIndices = targetGrid.findNearestGridPoint(targetPosition);
                if (targetIndices == null)
                    this.targetGridIndices.append(-1);
                else
                    this.targetGridIndices.append(targetIndices.getCoordinateValue(1)*targetGrid.getGridExtent().getSpan(0)
                                                + targetIndices.getCoordinateValue(0) );
                */
            }
        }
        this.minIIndex = minBounds[0];
        this.maxIIndex = maxBounds[0];
        this.minJIndex = minBounds[1];
        this.maxJIndex = maxBounds[1];
    }

    /**
     * Adds a new pixel index to this map.  Does nothing if either i or j is
     * negative.
     * @param i The i index of the point in the source data
     * @param j The j index of the point in the source data
     * @param targetGridIndex The index of the corresponding point in the target domain
     */
    private void put(int i, int j, int targetGridIndex)
    {
        // If either of the indices are negative there is no data for this
        // target grid point
        if (i < 0 || j < 0) return;

        // Modify the bounding box if necessary
        if (i < this.minIIndex) this.minIIndex = i;
        if (i > this.maxIIndex) this.maxIIndex = i;
        if (j < this.minJIndex) this.minJIndex = j;
        if (j > this.maxJIndex) this.maxJIndex = j;

        // Calculate a single integer representing this grid point in the source grid
        // TODO: watch out for overflows (would only happen with a very large grid!)
        long sourceGridIndex = (long)j * this.sourceGridISize + i;

        // Add to the arrays holding the mapping
        this.sourceGridIndices.append(sourceGridIndex);
        this.targetGridIndices.append(targetGridIndex);
    }

    /**
     * Returns true if this PixelMap does not contain any data: this will happen
     * if there is no intersection between the requested data and the data on disk.
     * @return true if this PixelMap does not contain any data: this will happen
     * if there is no intersection between the requested data and the data on disk
     */
    public boolean isEmpty()
    {
        return this.sourceGridIndices.size() == 0;
    }

    /**
     * Gets the minimum i index in the whole pixel map
     * @return the minimum i index in the whole pixel map
     */
    public int getMinIIndex()
    {
        return minIIndex;
    }

    /**
     * Gets the minimum j index in the whole pixel map
     * @return the minimum j index in the whole pixel map
     */
    public int getMinJIndex()
    {
        return minJIndex;
    }

    /**
     * Gets the maximum i index in the whole pixel map
     * @return the maximum i index in the whole pixel map
     */
    public int getMaxIIndex()
    {
        return maxIIndex;
    }

    /**
     * Gets the maximum j index in the whole pixel map
     * @return the maximum j index in the whole pixel map
     */
    public int getMaxJIndex()
    {
        return maxJIndex;
    }

    /**
     * <p>Gets the number of unique i-j pairs in this pixel map. When combined
     * with the size of the resulting image we can quantify the under- or
     * oversampling.  This is the number of data points that will be extracted
     * by the {@link DataReadingStrategy#PIXEL_BY_PIXEL PIXEL_BY_PIXEL} data
     * reading strategy.</p>
     * <p>This implementation counts the number of unique pairs by cycling through
     * the {@link #iterator()} and so is not a cheap operation.  Use sparingly,
     * e.g. for debugging.</p>
     * @return the number of unique i-j pairs in this pixel map.
     */
    public int getNumUniqueIJPairs()
    {
        int count = 0;
        for (PixelMapEntry pme : this) count++;
        return count;
    }

    /**
     * Gets the size of the i-j bounding box that encompasses all data.  This is
     * the number of data points that will be extracted using the
     * {@link DataReadingStrategy#BOUNDING_BOX BOUNDING_BOX} data reading strategy.
     * @return the size of the i-j bounding box that encompasses all data.
     */
    public long getBoundingBoxSize()
    {
        return (long)(this.maxIIndex - this.minIIndex + 1) *
                     (this.maxJIndex - this.minJIndex + 1);
    }

    /**
     * Returns an unmodifiable iterator over all the {@link PixelMapEntry}s in this PixelMap.
     */
    @Override
    public Iterator<PixelMapEntry> iterator()
    {
        return new Iterator<PixelMapEntry>()
        {
            /** Index in the array of entries */
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < sourceGridIndices.size();
            }

            @Override
            public PixelMapEntry next() {
                final long entrySourceIndex = sourceGridIndices.getLong(index);
                //
                final List<Integer> entryTargetIndices = new ArrayList<Integer>();
                entryTargetIndices.add(targetGridIndices.getInt(index));
                index++;

                // Now find all the other entries that use the same source grid
                // index
                boolean done = false;
                while (!done && this.hasNext()) {
                    long newSourceIndex = sourceGridIndices.getLong(index);
                    if (newSourceIndex == entrySourceIndex) {
                        entryTargetIndices.add(targetGridIndices.getInt(index));
                        index++;
                    } else {
                        done = true;
                    }
                }

                return new PixelMapEntry() {

                    @Override
                    public int getSourceGridIIndex() {
                        return (int)(entrySourceIndex % sourceGridISize);
                    }

                    @Override
                    public int getSourceGridJIndex() {
                        return (int)(entrySourceIndex / sourceGridISize);
                    }

                    @Override
                    public List<Integer> getTargetGridPoints() {
                        return entryTargetIndices;
                    }

                };
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

        };
    }

    public static void main(String[] args) throws Exception
    {
        RegularAxis lonAxis = new RegularAxisImpl("lon", 64.01358, 0.045, 347400, true);
        RegularAxis latAxis = new RegularAxisImpl("lat", 80.12541, -0.045, 35640, false);
        RegularGrid sourceDomain = new RegularGridImpl(lonAxis, latAxis, DefaultGeographicCRS.WGS84);
        RegularGrid targetDomain = new RegularGridImpl(DefaultGeographicBoundingBox.WORLD, 10, 10);

        Runtime rt = Runtime.getRuntime();

        long startMemUsed = memUsed(rt);
        long start = System.nanoTime();
        PixelMap pixelMap = new PixelMap(sourceDomain, targetDomain);
        long finish = System.nanoTime();
        long memUsed = memUsed(rt) - startMemUsed;

        System.out.println("Built PixelMap in " + ((finish - start) / 1.e6) + " ms");
        //System.out.println("Number of entries " + pixelMap.numEntries + " (" + pixelMap.pixelMapEntries.length + ")");
        //System.out.println("Num unique pairs = " + pixelMap.getNumUniqueIJPairs());
        //System.out.println("Total insert time " + (pixelMap.insertTime / 1.e6));
        //System.out.println("Stuff shifted " + pixelMap.stuffShifted);
        System.out.println("mem used " + memUsed);
        // With compression:    222 ms, 370k   840x400
        // Without compression: 166ms, 2.7M
        // With compression:    222 ms, 370k   512x512
        // Without compression: 140ms, 2.1M
    }

    private static long memUsed(Runtime rt)
    {
        System.gc();
        return rt.totalMemory() - rt.freeMemory();
    }

}
