/*
 * Copyright (c) 2013, Netherlands Forensic Institute
 * All rights reserved.
 */
package nl.minvenj.nfi.lrmixstudio.domain;

import nl.minvenj.nfi.lrmixstudio.domain.LocusProbabilities;
import java.util.Collection;
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
public class LocusProbabilitiesTest {

    public LocusProbabilitiesTest() {
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
     * Test of addLocusProbability method, of class ReplicateProbabilities.
     */
    @Test
    public void testAddLocusProbability() {
        System.out.println("addLocusProbability");
        String locus = "Locus1";
        Double probability = 0.1;
        LocusProbabilities instance = new LocusProbabilities();
        instance.addLocusProbability(locus, probability);
    }

    /**
     * Test of getLoci method, of class ReplicateProbabilities.
     */
    @Test
    public void testGetLoci() {
        System.out.println("getLoci");
        LocusProbabilities instance = new LocusProbabilities();
        instance.addLocusProbability("Locus 1", 0.1);
        Collection result = instance.getLoci();
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals("Locus 1", result.toArray()[0]);
    }

    /**
     * Test of getLocusProbability method, of class ReplicateProbabilities.
     */
    @Test
    public void testGetLocusProbability() {
        System.out.println("getLocusProbability");
        LocusProbabilities instance = new LocusProbabilities();
        instance.addLocusProbability("Locus 1", 0.1);
        Double expResult = 0.1;
        Double result = instance.getLocusProbability("Locus 1");
        assertEquals(expResult, result);
    }

    /**
     * Test of getGlobalProbability method, of class ReplicateProbabilities.
     */
    @Test
    public void testGetGlobalProbability() {
        System.out.println("getGlobalProbability");
        LocusProbabilities instance = new LocusProbabilities();
        instance.addLocusProbability("Locus 1", 0.1);
        instance.addLocusProbability("Locus 2", 0.2);
        instance.addLocusProbability("Locus 3", 0.3);
        Double expResult = 0.006;
        Double result = instance.getGlobalProbability();
        assertEquals(expResult, result, 0.0001);
    }

    /**
     * Test of toString method, of class ReplicateProbabilities.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        LocusProbabilities instance = new LocusProbabilities();
        String expResult = "{}";
        String result = instance.toString();
        assertEquals(expResult, result);

        instance.addLocusProbability("Locus 1", 0.1);
        assertEquals("{Locus 1=0.1}", instance.toString());
    }
}