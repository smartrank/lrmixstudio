/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.minvenj.nfi.lrmixstudio;

import nl.minvenj.nfi.lrmixstudio.domain.DomainSuite;
import nl.minvenj.nfi.lrmixstudio.io.IoSuite;
import nl.minvenj.nfi.lrmixstudio.model.ModelSuite;
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
@Suite.SuiteClasses({DomainSuite.class, IoSuite.class, ModelSuite.class})
public class LrmixStudioSuite {

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