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
public class Case05Test extends ReferenceCaseTest {

    private static final String REFERENCE_CASE5_SAMPLE_FILENAME = "/testfiles/case-05/sample.txt";
    private static final String REFERENCE_CASE5_SUSPECT_FILENAME = "/testfiles/case-05/suspect.txt";

    @Test
    public void testReferenceCase5Parameters1() throws InterruptedException, TimeoutException {
        System.out.println("testReferenceCase5Parameters1");
        final Collection<Sample> replicates = readReplicates(REFERENCE_CASE5_SAMPLE_FILENAME);
        final Collection<Sample> suspectSamples = readProfiles(REFERENCE_CASE5_SUSPECT_FILENAME);
        final PopulationStatistics popStats = readPopulationStatistics(REFERENCE_NFI_POPULATION_STATISTICS_FILENAME);

        final Hypothesis prosecutionHypothesis = new Hypothesis("Prosecution", popStats);
        prosecutionHypothesis.setUnknownCount(2);
        prosecutionHypothesis.setUnknownDropoutProbability(0);
        prosecutionHypothesis.setDropInProbability(0.1);
        prosecutionHypothesis.setThetaCorrection(0);

        final Hypothesis defenseHypothesis = new Hypothesis("Defense", popStats);
        defenseHypothesis.setUnknownCount(3);
        defenseHypothesis.setUnknownDropoutProbability(0);
        defenseHypothesis.setDropInProbability(0.1);
        defenseHypothesis.setThetaCorrection(0);

        for (final Sample s : suspectSamples) {
            prosecutionHypothesis.addContributor(s, 0.5);
            defenseHypothesis.addNonContributor(s, 0.5);
        }

        final ConfigurationData config = new ConfigurationData();
        config.setDefense(defenseHypothesis);
        config.setProsecution(prosecutionHypothesis);
        config.addProfiles(suspectSamples);
        config.addReplicates(replicates);
        config.setStatistics(popStats);

        final SplitDropThreadPool instance = new SplitDropThreadPool();
        final LikelihoodRatio result = instance.doAnalysis(config);
        
        final String expectedResult = "much more likely";
        assertEquals(expectedResult, getProbabilityTerm(result.getOverallRatio()));
    }

    @Test
    public void testReferenceCase5Parameters2() throws InterruptedException, TimeoutException {
        System.out.println("testReferenceCase5Parameters2");
        final Collection<Sample> replicates = readReplicates(REFERENCE_CASE5_SAMPLE_FILENAME);
        final Collection<Sample> suspectSamples = readProfiles(REFERENCE_CASE5_SUSPECT_FILENAME);
        final PopulationStatistics popStats = readPopulationStatistics(REFERENCE_NFI_POPULATION_STATISTICS_FILENAME);

        final Hypothesis prosecutionHypothesis = new Hypothesis("Prosecution", popStats);
        prosecutionHypothesis.setUnknownCount(2);
        prosecutionHypothesis.setUnknownDropoutProbability(0);
        prosecutionHypothesis.setDropInProbability(0.1);
        prosecutionHypothesis.setThetaCorrection(0.05);

        final Hypothesis defenseHypothesis = new Hypothesis("Defense", popStats);
        defenseHypothesis.setUnknownCount(3);
        defenseHypothesis.setUnknownDropoutProbability(0);
        defenseHypothesis.setDropInProbability(0.1);
        defenseHypothesis.setThetaCorrection(0.05);

        for (final Sample s : suspectSamples) {
            prosecutionHypothesis.addContributor(s, 0.5);
            defenseHypothesis.addNonContributor(s, 0.5);
        }

        final ConfigurationData config = new ConfigurationData();
        config.setDefense(defenseHypothesis);
        config.setProsecution(prosecutionHypothesis);
        config.addProfiles(suspectSamples);
        config.addReplicates(replicates);
        config.setStatistics(popStats);

        final SplitDropThreadPool instance = new SplitDropThreadPool();
        final LikelihoodRatio result = instance.doAnalysis(config);

        final String expectedResult = "more likely";
        assertEquals(expectedResult, getProbabilityTerm(result.getOverallRatio()));
    }

    @Test
    public void testReferenceCase5Parameters3() throws InterruptedException, TimeoutException {
        System.out.println("testReferenceCase5Parameters3");
        final Collection<Sample> replicates = readReplicates(REFERENCE_CASE5_SAMPLE_FILENAME);
        final Collection<Sample> suspectSamples = readProfiles(REFERENCE_CASE5_SUSPECT_FILENAME);
        final PopulationStatistics popStats = readPopulationStatistics(REFERENCE_NFI_POPULATION_STATISTICS_FILENAME);

        final Hypothesis prosecutionHypothesis = new Hypothesis("Prosecution", popStats);
        prosecutionHypothesis.setUnknownCount(2);
        prosecutionHypothesis.setUnknownDropoutProbability(0);
        prosecutionHypothesis.setDropInProbability(0.1);
        prosecutionHypothesis.setThetaCorrection(0.15);

        final Hypothesis defenseHypothesis = new Hypothesis("Defense", popStats);
        defenseHypothesis.setUnknownCount(3);
        defenseHypothesis.setUnknownDropoutProbability(0);
        defenseHypothesis.setDropInProbability(0.1);
        defenseHypothesis.setThetaCorrection(0.15);

        for (final Sample s : suspectSamples) {
            prosecutionHypothesis.addContributor(s, 0.5);
            defenseHypothesis.addNonContributor(s, 0.5);
        }

        final ConfigurationData config = new ConfigurationData();
        config.setDefense(defenseHypothesis);
        config.setProsecution(prosecutionHypothesis);
        config.addProfiles(suspectSamples);
        config.addReplicates(replicates);
        config.setStatistics(popStats);

        final SplitDropThreadPool instance = new SplitDropThreadPool();
        final LikelihoodRatio result = instance.doAnalysis(config);

        final String expectedResult = "approximately equally likely";
        assertEquals(expectedResult, getProbabilityTerm(result.getOverallRatio()));
    }
}
