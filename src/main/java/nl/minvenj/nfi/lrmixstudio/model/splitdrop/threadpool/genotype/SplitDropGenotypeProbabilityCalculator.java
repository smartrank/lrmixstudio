/*
 * Copyright (c) 2014, Netherlands Forensic Institute
 * All rights reserved.
 */
package nl.minvenj.nfi.lrmixstudio.model.splitdrop.threadpool.genotype;

import org.slf4j.LoggerFactory;

import nl.minvenj.nfi.lrmixstudio.domain.Allele;
import nl.minvenj.nfi.lrmixstudio.domain.Hypothesis;
import nl.minvenj.nfi.lrmixstudio.domain.Locus;
import nl.minvenj.nfi.lrmixstudio.domain.PopulationStatistics;
import nl.minvenj.nfi.lrmixstudio.gui.ApplicationSettings;
import nl.minvenj.nfi.lrmixstudio.model.splitdrop.threadpool.ValidationLogger;

/**
 *
 * @author dejong
 */
class SplitDropGenotypeProbabilityCalculator implements GenotypeProbabilityCalculator {

    private static final ValidationLogger LOG = new ValidationLogger(LoggerFactory.getLogger(SplitDropGenotypeProbabilityCalculator.class));
    private final double _theta;
    private final double _oneMinusTheta;
    private final PopulationStatistics _populationStatistics;

    public SplitDropGenotypeProbabilityCalculator(Hypothesis hypothesis) {
        _theta = hypothesis.getThetaCorrection();
        _oneMinusTheta = 1.0 - _theta;
        _populationStatistics = hypothesis.getPopulationStatistics();
    }

    @Override
    public double calculate(int[] alleleCounts, Locus locus) {
        double genotypeProbability = 1;
        if (!locus.isHomozygote()) {
            genotypeProbability = 2;
        }
        for (Allele allele : locus.getAlleles()) {
            genotypeProbability *= (alleleCounts[allele.getId()] * _theta + _oneMinusTheta * _populationStatistics.getProbability(locus, allele));
            alleleCounts[allele.getId()]++;
        }

        if (ApplicationSettings.isValidationMode()) {
            StringBuilder sb = new StringBuilder();
            if (!locus.isHomozygote()) {
                sb.append("2 ");
            }

            for (Allele allele : locus.getAlleles()) {
                sb.append("( n").append(allele.getAllele()).append(" * Theta + (1-Theta * p").append(allele.getAllele()).append(") )");
            }
            LOG.info("Unrelated Genotype Probability for locus {}:   {}", locus, sb.toString());
        }

        return genotypeProbability;
    }
}
