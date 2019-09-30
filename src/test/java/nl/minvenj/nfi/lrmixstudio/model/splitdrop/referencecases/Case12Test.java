package nl.minvenj.nfi.lrmixstudio.model.splitdrop.referencecases;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
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
//@Ignore
public class Case12Test extends ReferenceCaseTest {

    private static final String REFERENCE_SAMPLE_FILENAME = "/testfiles/case-12/sample.csv";
    private static final String REFERENCE_SUSPECT_FILENAME = "/testfiles/case-12/suspect.csv";
    private static final String REFERENCE_VICTIM1_FILENAME = "/testfiles/case-12/victim1.csv";
    private static final String REFERENCE_VICTIM2_FILENAME = "/testfiles/case-12/victim2.csv";

    public Case12Test() {
    }

    @Test
    public void testReferenceCase12Parameter1() throws InterruptedException, TimeoutException {
        System.out.println("testReferenceCase12Parameter1");
        Collection<Sample> replicates = readReplicates(REFERENCE_SAMPLE_FILENAME);
        Collection<Sample> suspectSamples = readProfiles(REFERENCE_SUSPECT_FILENAME);
        Collection<Sample> victimSamples = readProfiles(REFERENCE_VICTIM1_FILENAME);
        PopulationStatistics popStats = readPopulationStatistics(REFERENCE_NFI_POPULATION_STATISTICS_FILENAME);

        Hypothesis prosecutionHypothesis = new Hypothesis("Prosecution", popStats);
        prosecutionHypothesis.setUnknownCount(2);
        prosecutionHypothesis.setUnknownDropoutProbability(0.11);
        prosecutionHypothesis.setDropInProbability(0.05);
        prosecutionHypothesis.setThetaCorrection(0);

        Hypothesis defenseHypothesis = new Hypothesis("Defense", popStats);
        defenseHypothesis.setUnknownCount(3);
        defenseHypothesis.setUnknownDropoutProbability(0.11);
        defenseHypothesis.setDropInProbability(0.05);
        defenseHypothesis.setThetaCorrection(0);

        for (Sample s : suspectSamples) {
            prosecutionHypothesis.addContributor(s, 0.11);
            defenseHypothesis.addNonContributor(s, 0.11);
        }

        for (Sample s : victimSamples) {
            prosecutionHypothesis.addContributor(s, 0.11);
            defenseHypothesis.addContributor(s, 0.11);
        }

        ConfigurationData config = new ConfigurationData();
        config.setDefense(defenseHypothesis);
        config.setProsecution(prosecutionHypothesis);
        config.addProfiles(suspectSamples);
        config.addProfiles(victimSamples);
        config.addReplicates(replicates);
        config.setStatistics(popStats);

        SplitDropThreadPool instance = new SplitDropThreadPool();
        instance.doAnalysis(config);
        LikelihoodRatio result = instance.getLikelihoodRatio();

        String expectedResult = "slightly more likely";
        assertEquals(expectedResult, getProbabilityTerm(result.getOverallRatio()));
    }

    @Test
    public void testReferenceCase12Parameter2() throws InterruptedException, TimeoutException {
        System.out.println("testReferenceCase12Parameter2");
        Collection<Sample> replicates = readReplicates(REFERENCE_SAMPLE_FILENAME);
        Collection<Sample> suspectSamples = readProfiles(REFERENCE_SUSPECT_FILENAME);
        Collection<Sample> victimSamples = readProfiles(REFERENCE_VICTIM1_FILENAME);
        PopulationStatistics popStats = readPopulationStatistics(REFERENCE_NFI_POPULATION_STATISTICS_FILENAME);

        Hypothesis prosecutionHypothesis = new Hypothesis("Prosecution", popStats);
        prosecutionHypothesis.setUnknownCount(2);
        prosecutionHypothesis.setUnknownDropoutProbability(0.53);
        prosecutionHypothesis.setDropInProbability(0.05);
        prosecutionHypothesis.setThetaCorrection(0);

        Hypothesis defenseHypothesis = new Hypothesis("Defense", popStats);
        defenseHypothesis.setUnknownCount(3);
        defenseHypothesis.setUnknownDropoutProbability(0.53);
        defenseHypothesis.setDropInProbability(0.05);
        defenseHypothesis.setThetaCorrection(0);

        for (Sample s : suspectSamples) {
            prosecutionHypothesis.addContributor(s, 0.53);
            defenseHypothesis.addNonContributor(s, 0.53);
        }

        for (Sample s : victimSamples) {
            prosecutionHypothesis.addContributor(s, 0);
            defenseHypothesis.addContributor(s, 0);
        }

        ConfigurationData config = new ConfigurationData();
        config.setDefense(defenseHypothesis);
        config.setProsecution(prosecutionHypothesis);
        config.addProfiles(suspectSamples);
        config.addProfiles(victimSamples);
        config.addReplicates(replicates);
        config.setStatistics(popStats);

        SplitDropThreadPool instance = new SplitDropThreadPool();
        instance.doAnalysis(config);
        LikelihoodRatio result = instance.getLikelihoodRatio();

        String expectedResult = "much more likely";
        assertEquals(expectedResult, getProbabilityTerm(result.getOverallRatio()));
    }

    @Test
    public void testReferenceCase12Parameter3() throws InterruptedException, TimeoutException {
        System.out.println("testReferenceCase12Parameter3");
        Collection<Sample> replicates = readReplicates(REFERENCE_SAMPLE_FILENAME);
        Collection<Sample> suspectSamples = readProfiles(REFERENCE_SUSPECT_FILENAME);
        Collection<Sample> victim1Samples = readProfiles(REFERENCE_VICTIM1_FILENAME);
        Collection<Sample> victim2Samples = readProfiles(REFERENCE_VICTIM2_FILENAME);
        PopulationStatistics popStats = readPopulationStatistics(REFERENCE_NFI_POPULATION_STATISTICS_FILENAME);

        Hypothesis prosecutionHypothesis = new Hypothesis("Prosecution", popStats);
        prosecutionHypothesis.setUnknownCount(2);
        prosecutionHypothesis.setUnknownDropoutProbability(0.22);
        prosecutionHypothesis.setDropInProbability(0.05);
        prosecutionHypothesis.setThetaCorrection(0);

        Hypothesis defenseHypothesis = new Hypothesis("Defense", popStats);
        defenseHypothesis.setUnknownCount(3);
        defenseHypothesis.setUnknownDropoutProbability(0.22);
        defenseHypothesis.setDropInProbability(0.05);
        defenseHypothesis.setThetaCorrection(0);

        for (Sample s : suspectSamples) {
            prosecutionHypothesis.addContributor(s, 0.22);
            defenseHypothesis.addNonContributor(s, 0.22);
        }

        for (Sample s : victim1Samples) {
            prosecutionHypothesis.addContributor(s, 0);
            defenseHypothesis.addContributor(s, 0);
        }

        for (Sample s : victim2Samples) {
            prosecutionHypothesis.addContributor(s, 0.22);
            defenseHypothesis.addContributor(s, 0.22);
        }

        ConfigurationData config = new ConfigurationData();
        config.setDefense(defenseHypothesis);
        config.setProsecution(prosecutionHypothesis);
        config.addProfiles(suspectSamples);
        config.addProfiles(victim1Samples);
        config.addProfiles(victim2Samples);
        config.addReplicates(replicates);
        config.setStatistics(popStats);

        SplitDropThreadPool instance = new SplitDropThreadPool();
        instance.doAnalysis(config);
        LikelihoodRatio result = instance.getLikelihoodRatio();

        String expectedResult = "much more likely";
        assertEquals(expectedResult, getProbabilityTerm(result.getOverallRatio()));
    }
}
