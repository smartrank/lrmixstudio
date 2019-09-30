/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.minvenj.nfi.lrmixstudio.model;

import nl.minvenj.nfi.lrmixstudio.model.ConfigurationDataElement;
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
public class ConfigurationDataElementTest {

    public ConfigurationDataElementTest() {
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
     * Test of values method, of class ConfigurationDataElement.
     */
    @Test
    public void testValues() {
        System.out.println("values");
        ConfigurationDataElement[] result = ConfigurationDataElement.values();
        assertTrue(result.length > 0);
    }

    /**
     * Test of valueOf method, of class ConfigurationDataElement.
     */
    @Test
    public void testValueOf() {
        System.out.println("valueOf");
        String name = "ACTIVELOCI";
        ConfigurationDataElement expResult = ConfigurationDataElement.ACTIVELOCI;
        ConfigurationDataElement result = ConfigurationDataElement.valueOf(name);
        assertEquals(expResult, result);
    }
}