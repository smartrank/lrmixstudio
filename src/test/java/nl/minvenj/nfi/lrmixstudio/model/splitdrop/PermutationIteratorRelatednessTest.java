/*
 * Copyright (c) 2014, Netherlands Forensic Institute
 * All rights reserved.
 */
package nl.minvenj.nfi.lrmixstudio.model.splitdrop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import nl.minvenj.nfi.lrmixstudio.domain.Allele;
import nl.minvenj.nfi.lrmixstudio.domain.Locus;

/**
 *
 * @author dejong
 */
public class PermutationIteratorRelatednessTest {

    ArrayList<Locus> _possibleLoci = new ArrayList<>();

    public PermutationIteratorRelatednessTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        _possibleLoci = new ArrayList<>();
        Locus locus1 = new Locus("Locus 1");
        locus1.addAllele(new Allele("1"));
        locus1.addAllele(new Allele("1"));
        Locus locus2 = new Locus("Locus 2");
        locus2.addAllele(new Allele("2"));
        locus2.addAllele(new Allele("2"));
        Locus locus3 = new Locus("Locus 3");
        locus3.addAllele(new Allele("3"));
        locus3.addAllele(new Allele("3"));
        Locus locus4 = new Locus("Locus 4");
        locus4.addAllele(new Allele("4"));
        locus4.addAllele(new Allele("4"));
        Locus locus5 = new Locus("Locus 5");
        locus5.addAllele(new Allele("5"));
        locus5.addAllele(new Allele("5"));
        Locus locus6 = new Locus("Locus 5");
        locus6.addAllele(new Allele("6"));
        locus6.addAllele(new Allele("6"));

        _possibleLoci.add(locus1);
        _possibleLoci.add(locus2);
        _possibleLoci.add(locus3);
        _possibleLoci.add(locus4);
        _possibleLoci.add(locus5);
        _possibleLoci.add(locus6);
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of hasNext method, of class PermutationIteratorRelatedness.
     */
    @Test
    public void testHasNext() {
        System.out.println("hasNext");
        PermutationIteratorRelatedness instance = new PermutationIteratorRelatedness(0, _possibleLoci, 0);
        assertFalse(instance.hasNext());
        PermutationIteratorRelatedness instance2 = new PermutationIteratorRelatedness(1, _possibleLoci, 0);
        assertTrue(instance2.hasNext());
    }

    /**
     * Test of next method, of class PermutationIteratorRelatedness.
     */
    @Test
    public void testNext() {
        System.out.println("next");
        PermutationIteratorRelatedness instance = new PermutationIteratorRelatedness(1, _possibleLoci, 0);
        Permutation permutation = instance.next();
        assertEquals(1, permutation.getLoci().length);
    }

    private void testSize(int unknownCount, int start) {
        System.out.println(unknownCount + " unknowns starting with " + start + " of " + _possibleLoci.size());
        PermutationIteratorRelatedness instance = new PermutationIteratorRelatedness(unknownCount, _possibleLoci, start);
        long size = instance.size();
        long count = 0;
        long permutations = 0;
        System.out.println("  Size = " + size);
        while (instance.hasNext()) {
            Permutation next = instance.next();
            assertNotNull(next);
            permutations += next.getPermutationFactor();
            System.out.println("  " + next.toLogString(next.getLoci()) + " pf=" + next.getPermutationFactor());
            count++;
        }
        System.out.println("Got " + count + " items representing " + permutations + " permutations");
        System.out.println("");
        assertEquals(Math.pow(_possibleLoci.size(), unknownCount - 1), permutations, 0.0);
        assertEquals("Size is reported as " + size + " when " + count + " items were actually returned!", count, size);
    }

    /**
     * Test of size method, of class PermutationIteratorRelatedness.
     */
    @Test
    public void testSize() {
        System.out.println("size");
        for (int unknowns = 1; unknowns < 6; unknowns++) {
            for (int start = 0; start < _possibleLoci.size(); start++) {
                testSize(unknowns, start);
            }
        }
    }

}
