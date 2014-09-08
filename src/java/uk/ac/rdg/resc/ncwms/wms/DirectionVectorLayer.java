/*
 * Copyright (c) 2014 SOCIB
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

package uk.ac.rdg.resc.ncwms.wms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.opengis.metadata.extent.GeographicBoundingBox;

import uk.ac.rdg.resc.edal.coverage.domain.Domain;
import uk.ac.rdg.resc.edal.coverage.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.geometry.HorizontalPosition;
import uk.ac.rdg.resc.edal.util.Range;
import uk.ac.rdg.resc.ncwms.exceptions.InvalidDimensionValueException;
import uk.ac.rdg.resc.ncwms.graphics.ColorPalette;

/**
 * Implementation of a {@link VectorLayer} that wraps a Layer object containing
 * a direction measurement. The properties are derived directly from the 
 * underlying layer
 *
 * @author Joan Pau Beltran
 */
public class DirectionVectorLayer extends SimpleVectorLayer {

    public DirectionVectorLayer(String id, ScalarLayer directionLayer, boolean trueNorth, boolean fromDir) {
        super(id, 
              new DirectionLayerXComponent(directionLayer, trueNorth, fromDir, Math.PI / 180.0),
              new DirectionLayerYComponent(directionLayer, trueNorth, fromDir, Math.PI / 180.0),
              true);
    }
    
    static private class WrappedLayer implements ScalarLayer {
        protected ScalarLayer wrappedLayer;
        protected WrappedLayer(ScalarLayer layer) {
            this.wrappedLayer = layer;
        }
        @Override
        public String getId() {
            return this.wrappedLayer.getId();
        }
        @Override
        public String getLayerAbstract() {
            return this.wrappedLayer.getLayerAbstract();
        }
        @Override
        public Dataset getDataset() {
            return this.wrappedLayer.getDataset();
        }
        @Override
        public String getUnits() {
            return this.wrappedLayer.getUnits();
        }
        @Override
        public boolean isQueryable() {
            return this.wrappedLayer.isQueryable();
        }
        @Override
        public boolean isIntervalTime() { 
            return this.wrappedLayer.isIntervalTime(); 
        }
        @Override
        public GeographicBoundingBox getGeographicBoundingBox() {
            return this.wrappedLayer.getGeographicBoundingBox();
        }
        @Override
        public HorizontalGrid getHorizontalGrid() {
            return this.wrappedLayer.getHorizontalGrid();
        }
        @Override
        public Chronology getChronology() {
            return this.wrappedLayer.getChronology();
        }
        @Override
        public List<DateTime> getTimeValues() {
            return this.wrappedLayer.getTimeValues();
        }
        @Override
        public DateTime getCurrentTimeValue() {
            return this.wrappedLayer.getCurrentTimeValue();
        }
        @Override
        public DateTime getDefaultTimeValue() {
            return this.wrappedLayer.getDefaultTimeValue();
        }
        @Override
        public List<Double> getElevationValues() {
            return this.wrappedLayer.getElevationValues();
        }
        @Override
        public double getDefaultElevationValue() {
            return this.wrappedLayer.getDefaultElevationValue();
        }
        @Override
        public String getElevationUnits() {
            return this.wrappedLayer.getElevationUnits();
        }
        @Override
        public boolean isElevationPositive() {
            return this.wrappedLayer.isElevationPositive();
        }
        @Override
        public boolean isElevationPressure() {
            return this.wrappedLayer.isElevationPressure();
        }
        @Override
        public ColorPalette getDefaultColorPalette() {
            return this.wrappedLayer.getDefaultColorPalette();
        }
        @Override
        public boolean isLogScaling() {
            return this.wrappedLayer.isLogScaling();
        }
        @Override
        public int getDefaultNumColorBands() {
            return this.wrappedLayer.getDefaultNumColorBands();
        }
        @Override
        public Range<Float> getApproxValueRange() {
            return this.wrappedLayer.getApproxValueRange();
        }
        @Override
        public List<Float> readHorizontalPoints(DateTime time,
                double elevation, Domain<HorizontalPosition> points)
                throws InvalidDimensionValueException, IOException {
            return this.wrappedLayer.readHorizontalPoints(time, elevation, points);
        }
        @Override
        public Float readSinglePoint(DateTime time, double elevation,
                HorizontalPosition xy) throws InvalidDimensionValueException,
                IOException {
            return this.wrappedLayer.readSinglePoint(time, elevation, xy);
        }
        @Override
        public List<Float> readTimeseries(List<DateTime> times,
                double elevation, HorizontalPosition xy)
                throws InvalidDimensionValueException, IOException {
            return this.wrappedLayer.readTimeseries(times, elevation, xy);
        }
        @Override
        public List<List<Float>> readVerticalSection(DateTime time,
                List<Double> elevations, Domain<HorizontalPosition> points)
                throws InvalidDimensionValueException, IOException {
            return this.wrappedLayer.readVerticalSection(time, elevations, points);
        }
        @Override
        public String getName() {
            return this.wrappedLayer.getName();
        }
        @Override
        public String getTitle() {
            return this.wrappedLayer.getTitle();
        }
    }

    static private class DirectionLayerXComponent extends WrappedLayer {
        double factor;
        double offset;
        public DirectionLayerXComponent(ScalarLayer directionLayer,
                                        boolean trueNorth, boolean fromDir,
                                        double factor) {
            super(directionLayer);
            this.factor = factor * (trueNorth ? -1.0 : 1.0);
            this.offset = ((trueNorth ? 0.5 : 0.0) + (fromDir ? 1.0 : 0.0)) * Math.PI;
        }
        @Override
        public List<Float> readHorizontalPoints(DateTime time,
                double elevation, Domain<HorizontalPosition> points)
                throws InvalidDimensionValueException, IOException {
            ArrayList<Float> values = new ArrayList<Float>(super.readHorizontalPoints(time, elevation, points));
            for (ListIterator<Float> i = values.listIterator(); i.hasNext(); ) {
                Float d = i.next();
                if (d != null) {
                    Float v = new Float(Math.cos(d * this.factor + this.offset));
                    i.set(v);
                }
            }
            return values;
        }
        @Override
        public Float readSinglePoint(DateTime time, double elevation,
                HorizontalPosition xy) throws InvalidDimensionValueException,
                IOException {
            Float v = null;
            Float d = super.readSinglePoint(time, elevation, xy);
            if (d != null) {
                v = new Float(Math.cos(d * this.factor + this.offset));
            }
            return v;
        }
        @Override
        public List<Float> readTimeseries(List<DateTime> times,
                double elevation, HorizontalPosition xy)
                throws InvalidDimensionValueException, IOException {
            // TODO Auto-generated method stub
            ArrayList<Float> values = new ArrayList<Float>(super.readTimeseries(times, elevation, xy));
            for (ListIterator<Float> i = values.listIterator(); i.hasNext(); ) {
                Float d = i.next();
                if (d != null) {
                    Float v = new Float(Math.cos(d * this.factor + this.offset));
                    i.set(v);
                }
            }
            return values;
        }
        @Override
        public List<List<Float>> readVerticalSection(DateTime time,
                List<Double> elevations, Domain<HorizontalPosition> points)
                throws InvalidDimensionValueException, IOException {
            ArrayList<List<Float>> levels = new ArrayList<List<Float>>(super.readVerticalSection(time, elevations, points));
            for (ListIterator<List<Float>> j = levels.listIterator(); j.hasNext(); ) {
                List<Float> level = j.next();
                if (level != null) {
                    ArrayList<Float> values = new ArrayList<Float>(level);
                    for (ListIterator<Float> i = values.listIterator(); i.hasNext(); ) {
                        Float d = i.next();
                        if (d != null) {
                            Float v = new Float(Math.cos(d * this.factor + this.offset));
                            i.set(v);
                        }
                    }
                }
            }
            return levels;
        }
        @Override
        public String getName() {
            return super.getName() + "_x-component";
        }
        @Override
        public String getTitle() {
            return "X component of direction vector of " + super.getTitle();
        }
    }

    static private class DirectionLayerYComponent extends WrappedLayer {
        double factor;
        double offset;
        public DirectionLayerYComponent(ScalarLayer directionLayer, boolean trueNorth, boolean fromDir, double factor) {
            super(directionLayer);
            this.factor = factor * (trueNorth ? -1.0 : 1.0);
            this.offset = ((trueNorth ? 0.5 : 0.0) + (fromDir ? 1.0 : 0.0)) * Math.PI;
        }
        @Override
        public List<Float> readHorizontalPoints(DateTime time,
                double elevation, Domain<HorizontalPosition> points)
                throws InvalidDimensionValueException, IOException {
            ArrayList<Float> values = new ArrayList<Float>(super.readHorizontalPoints(time, elevation, points));
            for (ListIterator<Float> i = values.listIterator(); i.hasNext(); ) {
                Float d = i.next();
                if (d != null) {
                    Float v = new Float(Math.sin(d * this.factor + this.offset));
                    i.set(v);
                }
            }
            return values;
        }
        @Override
        public Float readSinglePoint(DateTime time, double elevation,
                HorizontalPosition xy) throws InvalidDimensionValueException,
                IOException {
            Float v = null;
            Float d = super.readSinglePoint(time, elevation, xy);
            if (d != null) {
                v = new Float(Math.sin(d * factor));
            }
            return v;
        }
        @Override
        public List<Float> readTimeseries(List<DateTime> times,
                double elevation, HorizontalPosition xy)
                throws InvalidDimensionValueException, IOException {
            // TODO Auto-generated method stub
            ArrayList<Float> values = new ArrayList<Float>(super.readTimeseries(times, elevation, xy));
            for (ListIterator<Float> i = values.listIterator(); i.hasNext(); ) {
                Float d = i.next();
                if (d != null) {
                    Float v = new Float(Math.sin(d * this.factor + this.offset));
                    i.set(v);
                }
            }
            return values;
        }
        @Override
        public List<List<Float>> readVerticalSection(DateTime time,
                List<Double> elevations, Domain<HorizontalPosition> points)
                throws InvalidDimensionValueException, IOException {
            ArrayList<List<Float>> levels = new ArrayList<List<Float>>(super.readVerticalSection(time, elevations, points));
            for (ListIterator<List<Float>> j = levels.listIterator(); j.hasNext(); ) {
                List<Float> level = j.next();
                if (level != null) {
                    ArrayList<Float> values = new ArrayList<Float>(level);
                    for (ListIterator<Float> i = values.listIterator(); i.hasNext(); ) {
                        Float d = i.next();
                        if (d != null) {
                            Float v = new Float(Math.sin(d * this.factor + this.offset));
                            i.set(v);
                        }
                    }
                }
            }
            return levels;
        }
        @Override
        public String getName() {
            return super.getName() + "_y-component";
        }
        @Override
        public String getTitle() {
            return "Y component of direction vector of " + super.getTitle();
        }
    }
}
