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
        AnalysisReport instance = new AnalysisReportImpl();
        LikelihoodRatio expResult = null;
        LikelihoodRatio result = instance.getLikelihoodRatio();
        assertEquals(expResult, result);
    }

    /**
     * Test of getDefenseHypothesis method, of class AnalysisReport.
     */
    @Test
    public void testGetDefenseHypothesis() {
        System.out.println("getDefenseHypothesis");
        AnalysisReport instance = new AnalysisReportImpl();
        Hypothesis expResult = null;
        Hypothesis result = instance.getDefenseHypothesis();
        assertEquals(expResult, result);
    }

    /**
     * Test of getProsecutionHypothesis method, of class AnalysisReport.
     */
    @Test
    public void testGetProsecutionHypothesis() {
        System.out.println("getProsecutionHypothesis");
        AnalysisReport instance = new AnalysisReportImpl();
        Hypothesis expResult = null;
        Hypothesis result = instance.getProsecutionHypothesis();
        assertEquals(expResult, result);
    }

    /**
     * Test of getStartTime method, of class AnalysisReport.
     */
    @Test
    public void testGetStartTime() {
        System.out.println("getStartTime");
        AnalysisReport instance = new AnalysisReportImpl();
        long expResult = 0L;
        long result = instance.getStartTime();
        assertEquals(expResult, result);
    }

    /**
     * Test of getStopTime method, of class AnalysisReport.
     */
    @Test
    public void testGetStopTime() {
        System.out.println("getStopTime");
        AnalysisReport instance = new AnalysisReportImpl();
        long expResult = 0L;
        long result = instance.getStopTime();
        assertEquals(expResult, result);
    }

    /**
     * Test of isSucceeded method, of class AnalysisReport.
     */
    @Test
    public void testIsSucceeded() {
        System.out.println("isSucceeded");
        AnalysisReport instance = new AnalysisReportImpl();
        boolean expResult = false;
        boolean result = instance.isSucceeded();
        assertEquals(expResult, result);
    }

    /**
     * Test of getException method, of class AnalysisReport.
     */
    @Test
    public void testGetException() {
        System.out.println("getException");
        AnalysisReport instance = new AnalysisReportImpl();
        Exception expResult = null;
        Exception result = instance.getException();
        assertEquals(expResult, result);
    }

    /**
     * Test of getCaseNumber method, of class AnalysisReport.
     */
    @Test
    public void testGetCaseNumber() {
        System.out.println("getCaseNumber");
        AnalysisReport instance = new AnalysisReportImpl();
        String expResult = "";
        String result = instance.getCaseNumber();
        assertEquals(expResult, result);
    }

    /**
     * Test of getProgramVersion method, of class AnalysisReport.
     */
    @Test
    public void testGetProgramVersion() {
        System.out.println("getProgramVersion");
        AnalysisReport instance = new AnalysisReportImpl();
        String expResult = "";
        String result = instance.getProgramVersion();
        assertEquals(expResult, result);
    }

    /**
     * Test of getSensitivityAnalysisResults method, of class AnalysisReport.
     */
    @Test
    public void testGetSensitivityAnalysisResults() {
        System.out.println("getSensitivityAnalysisResults");
        AnalysisReport instance = new AnalysisReportImpl();
        SensitivityAnalysisResults expResult = null;
        SensitivityAnalysisResults result = instance.getSensitivityAnalysisResults();
        assertEquals(expResult, result);
    }

    /**
     * Test of getPopulationStatistics method, of class AnalysisReport.
     */
    @Test
    public void testGetPopulationStatistics() {
        System.out.println("getPopulationStatistics");
        AnalysisReport instance = new AnalysisReportImpl();
        PopulationStatistics expResult = null;
        PopulationStatistics result = instance.getPopulationStatistics();
        assertEquals(expResult, result);
    }

    /**
     * Test of getReplicates method, of class AnalysisReport.
     */
    @Test
    public void testGetReplicates() {
        System.out.println("getReplicates");
        AnalysisReport instance = new AnalysisReportImpl();
        Collection expResult = null;
        Collection result = instance.getReplicates();
        assertEquals(expResult, result);
    }

    /**
     * Test of getProfiles method, of class AnalysisReport.
     */
    @Test
    public void testGetProfiles() {
        System.out.println("getProfiles");
        AnalysisReport instance = new AnalysisReportImpl();
        Collection expResult = null;
        Collection result = instance.getProfiles();
        assertEquals(expResult, result);
    }

    /**
     * Test of getGuid method, of class AnalysisReport.
     */
    @Test
    public void testGetGuid() {
        System.out.println("getGuid");
        AnalysisReport instance = new AnalysisReportImpl();
        int expResult = 0;
        int result = instance.getGuid();
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
        public void addProcessingTime(long processingTime) {
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
        public boolean isDropoutCompatible(AnalysisReport currentReport) {
            return false;
        }

        @Override
        public boolean isSensitivityCompatible(AnalysisReport currentReport) {
            return false;
        }

        @Override
        public void setSensitivityAnalysisResults(SensitivityAnalysisResults sensitivityAnalysisResults) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getLogfileName() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void setLogfileName(String name) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }
}