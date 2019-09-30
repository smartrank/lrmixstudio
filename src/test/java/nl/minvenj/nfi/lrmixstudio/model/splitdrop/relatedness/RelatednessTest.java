package nl.minvenj.nfi.lrmixstudio.model.splitdrop.relatedness;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeoutException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import nl.minvenj.nfi.lrmixstudio.domain.Hypothesis;
import nl.minvenj.nfi.lrmixstudio.domain.LikelihoodRatio;
import nl.minvenj.nfi.lrmixstudio.domain.PopulationStatistics;
import nl.minvenj.nfi.lrmixstudio.domain.Relatedness.Relation;
import nl.minvenj.nfi.lrmixstudio.domain.Sample;
import nl.minvenj.nfi.lrmixstudio.model.ConfigurationData;
import nl.minvenj.nfi.lrmixstudio.model.splitdrop.referencecases.ReferenceCaseTest;
import nl.minvenj.nfi.lrmixstudio.model.splitdrop.threadpool.SplitDropThreadPool;

/**
 *
 * @author dejong
 */
@RunWith(Parameterized.class)
public class RelatednessTest extends ReferenceCaseTest {

    private static final String TESTFILES_PATH = "/testfiles/relatednessTestFiles/";
    private static final String SAMPLE_FILENAME = TESTFILES_PATH + "sample-relatedness.csv";
    private static final String HETEROZYGOTE_SUSPECT_FILENAME = "heterozygote-suspect.csv";
    private static final String HOMOZYGOTE_SUSPECT_FILENAME = "homozygote-suspect.csv";
    private static final String POPULATION_STATISTICS_FILENAME = TESTFILES_PATH + "allele-frequencies-relatedness.csv";

    @Parameters(name = "{0} Theta = {1}, Relation = {2}")
    public static Collection<Object[]> data() {
        Object[][] data = new Object[][]{
            // Parent/Child relationships
            {HETEROZYGOTE_SUSPECT_FILENAME, 0.0, Relation.PARENT_CHILD, 5.3849525258383775},
            {HOMOZYGOTE_SUSPECT_FILENAME, 0.0, Relation.PARENT_CHILD, 0.00005642694F},
            {HETEROZYGOTE_SUSPECT_FILENAME, 0.1, Relation.PARENT_CHILD, 4.011541},
            {HOMOZYGOTE_SUSPECT_FILENAME, 0.1, Relation.PARENT_CHILD, 0.00006896539},
            // Sibling relationships
            {HETEROZYGOTE_SUSPECT_FILENAME, 0.0, Relation.SIBLING, 2.7881509697775178},
            {HOMOZYGOTE_SUSPECT_FILENAME, 0.0, Relation.SIBLING, 9.320477851932745E-5},
            {HETEROZYGOTE_SUSPECT_FILENAME, 0.1, Relation.SIBLING, 2.489488532658792},
            {HOMOZYGOTE_SUSPECT_FILENAME, 0.1, Relation.SIBLING, 0.0001036537},
            // Cousin relationships
            {HETEROZYGOTE_SUSPECT_FILENAME, 0.0, Relation.COUSIN, 10.654894028306222},
            {HOMOZYGOTE_SUSPECT_FILENAME, 0.0, Relation.COUSIN, 0.00009966213},
            {HETEROZYGOTE_SUSPECT_FILENAME, 0.1, Relation.COUSIN, 6.970299684112271},
            {HOMOZYGOTE_SUSPECT_FILENAME, 0.1, Relation.COUSIN, 0.00009244957},
            // Half-sibling relationships            
            {HETEROZYGOTE_SUSPECT_FILENAME, 0.0, Relation.HALF_SIBLING, 8.034068132709404},
            {HOMOZYGOTE_SUSPECT_FILENAME, 0.0, Relation.HALF_SIBLING, 0.00007938643},
            {HETEROZYGOTE_SUSPECT_FILENAME, 0.1, Relation.HALF_SIBLING, 5.59479713239053},
            {HOMOZYGOTE_SUSPECT_FILENAME, 0.1, Relation.HALF_SIBLING, 0.00008302558},
            // Grandparent relationships
            {HETEROZYGOTE_SUSPECT_FILENAME, 0.0, Relation.GRANDPARENT_GRANDCHILD, 8.034068132709404},
            {HOMOZYGOTE_SUSPECT_FILENAME, 0.0, Relation.GRANDPARENT_GRANDCHILD, 0.00007938643},
            {HETEROZYGOTE_SUSPECT_FILENAME, 0.1, Relation.GRANDPARENT_GRANDCHILD, 5.59479713239053},
            {HOMOZYGOTE_SUSPECT_FILENAME, 0.1, Relation.GRANDPARENT_GRANDCHILD, 0.00008302558},
            // Uncle/Aunt relationships
            {HETEROZYGOTE_SUSPECT_FILENAME, 0.0, Relation.AUNT_UNCLE_NIECE_NEPHEW, 8.034068132709404},
            {HOMOZYGOTE_SUSPECT_FILENAME, 0.0, Relation.AUNT_UNCLE_NIECE_NEPHEW, 0.00007938643},
            {HETEROZYGOTE_SUSPECT_FILENAME, 0.1, Relation.AUNT_UNCLE_NIECE_NEPHEW, 5.59479713239053},
            {HOMOZYGOTE_SUSPECT_FILENAME, 0.1, Relation.AUNT_UNCLE_NIECE_NEPHEW, 0.00008302558}};
        return Arrays.asList(data);
    }

    @Rule
    public TestRule watcher = new TestWatcher() {
        protected void starting(Description description) {
            System.out.println("Starting test: " + description.getMethodName());
        }
    };

    private final ConfigurationData _config;
    private final double _expected;

    public RelatednessTest(String suspectFilename, double theta, Relation relation, double expected) {
        PopulationStatistics popStats = readPopulationStatistics(POPULATION_STATISTICS_FILENAME);
        Collection<Sample> replicates = readReplicates(SAMPLE_FILENAME);
        Collection<Sample> suspectSamples = readProfiles(TESTFILES_PATH + suspectFilename);
        Hypothesis prosecution = new Hypothesis("Prosecution", 0, popStats, 0.05, 0, theta);
        Hypothesis defense = new Hypothesis("Defense", 1, popStats, 0.05, 0.1, theta);

        for (Sample s : suspectSamples) {
            prosecution.addContributor(s, 0.1);
            defense.addNonContributor(s, 0);
            defense.getRelatedness().setRelative(s);
        }

        defense.getRelatedness().setRelation(relation);

        _config = new ConfigurationData();
        _config.setDefense(defense);
        _config.setProsecution(prosecution);
        _config.addReplicates(replicates);
        _config.addProfiles(suspectSamples);
        _config.setStatistics(popStats);
        _config.setThreadCount(1);

        _expected = expected;
    }

    @Test
    public void relatednessTest() throws InterruptedException, TimeoutException {
        SplitDropThreadPool instance = new SplitDropThreadPool();
        instance.doAnalysis(_config);
        LikelihoodRatio result = instance.getLikelihoodRatio();

        assertEquals(new BigDecimal(_expected).setScale(7, RoundingMode.HALF_UP).doubleValue(), (double) result.getOverallRatio().getRatio(), 0.0000001);
    }
}
