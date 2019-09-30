/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.minvenj.nfi.lrmixstudio.model.splitdrop.threadpool;

import nl.minvenj.nfi.lrmixstudio.model.splitdrop.threadpool.LocusProbability;
import nl.minvenj.nfi.lrmixstudio.model.splitdrop.threadpool.LocusProbabilityJob;
import java.math.BigDecimal;
import java.util.ArrayList;
import nl.minvenj.nfi.lrmixstudio.domain.Allele;
import nl.minvenj.nfi.lrmixstudio.domain.Hypothesis;
import nl.minvenj.nfi.lrmixstudio.domain.Locus;
import nl.minvenj.nfi.lrmixstudio.domain.PopulationStatistics;
import nl.minvenj.nfi.lrmixstudio.domain.Sample;
import nl.minvenj.nfi.lrmixstudio.model.DefaultAnalysisProgressListenerImpl;
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
public class LocusProbabilityJobTest {

    public LocusProbabilityJobTest() {
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
     * Test of getProbability method, of class LocusProbabilityJob.
     */
    @Test
    public void testGetProbability() throws Exception {
        System.out.println("getProbability");
        ArrayList<Sample> samples = new ArrayList<>();
        Sample r1 = new Sample("R1");
        Locus lr1 = new Locus("FGA");
        lr1.addAllele(new Allele("11"));
        lr1.addAllele(new Allele("12"));
        r1.addLocus(lr1);

        Sample p1 = new Sample("P");
        Locus lp1 = new Locus("FGA");
        lp1.addAllele(new Allele("11"));
        lp1.addAllele(new Allele("12"));
        p1.addLocus(lr1);

        samples.add(r1);

        Hypothesis prosecution = new Hypothesis("Prosecution", new PopulationStatistics("popStats"));
        prosecution.addContributor(p1, 0.1);

        LocusProbabilityJob instance = new LocusProbabilityJob("FGA", null, samples, prosecution, new DefaultAnalysisProgressListenerImpl());
        instance.call();
        double result = instance.getProbability().getValue();
        assertEquals(0.81, result, 0.0);
    }

    /**
     * Test of call method, of class LocusProbabilityJob.
     */
    @Test
    public void testCall() throws Exception {
        System.out.println("call");
        ArrayList<Sample> samples = new ArrayList<>();
        Sample r1 = new Sample("R1");
        Locus lr1 = new Locus("FGA");
        lr1.addAllele(new Allele("11"));
        lr1.addAllele(new Allele("12"));
        r1.addLocus(lr1);

        Sample p1 = new Sample("P");
        Locus lp1 = new Locus("FGA");
        lp1.addAllele(new Allele("11"));
        lp1.addAllele(new Allele("12"));
        p1.addLocus(lr1);

        samples.add(r1);

        Hypothesis prosecution = new Hypothesis("Prosecution", new PopulationStatistics("popStats"));
        prosecution.addContributor(p1, 0.1);

        LocusProbabilityJob instance = new LocusProbabilityJob("FGA", null, samples, prosecution, new DefaultAnalysisProgressListenerImpl());
        LocusProbability result = (LocusProbability) instance.call();
        assertEquals(0.81, result.getValue(), 0.001);
    }

    /**
     * Test of calculateReplicateProbability method, of class LocusProbabilityJob.
     */
    @Test
    public void testCalculateSingleLocusProbability_0args() {
        System.out.println("calculateSingleLocusProbability");
        ArrayList<Sample> samples = new ArrayList<>();
        Sample r1 = new Sample("R1");
        Locus lr1 = new Locus("FGA");
        lr1.addAllele(new Allele("11"));
        lr1.addAllele(new Allele("12"));
        r1.addLocus(lr1);

        Sample p1 = new Sample("P");
        Locus lp1 = new Locus("FGA");
        lp1.addAllele(new Allele("11"));
        lp1.addAllele(new Allele("12"));
        p1.addLocus(lr1);

        samples.add(r1);

        Hypothesis prosecution = new Hypothesis("Prosecution", new PopulationStatistics("popStats"));
        prosecution.addContributor(p1, 0.1);

        LocusProbabilityJob instance = new LocusProbabilityJob("FGA", null, samples, prosecution, new DefaultAnalysisProgressListenerImpl());
        double result = instance.calculateSingleLocusProbability();
        assertEquals(0.81, result, 0.0);
    }

    /**
     * Test of calculateReplicateProbability method, of class LocusProbabilityJob.
     */
    @Test
    public void testCalculateSingleLocusProbability_Hypothesis() {
        System.out.println("calculateSingleLocusProbability");
        ArrayList<Sample> samples = new ArrayList<>();
        Sample r1 = new Sample("R1");
        Locus lr1 = new Locus("FGA");
        lr1.addAllele(new Allele("11"));
        lr1.addAllele(new Allele("12"));
        r1.addLocus(lr1);

        Sample p1 = new Sample("P");
        Locus lp1 = new Locus("FGA");
        lp1.addAllele(new Allele("11"));
        lp1.addAllele(new Allele("12"));
        p1.addLocus(lr1);

        samples.add(r1);

        PopulationStatistics popStats = new PopulationStatistics("popStats");
        popStats.addStatistic("FGA", "11", new BigDecimal("0.15"));
        popStats.addStatistic("FGA", "12", new BigDecimal("0.20"));
        popStats.addStatistic("FGA", "13", new BigDecimal("0.65"));

        Hypothesis prosecution = new Hypothesis("Prosecution", new PopulationStatistics("popStats"));
        prosecution.addContributor(p1, 0.1);
        prosecution.setThetaCorrection(0.01);
        prosecution.setDropInProbability(0.05);
        prosecution.setUnknownCount(2);
        prosecution.setUnknownDropoutProbability(0.1);

        Locus[] unknowns1 = new Locus[2];
        unknowns1[0] = new Locus("FGA");
        unknowns1[0].addAllele(new Allele("11"));
        unknowns1[0].addAllele(new Allele("12"));
        unknowns1[1] = new Locus("FGA");
        unknowns1[1].addAllele(new Allele("12"));
        unknowns1[1].addAllele(new Allele("13"));

        LocusProbabilityJob instance = new LocusProbabilityJob("FGA", null, samples, prosecution, new DefaultAnalysisProgressListenerImpl());
        double result = instance.calculateReplicateProbability(unknowns1);
        assertEquals(0.09395595, result, 0.0);

        Locus[] unknowns2 = new Locus[2];
        unknowns2[0] = new Locus("FGA");
        unknowns2[0].addAllele(new Allele("12"));
        unknowns2[0].addAllele(new Allele("13"));
        unknowns2[1] = new Locus("FGA");
        unknowns2[1].addAllele(new Allele("11"));
        unknowns2[1].addAllele(new Allele("12"));

        result = instance.calculateReplicateProbability(unknowns2);
        assertEquals(0.09395595, result, 0.0);
    }
}
