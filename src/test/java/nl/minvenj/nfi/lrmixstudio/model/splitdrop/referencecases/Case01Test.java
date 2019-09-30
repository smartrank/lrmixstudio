package nl.minvenj.nfi.lrmixstudio.model.splitdrop.referencecases;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

import nl.minvenj.nfi.lrmixstudio.domain.Hypothesis;
import nl.minvenj.nfi.lrmixstudio.domain.LikelihoodRatio;
import nl.minvenj.nfi.lrmixstudio.domain.PopulationStatistics;
import nl.minvenj.nfi.lrmixstudio.domain.Sample;
import nl.minvenj.nfi.lrmixstudio.gui.SessionData;
import nl.minvenj.nfi.lrmixstudio.model.splitdrop.threadpool.SplitDropThreadPool;

/**
 *
 * @author dejong
 */
public class Case01Test extends ReferenceCaseTest {

    private static final String REFERENCE_CASE1_SAMPLE_FILENAME = "/testfiles/case-01/sample.txt";
    private static final String REFERENCE_CASE1_SUSPECT_FILENAME = "/testfiles/case-01/suspect.csv";

    @Test
    public void testReferenceCase1() throws InterruptedException, TimeoutException {
        System.out.println("testReferenceCase1");
        final Collection<Sample> replicates = readReplicates(REFERENCE_CASE1_SAMPLE_FILENAME);
        final Collection<Sample> suspectSamples = readProfiles(REFERENCE_CASE1_SUSPECT_FILENAME);
        final PopulationStatistics popStats = readPopulationStatistics(REFERENCE_NFI_POPULATION_STATISTICS_FILENAME);

        final Hypothesis prosecutionHypothesis = new Hypothesis("Prosecution", popStats);
        prosecutionHypothesis.setUnknownCount(0);
        prosecutionHypothesis.setUnknownDropoutProbability(0);
        prosecutionHypothesis.setDropInProbability(0);
        prosecutionHypothesis.setThetaCorrection(0);

        final Hypothesis defenseHypothesis = new Hypothesis("Defense", popStats);
        defenseHypothesis.setUnknownCount(1);
        defenseHypothesis.setUnknownDropoutProbability(0);
        defenseHypothesis.setDropInProbability(0);
        defenseHypothesis.setThetaCorrection(0);

        for (final Sample s : suspectSamples) {
            prosecutionHypothesis.addContributor(s, 0);
            defenseHypothesis.addNonContributor(s, 0);
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

        final String expectedResult = "extremely much more likely";
        assertEquals(expectedResult, getProbabilityTerm(result.getOverallRatio()));
    }
}
