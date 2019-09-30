package nl.minvenj.nfi.lrmixstudio.model.splitdrop.regression;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

import nl.minvenj.nfi.lrmixstudio.domain.Hypothesis;
import nl.minvenj.nfi.lrmixstudio.domain.LikelihoodRatio;
import nl.minvenj.nfi.lrmixstudio.domain.PopulationStatistics;
import nl.minvenj.nfi.lrmixstudio.domain.Sample;
import nl.minvenj.nfi.lrmixstudio.model.ConfigurationData;
import nl.minvenj.nfi.lrmixstudio.model.splitdrop.referencecases.ReferenceCaseTest;
import nl.minvenj.nfi.lrmixstudio.model.splitdrop.threadpool.SplitDropThreadPool;

/**
 *
 * @author dejong
 */
public class LRDYN204Test extends ReferenceCaseTest {

    private static final String REFERENCE_CASE2_SAMPLE_FILENAME = "/testfiles/regression/LRDYN-204/sample.txt";
    private static final String REFERENCE_CASE2_SUSPECT_FILENAME = "/testfiles/regression/LRDYN-204/suspect.txt";
    private static final String POPULATION_STATISTICS_LRDYN_204 = "/testfiles/regression/LRDYN-204/frequencies_NFI_LRDYN-204.csv";

    @Test
    public void testLRDYN204() throws InterruptedException, TimeoutException {
        System.out.println("testLRDYN204");
        Collection<Sample> replicates = readReplicates(REFERENCE_CASE2_SAMPLE_FILENAME);
        Collection<Sample> suspectSamples = readProfiles(REFERENCE_CASE2_SUSPECT_FILENAME);
        PopulationStatistics popStats = readPopulationStatistics(POPULATION_STATISTICS_LRDYN_204);

        Hypothesis prosecutionHypothesis = new Hypothesis("Prosecution", popStats);
        prosecutionHypothesis.setUnknownCount(0);
        prosecutionHypothesis.setUnknownDropoutProbability(0.1);
        prosecutionHypothesis.setDropInProbability(0.05);
        prosecutionHypothesis.setThetaCorrection(0);

        Hypothesis defenseHypothesis = new Hypothesis("Defense", popStats);
        defenseHypothesis.setUnknownCount(1);
        defenseHypothesis.setUnknownDropoutProbability(0.1);
        defenseHypothesis.setDropInProbability(0.05);
        defenseHypothesis.setThetaCorrection(0);

        for (Sample s : suspectSamples) {
            prosecutionHypothesis.addContributor(s, 0.2);
            defenseHypothesis.addNonContributor(s, 0.2);
        }

        ConfigurationData config = new ConfigurationData();
        config.setDefense(defenseHypothesis);
        config.setProsecution(prosecutionHypothesis);
        config.addReplicates(replicates);
        config.addProfiles(suspectSamples);
        config.setStatistics(popStats);
        
        SplitDropThreadPool instance = new SplitDropThreadPool();
        LikelihoodRatio result = instance.doAnalysis(config);

        assertEquals(55.43264, result.getOverallRatio().getRatio(), 0.00001);
    }
}
