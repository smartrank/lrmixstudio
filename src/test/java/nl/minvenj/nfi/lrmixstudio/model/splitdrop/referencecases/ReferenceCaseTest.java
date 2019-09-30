/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.minvenj.nfi.lrmixstudio.model.splitdrop.referencecases;

import static org.junit.Assert.fail;

import java.util.Collection;

import org.junit.Ignore;

import nl.minvenj.nfi.lrmixstudio.domain.Hypothesis;
import nl.minvenj.nfi.lrmixstudio.domain.LikelihoodRatio;
import nl.minvenj.nfi.lrmixstudio.domain.LocusProbabilities;
import nl.minvenj.nfi.lrmixstudio.domain.PopulationStatistics;
import nl.minvenj.nfi.lrmixstudio.domain.Ratio;
import nl.minvenj.nfi.lrmixstudio.domain.Sample;
import nl.minvenj.nfi.lrmixstudio.io.PopulationStatisticsReader;
import nl.minvenj.nfi.lrmixstudio.io.SampleReader;
import nl.minvenj.nfi.lrmixstudio.model.AnalysisProgressListener;

/**
 *
 * @author dejong
 */
@Ignore
public class ReferenceCaseTest {

    protected static final String REFERENCE_NFI_POPULATION_STATISTICS_FILENAME = "/testfiles/frequencies_NFI.csv";
    protected static final int THREADCOUNT = 20;

    public static final class ReferenceAnalysisProgressListenerImpl implements AnalysisProgressListener {

        @Override
        public void analysisStarted() {
            System.out.println("Analysis Started");
        }

        @Override
        public void analysisFinished(final LikelihoodRatio lr) {
            System.out.println("Analysis Finished");
        }

        @Override
        public void analysisFinished(final Exception e) {
        }

        @Override
        public void hypothesisStarted(final Hypothesis hypothesis) {
            System.out.println("Hypothesis " + hypothesis.getId() + " Started");
        }

        @Override
        public void hypothesisFinished(final Hypothesis hypothesis, final LocusProbabilities probability) {
            System.out.println();
        }

        @Override
        public synchronized void locusStarted(final Hypothesis hypothesis, final String locusName, final long jobsize) {
        }

        @Override
        public void locusFinished(final Hypothesis hypothesis, final String locusName, final Double locusProbability) {
        }
    }

    protected String getProbabilityTerm(final Ratio lr) {
        double ratio = lr.getRatio();
        String relativeTerm = "more";
        if (ratio < 1) {
            ratio = 1 / ratio;
            relativeTerm = "less";
        }
        if (ratio < 2) {
            return "approximately equally likely";
        }
        if (ratio < 10) {
            return "slightly " + relativeTerm + " likely";
        }
        if (ratio < 100) {
            return relativeTerm + " likely";
        }
        if (ratio < 10000) {
            return "much " + relativeTerm + " likely";
        }
        if (ratio < 1000000) {
            return "very much " + relativeTerm + " likely";
        }
        return "extremely much " + relativeTerm + " likely";
    }

    protected PopulationStatistics readPopulationStatistics(final String fileName) {
        PopulationStatistics popStats = null;
        try {
            popStats = new PopulationStatisticsReader(fileName).getStatistics();
        } catch (final Exception ex) {
            fail(ex.getClass().getName() + " " + ex.getMessage() + " for " + fileName);
        }
        return popStats;
    }

    protected Collection<Sample> readReplicates(final String fileName) {
        Collection<Sample> samples = null;
        try {
            samples = new SampleReader(fileName, true).getSamples();
        } catch (final Exception ex) {
            fail(ex.getClass().getName() + " " + ex.getMessage() + " for " + fileName);
        }
        return samples;
    }

    protected Collection<Sample> readProfiles(final String fileName) {
        Collection<Sample> samples = null;
        try {
            samples = new SampleReader(fileName, false).getSamples();
        } catch (final Exception ex) {
            fail(ex.getClass().getName() + " " + ex.getMessage() + " for " + fileName);
        }
        return samples;
    }
}
