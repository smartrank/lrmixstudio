/*
 * Copyright (c) 2013, Netherlands Forensic Institute
 * All rights reserved.
 */
package nl.minvenj.nfi.lrmixstudio.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author dejong
 */
public class HypothesisTest {

    public HypothesisTest() {
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
     * Test of addContributor method, of class Hypothesis.
     */
    @Test
    public void testAddContributor() {
        System.out.println("addContributor");
        Sample contributorSample = new Sample("SomeSample");
        Hypothesis instance = new Hypothesis("HypothesisId", 0, null, 0, 0, 0);
        instance.addContributor(contributorSample, 0);
    }

    /**
     * Test of addNonContributor method, of class Hypothesis.
     */
    @Test
    public void testAddNonContributor() {
        System.out.println("addNonContributor");
        Sample contributorSample = new Sample("SomeSample");
        Hypothesis instance = new Hypothesis("HypothesisId", 0, null, 0, 0, 0);
        instance.addNonContributor(contributorSample, 0);
    }

    /**
     * Test of getUnknownCount method, of class Hypothesis.
     */
    @Test
    public void testGetUnknownCount() {
        System.out.println("getUnknownCount");
        Hypothesis instance = new Hypothesis("HypothesisId", 0, null, 0, 0, 0);
        int expResult = 0;
        int result = instance.getUnknownCount();
        assertEquals(expResult, result);
        instance = new Hypothesis("HypothesisId", 1, null, 0, 0, 0);
        expResult = 1;
        result = instance.getUnknownCount();
        assertEquals(expResult, result);
    }

    /**
     * Test of getName method, of class Hypothesis.
     */
    @Test
    public void testGetId() {
        System.out.println("getId");
        Hypothesis instance = new Hypothesis("HypothesisId", 0, null, 0, 0, 0);
        String expResult = "HypothesisId";
        String result = instance.getId();
        assertEquals(expResult, result);
    }

    /**
     * Test of getContributors method, of class Hypothesis.
     */
    @Test
    public void testGetContributors() {
        System.out.println("getContributors");
        Sample contributorSample1 = new Sample("SomeSample1");
        Sample contributorSample2 = new Sample("SomeSample2");
        Hypothesis instance = new Hypothesis("HypothesisId", 0, null, 0, 0, 0);

        assertNotNull(instance.getContributors());
        assertTrue(instance.getContributors().isEmpty());

        instance.addContributor(contributorSample1, 0);

        assertEquals(1, instance.getContributors().size());
        Iterator<Contributor> i = instance.getContributors().iterator();
        assertEquals(contributorSample1, i.next().getSample());

        instance.addContributor(contributorSample2, 0);
        Iterator<Contributor> i2 = instance.getContributors().iterator();
        assertEquals(contributorSample1, i2.next().getSample());
        assertEquals(contributorSample2, i2.next().getSample());
    }

    /**
     * Test of getNonContributors method, of class Hypothesis.
     */
    @Test
    public void testGetNonContributors() {
        System.out.println("getNonContributors");
        Sample contributorSample1 = new Sample("SomeSample1");
        Sample contributorSample2 = new Sample("SomeSample2");
        Hypothesis instance = new Hypothesis("HypothesisId", 0, null, 0, 0, 0);

        assertNotNull(instance.getContributors());
        assertTrue(instance.getContributors().isEmpty());

        instance.addNonContributor(contributorSample1, 0);

        assertEquals(1, instance.getNonContributors().size());
        Iterator<Contributor> i = instance.getNonContributors().iterator();
        assertEquals(contributorSample1, i.next().getSample());

        instance.addNonContributor(contributorSample2, 0);
        Iterator<Contributor> i2 = instance.getNonContributors().iterator();
        assertEquals(contributorSample1, i2.next().getSample());
        assertEquals(contributorSample2, i2.next().getSample());
    }

    /**
     * Test of getPopulationStatistics method, of class Hypothesis.
     */
    @Test
    public void testGetPopulationStatistics() {
        System.out.println("getPopulationStatistics");
        PopulationStatistics popStats = new PopulationStatistics("");
        Hypothesis instance = new Hypothesis("HypothesisId", 0, popStats, 0, 0, 0);
        assertEquals(popStats, instance.getPopulationStatistics());
    }

    /**
     * Test of getDropInProbability method, of class Hypothesis.
     */
    @Test
    public void testGetDropInProbability() {
        System.out.println("getDropInProbability");
        Hypothesis instance = new Hypothesis("HypothesisId", 0, null, 0.1, 0.2, 0.3);
        assertEquals(0.1, instance.getDropInProbability(), 0.0);
    }

    /**
     * Test of getContributor method, of class Hypothesis.
     */
    @Test
    public void testGetContributor() {
        System.out.println("getContributor");
        Allele a = new Allele("1");
        Locus l = new Locus("SomeLocus");
        l.addAllele(a);
        Sample s = new Sample("SampleId");
        s.addLocus(l);
        Hypothesis instance = new Hypothesis("HypothesisId", 0, null, 0.1, 0.2, 0.3);
        instance.addContributor(s, 0);
        Contributor result = instance.getContributor(a);
        assertEquals(s, result.getSample());
    }

    /**
     * Test of getThetaCorrection method, of class Hypothesis.
     */
    @Test
    public void testGetThetaCorrection() {
        System.out.println("getThetaCorrection");
        Hypothesis instance = new Hypothesis("HypothesisId", 0, null, 0.1, 0.2, 0.3);
        assertEquals(0.3, instance.getThetaCorrection(), 0.0);
    }

    /**
     * Test of getUnknownDropoutProbability method, of class Hypothesis.
     */
    @Test
    public void testGetUnknownDropoutProbability() {
        System.out.println("getUnknownDropoutProbability");
        Hypothesis instance = new Hypothesis("HypothesisId", 0, null, 0, 0.1, 0);
        double expResult = 0.1;
        double result = instance.getUnknownDropoutProbability();
        assertEquals(expResult, result, 0.0);
    }

    /**
     * Test of setUnknownCount method, of class Hypothesis.
     */
    @Test
    public void testSetUnknownCount() {
        System.out.println("setUnknownCount");
        int unknowns = 10;
        Hypothesis instance = new Hypothesis("HypothesisId", 0, null, 0, 0.1, 0.1);;
        instance.setUnknownCount(unknowns);
        assertEquals(unknowns, instance.getUnknownCount());
    }

    /**
     * Test of setDropInProbability method, of class Hypothesis.
     */
    @Test
    public void testSetDropInProbability() {
        System.out.println("setDropInProbability");
        double dropIn = 0.0;
        Hypothesis instance = new Hypothesis("HypothesisId", 0, null, 0, 0.1, 0.1);;
        instance.setDropInProbability(dropIn);
        assertEquals(dropIn, instance.getDropInProbability(), 0.01);
    }

    /**
     * Test of getContributor method, of class Hypothesis.
     */
    @Test
    public void testGetContributor_Allele() {
        System.out.println("getContributor");
        Allele a = new Allele("1");
        Locus l = new Locus("SomeLocus");
        l.addAllele(a);
        Sample s = new Sample("SampleId");
        s.addLocus(l);
        Hypothesis instance = new Hypothesis("HypothesisId", 0, null, 0.1, 0.2, 0.3);
        instance.addContributor(s, 0);
        Contributor result = instance.getContributor(a);
        assertEquals(s, result.getSample());
    }

    /**
     * Test of setThetaCorrection method, of class Hypothesis.
     */
    @Test
    public void testSetThetaCorrection() {
        System.out.println("setThetaCorrection");
        double theta = 0.12;
        Hypothesis instance = new Hypothesis("HypothesisId", 0, null, 0.1, 0.2, 0.3);
        instance.setThetaCorrection(theta);
        assertEquals(theta, instance.getThetaCorrection(), 0.01);
    }

    /**
     * Test of setUnknownDropoutProbability method, of class Hypothesis.
     */
    @Test
    public void testSetUnknownDropoutProbability() {
        System.out.println("setUnknownDropoutProbability");
        double dropOut = 0.12;
        Hypothesis instance = new Hypothesis("HypothesisId", 0, null, 0.1, 0.2, 0.3);
        instance.setUnknownDropoutProbability(dropOut);
        assertEquals(dropOut, instance.getUnknownDropoutProbability(), 0.01);
    }

    /**
     * Test of getGuid method, of class Hypothesis.
     */
    @Test
    public void testGetGuid() {
        System.out.println("getGuid");
        Allele a = new Allele("1");
        Locus l = new Locus("SomeLocus");
        l.addAllele(a);
        Sample s = new Sample("SampleId");
        s.addLocus(l);
        Hypothesis instance = new Hypothesis("HypothesisId", 0, new PopulationStatistics("bla"), 0.1, 0.2, 0.3);
        instance.addContributor(s, 0);
        int expResult = 168151356;
        int result = instance.getGuid();
        assertEquals(expResult, result);
    }

    /**
     * Test of getContributor method, of class Hypothesis.
     */
    @Test
    public void testGetContributor_Sample() {
        System.out.println("getContributor");
        Allele a = new Allele("1");
        Locus l = new Locus("SomeLocus");
        l.addAllele(a);
        Sample s = new Sample("SampleId");
        s.addLocus(l);
        Hypothesis instance = new Hypothesis("HypothesisId", 0, null, 0.1, 0.2, 0.3);
        instance.addContributor(s, 0);
        Contributor result = instance.getContributor(s);
        assertEquals(s, result.getSample());
    }

    /**
     * Test of copy method, of class Hypothesis.
     */
    @Test
    public void testCopy() {
        System.out.println("copy");
        Hypothesis instance = new Hypothesis("HypothesisId", 0, new PopulationStatistics("bla"), 0.1, 0.2, 0.3);
        Hypothesis result = instance.copy();
        assertEquals(instance.getGuid(), result.getGuid());
    }

    /**
     * Test of toString method, of class Hypothesis.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        Hypothesis instance = new Hypothesis("HypothesisId", 0, new PopulationStatistics("bla"), 0.1, 0.2, 0.3);
        Allele a = new Allele("1");
        Locus l = new Locus("SomeLocus");
        l.addAllele(a);
        Sample s = new Sample("SampleId");
        s.addLocus(l);
        instance.addContributor(s, 0.12);
        String expResult = "SampleId(0.12), DropIn 0.10, Theta 0.30";
        String result = instance.toString();
        assertEquals(expResult, result);
    }

    /**
     * Test of isContributor method, of class Hypothesis.
     */
    @Test
    public void testIsContributor() {
        System.out.println("isContributor");
        Hypothesis instance = new Hypothesis("HypothesisId", 0, null, 0, 0.1, 0.1);

        Sample sample1 = new Sample("Sample 1", "");
        Locus locus1 = new Locus("Locus 1");
        locus1.addAllele(new Allele("10"));
        locus1.addAllele(new Allele("11"));
        sample1.addLocus(locus1);

        Sample sample2 = new Sample("Sample 2", "");
        Locus locus2 = new Locus("Locus 1");
        locus2.addAllele(new Allele("10"));
        locus2.addAllele(new Allele("12"));
        sample2.addLocus(locus2);

        instance.addContributor(sample1, 0);

        boolean result = instance.isContributor(sample1);
        assertEquals(true, result);
        result = instance.isContributor(sample2);
        assertEquals(false, result);
    }
}