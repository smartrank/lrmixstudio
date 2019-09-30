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
class GrandparentGenotypeProbabilityCalculatorWithTheta extends AbstractGenotypeProbabilityCalculator {

    private static final Logger LOG = new ValidationLogger(LoggerFactory.getLogger(GrandparentGenotypeProbabilityCalculatorWithTheta.class));
    private final Sample _referenceSample;
    private final PopulationStatistics _populationStatistics;
    private final double _theta;
    private final double _oneMinusTheta;
    private final double _onePlusTheta;
    private final double _twoTimesOnePlusTheta;
    private final double _fourTimesOnePlusTheta;
    private final double _twoTheta;
    private final double _onePlusTwoTheta;

    public GrandparentGenotypeProbabilityCalculatorWithTheta(Hypothesis hypothesis) {
        _referenceSample = hypothesis.getRelatedness().getRelative();
        _populationStatistics = hypothesis.getPopulationStatistics();
        _theta = hypothesis.getThetaCorrection();
        _oneMinusTheta = 1 - _theta;
        _onePlusTheta = 1 + _theta;
        _onePlusTwoTheta = 1 + 2 * _theta;
        _twoTimesOnePlusTheta = 2 * _onePlusTheta;
        _fourTimesOnePlusTheta = 4 * _onePlusTheta;
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
        final Allele referenceAllele = referenceLocus.getAlleles().iterator().next();
        final Allele[] subjectAlleles = subjectLocus.getAlleles().toArray(new Allele[2]);
        if (subjectLocus.getAlleles().contains(referenceAllele)) {
            LOG.info("Grandparent Genotype Probability for reference {}, subject {} (aa-ab)", toLogString(referenceLocus), toLogString(subjectLocus));
            // Find non-matching allele (b) in subject
            Allele nonMatching = subjectAlleles[0];
            if (nonMatching.equals(referenceAllele)) {
                nonMatching = subjectAlleles[1];
            }
            LOG.info("  ( ( ( 1 - theta ) * p{} ) / ( 2 * ( 1 + theta ) ) ) * ( 1 + ( ( 2 * ( 2 * theta + ( 1 - theta ) * p{} ) ) / ( 1 + 2 * theta ) ) )", nonMatching.getAllele(), referenceAllele.getAllele());
            double pA = _populationStatistics.getProbability(subjectLocus, referenceAllele);
            double pB = _populationStatistics.getProbability(subjectLocus, nonMatching);
            return (((1 - _theta) * pB) / (2 * (1 + _theta))) * (1 + ((2 * (2 * _theta + (1 - _theta) * pA)) / (1 + 2 * _theta)));
        }
        // Reference and subject do not share any alleles
        LOG.info("Grandparent Genotype Probability for reference {}, subject {} (aa-bc)", toLogString(referenceLocus), toLogString(subjectLocus));
        LOG.info("  ( ( 1 - theta )^2 * p{} * p{} ) / ( ( 1 + theta ) * ( 1 + 2 * theta ) )", subjectAlleles[0].getAllele(), subjectAlleles[1].getAllele());
        return ((1 - _theta) * (1 - _theta) * _populationStatistics.getProbability(subjectLocus, subjectAlleles[0]) * _populationStatistics.getProbability(subjectLocus, subjectAlleles[1])) / ((1 + _theta) * (1 + 2 * _theta));
    }

    private double homozygoteReferenceHomozygoteSubject(Locus referenceLocus, Locus subjectLocus) {
        // Parent and child are homozygote
        final Allele subjectAllele = subjectLocus.getAlleles().iterator().next();
        if (referenceLocus.getAlleles().contains(subjectAllele)) {
            LOG.info("Grandparent Genotype Probability for reference {}, subject {} (aa-aa)", toLogString(referenceLocus), toLogString(subjectLocus));
            LOG.info("  ( ( 2 * theta + ( 1 - theta ) * p{} ) / ( 2 * ( 1 + theta ) ) ) * ( 1 + ( ( 3 * theta + ( 1 - theta ) * p{} ) / ( 1 + 2 * theta ) ) )", subjectAllele.getAllele(), subjectAllele.getAllele());
            double p = _populationStatistics.getProbability(referenceLocus, subjectAllele);
            return ((2 * _theta + (1 - _theta) * p) / (2 * (1 + _theta))) * (1 + ((3 * _theta + (1 - _theta) * p) / (1 + 2 * _theta)));
        } 
        // Parent and child alleles do not match
        LOG.info("Grandparent Genotype Probability for reference {}, subject {} (aa-bb)", toLogString(referenceLocus), toLogString(subjectLocus));
        LOG.info("  ( ( 1 - theta ) * p{} * (theta + (1 - theta) * p{} ) ) / ( 2 * ( 1 + theta ) * ( 1 + 2 * theta ) )", subjectAllele.getAllele(), subjectAllele.getAllele());
        double p = _populationStatistics.getProbability(referenceLocus, subjectAllele);
        return ((1 - _theta) * p * (_theta + (1 - _theta) * p)) / (2 * (1 + _theta) * (1 + 2 * _theta));
    }

    private double heterozygoteReferenceHomozygoteSubject(Locus referenceLocus, Locus subjectLocus) {
        Allele subjectAllele = subjectLocus.getAlleles().iterator().next();
        final Double pSubject = _populationStatistics.getProbability(referenceLocus, subjectAllele);
        if (referenceLocus.hasAllele(subjectAllele.getAllele())) {
            // Parent and child have a matching allele
            LOG.info("Grandparent Genotype Probability for reference {}, subject {} (ab-aa)", toLogString(referenceLocus), toLogString(subjectLocus));
            LOG.info("  ( ( theta + ( 1 - theta ) * p{}) / ( 4 * ( 1 + theta ) ) ) * (  1 + ( ( 2 * (2 * theta + ( 1 - theta ) * p{} ) ) / ( 1 + 2 * theta ) ) )", subjectAllele, subjectAllele);
            return ((_theta + (1 - _theta) * pSubject) / (4 * (1 + _theta))) * (1 + ((2 * (2 * _theta + (1 - _theta) * pSubject)) / (1 + 2 * _theta)));
        }

        LOG.info("Grandparent Genotype Probability for reference {}, subject {} (ab-cc)", toLogString(referenceLocus), toLogString(subjectLocus));
        LOG.info("  ( ( theta + ( 1 - theta ) * p{} ) * ( 1 - theta ) * p{} ) / ( 2 * ( 1 + theta ) * ( 1 + 2 * theta ) )", subjectAllele.getAllele(), subjectAllele.getAllele());
        return ((_theta + (1 - _theta) * pSubject) * (1 - _theta) * pSubject) / (2 * (1 + _theta) * (1 + 2 * _theta));
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
                LOG.info("Grandparent Genotype Probability for reference {}, subject {} (ab-cd)", toLogString(referenceLocus), toLogString(subjectLocus));
                Allele[] mismatchArray = mismatch.toArray(new Allele[mismatch.size()]);
                LOG.info("  ( ( 1 - theta ) * ( 1 - theta ) * p{} * p{} ) / ( ( 1 + theta ) * ( 1 + 2 * theta ) )", mismatchArray[0].getAllele(), mismatchArray[1].getAllele());
                double p = 1;
                for (Allele a : mismatch) {
                    p *= (1 - _theta) * _populationStatistics.getProbability(subjectLocus, a);
                }
                return p / ((1 + _theta) * (1 + 2 * _theta));
            case 1: // One matching allele (ab-ac)
                LOG.info("Grandparent Genotype Probability for reference {}, subject {} (ab-ac)", toLogString(referenceLocus), toLogString(subjectLocus));
                final Allele mismatchAllele = mismatch.iterator().next();
                final Allele matchAllele = match.iterator().next();
                final double pMismatch = _populationStatistics.getProbability(subjectLocus, mismatchAllele);
                final double pMatch = _populationStatistics.getProbability(subjectLocus, matchAllele);
                LOG.info("  ( ( ( 1 - theta ) * p{} ) / ( 4 * (1 + theta))) * ( 1 + ( ( 4 * ( theta + (1 - theta) * p{} ) ) / ( 1 + 2 * theta ) ) ) ", mismatchAllele.getAllele(), matchAllele.getAllele());
                return ((_oneMinusTheta * pMismatch) / (4 * _onePlusTheta)) * (1 + ((4 * (_theta + (1 - _theta) * pMatch)) / (1 + 2 * _theta)));
            case 2: // All alleles match (ab-ab)
                LOG.info("Grandparent Genotype Probability for reference {}, subject {} (ab-ab)", toLogString(referenceLocus), toLogString(subjectLocus));
                Allele[] alleles = match.toArray(new Allele[2]);
                LOG.info("  ( ( 1 / ( 4 * ( 1 + theta ) ) ) * ( 2 * theta + ( 1 - theta ) * ( p{} + p{} ) + ( 4 * ( theta + ( 1 - theta ) * p{} )* ( theta + ( 1 - theta ) * p{} ) ) / ( 1 + 2 * theta ) ) ) ", alleles[0], alleles[1], alleles[0], alleles[1]);
                double pA = _populationStatistics.getProbability(subjectLocus, alleles[0]);
                double pB = _populationStatistics.getProbability(subjectLocus, alleles[1]);
                return ((1 / (4 * (1 + _theta))) * (2 * _theta + (1 - _theta) * (pA + pB) + (4 * (_theta + (1 - _theta) * pA) * (_theta + (1 - _theta) * pB)) / (1 + 2 * _theta)));
            default:
                throw new IllegalStateException("Unexpected value for matching alleles count: " + match.size() + " for collection " + match);
        }
    }
}
