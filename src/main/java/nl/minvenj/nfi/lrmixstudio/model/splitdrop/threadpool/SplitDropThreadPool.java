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

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.minvenj.nfi.lrmixstudio.domain.Hypothesis;
import nl.minvenj.nfi.lrmixstudio.domain.LikelihoodRatio;
import nl.minvenj.nfi.lrmixstudio.domain.LocusProbabilities;
import nl.minvenj.nfi.lrmixstudio.model.AnalysisProgressListener;
import nl.minvenj.nfi.lrmixstudio.model.ConfigurationData;
import nl.minvenj.nfi.lrmixstudio.model.LRMathModel;

public final class SplitDropThreadPool implements LRMathModel, AnalysisProgressListener {

    private static final Logger LOG = LoggerFactory.getLogger(SplitDropThreadPool.class);
    private final ArrayList<AnalysisProgressListener> progress = new ArrayList();
    private ExecutorService service;
    private final Map<String, LocusProbabilities> probabilities;
    private final AtomicInteger locusCount;
    private LikelihoodRatio lr;
    private final ArrayList<Future<LocusProbability>> futures = new ArrayList<>();
    private Thread _watchDog;
    private boolean _interrupted;

    private class WatchdogThread extends Thread {

        Collection<Future<LocusProbability>> _futures;

        public WatchdogThread(final Collection<Future<LocusProbability>> futures) {
            _futures = futures;
        }

        @Override
        public void run() {
            try {
                for (final Future<LocusProbability> f : futures) {
                    final LocusProbability prob = f.get();
                    LocusProbabilities probs = probabilities.get(prob.getHypothesis().getId());
                    if (probs == null) {
                        probs = new LocusProbabilities();
                        probabilities.put(prob.getHypothesis().getId(), probs);
                    }
                    Double current = probs.getLocusProbability(prob.getLocusName());
                    LOG.debug("{}.{} Probability = {}", prob.getHypothesis().getId(), prob.getLocusName(), prob.getValue());
                    if (current == null) {
                        current = prob.getValue();
                    } else {
                        current += prob.getValue();
                    }
                    probs.addLocusProbability(prob.getLocusName(), current);
                }
                lr = new LikelihoodRatio();
                lr.add(probabilities.get("Prosecution"), probabilities.get("Defense"));
                analysisFinished(lr);
            } catch (InterruptedException | ExecutionException ex) {
                analysisFinished(ex);
            }
            catch (final Throwable t) {
                analysisFinished(new UnsupportedOperationException(t));
            }
        }
    }

    public SplitDropThreadPool() {
        this(null);
    }

    public SplitDropThreadPool(final AnalysisProgressListener progressListener) {
        if (progressListener != null) {
            addProgressListener(progressListener);
        }
        probabilities = new HashMap<>();
        locusCount = new AtomicInteger(0);
    }

    @Override
    public void startAnalysis(final ConfigurationData config) {
        System.gc();
        LOG.debug("Starting analysis with {} threads", config.getThreadCount());
        _interrupted = false;
        service = Executors.newFixedThreadPool(config.getThreadCount());
        addProgressListener((AnalysisProgressListener) config.getCurrentReport());
        probabilities.put("Defense", new LocusProbabilities());
        probabilities.put("Prosecution", new LocusProbabilities());
        analysisStarted();
        final ArrayList<LocusProbabilityJob> jobs = new ArrayList<>();
        for (final String locusName : config.getEnabledLoci()) {
            if (config.getProsecution() != null) {
                jobs.addAll(LocusProbabilityJobGenerator.generate(locusName, config.getActiveReplicates(), config.getProsecution(), this));
            }
            if (config.getDefense() != null) {
                jobs.addAll(LocusProbabilityJobGenerator.generate(locusName, config.getActiveReplicates(), config.getDefense(), this));
            }
        }

        for (final LocusProbabilityJob job : jobs) {
            futures.add(service.submit(job));
        }
        service.shutdown();
        _watchDog = new WatchdogThread(futures);
        _watchDog.start();
    }

    @Override
    public LikelihoodRatio doAnalysis(final ConfigurationData config) throws InterruptedException {
        startAnalysis(config);
        _watchDog.join();
        if (_interrupted) {
            throw new InterruptedException();
        }
        return lr;
    }

    @Override
    public void doSensitivityAnalysis(final ConfigurationData config) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void doPerformanceAnalysis(final ConfigurationData config) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addProgressListener(final AnalysisProgressListener listener) {
        if (listener != null) {
            progress.add(listener);
        }
    }

    @Override
    public void analysisStarted() {
        for (final AnalysisProgressListener listener : progress) {
            listener.analysisStarted();
        }
    }

    @Override
    public void analysisFinished(final LikelihoodRatio lr) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                for (final AnalysisProgressListener listener : progress) {
                    listener.analysisFinished(lr);
                }
            }
        });
    }

    @Override
    public void analysisFinished(final Throwable e) {
        if (!((e instanceof InterruptedException) || (e.getCause() instanceof InterruptedException))) {
            LOG.error(e.getMessage(), e);
        } else
            _interrupted = true;
        for (final AnalysisProgressListener listener : progress) {
            listener.analysisFinished(e);
        }
    }

    @Override
    public void hypothesisStarted(final Hypothesis hypothesis) {
        for (final AnalysisProgressListener listener : progress) {
            listener.hypothesisStarted(hypothesis);
        }
    }

    @Override
    public void hypothesisFinished(final Hypothesis hypothesis, final LocusProbabilities probabilities) {
        for (final AnalysisProgressListener listener : progress) {
            listener.hypothesisFinished(hypothesis, probabilities);
        }
    }

    @Override
    public synchronized void locusStarted(final Hypothesis hypothesis, final String locusName, final long jobsize) {
        locusCount.incrementAndGet();
        LOG.debug("Job {} Hypothesis {} Locus {} started with jobsize {}", locusCount, hypothesis.getId(), locusName, jobsize);
        for (final AnalysisProgressListener listener : progress) {
            listener.locusStarted(hypothesis, locusName, jobsize);
        }
    }

    @Override
    public synchronized void locusFinished(final Hypothesis hypothesis, final String locusName, final Double locusProbability) {
        LOG.debug("Hypothesis {} Locus {} finished with result {}", hypothesis.getId(), locusName, locusProbability);
        LOG.debug("{} jobs still active", locusCount);
        for (final AnalysisProgressListener listener : progress) {
            listener.locusFinished(hypothesis, locusName, locusProbability);
        }
    }

    @Override
    public String getId() {
        return "SplitDrop Threadpool Edition";
    }

    @Override
    public void interrupt() {
        if (service != null) {
            service.shutdownNow();
        }
        if (_watchDog != null) {
            _watchDog.interrupt();
        }
    }

    @Override
    public LikelihoodRatio getLikelihoodRatio() throws InterruptedException, TimeoutException {
        if (service == null || !service.isShutdown()) {
            throw new RuntimeException("Analysis not running!");
        }
        return lr;
    }
}
