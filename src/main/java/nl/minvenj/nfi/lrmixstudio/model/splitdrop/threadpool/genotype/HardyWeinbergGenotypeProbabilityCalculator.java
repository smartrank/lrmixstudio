/*
 * Copyright (c) 2014, Netherlands Forensic Institute
 * All rights reserved.
 */
package nl.minvenj.nfi.lrmixstudio.model.splitdrop.threadpool.genotype;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.minvenj.nfi.lrmixstudio.domain.Allele;
import nl.minvenj.nfi.lrmixstudio.domain.Hypothesis;
import nl.minvenj.nfi.lrmixstudio.domain.Locus;
import nl.minvenj.nfi.lrmixstudio.domain.PopulationStatistics;
import nl.minvenj.nfi.lrmixstudio.gui.ApplicationSettings;
import nl.minvenj.nfi.lrmixstudio.model.splitdrop.threadpool.ValidationLogger;

/**
 * Calculates a genotype's probability according to Hardy/Weinberg.
 *
 * @author dejong
 */
public class HardyWeinbergGenotypeProbabilityCalculator implements GenotypeProbabilityCalculator {

    private static final Logger LOG = new ValidationLogger(LoggerFactory.getLogger(HardyWeinbergGenotypeProbabilityCalculator.class));
    private final PopulationStatistics _populationStatistics;

    HardyWeinbergGenotypeProbabilityCalculator(Hypothesis hypothesis) {
        _populationStatistics = hypothesis.getPopulationStatistics();
    }

    @Override
    public double calculate(int[] alleleCounts, Locus locus) {
        double genotypeProbability = 1;
        if (!locus.isHomozygote()) {
            genotypeProbability = 2;
        }
        for (Allele allele : locus.getAlleles()) {
            genotypeProbability *= _populationStatistics.getProbability(locus, allele);
        }

        if (ApplicationSettings.isValidationMode()) {
            String formula = "";
            if (!locus.isHomozygote()) {
                formula = "2 ";
            }
            for (Allele allele : locus.getAlleles()) {
                formula += "p" + allele.getAllele();
            }
            LOG.info("Hardy/Weinberg Genotype Probability for locus {}: {}", toLogString(locus), formula);
        }

        return genotypeProbability;
    }

    public String toLogString(Locus locus) {
        String retval = locus.getName() + ":" + locus.getAlleles();
        return retval;
    }
}
