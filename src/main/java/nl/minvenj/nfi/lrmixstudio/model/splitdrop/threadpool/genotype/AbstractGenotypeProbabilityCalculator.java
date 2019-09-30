/*
 * Copyright (c) 2014, Netherlands Forensic Institute
 * All rights reserved.
 */
package nl.minvenj.nfi.lrmixstudio.model.splitdrop.threadpool.genotype;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.minvenj.nfi.lrmixstudio.domain.Allele;
import nl.minvenj.nfi.lrmixstudio.domain.Locus;
import nl.minvenj.nfi.lrmixstudio.domain.Sample;
import nl.minvenj.nfi.lrmixstudio.gui.ApplicationSettings;
import nl.minvenj.nfi.lrmixstudio.model.splitdrop.threadpool.ValidationLogger;

/**
 * Abstract base class for the Genotype Probability calculator classes.
 *
 * @author dejong
 */
public abstract class AbstractGenotypeProbabilityCalculator implements GenotypeProbabilityCalculator {

    private static final Logger LOG = new ValidationLogger(LoggerFactory.getLogger(AbstractGenotypeProbabilityCalculator.class));
    private final HashMap<Locus, HashMap<Locus, Double>> _probabilityCache = new HashMap<>();

    @Override
    public double calculate(int[] alleleCounts, Locus subjectLocus) {
        // The allele counts are not required for the probability of related unkowns, but the counts are 
        // required for the genotype probability of any other - unrelated - unknowns. We will therefore 
        // update the counts for the alleles in the first - related - unknown.
        for (Allele subjectAllele : subjectLocus.getAlleles()) {
            alleleCounts[subjectAllele.getId()]++;
        }

        Locus referenceLocus = getReferenceSample().getLocus(subjectLocus.getName());
        HashMap<Locus, Double> probsPerReferenceLocus = _probabilityCache.get(referenceLocus);
        if (!ApplicationSettings.isValidationMode()) {
            if (probsPerReferenceLocus == null) {
                probsPerReferenceLocus = new HashMap<>();
                _probabilityCache.put(referenceLocus, probsPerReferenceLocus);
            } else {
                Double cachedValue = probsPerReferenceLocus.get(subjectLocus);
                if (cachedValue != null) {
                    LOG.debug("Genotype Probability for {} from cache = {}", referenceLocus, cachedValue);
                    return cachedValue;
                }
            }
        }

        double calculateSpi = calculateSpi(alleleCounts, referenceLocus, subjectLocus);
        if (!ApplicationSettings.isValidationMode()) {
            probsPerReferenceLocus.put(subjectLocus, calculateSpi);
        }
        LOG.debug("Genotype Probability = {}", calculateSpi);
        return calculateSpi;
    }

    /**
     * Method to be implemented by concrete subclasses of this class to perform
     * the actual probability calculations.
     *
     * @param alleleCounts An array of integers containing the allele counts of
     * all alleles up to the specified locus
     * @param referenceLocus The locus of the reference sample
     * @param subjectLocus The locus for which the genotype probability is to be
     * calculated
     * @return a double containing the probability of observing the supplied
     * genotype
     */
    protected abstract double calculateSpi(int[] alleleCounts, Locus referenceLocus, Locus subjectLocus);

    /**
     * Obtains the reference sample.
     *
     * @return A Sample representing the reference.
     */
    protected abstract Sample getReferenceSample();

    /**
     * Converts a locus to a string suitable for logging to the validation log.
     *
     * @param locus The target locus
     * @return A String suitable for inclusion in the validation log.
     */
    public String toLogString(Locus locus) {
        String retval = locus.getName();
        if (ApplicationSettings.isValidationMode()) {
            retval += ":" + locus.getAlleles();
        }
        return retval;
    }
}
