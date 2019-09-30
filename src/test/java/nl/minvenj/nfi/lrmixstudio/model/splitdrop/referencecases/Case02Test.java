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
public class Case02Test extends ReferenceCaseTest {

    private static final String REFERENCE_CASE2_SAMPLE_FILENAME = "/testfiles/case-02/sample.txt";
    private static final String REFERENCE_CASE2_SUSPECT_FILENAME = "/testfiles/case-02/suspect.txt";

    @Test
    public void testReferenceCase2() throws InterruptedException, TimeoutException {
        System.out.println("testReferenceCase2");
        final Collection<Sample> replicates = readReplicates(REFERENCE_CASE2_SAMPLE_FILENAME);
        final Collection<Sample> suspectSamples = readProfiles(REFERENCE_CASE2_SUSPECT_FILENAME);
        final PopulationStatistics popStats = readPopulationStatistics(REFERENCE_NFI_POPULATION_STATISTICS_FILENAME);

        final Hypothesis prosecutionHypothesis = new Hypothesis("Prosecution", popStats);
        prosecutionHypothesis.setUnknownCount(0);
        prosecutionHypothesis.setUnknownDropoutProbability(0.1);
        prosecutionHypothesis.setDropInProbability(0.05);
        prosecutionHypothesis.setThetaCorrection(0);

        final Hypothesis defenseHypothesis = new Hypothesis("Defense", popStats);
        defenseHypothesis.setUnknownCount(1);
        defenseHypothesis.setUnknownDropoutProbability(0.1);
        defenseHypothesis.setDropInProbability(0.05);
        defenseHypothesis.setThetaCorrection(0);

        for (final Sample s : suspectSamples) {
            prosecutionHypothesis.addContributor(s, 0.2);
            defenseHypothesis.addNonContributor(s, 0.2);
        }

        final ConfigurationData config = new ConfigurationData();
        config.setDefense(defenseHypothesis);
        config.setProsecution(prosecutionHypothesis);
        config.addReplicates(replicates);
        config.addProfiles(suspectSamples);
        config.setStatistics(popStats);

        final SplitDropThreadPool instance = new SplitDropThreadPool();
        final LikelihoodRatio result = instance.doAnalysis(config);
//        LikelihoodRatio result = instance.getLikelihoodRatio();

        final String expectedResult = "more likely";
        assertEquals(expectedResult, getProbabilityTerm(result.getOverallRatio()));
    }
}
