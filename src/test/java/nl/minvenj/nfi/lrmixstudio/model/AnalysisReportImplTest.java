/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.minvenj.nfi.lrmixstudio.model;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import nl.minvenj.nfi.lrmixstudio.domain.Allele;
import nl.minvenj.nfi.lrmixstudio.domain.Hypothesis;
import nl.minvenj.nfi.lrmixstudio.domain.LikelihoodRatio;
import nl.minvenj.nfi.lrmixstudio.domain.Locus;
import nl.minvenj.nfi.lrmixstudio.domain.LocusProbabilities;
import nl.minvenj.nfi.lrmixstudio.domain.PopulationStatistics;
import nl.minvenj.nfi.lrmixstudio.domain.Sample;

/**
 *
 * @author dejong
 */
public class AnalysisReportImplTest {

    protected ConfigurationData config;
    private PopulationStatistics popStats;
    private Hypothesis defense;
    private Hypothesis prosecution;
    private Collection<Sample> replicates;
    private Collection<Sample> profiles;
    private String programVersion;

    public AnalysisReportImplTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        config = new ConfigurationData();
        popStats = new PopulationStatistics("Population Statistics filename");
        defense = new Hypothesis("Defense", popStats);
        prosecution = new Hypothesis("Defense", popStats);

        final Sample replicate1 = new Sample("Replicate 1");
        final Locus locus1 = new Locus("Locus 1");
        locus1.addAllele(new Allele("11"));
        locus1.addAllele(new Allele("12"));
        replicate1.addLocus(locus1);

        final Sample replicate2 = new Sample("Replicate 2");
        final Locus locus2 = new Locus("Locus 1");
        locus2.addAllele(new Allele("11"));
        locus2.addAllele(new Allele("11"));
        replicate2.addLocus(locus2);

        final Sample profile1 = new Sample("Profile 1");
        final Locus locus3 = new Locus("Locus 1");
        locus3.addAllele(new Allele("12"));
        locus3.addAllele(new Allele("12"));
        profile1.addLocus(locus3);

        profiles = new ArrayList<>();
        profiles.add(profile1);

        replicates = new ArrayList<>();
        replicates.add(replicate1);
        replicates.add(replicate2);

        config.addReplicates(replicates);
        config.addProfiles(profiles);
        config.setProsecution(prosecution);
        config.setDefense(defense);
        config.setStatistics(popStats);
        config.setProgramVersion(programVersion);
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getLikelihoodRatio method, of class AnalysisReportImpl.
     */
    @Test
    public void testGetLikelihoodRatio() {
        System.out.println("getLikelihoodRatio");
        final AnalysisReportImpl instance = new AnalysisReportImpl(config);
        final LikelihoodRatio expResult = new LikelihoodRatio();
        assertNull(instance.getLikelihoodRatio());
        instance.analysisFinished(expResult);
        assertEquals(expResult, instance.getLikelihoodRatio());
    }

    /**
     * Test of getDefenseHypothesis method, of class AnalysisReportImpl.
     */
    @Test
    public void testGetDefenseHypothesis() {
        System.out.println("getDefenseHypothesis");
        final AnalysisReportImpl instance = new AnalysisReportImpl(config);
        final Hypothesis result = instance.getDefenseHypothesis();
        assertEquals(defense, result);
    }

    /**
     * Test of getProsecutionHypothesis method, of class AnalysisReportImpl.
     */
    @Test
    public void testGetProsecutionHypothesis() {
        System.out.println("getProsecutionHypothesis");
        final AnalysisReportImpl instance = new AnalysisReportImpl(config);
        final Hypothesis result = instance.getProsecutionHypothesis();
        assertEquals(prosecution, result);
    }

    /**
     * Test of getStartTime method, of class AnalysisReportImpl.
     */
    @Test
    public void testGetStartTime() {
        System.out.println("getStartTime");
        final AnalysisReportImpl instance = new AnalysisReportImpl(config);
        final long now = System.currentTimeMillis();
        instance.analysisStarted();
        assertEquals(now, instance.getStartTime(), 1000);
    }

    /**
     * Test of getStopTime method, of class AnalysisReportImpl.
     */
    @Test
    public void testGetStopTime() {
        System.out.println("getStopTime");
        final AnalysisReportImpl instance = new AnalysisReportImpl(config);
        instance.analysisStarted();
        instance.analysisFinished(new Exception("Test Exception"));
        final long now = System.currentTimeMillis();
        final long result = instance.getStopTime();
        assertEquals(now, result, 1000);
    }

    /**
     * Test of isSucceeded method, of class AnalysisReportImpl.
     */
    @Test
    public void testIsSucceeded() {
        System.out.println("isSucceeded");
        final AnalysisReportImpl instance = new AnalysisReportImpl(config);
        assertFalse(instance.isSucceeded());
        instance.analysisFinished(new LikelihoodRatio());
        assertTrue(instance.isSucceeded());
    }

    /**
     * Test of getException method, of class AnalysisReportImpl.
     */
    @Test
    public void testGetException() {
        System.out.println("getException");
        final AnalysisReportImpl instance = new AnalysisReportImpl(config);
        final Throwable expResult = null;
        final Throwable result = instance.getException();
        assertEquals(expResult, result);
    }

    /**
     * Test of analysisStarted method, of class AnalysisReportImpl.
     */
    @Test
    public void testAnalysisStarted() {
        System.out.println("analysisStarted");
        final AnalysisReportImpl instance = new AnalysisReportImpl(config);
        instance.analysisStarted();
    }

    /**
     * Test of analysisFinished method, of class AnalysisReportImpl.
     */
    @Test
    public void testAnalysisFinished_LikelihoodRatio() {
        System.out.println("analysisFinished");
        final LikelihoodRatio lr = new LikelihoodRatio();
        final AnalysisReportImpl instance = new AnalysisReportImpl(config);
        instance.analysisFinished(lr);
        assertEquals(lr, instance.getLikelihoodRatio());
    }

    /**
     * Test of analysisFinished method, of class AnalysisReportImpl.
     */
    @Test
    public void testAnalysisFinished_Exception() {
        System.out.println("analysisFinished");
        final Exception e = new Exception("Test Exception");
        final AnalysisReportImpl instance = new AnalysisReportImpl(config);
        instance.analysisFinished(e);
        assertFalse(instance.isSucceeded());
        assertEquals(e, instance.getException());
    }

    /**
     * Test of hypothesisStarted method, of class AnalysisReportImpl.
     */
    @Test
    public void testHypothesisStarted() {
        System.out.println("hypothesisStarted");
        final AnalysisReportImpl instance = new AnalysisReportImpl(config);
        instance.hypothesisStarted(defense);
    }

    /**
     * Test of hypothesisFinished method, of class AnalysisReportImpl.
     */
    @Test
    public void testHypothesisFinished() {
        System.out.println("hypothesisFinished");
        final LocusProbabilities probabilities = new LocusProbabilities();
        final AnalysisReportImpl instance = new AnalysisReportImpl(config);
        instance.hypothesisFinished(defense, probabilities);
    }

    /**
     * Test of locusStarted method, of class AnalysisReportImpl.
     */
    @Test
    public void testLocusStarted() {
        System.out.println("locusStarted");
        final String locusName = "FGA";
        final long jobsize = 0L;
        final AnalysisReportImpl instance = new AnalysisReportImpl(config);
        instance.locusStarted(defense, locusName, jobsize);
    }

    /**
     * Test of locusFinished method, of class AnalysisReportImpl.
     */
    @Test
    public void testLocusFinished() {
        System.out.println("locusFinished");
        final String locusName = "FGA";
        final Double locusProbability = 0.0;
        final AnalysisReportImpl instance = new AnalysisReportImpl(config);
        instance.locusFinished(defense, locusName, locusProbability);
    }

    /**
     * Test of getCaseNumber method, of class AnalysisReportImpl.
     */
    @Test
    public void testGetCaseNumber() {
        System.out.println("getCaseNumber");
        final AnalysisReportImpl instance = new AnalysisReportImpl(config);
        final String expResult = "4321";
        instance.setCaseNumber(expResult);
        final String result = instance.getCaseNumber();
        assertEquals(expResult, result);
    }

    /**
     * Test of getProgramVersion method, of class AnalysisReportImpl.
     */
    @Test
    public void testGetProgramVersion() {
        System.out.println("getProgramVersion");
        final AnalysisReportImpl instance = new AnalysisReportImpl(config);
        final String result = instance.getProgramVersion();
        assertEquals(programVersion, result);
    }

    /**
     * Test of getReplicates method, of class AnalysisReportImpl.
     */
    @Test
    public void testGetReplicates() {
        System.out.println("getReplicates");
        final AnalysisReportImpl instance = new AnalysisReportImpl(config);
        final Collection result = instance.getReplicates();
        assertArrayEquals(replicates.toArray(), result.toArray());
    }

    /**
     * Test of getProfiles method, of class AnalysisReportImpl.
     */
    @Test
    public void testGetProfiles() {
        System.out.println("getProfiles");
        final AnalysisReportImpl instance = new AnalysisReportImpl(config);
        final Collection result = instance.getProfiles();
        assertArrayEquals(profiles.toArray(), result.toArray());
    }

    /**
     * Test of getPopulationStatistics method, of class AnalysisReportImpl.
     */
    @Test
    public void testGetPopulationStatistics() {
        System.out.println("getPopulationStatistics");
        final AnalysisReportImpl instance = new AnalysisReportImpl(config);
        final PopulationStatistics result = instance.getPopulationStatistics();
        assertEquals(popStats, result);
    }

    /**
     * Test of getSensitivityAnalysisResults method, of class
     * AnalysisReportImpl.
     */
    @Test
    public void testGetSensitivityAnalysisResults() {
        System.out.println("getSensitivityAnalysisResults");
        final AnalysisReportImpl instance = new AnalysisReportImpl(config);
        final SensitivityAnalysisResults expResult = new SensitivityAnalysisResults();
        final SensitivityAnalysisResults result = instance.getSensitivityAnalysisResults();
        assertEquals(expResult.toString(), result.toString());
    }

    /**
     * Test of setCaseNumber method, of class AnalysisReportImpl.
     */
    @Test
    public void testSetCaseNumber() {
        System.out.println("setCaseNumber");
        final AnalysisReportImpl instance = new AnalysisReportImpl(config);
        final String expResult = "4321";
        instance.setCaseNumber(expResult);
    }

    /**
     * Test of setDefenseHypothesis method, of class AnalysisReportImpl.
     */
    @Test
    public void testSetDefenseHypothesis() {
        System.out.println("setDefenseHypothesis");
        final AnalysisReportImpl instance = new AnalysisReportImpl(config);
        final Hypothesis newDefense = new Hypothesis("New Defense", popStats);
        assertEquals(defense, instance.getDefenseHypothesis());
        instance.setDefenseHypothesis(newDefense);
        assertEquals(newDefense, instance.getDefenseHypothesis());
    }

    /**
     * Test of setProsecutionHypothesis method, of class AnalysisReportImpl.
     */
    @Test
    public void testSetProsecutionHypothesis() {
        System.out.println("setProsecutionHypothesis");
        final AnalysisReportImpl instance = new AnalysisReportImpl(config);
        final Hypothesis newProsecution = new Hypothesis("New Prosecution", popStats);
        assertEquals(prosecution, instance.getProsecutionHypothesis());
        instance.setProsecutionHypothesis(newProsecution);
        assertEquals(newProsecution, instance.getProsecutionHypothesis());
    }

    /**
     * Test of getGuid method, of class AnalysisReportImpl.
     */
    @Test
    public void testGetGuid() {
        System.out.println("getGuid");
        AnalysisReportImpl instance = new AnalysisReportImpl(config);
        int expResult = -83425509;
        int result = instance.getGuid();
        assertEquals(expResult, result);

        config.setRareAlleleFrequency(0.5);
        instance = new AnalysisReportImpl(config);
        expResult = -220523865;
        result = instance.getGuid();
        assertEquals(expResult, result);

        config.setRareAlleleFrequency(PopulationStatistics.DEFAULT_FREQUENCY);
        instance = new AnalysisReportImpl(config);
        expResult = -83425509;
        result = instance.getGuid();
        assertEquals(expResult, result);
    }

    /**
     * Test of enrich method, of class AnalysisReportImpl.
     */
    @Test
    public void testEnrich() {
        System.out.println("enrich");
        final AnalysisReportImpl instance = new AnalysisReportImpl(config);
        final AnalysisReportImpl other = new AnalysisReportImpl(config);
        assertNull(other.getSensitivityAnalysisResults().getDropoutEstimation());
        other.getSensitivityAnalysisResults().setDropoutEstimation(new DropoutEstimation());
        instance.enrich(other);
        assertNotNull(instance.getSensitivityAnalysisResults().getDropoutEstimation());
    }
}