/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.minvenj.nfi.lrmixstudio.model;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import nl.minvenj.nfi.lrmixstudio.domain.Hypothesis;
import nl.minvenj.nfi.lrmixstudio.domain.LikelihoodRatio;
import nl.minvenj.nfi.lrmixstudio.domain.LocusProbabilities;

/**
 *
 * @author dejong
 */
public class AnalysisProgressListenerTest {

    public AnalysisProgressListenerTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of analysisStarted method, of class AnalysisProgressListener.
     */
    @Test
    public void testAnalysisStarted() {
        System.out.println("analysisStarted");
        final AnalysisProgressListener instance = new AnalysisProgressListenerImpl();
        instance.analysisStarted();
    }

    /**
     * Test of analysisDone method, of class AnalysisProgressListener.
     */
    @Test
    public void testAnalysisDone() {
        System.out.println("analysisDone");
        final LikelihoodRatio lr = null;
        final AnalysisProgressListener instance = new AnalysisProgressListenerImpl();
        instance.analysisFinished(lr);
    }

    /**
     * Test of hypothesisStarted method, of class AnalysisProgressListener.
     */
    @Test
    public void testHypothesisStarted() {
        System.out.println("hypothesisStarted");
        final Hypothesis hypothesis = null;
        final AnalysisProgressListener instance = new AnalysisProgressListenerImpl();
        instance.hypothesisStarted(hypothesis);
    }

    /**
     * Test of hypothesisFinished method, of class AnalysisProgressListener.
     */
    @Test
    public void testHypothesisFinished() {
        System.out.println("hypothesisFinished");
        final Hypothesis hypothesis = null;
        final LocusProbabilities probabilities = null;
        final AnalysisProgressListener instance = new AnalysisProgressListenerImpl();
        instance.hypothesisFinished(hypothesis, probabilities);
    }

    /**
     * Test of locusStarted method, of class AnalysisProgressListener.
     */
    @Test
    public void testLocusStarted() {
        System.out.println("locusStarted");
        final Hypothesis hypothesis = null;
        final String locusName = "";
        final long jobsize = 0L;
        final AnalysisProgressListener instance = new AnalysisProgressListenerImpl();
        instance.locusStarted(hypothesis, locusName, jobsize);
    }

    /**
     * Test of locusFinished method, of class AnalysisProgressListener.
     */
    @Test
    public void testLocusFinished() {
        System.out.println("locusFinished");
        final String locusName = "";
        final Hypothesis hypothesis = null;
        final Double locusProbability = null;
        final AnalysisProgressListener instance = new AnalysisProgressListenerImpl();
        instance.locusFinished(hypothesis, locusName, locusProbability);
    }

    /**
     * Test of analysisFinished method, of class AnalysisProgressListener.
     */
    @Test
    public void testAnalysisFinished_LikelihoodRatio() {
        System.out.println("analysisFinished");
        final LikelihoodRatio lr = null;
        final AnalysisProgressListener instance = new AnalysisProgressListenerImpl();
        instance.analysisFinished(lr);
    }

    /**
     * Test of analysisFinished method, of class AnalysisProgressListener.
     */
    @Test
    public void testAnalysisFinished_Exception() {
        System.out.println("analysisFinished");
        final Exception e = null;
        final AnalysisProgressListener instance = new AnalysisProgressListenerImpl();
        instance.analysisFinished(e);
    }

    private static class AnalysisProgressListenerImpl implements AnalysisProgressListener {

        @Override
        public void analysisStarted() {
        }

        @Override
        public void hypothesisStarted(final Hypothesis hypothesis) {
        }

        @Override
        public void hypothesisFinished(final Hypothesis hypothesis, final LocusProbabilities probabilities) {
        }

        @Override
        public void locusStarted(final Hypothesis hypothesis, final String locusName, final long jobsize) {
        }

        @Override
        public void locusFinished(final Hypothesis hypothesis, final String locusName, final Double locusProbability) {
        }

        @Override
        public void analysisFinished(final LikelihoodRatio lr) {
        }

        @Override
        public void analysisFinished(final Throwable e) {
        }
    }
}