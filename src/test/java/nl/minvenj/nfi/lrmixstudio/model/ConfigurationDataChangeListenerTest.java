/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.minvenj.nfi.lrmixstudio.model;

import nl.minvenj.nfi.lrmixstudio.model.ConfigurationDataChangeListener;
import nl.minvenj.nfi.lrmixstudio.model.ConfigurationDataElement;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author dejong
 */
public class ConfigurationDataChangeListenerTest {

    public ConfigurationDataChangeListenerTest() {
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
     * Test of dataChanged method, of class ConfigurationDataChangeListener.
     */
    @Test
    public void testDataChanged() {
        System.out.println("dataChanged");
        ConfigurationDataElement element = ConfigurationDataElement.PROFILES;
        ConfigurationDataChangeListener instance = new ConfigurationDataChangeListenerImpl();
        instance.dataChanged(element);
    }

    public class ConfigurationDataChangeListenerImpl implements ConfigurationDataChangeListener {

        public void dataChanged(ConfigurationDataElement element) {
        }
    }
}