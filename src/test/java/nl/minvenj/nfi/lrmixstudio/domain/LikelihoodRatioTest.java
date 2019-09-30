/*
 * Copyright (c) 2013, Netherlands Forensic Institute
 * All rights reserved.
 */
package nl.minvenj.nfi.lrmixstudio.domain;

import nl.minvenj.nfi.lrmixstudio.domain.LocusProbabilities;
import nl.minvenj.nfi.lrmixstudio.domain.LikelihoodRatio;
import java.util.Collection;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author dejong
 */
public class LikelihoodRatioTest {

    public LikelihoodRatioTest() {
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
     * Test of add method, of class LikelihoodRatio.
     */
    @Test
    public void testAdd() {
        System.out.println("add");
        LocusProbabilities prosecution = new LocusProbabilities();
        LocusProbabilities defense = new LocusProbabilities();
        LikelihoodRatio instance = new LikelihoodRatio();
        instance.add(prosecution, defense);
    }

    /**
     * Test of getLoci method, of class LikelihoodRatio.
     */
    @Test
    public void testGetLoci() {
        System.out.println("getLoci");
        LocusProbabilities prosecution = new LocusProbabilities();
        prosecution.addLocusProbability("SomeLocus", 0.1);

        LocusProbabilities defense = new LocusProbabilities();
        defense.addLocusProbability("SomeLocus", 0.1);

        LikelihoodRatio instance = new LikelihoodRatio();
        instance.add(prosecution, defense);

        assertEquals("SomeLocus", instance.getLoci().iterator().next());
    }

    /**
     * Test of getRatio method, of class LikelihoodRatio.
     */
    @Test
    public void testGetRatio() {
        System.out.println("getRatio");
        LocusProbabilities prosecution = new LocusProbabilities();
        prosecution.addLocusProbability("SomeLocus", 0.1);

        LocusProbabilities defense = new LocusProbabilities();
        defense.addLocusProbability("SomeLocus", 0.2);

        LikelihoodRatio instance = new LikelihoodRatio();
        instance.add(prosecution, defense);

        assertEquals(0.5, instance.getRatio("SomeLocus").getRatio(), 0.0);
    }

    /**
     * Test of getOverallRatio method, of class LikelihoodRatio.
     */
    @Test
    public void testGetOverallRatio() {
        System.out.println("getRatio");
        LocusProbabilities prosecution = new LocusProbabilities();
        prosecution.addLocusProbability("Locus1", 0.1);
        prosecution.addLocusProbability("Locus2", 0.1);

        LocusProbabilities defense = new LocusProbabilities();
        defense.addLocusProbability("Locus1", 0.2);
        defense.addLocusProbability("Locus2", 0.4);

        LikelihoodRatio instance = new LikelihoodRatio();
        instance.add(prosecution, defense);

        assertEquals(0.125, instance.getOverallRatio().getRatio(), 0.0);
    }

    /**
     * Test of getRatios method, of class LikelihoodRatio.
     */
    @Test
    public void testGetRatios() {
        System.out.println("getRatios");
        LikelihoodRatio instance = new LikelihoodRatio();
        Collection result = instance.getRatios();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}