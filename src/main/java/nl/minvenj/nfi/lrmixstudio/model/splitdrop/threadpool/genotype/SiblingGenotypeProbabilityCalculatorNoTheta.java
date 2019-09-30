/*
 * Copyright (c) 2016, Netherlands Forensic Institute
 * All rights reserved.
 */
package nl.minvenj.nfi.lrmixstudio.model.splitdrop.threadpool.genotype;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.minvenj.nfi.lrmixstudio.domain.Allele;
import nl.minvenj.nfi.lrmixstudio.domain.Hypothesis;
import nl.minvenj.nfi.lrmixstudio.domain.Locus;
import nl.minvenj.nfi.lrmixstudio.domain.PopulationStatistics;
import nl.minvenj.nfi.lrmixstudio.domain.Sample;
import nl.minvenj.nfi.lrmixstudio.gui.ApplicationSettings;
import nl.minvenj.nfi.lrmixstudio.model.splitdrop.threadpool.ValidationLogger;

/**
 * Calculates the genotype probability for a locus given an assumed sibling
 * relationship with a certain profiles individual. Theta is assumed to be zero.
 *
 * @author dejong
 */
class SiblingGenotypeProbabilityCalculatorNoTheta extends AbstractGenotypeProbabilityCalculator {

    private static final Logger LOG = new ValidationLogger(LoggerFactory.getLogger(SiblingGenotypeProbabilityCalculatorNoTheta.class));
    private final Sample _referenceSample;
    private final PopulationStatistics _populationStatistics;

    public SiblingGenotypeProbabilityCalculatorNoTheta(Hypothesis hypothesis) {
        if (hypothesis.getThetaCorrection() > 0) {
            throw new IllegalArgumentException("Theta should be zero!");
        }
        _referenceSample = hypothesis.getRelatedness().getRelative();
        _populationStatistics = hypothesis.getPopulationStatistics();
    }

    @Override
    public double calculateSpi(int[] alleleCounts, Locus referenceLocus, Locus subjectLocus) {
        double prob;
        if (referenceLocus.isHomozygote()) {
            prob = homozygoteReference(referenceLocus, subjectLocus);
        } else {
            prob = heterozygoteReference(referenceLocus, subjectLocus);
        }
        return prob;
    }

    /**
     * Calculates the genotype probability for a subject against a homozygote
     * reference.
     *
     * @param referenceLocus The reference locus
     * @param subjectLocus The current subject to evaluate against the reference
     * @return a double containing the genotype probability of the subject as
     * sibling of the reference
     */
    private double homozygoteReference(Locus referenceLocus, Locus subjectLocus) {
        double prob = 1;
        String formula = "";
        for (Allele subjectAllele : subjectLocus.getAlleles()) {
            if (referenceLocus.hasAllele(subjectAllele.getAllele())) {
                if (ApplicationSettings.isValidationMode()) {
                    formula += "(1 + p" + subjectAllele.getAllele() + ") ";
                }
                prob *= (1 + _populationStatistics.getProbability(subjectLocus, subjectAllele));
            } else {
                if (ApplicationSettings.isValidationMode()) {
                    formula += "p" + subjectAllele.getAllele() + " ";
                }
                prob *= _populationStatistics.getProbability(subjectLocus, subjectAllele);
            }
        }

        LOG.info("Sibling Genotype Probability (Theta==0) for reference {}, subject {} (aa-any)", toLogString(referenceLocus), toLogString(subjectLocus));
        if (subjectLocus.isHomozygote()) {
            LOG.info("  ( {} ) / 4", formula);
            prob /= 4;
        } else {
            LOG.info("  ( {} ) / 2", formula);
            prob /= 2;
        }

        return prob;
    }

    /**
     * Calculates the genotype probability for a subject against a heterozygote
     * reference.
     *
     * @param referenceLocus The reference locus
     * @param subjectLocus The current subject to evaluate against the reference
     * @return a double containing the genotype probability of the subject as
     * sibling of the reference
     */
    private double heterozygoteReference(Locus referenceLocus, Locus subjectLocus) {
        if (subjectLocus.isHomozygote()) {
            return heterozygoteReferenceHomozygoteSubject(referenceLocus, subjectLocus);
        } else {
            return heterozygoteReferenceHeterozygoteSubject(referenceLocus, subjectLocus);
        }
    }

    private double heterozygoteReferenceHomozygoteSubject(Locus referenceLocus, Locus subjectLocus) {
        Allele subjectAllele = subjectLocus.getAlleles().iterator().next();
        final Double probability = _populationStatistics.getProbability(referenceLocus, subjectAllele);
        if (referenceLocus.getAlleles().contains(subjectAllele)) {
            // Reference and subject have a matching allele (ab-aa)
            LOG.info("Sibling Genotype Probability (Theta==0) for reference {}, subject {} (ab-aa)", toLogString(referenceLocus), toLogString(subjectLocus));
            LOG.info("  (p{} * ( 1 + p{} )) / 4", subjectAllele.getAllele(), subjectAllele.getAllele());
            return probability * (1 + probability) / 4;
        } else {
            // Reference and subject do not have a matching allele (ab-cc)
            LOG.info("Sibling Genotype Probability (Theta==0) for reference {}, subject {} (ab-cc)", toLogString(referenceLocus), toLogString(subjectLocus));
            LOG.info("  p{}^2 / 4", subjectAllele.getAllele());
            return probability * probability / 4;
        }

    }

    private double heterozygoteReferenceHeterozygoteSubject(Locus referenceLocus, Locus subjectLocus) {
        // Reference and subject are both heterozygote
        int matchCount = 0;
        ArrayList<Allele> matching = new ArrayList<>();
        ArrayList<Allele> mismatching = new ArrayList<>();
        for (Allele subjectAllele : subjectLocus.getAlleles()) {
            if (referenceLocus.getAlleles().contains(subjectAllele)) {
                matching.add(subjectAllele);
            } else {
                mismatching.add(subjectAllele);
            }
        }

        Allele[] matchingArray = matching.toArray(new Allele[matching.size()]);
        Allele[] mismatchingArray = mismatching.toArray(new Allele[mismatching.size()]);

        switch (matching.size()) {
            case 0: // No match (ab-cd)
                LOG.info("Sibling Genotype Probability (Theta==0) for reference {}, subject {} (ab-cd)", toLogString(referenceLocus), toLogString(subjectLocus));
                LOG.info("  (p{} * p{}) / 2", mismatchingArray[0].getAllele(), mismatchingArray[1].getAllele());
                return _populationStatistics.getProbability(referenceLocus, mismatchingArray[0]) * _populationStatistics.getProbability(referenceLocus, mismatchingArray[1]) / 2;
            case 1: // One allele matches (ab-ac)
                LOG.info("Sibling Genotype Probability (Theta==0) for reference {}, subject {} (ab-ac)", toLogString(referenceLocus), toLogString(subjectLocus));
                LOG.info("  (p{} * (1 + 2 * p{}) / 4", mismatchingArray[0].getAllele(), matchingArray[0].getAllele());
                return _populationStatistics.getProbability(referenceLocus, mismatchingArray[0]) * (1 + 2 * _populationStatistics.getProbability(referenceLocus, matchingArray[0])) / 4;
            case 2: // Both alleles match (ab-ab)
                double pA = _populationStatistics.getProbability(referenceLocus, matchingArray[0]);
                double pB = _populationStatistics.getProbability(referenceLocus, matchingArray[1]);

                LOG.info("Sibling Genotype Probability (Theta==0) for reference {}, subject {} (ab-ab)", toLogString(referenceLocus), toLogString(subjectLocus));
                LOG.info("  (1 + p{} + p{} + 2*p{}p{}) / 4", matchingArray[0].getAllele(), matchingArray[1].getAllele(), matchingArray[0].getAllele(), matchingArray[1].getAllele());
                return (1f + pA + pB + 2f * pA * pB) / 4f;
            default:
                throw new IllegalStateException("Unexpected match count: " + matchCount);
        }
    }

    @Override
    protected Sample getReferenceSample() {
        return _referenceSample;
    }

}
