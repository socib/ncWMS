package uk.ac.rdg.resc.edal.coverage.grid.impl;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.ArrayList;
import java.util.List;

import org.geotoolkit.referencing.CRS;
import org.opengis.coverage.grid.GridEnvelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.cs.RangeMeaning;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;

import uk.ac.rdg.resc.edal.coverage.domain.Domain;
import uk.ac.rdg.resc.edal.coverage.grid.GridCoordinates;
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.coverage.grid.MapGrid;
import uk.ac.rdg.resc.edal.coverage.grid.RegularAxis;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.geometry.HorizontalPosition;
import uk.ac.rdg.resc.edal.geometry.impl.HorizontalPositionImpl;

/**
 * An implementation of the bogus grid class type to drive to an efficient 
 * creation of PixelMaps that force the desired data loading behavior.
 * 
 * The methods of the RegularGrid interface are not supported.
 */
public class MapGridImpl implements MapGrid
{
    private BoundingBox mapExtent;
    private GridEnvelope gridExtent;
    private List<HorizontalPosition> mapPoints;
    
    public MapGridImpl(HorizontalGrid sourceGrid, HorizontalGrid targetGrid)
    {
        mapExtent = targetGrid.getExtent();
        gridExtent = computeGridExtent(sourceGrid, mapExtent);
        mapPoints = extractMapPoints(sourceGrid, gridExtent, mapExtent.getCoordinateReferenceSystem());
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
    public int getDimension()
    {
        return 2;
    }

    @Override
    public long size()
    {
        if (gridExtent == null)
            return 0;
        return  gridExtent.getSpan(0) * gridExtent.getSpan(1);
    }

    @Override
    public CoordinateReferenceSystem getCoordinateReferenceSystem()
    {
        return mapExtent.getCoordinateReferenceSystem();
    }

    @Override
    public List<HorizontalPosition> getDomainObjects()
    {
        return mapPoints;
    }

    @Override
    public HorizontalPosition transformCoordinates(int i, int j)
    {
        if (gridExtent == null)
            return null;
        final int[] minBounds = gridExtent.getLow().getCoordinateValues();
        final int[] maxBounds = gridExtent.getHigh().getCoordinateValues();
        if (i < minBounds[0] || maxBounds[0] < i ||
            j < minBounds[1] || maxBounds[1] < j )
            return null;
        int step = maxBounds[1] - minBounds[1] + 1;
        int index = step * (i - minBounds[0]) + (j - minBounds[1]);
        return this.mapPoints.get(index);
    }

    @Override
    public HorizontalPosition transformCoordinates(GridCoordinates coords)
    {
        if (coords.getDimension() != 2)
            throw new IllegalArgumentException("GridCoordinates must be 2D");
        return transformCoordinates(coords.getCoordinateValue(0),
                                    coords.getCoordinateValue(1));
    }

    @Override
    public GridCoordinates inverseTransformCoordinates(HorizontalPosition pos)
    {
        int index = mapPoints.indexOf(pos);
        if (index < 0)
            return null;
        int[] minBounds = gridExtent.getLow().getCoordinateValues();
        int[] maxBounds = gridExtent.getHigh().getCoordinateValues();
        int step = maxBounds[1] - minBounds[1] + 1;
        int i = minBounds[0] + index / step;
        int j = minBounds[1] + index % step;
        return new GridCoordinatesImpl(i, j);
    }

    @Override
    public List<String> getAxisNames()
    {
        throw new UnsupportedOperationException("operation not allowed for map grid");
    }
    
    @Override
    public GridCoordinates findNearestGridPoint(HorizontalPosition pos)
    {
        throw new UnsupportedOperationException("operation not allowed for map grid");
    }

    @Override
    public List<GridCoordinates> findNearestGridPoints(Domain<HorizontalPosition> domain)
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

    /**
     * Compute the extent of the subgrid of a grid that wraps a bounding box.
     * @param grid the source grid.
     * @param bbox
     * @return the extent of the wrapping subgrid.
     */
    private static GridEnvelope computeGridExtent(HorizontalGrid grid,
                                                  BoundingBox bbox)
    {
        final CoordinateReferenceSystem sourceCRS = grid.getCoordinateReferenceSystem();
        final CoordinateReferenceSystem targetCRS = bbox.getCoordinateReferenceSystem();
        final CoordinateSystem sourceCS = sourceCRS.getCoordinateSystem();
        final double[] axisMinValues = {sourceCS.getAxis(0).getMinimumValue(),
                                        sourceCS.getAxis(1).getMinimumValue()};
        final double[] axisMaxValues = {sourceCS.getAxis(0).getMaximumValue(),
                                        sourceCS.getAxis(1).getMaximumValue()};
        final double[] axisRanges = {axisMaxValues[0] - axisMinValues[0],
                                     axisMaxValues[1] - axisMinValues[1]};
        final boolean[] axisWrap = {
                sourceCS.getAxis(0).getRangeMeaning().equals(RangeMeaning.WRAPAROUND) && ! Double.isInfinite(axisRanges[0]),
                sourceCS.getAxis(1).getRangeMeaning().equals(RangeMeaning.WRAPAROUND) && ! Double.isInfinite(axisRanges[1])
        };
        final MathTransform transform;
        try {
            transform = CRS.findMathTransform(sourceCRS, targetCRS, true);
        } catch (FactoryException e) {
            throw new RuntimeException(e);
        }
        final int[] minIndices = grid.getGridExtent().getLow().getCoordinateValues();
        final int[] maxIndices = grid.getGridExtent().getHigh().getCoordinateValues(); 
        final double[] minCoords = bbox.getLowerCorner().getCoordinate();
        final double[] maxCoords = bbox.getUpperCorner().getCoordinate();
        int[] minBounds = {maxIndices[0] + 1, maxIndices[1] + 1};
        int[] maxBounds = {minIndices[0] - 1, minIndices[1] - 1};
        HorizontalPosition position;
        double[] coords = {0.0, 0.0};
        for (int i = minIndices[0]; i <= maxIndices[0]; i++)
        {
            for (int j = minIndices[1]; j <= maxIndices[1]; j++)
            {
                if (i < minBounds[0] || i > maxBounds[0] ||
                    j < minBounds[1] || j > maxBounds[1])
                {
                    position = grid.transformCoordinates(i, j);
                    coords[0] = position.getX();
                    coords[1] = position.getY();
                    // To wrap (normalize) the coordinates we could use the modulo operation
                    // but we should recall that Java spec states that the sign of the result
                    // is the sign of the dividend:
                    // coords[0] = axisMinValues[0] + (coords[0] - axisMinValues[0]) % axisRanges[0] + (coords[0] < axisMinValues[0] ? axisRanges[0] : 0.0);
                    // coords[1] = axisMinValues[1] + (coords[1] - axisMinValues[1]) % axisRanges[1] + (coords[0] < axisMinValues[1] ? axisRanges[1] : 0.0);
                    if (axisWrap[0] && (coords[0] < axisMinValues[0] || coords[0] > axisMaxValues[0]))
                        coords[0] -= Math.floor((coords[0] - axisMinValues[0]) / axisRanges[0]) * axisRanges[0];
                    if (axisWrap[1] && (coords[1] < axisMinValues[1] || coords[1] > axisMaxValues[1]))
                        coords[1] -= Math.floor((coords[1] - axisMinValues[1]) / axisRanges[1]) * axisRanges[1];
                    if (! transform.isIdentity())
                        try {
                            transform.transform(coords, 0, coords, 0, 1);
                        } catch (TransformException e) {
                            throw new RuntimeException(e);
                        }
                    if (minCoords[0] <= coords[0] && coords[0] <= maxCoords[0] &&
                        minCoords[1] <= coords[1] && coords[1] <= maxCoords[1])
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
        if (minBounds[0] <= maxBounds[0] && minBounds[1] <= maxBounds[1])
            return new GridEnvelopeImpl(new GridCoordinatesImpl(minBounds),
                                        new GridCoordinatesImpl(maxBounds));
        else
            return null;
    }

    /**
     * Extract the coordinates of the points of a subgrid in another reference system.
     * @param sourceGrid the source grid.
     * @param gridExtent the grid extent of the subgrid.
     * @param targetCRS the target reference system.
     * @return the list of points of the subgrid in the target reference system.
     */
    private static List<HorizontalPosition> extractMapPoints(HorizontalGrid sourceGrid,
                                                             GridEnvelope gridExtent,
                                                             CoordinateReferenceSystem targetCRS)
    {
        if (gridExtent == null)
            return new ArrayList<HorizontalPosition>();
        final CoordinateReferenceSystem sourceCRS = sourceGrid.getCoordinateReferenceSystem();
        final CoordinateSystem sourceCS = sourceCRS.getCoordinateSystem();
        final double[] axisMinValues = {sourceCS.getAxis(0).getMinimumValue(),
                                        sourceCS.getAxis(1).getMinimumValue()};
        final double[] axisMaxValues = {sourceCS.getAxis(0).getMaximumValue(),
                                        sourceCS.getAxis(1).getMaximumValue()};
        final double[] axisRanges = {axisMaxValues[0] - axisMinValues[0],
                                     axisMaxValues[1] - axisMinValues[1]};
        final boolean[] axisWrap = {
                sourceCS.getAxis(0).getRangeMeaning().equals(RangeMeaning.WRAPAROUND) && ! Double.isInfinite(axisRanges[0]),
                sourceCS.getAxis(1).getRangeMeaning().equals(RangeMeaning.WRAPAROUND) && ! Double.isInfinite(axisRanges[1])
        };
        final MathTransform transform;
        try {
            transform = CRS.findMathTransform(sourceCRS, targetCRS, true);
        } catch (FactoryException e) {
            throw new RuntimeException(e);
        }
        final int[] minIndices = gridExtent.getLow().getCoordinateValues();
        final int[] maxIndices = gridExtent.getHigh().getCoordinateValues(); 
        final int size = gridExtent.getSpan(0) * gridExtent.getSpan(1);
        HorizontalPosition position;
        double[] coords = {0.0, 0.0};
        List<HorizontalPosition> mapPoints = new ArrayList<HorizontalPosition>(size);
        for (int i = minIndices[0]; i <= maxIndices[0]; i++)
        {
            for (int j = minIndices[1]; j <= maxIndices[1]; j++)
            {
                position = sourceGrid.transformCoordinates(i, j);
                coords[0] = position.getX();
                coords[1] = position.getY();
                if (axisWrap[0] && (coords[0] < axisMinValues[0] || coords[0] > axisMaxValues[0]))
                    coords[0] -= Math.floor((coords[0] - axisMinValues[0]) / axisRanges[0]) * axisRanges[0];
                if (axisWrap[1] && (coords[1] < axisMinValues[1] || coords[1] > axisMaxValues[1]))
                    coords[1] -= Math.floor((coords[1] - axisMinValues[1]) / axisRanges[1]) * axisRanges[1];
                if (! transform.isIdentity())
                    try {
                         transform.transform(coords, 0, coords, 0, 1);
                    } catch (TransformException e) {
                        throw new RuntimeException(e);
                    }
                mapPoints.add(new HorizontalPositionImpl(coords[0], coords[1], targetCRS));
            }
        }
        return mapPoints;
    }

}
