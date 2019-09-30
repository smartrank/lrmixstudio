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
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.minvenj.nfi.lrmixstudio.domain.LikelihoodRatio;

/**
 * This class represents the results of a performance test as described in
 * "LRmix: statistical specifications" Version 1.0 by Hinda Haned, July 4, 2013
 */
public class NonContributorTestResults {

    private static final Logger LOG = LoggerFactory.getLogger(NonContributorTestResults.class);
    private final String _description;
    private Double _original;
    private Double _minimum;
    private Double _ninetyninePercent;
    private Double _fiftyPercent;
    private Double _onePercent;
    private Double _maximum;
    private BufferedImage _preview;
    private BufferedImage _graphImage;
    private long _iterations;
    private int _overOriginalCount;
    private int _over1Count;
    private int _percentageOverOriginalLR;
    private int _percentageOver1;

    public NonContributorTestResults(String description) {
        _description = description;
    }

    public NonContributorTestResults(String description, long iterations, LikelihoodRatio originalLR, Collection<Double> prosecutionProbabilities) {

        _iterations = iterations;
        _description = description;
        if (originalLR != null) {
            _original = Math.log10(originalLR.getOverallRatio().getRatio());
            if (prosecutionProbabilities.size() > 0) {
                Percentile percentile = new Percentile();
                double[] doubles = new double[prosecutionProbabilities.size()];
                Iterator<Double> iterator = prosecutionProbabilities.iterator();
                int idx = 0;
                _over1Count = 0;
                _overOriginalCount = 0;
                while (iterator.hasNext()) {
                    doubles[idx] = iterator.next() / originalLR.getOverallRatio().getDefenseProbability();
                    if (doubles[idx] > 1) {
                        _over1Count++;
                    }
                    if (doubles[idx] > originalLR.getOverallRatio().getRatio()) {
                        _overOriginalCount++;
                    }
                    idx++;
                }
                Arrays.sort(doubles);
                _minimum = Math.log10(doubles[0]);
                _maximum = Math.log10(doubles[doubles.length - 1]);
                percentile.setData(doubles);

                _onePercent = Math.log10(percentile.evaluate(1));
                _fiftyPercent = Math.log10(percentile.evaluate(50));
                _ninetyninePercent = Math.log10(percentile.evaluate(99));

                _percentageOver1 = (_over1Count * 100) / doubles.length;
                _percentageOverOriginalLR = (_overOriginalCount * 100) / doubles.length;
            } else {
                _onePercent = 0.0;
                _fiftyPercent = 0.0;
                _ninetyninePercent = 0.0;
                _minimum = 0.0;
                _maximum = 0.0;
            }
        }
    }

    public String getDescription() {
        return _description;
    }

    /**
     * Set the LR for the original hypotheses, i.e. before the persons of
     * interest were replaced by simulated individuals
     */
    public void setOriginalLR(final Number original) {
        this._original = Math.log10(original.doubleValue());
    }

    /**
     * @return The LR for the original hypotheses, i.e. before the persons of
     * interest were replaced by simulated individuals
     */
    public Double getOriginalLR() {
        return _original;
    }

    /**
     * @return The _minimum value encountered for LR when evaluating hypotheses
     * enriched with simulated individuals
     */
    public Double getMinimum() {
        return _minimum;
    }

    /**
     * @return The 1 Percentile value for the range of encountered values of LR
     * when evaluating hypotheses enriched with simulated individuals
     */
    public Double getOnePercent() {
        return _onePercent;
    }

    /**
     * @return The 50 Percentile value for the range of encountered values of LR
     * when evaluating hypotheses enriched with simulated individuals
     */
    public Double getFiftyPercent() {
        return _fiftyPercent;
    }

    /**
     * @return The 99 Percentile value for the range of encountered values of LR
     * when evaluating hypotheses enriched with simulated individuals
     */
    public Double getNinetyninePercent() {
        return _ninetyninePercent;
    }

    /**
     * @return The _maximum value for the range of encountered values of LR when
     * evaluating hypotheses enriched with simulated individuals
     */
    public Double getMaximum() {
        return _maximum;
    }

    /**
     * Sets the _minimum value encountered for LR when evaluating hypotheses
     * enriched with simulated individuals
     *
     * @param minimum The value to set
     */
    public void setMinimum(Number minimum) {
        this._minimum = Math.log10(minimum.doubleValue());
    }

    /**
     * Sets the 99 percentile value encountered for LR when evaluating
     * hypotheses enriched with simulated individuals
     *
     * @param ninetyninePercent The value to set
     */
    public void setNinetyninePercent(Number ninetyninePercent) {
        this._ninetyninePercent = Math.log10(ninetyninePercent.doubleValue());
    }

    /**
     * Sets the 50 percentile value encountered for LR when evaluating
     * hypotheses enriched with simulated individuals
     *
     * @param fiftyPercent the _fiftyPercent to set
     */
    public void setFiftyPercent(Number fiftyPercent) {
        this._fiftyPercent = Math.log10(fiftyPercent.doubleValue());
    }

    /**
     * Sets the 1 percentile value encountered for LR when evaluating hypotheses
     * enriched with simulated individuals
     *
     * @param onePercent the _onePercent to set
     */
    public void setOnePercent(Number onePercent) {
        this._onePercent = Math.log10(onePercent.doubleValue());
    }

    /**
     * Sets the _maximum percentile value encountered for LR when evaluating
     * hypotheses enriched with simulated individuals
     *
     * @param maximum the _maximum to set
     */
    public void setMaximum(Number maximum) {
        this._maximum = Math.log10(maximum.doubleValue());
    }

    @Override
    public String toString() {
        return "Original = " + _original + ", min = " + _minimum + ", 1% = " + _onePercent + ", 50% = " + _fiftyPercent + ", 99%  = " + _ninetyninePercent + ", max = " + _maximum;
    }

    public void setPreview(BufferedImage bufferedImage) {
        _preview = bufferedImage;
    }

    public BufferedImage getPreview() {
        return _preview;
    }

    public void setGraphImage(BufferedImage bufferedImage) {
        _graphImage = bufferedImage;
    }

    public BufferedImage getGraphImage() {
        return _graphImage;
    }

    public long getIterations() {
        return _iterations;
    }

    public int getPercentageOverOriginalLR() {
        return _percentageOverOriginalLR;
    }

    public int getPercentageOver1() {
        return _percentageOver1;
    }

    /**
     * @return the _overOriginalCount
     */
    public int getOverOriginalCount() {
        return _overOriginalCount;
    }

    /**
     * @return the _over1Count
     */
    public int getOver1Count() {
        return _over1Count;
    }
}
