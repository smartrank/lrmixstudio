/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.minvenj.nfi.lrmixstudio.model;

import nl.minvenj.nfi.lrmixstudio.model.DefaultAnalysisProgressListenerImpl;
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
public class DefaultAnalysisProgressListenerImplTest {

    public DefaultAnalysisProgressListenerImplTest() {
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
     * Test of analysisStarted method, of class DefaultAnalysisProgressListenerImpl.
     */
    @Test
    public void testAnalysisStarted() {
        System.out.println("analysisStarted");
        DefaultAnalysisProgressListenerImpl instance = new DefaultAnalysisProgressListenerImpl();
        instance.analysisStarted();
    }

    /**
     * Test of analysisDone method, of class DefaultAnalysisProgressListenerImpl.
     */
    @Test
    public void testAnalysisDone() {
        System.out.println("analysisDone");
        LikelihoodRatio lr = null;
        DefaultAnalysisProgressListenerImpl instance = new DefaultAnalysisProgressListenerImpl();
        instance.analysisFinished(lr);
    }

    /**
     * Test of hypothesisStarted method, of class DefaultAnalysisProgressListenerImpl.
     */
    @Test
    public void testHypothesisStarted() {
        System.out.println("hypothesisStarted");
        Hypothesis hypothesis = null;
        DefaultAnalysisProgressListenerImpl instance = new DefaultAnalysisProgressListenerImpl();
        instance.hypothesisStarted(hypothesis);
    }

    /**
     * Test of hypothesisFinished method, of class DefaultAnalysisProgressListenerImpl.
     */
    @Test
    public void testHypothesisFinished() {
        System.out.println("hypothesisFinished");
        Hypothesis hypothesis = null;
        LocusProbabilities probabilities = null;
        DefaultAnalysisProgressListenerImpl instance = new DefaultAnalysisProgressListenerImpl();
        instance.hypothesisFinished(hypothesis, probabilities);
    }

    /**
     * Test of locusStarted method, of class DefaultAnalysisProgressListenerImpl.
     */
    @Test
    public void testLocusStarted() {
        System.out.println("locusStarted");
        Hypothesis hypothesis = null;
        String locusName = "";
        long jobsize = 0L;
        DefaultAnalysisProgressListenerImpl instance = new DefaultAnalysisProgressListenerImpl();
        instance.locusStarted(hypothesis, locusName, jobsize);
    }

    /**
     * Test of locusFinished method, of class DefaultAnalysisProgressListenerImpl.
     */
    @Test
    public void testLocusFinished() {
        System.out.println("locusFinished");
        Hypothesis hypothesis = null;
        String locusName = "";
        Double locusProbability = null;
        DefaultAnalysisProgressListenerImpl instance = new DefaultAnalysisProgressListenerImpl();
        instance.locusFinished(hypothesis, locusName, locusProbability);
    }

    /**
     * Test of analysisFinished method, of class DefaultAnalysisProgressListenerImpl.
     */
    @Test
    public void testAnalysisFinished_LikelihoodRatio() {
        System.out.println("analysisFinished");
        LikelihoodRatio lr = null;
        DefaultAnalysisProgressListenerImpl instance = new DefaultAnalysisProgressListenerImpl();
        instance.analysisFinished(lr);
    }

    /**
     * Test of analysisFinished method, of class DefaultAnalysisProgressListenerImpl.
     */
    @Test
    public void testAnalysisFinished_Exception() {
        System.out.println("analysisFinished");
        Exception e = null;
        DefaultAnalysisProgressListenerImpl instance = new DefaultAnalysisProgressListenerImpl();
        instance.analysisFinished(e);
    }
}