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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ucar.nc2.dt.GridCoordSystem;
import uk.ac.rdg.resc.edal.cdm.CurvilinearGrid.Cell;
import uk.ac.rdg.resc.edal.cdm.kdtree.KDTree;
import uk.ac.rdg.resc.edal.cdm.kdtree.Point;
import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates;
import uk.ac.rdg.resc.edal.coverage.grid.impl.GridCoordinatesImpl;
import uk.ac.rdg.resc.edal.geometry.LonLatPosition;
import uk.ac.rdg.resc.edal.geometry.impl.LonLatPositionImpl;
import uk.ac.rdg.resc.edal.util.CollectionUtils;

/**
 * A HorizontalGrid that uses an KdTree to look up the nearest neighbour of a point.
 */
final class KdTreeGrid extends AbstractCurvilinearGrid
{
    private static final Logger logger = LoggerFactory.getLogger(KdTreeGrid.class);

    /**
     * In-memory cache of LookUpTableGrid objects to save expensive re-generation of same object
     * @todo The CurvilinearGrid objects can be very big.  Really we only need to key
     * on the arrays of lon and lat: all other quantities can be calculated from
     * these.  This means that we could make other large objects available for
     * garbage collection.
     */
    private static final Map<CurvilinearGrid, KdTreeGrid> CACHE =
            CollectionUtils.newHashMap();

    private final KDTree kdTree;
    private double max_distance;
    
    // Minimisation iterations: 0 = no searching, 1=search neighbours only, >1 = minimisation
    private int max_minimisation_iterations = 1;

    /**
     * The passed-in coordSys must have 2D horizontal coordinate axes.
     */
    public static KdTreeGrid generate(GridCoordSystem coordSys)
    {
        CurvilinearGrid curvGrid = new CurvilinearGrid(coordSys);

        synchronized(CACHE)
        {
            KdTreeGrid kdTreeGrid = CACHE.get(curvGrid);
            if (kdTreeGrid == null)
            {
                logger.debug("Need to generate new kdtree");
                // Create the KdTree for this coordinate system
                long start = System.nanoTime();
                KDTree kdTree = new KDTree(curvGrid);
                kdTree.buildTree();
                long finish = System.nanoTime();
                logger.debug("Generated new kdtree in {} seconds", (finish - start) / 1.e9);
                // Create the Grid
                kdTreeGrid = new KdTreeGrid(curvGrid, kdTree);
                // Now put this in the cache
                CACHE.put(curvGrid, kdTreeGrid);
            }
            else
            {
                logger.debug("kdree found in cache");
            }
            return kdTreeGrid;
        }
    }

    public static void clearCache() {
        synchronized(CACHE) {
            CACHE.clear();
        }
    }

    /** Private constructor to prevent direct instantiation */
    private KdTreeGrid(CurvilinearGrid curvGrid, KDTree kdTree)
    {
        // All points will be returned in WGS84 lon-lat
        super(curvGrid);
        this.kdTree = kdTree;
        this.max_distance = Math.sqrt(curvGrid.getMeanCellArea());
    }

    void setQueryingParameters(double nominalMinimumResolution, double expansionFactor, double maxDistance, int minimisationIterations) {
        this.kdTree.setQueryParameters(expansionFactor, nominalMinimumResolution);
        this.max_distance = maxDistance;
        this.max_minimisation_iterations = minimisationIterations;
    }

    /**
     * @return the nearest grid point to the given lat-lon point, or null if the
     * lat-lon point is not contained within this layer's domain.
     */
    @Override
    protected GridCoordinates findNearestGridPoint(double lon, double lat)
    {
        LonLatPosition lonLatPos = new LonLatPositionImpl(lon, lat);

        // Find a set of candidate nearest-neighbours from the kd-tree
        List<Point> nns = this.kdTree.approxNearestNeighbour(lat, lon, this.max_distance);

        // Now find the real nearest neighbour
        double shortestDistanceSq = Double.MAX_VALUE;
        CurvilinearGrid.Cell closestCell = null;
        int number_points_horizontal = this.curvGrid.getNi();
        for (Point nn : nns) {
            int this_point_index = nn.getIndex();
            int i = this_point_index % number_points_horizontal;
            int j = this_point_index / number_points_horizontal;
            CurvilinearGrid.Cell cell = this.curvGrid.getCell(i, j);
            double distanceSq = cell.findDistanceSq(lonLatPos);
            if (distanceSq < shortestDistanceSq) {
                shortestDistanceSq = distanceSq;
                closestCell = cell;
            }
        }

        if (closestCell == null) return null;

        if (closestCell.contains(lonLatPos)) {
            return new GridCoordinatesImpl(closestCell.getI(), closestCell.getJ());
        }

        // We do a gradient-descent method to find the true nearest neighbour
        // We store the grid coordinates that we have already examined.
        Set<Cell> examined = new HashSet<Cell>();
        examined.add(closestCell);

        boolean found_closer_neighbour = true;
        boolean found_containing_cell = false;
        for (int i = 0; found_closer_neighbour && !found_containing_cell && i < this.max_minimisation_iterations; i++) {
            found_closer_neighbour = false;
            for (Cell neighbour : closestCell.getNeighbours()) {
                if (!examined.contains(neighbour)) {
                    double distanceSq = neighbour.findDistanceSq(lonLatPos);
                    if (distanceSq < shortestDistanceSq) {
                        closestCell = neighbour;
                        shortestDistanceSq = distanceSq;
                        found_closer_neighbour = true;
                        if (neighbour.contains(lonLatPos)) {
                            found_containing_cell = true;
                            break;
                        }
                    }
                    examined.add(neighbour);
                }
            }
        }


        return new GridCoordinatesImpl(closestCell.getI(), closestCell.getJ());

    }

}
