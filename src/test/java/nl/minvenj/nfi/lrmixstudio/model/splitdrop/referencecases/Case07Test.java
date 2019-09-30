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
public class Case07Test extends ReferenceCaseTest {

    private static final String REFERENCE_CASE7_SAMPLE_FILENAME = "/testfiles/case-07/sample.txt";
    private static final String REFERENCE_CASE7_SUSPECT_FILENAME = "/testfiles/case-07/suspect.txt";
    private static final String REFERENCE_CASE7_VICTIM1_FILENAME = "/testfiles/case-07/victim1.txt";
    private static final String REFERENCE_CASE7_VICTIM2_FILENAME = "/testfiles/case-07/victim2.txt";

    @Test
    public void testReferenceCase7Parameter1() throws InterruptedException, TimeoutException {
        System.out.println("testReferenceCase7Parameter1");
        Collection<Sample> replicates = readReplicates(REFERENCE_CASE7_SAMPLE_FILENAME);
        Collection<Sample> suspectSamples = readProfiles(REFERENCE_CASE7_SUSPECT_FILENAME);
        Collection<Sample> victimSamples = readProfiles(REFERENCE_CASE7_VICTIM1_FILENAME);
        PopulationStatistics popStats = readPopulationStatistics(REFERENCE_NFI_POPULATION_STATISTICS_FILENAME);

        Hypothesis prosecutionHypothesis = new Hypothesis("Prosecution", popStats);
        prosecutionHypothesis.setUnknownCount(1);
        prosecutionHypothesis.setUnknownDropoutProbability(0.2);
        prosecutionHypothesis.setDropInProbability(0.01);
        prosecutionHypothesis.setThetaCorrection(0.03);

        Hypothesis defenseHypothesis = new Hypothesis("Defense", popStats);
        defenseHypothesis.setUnknownCount(2);
        defenseHypothesis.setUnknownDropoutProbability(0.2);
        defenseHypothesis.setDropInProbability(0.01);
        defenseHypothesis.setThetaCorrection(0.03);

        for (Sample s : suspectSamples) {
            prosecutionHypothesis.addContributor(s, 0.2);
            defenseHypothesis.addNonContributor(s, 0.2);
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

        SplitDropThreadPool instance = new SplitDropThreadPool();
        LikelihoodRatio result = instance.doAnalysis(config);

        String expectedResult = "more likely";
        assertEquals(expectedResult, getProbabilityTerm(result.getOverallRatio()));
    }

    @Test
    public void testReferenceCase7Parameter2() throws InterruptedException, TimeoutException {
        System.out.println("testReferenceCase7Parameter2");
        Collection<Sample> replicates = readReplicates(REFERENCE_CASE7_SAMPLE_FILENAME);
        Collection<Sample> suspectSamples = readProfiles(REFERENCE_CASE7_SUSPECT_FILENAME);
        Collection<Sample> victimSamples = readProfiles(REFERENCE_CASE7_VICTIM2_FILENAME);
        PopulationStatistics popStats = readPopulationStatistics(REFERENCE_NFI_POPULATION_STATISTICS_FILENAME);

        Hypothesis prosecutionHypothesis = new Hypothesis("Prosecution", popStats);
        prosecutionHypothesis.setUnknownCount(1);
        prosecutionHypothesis.setUnknownDropoutProbability(0.2);
        prosecutionHypothesis.setDropInProbability(0.01);
        prosecutionHypothesis.setThetaCorrection(0.03);

        Hypothesis defenseHypothesis = new Hypothesis("Defense", popStats);
        defenseHypothesis.setUnknownCount(2);
        defenseHypothesis.setUnknownDropoutProbability(0.2);
        defenseHypothesis.setDropInProbability(0.01);
        defenseHypothesis.setThetaCorrection(0.03);

        for (Sample s : suspectSamples) {
            prosecutionHypothesis.addContributor(s, 0.2);
            defenseHypothesis.addNonContributor(s, 0.2);
        }

        for (Sample s : victimSamples) {
            prosecutionHypothesis.addContributor(s, 0.1);
            defenseHypothesis.addContributor(s, 0.1);
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
