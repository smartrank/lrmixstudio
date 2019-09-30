/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.minvenj.nfi.lrmixstudio.model;

import nl.minvenj.nfi.lrmixstudio.model.LRMathModelFactory;
import nl.minvenj.nfi.lrmixstudio.model.LRMathModel;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author dejong
 */
public class LRMathModelFactoryTest {

    public LRMathModelFactoryTest() {
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
     * Test of getAllModelNames method, of class LRMathModelFactory.
     */
    @Test
    public void testGetAllModelNames() {
        System.out.println("getAllModelNames");
        Iterable result = LRMathModelFactory.getAllModelNames();
        assertNotNull(result);
        assertTrue(result.iterator().hasNext());
    }

    /**
     * Test of getDefaultModelName method, of class LRMathModelFactory.
     */
    @Test
    public void testGetDefaultModelName() {
        System.out.println("getDefaultModelName");
        String expResult = "SplitDrop Executor Edition";
        String result = LRMathModelFactory.getDefaultModelName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getMathematicalModel method, of class LRMathModelFactory.
     */
    @Test
    public void testGetMathematicalModel() throws Exception {
        System.out.println("getMathematicalModel");
        LRMathModel result = LRMathModelFactory.getMathematicalModel(LRMathModelFactory.getDefaultModelName());
        assertNotNull(result);
    }
}