package uk.ac.rdg.resc.edal.coverage.grid.impl;

import uk.ac.rdg.resc.edal.cdm.MapGrid;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;

public class MapGridImpl extends RegularGridImpl implements MapGrid {

    public MapGridImpl(BoundingBox bbox, int width, int height)
    {
        super(bbox, width, height);
    }
}
