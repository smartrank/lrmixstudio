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

import java.util.Collection;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.minvenj.nfi.lrmixstudio.domain.Allele;
import nl.minvenj.nfi.lrmixstudio.domain.Hypothesis;
import nl.minvenj.nfi.lrmixstudio.domain.Locus;
import nl.minvenj.nfi.lrmixstudio.domain.Sample;
import nl.minvenj.nfi.lrmixstudio.model.AnalysisProgressListener;
import nl.minvenj.nfi.lrmixstudio.model.DefaultAnalysisProgressListenerImpl;
import nl.minvenj.nfi.lrmixstudio.model.splitdrop.Permutation;
import nl.minvenj.nfi.lrmixstudio.model.splitdrop.PermutationIterator;

/**
 * This class performs probability calculations at a single locus for a single
 * hypothesis.
 */
public class LocusProbabilityJobValidation extends LocusProbabilityJob {

    private static final Logger LOG = new ValidationLogger(LoggerFactory.getLogger(LocusProbabilityJobValidation.class));

    LocusProbabilityJobValidation(String locusName, PermutationIterator permutationIterator, Collection<Sample> replicates, Hypothesis hypothesis) {
        super(locusName, permutationIterator, replicates, hypothesis, new DefaultAnalysisProgressListenerImpl());
    }

    LocusProbabilityJobValidation(String locusName, PermutationIterator permutationIterator, Collection<Sample> replicates, Hypothesis hypothesis, AnalysisProgressListener progress) {
        super(locusName, permutationIterator, replicates, hypothesis, progress);
    }

    @Override
    public LocusProbability call() throws Exception {
        try {
            if (_permutationIterator == null) {
                _locusProbability.setValue(calculateSingleLocusProbability());
                LOG.info("{} Locus {} locusProbability = {}", hypothesis.getId(), locusName, _locusProbability.getValue());
                LOG.info("");
            } else {
                Permutation permutation;
                while ((permutation = _permutationIterator.next()) != null) {
                    if (Thread.interrupted()) {
                        throw new InterruptedException();
                    }

                    double replicateProbability = calculateReplicateProbability(permutation.getLoci());
                    double genotypeProbability = calculateGenotypeProbability(permutation.getLoci());
                    double prob = genotypeProbability * replicateProbability;
                    _locusProbability.addValue(permutation.getPermutationFactor() * prob);
                    LOG.info("{} Locus {} Permutation {} ({}) pRep = {} pGen = {} locusProbability = {}", hypothesis.getId(), locusName, toLogString(permutation.getLoci()), permutation.getPermutationFactor(), replicateProbability, genotypeProbability, prob);
                    LOG.info("");
                }
            }
        } catch (NoSuchElementException e) {
            // Can happen if multiple thread share a permutation iterator object. Nothing to worry about.
            LOG.debug("No Such Element", e);
        } catch (InterruptedException ie) {
            LOG.debug("Calculations for locus {} were interrupted!", locusName);
            progress.analysisFinished(ie);
            throw ie;
        } catch (Exception e) {
            LOG.error("Error in Locus Probability calculation for {}", locusName, e);
            progress.analysisFinished(e);
            throw e;
        }

        LOG.debug("Hypothesis {} Locus {} Done", hypothesis.getId(), locusName);
        progress.locusFinished(hypothesis, locusName, _locusProbability.getValue());

        return _locusProbability;
    }

    private String getPermutationAllelesLogString() {
        String retval = "[";
        for (int idx = 0; idx < currentAlleleCount; idx++) {
            if (idx != 0) {
                retval += ", ";
            }
            retval += allAlleles[idx].getAllele();
        }
        retval += "]";
        return retval;
    }

    @Override
    public double calculateReplicateProbability(Locus[] unknowns) {
        locusProbabilities.clear();
        double replicateProbability = 1;

        currentAlleleCount = allAlleleCount;
        if (unknowns != null) {
            for (Locus unknown : unknowns) {
                boolean skipHomozygotes = false;
                for (Allele allele : unknown.getAlleles()) {
                    if (!skipHomozygotes) {
                        allAlleles[currentAlleleCount++] = allele;
                        skipHomozygotes = allele.isHomozygote();
                    }
                }
            }
        }

        for (Locus replicateLocus : replicateLoci) {
            replicateProbability *= calculateReplicateProbability(replicateLocus);
        }
        return replicateProbability;
    }

    @Override
    public double calculateReplicateProbability(Locus replicateLocus) {
        double returnValue = 1.0;

        String formula = "";
        String[] presentFactors = new String[Allele.getRegisteredAlleleCount()];
        for (int idx = 0; idx < presentFactors.length; idx++) {
            presentFactors[idx] = "";
        }

        classifyAlleles(replicateLocus, presentFactors);

        for (int idx = 0; idx < droppedOutACount; idx++) {
            Allele a = droppedOutA[idx];
            if (a.getLocus().getSample() == null) {
                if (a.isHomozygote()) {
                    formula += " d_unknown^2";
                    returnValue *= dropOutProbabilityHomozygote;
                } else {
                    formula += " d_unknown";
                    returnValue *= dropOutProbability;
                }
            } else {
                if (a.isHomozygote()) {
                    formula += " d_" + a.getLocus().getSampleId() + "^2";
                    returnValue *= hypothesis.getContributor(a).getDropOutProbability(a.isHomozygote());
                } else {
                    formula += " d_" + a.getLocus().getSampleId();
                    returnValue *= hypothesis.getContributor(a).getDropOutProbability(a.isHomozygote());
                }
            }
        }

        for (int idx = presentMinId; idx <= presentMaxId; idx++) {
            if (!presentFactors[idx].isEmpty()) {
                formula += " (1-" + presentFactors[idx] + ")";
                returnValue *= 1 - presentBProbabilities[idx];
            }
        }

        if (droppedInCCount == 0) {
            formula += " (1-c)";
            returnValue *= 1 - hypothesis.getDropInProbability();
        } else {
            for (int idx = 0; idx < droppedInCCount; idx++) {
                Allele a = droppedInC[idx];
                formula += " cp" + a.getAllele();
                returnValue *= hypothesis.getDropInProbability() * hypothesis.getPopulationStatistics().getProbability(replicateLocus, a);
            }
        }

        LOG.info("{} Locus {} Replicate {} Permutation {} replicate probability formula: {}", hypothesis.getId(), locusName, replicateLocus.getAlleles(), getPermutationAllelesLogString(), formula);

        return returnValue;
    }

    protected void classifyAlleles(Locus replicateLocus, String[] presentFactors) {
        droppedOutACount = 0;
        droppedInCCount = 0;

        presentMinId = presentBProbabilities.length - 1;
        presentMaxId = 0;

        // Assume all alleles have dropped in. We will move the alleles out of this set as appropriate. 
        // Note that alleles from homozygote loci only get added once
        for (Allele allele : replicateLocus.getAlleles()) {
            if (!contains(droppedInC, droppedInCCount, allele)) {
                droppedInC[droppedInCCount++] = allele;
            }
            presentBProbabilities[allele.getId()] = 1;
            if (allele.getId() < presentMinId) {
                presentMinId = allele.getId();
            }
            if (allele.getId() > presentMaxId) {
                presentMaxId = allele.getId();
            }
        }

        for (int alleleIndex = 0; alleleIndex < currentAlleleCount; alleleIndex++) {
            Allele allele = allAlleles[alleleIndex];
            // If this allele is not present in the replicate, it goes into set A
            if (!replicateLocus.getAlleles().contains(allele)) {
                droppedOutA[droppedOutACount++] = allele;
            } else {
                // Remove this allele from the Dropped In set C
                int cIndex = indexOf(droppedInC, droppedInCCount, allele);
                if (cIndex >= 0) {
                    remove(droppedInC, droppedInCCount--, cIndex);
                }

                // Update the product of dropout probabilities for this allele
                if (allele.getLocus().getSample() == null) {
                    presentFactors[allele.getId()] += " d_unknown";
                    presentBProbabilities[allele.getId()] *= allele.isHomozygote() ? presentMultipleProbabilityHomozygote : presentMultipleProbability;
                } else {
                    presentFactors[allele.getId()] += " d_" + allele.getLocus().getSampleId();
                    presentBProbabilities[allele.getId()] *= hypothesis.getContributor(allele).getPresentMultipleProbability(allele.isHomozygote());
                }

                if (allele.isHomozygote()) {
                    presentFactors[allele.getId()] += "^2";
                }
            }
        }
    }

    private Allele remove(Allele[] array, int size, int index) {
        Allele a = array[index];
        array[index] = array[size - 1];
        return a;
    }

    private int indexOf(Allele[] array, int size, Allele allele) {
        for (int idx = 0; idx < size; idx++) {
            if (array[idx].getId() == allele.getId()) {
                return idx;
            }
        }
        return -1;
    }

    private boolean contains(Allele[] array, int size, Allele allele) {
        for (int idx = 0; idx < size; idx++) {
            if (array[idx].getId() == allele.getId()) {
                return true;
            }
        }
        return false;
    }
}
