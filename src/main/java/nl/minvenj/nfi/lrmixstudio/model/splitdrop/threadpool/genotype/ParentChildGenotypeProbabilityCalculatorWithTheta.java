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
 *
 * @author dejong
 */
class ParentChildGenotypeProbabilityCalculatorWithTheta extends AbstractGenotypeProbabilityCalculator {

    private static final Logger LOG = new ValidationLogger(LoggerFactory.getLogger(ParentChildGenotypeProbabilityCalculatorWithTheta.class));
    private final Sample _referenceSample;
    private final PopulationStatistics _populationStatistics;
    private final double _theta;
    private final double _oneMinusTheta;
    private final double _onePlusTheta;
    private final double _twoTimesOnePlusTheta;
    private final double _twoTheta;

    public ParentChildGenotypeProbabilityCalculatorWithTheta(Hypothesis hypothesis) {
        _referenceSample = hypothesis.getRelatedness().getRelative();
        _populationStatistics = hypothesis.getPopulationStatistics();
        _theta = hypothesis.getThetaCorrection();
        _oneMinusTheta = 1 - _theta;
        _onePlusTheta = 1 + _theta;
        _twoTimesOnePlusTheta = 2 * _onePlusTheta;
        _twoTheta = 2 * _theta;
    }

    @Override
    protected Sample getReferenceSample() {
        return _referenceSample;
    }

    @Override
    public double calculateSpi(int[] alleleCounts, Locus referenceLocus, Locus subjectLocus) {
        if (referenceLocus.isHomozygote()) {
            return homozygoteReference(referenceLocus, subjectLocus);
        } else {
            return heterozygoteReference(referenceLocus, subjectLocus);
        }
    }

    private double homozygoteReference(Locus referenceLocus, Locus subjectLocus) {
        if (subjectLocus.isHomozygote()) {
            return homozygoteReferenceHomozygoteSubject(referenceLocus, subjectLocus);
        }
        return homozygoteReferenceHeterozygoteSubject(referenceLocus, subjectLocus);
    }

    private double heterozygoteReference(Locus referenceLocus, Locus subjectLocus) {
        if (subjectLocus.isHomozygote()) {
            return heterozygoteReferenceHomozygoteSubject(referenceLocus, subjectLocus);
        }
        return heterozygoteReferenceHeterozygoteSubject(referenceLocus, subjectLocus);
    }

    private double homozygoteReferenceHeterozygoteSubject(Locus referenceLocus, Locus subjectLocus) {
        // Parent is homozygote but child is heterozygote
        final Allele parentAllele = referenceLocus.getAlleles().iterator().next();
        if (subjectLocus.getAlleles().contains(parentAllele)) {
            // Parent and child share an allele. Find the other allele.
            LOG.info("Parent/Child Genotype Probability for reference {}, subject {} (aa-ab)", toLogString(referenceLocus), toLogString(subjectLocus));
            for (Allele childAllele : subjectLocus.getAlleles()) {
                if (!referenceLocus.getAlleles().contains(childAllele)) {
                    LOG.info("  (1-theta)*p{}/(1+theta)", childAllele);
                    return (_oneMinusTheta * _populationStatistics.getProbability(referenceLocus, childAllele)) / _onePlusTheta;
                }
            }
        }
        // Parent and child do not share any alleles
        LOG.info("Parent/Child Genotype Probability for reference {}, subject {} (aa-bc)", toLogString(referenceLocus), toLogString(subjectLocus));
        LOG.info("  0");
        return 0;
    }

    private double homozygoteReferenceHomozygoteSubject(Locus referenceLocus, Locus subjectLocus) {
        // Parent and child are homozygote
        final Allele childAllele = subjectLocus.getAlleles().iterator().next();
        if (referenceLocus.getAlleles().contains(childAllele)) {
            LOG.info("Parent/Child Genotype Probability for reference {}, subject {} (aa-aa)", toLogString(referenceLocus), toLogString(subjectLocus));
            LOG.info("  (2 * theta + ( 1- theta) * p{}) / (1 + theta)", childAllele);
            return (_twoTheta + _oneMinusTheta * _populationStatistics.getProbability(referenceLocus, childAllele)) / _onePlusTheta;
        } 
        // Parent and child alleles do not match
        LOG.info("Parent/Child Genotype Probability for reference {}, subject {} (aa-bb)", toLogString(referenceLocus), toLogString(subjectLocus));
        LOG.info("  0");
        return 0;
    }

    private double heterozygoteReferenceHomozygoteSubject(Locus referenceLocus, Locus subjectLocus) {
        Allele childAllele = subjectLocus.getAlleles().iterator().next();
        if (referenceLocus.hasAllele(childAllele.getAllele())) {
            // Parent and child have a matching allele
            LOG.info("Parent/Child Genotype Probability for reference {}, subject {} (ab-aa)", toLogString(referenceLocus), toLogString(subjectLocus));
            LOG.info("  (theta + ( 1- theta) * p{}) / (2*(1 + theta))", childAllele);
            return (_theta + _oneMinusTheta * _populationStatistics.getProbability(referenceLocus, childAllele)) / _twoTimesOnePlusTheta;
        }

        LOG.info("Parent/Child Genotype Probability for reference {}, subject {} (ab-cc)", toLogString(referenceLocus), toLogString(subjectLocus));
        LOG.info("  0");
        return 0;
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
                LOG.info("Parent/Child Genotype Probability for reference {}, subject {} (ab-cd)", toLogString(referenceLocus), toLogString(subjectLocus));
                LOG.info("  0");
                return 0;
            case 1: // One matching allele (ab-ac)
                LOG.info("Parent/Child Genotype Probability for reference {}, subject {} (ab-ac)", toLogString(referenceLocus), toLogString(subjectLocus));
                final Allele allele = mismatch.iterator().next();
                LOG.info("  ( ( 1 - theta ) * p{} ) / (2 * (1 + theta))", allele.getAllele());
                return (_oneMinusTheta * _populationStatistics.getProbability(subjectLocus, allele)) / _twoTimesOnePlusTheta;
            case 2: // All alleles match (ab-ab)
                LOG.info("Parent/Child Genotype Probability for reference {}, subject {} (ab-ab)", toLogString(referenceLocus), toLogString(subjectLocus));
                String matchNames = "";
                double probability = 0;
                for (Allele a : match) {
                    if (LOG.isInfoEnabled()) {
                        if (!matchNames.isEmpty()) {
                            matchNames += " + ";
                        }
                        matchNames += a.getAllele();
                    }
                    probability += _populationStatistics.getProbability(subjectLocus, a);
                }
                LOG.info("  (2 * theta + ( 1 - theta )*( {} ) / ( 2 * ( 1 + theta ) )", matchNames);
                return (_twoTheta + _oneMinusTheta * probability) / _twoTimesOnePlusTheta;
            default:
                throw new IllegalStateException("Unexpected value for matching alleles count: " + match.size() + " for collection " + match);
        }
    }
}
