package nl.minvenj.nfi.lrmixstudio.model.splitdrop.regression;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

import nl.minvenj.nfi.lrmixstudio.domain.Hypothesis;
import nl.minvenj.nfi.lrmixstudio.domain.LikelihoodRatio;
import nl.minvenj.nfi.lrmixstudio.domain.PopulationStatistics;
import nl.minvenj.nfi.lrmixstudio.domain.Sample;
import nl.minvenj.nfi.lrmixstudio.gui.ApplicationSettings;
import nl.minvenj.nfi.lrmixstudio.gui.SessionData;
import nl.minvenj.nfi.lrmixstudio.model.splitdrop.referencecases.ReferenceCaseTest;
import nl.minvenj.nfi.lrmixstudio.model.splitdrop.threadpool.SplitDropThreadPool;

/**
 * Regression test for LRDYN-262.
 *
 * Summary:
 *  If a reference profile contains an empty locus, the LR for this locus should be 1 (or the locus should be ignored).
 */
public class LRDYN262Test extends ReferenceCaseTest {

    private static final String LRDYN_262_SAMPLE_FILENAME = "/testfiles/regression/LRDYN-262/sample-LRDYN-262.csv";
    private static final String LRDYN_262_SUSPECT_FILENAME = "/testfiles/regression/LRDYN-262/suspect-LRDYN-262.csv";
    private static final String LRDYN_262_STATS_FILENAME = "/testfiles/regression/LRDYN-262/frequencies-LRDYN-262.csv";

    @Test
    public void testRegressionLRDYN262() throws InterruptedException, TimeoutException {
        System.out.println("testRegressionLRDYN262");
        final Collection<Sample> replicates = readReplicates(LRDYN_262_SAMPLE_FILENAME);
        final Collection<Sample> suspectSamples = readProfiles(LRDYN_262_SUSPECT_FILENAME);
        final PopulationStatistics popStats = readPopulationStatistics(LRDYN_262_STATS_FILENAME);

        final Hypothesis prosecutionHypothesis = new Hypothesis("Prosecution", popStats);
        prosecutionHypothesis.setUnknownCount(0);
        prosecutionHypothesis.setUnknownDropoutProbability(0);
        prosecutionHypothesis.setDropInProbability(0.05);
        prosecutionHypothesis.setThetaCorrection(0.03);

        final Hypothesis defenseHypothesis = new Hypothesis("Defense", popStats);
        defenseHypothesis.setUnknownCount(1);
        defenseHypothesis.setUnknownDropoutProbability(0.1);
        defenseHypothesis.setDropInProbability(0.05);
        defenseHypothesis.setThetaCorrection(0.03);

        for (final Sample s : suspectSamples) {
            prosecutionHypothesis.addContributor(s, 0.1);
            defenseHypothesis.addNonContributor(s, 0.1);
        }

        final SessionData session = new SessionData();
        session.setDefense(defenseHypothesis);
        session.setProsecution(prosecutionHypothesis);
        session.setStatistics(popStats);
        session.addReplicates(replicates);
        session.addProfiles(suspectSamples);

        final SplitDropThreadPool instance = new SplitDropThreadPool();
        instance.doAnalysis(session);
        final LikelihoodRatio result = instance.getLikelihoodRatio();

        // Expected value was the result of calculation with all parameters equal but no duplicate allele 16 in the sample at locus VWA
        assertEquals(1.0, result.getOverallRatio().getRatio(), 0.00001);
    }
}
