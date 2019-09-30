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
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.minvenj.nfi.lrmixstudio.domain.Hypothesis;
import nl.minvenj.nfi.lrmixstudio.domain.LikelihoodRatio;
import nl.minvenj.nfi.lrmixstudio.domain.LocusProbabilities;
import nl.minvenj.nfi.lrmixstudio.domain.Sample;
import nl.minvenj.nfi.lrmixstudio.gui.ApplicationStateChangeListener;
import nl.minvenj.nfi.lrmixstudio.gui.SessionData;
import nl.minvenj.nfi.lrmixstudio.model.AnalysisReport;
import nl.minvenj.nfi.lrmixstudio.model.DropoutEstimation;

public class DropoutEstimator extends Thread {

    private final SessionData session;
    private final Collection<Sample> personsOfInterest;
    private final BigDecimal dropin;
    private static final Logger LOG = LoggerFactory.getLogger(DropoutEstimator.class);
    private final DropoutEstimationProgressListener progress;
    private DropoutEstimation estimate;
    private final Integer iterations;
    private final boolean varyDefenseUnknowns;
    private final boolean varyProsecutionUnknowns;
    private final int observedAlleleCount;
    private final BigDecimal dropoutFrom;
    private final BigDecimal dropoutTo;
    private final BigDecimal dropoutSteps;

    public DropoutEstimator(final SessionData session, final ArrayList<Sample> personsOfInterest, final BigDecimal dropoutFrom, final BigDecimal dropoutTo, final BigDecimal dropoutSteps, final BigDecimal dropin, final DropoutEstimationProgressListener progress, final Integer iterations, final boolean varyDefenseUnknowns, final boolean varyProsecutionUnknowns) {
        LOG.trace("DropoutEstimator");
        this.observedAlleleCount = session.getObservedAlleleCount();
        this.session = session;
        this.personsOfInterest = personsOfInterest;
        this.dropoutFrom = dropoutFrom;
        this.dropoutTo = dropoutTo;
        this.dropoutSteps = dropoutSteps;
        this.dropin = dropin;
        this.progress = progress;
        this.iterations = iterations;
        this.varyDefenseUnknowns = varyDefenseUnknowns;
        this.varyProsecutionUnknowns = varyProsecutionUnknowns;
    }

    /**
     * Performs a dropout estimation for the given hypothesis.
     *
     * @param dropoutEstimation The DropoutEstimation class that will be used to
     * hold the result.
     * @param observedAlleleCount The observed allele count in the replicates
     * @param hypothesis The current hypothesis
     */
    private void estimate(final DropoutEstimation dropoutEstimation, final Hypothesis hypothesis, final boolean varyUnknowns) throws InterruptedException {
        LOG.debug("Starting dropout estimation for {}", hypothesis.getId());
        LOG.debug("Observed alleles: {}", observedAlleleCount);

        // This arraylist is used to store all dropouts at which a monte carlo simulation yields a number of surviving alleles that matches the observed allele count
        final ArrayList<Double> succesfulDropouts = new ArrayList<>();

        // Work out the step size
        BigDecimal dropoutStepsize = dropoutTo.subtract(dropoutFrom).divide(dropoutSteps, 2, RoundingMode.HALF_UP);

        // The minimum stepsize is 0.01
        if (dropoutStepsize.compareTo(new BigDecimal("0.01")) < 0) {
            dropoutStepsize = new BigDecimal("0.01");
        }

        final int steps = dropoutTo.subtract(dropoutFrom).divide(dropoutStepsize, RoundingMode.UP).intValue();

        final int[][] results = new int[iterations][steps + 1];
        progress.hypothesisStarted(hypothesis);

        final ExecutorService service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        final ArrayList<Future<ArrayList<Double>>> futures = new ArrayList<>();

        // Perform the dropout estimation using the configured number of iterations
        for (int iteration = 0; iteration < iterations; iteration++) {
            futures.add(service.submit((Callable) new DropoutEstimationJob(results[iteration], session, hypothesis, dropoutFrom, dropoutTo, dropoutStepsize, dropin, personsOfInterest, varyUnknowns)));
        }

        service.shutdown();

        for (int iteration = 0; iteration < iterations; iteration++) {
            try {
                final Future<ArrayList<Double>> future = futures.remove(0);
                final ArrayList<Double> iterationResults = future.get();
                if (!iterationResults.isEmpty()) {
                    LOG.debug("Iteration {} resulted in {} Succesful dropouts: {}", iteration, iterationResults.size(), iterationResults);
                    succesfulDropouts.addAll(iterationResults);
                }
                progress.setIteration(iteration, hypothesis, iterationResults, iterations);
            } catch (ExecutionException | InterruptedException ex) {
                service.shutdownNow();
                service.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
                progress.analysisFinished(ex);
                throw new IllegalArgumentException(ex);
            }
        }

        Collections.sort(succesfulDropouts);
        LOG.debug("{} Succesful dropouts: {}", succesfulDropouts.size(), succesfulDropouts);

//        try {
//            FileOutputStream fos = new FileOutputStream("dropout_" + hypothesis.getId() + ".csv");
//
//            fos.write("DropOut".getBytes());
//            for (int column = 0; column < results.length; column++) {
//                fos.write((",Iteration " + (column + 1)).getBytes());
//            }
//            fos.write("\n".getBytes());
//
//            BigDecimal curDropout = dropoutFrom.plus();
//            for (int dropoutIndex = 0; dropoutIndex < results[0].length; dropoutIndex++) {
//                fos.write((curDropout.toPlainString()).getBytes());
//                for (int iteration = 0; iteration < results.length; iteration++) {
//                    fos.write(("," + results[iteration][dropoutIndex]).getBytes());
//                }
//                curDropout.add(dropoutSteps);
//                fos.write("\n".getBytes());
//            }
//            fos.close();
//        } catch (Exception e) {
//            LOG.error("Error during writing of 'dropout_" + hypothesis.getId() + ".csv'", e);
//        }

        // Store the minimum and maximum
        if (!succesfulDropouts.isEmpty()) {
            LOG.debug("Sorting collection");
            Collections.sort(succesfulDropouts);

            // Determine frequencies
            // Obtain the 5% and 95% percentiles.
            final Percentile percentile = new Percentile();
            final double[] dropouts = new double[succesfulDropouts.size()];
            for (int idx = 0; idx < dropouts.length; idx++) {
                dropouts[idx] = succesfulDropouts.get(idx);
            }
            percentile.setData(dropouts);

            final double min = percentile.evaluate(5);
            LOG.debug("5% Percentile = {}", min);
            final double max = percentile.evaluate(95);
            LOG.debug("95% Percentile = {}", max);

            dropoutEstimation.setValues(hypothesis.getId(), new BigDecimal(min), new BigDecimal(max));
            progress.hypothesisFinished(hypothesis, new LocusProbabilities());
        } else {
            final IllegalArgumentException ex = new IllegalArgumentException("Dropout estimation resulted in no matching attempts for " + hypothesis.getId());
            throw ex;
        }

    }

    public DropoutEstimation estimate() throws InterruptedException {
        final DropoutEstimation dropoutEstimation = new DropoutEstimation();
        dropoutEstimation.setAlleleCount(observedAlleleCount);
        dropoutEstimation.setReplicateCount(session.getActiveReplicates().size());
        dropoutEstimation.setIterations(iterations);
        progress.analysisStarted();
        try {
            final long start = System.currentTimeMillis();
            estimate(dropoutEstimation, session.getDefense(), varyDefenseUnknowns);
            estimate(dropoutEstimation, session.getProsecution(), varyProsecutionUnknowns);
            session.getCurrentReport().addProcessingTime(System.currentTimeMillis() - start);
            progress.analysisFinished(new LikelihoodRatio());
        }
        catch (final Throwable t) {
            progress.analysisFinished(t);
        }
        return dropoutEstimation;
    }

    @Override
    public void run() {
        try {
            session.setApplicationState(ApplicationStateChangeListener.APP_STATE.DROPOUT_ESTIMATION_RUNNING);
            estimate = estimate();
            session.getCurrentReport().getSensitivityAnalysisResults().setDropoutEstimation(estimate);
            for (final AnalysisReport report : session.getEquivalentReportsForDropoutEstimate(session.getCurrentReport())) {
                if (report.getSensitivityAnalysisResults().getDropoutEstimation() == null) {
                    report.getSensitivityAnalysisResults().setDropoutEstimation(estimate);
                }
            }
        } catch (final InterruptedException ex) {
            LOG.info("Dropout estimation interrupted");
            session.setStatusMessage("Dropout estimation interrupted");
        }
        catch (final Throwable ex) {
            if (ex.getCause() instanceof InterruptedException) {
                LOG.info("Dropout estimation interrupted");
                session.setStatusMessage("Dropout estimation interrupted");
            } else {
                LOG.error("Error running dropout estimation", ex);
                session.setStatusMessage("Error running dropout estimation");
                session.setErrorMessage(ex.getMessage() == null ? ex.getClass().getName() : ex.getMessage());
            }
        } finally {
            session.setApplicationState(ApplicationStateChangeListener.APP_STATE.READY_FOR_ANALYSIS);
        }
    }

    /**
     * @return the estimate
     */
    public DropoutEstimation getEstimate() {
        return estimate;
    }
}
