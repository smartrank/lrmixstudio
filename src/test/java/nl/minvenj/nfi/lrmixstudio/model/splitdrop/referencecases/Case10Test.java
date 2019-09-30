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
public class Case10Test extends ReferenceCaseTest {

    private static final String REFERENCE_CASE10_SAMPLE_FILENAME = "/testfiles/case-10/sample.csv";
    private static final String REFERENCE_CASE10_SUSPECT_FILENAME = "/testfiles/case-10/suspect.csv";
    private static final String REFERENCE_CASE10_VICTIM_FILENAME = "/testfiles/case-10/victim.csv";

    public Case10Test() {
    }

    @Test
    public void testReferenceCase10Parameter1() throws InterruptedException, TimeoutException {
        System.out.println("testReferenceCase10Parameter1");
        Collection<Sample> replicates = readReplicates(REFERENCE_CASE10_SAMPLE_FILENAME);
        Collection<Sample> suspectSamples = readProfiles(REFERENCE_CASE10_SUSPECT_FILENAME);
        Collection<Sample> victimSamples = readProfiles(REFERENCE_CASE10_VICTIM_FILENAME);
        PopulationStatistics popStats = readPopulationStatistics(REFERENCE_NFI_POPULATION_STATISTICS_FILENAME);

        Hypothesis prosecutionHypothesis = new Hypothesis("Prosecution", popStats);
        prosecutionHypothesis.setUnknownCount(0);
        prosecutionHypothesis.setUnknownDropoutProbability(0.01);
        prosecutionHypothesis.setDropInProbability(0.05);
        prosecutionHypothesis.setThetaCorrection(0.01);

        Hypothesis defenseHypothesis = new Hypothesis("Defense", popStats);
        defenseHypothesis.setUnknownCount(1);
        defenseHypothesis.setUnknownDropoutProbability(0.01);
        defenseHypothesis.setDropInProbability(0.05);
        defenseHypothesis.setThetaCorrection(0.01);

        for (Sample s : suspectSamples) {
            prosecutionHypothesis.addContributor(s, 0);
            defenseHypothesis.addNonContributor(s, 0);
        }

        for (Sample s : victimSamples) {
            prosecutionHypothesis.addContributor(s, 0.01);
            defenseHypothesis.addContributor(s, 0.01);
        }

        ConfigurationData config = new ConfigurationData();
        config.setDefense(defenseHypothesis);
        config.setProsecution(prosecutionHypothesis);
        config.addProfiles(suspectSamples);
        config.addProfiles(victimSamples);
        config.addReplicates(replicates);
        config.setStatistics(popStats);

        SplitDropThreadPool instance = new SplitDropThreadPool();
        LikelihoodRatio result = instance.doAnalysis(config);
        
        String expectedResult = "extremely much less likely";
        assertEquals(expectedResult, getProbabilityTerm(result.getOverallRatio()));
    }

    @Test
    public void testReferenceCase10Parameter2() throws InterruptedException, TimeoutException {
        System.out.println("testReferenceCase10Parameter2");
        Collection<Sample> replicates = readReplicates(REFERENCE_CASE10_SAMPLE_FILENAME);
        Collection<Sample> suspectSamples = readProfiles(REFERENCE_CASE10_SUSPECT_FILENAME);
        Collection<Sample> victimSamples = readProfiles(REFERENCE_CASE10_VICTIM_FILENAME);
        PopulationStatistics popStats = readPopulationStatistics(REFERENCE_NFI_POPULATION_STATISTICS_FILENAME);

        Hypothesis prosecutionHypothesis = new Hypothesis("Prosecution", popStats);
        prosecutionHypothesis.setUnknownCount(0);
        prosecutionHypothesis.setUnknownDropoutProbability(0.99);
        prosecutionHypothesis.setDropInProbability(0.05);
        prosecutionHypothesis.setThetaCorrection(0.01);

        Hypothesis defenseHypothesis = new Hypothesis("Defense", popStats);
        defenseHypothesis.setUnknownCount(1);
        defenseHypothesis.setUnknownDropoutProbability(0.99);
        defenseHypothesis.setDropInProbability(0.05);
        defenseHypothesis.setThetaCorrection(0.01);

        for (Sample s : suspectSamples) {
            prosecutionHypothesis.addContributor(s, 0);
            defenseHypothesis.addContributor(s, 0);
        }

        for (Sample s : victimSamples) {
            prosecutionHypothesis.addContributor(s, 0.99);
            defenseHypothesis.addNonContributor(s, 0.99);
        }

        ConfigurationData config = new ConfigurationData();
        config.setDefense(defenseHypothesis);
        config.setProsecution(prosecutionHypothesis);
        config.addProfiles(suspectSamples);
        config.addProfiles(victimSamples);
        config.addReplicates(replicates);
        config.setStatistics(popStats);

        SplitDropThreadPool instance = new SplitDropThreadPool();
        LikelihoodRatio result = instance.doAnalysis(config);

        String expectedResult = "much more likely";
        assertEquals(expectedResult, getProbabilityTerm(result.getOverallRatio()));
     }
}
