/**
 * Copyright (C) 2013, 2014 Netherlands Forensic Institute
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package nl.minvenj.nfi.lrmixstudio.model;

import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.minvenj.nfi.lrmixstudio.domain.Ratio;

/**
 * This class represents the results of a sensitivity analysis
 */
public class SensitivityAnalysisResults {

    private static final Logger LOG = LoggerFactory.getLogger(SensitivityAnalysisResults.class);
    /**
     * Contains the results of a dropout estimation for the same settings as
     * used for this sensitivity analysis.
     */
    private DropoutEstimation _dropoutEstimation;
    /**
     * The collection of ranges representing the executed analyses
     */
    private ArrayList<Range> _ranges;
    private BufferedImage _preview;
    private BufferedImage _graphImage;
    private Collection<Ratio> _ratios;
    private AtomicInteger _rangeIndex;

    public BufferedImage getPreview() {
        return _preview;
    }

    public void setPreview(BufferedImage image) {
        _preview = image;
    }

    public BufferedImage getGraphImage() {
        return _graphImage;
    }

    public void setGraphImage(BufferedImage graphImage) {
        _graphImage = graphImage;
    }

    /**
     * Represents a single point in a Range
     */
    public static class Point {

        /**
         * The X value of this point
         */
        private BigDecimal _x;
        /**
         * The Y value of this point
         */
        private BigDecimal _y;
        /**
         * The containing range
         */
        private Range _range;

        /**
         * Constructs a new Point
         *
         * @param range The range to which the point belongs
         * @param xValue The X value for the new point
         * @param yValue The Y value for the new point
         */
        public Point(Range range, BigDecimal xValue, BigDecimal yValue) {
            LOG.debug("Range {} x={}, y={}", range != null ? range.getId() : "null", xValue, yValue);
            _range = range;
            _x = xValue;
            _y = yValue;
        }

        /**
         * @return the _x value for this point
         */
        public BigDecimal getX() {
            return _x;
        }

        /**
         * @param x Sets the X value for this point
         */
        public void setX(BigDecimal x) {
            this._x = x;
        }

        /**
         * @return the _y value for this point
         */
        public BigDecimal getY() {
            return _y;
        }

        /**
         * @return the ID of the range to which this point belongs
         */
        public String getRangeId() {
            return _range == null ? "null" : _range.getId();
        }

        public int getRangeIndex() {
            return _range.getRangeIndex();
        }

        private void setRange(Range range) {
            _range = range;
        }
    }

    public static enum RangeType {
        LR, P_DEFENSE, P_PROSECUTION
    }

    /**
     * This class represents a range of points
     */
    public static class Range {

        /**
         * The full description of the data in the range
         */
        private final String _description;

        /**
         * The ID of the range. This is the name for the range used in the
         * sensitivity analysis graph. This uses a shortened version of the full
         * description to _identify ranges.
         */
        private String _id;
        /**
         * A collection of all points in this range
         */
        private ArrayList<Point> points;

        /**
         * Indicates the type of data stored in this range
         */
        private final RangeType _type;

        /**
         * Indicates the index of this range in the collection of ranges. This
         * so that we can tell JasperReports to order by this field and not try
         * to order the ranges.
         */
        private int _rangeIndex;

        public Range(int rangeIndex, RangeType type, String description, double[][] values) {
            _rangeIndex = rangeIndex;
            _type = type;
            _description = description;
            _id = description;
            points = new ArrayList();
            for (int pointIdx = 0; pointIdx < values[0].length; pointIdx++) {
                points.add(new Point(this, new BigDecimal(values[0][pointIdx]), new BigDecimal(values[1][pointIdx])));
            }
        }

        private Range(int rangeIndex, RangeType type, String rangeDescription, Collection<Point> values) {
            _rangeIndex = rangeIndex;
            _type = type;
            _description = rangeDescription;
            points = new ArrayList();
            for (Point point : values) {
                point.setRange(this);
                points.add(point);
            }
        }

        /**
         * @return the _id
         */
        public String getId() {
            return _id;
        }

        public void setId(String id) {
            _id = id;
        }

        /**
         * @return the description
         */
        public String getDescription() {
            return _description;
        }

        /**
         * @return the points
         */
        public List<Point> getPoints() {
            return Collections.unmodifiableList(points);
        }

        @Override
        public String toString() {
            return _id.isEmpty() ? _description : _id;
        }

        public RangeType getRangeType() {
            return _type;
        }

        public int getRangeIndex() {
            return _rangeIndex;
        }
    }

    /**
     * Creates a new empty SensitivityAnalysisResults object
     */
    public SensitivityAnalysisResults() {
        _ranges = new ArrayList<>();
        _rangeIndex = new AtomicInteger();
    }

    /**
     * Gets the ranges currently contained in this results object. A range is a
     * named sequence of X/Y pairs.
     *
     * @return A collection of Range classes
     */
    public Collection<Range> getRanges() {
        return Collections.unmodifiableList(_ranges);
    }

    /**
     * Sets a new Dropout Estimation result
     *
     * @param dropoutEstimation The Dropout Estimation to set
     */
    public void setDropoutEstimation(DropoutEstimation dropoutEstimation) {
        this._dropoutEstimation = dropoutEstimation;
    }

    /**
     * @return the dropout estimation, or null if this is not present
     */
    public DropoutEstimation getDropoutEstimation() {
        return _dropoutEstimation;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (_ranges.isEmpty()) {
            builder.append("Not performed.");
        } else {
            builder.append("Present.");
        }
        if (_dropoutEstimation == null) {
            builder.append(" No Dropout Estimation.");
        } else {
            builder.append(" Dropout Estimation ").append(_dropoutEstimation);
        }
        return builder.toString();
    }

    /**
     * Adds a range to the results object
     *
     * @param type the type of data stored in this range
     * @param rangeId The ID of the range to add
     * @param values An array of doubles representing the _x and _y values of
     * the points in the range
     */
    public void addRange(RangeType type, String rangeId, double[][] values) {
        _ranges.add(new Range(_rangeIndex.incrementAndGet(), type, rangeId, values));
    }

    /**
     * Adds a range to the results object
     *
     * @param type the type of data stored in this range
     * @param rangeId The ID of the range to add
     * @param values A collection of Point classes representing the _x and _y
     * values in the range
     */
    public void addRange(RangeType type, String rangeId, Collection<Point> values) {
        _ranges.add(new Range(_ranges.size(), type, rangeId, values));
    }

    /**
     * Adds a range to this results object
     *
     * @param range A Range object
     */
    public void addRange(Range range) {
        _ranges.add(range);
    }

    /**
     * Deletes the named range for the analysis results
     *
     * @param rangeId The ID of the range to delete
     */
    public void deleteRangeById(String rangeId) {
        for (Range range : _ranges) {
            if (range.getId().equalsIgnoreCase(rangeId) || range.getDescription().equalsIgnoreCase(rangeId)) {
                _ranges.remove(range);
                return;
            }
        }
    }

    /**
     * Supplies this SensitivityAnalysisResult object with the ratio for another
     * iteration of the Sensitivity Analysis.
     *
     * @param ratio The ratio to add
     */
    public void addRatio(Ratio ratio) {
        _ratios.add(ratio);
    }

    public Collection<Ratio> getRatios() {
        return _ratios;
    }
}
