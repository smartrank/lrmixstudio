/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.minvenj.nfi.lrmixstudio.model;

import nl.minvenj.nfi.lrmixstudio.model.DropoutEstimation;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author dejong
 */
public class DropoutEstimationTest {

    public DropoutEstimationTest() {
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
     * Test of setMaximum method, of class DropoutEstimation.
     */
    @Test
    public void testSetValues() {
        System.out.println("setValues");
        BigDecimal maximum = BigDecimal.ONE;
        BigDecimal minimum = BigDecimal.ZERO;
        DropoutEstimation instance = new DropoutEstimation();
        instance.setValues("Prosecution", minimum, maximum);
    }

    /**
     * Test of getMaximum method, of class DropoutEstimation.
     */
    @Test
    public void testGetMaximum() {
        System.out.println("getMaximum2");
        BigDecimal maximum = new BigDecimal(1.00);
        BigDecimal minimum = new BigDecimal(0.00);
        DropoutEstimation instance = new DropoutEstimation();
        instance.setValues("dummy", minimum, maximum);
        assertEquals("1.00", instance.getMaximum().toString());
    }

    /**
     * Test of getMinimum method, of class DropoutEstimation.
     */
    @Test
    public void testGetMinimum() {
        System.out.println("getMinimum2");
        BigDecimal maximum = new BigDecimal(1.00).setScale(2, RoundingMode.HALF_UP);
        BigDecimal minimum = new BigDecimal(0.00).setScale(2, RoundingMode.HALF_UP);
        DropoutEstimation instance = new DropoutEstimation();
        instance.setValues("dummy", minimum, maximum);
        assertEquals("0.00", instance.getMinimum().toString());
    }

    /**
     * Test of equals method, of class DropoutEstimation.
     */
//    @Test
//    public void testEquals() {
//        System.out.println("equals");
//        DropoutEstimation instance1 = new DropoutEstimation();
//        instance1.setMinimum(BigDecimal.ZERO);
//        instance1.setMaximum(BigDecimal.ONE);
//        DropoutEstimation instance2 = new DropoutEstimation();
//        instance2.setMinimum(BigDecimal.ZERO);
//        instance2.setMaximum(BigDecimal.ONE);
//        DropoutEstimation instance3 = new DropoutEstimation();
//        instance3.setMinimum(BigDecimal.ONE);
//        instance3.setMaximum(BigDecimal.TEN);
//        assertTrue(instance1.equals(instance2));
//        assertFalse(instance1.equals(instance3));
//    }

    /**
     * Test of hashCode method, of class DropoutEstimation.
     */
//    @Test
//    public void testHashCode() {
//        System.out.println("hashCode");
//        DropoutEstimation instance = new DropoutEstimation();
//        instance.setMinimum(BigDecimal.ONE);
//        instance.setMaximum(BigDecimal.TEN);
//        int expResult = 50362;
//        int result = instance.hashCode();
//        assertEquals(expResult, result);
//    }

    /**
     * Test of toString method, of class DropoutEstimation.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        DropoutEstimation instance = new DropoutEstimation();
        String expResult = "Min: 1 Max: 0";
        String result = instance.toString();
        assertEquals(expResult, result);
    }
}