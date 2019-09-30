/*
 * Copyright (c) 2013, Netherlands Forensic Institute
 * All rights reserved.
 */
package nl.minvenj.nfi.lrmixstudio.domain;

import nl.minvenj.nfi.lrmixstudio.domain.Locus;
import nl.minvenj.nfi.lrmixstudio.domain.Sample;
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
public class SampleTest {
    
    public SampleTest() {
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
     * Test of addLocus method, of class Sample.
     */
    @Test
    public void testAddLocus() {
        System.out.println("addLocus");
        Locus locus = new Locus("TestLocus");
        Sample instance = new Sample("TestSample");
        instance.addLocus(locus);
    }

    /**
     * Test of getLoci method, of class Sample.
     */
    @Test
    public void testGetLoci() {
        System.out.println("getLoci");
        
        Sample instance = new Sample("TestSample");
        Collection<Locus> result = instance.getLoci();

        assertNotNull(result);
        
        Locus locus = new Locus("TestLocus");
        instance.addLocus(locus);

        result = instance.getLoci();
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    /**
     * Test of size method, of class Sample.
     */
    @Test
    public void testSize() {
        System.out.println("size");
        Sample instance = new Sample("TestSample");
        
        assertEquals(0, instance.size());
        
        Locus locus = new Locus("TestLocus");
        instance.addLocus(locus);

        assertEquals(1, instance.size());
    }

    /**
     * Test of getLocus method, of class Sample.
     */
    @Test
    public void testGetLocus() {
        System.out.println("getLocus");
        String id = "TestLocus";
        Sample instance = new Sample("TestSample");
        instance.addLocus(new Locus(id));
        Locus expResult = new Locus(id);
        Locus result = instance.getLocus(id);
        assertEquals(expResult, result);
    }

    /**
     * Test of getName method, of class Sample.
     */
    @Test
    public void testGetId() {
        System.out.println("getId");
        Sample instance = new Sample("TestId");
        String expResult = "TestId";
        String result = instance.getId();
        assertEquals(expResult, result);
    }

    /**
     * Test of toString method, of class Sample.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        Sample instance = new Sample("SomeId");
        String expResult = "SomeId";
        String result = instance.toString();
        assertEquals(expResult, result);
    }

    /**
     * Test of isEnabled method, of class Sample.
     */
    @Test
    public void testIsEnabled() {
        System.out.println("isEnabled");
        Sample instance = new Sample("SomeId");
        instance.setEnabled(false);
        assertFalse(instance.isEnabled());
        instance.setEnabled(true);
        assertTrue(instance.isEnabled());
    }

    /**
     * Test of setEnabled method, of class Sample.
     */
    @Test
    public void testSetEnabled() {
        System.out.println("setEnabled");
        boolean enabled = false;
        Sample instance = new Sample("SomeId");
        instance.setEnabled(enabled);
    }

    /**
     * Test of getSourceFile method, of class Sample.
     */
    @Test
    public void testGetSourceFile() {
        System.out.println("getSourceFile");
        Sample instance = new Sample("SampleId", "SampleFile");
        String expResult = "SampleFile";
        String result = instance.getSourceFile();
        assertEquals(expResult, result);
    }

    /**
     * Test of setSourceFileHash method, of class Sample.
     */
    @Test
    public void testSetSourceFileHash() {
        System.out.println("setSourceFileHash");
        String fileHash = "";
        Sample instance = new Sample("ID");
        instance.setSourceFileHash(fileHash);
    }

    /**
     * Test of getSourceFileHash method, of class Sample.
     */
    @Test
    public void testGetSourceFileHash() {
        System.out.println("getSourceFileHash");
        String fileHash = "hash";
        Sample instance = new Sample("ID");
        instance.setSourceFileHash(fileHash);
        String result = instance.getSourceFileHash();
        assertEquals(fileHash, result);
    }

    /**
     * Test of setId method, of class Sample.
     */
    @Test
    public void testSetId() {
        System.out.println("setId");
        String id = "newId";
        Sample instance = new Sample("id");
        instance.setId(id);
        assertEquals(id, instance.getId());
    }
}