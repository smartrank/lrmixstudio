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
package nl.minvenj.nfi.lrmixstudio.gui.tabs.noncontributor;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.RejectedExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.minvenj.nfi.lrmixstudio.domain.Contributor;
import nl.minvenj.nfi.lrmixstudio.domain.LikelihoodRatio;
import nl.minvenj.nfi.lrmixstudio.domain.Locus;
import nl.minvenj.nfi.lrmixstudio.domain.Ratio;
import nl.minvenj.nfi.lrmixstudio.domain.Sample;
import nl.minvenj.nfi.lrmixstudio.gui.SessionData;
import nl.minvenj.nfi.lrmixstudio.model.AnalysisReportImpl;
import nl.minvenj.nfi.lrmixstudio.model.LRMathModel;
import nl.minvenj.nfi.lrmixstudio.model.LRMathModelFactory;
import nl.minvenj.nfi.lrmixstudio.model.NonContributorTestResults;
import nl.minvenj.nfi.lrmixstudio.model.RandomProfileGenerator;

/**
 *
 * @author dejong
 */
class NonContributorTest extends Thread {

    private static final Logger LOG = LoggerFactory.getLogger(NonContributorTest.class);
    private final SessionData _session;
    private final NonContributorTestProgressListener _progress;
    private final Integer _iterations;
    private final Sample _personOfInterest;
    private LRMathModel _model = null;
    private final HashMap<Integer, Double> _cachedProbabilities;

    public NonContributorTest(SessionData session, Sample personOfInterest, Integer iterations, NonContributorTestProgressListener performanceTestProgressListener) {
        _session = session;
        _progress = performanceTestProgressListener;
        _iterations = iterations;
        _personOfInterest = personOfInterest;
        _cachedProbabilities = new HashMap<>();
    }

    @Override
    public void interrupt() {
        if (_model != null) {
            _model.interrupt();
        }
        super.interrupt();
    }

    @Override
    public void run() {
        String description = _personOfInterest.getId();
        long start = System.currentTimeMillis();
        LikelihoodRatio originalLR = _session.getCurrentReport().getLikelihoodRatio();
        ArrayList<Double> results = new ArrayList<>();
        long iteration = 0;
        try {
            _progress.analysisStarted(_iterations);
            if (originalLR == null) {
                _model = LRMathModelFactory.getMathematicalModel(_session.getMathematicalModelName());
                originalLR = _model.doAnalysis(_session);
            }

            RandomProfileGenerator randomProfileGenerator = new RandomProfileGenerator(_session.getEnabledLoci(), _session.getStatistics());
            for (iteration = 0; iteration < _iterations; iteration++) {
                SessionData randomSession = new SessionData(_session);
                Sample randomDude = randomProfileGenerator.getRandomSample();
                try {
                    if (randomSession.getDefense().isContributor(_personOfInterest)) {
                        Contributor contributor = randomSession.getDefense().getContributor(_personOfInterest);
                        contributor.setSample(randomDude);
                    } else {
                        // If the person of interest is not a contributor to the defense, there is no need to re-evaluate the defense hypothesis
                        randomSession.setDefense(null);
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage());
                }
                try {
                    if (randomSession.getProsecution().isContributor(_personOfInterest)) {
                        Contributor contributor = randomSession.getProsecution().getContributor(_personOfInterest);
                        contributor.setSample(randomDude);
                    } else // If the person of interest is not a contributor to the prosecution, there is no need to re-evaluate the prosecution hypothesis
                    {
                        randomSession.setProsecution(null);
                    }
                } catch (Exception e) {
                    LOG.error("Error setting prosecution hypothesis", e);
                }

                int cacheMisses = 0;

                // Disable loci for which we already have a result for an identical allele genotype evaluated earlier
                for (Locus randomLocus : randomDude.getLoci()) {
                    if (!_cachedProbabilities.containsKey(randomLocus.hashCode())) {
                        randomSession.setLocusEnabled(randomLocus.getName(), true);
                        cacheMisses++;
                    } else {
                        randomSession.setLocusEnabled(randomLocus.getName(), false);
                    }
                }

                if (cacheMisses != 0) {
                    _model = LRMathModelFactory.getMathematicalModel(randomSession.getMathematicalModelName());
                    _model.addProgressListener(_progress);
                    LikelihoodRatio randomLR = _model.doAnalysis(randomSession);
                    if (randomLR == null) {
                        _progress.iterationDone(0.0);
                        continue;
                    }
                    // Add results to the cache
                    for (Ratio ratio : randomLR.getRatios()) {
                        Locus randomLocus = randomDude.getLocus(ratio.getLocusName());
                        _cachedProbabilities.put(randomLocus.hashCode(), ratio.getProsecutionProbability());
                    }
                }
                double prosecutionProbability = 1.0;
                // Enrich calculated results with cached results
                for (Locus randomLocus : randomDude.getLoci()) {
                    if (randomSession.isLocusValid(randomLocus.getName())) {
                        prosecutionProbability *= _cachedProbabilities.get(randomLocus.hashCode());
                    }
                }
                results.add(prosecutionProbability);
                _progress.iterationDone(prosecutionProbability);
            }
            _progress.analysisFinished(storeResults(description, iteration, originalLR, results, start));
        } catch (InterruptedException ex) {
            if (_model != null) {
                _model.interrupt();
            }
            _progress.analysisFinished(storeResults(description, iteration, originalLR, results, start));
        } catch (RejectedExecutionException ex) {
            LOG.debug("Performance test interrupted!");
            _progress.analysisFinished(storeResults(description, iteration, originalLR, results, start));
        } catch (InstantiationException | IllegalAccessException | NoSuchAlgorithmException | IllegalArgumentException ex) {
            storeResults(description, iteration, originalLR, results, start);
            _progress.analysisFinished(ex, storeResults(description, iteration, originalLR, results, start));
        }
    }

    private NonContributorTestResults storeResults(String description, long iteration, LikelihoodRatio originalLR, ArrayList<Double> results, long start) {
        NonContributorTestResults testResults = new NonContributorTestResults(description, iteration, originalLR, results);
        ((AnalysisReportImpl) _session.getCurrentReport()).setPerformanceTestResults(testResults);
        _session.getCurrentReport().addProcessingTime(System.currentTimeMillis() - start);
        return testResults;
    }
}
