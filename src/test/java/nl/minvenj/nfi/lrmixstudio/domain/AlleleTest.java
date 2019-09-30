/*
 * Copyright (c) 2013, Netherlands Forensic Institute
 * All rights reserved.
 */
package nl.minvenj.nfi.lrmixstudio.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author dejong
 */
public class AlleleTest {

    public AlleleTest() {
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
     * Test of getAllele method, of class Allele.
     */
    @Test
    public void testGetAllele() {
        System.out.println("getAllele");
        final Allele instance = new Allele("1");
        final String expResult = "1";
        final String result = instance.getAllele();
        assertEquals(expResult, result);
    }

    /**
     * Test of getPeak method, of class Allele.
     */
    @Test
    public void testGetPeak() {
        System.out.println("getPeak");
        final Allele instance = new Allele("1", 1.0F);
        final float expResult = 1.0F;
        final float result = instance.getPeak();
        assertEquals(expResult, result, 0.0);
    }

    /**
     * Test of equals method, of class Allele.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");
        final Allele instance = new Allele("12.3");
        assertFalse(instance.equals(null));
        assertTrue(instance.equals(instance));
        assertFalse(instance.equals(new Allele("8")));
        assertTrue(instance.equals(new Allele("12.3")));
    }

    /**
     * Test of setLocus method, of class Allele.
     */
    @Test
    public void testSetLocus() {
        System.out.println("setLocus");
        final Locus locus = new Locus("TestLocus");
        final Allele instance = new Allele("1");
        instance.setLocus(locus);
        assertEquals(locus, instance.getLocus());
    }

    /**
     * Test of getLocus method, of class Allele.
     */
    @Test
    public void testGetLocus() {
        System.out.println("getLocus");
        final Locus locus = new Locus("Testlocus");
        final Allele instance = new Allele("1");
        locus.addAllele(instance);
        final Locus expResult = locus;
        final Locus result = instance.getLocus();
        assertEquals(expResult, result);
    }

    /**
     * Test of isHomozygote method, of class Allele.
     */
    @Test
    public void testIsHomozygote() {
        System.out.println("isHomozygote");
        final Locus locus = new Locus("Dummy");
        final Allele instance = new Allele("1");
        locus.addAllele(instance);
        assertFalse(instance.isHomozygote());
        locus.addAllele(instance);
        assertTrue(instance.isHomozygote());
    }

    /**
     * Test of toString method, of class Allele.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        final Allele instance = new Allele("1");
        final String expResult = "1";
        final String result = instance.toString();
        assertEquals(expResult, result);
    }

    /**
     * Test of hashCode method, of class Allele.
     */
    @Test
    public void testHashCode() {
        System.out.println("hashCode");
        final Allele instance = new Allele("1");
        final int expResult = 210;
        final int result = instance.hashCode();
        assertEquals(expResult, result);
    }

    /**
     * Test of getId method, of class Allele.
     */
    @Test
    public void testGetId_String() {
        System.out.println("getId");
        final int id1 = Allele.getId("11");
        final int id2 = Allele.getId("12");
        assertTrue(id1 != id2);
        final int id3 = Allele.getId("11.0");
        assertEquals(id1, id3);
    }

    /**
     * Test of getRegisteredAlleleCount method, of class Allele.
     */
    @Test
    @Ignore
    public void testGetRegisteredAlleleCount() {
        System.out.println("getRegisteredAlleleCount");
        final int initialCount = Allele.getRegisteredAlleleCount();
        final int id = Allele.getId("13");
        final int updatedCount = Allele.getRegisteredAlleleCount();
        assertTrue(initialCount < updatedCount);
    }

    /**
     * Test of normalize method, of class Allele.
     */
    @Test
    public void testNormalize() {
        System.out.println("normalize");
        assertEquals("11", Allele.normalize("11"));
        assertEquals("11", Allele.normalize("11.0"));
        assertEquals("1", Allele.normalize("1.0"));
        assertEquals("10", Allele.normalize("10"));
        assertEquals("10", Allele.normalize("10.0"));
    }

    /**
     * Test of getId method, of class Allele.
     */
    @Test
    public void testGetId_0args() {
        System.out.println("getId");
        final Allele instance1 = new Allele("14");
        final Allele instance2 = new Allele("15");
        assertTrue(instance1.getId() != instance2.getId());
    }

    @Test
    public void testSetPeak() {
        System.out.println("setPeak");
        final Allele allele = new Allele("14");
        assertEquals(0, allele.getPeak(), 0.00001f);
        allele.setPeak(12345.6f);
        assertEquals(12345.6f, allele.getPeak(), 0.00001f);
    }
}