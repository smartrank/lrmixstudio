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
public class Case05Test extends ReferenceCaseTest {

    private static final String REFERENCE_CASE5_SAMPLE_FILENAME = "/testfiles/case-05/sample.txt";
    private static final String REFERENCE_CASE5_SUSPECT_FILENAME = "/testfiles/case-05/suspect.txt";

    @Test
    public void testReferenceCase5Parameters1() throws InterruptedException, TimeoutException {
        assumeTrue(Runtime.getRuntime().availableProcessors() > 4);
        System.out.println("testReferenceCase5Parameters1");
        Collection<Sample> replicates = readReplicates(REFERENCE_CASE5_SAMPLE_FILENAME);
        Collection<Sample> suspectSamples = readProfiles(REFERENCE_CASE5_SUSPECT_FILENAME);
        PopulationStatistics popStats = readPopulationStatistics(REFERENCE_NFI_POPULATION_STATISTICS_FILENAME);

        Hypothesis prosecutionHypothesis = new Hypothesis("Prosecution", popStats);
        prosecutionHypothesis.setUnknownCount(2);
        prosecutionHypothesis.setUnknownDropoutProbability(0);
        prosecutionHypothesis.setDropInProbability(0.1);
        prosecutionHypothesis.setThetaCorrection(0);

        Hypothesis defenseHypothesis = new Hypothesis("Defense", popStats);
        defenseHypothesis.setUnknownCount(3);
        defenseHypothesis.setUnknownDropoutProbability(0);
        defenseHypothesis.setDropInProbability(0.1);
        defenseHypothesis.setThetaCorrection(0);

        for (Sample s : suspectSamples) {
            prosecutionHypothesis.addContributor(s, 0.5);
            defenseHypothesis.addNonContributor(s, 0.5);
        }

        ConfigurationData config = new ConfigurationData();
        config.setDefense(defenseHypothesis);
        config.setProsecution(prosecutionHypothesis);
        config.addProfiles(suspectSamples);
        config.addReplicates(replicates);
        config.setStatistics(popStats);

        SplitDropThreadPool instance = new SplitDropThreadPool();
        LikelihoodRatio result = instance.doAnalysis(config);
        
        String expectedResult = "much more likely";
        assertEquals(expectedResult, getProbabilityTerm(result.getOverallRatio()));
    }

    @Test
    public void testReferenceCase5Parameters2() throws InterruptedException, TimeoutException {
        assumeTrue(Runtime.getRuntime().availableProcessors() > 4);
        System.out.println("testReferenceCase5Parameters2");
        Collection<Sample> replicates = readReplicates(REFERENCE_CASE5_SAMPLE_FILENAME);
        Collection<Sample> suspectSamples = readProfiles(REFERENCE_CASE5_SUSPECT_FILENAME);
        PopulationStatistics popStats = readPopulationStatistics(REFERENCE_NFI_POPULATION_STATISTICS_FILENAME);

        Hypothesis prosecutionHypothesis = new Hypothesis("Prosecution", popStats);
        prosecutionHypothesis.setUnknownCount(2);
        prosecutionHypothesis.setUnknownDropoutProbability(0);
        prosecutionHypothesis.setDropInProbability(0.1);
        prosecutionHypothesis.setThetaCorrection(0.05);

        Hypothesis defenseHypothesis = new Hypothesis("Defense", popStats);
        defenseHypothesis.setUnknownCount(3);
        defenseHypothesis.setUnknownDropoutProbability(0);
        defenseHypothesis.setDropInProbability(0.1);
        defenseHypothesis.setThetaCorrection(0.05);

        for (Sample s : suspectSamples) {
            prosecutionHypothesis.addContributor(s, 0.5);
            defenseHypothesis.addNonContributor(s, 0.5);
        }

        ConfigurationData config = new ConfigurationData();
        config.setDefense(defenseHypothesis);
        config.setProsecution(prosecutionHypothesis);
        config.addProfiles(suspectSamples);
        config.addReplicates(replicates);
        config.setStatistics(popStats);

        SplitDropThreadPool instance = new SplitDropThreadPool();
        LikelihoodRatio result = instance.doAnalysis(config);

        String expectedResult = "more likely";
        assertEquals(expectedResult, getProbabilityTerm(result.getOverallRatio()));
    }

    @Test
    public void testReferenceCase5Parameters3() throws InterruptedException, TimeoutException {
        assumeTrue(Runtime.getRuntime().availableProcessors() > 4);
        System.out.println("testReferenceCase5Parameters3");
        Collection<Sample> replicates = readReplicates(REFERENCE_CASE5_SAMPLE_FILENAME);
        Collection<Sample> suspectSamples = readProfiles(REFERENCE_CASE5_SUSPECT_FILENAME);
        PopulationStatistics popStats = readPopulationStatistics(REFERENCE_NFI_POPULATION_STATISTICS_FILENAME);

        Hypothesis prosecutionHypothesis = new Hypothesis("Prosecution", popStats);
        prosecutionHypothesis.setUnknownCount(2);
        prosecutionHypothesis.setUnknownDropoutProbability(0);
        prosecutionHypothesis.setDropInProbability(0.1);
        prosecutionHypothesis.setThetaCorrection(0.15);

        Hypothesis defenseHypothesis = new Hypothesis("Defense", popStats);
        defenseHypothesis.setUnknownCount(3);
        defenseHypothesis.setUnknownDropoutProbability(0);
        defenseHypothesis.setDropInProbability(0.1);
        defenseHypothesis.setThetaCorrection(0.15);

        for (Sample s : suspectSamples) {
            prosecutionHypothesis.addContributor(s, 0.5);
            defenseHypothesis.addNonContributor(s, 0.5);
        }

        ConfigurationData config = new ConfigurationData();
        config.setDefense(defenseHypothesis);
        config.setProsecution(prosecutionHypothesis);
        config.addProfiles(suspectSamples);
        config.addReplicates(replicates);
        config.setStatistics(popStats);

        SplitDropThreadPool instance = new SplitDropThreadPool();
        LikelihoodRatio result = instance.doAnalysis(config);

        String expectedResult = "approximately equally likely";
        assertEquals(expectedResult, getProbabilityTerm(result.getOverallRatio()));
    }
}
