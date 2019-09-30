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
import nl.minvenj.nfi.lrmixstudio.model.splitdrop.threadpool.ValidationLogger;

/**
 * Calculates the genotype probability for a locus given an assumed
 * grandparent/child * relationship with a certain profiles individual. Theta is assumed to be zero.
 *
 * @author dejong
 */
class GrandparentGenotypeProbabilityCalculatorNoTheta extends AbstractGenotypeProbabilityCalculator {

    private static final Logger LOG = new ValidationLogger(LoggerFactory.getLogger(GrandparentGenotypeProbabilityCalculatorNoTheta.class));
    private final Sample _referenceSample;
    private final PopulationStatistics _populationStatistics;

    public GrandparentGenotypeProbabilityCalculatorNoTheta(Hypothesis hypothesis) {
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
            // Reference and subject alleles match (aa-aa)
            LOG.info("Grandparent Genotype Probability (Theta==0) for reference {}, subject {} (aa-aa)", toLogString(referenceLocus), toLogString(subjectLocus));
            LOG.info("  (p{} * (1 + p{} )) / 2", subjectAllele.getAllele(), subjectAllele.getAllele());
            double p = _populationStatistics.getProbability(referenceLocus, subjectAllele);
            return p * (1 + p) / 2;
        } else {
            // Reference and subject alleles do not match (aa-bb)
            LOG.info("Grandparent Genotype Probability (Theta==0) for reference {}, subject {} (aa-bb)", toLogString(referenceLocus), toLogString(subjectLocus));
            LOG.info("  p{}^2 / 2", subjectAllele.getAllele());
            double p = _populationStatistics.getProbability(referenceLocus, subjectAllele);
            return p * p / 2;
        }
    }

    private double homozygoteReferenceHeterozygoteSubject(Locus referenceLocus, Locus subjectLocus) {
        final Allele referenceAllele = referenceLocus.getAlleles().iterator().next();
        if (subjectLocus.getAlleles().contains(referenceAllele)) {
            // Reference and subject share an allele (aa-ab)
            Allele nonmatchingAllele = null;
            for (Allele subjectAllele : subjectLocus.getAlleles()) {
                if (!referenceLocus.getAlleles().contains(subjectAllele)) {
                    nonmatchingAllele = subjectAllele;
                }
            }
            LOG.info("Grandparent Genotype Probability (Theta==0) for reference {}, subject {} (aa-ab)", toLogString(referenceLocus), toLogString(subjectLocus));
            LOG.info("  ( p{} * ( 1 + 2 * p{} ) ) / 2", nonmatchingAllele.getAllele(), referenceAllele.getAllele());
            return _populationStatistics.getProbability(referenceLocus, nonmatchingAllele) * (1 + 2 * _populationStatistics.getProbability(referenceLocus, referenceAllele)) / 2;
        }
        // Reference and subject do not share any alleles (aa-bc)
        Allele[] subjectAlleleArray = subjectLocus.getAlleles().toArray(new Allele[2]);
        LOG.info("Grandparent Genotype Probability (Theta==0) for reference {}, subject {} (aa-bc)", toLogString(referenceLocus), toLogString(subjectLocus));
        LOG.info("  p{} * p{}", subjectAlleleArray[0].getAllele(), subjectAlleleArray[1].getAllele());
        return _populationStatistics.getProbability(subjectLocus, subjectAlleleArray[0]) * _populationStatistics.getProbability(subjectLocus, subjectAlleleArray[1]);
    }

    private double heterozygoteReferenceHomozygoteSubject(Locus referenceLocus, Locus subjectLocus) {
        Allele subjectAllele = subjectLocus.getAlleles().iterator().next();
        if (referenceLocus.hasAllele(subjectAllele.getAllele())) {
            // Parent and child have a matching allele (ab-aa)
            LOG.info("Grandparent Genotype Probability (Theta==0) for reference {}, subject {} (ab-aa)", toLogString(referenceLocus), toLogString(subjectLocus));
            LOG.info("  p{} * ( 1 + 2 * p{} ) / 4", subjectAllele.getAllele(), subjectAllele.getAllele());
            return _populationStatistics.getProbability(referenceLocus, subjectAllele) * (1 + 2 * _populationStatistics.getProbability(referenceLocus, subjectAllele)) / 4;
        } 
        // Parent and child do not have a matching allele (ab-cc)
        LOG.info("Grandparent Genotype Probability (Theta==0) for reference {}, subject {} (ab-cc)", toLogString(referenceLocus), toLogString(subjectLocus));
        LOG.info("  p{}^2 / 2", subjectAllele.getAllele());
        return _populationStatistics.getProbability(referenceLocus, subjectAllele) * _populationStatistics.getProbability(referenceLocus, subjectAllele) / 2;
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
                LOG.info("Grandparent Genotype Probability (Theta==0) for reference {}, subject {} (ab-cd)", toLogString(referenceLocus), toLogString(subjectLocus));
                LOG.info("  p{} * p{}", mismatchArray[0].getAllele(), mismatchArray[1].getAllele());
                return _populationStatistics.getProbability(subjectLocus, mismatchArray[0]) * _populationStatistics.getProbability(subjectLocus, mismatchArray[1]);
            case 1: // One matching allele (ab-ac)
                LOG.info("Grandparent Genotype Probability (Theta==0) for reference {}, subject {} (ab-ac)", toLogString(referenceLocus), toLogString(subjectLocus));
                LOG.info("  ( p{} * ( 1 + 4 * p{} ) ) / 4", mismatchArray[0].getAllele(), matchArray[0].getAllele());
                return _populationStatistics.getProbability(subjectLocus, mismatchArray[0]) * (1 + 4 * _populationStatistics.getProbability(subjectLocus, matchArray[0])) / 4;
            case 2: // All alleles match (ab-ab)
                LOG.info("Grandparent Genotype Probability (Theta==0) for reference {}, subject {} (ab-ab)", toLogString(referenceLocus), toLogString(subjectLocus));
                LOG.info("  ( p{} + p{} + 4 * p{} * p{} ) / 4", matchArray[0].getAllele(), matchArray[1].getAllele(), matchArray[0].getAllele(), matchArray[1].getAllele());
                double pa = _populationStatistics.getProbability(subjectLocus, matchArray[0]);
                double pb = _populationStatistics.getProbability(subjectLocus, matchArray[1]);
                return (pa + pb + 4 * pa * pb) / 4;
            default:
                throw new IllegalStateException("Unexpected value for matching alleles count: " + match.size() + " for collection " + match);
        }
    }

}
