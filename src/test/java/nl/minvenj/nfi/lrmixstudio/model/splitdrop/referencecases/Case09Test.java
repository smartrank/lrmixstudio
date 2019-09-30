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
public class Case09Test extends ReferenceCaseTest {

    private static final String REFERENCE_CASE9_SAMPLE_FILENAME = "/testfiles/case-09/sample.txt";
    private static final String REFERENCE_CASE9_SUSPECT_FILENAME = "/testfiles/case-09/suspect.txt";

    public Case09Test() {
    }

    @Test
    public void testReferenceCase9Parameter1() throws InterruptedException, TimeoutException {
        System.out.println("testReferenceCase9Parameter1");
        Collection<Sample> replicates = readReplicates(REFERENCE_CASE9_SAMPLE_FILENAME);
        Collection<Sample> suspectSamples = readProfiles(REFERENCE_CASE9_SUSPECT_FILENAME);
        PopulationStatistics popStats = readPopulationStatistics(REFERENCE_NFI_POPULATION_STATISTICS_FILENAME);

        Hypothesis prosecutionHypothesis = new Hypothesis("Prosecution", popStats);
        prosecutionHypothesis.setUnknownCount(1);
        prosecutionHypothesis.setUnknownDropoutProbability(0.1);
        prosecutionHypothesis.setDropInProbability(0.05);
        prosecutionHypothesis.setThetaCorrection(0);

        Hypothesis defenseHypothesis = new Hypothesis("Defense", popStats);
        defenseHypothesis.setUnknownCount(1);
        defenseHypothesis.setUnknownDropoutProbability(0.1);
        defenseHypothesis.setDropInProbability(0.05);
        defenseHypothesis.setThetaCorrection(0);

        for (Sample s : suspectSamples) {
            prosecutionHypothesis.addContributor(s, 0.4);
            defenseHypothesis.addNonContributor(s, 0.4);
        }

        ConfigurationData config = new ConfigurationData();
        config.setDefense(defenseHypothesis);
        config.setProsecution(prosecutionHypothesis);
        config.addProfiles(suspectSamples);
        config.addReplicates(replicates);
        config.setStatistics(popStats);

        SplitDropThreadPool instance = new SplitDropThreadPool();
        instance.doAnalysis(config);
        LikelihoodRatio result = instance.getLikelihoodRatio();

        String expectedResult = "extremely much more likely";
        assertEquals(expectedResult, getProbabilityTerm(result.getOverallRatio()));
    }

    @Test
    public void testReferenceCase9Parameter2() throws InterruptedException, TimeoutException {
        System.out.println("testReferenceCase9Parameter2");
        Collection<Sample> replicates = readReplicates(REFERENCE_CASE9_SAMPLE_FILENAME);
        Collection<Sample> suspectSamples = readProfiles(REFERENCE_CASE9_SUSPECT_FILENAME);
        PopulationStatistics popStats = readPopulationStatistics(REFERENCE_NFI_POPULATION_STATISTICS_FILENAME);

        Hypothesis prosecutionHypothesis = new Hypothesis("Prosecution", popStats);
        prosecutionHypothesis.setUnknownCount(1);
        prosecutionHypothesis.setUnknownDropoutProbability(0.1);
        prosecutionHypothesis.setDropInProbability(0.05);
        prosecutionHypothesis.setThetaCorrection(0);

        Hypothesis defenseHypothesis = new Hypothesis("Defense", popStats);
        defenseHypothesis.setUnknownCount(2);
        defenseHypothesis.setUnknownDropoutProbability(0.1);
        defenseHypothesis.setDropInProbability(0.05);
        defenseHypothesis.setThetaCorrection(0);

        for (Sample s : suspectSamples) {
            prosecutionHypothesis.addContributor(s, 0.4);
            defenseHypothesis.addNonContributor(s, 0.4);
        }

        ConfigurationData config = new ConfigurationData();
        config.setDefense(defenseHypothesis);
        config.setProsecution(prosecutionHypothesis);
        config.addProfiles(suspectSamples);
        config.addReplicates(replicates);
        config.setStatistics(popStats);

        SplitDropThreadPool instance = new SplitDropThreadPool();
        instance.doAnalysis(config);
        LikelihoodRatio result = instance.getLikelihoodRatio();

        String expectedResult = "extremely much less likely";
        assertEquals(expectedResult, getProbabilityTerm(result.getOverallRatio()));

     }
}
