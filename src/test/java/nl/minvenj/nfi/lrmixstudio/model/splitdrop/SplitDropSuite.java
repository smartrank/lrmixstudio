/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.minvenj.nfi.lrmixstudio.model.splitdrop;

import nl.minvenj.nfi.lrmixstudio.model.splitdrop.threadpool.ThreadpoolSuite;
import nl.minvenj.nfi.lrmixstudio.model.splitdrop.validation.ValidationTest;
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
@Suite.SuiteClasses({ThreadpoolSuite.class, PermutationIteratorPlainTest.class, ValidationTest.class})
public class SplitDropSuite {

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