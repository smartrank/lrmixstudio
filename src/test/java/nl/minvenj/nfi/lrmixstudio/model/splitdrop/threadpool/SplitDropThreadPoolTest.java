/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.minvenj.nfi.lrmixstudio.model.splitdrop.threadpool;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import nl.minvenj.nfi.lrmixstudio.domain.Hypothesis;
import nl.minvenj.nfi.lrmixstudio.domain.LikelihoodRatio;
import nl.minvenj.nfi.lrmixstudio.domain.LocusProbabilities;
import nl.minvenj.nfi.lrmixstudio.domain.PopulationStatistics;
import nl.minvenj.nfi.lrmixstudio.domain.Ratio;
import nl.minvenj.nfi.lrmixstudio.model.AnalysisProgressListener;
import nl.minvenj.nfi.lrmixstudio.model.ConfigurationData;

/**
 *
 * @author dejong
 */
public class SplitDropThreadPoolTest {

    public SplitDropThreadPoolTest() {
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
     * Test of doAnalysis method, of class SplitDropThreadPool.
     */
    @Test
    public void testDoAnalysis() throws InterruptedException {
        System.out.println("doAnalysis");
        final ConfigurationData config = new ConfigurationData();
        final PopulationStatistics popStats = new PopulationStatistics("DummyPopStats");
        config.setProsecution(new Hypothesis("Prosecution", popStats));
        config.setDefense(new Hypothesis("Defense", popStats));
        config.setStatistics(popStats);
        config.setCaseNumber("Case Number");
        final SplitDropThreadPool instance = new SplitDropThreadPool();
        final LikelihoodRatio lr = instance.doAnalysis(config);
        assertNotNull(lr);
        final Ratio overall = lr.getOverallRatio();
        assertNotNull(overall);
        assertEquals(1.0, overall.getRatio(), 0.00001);
    }

    /**
     * Test of doSensitivityAnalysis method, of class SplitDropThreadPool.
     */
    @Test
    @Ignore("Not supported yet")
    public void testDoSensitivityAnalysis() {
        System.out.println("doSensitivityAnalysis");
        final ConfigurationData config = null;
        final SplitDropThreadPool instance = new SplitDropThreadPool();
        instance.doSensitivityAnalysis(config);
    }

    /**
     * Test of doPerformanceAnalysis method, of class SplitDropThreadPool.
     */
    @Test
    @Ignore("Not supported yet")
    public void testDoPerformanceAnalysis() {
        System.out.println("doPerformanceAnalysis");
        final ConfigurationData config = null;
        final SplitDropThreadPool instance = new SplitDropThreadPool();
        instance.doPerformanceAnalysis(config);
    }

    /**
     * Test of addProgressListener method, of class SplitDropThreadPool.
     */
    @Test
    public void testAddProgressListener() {
        System.out.println("addProgressListener");
        final SplitDropThreadPool instance = new SplitDropThreadPool();
        final AnalysisProgressListenerImpl progress = new AnalysisProgressListenerImpl();
        instance.addProgressListener(progress);
    }

    /**
     * Test of analysisStarted method, of class SplitDropThreadPool.
     */
    @Test
    public void testAnalysisStarted() {
        System.out.println("analysisStarted");
        final SplitDropThreadPool instance = new SplitDropThreadPool();
        final AnalysisProgressListenerImpl progress = new AnalysisProgressListenerImpl();
        instance.addProgressListener(progress);
        instance.analysisStarted();
        assertEquals("analysisStarted", progress.getLog());

    }

    /**
     * Test of analysisFinished method, of class SplitDropThreadPool.
     */
    @Test
    public void testAnalysisFinished_Exception() {
        System.out.println("analysisFinished");
        final Exception e = new Exception();
        final SplitDropThreadPool instance = new SplitDropThreadPool();
        final AnalysisProgressListenerImpl progress = new AnalysisProgressListenerImpl();
        instance.addProgressListener(progress);
        instance.analysisFinished(e);
        assertEquals("analysisFinished exception=java.lang.Exception", progress.getLog());
    }

    /**
     * Test of hypothesisStarted method, of class SplitDropThreadPool.
     */
    @Test
    public void testHypothesisStarted() {
        System.out.println("hypothesisStarted");
        final Hypothesis hypothesis = new Hypothesis("Prosecution", new PopulationStatistics("dummyPopStats"));
        final SplitDropThreadPool instance = new SplitDropThreadPool();
        final AnalysisProgressListenerImpl progress = new AnalysisProgressListenerImpl();
        instance.addProgressListener(progress);
        instance.hypothesisStarted(hypothesis);
        assertEquals("hypothesisStarted Prosecution", progress.getLog());
    }

    /**
     * Test of hypothesisFinished method, of class SplitDropThreadPool.
     */
    @Test
    public void testHypothesisFinished() {
        System.out.println("hypothesisFinished");
        final Hypothesis hypothesis = new Hypothesis("Prosecution", new PopulationStatistics("dummyPopStats"));
        final LocusProbabilities probabilities = new LocusProbabilities();
        final SplitDropThreadPool instance = new SplitDropThreadPool();
        final AnalysisProgressListenerImpl progress = new AnalysisProgressListenerImpl();
        instance.addProgressListener(progress);
        instance.hypothesisFinished(hypothesis, probabilities);
        assertEquals("hypothesisFinished Prosecution", progress.getLog());
    }

    /**
     * Test of locusStarted method, of class SplitDropThreadPool.
     */
    @Test
    public void testLocusStarted() {
        System.out.println("locusStarted");
        final Hypothesis hypothesis = new Hypothesis("Prosecution", new PopulationStatistics("dummyPopStats"));
        final String locusName = "vWA";
        final long jobsize = 0L;
        final SplitDropThreadPool instance = new SplitDropThreadPool();
        final AnalysisProgressListenerImpl progress = new AnalysisProgressListenerImpl();
        instance.addProgressListener(progress);
        instance.locusStarted(hypothesis, locusName, jobsize);
        assertEquals("locusStarted vWA", progress.getLog());
    }

    /**
     * Test of getId method, of class SplitDropThreadPool.
     */
    @Test
    public void testGetId() {
        System.out.println("getId");
        final SplitDropThreadPool instance = new SplitDropThreadPool();
        final String expResult = "SplitDrop Threadpool Edition";
        final String result = instance.getId();
        assertEquals(expResult, result);
    }

    /**
     * Test of interrupt method, of class SplitDropThreadPool.
     */
    @Test
    public void testInterrupt() {
        System.out.println("interrupt");
        final SplitDropThreadPool instance = new SplitDropThreadPool();
        instance.interrupt();
    }

    /**
     * Test of getLikelihoodRatio method, of class SplitDropThreadPool.
     */
    @Test
    public void testGetLikelihoodRatio() throws Exception {
        System.out.println("getLikelihoodRatio");
        try {

            final SplitDropThreadPool instance = new SplitDropThreadPool();
            final LikelihoodRatio result = instance.getLikelihoodRatio();
            fail("Expected exception not thrown!");
        } catch (final Exception e) {
            if (!"Analysis not running!".equalsIgnoreCase(e.getMessage())) {
                throw e;
            }
        }
    }

    private static class AnalysisProgressListenerImpl implements AnalysisProgressListener {

        private final StringBuilder log;

        public AnalysisProgressListenerImpl() {
            log = new StringBuilder();
        }

        public String getLog() {
            return log.toString();
        }

        @Override
        public void analysisStarted() {
            log.append("analysisStarted");
        }

        @Override
        public void analysisFinished(final LikelihoodRatio lr) {
            log.append("analysisFinished with LR");
        }

        @Override
        public void analysisFinished(final Throwable e) {
            log.append("analysisFinished exception=").append(e);
        }

        @Override
        public void hypothesisStarted(final Hypothesis hypothesis) {
            log.append("hypothesisStarted ").append(hypothesis.getId());
        }

        @Override
        public void hypothesisFinished(final Hypothesis hypothesis, final LocusProbabilities probability) {
            log.append("hypothesisFinished ").append(hypothesis.getId());
        }

        @Override
        public void locusStarted(final Hypothesis hypothesis, final String locusName, final long jobsize) {
            log.append("locusStarted ").append(locusName);
        }

        @Override
        public void locusFinished(final Hypothesis hypothesis, final String locusName, final Double locusProbability) {
            log.append("locusFinished ").append(locusName);
        }
    }
}
