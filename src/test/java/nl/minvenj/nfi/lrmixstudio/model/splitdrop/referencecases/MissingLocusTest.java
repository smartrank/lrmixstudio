package nl.minvenj.nfi.lrmixstudio.model.splitdrop.referencecases;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

import nl.minvenj.nfi.lrmixstudio.domain.Hypothesis;
import nl.minvenj.nfi.lrmixstudio.domain.LikelihoodRatio;
import nl.minvenj.nfi.lrmixstudio.domain.PopulationStatistics;
import nl.minvenj.nfi.lrmixstudio.domain.Sample;
import nl.minvenj.nfi.lrmixstudio.model.ConfigurationData;
import nl.minvenj.nfi.lrmixstudio.model.splitdrop.threadpool.SplitDropThreadPool;

/**
 *
 * @author dejong
 */
public class MissingLocusTest extends ReferenceCaseTest {

    private static final String REFERENCE_SAMPLE_FILENAME = "/testfiles/MissingLocus/sample.csv";
    private static final String REFERENCE_SUSPECT_FILENAME = "/testfiles/MissingLocus/suspect.csv";

    public MissingLocusTest() {
    }

    @Test
    public void testMissingLocus() throws InterruptedException, TimeoutException {
        System.out.println("testMissingLocus");
        final Collection<Sample> replicates = readReplicates(REFERENCE_SAMPLE_FILENAME);
        final Collection<Sample> suspectSamples = readProfiles(REFERENCE_SUSPECT_FILENAME);
        final PopulationStatistics popStats = readPopulationStatistics(REFERENCE_NFI_POPULATION_STATISTICS_FILENAME);

        final Hypothesis prosecutionHypothesis = new Hypothesis("Prosecution", popStats);
        prosecutionHypothesis.setUnknownCount(1);
        prosecutionHypothesis.setUnknownDropoutProbability(0.01);
        prosecutionHypothesis.setDropInProbability(0.05);
        prosecutionHypothesis.setThetaCorrection(0);

        final Hypothesis defenseHypothesis = new Hypothesis("Defense", popStats);
        defenseHypothesis.setUnknownCount(2);
        defenseHypothesis.setUnknownDropoutProbability(0.01);
        defenseHypothesis.setDropInProbability(0.05);
        defenseHypothesis.setThetaCorrection(0);

        for (final Sample s : suspectSamples) {
            prosecutionHypothesis.addContributor(s, 0.01);
            defenseHypothesis.addNonContributor(s, 0.01);
        }

        final ConfigurationData config = new ConfigurationData();
        config.setDefense(defenseHypothesis);
        config.setProsecution(prosecutionHypothesis);
        config.addProfiles(suspectSamples);
        config.addReplicates(replicates);
        config.setStatistics(popStats);

        final SplitDropThreadPool instance = new SplitDropThreadPool();
        final LikelihoodRatio ratio = instance.doAnalysis(config);

        // Test expected enablement of loci
        final List<String> expectedEnabledLoci = Arrays.asList("D10S1248", "D16S539", "D2S1338", "D8S1179", "D21S11", "D18S51", "D19S433", "FGA", "D2S441", "D3S1358", "D1S1656", "D12S391", "FGA");
        for (final String enabledLocus : expectedEnabledLoci) {
            assertNotNull("Locus " + enabledLocus + " was unexpectedly missing from the result", ratio.getRatio(enabledLocus));
        }
        // Test expected disablement of loci
        final List<String> expectedDisabledLoci = Arrays.asList("vWA", "THO1");
        for (final String disabledLocus : expectedDisabledLoci) {
            assertNull("Locus " + disabledLocus + " was unexpectedly present in the result", ratio.getRatio(disabledLocus));
        }
        // Test for unexpected loci
        for (final String resultLocus : ratio.getLoci()) {
            assertFalse("Unknown Locus " + resultLocus + " was present in the result", expectedDisabledLoci.contains(resultLocus) || expectedDisabledLoci.contains(resultLocus));
        }

        assertEquals(new Double("4.317901E+10"), ratio.getOverallRatio().getRatio(), 100000);
    }
}
