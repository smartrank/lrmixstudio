/*
 * Copyright (c) 2014, Netherlands Forensic Institute
 * All rights reserved.
 */
package nl.minvenj.nfi.lrmixstudio.model.splitdrop.threadpool.genotype;

import nl.minvenj.nfi.lrmixstudio.domain.Locus;

/**
 * An interface for classes that calculate genotype probabilities.
 *
 * @author dejong
 */
public interface GenotypeProbabilityCalculator {

    /**
     * Calculates the genotype probability for the supplied locus.
     *
     * @param alleleCounts An array of integers containing the allele counts of
     * all alleles up to the specified locus
     * @param locus The locus for which the genotype probability is to be
     * calculated
     * @return a double containing the probability of observing the supplied
     * genotype
     */
    public double calculate(int[] alleleCounts, Locus locus);
}
