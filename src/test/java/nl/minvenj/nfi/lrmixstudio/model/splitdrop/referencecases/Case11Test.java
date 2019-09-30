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
public class Case11Test extends ReferenceCaseTest {

    private static final String REFERENCE_CASE11_SAMPLE_FILENAME = "/testfiles/case-11/sample.csv";
    private static final String REFERENCE_CASE11_SUSPECT_FILENAME = "/testfiles/case-11/suspect.csv";
    private static final String REFERENCE_CASE11_VICTIM_FILENAME = "/testfiles/case-11/victim.csv";

    public Case11Test() {
    }

    @Test
    public void testReferenceCase11Parameter1() throws InterruptedException, TimeoutException {
        System.out.println("testReferenceCase11Parameter1");
        Collection<Sample> replicates = readReplicates(REFERENCE_CASE11_SAMPLE_FILENAME);
        Collection<Sample> suspectSamples = readProfiles(REFERENCE_CASE11_SUSPECT_FILENAME);
        Collection<Sample> victimSamples = readProfiles(REFERENCE_CASE11_VICTIM_FILENAME);
        PopulationStatistics popStats = readPopulationStatistics(REFERENCE_NFI_POPULATION_STATISTICS_FILENAME);

        Hypothesis prosecutionHypothesis = new Hypothesis("Prosecution", popStats);
        prosecutionHypothesis.setUnknownCount(0);
        prosecutionHypothesis.setUnknownDropoutProbability(0.31);
        prosecutionHypothesis.setDropInProbability(0.05);
        prosecutionHypothesis.setThetaCorrection(0.01);

        Hypothesis defenseHypothesis = new Hypothesis("Defense", popStats);
        defenseHypothesis.setUnknownCount(1);
        defenseHypothesis.setUnknownDropoutProbability(0.31);
        defenseHypothesis.setDropInProbability(0.05);
        defenseHypothesis.setThetaCorrection(0.01);

        for (Sample s : suspectSamples) {
            prosecutionHypothesis.addContributor(s, 0.31);
            defenseHypothesis.addNonContributor(s, 0.31);
        }

        for (Sample s : victimSamples) {
            prosecutionHypothesis.addContributor(s, 0.31);
            defenseHypothesis.addContributor(s, 0.31);
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

        String expectedResult = "approximately equally likely";
        assertEquals(expectedResult, getProbabilityTerm(result.getOverallRatio()));
    }

    @Test
    public void testReferenceCase11Parameter2() throws InterruptedException, TimeoutException {
        System.out.println("testReferenceCase11Parameter2");
        Collection<Sample> replicates = readReplicates(REFERENCE_CASE11_SAMPLE_FILENAME);
        Collection<Sample> suspectSamples = readProfiles(REFERENCE_CASE11_SUSPECT_FILENAME);
        Collection<Sample> victimSamples = readProfiles(REFERENCE_CASE11_VICTIM_FILENAME);
        PopulationStatistics popStats = readPopulationStatistics(REFERENCE_NFI_POPULATION_STATISTICS_FILENAME);

        Hypothesis prosecutionHypothesis = new Hypothesis("Prosecution", popStats);
        prosecutionHypothesis.setUnknownCount(0);
        prosecutionHypothesis.setUnknownDropoutProbability(0.27);
        prosecutionHypothesis.setDropInProbability(0.05);
        prosecutionHypothesis.setThetaCorrection(0.01);

        Hypothesis defenseHypothesis = new Hypothesis("Defense", popStats);
        defenseHypothesis.setUnknownCount(1);
        defenseHypothesis.setUnknownDropoutProbability(0.27);
        defenseHypothesis.setDropInProbability(0.05);
        defenseHypothesis.setThetaCorrection(0.01);

        for (Sample s : suspectSamples) {
            prosecutionHypothesis.addContributor(s, 0.27);
            defenseHypothesis.addNonContributor(s, 0.27);
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

        String expectedResult = "approximately equally likely";
        assertEquals(expectedResult, getProbabilityTerm(result.getOverallRatio()));
     }
}
