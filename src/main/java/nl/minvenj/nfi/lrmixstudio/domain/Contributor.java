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
package nl.minvenj.nfi.lrmixstudio.domain;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * This class represents a DNA sample when evaluated as a (non-)contributor in
 * the context of a hypothesis.
 */
public class Contributor implements Cloneable {

    private Sample sample;
    private double dropOutProbability = 0;
    private double dropOutProbabilityHomozygote = 0;
    private double presentOnceProbability = 0;
    private double presentOnceProbabilityHomozygote = 0;
    private double presentMultipleProbability = 0;
    private double presentMultipleProbabilityHomozygote = 0;

    /**
     * Constructs a new Contributor class based on a {@link Sample} and dropout
     * probability
     *
     * @param sample The sample to be evaluated
     * @param dropOutProbability The dropout probability for this sample
     */
    public Contributor(Sample sample, double dropOutProbability) {
        setProbabilities(dropOutProbability);
        this.sample = sample;
    }

    /**
     * Constructs a new Contributor class by copying an existing Contributor
     * instance. The new Contributor will contain a reference to the sample of
     * the original but have an independent dropout value.
     *
     * @param original The original contributor to copy
     */
    public Contributor(Contributor original) {
        setProbabilities(original.dropOutProbability);
        this.sample = original.sample;
    }

    /**
     * Gets the dropout probability for this contributor
     *
     * @param homozygote true if the returned value is to be used for a
     * homozygote locus
     * @return the dropout probability for this contributor
     */
    public double getDropOutProbability(boolean homozygote) {
        return homozygote ? dropOutProbabilityHomozygote : dropOutProbability;
    }

    /**
     * @return the contributor's sample
     */
    public Sample getSample() {
        return sample;
    }

    /**
     * Gets the dropout probability for alleles that are present once in the
     * combined DNA of the hypothesized contributors
     *
     * @param homozygote true if the returned value is used for a homozygote
     * locus
     * @return the dropout probability for alleles that are present once in the
     * hypothesized contributors profiles.
     */
    public double getPresentOnceProbability(boolean homozygote) {
        return homozygote ? presentOnceProbabilityHomozygote : presentOnceProbability;
    }

    /**
     * Gets the dropout probability for alleles that are present multiple times
     * in the combined DNA of the hypothesized contributors
     *
     * @param homozygote true if the returned value is used for a homozygote
     * locus
     * @return the dropout probability for alleles that are present multiple
     * times in the hypothesized contributors profiles.
     */
    public double getPresentMultipleProbability(boolean homozygote) {
        return homozygote ? presentMultipleProbabilityHomozygote : presentMultipleProbability;
    }

    /**
     * Sets the dropout probability for this contributor
     *
     * @param dropout The new value for the dropout probability
     */
    public void setDropoutProbability(double dropout) {
        setProbabilities(dropout);
    }

    /**
     * Gets the dropout probability for this contributor
     */
    public double getDropoutProbability() {
        return dropOutProbability;
    }

    /**
     * sets the dropout probabilities for the various scenarios (present once,
     * present multiple times, dropped out)
     *
     * @param dropOutProbability The global dropout probability for this
     * contributor
     */
    private void setProbabilities(double dropOutProbability) {
        this.dropOutProbability = dropOutProbability;
        this.dropOutProbabilityHomozygote = dropOutProbability * dropOutProbability;
        this.presentOnceProbability = 1 - dropOutProbability;
        this.presentOnceProbabilityHomozygote = 1 - dropOutProbabilityHomozygote;
        this.presentMultipleProbability = dropOutProbability;
        this.presentMultipleProbabilityHomozygote = dropOutProbabilityHomozygote;
    }

    @Override
    public String toString() {
        return sample.getId() + "(" + new BigDecimal(dropOutProbability, MathContext.UNLIMITED).setScale(2, RoundingMode.HALF_UP) + ")";
    }

    public void setSample(Sample newSample) {
        sample = newSample;
    }
}
