/*
 * Copyright (c) 2013, Netherlands Forensic Institute
 * All rights reserved.
 */
package nl.minvenj.nfi.lrmixstudio.domain;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 *
 * @author dejong
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({AlleleTest.class, LocusTest.class, HypothesisTest.class, ContributorTest.class, SampleTest.class, RatioTest.class, LocusProbabilitiesTest.class, PopulationStatisticsTest.class, LikelihoodRatioTest.class})
public class DomainSuite {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }
    
}