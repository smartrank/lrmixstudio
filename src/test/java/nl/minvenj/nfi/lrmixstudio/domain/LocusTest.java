/*
 * Copyright (c) 2013, Netherlands Forensic Institute
 * All rights reserved.
 */
package nl.minvenj.nfi.lrmixstudio.domain;

import nl.minvenj.nfi.lrmixstudio.domain.Locus;
import nl.minvenj.nfi.lrmixstudio.domain.Sample;
import nl.minvenj.nfi.lrmixstudio.domain.Allele;
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
public class LocusTest {
    
    public LocusTest() {
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
     * Test of getName method, of class Locus.
     */
    @Test
    public void testGetId() {
        System.out.println("getId");
        Locus instance = new Locus("SomeID");
        String expResult = "SomeID";
        String result = instance.getName();
        assertEquals(expResult, result);
    }

    /**
     * Test of addAllele method, of class Locus.
     */
    @Test
    public void testAddAllele() {
        System.out.println("addAllele");
        Allele allele = new Allele("18.2");
        Locus instance = new Locus("SomeID");
        instance.addAllele(allele);
    }

    /**
     * Test of getAlleles method, of class Locus.
     */
    @Test
    public void testGetAlleles() {
        System.out.println("getAlleles");
        Allele allele = new Allele("18.1");
        Locus instance = new Locus("SomeID");
        
        Collection<Allele> alleles = instance.getAlleles();
        assertNotNull(alleles);
        assertEquals(0, alleles.size());

        instance.addAllele(allele);
        alleles = instance.getAlleles();
        assertNotNull(alleles);
        assertEquals(1, alleles.size());
        
        assertTrue(alleles.contains(allele));
    }

    /**
     * Test of size method, of class Locus.
     */
    @Test
    public void testSize() {
        System.out.println("size");
        Allele allele = new Allele("18.2");
        Locus instance = new Locus("SomeID");
        instance.addAllele(allele);
        assertEquals(1, instance.size());
    }

    /**
     * Test of equals method, of class Locus.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");
        Locus instance = new Locus("SomeId");
        Locus other = new Locus("SomeId");
        assertTrue(instance.equals(instance));
        assertTrue(instance.equals(other));
        
        instance.addAllele(new Allele("1"));
        assertFalse(instance.equals(other));
        
        other.addAllele(new Allele("1"));
        assertTrue(instance.equals(other));
    }

    /**
     * Test of hashCode method, of class Locus.
     */
    @Test
    public void testHashCode() {
        System.out.println("hashCode");
        
        Locus instance = new Locus("SomeId");
        Locus other = new Locus("SomeId");
        assertEquals(instance.hashCode(), other.hashCode());
        
        instance.addAllele(new Allele("1"));
        assertFalse(instance.hashCode()==other.hashCode());
        
        other.addAllele(new Allele("1"));
        assertTrue(instance.hashCode()==other.hashCode());
    }

    /**
     * Test of setSample method, of class Locus.
     */
    @Test
    public void testSetSample() {
        System.out.println("setSample");
        Sample sample = new Sample("1");
        Locus instance = new Locus("Locus");
        instance.setSample(sample);
        assertEquals(sample, instance.getSample());
    }

    /**
     * Test of getSample method, of class Locus.
     */
    @Test
    public void testGetSample() {
        System.out.println("getSample");
        Sample sample = new Sample("1");
        Locus instance = new Locus("Locus");
        instance.setSample(sample);
        assertEquals(sample, instance.getSample());
    }

    /**
     * Test of setHomozygote method, of class Locus.
     */
    @Test
    public void testSetHomozygote() {
        System.out.println("setHomozygote");
        Locus instance = new Locus("Locus");
        instance.setHomozygote(true);
        instance.setHomozygote(false);
    }

    /**
     * Test of isHomozygote method, of class Locus.
     */
    @Test
    public void testIsHomozygote() {
        System.out.println("isHomozygote");
        Locus instance = new Locus("Locus1");
        instance.addAllele(new Allele("1"));
        instance.addAllele(new Allele("1"));
        boolean expResult = true;
        boolean result = instance.isHomozygote();
        assertEquals(expResult, result);

        Locus instance2 = new Locus("Locus2");
        instance2.addAllele(new Allele("2"));
        instance2.addAllele(new Allele("3"));
        boolean expResult2 = false;
        boolean result2 = instance2.isHomozygote();
        assertEquals(expResult2, result2);
    }

    /**
     * Test of toString method, of class Locus.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        Locus instance = new Locus("SomeId");
        String expResult = "SomeId";
        String result = instance.toString();
        assertEquals(expResult, result);
    }

    /**
     * Test of getSourceFile method, of class Locus.
     */
    @Test
    public void testGetSourceFile() {
        System.out.println("getSourceFile");
        Locus instance = new Locus("bla");
        String expResult = "sampleFile";
        instance.setSample(new Sample("sample", expResult));
        String result = instance.getSourceFile();
        assertEquals(expResult, result);
    }

    /**
     * Test of getSampleId method, of class Locus.
     */
    @Test
    public void testGetSampleId() {
        System.out.println("getSampleId");
        Locus instance = new Locus("bla");
        instance.setSample(new Sample("sampleId", "sampleFile"));
        String expResult = "sampleFile";
        String result = instance.getSourceFile();
        assertEquals(expResult, result);
    }
}