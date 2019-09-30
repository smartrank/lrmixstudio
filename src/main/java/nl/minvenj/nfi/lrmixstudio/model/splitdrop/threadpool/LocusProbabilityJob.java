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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.minvenj.nfi.lrmixstudio.domain.Allele;
import nl.minvenj.nfi.lrmixstudio.domain.Contributor;
import nl.minvenj.nfi.lrmixstudio.domain.Hypothesis;
import nl.minvenj.nfi.lrmixstudio.domain.Locus;
import nl.minvenj.nfi.lrmixstudio.domain.Relatedness.Relation;
import nl.minvenj.nfi.lrmixstudio.domain.Sample;
import nl.minvenj.nfi.lrmixstudio.model.AnalysisProgressListener;
import nl.minvenj.nfi.lrmixstudio.model.DefaultAnalysisProgressListenerImpl;
import nl.minvenj.nfi.lrmixstudio.model.splitdrop.Permutation;
import nl.minvenj.nfi.lrmixstudio.model.splitdrop.PermutationIterator;
import nl.minvenj.nfi.lrmixstudio.model.splitdrop.threadpool.genotype.GenotypeProbabilityCalculator;
import nl.minvenj.nfi.lrmixstudio.model.splitdrop.threadpool.genotype.GenotypeProbabilityCalculatorFactory;

/**
 * This class performs probability calculations at a single locus for a single
 * hypothesis.
 */
public class LocusProbabilityJob implements Callable<LocusProbability> {

    private static final Logger LOG = LoggerFactory.getLogger(LocusProbabilityJob.class);
    protected final Hypothesis hypothesis;
    protected final LocusProbability _locusProbability;
    protected final PermutationIterator _permutationIterator;
    protected final Collection<Locus> replicateLoci;
    protected final AnalysisProgressListener progress;
    protected final String locusName;
    // Set A is the drop-out set. These alleles are present in the reference sample, but not the replicate
    protected final Allele[] droppedOutA = new Allele[Allele.getRegisteredAlleleCount()];
    protected int droppedOutACount = 0;
    // This array holds the product of all dropout probabilitities for all alleles that are present in one or more profile
    protected final double[] presentBProbabilities = new double[Allele.getRegisteredAlleleCount()];
    // Set C is the set of of alleles that are present in the replicate, but not the reference sample
    protected final Allele[] droppedInC = new Allele[Allele.getRegisteredAlleleCount()];
    protected int droppedInCCount = 0;

    protected int presentMinId;
    protected int presentMaxId;

    // This array contains all alleles for all contributors augmented with the alleles for the current permutation
    protected final Allele[] allAlleles = new Allele[Allele.getRegisteredAlleleCount() * 2];
    protected int allAlleleCount = 0;

// Cache for locus results. This to be able to skip recalculation for identical replicate loci
    protected final HashMap<Locus, Double> locusProbabilities;

    protected double dropOutProbability;
    protected double dropOutProbabilityHomozygote;
    protected double presentOnceProbability;
    protected double presentOnceProbabilityHomozygote;
    protected double presentMultipleProbability;
    protected double presentMultipleProbabilityHomozygote;
    protected double oneMinusTheta;
    protected double denominator;

    protected int totalAlleleCount;
    protected final int[] _alleleCounts = new int[Allele.getRegisteredAlleleCount()];
    protected int[] _localAlleleCounts = new int[Allele.getRegisteredAlleleCount()];
    protected int currentAlleleCount;
    private final GenotypeProbabilityCalculator _relatedGenotypeCalculator;
    private final GenotypeProbabilityCalculator _unrelatedGenotypeCalculator;

    LocusProbabilityJob(String locusName, PermutationIterator permutationIterator, Collection<Sample> replicates, Hypothesis hypothesis) {
        this(locusName, permutationIterator, replicates, hypothesis, new DefaultAnalysisProgressListenerImpl());
    }

    LocusProbabilityJob(String locusName, PermutationIterator permutationIterator, Collection<Sample> replicates, Hypothesis hypothesis, AnalysisProgressListener progress) {
        if (locusName == null) {
            throw new IllegalArgumentException("No locus name specified!");
        }

        if (replicates == null || replicates.isEmpty()) {
            throw new IllegalArgumentException("No replicates specified!");
        }

        this.locusProbabilities = new HashMap<>(replicates.size());
        _permutationIterator = permutationIterator;
        this.replicateLoci = new ArrayList<>();
        for (Sample replicate : replicates) {
            Locus locus = replicate.getLocus(locusName);
            if (locus == null) {
                locus = new Locus(locusName);
                locus.setSample(replicate);
            }
            replicateLoci.add(locus);
        }
        this.hypothesis = hypothesis;
        this.progress = progress;
        this.locusName = locusName;
        _locusProbability = new LocusProbability(hypothesis, locusName);

        dropOutProbability = hypothesis.getUnknownDropoutProbability();
        dropOutProbabilityHomozygote = dropOutProbability * dropOutProbability;
        presentOnceProbability = 1 - dropOutProbability;
        presentOnceProbabilityHomozygote = 1 - dropOutProbabilityHomozygote;
        presentMultipleProbability = dropOutProbability;
        presentMultipleProbabilityHomozygote = dropOutProbabilityHomozygote;

        oneMinusTheta = 1.0 - hypothesis.getThetaCorrection();

        _relatedGenotypeCalculator = GenotypeProbabilityCalculatorFactory.getRelatedGenotypeProbabilityCalculator(hypothesis);
        _unrelatedGenotypeCalculator = GenotypeProbabilityCalculatorFactory.getUnrelatedGenotypeProbabilityCalculator(hypothesis);

        // Build allele count table
        for (Contributor contributor : hypothesis.getContributors()) {
            Locus locus = contributor.getSample().getLocus(locusName);

            if (locus == null) {
                throw new IllegalArgumentException("Input error: profile " + contributor.getSample().getId() + " does not contain locus " + locusName);
            }

            boolean skipHomozygotes = false;
            for (Allele allele : locus.getAlleles()) {
                _alleleCounts[allele.getId()]++;
                totalAlleleCount++;
                if (!skipHomozygotes) {
                    allAlleles[allAlleleCount++] = allele;
                    skipHomozygotes = allele.isHomozygote();
                }
            }
        }

        for (Contributor nonContributor : hypothesis.getNonContributors()) {
            Locus locus = nonContributor.getSample().getLocus(locusName);
            if (locus == null) {
                throw new IllegalArgumentException("Input error: profile " + nonContributor.getSample().getId() + " does not contain locus " + locusName);
            }
            for (Allele allele : locus.getAlleles()) {
                _alleleCounts[allele.getId()]++;
                totalAlleleCount++;
            }
        }

        denominator = 1;
        if (hypothesis.getThetaCorrection() > 0) {
            for (int i = totalAlleleCount + (hypothesis.getRelatedness().getRelation() == Relation.NONE ? 0 : 2); i < totalAlleleCount + hypothesis.getUnknownCount() * 2; i++) {
                denominator *= (1 + (i - 1) * hypothesis.getThetaCorrection());
            }
        }

        if (progress != null) {
            progress.locusStarted(hypothesis, locusName, (permutationIterator == null ? 1 : permutationIterator.size()) * replicateLoci.size());
        }

    }

    public LocusProbability getProbability() {
        return _locusProbability;
    }

    /**
     * Converts an array of Locus objects into a string suitable for logging.
     *
     * @param loci An array of Locus objects
     * @return A string that contains the contents of the loci
     */
    public String toLogString(Locus[] loci) {
        String retval = "";
        for (Locus locus : loci) {
            retval += locus.getAlleles() + "; ";
        }
        return "[" + retval.substring(0, retval.length() - 2) + "]";
    }

    @Override
    public LocusProbability call() throws Exception {
        LOG.debug("Started {}", locusName);
        try {
            if (_permutationIterator == null) {
                _locusProbability.setValue(calculateSingleLocusProbability());
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

    public double calculateSingleLocusProbability() {
        return calculateReplicateProbability(new Locus[]{});
    }

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
            Double precalculated = locusProbabilities.get(replicateLocus);
            if (precalculated == null) {
                precalculated = calculateReplicateProbability(replicateLocus);
                locusProbabilities.put(replicateLocus, precalculated);
            }
            replicateProbability *= precalculated;
        }
        return replicateProbability;
    }

    public double calculateReplicateProbability(Locus replicateLocus) {
        double returnValue = 1.0;

        boolean[] presentFlags = new boolean[Allele.getRegisteredAlleleCount()];

        classifyAlleles(replicateLocus, presentFlags);

        for (int idx = 0; idx < droppedOutACount; idx++) {
            Allele a = droppedOutA[idx];
            if (a.getLocus().getSample() == null) {
                returnValue *= a.isHomozygote() ? dropOutProbabilityHomozygote : dropOutProbability;
            } else {
                returnValue *= hypothesis.getContributor(a).getDropOutProbability(a.isHomozygote());
            }
        }

        for (int idx = presentMinId; idx <= presentMaxId; idx++) {
            if (presentFlags[idx]) {
                returnValue *= 1 - presentBProbabilities[idx];
            }
        }

        if (droppedInCCount == 0) {
            returnValue *= 1 - hypothesis.getDropInProbability();
        } else {
            for (int idx = 0; idx < droppedInCCount; idx++) {
                Allele a = droppedInC[idx];
                returnValue *= hypothesis.getDropInProbability() * hypothesis.getPopulationStatistics().getProbability(replicateLocus, a);
            }
        }

        return returnValue;
    }

    protected void classifyAlleles(Locus replicateLocus, boolean[] presentFlags) {
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
            presentBProbabilities[allele.getId()] = 1.0;
            presentFlags[allele.getId()] = false;
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

                presentFlags[allele.getId()] = true;

                // Update the product of dropout probabilities for this allele
                if (allele.getLocus().getSample() == null) {
                    presentBProbabilities[allele.getId()] *= allele.isHomozygote() ? presentMultipleProbabilityHomozygote : presentMultipleProbability;
                } else {
                    presentBProbabilities[allele.getId()] *= hypothesis.getContributor(allele).getPresentMultipleProbability(allele.isHomozygote());
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

    protected double calculateGenotypeProbability(Locus[] genotypeSetForUnknowns) {
        double genotypeProbability = 1;
        System.arraycopy(_alleleCounts, 0, _localAlleleCounts, 0, _alleleCounts.length);

        // Calculate the genotype probability for the first unknown contributor
        genotypeProbability = _relatedGenotypeCalculator.calculate(_localAlleleCounts, genotypeSetForUnknowns[0]);
        for (int idx = 1; idx < genotypeSetForUnknowns.length; idx++) {
            genotypeProbability *= _unrelatedGenotypeCalculator.calculate(_localAlleleCounts, genotypeSetForUnknowns[idx]);
        }

        return genotypeProbability / denominator;
    }
}
