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
 * Calculates the genotype probability for a locus given an assumed parent/child
 * relationship with a certain profiles individual. Theta is assumed to be zero.
 *
 * @author dejong
 */
class ParentChildGenotypeProbabilityCalculatorNoTheta extends AbstractGenotypeProbabilityCalculator {

    private static final Logger LOG = new ValidationLogger(LoggerFactory.getLogger(ParentChildGenotypeProbabilityCalculatorNoTheta.class));
    private final Sample _referenceSample;
    private final PopulationStatistics _populationStatistics;

    public ParentChildGenotypeProbabilityCalculatorNoTheta(Hypothesis hypothesis) {
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
            LOG.info("Parent/Child Genotype Probability (Theta==0) for reference {}, subject {} (aa-aa)", toLogString(referenceLocus), toLogString(subjectLocus));
            LOG.info("  p{}", subjectAllele.getAllele());
            return _populationStatistics.getProbability(referenceLocus, subjectAllele);
        } else {
            // Parent and child alleles do not match (aa-bb)
            LOG.info("Parent/Child Genotype Probability (Theta==0) for reference {}, subject {} (aa-bb)", toLogString(referenceLocus), toLogString(subjectLocus));
            LOG.info("  0");
            return 0;
        }

    }

    private double homozygoteReferenceHeterozygoteSubject(Locus referenceLocus, Locus subjectLocus) {
        final Allele parentAllele = referenceLocus.getAlleles().iterator().next();
        if (subjectLocus.getAlleles().contains(parentAllele)) {
            LOG.info("Parent/Child Genotype Probability (Theta==0) for reference {}, subject {} (aa-ab)", toLogString(referenceLocus), toLogString(subjectLocus));
            // Parent and child share an allele (aa-ab)
            for (Allele childAllele : subjectLocus.getAlleles()) {
                if (!referenceLocus.getAlleles().contains(childAllele)) {
                    LOG.info("  p{}", childAllele.getAllele());
                    return _populationStatistics.getProbability(referenceLocus, childAllele);
                }
            }
        }
        // Parent and child do not share any alleles (aa-bc)
        LOG.info("Parent/Child Genotype Probability (Theta==0) for reference {}, subject {} (aa-bc)", toLogString(referenceLocus), toLogString(subjectLocus));
        LOG.info("  0");
        return 0;
    }

    private double heterozygoteReferenceHomozygoteSubject(Locus referenceLocus, Locus subjectLocus) {
        Allele childAllele = subjectLocus.getAlleles().iterator().next();
        if (referenceLocus.hasAllele(childAllele.getAllele())) {
            // Parent and child have a matching allele (ab-aa)
            LOG.info("Parent/Child Genotype Probability (Theta==0) for reference {}, subject {} (ab-aa)", toLogString(referenceLocus), toLogString(subjectLocus));
            LOG.info("  p{}/2", childAllele.getAllele());
            return _populationStatistics.getProbability(referenceLocus, childAllele) / 2;
        } else {
            // Parent and child do not have a matching allele (ab-cc)
            LOG.info("Parent/Child Genotype Probability (Theta==0) for reference {}, subject {} (ab-cc)", toLogString(referenceLocus), toLogString(subjectLocus));
            LOG.info("  0");
            return 0;
        }
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

        switch (match.size()) {
            case 0: // No matching alleles (ab-cd)
            LOG.info("Parent/Child Genotype Probability (Theta==0) for reference {}, subject {} (ab-cd)", toLogString(referenceLocus), toLogString(subjectLocus));
                LOG.info("  0");
                return 0;
            case 1: // One matching allele (ab-ac)
            LOG.info("Parent/Child Genotype Probability (Theta==0) for reference {}, subject {} (ab-ac)", toLogString(referenceLocus), toLogString(subjectLocus));
                final Allele allele = mismatch.iterator().next();
                LOG.info("  p{}/2", allele.getAllele());
                return _populationStatistics.getProbability(subjectLocus, allele) / 2;
            case 2: // All alleles match (ab-ab)
            LOG.info("Parent/Child Genotype Probability (Theta==0) for reference {}, subject {} (ab-ab)", toLogString(referenceLocus), toLogString(subjectLocus));
                double probability = 0;
                for (Allele a : match) {
                    LOG.info("  p{}", a.getAllele());
                    probability += _populationStatistics.getProbability(subjectLocus, a);
                }
                LOG.info("  /2");
                return probability / 2;
            default:
                throw new IllegalStateException("Unexpected value for matching alleles count: " + match.size() + " for collection " + match);
        }
    }

}
