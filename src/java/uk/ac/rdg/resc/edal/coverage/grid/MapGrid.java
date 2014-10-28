package uk.ac.rdg.resc.edal.coverage.grid;


/**
 * A bogus grid class type to drive to an efficient creation of PixelMaps that
 * force the desired data loading behavior.
 * 
 * This class should extend HorizontalGrid, but it extends RegularGrid
 * because the method readXYComponents of the class SimpleVectorLayer
 * wrongly requires a regular grid as target domain.
 */
public interface MapGrid extends RegularGrid
{}
