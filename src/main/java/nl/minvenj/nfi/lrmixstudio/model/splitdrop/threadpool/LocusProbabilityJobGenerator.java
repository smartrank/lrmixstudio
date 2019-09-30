/**
 * Copyright (C) 2013, 2014 Netherlands Forensic Institute
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package nl.minvenj.nfi.lrmixstudio.model.splitdrop.threadpool;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.minvenj.nfi.lrmixstudio.domain.Allele;
import nl.minvenj.nfi.lrmixstudio.domain.Contributor;
import nl.minvenj.nfi.lrmixstudio.domain.Hypothesis;
import nl.minvenj.nfi.lrmixstudio.domain.Locus;
import nl.minvenj.nfi.lrmixstudio.domain.Sample;
import nl.minvenj.nfi.lrmixstudio.gui.ApplicationSettings;
import nl.minvenj.nfi.lrmixstudio.model.AnalysisProgressListener;
import nl.minvenj.nfi.lrmixstudio.model.splitdrop.PermutationIterator;
import nl.minvenj.nfi.lrmixstudio.model.splitdrop.PermutationIteratorFactory;

public class LocusProbabilityJobGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(LocusProbabilityJobGenerator.class);

    private LocusProbabilityJobGenerator() {
    }

    public static ArrayList<LocusProbabilityJob> generate(final String locusName, final Collection<Sample> activeReplicates, final Hypothesis hypothesis, final AnalysisProgressListener progress) {
        return generate(null, locusName, activeReplicates, hypothesis, progress);
    }

    public static ArrayList<LocusProbabilityJob> generate(final ExecutorService service, final String locusName, final Collection<Sample> activeReplicates, final Hypothesis hypothesis, final AnalysisProgressListener progress) {

        final ArrayList<LocusProbabilityJob> retval = new ArrayList<>();

        if (hypothesis.getUnknownCount() > 0) {

            LOG.debug("Locus {} under {} has {} unknowns.", locusName, hypothesis.getId(), hypothesis.getUnknownCount());
            // Create the set of all possible allele combinations at the current locus
            final HashMap<String, Locus> possibleAlleleCombinations = new HashMap<>();
            final Collection<String> alleleCollection = new ArrayList<>();

            // LRDYN-133 Add rare alleles observed in the replicates
            for (final Sample replicate : activeReplicates) {
                Locus replicateLocus = replicate.getLocus(locusName);
                if (replicateLocus == null) {
                    replicateLocus = new Locus(locusName);
                    replicateLocus.setSample(replicate);
                }
                for (final Allele replicateAllele : replicateLocus.getAlleles()) {
                    if (!alleleCollection.contains(replicateAllele.getAllele())) {
                        alleleCollection.add(replicateAllele.getAllele());
                    }
                }
            }

            // Add alleles in the contributor samples (only non-rare alleles)
            for (final Contributor con : hypothesis.getContributors()) {
                Locus conLocus = con.getSample().getLocus(locusName);
                if (conLocus == null) {
                    conLocus = new Locus(locusName);
                    conLocus.setSample(con.getSample());
                }
                for (final Allele conAllele : conLocus.getAlleles()) {
                    if (!alleleCollection.contains(conAllele.getAllele()) && !hypothesis.getPopulationStatistics().isRareAllele(conAllele)) {
                        alleleCollection.add(conAllele.getAllele());
                    }
                }
            }

            // Add alleles in the non-contributor samples (only non-rare alleles)
            for (final Contributor nonCon : hypothesis.getNonContributors()) {
                Locus nonConLocus = nonCon.getSample().getLocus(locusName);
                if (nonConLocus == null) {
                    nonConLocus = new Locus(locusName);
                    nonConLocus.setSample(nonCon.getSample());
                }
                for (final Allele nonConAllele : nonConLocus.getAlleles()) {
                    if (!alleleCollection.contains(nonConAllele.getAllele()) && !hypothesis.getPopulationStatistics().isRareAllele(nonConAllele)) {
                        alleleCollection.add(nonConAllele.getAllele());
                    }
                }
            }

            // Add a single allele that has the combined probabilities of all alleles not in the samples and profiles
            Double otherFrequency = 0.0;
            for (final String allele : hypothesis.getPopulationStatistics().getAlleles(locusName)) {
                if (!allele.endsWith("-other") && !alleleCollection.contains(allele)) {
                    otherFrequency += hypothesis.getPopulationStatistics().getProbability(locusName, allele);
                }
            }

            // Do not add the combined allele if its frequency is not a number, infinity or zero
            if (!otherFrequency.isInfinite() && !otherFrequency.isNaN() && otherFrequency > 0) {
                alleleCollection.add(locusName + "-other");
                hypothesis.getPopulationStatistics().addCompoundStatistic(locusName, locusName + "-other", new BigDecimal(otherFrequency));
            }

            final String[] alleles = alleleCollection.toArray(new String[0]);
            Arrays.sort(alleles);
            for (int allele1Idx = 0; allele1Idx < alleles.length; allele1Idx++) {
                for (int allele2Idx = allele1Idx; allele2Idx < alleles.length; allele2Idx++) {
                    final String name = allele1Idx + "." + allele2Idx;
                    if (!possibleAlleleCombinations.containsKey(name)) {
                        final Locus newLocus = new Locus(locusName);
                        newLocus.addAllele(new Allele(alleles[allele1Idx]));
                        newLocus.addAllele(new Allele(alleles[allele2Idx]));
                        possibleAlleleCombinations.put(name, newLocus);
                    }
                }
            }

            LOG.debug("Possible Allele Combinations: {}", possibleAlleleCombinations);
            for (int idx = 0; idx < possibleAlleleCombinations.size(); idx++) {
                final PermutationIterator permutationIterator = PermutationIteratorFactory.getPermutationIterator(hypothesis, possibleAlleleCombinations.values(), idx);
                LocusProbabilityJob job;
                if (ApplicationSettings.isValidationMode()) {
                    job = new LocusProbabilityJobValidation(locusName, permutationIterator, activeReplicates, hypothesis, progress);
                } else {
                    job = new LocusProbabilityJob(locusName, permutationIterator, activeReplicates, hypothesis, progress);
                }
                retval.add(job);
                if (service != null) {
                    service.submit(job);
                }
            }
        } else {
            LOG.debug("Locus {} under {} has no unknowns.", locusName, hypothesis.getId());
            LocusProbabilityJob job;
            if (ApplicationSettings.isValidationMode()) {
                job = new LocusProbabilityJobValidation(locusName, null, activeReplicates, hypothesis, progress);
            } else {
                job = new LocusProbabilityJob(locusName, null, activeReplicates, hypothesis, progress);
            }
            retval.add(job);
            if (service != null) {
                service.submit(job);
            }
        }
        return retval;
    }
}
