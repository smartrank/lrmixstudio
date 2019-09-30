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
public class Case06Test extends ReferenceCaseTest {

    private static final String REFERENCE_CASE6_SAMPLE_FILENAME = "/testfiles/case-06/sample.txt";
    private static final String REFERENCE_CASE6_SUSPECT_FILENAME = "/testfiles/case-06/suspect.txt";

    @Test
    public void testReferenceCase6() throws InterruptedException, TimeoutException {
        System.out.println("testReferenceCase6");
        Collection<Sample> replicates = readReplicates(REFERENCE_CASE6_SAMPLE_FILENAME);
        Collection<Sample> suspectSamples = readProfiles(REFERENCE_CASE6_SUSPECT_FILENAME);
        PopulationStatistics popStats = readPopulationStatistics(REFERENCE_NFI_POPULATION_STATISTICS_FILENAME);

        Hypothesis prosecutionHypothesis = new Hypothesis("Prosecution", popStats);
        prosecutionHypothesis.setUnknownCount(1);
        prosecutionHypothesis.setUnknownDropoutProbability(0);
        prosecutionHypothesis.setDropInProbability(0.1);
        prosecutionHypothesis.setThetaCorrection(0.5);

        Hypothesis defenseHypothesis = new Hypothesis("Defense", popStats);
        defenseHypothesis.setUnknownCount(2);
        defenseHypothesis.setUnknownDropoutProbability(0);
        defenseHypothesis.setDropInProbability(0.1);
        defenseHypothesis.setThetaCorrection(0.5);

        for (Sample s : suspectSamples) {
            prosecutionHypothesis.addContributor(s, 0);
            defenseHypothesis.addNonContributor(s, 0);
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

        String expectedResult = "more likely";
        assertEquals(expectedResult, getProbabilityTerm(result.getOverallRatio()));
    }
}
