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
package nl.minvenj.nfi.lrmixstudio.gui.tabs.sensitivity;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.minvenj.nfi.lrmixstudio.domain.Allele;
import nl.minvenj.nfi.lrmixstudio.domain.Contributor;
import nl.minvenj.nfi.lrmixstudio.domain.Hypothesis;
import nl.minvenj.nfi.lrmixstudio.domain.Locus;
import nl.minvenj.nfi.lrmixstudio.domain.PopulationStatistics;
import nl.minvenj.nfi.lrmixstudio.domain.Sample;
import nl.minvenj.nfi.lrmixstudio.gui.SessionData;
import nl.minvenj.nfi.lrmixstudio.model.RandomProfileGenerator;

public class DropoutEstimationJob implements Callable<ArrayList<Double>> {

    private final int[] results;
    private final Hypothesis hypothesis;
    private final Collection<Sample> personsOfInterest;
    private final double dropin;
    private static final Logger LOG = LoggerFactory.getLogger(DropoutEstimationJob.class);
    private final boolean varyUnknowns;
    private final int observedAlleleCount;
    private final Iterable<String> loci;
    private final PopulationStatistics stats;
    private final BigDecimal dropoutFrom;
    private final BigDecimal dropoutTo;
    private final BigDecimal dropoutStepSize;
    private final Collection<String> _enabledLoci;

    public DropoutEstimationJob(int[] results, SessionData session, Hypothesis hypothesis, BigDecimal dropoutFrom, BigDecimal dropoutTo, BigDecimal dropoutStepSize, BigDecimal dropin, Collection<Sample> personsOfInterest, boolean varyUnknowns) {
        synchronized (session) {
            this.results = results;
            this.observedAlleleCount = session.getObservedAlleleCount();
            this.loci = session.getEnabledLoci();
            this.stats = session.getStatistics();
            _enabledLoci = session.getEnabledLoci();
            this.hypothesis = hypothesis;
            this.dropoutFrom = dropoutFrom;
            this.dropoutTo = dropoutTo;
            this.dropoutStepSize = dropoutStepSize;
            this.dropin = dropin.doubleValue();
            this.personsOfInterest = personsOfInterest;
            this.varyUnknowns = varyUnknowns;
        }
    }

    @Override
    public ArrayList<Double> call() throws Exception {
        LOG.debug("DropoutEstimationJob for {}", hypothesis.getId());
        final ArrayList<Contributor> mixture = new ArrayList<>();
        final ArrayList<Contributor> randomDudes = new ArrayList<>();
        final SecureRandom rnd = new SecureRandom(/*SecureRandom.getSeed(4096)*/);
        final RandomProfileGenerator randomDudeGenerator = new RandomProfileGenerator(_enabledLoci, stats, rnd);
        final ArrayList<Double> succesfulDropouts = new ArrayList<>();

        // Simulate a mixture by adding a random profile for each unknown
        for (int randomDude = 0; randomDude < hypothesis.getUnknownCount(); randomDude++) {
            try {
                Contributor randomContributor = new Contributor(randomDudeGenerator.getRandomSample(), hypothesis.getUnknownDropoutProbability());
                randomDudes.add(randomContributor);
                mixture.add(randomContributor);
            } catch (NoSuchAlgorithmException ex) {
                throw new IllegalArgumentException(ex);
            }
        }


        // Add contributors to the mixture
        for (Contributor contributor : hypothesis.getContributors()) {
            mixture.add(new Contributor(contributor));
        }

        // Vary dropout probability for persons of interest and unknowns
        int stepCount = 0;
        BigDecimal curDropout = dropoutFrom;
        while (curDropout.compareTo(dropoutTo) <= 0) {
            double dropout = curDropout.doubleValue();
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }

            ArrayList<String> survivingAlleles = new ArrayList<>();

            // Set the dropout for persons of interest and random dudes
            for (Contributor contributor : mixture) {
                if (personsOfInterest.contains(contributor.getSample()) || (varyUnknowns && randomDudes.contains(contributor))) {
                    LOG.debug("Setting dropout to {} for {}", dropout, contributor);
                    contributor.setDropoutProbability(dropout);
                }
            }

            applyDropout(rnd, mixture, survivingAlleles);

            // Apply dropIn probability
            if (dropin > 0) {
                applyDropin(rnd, randomDudeGenerator, survivingAlleles);
            }

            // Store surviving alleles
            results[stepCount++] = survivingAlleles.size();

            // If surviving alleles count matches the observed count, record the current dropout value
            int simulatedAlleleCount = survivingAlleles.size();
            if (simulatedAlleleCount == observedAlleleCount) {
                LOG.debug("Succesful dropOut {}", dropout);
                succesfulDropouts.add(BigDecimal.valueOf(dropout).round(new MathContext(2, RoundingMode.HALF_UP)).doubleValue());
            }

            curDropout = curDropout.add(dropoutStepSize);
        }
        LOG.debug("{} Succesful dropouts {} for results {}", hypothesis.getId(), succesfulDropouts, results);
        return succesfulDropouts;
    }

    protected void applyDropout(final SecureRandom rnd, ArrayList<Contributor> mixture, ArrayList<String> survivingAlleles) throws InterruptedException {
        // Apply dropout probability to all alleles in the active loci
        for (Contributor contributor : mixture) {
            for (String locusName : loci) {
                Locus locus = contributor.getSample().getLocus(locusName);
                if (locus != null) {
                    for (Allele allele : locus.getAlleles()) {
                        if (rnd.nextDouble() > contributor.getDropoutProbability() && !survivingAlleles.contains(locus.getName() + "." + allele.getAllele())) {
                            survivingAlleles.add(locus.getName() + "." + allele.getAllele());
                            if (Thread.interrupted()) {
                                throw new InterruptedException();
                            }
                        }
                    }
                }
            }
        }
    }

    protected void applyDropin(final SecureRandom rnd, final RandomProfileGenerator randomDudeGenerator, ArrayList<String> survivingAlleles) {
        for (String locusName : loci) {
            if (rnd.nextDouble() < hypothesis.getDropInProbability()) {
                String dropinAllele = locusName + "." + randomDudeGenerator.getRandomAllele(locusName).getAllele();
                if (!survivingAlleles.contains(dropinAllele)) {
                    String droppedIn = locusName + "." + randomDudeGenerator.getRandomAllele(locusName).getAllele();
                    survivingAlleles.add(droppedIn);
                }
            }
        }
    }
}
