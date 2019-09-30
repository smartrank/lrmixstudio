/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.minvenj.nfi.lrmixstudio.model;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import nl.minvenj.nfi.lrmixstudio.domain.Hypothesis;
import nl.minvenj.nfi.lrmixstudio.domain.Locus;
import nl.minvenj.nfi.lrmixstudio.domain.PopulationStatistics;
import nl.minvenj.nfi.lrmixstudio.domain.Sample;

/**
 *
 * @author dejong
 */
public class ConfigurationDataTest {

    public ConfigurationDataTest() {
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
     * Test of addDataChangeListener method, of class ConfigurationData.
     */
    @Test
    public void testAddDataChangeListener() {
        System.out.println("addDataChangeListener");
        ConfigurationDataChangeListener listener = new ConfigurationDataChangeListener() {
            @Override
            public void dataChanged(ConfigurationDataElement element) {
            }
        };

        ConfigurationData instance = new ConfigurationData();
        instance.addDataChangeListener(listener);
    }

    /**
     * Test of getDefense method, of class ConfigurationData.
     */
    @Test
    public void testGetDefense() {
        System.out.println("getDefense");
        ConfigurationData instance = new ConfigurationData();
        Hypothesis expResult = new Hypothesis("Test", new PopulationStatistics(""));
        instance.setDefense(expResult);
        Hypothesis result = instance.getDefense();
        assertEquals(expResult, result);
    }

    /**
     * Test of setDefense method, of class ConfigurationData.
     */
    @Test
    public void testSetDefense() {
        System.out.println("setDefense");
        ConfigurationData instance = new ConfigurationData();
        Hypothesis expResult = new Hypothesis("Test", new PopulationStatistics(""));
        instance.setDefense(expResult);
    }

    /**
     * Test of getAllProfiles method, of class ConfigurationData.
     */
    @Test
    public void testGetAllProfiles() {
        System.out.println("getAllProfiles");
        ConfigurationData instance = new ConfigurationData();
        Collection<Sample> expResult = new ArrayList<>();
        expResult.add(new Sample("s1"));
        expResult.add(new Sample("s2"));
        instance.addProfiles(expResult);
        Collection result = instance.getAllProfiles();
        assertNotNull(result);
        assertArrayEquals(expResult.toArray(), result.toArray());
    }

    /**
     * Test of getActiveProfiles method, of class ConfigurationData.
     */
    @Test
    public void testGetActiveProfiles() {
        System.out.println("getActiveProfiles");
        ConfigurationData instance = new ConfigurationData();
        Collection<Sample> expResult = new ArrayList<>();
        Sample s1 = new Sample("s1");
        Sample s2 = new Sample("s2");
        expResult.add(s1);
        expResult.add(s2);
        instance.addProfiles(expResult);
        s1.setEnabled(false);

        Collection result = instance.getActiveProfiles();
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    /**
     * Test of addProfiles method, of class ConfigurationData.
     */
    @Test
    public void testSetProfiles() {
        System.out.println("setProfiles");
        ConfigurationData instance = new ConfigurationData();
        Collection<Sample> expResult = new ArrayList<>();
        expResult.add(new Sample("s1"));
        expResult.add(new Sample("s2"));
        instance.addProfiles(expResult);
    }

    /**
     * Test of getProsecution method, of class ConfigurationData.
     */
    @Test
    public void testGetProsecution() {
        System.out.println("getProsecution");
        ConfigurationData instance = new ConfigurationData();
        Hypothesis expResult = new Hypothesis("Test", new PopulationStatistics(""));
        instance.setProsecution(expResult);
        assertEquals(expResult, instance.getProsecution());
    }

    /**
     * Test of setProsecution method, of class ConfigurationData.
     */
    @Test
    public void testSetProsecution() {
        System.out.println("setProsecution");
        ConfigurationData instance = new ConfigurationData();
        Hypothesis expResult = new Hypothesis("Test", new PopulationStatistics(""));
        instance.setProsecution(expResult);
    }

    /**
     * Test of getActiveReplicates method, of class ConfigurationData.
     */
    @Test
    public void testGetActiveReplicates() {
        System.out.println("getActiveReplicates");
        ConfigurationData instance = new ConfigurationData();
        Collection<Sample> expResult = new ArrayList<>();
        Sample s1 = new Sample("s1");
        Sample s2 = new Sample("s2");
        expResult.add(s1);
        expResult.add(s2);
        instance.addReplicates(expResult);
        s1.setEnabled(false);

        Collection result = instance.getActiveReplicates();
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    /**
     * Test of getAllReplicates method, of class ConfigurationData.
     */
    @Test
    public void testGetAllReplicates() {
        System.out.println("getAllReplicates");
        ConfigurationData instance = new ConfigurationData();
        Collection<Sample> expResult = new ArrayList<>();
        Sample s1 = new Sample("s1");
        Sample s2 = new Sample("s2");
        expResult.add(s1);
        expResult.add(s2);
        instance.addReplicates(expResult);

        Collection result = instance.getActiveReplicates();
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    /**
     * Test of addReplicates method, of class ConfigurationData.
     */
    @Test
    public void testSetReplicates() {
        System.out.println("setReplicates");
        ConfigurationData instance = new ConfigurationData();
        Collection<Sample> expResult = new ArrayList<>();
        Sample s1 = new Sample("s1");
        Sample s2 = new Sample("s2");
        expResult.add(s1);
        expResult.add(s2);
        instance.addReplicates(expResult);
    }

    /**
     * Test of getStatistics method, of class ConfigurationData.
     */
    @Test
    public void testGetStatistics() {
        System.out.println("getStatistics");
        ConfigurationData instance = new ConfigurationData();
        PopulationStatistics expResult = new PopulationStatistics("PopulationStatistics");
        instance.setStatistics(expResult);
        PopulationStatistics result = instance.getStatistics();
        assertEquals(expResult, result);
    }

    /**
     * Test of setStatistics method, of class ConfigurationData.
     */
    @Test
    public void testSetStatistics() {
        System.out.println("setStatistics");
        ConfigurationData instance = new ConfigurationData();
        PopulationStatistics expResult = new PopulationStatistics("PopulationStatistics");
        instance.setStatistics(expResult);
    }

    /**
     * Test of getCaseNumber method, of class ConfigurationData.
     */
    @Test
    public void testGetCaseNumber() {
        System.out.println("getCaseNumber");
        ConfigurationData instance = new ConfigurationData();
        String expResult = "1234";
        instance.setCaseNumber(expResult);
        String result = instance.getCaseNumber();
        assertEquals(expResult, result);
    }

    /**
     * Test of getProgramVersion method, of class ConfigurationData.
     */
    @Test
    public void testGetProgramVersion() {
        System.out.println("getProgramVersion");
        ConfigurationData instance = new ConfigurationData();
        String expResult = "4321";
        instance.setProgramVersion(expResult);
        String result = instance.getProgramVersion();
        assertEquals(expResult, result);
    }

    /**
     * Test of setCaseNumber method, of class ConfigurationData.
     */
    @Test
    public void testSetCaseNumber() {
        System.out.println("setCaseNumber");
        String caseNumber = "";
        ConfigurationData instance = new ConfigurationData();
        instance.setCaseNumber(caseNumber);
    }

    /**
     * Test of setProgramVersion method, of class ConfigurationData.
     */
    @Test
    public void testSetProgramVersion() {
        System.out.println("setProgramVersion");
        String programVersion = "";
        ConfigurationData instance = new ConfigurationData();
        instance.setProgramVersion(programVersion);
    }

    /**
     * Test of clear method, of class ConfigurationData.
     */
    @Test
    public void testClear() {
        System.out.println("clear");
        ConfigurationData instance = new ConfigurationData();
        Collection<Sample> expResult = new ArrayList<>();
        Sample s1 = new Sample("s1");
        Sample s2 = new Sample("s2");
        expResult.add(s1);
        expResult.add(s2);
        instance.addProfiles(expResult);
        instance.clear();
        Collection<Sample> result = instance.getAllProfiles();
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    /**
     * Test of getMathematicalModelName method, of class ConfigurationData.
     */
    @Test
    public void testGetMathematicalModelName() {
        System.out.println("getMathematicalModelName");
        ConfigurationData instance = new ConfigurationData();
        String expResult = "ModelName";
        instance.setMathematicalModelName(expResult);
        String result = instance.getMathematicalModelName();
        assertEquals(expResult, result);
    }

    /**
     * Test of setMathematicalModelName method, of class ConfigurationData.
     */
    @Test
    public void testSetMathematicalModelName() {
        System.out.println("setMathematicalModelName");
        ConfigurationData instance = new ConfigurationData();
        String expResult = "ModelName";
        instance.setMathematicalModelName(expResult);
    }

    /**
     * Test of fireUpdated method, of class ConfigurationData.
     */
    @Test
    public void testFireUpdated() {
        System.out.println("fireUpdated");
        ConfigurationData instance = new ConfigurationData();
        final ConfigurationDataElement element = ConfigurationDataElement.CASENUMBER;
        instance.addDataChangeListener(new ConfigurationDataChangeListener() {
            @Override
            public void dataChanged(ConfigurationDataElement e) {
                if (element != e) {
                    throw new RuntimeException("Element not equals to e");
                }
            }
        });
        try {
            instance.fireUpdated(ConfigurationDataElement.ACTIVELOCI);
            fail("Expected exception not thrown");
        } catch (Exception e) {
            // Do nothing. This is expected
        }
        instance.fireUpdated(element);
    }

    /**
     * Test of getEnabledLoci method, of class ConfigurationData.
     */
    @Test
    public void testGetEnabledLoci() {
        System.out.println("getEnabledLoci");
        ConfigurationData instance = new ConfigurationData();
        assertEquals(0, instance.getEnabledLoci().size());
        Sample s = new Sample("DummySample");
        s.addLocus(new Locus("VWA"));
        instance.addReplicates(Arrays.asList(s));
        assertEquals(1, instance.getEnabledLoci().size());
        instance.setLocusEnabled("VWA", false);
        assertEquals(0, instance.getEnabledLoci().size());
        assertEquals(1, instance.getAllReplicates().size());
    }

    /**
     * Test of getAllLoci method, of class ConfigurationData.
     */
    @Test
    public void testGetAllLoci() {
        System.out.println("getAllLoci");
        ConfigurationData instance = new ConfigurationData();
        Collection result = instance.getEnabledLoci();
        assertEquals(0, result.size());
        Sample s = new Sample("TestSample");
        s.addLocus(new Locus("1"));
        s.addLocus(new Locus("2"));
        s.addLocus(new Locus("3"));
        instance.addReplicates(Arrays.asList(s));
        result = instance.getEnabledLoci();
        assertEquals(3, result.size());
    }

    /**
     * Test of setLocusEnabled method, of class ConfigurationData.
     */
    @Test
    public void testSetLocusEnabled() {
        System.out.println("setLocusEnabled");
        ConfigurationData instance = new ConfigurationData();
        instance.setLocusEnabled("FGA", false);
    }

    /**
     * Test of setLocusEnabled method, of class ConfigurationData.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testSetLocusEnabledForNonexistantLocus() {
        System.out.println("setLocusEnabled");
        ConfigurationData instance = new ConfigurationData();
        instance.setLocusEnabled("Non-existant locus name", true);
    }

    /**
     * Test of isLocusEnabled method, of class ConfigurationData.
     */
    @Test
    public void testIsLocusEnabled() throws IOException {
        System.out.println("isLocusEnabled");
        ConfigurationData instance = new ConfigurationData();
        assertFalse(instance.isLocusEnabled("FGA"));
    }

    /**
     * Test of isLocusValid method, of class ConfigurationData.
     */
    @Test
    public void testIsLocusValid() {
        System.out.println("isLocusValid");
        ConfigurationData instance = new ConfigurationData();
        assertFalse(instance.isLocusValid("FGA"));
        Sample s = new Sample("DummySample");
        s.addLocus(new Locus("FGA"));
        instance.addReplicates(Arrays.asList(s));
        assertTrue(instance.isLocusValid("FGA"));
        assertFalse(instance.isLocusValid("UnknownValue"));
    }

    /**
     * Test of getObservedAlleleCount method, of class ConfigurationData.
     */
    @Test
    public void testGetObservedAlleleCount() {
        System.out.println("getObservedAlleleCount");
        ConfigurationData instance = new ConfigurationData();
        Collection<Sample> expResult = new ArrayList<>();
        Sample s1 = new Sample("s1");
        Sample s2 = new Sample("s2");
        expResult.add(s1);
        expResult.add(s2);
        instance.addReplicates(expResult);
        int result = instance.getObservedAlleleCount();
        assertEquals(0, result);
    }

    /**
     * Test of addReport method, of class ConfigurationData.
     */
    @Test
    public void testAddReport() {
        System.out.println("addReport");
        AnalysisReportImpl report = new AnalysisReportImpl(new ConfigurationData());

        PopulationStatistics popStats = new PopulationStatistics("bla");
        report.setDefenseHypothesis(new Hypothesis("Defense", popStats));
        report.setProsecutionHypothesis(new Hypothesis("Prosecution", popStats));

        ConfigurationData instance = new ConfigurationData();
        instance.addReport(report);
    }

    /**
     * Test of getCurrentReport method, of class ConfigurationData.
     */
    @Test
    public void testGetCurrentReport() {
        System.out.println("getCurrentReport");
        ConfigurationData instance = new ConfigurationData();
        PopulationStatistics popStats = new PopulationStatistics("bla");
        instance.setDefense(new Hypothesis("Defense", popStats));
        instance.setProsecution(new Hypothesis("Prosecution", popStats));

        AnalysisReport result = instance.getCurrentReport();
        assertNotNull(result);
    }

    /**
     * Test of getReports method, of class ConfigurationData.
     */
    @Test
    public void testGetReports() {
        System.out.println("getReports");
        ConfigurationData instance = new ConfigurationData();
        Collection result = instance.getReports();
        assertNotNull(result);
        assertEquals(0, result.size());
    }
}