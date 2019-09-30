/*
 * Copyright (c) 2013, Netherlands Forensic Institute
 * All rights reserved.
 */
package nl.minvenj.nfi.lrmixstudio.domain;

import nl.minvenj.nfi.lrmixstudio.domain.Ratio;
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
public class RatioTest {

    public RatioTest() {
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
     * Test of getLocusName method, of class Ratio.
     */
    @Test
    public void testGetLocusName() {
        System.out.println("getLocusName");
        Ratio instance = new Ratio("id", 0.3, 0.1);
        String expResult = "id";
        String result = instance.getLocusName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getDefenseProbability method, of class Ratio.
     */
    @Test
    public void testGetDefenseProbability() {
        System.out.println("getDefenseProbability");
        Ratio instance = new Ratio("id", 0.3, 0.1);
        double expResult = 0.1;
        double result = instance.getDefenseProbability();
        assertEquals(expResult, result, 0.01);
    }

    /**
     * Test of getProsecutionProbability method, of class Ratio.
     */
    @Test
    public void testGetProsecutionProbability() {
        System.out.println("getProsecutionProbability");
        Ratio instance = new Ratio("id", 0.3, 0.1);
        double expResult = 0.3;
        double result = instance.getProsecutionProbability();
        assertEquals(expResult, result, 0.01);
    }

    /**
     * Test of getRatio method, of class Ratio.
     */
    @Test
    public void testGetRatio() {
        System.out.println("getRatio");
        Ratio instance = new Ratio("id", 0.3, 0.1);
        Double expResult = 3.0;
        Double result = instance.getRatio();
        assertEquals(expResult, result, 0.01);
    }

    /**
     * Test of toString method, of class Ratio.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        Ratio instance = new Ratio("id", 0.3, 0.1);
        String expResult = "id: 0.3 / 0.1 = 3";
        String result = instance.toString();
        assertEquals(expResult, result);
    }
}