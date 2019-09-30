package nl.minvenj.nfi.lrmixstudio.model.splitdrop.referencecases;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

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
public class Case04Test extends ReferenceCaseTest {

    private static final String REFERENCE_CASE4_SAMPLE_FILENAME = "/testfiles/case-04/sample.txt";
    private static final String REFERENCE_CASE4_SUSPECT_FILENAME = "/testfiles/case-04/suspect.txt";
    private static final String REFERENCE_CASE4_VICTIM_FILENAME = "/testfiles/case-04/victim.txt";

    @Test
    public void testReferenceCase4Parameters1() throws InterruptedException, TimeoutException {
        assumeTrue(Runtime.getRuntime().availableProcessors() > 4);
        System.out.println("testReferenceCase4Parameters1");
        Collection<Sample> replicates = readReplicates(REFERENCE_CASE4_SAMPLE_FILENAME);
        Collection<Sample> suspectSamples = readProfiles(REFERENCE_CASE4_SUSPECT_FILENAME);
        Collection<Sample> victimSamples = readProfiles(REFERENCE_CASE4_VICTIM_FILENAME);
        PopulationStatistics popStats = readPopulationStatistics(REFERENCE_NFI_POPULATION_STATISTICS_FILENAME);

        Hypothesis prosecutionHypothesis = new Hypothesis("Prosecution", popStats);
        prosecutionHypothesis.setUnknownCount(2);
        prosecutionHypothesis.setUnknownDropoutProbability(0.3);
        prosecutionHypothesis.setDropInProbability(0.05);
        prosecutionHypothesis.setThetaCorrection(0);

        Hypothesis defenseHypothesis = new Hypothesis("Defense", popStats);
        defenseHypothesis.setUnknownCount(3);
        defenseHypothesis.setUnknownDropoutProbability(0.3);
        defenseHypothesis.setDropInProbability(0.05);
        defenseHypothesis.setThetaCorrection(0);

        for (Sample s : suspectSamples) {
            prosecutionHypothesis.addContributor(s, 0.3);
            defenseHypothesis.addNonContributor(s, 0.3);
        }

        for (Sample s : victimSamples) {
            prosecutionHypothesis.addContributor(s, 0);
            defenseHypothesis.addContributor(s, 0);
        }

        ConfigurationData config = new ConfigurationData();
        config.setDefense(defenseHypothesis);
        config.setProsecution(prosecutionHypothesis);
        config.addReplicates(replicates);
        config.addProfiles(suspectSamples);
        config.addProfiles(victimSamples);
        config.setStatistics(popStats);
        config.setCaseNumber("Reference Case 4");

        SplitDropThreadPool instance = new SplitDropThreadPool();
        instance.doAnalysis(config);
        LikelihoodRatio result = instance.getLikelihoodRatio();

        String expectedResult = "extremely much more likely";
        assertEquals(expectedResult, getProbabilityTerm(result.getOverallRatio()));
    }

    @Test
    public void testReferenceCase4Parameters2() throws InterruptedException, TimeoutException {
        assumeTrue(Runtime.getRuntime().availableProcessors() > 4);
        System.out.println("testReferenceCase4Parameters2");
        Collection<Sample> replicates = readReplicates(REFERENCE_CASE4_SAMPLE_FILENAME);
        Collection<Sample> suspectSamples = readProfiles(REFERENCE_CASE4_SUSPECT_FILENAME);
        PopulationStatistics popStats = readPopulationStatistics(REFERENCE_NFI_POPULATION_STATISTICS_FILENAME);

        Hypothesis prosecutionHypothesis = new Hypothesis("Prosecution", popStats);
        prosecutionHypothesis.setUnknownCount(2);
        prosecutionHypothesis.setUnknownDropoutProbability(0.2);
        prosecutionHypothesis.setDropInProbability(0.05);
        prosecutionHypothesis.setThetaCorrection(0.03);

        Hypothesis defenseHypothesis = new Hypothesis("Defense", popStats);
        defenseHypothesis.setUnknownCount(3);
        defenseHypothesis.setUnknownDropoutProbability(0.2);
        defenseHypothesis.setDropInProbability(0.05);
        defenseHypothesis.setThetaCorrection(0.03);

        for (Sample s : suspectSamples) {
            prosecutionHypothesis.addContributor(s, 0.4);
            defenseHypothesis.addNonContributor(s, 0.4);
        }

        ConfigurationData config = new ConfigurationData();
        config.setDefense(defenseHypothesis);
        config.setProsecution(prosecutionHypothesis);
        config.addReplicates(replicates);
        config.addProfiles(suspectSamples);
        config.setStatistics(popStats);

        SplitDropThreadPool instance = new SplitDropThreadPool();
        instance.doAnalysis(config);
        LikelihoodRatio result = instance.getLikelihoodRatio();

        String expectedResult = "much more likely";
        assertEquals(expectedResult, getProbabilityTerm(result.getOverallRatio()));
    }
}
