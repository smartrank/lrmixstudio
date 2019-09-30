/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.minvenj.nfi.lrmixstudio.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import static nl.minvenj.nfi.lrmixstudio.model.SensitivityAnalysisResults.RangeType.LR;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import nl.minvenj.nfi.lrmixstudio.model.SensitivityAnalysisResults.Point;
import nl.minvenj.nfi.lrmixstudio.model.SensitivityAnalysisResults.Range;

/**
 *
 * @author dejong
 */
public class SensitivityAnalysisResultsTest {

    public SensitivityAnalysisResultsTest() {
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
     * Test of getRanges method, of class SensitivityAnalysisResults.
     */
    @Test
    public void testGetRanges() {
        System.out.println("getRanges");
        SensitivityAnalysisResults instance = new SensitivityAnalysisResults();
        Collection result = instance.getRanges();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Test of setDropoutEstimation method, of class SensitivityAnalysisResults.
     */
    @Test
    public void testAddDropoutEstimation() {
        System.out.println("addDropoutEstimation");
        DropoutEstimation dropoutEstimation = new DropoutEstimation();
        SensitivityAnalysisResults instance = new SensitivityAnalysisResults();
        instance.setDropoutEstimation(dropoutEstimation);
        assertEquals(dropoutEstimation, instance.getDropoutEstimation());
    }

    /**
     * Test of getDropoutEstimation method, of class SensitivityAnalysisResults.
     */
    @Test
    public void testGetDropoutEstimation() {
        System.out.println("getDropoutEstimation");
        SensitivityAnalysisResults instance = new SensitivityAnalysisResults();
        DropoutEstimation expResult = null;
        DropoutEstimation result = instance.getDropoutEstimation();
        assertEquals(expResult, result);
    }

    /**
     * Test of toString method, of class SensitivityAnalysisResults.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        SensitivityAnalysisResults instance = new SensitivityAnalysisResults();
        String expResult = "Not performed. No Dropout Estimation.";
        String result = instance.toString();
        assertEquals(expResult, result);
    }

    /**
     * Test of addRange method, of class SensitivityAnalysisResults.
     */
    @Test
    public void testAddRange_String_doubleArrArr() {
        System.out.println("addRange");
        String rangeId = "rangeName";
        double[][] values = new double[][]{{1f, 2f, 3f}, {3f, 2f, 1f}};
        SensitivityAnalysisResults instance = new SensitivityAnalysisResults();
        instance.addRange(LR, rangeId, values);
        assertEquals(1, instance.getRanges().size());
    }

    /**
     * Test of addRange method, of class SensitivityAnalysisResults.
     */
    @Test
    public void testAddRange_String_Collection() {
        System.out.println("addRange");
        String rangeId = "RangeName";
        Collection<SensitivityAnalysisResults.Point> values = new ArrayList<>();
        values.add(new Point(null, BigDecimal.ZERO, BigDecimal.ONE));
        values.add(new Point(null, BigDecimal.ONE, BigDecimal.TEN));
        values.add(new Point(null, BigDecimal.TEN, BigDecimal.ZERO));
        SensitivityAnalysisResults instance = new SensitivityAnalysisResults();
        instance.addRange(LR, rangeId, values);
        assertEquals(1, instance.getRanges().size());
    }

    /**
     * Test of addRange method, of class SensitivityAnalysisResults.
     */
    @Test
    public void testAddRange_SensitivityAnalysisResultsRange() {
        System.out.println("addRange");
        SensitivityAnalysisResults instance = new SensitivityAnalysisResults();
        Range range = new Range(0, LR, "RangeName", new double[][]{{1f, 2f, 3f}, {3f, 2f, 1f}});
        instance.addRange(range);
        assertEquals(1, instance.getRanges().size());
    }

    /**
     * Test of deleteRangeById method, of class SensitivityAnalysisResults.
     */
    @Test
    public void testDeleteRange() {
        System.out.println("deleteRange");
        SensitivityAnalysisResults instance = new SensitivityAnalysisResults();
        instance.addRange(LR, "RangeName", new double[][]{{1f, 2f, 3f}, {3f, 2f, 1f}});
        instance.deleteRangeById("RangeName");
        assertTrue(instance.getRanges().isEmpty());
    }
}