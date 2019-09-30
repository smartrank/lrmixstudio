/*
 * Copyright (c) 2014, Netherlands Forensic Institute
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

/**
 * Calculates the genotype probability for a locus given an assumed cousin
 * relationship with a certain profiles individual. Theta is assumed to be zero.
 *
 * @author dejong
 */
class CousinsGenotypeProbabilityCalculatorNoTheta extends AbstractGenotypeProbabilityCalculator {

    private static final Logger LOG = LoggerFactory.getLogger(CousinsGenotypeProbabilityCalculatorNoTheta.class);
    private final Sample _referenceSample;
    private final PopulationStatistics _populationStatistics;

    public CousinsGenotypeProbabilityCalculatorNoTheta(Hypothesis hypothesis) {
        if (hypothesis.getThetaCorrection() > 0) {
            throw new IllegalArgumentException("Theta should be zero!");
        }
        _referenceSample = hypothesis.getRelatedness().getRelative();
        _populationStatistics = hypothesis.getPopulationStatistics();
    }

    @Override
    public double calculateSpi(int[] alleleCounts, Locus referenceLocus, Locus subjectLocus) {
        if (referenceLocus.isHomozygote()) {
            return homozygoteReference(referenceLocus, subjectLocus);
        } else {
            return heterozygoteReference(referenceLocus, subjectLocus);
        }
    }

    @Override
    protected Sample getReferenceSample() {
        return _referenceSample;
    }

    private double homozygoteReference(Locus referenceLocus, Locus subjectLocus) {
        if (subjectLocus.isHomozygote()) {
            return homozygoteReferenceHomozygoteSubject(referenceLocus, subjectLocus);
        } else {
            return homozygoteReferenceHeterozygoteSubject(referenceLocus, subjectLocus);
        }
    }

    private double heterozygoteReference(Locus referenceLocus, Locus subjectLocus) {
        if (subjectLocus.isHomozygote()) {
            return heterozygoteReferenceHomozygoteSubject(referenceLocus, subjectLocus);
        } else {
            return heterozygoteReferenceHeterozygoteSubject(referenceLocus, subjectLocus);
        }
    }

    private double homozygoteReferenceHomozygoteSubject(Locus referenceLocus, Locus subjectLocus) {
        final Allele subjectAllele = subjectLocus.getAlleles().iterator().next();
        if (referenceLocus.getAlleles().contains(subjectAllele)) {
            // Parent and child alleles match (aa-aa)
            LOG.info("Cousins Genotype Probability (Theta==0) for reference {}, subject {} (aa-aa)", toLogString(referenceLocus), toLogString(subjectLocus));
            LOG.info("  (p{} * (1 + 3 * p{})) / 4", subjectAllele.getAllele(), subjectAllele.getAllele());
            final Double p = _populationStatistics.getProbability(referenceLocus, subjectAllele);
            return (p * (1 + 3 * p)) / 4;
        } else {
            // Parent and child alleles do not match (aa-bb)
            LOG.info("Cousins Genotype Probability (Theta==0) for reference {}, subject {} (aa-bb)", toLogString(referenceLocus), toLogString(subjectLocus));
            LOG.info("  (3 * p{}^2) / 4", subjectAllele.getAllele());
            final Double p = _populationStatistics.getProbability(referenceLocus, subjectAllele);
            return (3 * p * p) / 4;
        }
    }

    private double homozygoteReferenceHeterozygoteSubject(Locus referenceLocus, Locus subjectLocus) {
        final Allele parentAllele = referenceLocus.getAlleles().iterator().next();
        if (subjectLocus.getAlleles().contains(parentAllele)) {
            LOG.info("Cousins Genotype Probability (Theta==0) for reference {}, subject {} (aa-ab)", toLogString(referenceLocus), toLogString(subjectLocus));
            // Reference and subject share an allele (aa-ab)
            Allele nonmatchingAllele = null;
            for (Allele childAllele : subjectLocus.getAlleles()) {
                if (!referenceLocus.getAlleles().contains(childAllele)) {
                    nonmatchingAllele = childAllele;
                }
            }
            LOG.info("  ( p{} * (1 + 6*p{})) / 4 ", nonmatchingAllele.getAllele(), parentAllele.getAllele());
            return _populationStatistics.getProbability(referenceLocus, nonmatchingAllele) * (1 + 6 * _populationStatistics.getProbability(referenceLocus, parentAllele)) / 4;
        }
        // Parent and child do not share any alleles (aa-bc)
        LOG.info("Cousins Genotype Probability (Theta==0) for reference {}, subject {} (aa-bc)", toLogString(referenceLocus), toLogString(subjectLocus));
        Allele[] alleles = subjectLocus.getAlleles().toArray(new Allele[2]);
        LOG.info("  (3 * p{} * p{}) / 2", alleles[0].getAllele(), alleles[1].getAllele());
        return 3 * _populationStatistics.getProbability(referenceLocus, alleles[0]) * _populationStatistics.getProbability(referenceLocus, alleles[1]) / 2;
    }

    private double heterozygoteReferenceHomozygoteSubject(Locus referenceLocus, Locus subjectLocus) {
        Allele childAllele = subjectLocus.getAlleles().iterator().next();
        if (referenceLocus.hasAllele(childAllele.getAllele())) {
            // Parent and child have a matching allele (ab-aa)
            LOG.info("Cousins Genotype Probability (Theta==0) for reference {}, subject {} (ab-aa)", toLogString(referenceLocus), toLogString(subjectLocus));
            LOG.info("  (p{} * (1 + 6 * p{})) / 8", childAllele.getAllele(), childAllele.getAllele());
            Double p = _populationStatistics.getProbability(referenceLocus, childAllele);
            return (p * (1 + 6 * p)) / 8;
        }
        // Parent and child do not have a matching allele (ab-cc)
        LOG.info("Cousins Genotype Probability (Theta==0) for reference {}, subject {} (ab-cc)", toLogString(referenceLocus), toLogString(subjectLocus));
        LOG.info("  (3 * p{}^2) / 2", childAllele.getAllele());
        Double p = _populationStatistics.getProbability(referenceLocus, childAllele);
        return (3 * p * p) / 2;
    }

    private double heterozygoteReferenceHeterozygoteSubject(Locus referenceLocus, Locus subjectLocus) {
        ArrayList<Allele> match = new ArrayList<>();
        ArrayList<Allele> mismatch = new ArrayList<>();
        for (Allele subjectAllele : subjectLocus.getAlleles()) {
            if (referenceLocus.getAlleles().contains(subjectAllele)) {
                match.add(subjectAllele);
            } else {
                mismatch.add(subjectAllele);
            }
        }

        Allele[] matchArray = match.toArray(new Allele[match.size()]);
        Allele[] mismatchArray = mismatch.toArray(new Allele[mismatch.size()]);

        switch (match.size()) {
            case 0: // No matching alleles (ab-cd)
                LOG.info("Cousins Genotype Probability (Theta==0) for reference {}, subject {} (ab-cd)", toLogString(referenceLocus), toLogString(subjectLocus));
                LOG.info("  3 * p{} * p{} / 2", mismatchArray[0].getAllele(), mismatchArray[1].getAllele());
                return 3 * _populationStatistics.getProbability(subjectLocus, mismatchArray[0]) * _populationStatistics.getProbability(subjectLocus, mismatchArray[1]) / 2;
            case 1: // One matching allele (ab-ac)
                LOG.info("Cousins Genotype Probability (Theta==0) for reference {}, subject {} (ab-ac)", toLogString(referenceLocus), toLogString(subjectLocus));
                LOG.info("  (p{} * (1 + 12*p{})) / 8", mismatchArray[0].getAllele(), matchArray[0].getAllele());
                return (_populationStatistics.getProbability(subjectLocus, mismatchArray[0]) * (1 + 12 * _populationStatistics.getProbability(subjectLocus, matchArray[0]))) / 8;
            case 2: // All alleles match (ab-ab)
                LOG.info("Cousins Genotype Probability (Theta==0) for reference {}, subject {} (ab-ab)", toLogString(referenceLocus), toLogString(subjectLocus));
                LOG.info("  (p{} + p{} + 12 * p{} * p{}) / 8", matchArray[0].getAllele(), matchArray[1].getAllele(), matchArray[0].getAllele(), matchArray[1].getAllele());
                double pA = _populationStatistics.getProbability(subjectLocus, matchArray[0]);
                double pB = _populationStatistics.getProbability(subjectLocus, matchArray[1]);
                return (pA + pB + 12 * pA * pB) / 8;
            default:
                throw new IllegalStateException("Unexpected value for matching alleles count: " + match.size() + " for collection " + match);
        }
    }

}
