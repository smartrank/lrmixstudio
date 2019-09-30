/*
 * Copyright (c) 2013, Netherlands Forensic Institute
 * All rights reserved.
 */
package nl.minvenj.nfi.lrmixstudio.domain;

import nl.minvenj.nfi.lrmixstudio.domain.Contributor;
import nl.minvenj.nfi.lrmixstudio.domain.Sample;
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
public class ContributorTest {

    public ContributorTest() {
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
     * Test of getDropOutProbability method, of class Contributor.
     */
    @Test
    public void testGetDropOutProbability() {
        System.out.println("getDropOutProbability");
        Contributor instance = new Contributor(new Sample("Dummy"), 0.1);
        assertEquals(0.1, instance.getDropOutProbability(false), 0.001);
        assertEquals(0.01, instance.getDropOutProbability(true), 0.001);
    }

    /**
     * Test of getSample method, of class Contributor.
     */
    @Test
    public void testGetSample() {
        System.out.println("getSample");
        Sample sample = new Sample("SampleId");
        Contributor instance = new Contributor(sample, 0.1);
        assertEquals(sample, instance.getSample());
    }

    /**
     * Test of getPresentOnceProbability method, of class Contributor.
     */
    @Test
    public void testGetPresentOnceProbability() {
        System.out.println("getPresentOnceProbability");
        Sample sample = new Sample("SampleId");
        Contributor instance = new Contributor(sample, 0.1);
        assertEquals(0.9, instance.getPresentOnceProbability(false), 0.0);
        assertEquals(0.99, instance.getPresentOnceProbability(true), 0.0);
    }

    /**
     * Test of getPresentMultipleProbability method, of class Contributor.
     */
    @Test
    public void testGetPresentMultipleProbability() {
        System.out.println("getPresentMultipleProbability");
        Sample sample = new Sample("SampleId");
        Contributor instance = new Contributor(sample, 0.1);
        assertEquals(0.1, instance.getPresentMultipleProbability(false), 0.01);
        assertEquals(0.01, instance.getPresentMultipleProbability(true), 0.01);
    }

    /**
     * Test of setDropoutProbability method, of class Contributor.
     */
    @Test
    public void testSetDropoutProbability() {
        System.out.println("setDropoutProbability");
        Sample sample = new Sample("SampleId");
        double dropout = 0.12;
        Contributor instance = new Contributor(sample, 0);
        instance.setDropoutProbability(dropout);
        assertEquals(dropout, instance.getDropoutProbability(), 0.01);
    }

    /**
     * Test of getDropoutProbability method, of class Contributor.
     */
    @Test
    public void testGetDropoutProbability() {
        System.out.println("getDropoutProbability");
        Sample sample = new Sample("SampleId");
        double expResult = 0.0;
        Contributor instance = new Contributor(sample, 0);
        double result = instance.getDropoutProbability();
        assertEquals(expResult, result, 0.0);
    }

    /**
     * Test of toString method, of class Contributor.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        Sample sample = new Sample("SampleId");
        Contributor instance = new Contributor(sample, 0);
        String result = instance.toString();
        assertEquals("SampleId(0.00)", result);
    }
}