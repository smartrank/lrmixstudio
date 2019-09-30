package nl.minvenj.nfi.lrmixstudio.model.splitdrop.regression;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

import nl.minvenj.nfi.lrmixstudio.domain.Hypothesis;
import nl.minvenj.nfi.lrmixstudio.domain.LikelihoodRatio;
import nl.minvenj.nfi.lrmixstudio.domain.PopulationStatistics;
import nl.minvenj.nfi.lrmixstudio.domain.Sample;
import nl.minvenj.nfi.lrmixstudio.gui.SessionData;
import nl.minvenj.nfi.lrmixstudio.model.splitdrop.referencecases.ReferenceCaseTest;
import nl.minvenj.nfi.lrmixstudio.model.splitdrop.threadpool.SplitDropThreadPool;

/**
 *
 * @author dejong
 */
public class LRDYN201Test extends ReferenceCaseTest {

    private static final String LRDYN_201_SAMPLE_FILENAME = "/testfiles/regression/LRDYN-201/sampleLRDYN-201.csv";
    private static final String LRDYN_201_SUSPECT_FILENAME = "/testfiles/regression/LRDYN-201/suspectLRDYN-201.csv";
    private static final String LRDYN_201_VICTIM_FILENAME = "/testfiles/regression/LRDYN-201/victimLRDYN-201.csv";
    private static final String LRDYN_201_STATS_FILENAME = "/testfiles/regression/LRDYN-201/sgmLRDYN-201.csv";

    @Test
    public void testRegressionLRDYN201() throws InterruptedException, TimeoutException {
        System.out.println("testReferenceCase1");
        Collection<Sample> replicates = readReplicates(LRDYN_201_SAMPLE_FILENAME);
        Collection<Sample> suspectSamples = readProfiles(LRDYN_201_SUSPECT_FILENAME);
        Collection<Sample> victimSamples = readProfiles(LRDYN_201_VICTIM_FILENAME);
        PopulationStatistics popStats = readPopulationStatistics(LRDYN_201_STATS_FILENAME);

        Hypothesis prosecutionHypothesis = new Hypothesis("Prosecution", popStats);
        prosecutionHypothesis.setUnknownCount(0);
        prosecutionHypothesis.setUnknownDropoutProbability(0);
        prosecutionHypothesis.setDropInProbability(0.05);
        prosecutionHypothesis.setThetaCorrection(0.01);

        Hypothesis defenseHypothesis = new Hypothesis("Defense", popStats);
        defenseHypothesis.setUnknownCount(1);
        defenseHypothesis.setUnknownDropoutProbability(0.1);
        defenseHypothesis.setDropInProbability(0.05);
        defenseHypothesis.setThetaCorrection(0.01);

        for (Sample s : suspectSamples) {
            prosecutionHypothesis.addContributor(s, 0.1);
            defenseHypothesis.addNonContributor(s, 0.1);
        }

        for (Sample s : victimSamples) {
            prosecutionHypothesis.addContributor(s, 0.1);
            defenseHypothesis.addContributor(s, 0.1);
        }

        SessionData session = new SessionData();
        session.setDefense(defenseHypothesis);
        session.setProsecution(prosecutionHypothesis);
        session.setStatistics(popStats);
        session.addReplicates(replicates);
        session.addProfiles(suspectSamples);

        SplitDropThreadPool instance = new SplitDropThreadPool();
        instance.doAnalysis(session);
        LikelihoodRatio result = instance.getLikelihoodRatio();

        // Expected value was the result of calculation with all parameters equal but no duplicate allele 16 in the sample at locus VWA
        assertEquals(260963.92194, result.getOverallRatio().getRatio(), 0.00001);
    }
}
