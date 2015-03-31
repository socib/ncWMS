package uk.ac.rdg.resc.edal.coverage.grid.impl;

import java.util.Arrays;
import java.util.List;

import org.geotoolkit.referencing.CRS;
import org.opengis.coverage.grid.GridEnvelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.cs.RangeMeaning;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;

import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates;
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.coverage.grid.RectilinearGrid;
import uk.ac.rdg.resc.edal.coverage.grid.ReferenceableAxis;
import uk.ac.rdg.resc.edal.coverage.grid.RegularAxis;
import uk.ac.rdg.resc.edal.coverage.grid.RegularGrid;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.geometry.HorizontalPosition;
import uk.ac.rdg.resc.edal.geometry.impl.HorizontalPositionImpl;

/**
 * Bogus grid class providing a view of the smallest subgrid of a source grid 
 * that wraps a target grid (in the coordinate reference system of the target),
 * possibly sub-sampled to have the same resolution as the target.
 * 
 * The aim of this class is to provide a hack to drive to an efficient 
 * creation of PixelMaps that force the desired data loading behavior: 
 * extract the values of a layer at the vertices of the subgrid that cover 
 * the bounding box of the map. In addition, the data is sub-sampled using 
 * a constant step along each axis if the largest space between neighboring 
 * vertices is less than the space spanned by a pixel along that axis.
 * 
 * Implementing the RegularGrid interface is required to pass instances
 * of this class as the target grid in the method readXYComponents of class
 * SimpleVectorLayer. The specific methods of the interface are not supported.
 */
public class MapGridImpl extends AbstractHorizontalGrid implements RegularGrid
{
    
    final private BoundingBox mapExtent;
    final private GridEnvelope gridExtent;
    final private AbstractHorizontalGrid sourceGrid;
    final private MathTransform transform;
    final private int[] subgridOffset = {0, 0};
    final private int[] subgridStride = {0, 0};
    final private int[] subgridLength = {0, 0};
    final private double[] axisMinVal = {0, 0};
    final private double[] axisMaxVal = {0, 0};
    final private double[] axisLength = {0, 0};
    final private boolean[] axisAround = {false, false};

    /**
     * Create a view (in the CRS of the target grid) of the subgrid
     * of a source grid that covers the given target grid 
     * @param sourceGrid the source grid.
     * @param targetGrid the target grid.
     */
    public MapGridImpl(HorizontalGrid sourceGrid, RegularGrid targetGrid)
    {
        super(targetGrid.getCoordinateReferenceSystem());
        // The method transformCoordinates specified by interface HorizontalGrid
        // produces temporary objects for the bound checking in the case of
        // rectilinear grids (because the grid extent is created on the fly 
        // instead of cached). So instead we use the unchecked version 
        // transformCoordinatesNoBoundsCheck in class AbstractHorizontalGrid
        // (which is a base class of all implemented grid classes).
        this.sourceGrid = (AbstractHorizontalGrid) sourceGrid;
        final CoordinateReferenceSystem targetCRS = targetGrid.getCoordinateReferenceSystem();
        final CoordinateReferenceSystem sourceCRS = sourceGrid.getCoordinateReferenceSystem();
        final CoordinateSystem sourceCS = sourceCRS.getCoordinateSystem();
        try {
            this.transform = CRS.findMathTransform(sourceCRS, targetCRS, true);
        } catch (FactoryException e) {
            throw new RuntimeException(e);
        }
        this.axisAround[0] = sourceCS.getAxis(0).getRangeMeaning().equals(RangeMeaning.WRAPAROUND) && ! Double.isInfinite(axisLength[0]);
        this.axisAround[1] = sourceCS.getAxis(1).getRangeMeaning().equals(RangeMeaning.WRAPAROUND) && ! Double.isInfinite(axisLength[1]);
        this.axisMinVal[0] = sourceCS.getAxis(0).getMinimumValue();
        this.axisMinVal[1] = sourceCS.getAxis(1).getMinimumValue();
        this.axisMaxVal[0] = sourceCS.getAxis(0).getMaximumValue();
        this.axisMaxVal[1] = sourceCS.getAxis(1).getMaximumValue();
        this.axisLength[0] = axisMaxVal[0] - axisMinVal[0];
        this.axisLength[1] = axisMaxVal[1] - axisMinVal[1];
        final int[] minIndices = sourceGrid.getGridExtent().getLow().getCoordinateValues();
        final int[] maxIndices = sourceGrid.getGridExtent().getHigh().getCoordinateValues(); 
        final double[] minCoords = targetGrid.getExtent().getLowerCorner().getCoordinate();
        final double[] maxCoords = targetGrid.getExtent().getUpperCorner().getCoordinate();
        final double[] mres = { Math.abs(targetGrid.getAxis(0).getCoordinateSpacing()),
                                Math.abs(targetGrid.getAxis(1).getCoordinateSpacing()) };
        final int[] minBounds = {maxIndices[0] + 1, maxIndices[1] + 1};
        final int[] maxBounds = {minIndices[0] - 1, minIndices[1] - 1};
        double[] xres = {Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY};
        double[] yres = {Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY};
        if (transform.isIdentity() && sourceGrid instanceof RectilinearGrid) {
            computeSubgrid((RectilinearGrid) sourceGrid, minIndices, maxIndices,
                            axisAround, axisLength, axisMinVal, axisMaxVal,
                            minCoords, maxCoords, minBounds, maxBounds, xres, yres);
        } else {
            computeSubgrid((AbstractHorizontalGrid) sourceGrid, minIndices, maxIndices,
                            axisAround, axisLength, axisMinVal, axisMaxVal, transform, 
                            minCoords, maxCoords, minBounds, maxBounds, xres, yres);
        }
        if (minBounds[0] <= maxBounds[0] && minBounds[1] <= maxBounds[1])
        {
            subgridOffset[0] = minBounds[0];
            subgridOffset[1] = minBounds[1];
            subgridStride[0] = Math.max(1, (int) Math.min(mres[0] / xres[0], mres[1] / yres[0]));
            subgridStride[1] = Math.max(1, (int) Math.min(mres[1] / xres[1], mres[1] / yres[1]));
            subgridLength[0] = Math.max(0, (maxBounds[0] - minBounds[0] + 1) / subgridStride[0]);
            subgridLength[1] = Math.max(0, (maxBounds[1] - minBounds[1] + 1) / subgridStride[1]);
        }
        
        mapExtent = targetGrid.getExtent();
        
        gridExtent = new GridEnvelopeImpl(subgridLength[0] - 1,
                                          subgridLength[1] - 1);
    }

    /**
     * Get the minimum index of the subgrid along a dimension.
     * @param dimension the axis of interest.
     * @return the smallest index of a vertex of the subgrid along that axis.
     */
    public int getSubgridOffset(int dimension)
    {
        return subgridOffset[dimension];
    }

    /**
     * Get the index step of the subgrid along a dimension.
     * @param dimension the axis of interest.
     * @return the step between indices of a vertices of the subgrid along that axis.
     */
    public int getSubgridStride(int dimension)
    {
        return subgridStride[dimension];
    }

    /**
     * Get the index step of the subgrid along a dimension.
     * @param dimension the axis of interest.
     * @return the number of vertices of the subgrid along that axis.
     */
    public int getSubgridLength(int dimension)
    {
        return subgridLength[dimension];
    }

    @Override
    public BoundingBox getExtent()
    {
        return mapExtent;
    }

    @Override
    public GridEnvelope getGridExtent()
    {
        return gridExtent;
    }

    @Override
    public HorizontalPosition transformCoordinatesNoBoundsCheck(int i, int j)
    {
        HorizontalPosition position = sourceGrid.transformCoordinatesNoBoundsCheck(
                subgridOffset[0] + subgridStride[0] * i, 
                subgridOffset[1] + subgridStride[1] * j); 
        double[] coords = position.getCoordinate();
        // To wrap (normalize) the coordinates we could use the modulo operation
        // but we should recall that Java spec states that the sign of the result
        // is the sign of the dividend:
        // coords[0] = axisMinValues[0] + (coords[0] - axisMinValues[0]) % axisRanges[0] + (coords[0] < axisMinValues[0] ? axisRanges[0] : 0.0);
        // coords[1] = axisMinValues[1] + (coords[1] - axisMinValues[1]) % axisRanges[1] + (coords[1] < axisMinValues[1] ? axisRanges[1] : 0.0);
        if (axisAround[0] && (coords[0] < axisMinVal[0] || coords[0] > axisMaxVal[0]))
            coords[0] -= Math.floor((coords[0] - axisMinVal[0]) / axisLength[0]) * axisLength[0];
        if (axisAround[1] && (coords[1] < axisMinVal[1] || coords[1] > axisMaxVal[1]))
            coords[1] -= Math.floor((coords[1] - axisMinVal[1]) / axisMaxVal[1]) * axisLength[1];
        if (! transform.isIdentity())
            try {
                transform.transform(coords, 0, coords, 0, 1);
            } catch (TransformException e) {
                throw new RuntimeException(e);
            }
        return new HorizontalPositionImpl(coords[0], coords[1], 
                                          this.getCoordinateReferenceSystem());
    }

    @Override
    protected GridCoordinates findNearestGridPoint(double x, double y)
    {
        throw new UnsupportedOperationException("operation not allowed for map grid");
    }

    @Override
    public List<String> getAxisNames()
    {
        throw new UnsupportedOperationException("operation not allowed for map grid");
    }

    @Override
    public RegularAxis getAxis(int index)
    {
        throw new UnsupportedOperationException("operation not allowed for map grid");
    }

    @Override
    public RegularAxis getXAxis()
    {
        throw new UnsupportedOperationException("operation not allowed for map grid");
    }

    @Override
    public RegularAxis getYAxis()
    {
        throw new UnsupportedOperationException("operation not allowed for map grid");
    }

    @Override
    public GridCoordinates inverseTransformCoordinates(HorizontalPosition pos)
    {
        throw new UnsupportedOperationException("operation not allowed for map grid");
    }

    /**
     * Compute the bounds and resolution of the subgrid of a generic grid
     * covering a given bounding box.
     * 
     * Compute the bounding indices and the resolution of the subgrid
     * of the source grid that wraps the bounding box defined by given corners.
     * The coordinate reference systems of the source grid and the target 
     * bounding box might differ, and the transform from the former to the 
     * latter must be given. Also compute the largest space between neighboring
     * vertices of the subgrid along each axis of the grid and the target CRS.
     * 
     * @param sourceGrid source grid.
     * @param minIndices minimum indices along each axis of the source grid.
     * @param maxIndices maximum indices along each axis of the source grid.
     * @param axisAround whether values of each axis of the source grid's CRS wrap around.
     * @param axisLength range of values of each axis of the source CRS.
     * @param axisMinVal minimum value of each axis of the source grid's CRS.
     * @param axisMaxVal maximum value of each axis of the source grid's CRS.
     * @param transform transform from the source grid's CRS to the target CRS.
     * @param minCoords minimum values of the bounding box in the target CRS.
     * @param maxCoords maximum values of the bounding box in the target CRS.
     * @param minBounds minimum indices of the subgrid.
     * @param maxBounds maximum indices of the subgrid.
     * @param xres resolution of each axis of the subgrid along the first axis of the target CRS.
     * @param yres resolution of each axis of the subgrid along the first axis of the target CRS.
     */
    private static void computeSubgrid(AbstractHorizontalGrid sourceGrid,
                                       int[] minIndices, int[] maxIndices,
                                       boolean[] axisAround, double axisLength[],
                                       double[] axisMinVal, double axisMaxVal[],
                                       MathTransform transform,
                                       double[] minCoords, double[] maxCoords,
                                       int[] minBounds, int[] maxBounds,
                                       double[] xres, double yres[])
    {
        HorizontalPosition position;
        double[] coords = new double[2];
        double[] xprev = new double[maxIndices[1] - minIndices[1] + 2];
        double[] yprev = new double[maxIndices[1] - minIndices[1] + 2];
        double[] xincr = {Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY};
        double[] yincr = {Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY};
        Arrays.fill(xprev, Double.NaN);
        Arrays.fill(yprev, Double.NaN);
        for (int i = minIndices[0]; i <= maxIndices[0]; i++)
        {
            for (int j = minIndices[1], k = 1; j <= maxIndices[1]; j++, k++)
            {
                position = sourceGrid.transformCoordinatesNoBoundsCheck(i, j);
                coords[0] = position.getX();
                coords[1] = position.getY();
                // To wrap (normalize) the coordinates we could use the modulo operation
                // but we should recall that Java spec states that the sign of the result
                // is the sign of the dividend:
                // coords[0] = (coords[0] < axisMinVal[0] ? axisMaxVal[0] : axisMinVal[0]) + (coords[0] - axisMinVal[0]) % axisLength[0];
                // coords[1] = (coords[1] < axisMinVal[1] ? axisMaxVal[1] : axisMinVal[1]) + (coords[1] - axisMinVal[1]) % axisLength[1];
                if (axisAround[0] && (coords[0] < axisMinVal[0] || coords[0] > axisMaxVal[0]))
                    coords[0] -= Math.floor((coords[0] - axisMinVal[0]) / axisLength[0]) * axisLength[0];
                if (axisAround[1] && (coords[1] < axisMinVal[1] || coords[1] > axisMaxVal[1]))
                    coords[1] -= Math.floor((coords[1] - axisMinVal[1]) / axisLength[1]) * axisLength[1];
                if (! transform.isIdentity())
                    try {
                        transform.transform(coords, 0, coords, 0, 1);
                    } catch (TransformException e) {
                        throw new RuntimeException(e);
                    }
                if (minCoords[0] <= coords[0] && coords[0] <= maxCoords[0] &&
                    minCoords[1] <= coords[1] && coords[1] <= maxCoords[1])
                {
                    xincr[0] = Math.abs(coords[0] - xprev[k]);
                    xincr[1] = Math.abs(coords[0] - xprev[k-1]);
                    yincr[0] = Math.abs(coords[1] - yprev[k]);
                    yincr[1] = Math.abs(coords[1] - yprev[k-1]);
                    if (xincr[0] > xres[0]) xres[0] = xincr[0];
                    if (xincr[1] > xres[1]) xres[1] = xincr[1];
                    if (yincr[0] > yres[0]) yres[0] = yincr[0];
                    if (yincr[1] > yres[1]) yres[1] = yincr[1];
                    if (i < minBounds[0]) minBounds[0] = i;
                    if (i > maxBounds[0]) maxBounds[0] = i;
                    if (j < minBounds[1]) minBounds[1] = j;
                    if (j > maxBounds[1]) maxBounds[1] = j;
                }
                xprev[k] = coords[0];
                yprev[k] = coords[1];
            }
        }
        if (minBounds[0] <= maxBounds[0] && minBounds[1] <= maxBounds[1])
        {
            minBounds[0] = Math.max(minBounds[0] - 1, minIndices[0]);
            maxBounds[0] = Math.min(maxBounds[0] + 1, maxIndices[0]);
            minBounds[1] = Math.max(minBounds[1] - 1, minIndices[1]);
            maxBounds[1] = Math.min(maxBounds[1] + 1, maxIndices[1]);
        }
    }

    /**
     * Compute the bounds and resolution of the subgrid of a rectilinear grid
     * covering a given bounding box.
     * 
     * The coordingate reference system of the source grid and the bounding box
     * are assumed to be the same and the properties of the rectilinear grid
     * are used for improved performance.
     * @param sourceGrid source grid.
     * @param minIndices minimum indices along each axis of the source grid.
     * @param maxIndices maximum indices along each axis of the source grid.
     * @param axisAround whether values of each axis of the source grid's CRS wrap around.
     * @param axisLength range of values of each axis of the source CRS.
     * @param axisMinVal minimum value of each axis of the source grid's CRS.
     * @param axisMaxVal maximum value of each axis of the source grid's CRS.
     * @param minCoords minimum values of the bounding box in the target CRS.
     * @param maxCoords maximum values of the bounding box in the target CRS.
     * @param minBounds minimum indices of the subgrid.
     * @param maxBounds maximum indices of the subgrid.
     * @param xres resolution of each axis of the subgrid along the first axis of the target CRS.
     * @param yres resolution of each axis of the subgrid along the first axis of the target CRS.
     */
    private static void computeSubgrid(RectilinearGrid sourceGrid,
                                       int[] minIndices, int[] maxIndices,
                                       boolean[] axisAround, double axisLength[],
                                       double[] axisMinVal, double axisMaxVal[],
                                       double[] minCoords, double[] maxCoords,
                                       int[] minBounds, int[] maxBounds,
                                       double[] xres, double yres[])
    {
        // Handle each axis independently.
        for (int i = 0; i < 2; i++)
        {
            ReferenceableAxis axis = sourceGrid.getAxis(i);
            double ares = (i == 0) ? xres[0] : yres[1];
            double incr, curr, prev = Double.NaN;
            for (int k = minIndices[i]; k <= maxIndices[i]; k++)
            {
                curr = axis.getCoordinateValue(k);
                if (axisAround[i] && (curr < axisMinVal[i] || curr > axisMaxVal[i]))
                    curr -= Math.floor((curr - axisMinVal[i]) / axisLength[i]) * axisLength[i];
                if (minCoords[i] <= curr && curr <= maxCoords[i]) {
                    incr = Math.abs(curr - prev);
                    if (incr > ares) ares = incr;
                    if (k < minBounds[i]) minBounds[i] = k;
                    if (k > maxBounds[i]) maxBounds[i] = k;
                }
                prev = curr;
            }
            xres[i] = (i == 0) ? ares : 0;
            yres[i] = (i == 0) ? 0 : ares;
            if (minBounds[i] <= maxBounds[i])
            {
                minBounds[i] = Math.max(minBounds[i] - 1, minIndices[i]);
                maxBounds[i] = Math.min(maxBounds[i] + 1, maxIndices[i]);
            }
        }
    }

}
