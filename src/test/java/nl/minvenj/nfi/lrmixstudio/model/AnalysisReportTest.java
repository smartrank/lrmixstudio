/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.minvenj.nfi.lrmixstudio.model;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.Collections;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import nl.minvenj.nfi.lrmixstudio.domain.Allele;
import nl.minvenj.nfi.lrmixstudio.domain.Hypothesis;
import nl.minvenj.nfi.lrmixstudio.domain.LikelihoodRatio;
import nl.minvenj.nfi.lrmixstudio.domain.PopulationStatistics;
import nl.minvenj.nfi.lrmixstudio.domain.Sample;

/**
 *
 * @author dejong
 */
public class AnalysisReportTest {

    public AnalysisReportTest() {
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
     * Test of getLikelihoodRatio method, of class AnalysisReport.
     */
    @Test
    public void testGetLikelihoodRatio() {
        System.out.println("getLikelihoodRatio");
        final AnalysisReport instance = new AnalysisReportImpl();
        final LikelihoodRatio expResult = null;
        final LikelihoodRatio result = instance.getLikelihoodRatio();
        assertEquals(expResult, result);
    }

    /**
     * Test of getDefenseHypothesis method, of class AnalysisReport.
     */
    @Test
    public void testGetDefenseHypothesis() {
        System.out.println("getDefenseHypothesis");
        final AnalysisReport instance = new AnalysisReportImpl();
        final Hypothesis expResult = null;
        final Hypothesis result = instance.getDefenseHypothesis();
        assertEquals(expResult, result);
    }

    /**
     * Test of getProsecutionHypothesis method, of class AnalysisReport.
     */
    @Test
    public void testGetProsecutionHypothesis() {
        System.out.println("getProsecutionHypothesis");
        final AnalysisReport instance = new AnalysisReportImpl();
        final Hypothesis expResult = null;
        final Hypothesis result = instance.getProsecutionHypothesis();
        assertEquals(expResult, result);
    }

    /**
     * Test of getStartTime method, of class AnalysisReport.
     */
    @Test
    public void testGetStartTime() {
        System.out.println("getStartTime");
        final AnalysisReport instance = new AnalysisReportImpl();
        final long expResult = 0L;
        final long result = instance.getStartTime();
        assertEquals(expResult, result);
    }

    /**
     * Test of getStopTime method, of class AnalysisReport.
     */
    @Test
    public void testGetStopTime() {
        System.out.println("getStopTime");
        final AnalysisReport instance = new AnalysisReportImpl();
        final long expResult = 0L;
        final long result = instance.getStopTime();
        assertEquals(expResult, result);
    }

    /**
     * Test of isSucceeded method, of class AnalysisReport.
     */
    @Test
    public void testIsSucceeded() {
        System.out.println("isSucceeded");
        final AnalysisReport instance = new AnalysisReportImpl();
        final boolean expResult = false;
        final boolean result = instance.isSucceeded();
        assertEquals(expResult, result);
    }

    /**
     * Test of getException method, of class AnalysisReport.
     */
    @Test
    public void testGetException() {
        System.out.println("getException");
        final AnalysisReport instance = new AnalysisReportImpl();
        final Throwable expResult = null;
        final Throwable result = instance.getException();
        assertEquals(expResult, result);
    }

    /**
     * Test of getCaseNumber method, of class AnalysisReport.
     */
    @Test
    public void testGetCaseNumber() {
        System.out.println("getCaseNumber");
        final AnalysisReport instance = new AnalysisReportImpl();
        final String expResult = "";
        final String result = instance.getCaseNumber();
        assertEquals(expResult, result);
    }

    /**
     * Test of getProgramVersion method, of class AnalysisReport.
     */
    @Test
    public void testGetProgramVersion() {
        System.out.println("getProgramVersion");
        final AnalysisReport instance = new AnalysisReportImpl();
        final String expResult = "";
        final String result = instance.getProgramVersion();
        assertEquals(expResult, result);
    }

    /**
     * Test of getSensitivityAnalysisResults method, of class AnalysisReport.
     */
    @Test
    public void testGetSensitivityAnalysisResults() {
        System.out.println("getSensitivityAnalysisResults");
        final AnalysisReport instance = new AnalysisReportImpl();
        final SensitivityAnalysisResults expResult = null;
        final SensitivityAnalysisResults result = instance.getSensitivityAnalysisResults();
        assertEquals(expResult, result);
    }

    /**
     * Test of getPopulationStatistics method, of class AnalysisReport.
     */
    @Test
    public void testGetPopulationStatistics() {
        System.out.println("getPopulationStatistics");
        final AnalysisReport instance = new AnalysisReportImpl();
        final PopulationStatistics expResult = null;
        final PopulationStatistics result = instance.getPopulationStatistics();
        assertEquals(expResult, result);
    }

    /**
     * Test of getReplicates method, of class AnalysisReport.
     */
    @Test
    public void testGetReplicates() {
        System.out.println("getReplicates");
        final AnalysisReport instance = new AnalysisReportImpl();
        final Collection expResult = null;
        final Collection result = instance.getReplicates();
        assertEquals(expResult, result);
    }

    /**
     * Test of getProfiles method, of class AnalysisReport.
     */
    @Test
    public void testGetProfiles() {
        System.out.println("getProfiles");
        final AnalysisReport instance = new AnalysisReportImpl();
        final Collection expResult = null;
        final Collection result = instance.getProfiles();
        assertEquals(expResult, result);
    }

    /**
     * Test of getGuid method, of class AnalysisReport.
     */
    @Test
    public void testGetGuid() {
        System.out.println("getGuid");
        final AnalysisReport instance = new AnalysisReportImpl();
        final int expResult = 0;
        final int result = instance.getGuid();
        assertEquals(expResult, result);
    }

    private class AnalysisReportImpl implements AnalysisReport {

        @Override
        public LikelihoodRatio getLikelihoodRatio() {
            return null;
        }

        @Override
        public Hypothesis getDefenseHypothesis() {
            return null;
        }

        @Override
        public Hypothesis getProsecutionHypothesis() {
            return null;
        }

        @Override
        public long getStartTime() {
            return 0L;
        }

        @Override
        public long getStopTime() {
            return 0L;
        }

        @Override
        public boolean isSucceeded() {
            return false;
        }

        @Override
        public Exception getException() {
            return null;
        }

        @Override
        public String getCaseNumber() {
            return "";
        }

        @Override
        public String getProgramVersion() {
            return "";
        }

        @Override
        public SensitivityAnalysisResults getSensitivityAnalysisResults() {
            return null;
        }

        @Override
        public PopulationStatistics getPopulationStatistics() {
            return null;
        }

        @Override
        public Collection<Sample> getReplicates() {
            return null;
        }

        @Override
        public Collection<Sample> getProfiles() {
            return null;
        }

        @Override
        public int getGuid() {
            return 0;
        }

        @Override
        public NonContributorTestResults getNonContributorTestResults() {
            return new NonContributorTestResults("Dummy");
        }

        @Override
        public String getRareAlleleFrequency() {
            return "";
        }

        @Override
        public Collection<Allele> getRareAlleles() {
            return Collections.emptyList();
        }

        @Override
        public boolean isExported() {
            return false;
        }

        @Override
        public void addProcessingTime(final long processingTime) {
        }

        @Override
        public long getProcessingTime() {
            return 0;
        }

        @Override
        public Collection<String> getEnabledLoci() {
            return Collections.emptyList();
        }

        @Override
        public Collection<String> getDisabledLoci() {
            return Collections.emptyList();
        }

        @Override
        public boolean isDropoutCompatible(final AnalysisReport currentReport) {
            return false;
        }

        @Override
        public boolean isSensitivityCompatible(final AnalysisReport currentReport) {
            return false;
        }

        @Override
        public void setSensitivityAnalysisResults(final SensitivityAnalysisResults sensitivityAnalysisResults) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getLogfileName() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void setLogfileName(final String name) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }
}