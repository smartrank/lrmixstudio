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
public class Case14Test extends ReferenceCaseTest {

    private static final String REFERENCE_SAMPLE_FILENAME = "/testfiles/case-14/sample.csv";
    private static final String REFERENCE_SUSPECT_FILENAME = "/testfiles/case-14/suspect.csv";

    public Case14Test() {
    }

    @Test
    public void testReferenceCase14() throws InterruptedException, TimeoutException {
        System.out.println("testReferenceCase14");
        final Collection<Sample> replicates = readReplicates(REFERENCE_SAMPLE_FILENAME);
        final Collection<Sample> suspectSamples = readProfiles(REFERENCE_SUSPECT_FILENAME);
        final PopulationStatistics popStats = readPopulationStatistics(REFERENCE_NFI_POPULATION_STATISTICS_FILENAME);

        final Hypothesis prosecutionHypothesis = new Hypothesis("Prosecution", popStats);
        prosecutionHypothesis.setUnknownCount(3);
        prosecutionHypothesis.setUnknownDropoutProbability(0.01);
        prosecutionHypothesis.setDropInProbability(0.05);
        prosecutionHypothesis.setThetaCorrection(0);

        final Hypothesis defenseHypothesis = new Hypothesis("Defense", popStats);
        defenseHypothesis.setUnknownCount(4);
        defenseHypothesis.setUnknownDropoutProbability(0.01);
        defenseHypothesis.setDropInProbability(0.05);
        defenseHypothesis.setThetaCorrection(0);

        for (final Sample s : suspectSamples) {
            prosecutionHypothesis.addContributor(s, 0.01);
            defenseHypothesis.addNonContributor(s, 0.01);
        }

        final ConfigurationData config = new ConfigurationData();
        config.setDefense(defenseHypothesis);
        config.setProsecution(prosecutionHypothesis);
        config.addProfiles(suspectSamples);
        config.addReplicates(replicates);
        config.setStatistics(popStats);

        final SplitDropThreadPool instance = new SplitDropThreadPool();
        instance.doAnalysis(config);
        final LikelihoodRatio result = instance.getLikelihoodRatio();

        final String expectedResult = "extremely much more likely";
        assertEquals(expectedResult, getProbabilityTerm(result.getOverallRatio()));
    }
}
