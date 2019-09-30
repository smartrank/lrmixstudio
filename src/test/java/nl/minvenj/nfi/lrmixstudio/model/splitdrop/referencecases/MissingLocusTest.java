package nl.minvenj.nfi.lrmixstudio.model.splitdrop.referencecases;


import static org.junit.Assert.fail;

import static nl.minvenj.nfi.lrmixstudio.model.splitdrop.referencecases.ReferenceCaseTest.REFERENCE_NFI_POPULATION_STATISTICS_FILENAME;

import java.util.Collection;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

import nl.minvenj.nfi.lrmixstudio.domain.Hypothesis;
import nl.minvenj.nfi.lrmixstudio.domain.PopulationStatistics;
import nl.minvenj.nfi.lrmixstudio.domain.Sample;
import nl.minvenj.nfi.lrmixstudio.model.ConfigurationData;
import nl.minvenj.nfi.lrmixstudio.model.splitdrop.threadpool.SplitDropThreadPool;

/**
 *
 * @author dejong
 */
public class MissingLocusTest extends ReferenceCaseTest {

    private static final String REFERENCE_SAMPLE_FILENAME = "/testfiles/MissingLocus/sample.csv";
    private static final String REFERENCE_SUSPECT_FILENAME = "/testfiles/MissingLocus/suspect.csv";

    public MissingLocusTest() {
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMissingLocus() throws InterruptedException, TimeoutException {
        System.out.println("testMissingLocus");
        Collection<Sample> replicates = readReplicates(REFERENCE_SAMPLE_FILENAME);
        Collection<Sample> suspectSamples = readProfiles(REFERENCE_SUSPECT_FILENAME);
        PopulationStatistics popStats = readPopulationStatistics(REFERENCE_NFI_POPULATION_STATISTICS_FILENAME);

        Hypothesis prosecutionHypothesis = new Hypothesis("Prosecution", popStats);
        prosecutionHypothesis.setUnknownCount(1);
        prosecutionHypothesis.setUnknownDropoutProbability(0.01);
        prosecutionHypothesis.setDropInProbability(0.05);
        prosecutionHypothesis.setThetaCorrection(0);

        Hypothesis defenseHypothesis = new Hypothesis("Defense", popStats);
        defenseHypothesis.setUnknownCount(2);
        defenseHypothesis.setUnknownDropoutProbability(0.01);
        defenseHypothesis.setDropInProbability(0.05);
        defenseHypothesis.setThetaCorrection(0);

        for (Sample s : suspectSamples) {
            prosecutionHypothesis.addContributor(s, 0.01);
            defenseHypothesis.addNonContributor(s, 0.01);
        }

        ConfigurationData config = new ConfigurationData();
        config.setDefense(defenseHypothesis);
        config.setProsecution(prosecutionHypothesis);
        config.addProfiles(suspectSamples);
        config.addReplicates(replicates);
        config.setStatistics(popStats);

        SplitDropThreadPool instance = new SplitDropThreadPool();
        instance.doAnalysis(config);
        fail("Expected an exception, but no such luck...");
    }
}
