/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.minvenj.nfi.lrmixstudio.model;

import nl.minvenj.nfi.lrmixstudio.model.splitdrop.SplitDropSuite;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 *
 * @author dejong
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({AnalysisProgressListenerTest.class, ConfigurationDataChangeListenerTest.class, LRMathModelFactoryTest.class, ConfigurationDataTest.class, AnalysisReportTest.class, AnalysisReportImplTest.class, DefaultAnalysisProgressListenerImplTest.class, SplitDropSuite.class, DropoutEstimationTest.class, SensitivityAnalysisResultsTest.class, ConfigurationDataElementTest.class})
public class ModelSuite {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }
    
}