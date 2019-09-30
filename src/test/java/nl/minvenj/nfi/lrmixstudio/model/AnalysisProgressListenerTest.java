/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.minvenj.nfi.lrmixstudio.model;

import nl.minvenj.nfi.lrmixstudio.model.AnalysisProgressListener;
import nl.minvenj.nfi.lrmixstudio.domain.Hypothesis;
import nl.minvenj.nfi.lrmixstudio.domain.LikelihoodRatio;
import nl.minvenj.nfi.lrmixstudio.domain.LocusProbabilities;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

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
        AnalysisProgressListener instance = new AnalysisProgressListenerImpl();
        instance.analysisStarted();
    }

    /**
     * Test of analysisDone method, of class AnalysisProgressListener.
     */
    @Test
    public void testAnalysisDone() {
        System.out.println("analysisDone");
        LikelihoodRatio lr = null;
        AnalysisProgressListener instance = new AnalysisProgressListenerImpl();
        instance.analysisFinished(lr);
    }

    /**
     * Test of hypothesisStarted method, of class AnalysisProgressListener.
     */
    @Test
    public void testHypothesisStarted() {
        System.out.println("hypothesisStarted");
        Hypothesis hypothesis = null;
        AnalysisProgressListener instance = new AnalysisProgressListenerImpl();
        instance.hypothesisStarted(hypothesis);
    }

    /**
     * Test of hypothesisFinished method, of class AnalysisProgressListener.
     */
    @Test
    public void testHypothesisFinished() {
        System.out.println("hypothesisFinished");
        Hypothesis hypothesis = null;
        LocusProbabilities probabilities = null;
        AnalysisProgressListener instance = new AnalysisProgressListenerImpl();
        instance.hypothesisFinished(hypothesis, probabilities);
    }

    /**
     * Test of locusStarted method, of class AnalysisProgressListener.
     */
    @Test
    public void testLocusStarted() {
        System.out.println("locusStarted");
        Hypothesis hypothesis = null;
        String locusName = "";
        long jobsize = 0L;
        AnalysisProgressListener instance = new AnalysisProgressListenerImpl();
        instance.locusStarted(hypothesis, locusName, jobsize);
    }

    /**
     * Test of locusFinished method, of class AnalysisProgressListener.
     */
    @Test
    public void testLocusFinished() {
        System.out.println("locusFinished");
        String locusName = "";
        Hypothesis hypothesis = null;
        Double locusProbability = null;
        AnalysisProgressListener instance = new AnalysisProgressListenerImpl();
        instance.locusFinished(hypothesis, locusName, locusProbability);
    }

    /**
     * Test of analysisFinished method, of class AnalysisProgressListener.
     */
    @Test
    public void testAnalysisFinished_LikelihoodRatio() {
        System.out.println("analysisFinished");
        LikelihoodRatio lr = null;
        AnalysisProgressListener instance = new AnalysisProgressListenerImpl();
        instance.analysisFinished(lr);
    }

    /**
     * Test of analysisFinished method, of class AnalysisProgressListener.
     */
    @Test
    public void testAnalysisFinished_Exception() {
        System.out.println("analysisFinished");
        Exception e = null;
        AnalysisProgressListener instance = new AnalysisProgressListenerImpl();
        instance.analysisFinished(e);
    }

    private static class AnalysisProgressListenerImpl implements AnalysisProgressListener {

        @Override
        public void analysisStarted() {
        }

        @Override
        public void hypothesisStarted(Hypothesis hypothesis) {
        }

        @Override
        public void hypothesisFinished(Hypothesis hypothesis, LocusProbabilities probabilities) {
        }

        @Override
        public void locusStarted(Hypothesis hypothesis, String locusName, long jobsize) {
        }

        @Override
        public void locusFinished(Hypothesis hypothesis, String locusName, Double locusProbability) {
        }

        @Override
        public void analysisFinished(LikelihoodRatio lr) {
        }

        @Override
        public void analysisFinished(Exception e) {
        }
    }
}