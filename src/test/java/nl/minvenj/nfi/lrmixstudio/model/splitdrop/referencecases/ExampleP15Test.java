package nl.minvenj.nfi.lrmixstudio.model.splitdrop.referencecases;

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
import nl.minvenj.nfi.lrmixstudio.model.splitdrop.threadpool.SplitDropThreadPool;

/**
 *
 * @author dejong
 */
@RunWith(Parameterized.class)
public class ExampleP15Test extends ReferenceCaseTest {

    private static final String P15_PATH = "/testfiles/Example-p15-single-locus-single-donor/";
    private static final String P15_SAMPLE_FILENAME = P15_PATH + "sample-example-p15.csv";
    private static final String P15_HETEROZYGOTE_SUSPECT_FILENAME = "suspect-example-p15.csv";
    private static final String P15_HOMOZYGOTE_SUSPECT_FILENAME = "homozygote-suspect.csv";
    private static final String P15_POPULATION_STATISTICS_FILENAME = P15_PATH + "allele-frequencies-example-p15.csv";

    @Parameters(name = "{0} Theta = {1}, Relation = {2}")
    public static Collection<Object[]> data() {
        final Object[][] data = new Object[][]{
            {P15_HETEROZYGOTE_SUSPECT_FILENAME, 0.0, Relation.NONE, 21.08466F},
            {P15_HETEROZYGOTE_SUSPECT_FILENAME, 0.1, Relation.NONE, 11.10528F},
            // Parent/Child relationships
            {P15_HETEROZYGOTE_SUSPECT_FILENAME, 0.0, Relation.PARENT_CHILD, 6.32803222319F},
            {P15_HOMOZYGOTE_SUSPECT_FILENAME, 0.0, Relation.PARENT_CHILD, 0.00005642709F},
            {P15_HETEROZYGOTE_SUSPECT_FILENAME, 0.1, Relation.PARENT_CHILD, 4.44387440576F},
            {P15_HOMOZYGOTE_SUSPECT_FILENAME, 0.1, Relation.PARENT_CHILD, 0.00006896558F},
            // Sibling relationships
            {P15_HETEROZYGOTE_SUSPECT_FILENAME, 0.0, Relation.SIBLING, 2.933665696903185F},
            {P15_HOMOZYGOTE_SUSPECT_FILENAME, 0.0, Relation.SIBLING, 0.00009744606},
            {P15_HETEROZYGOTE_SUSPECT_FILENAME, 0.1, Relation.SIBLING, 2.597226},
            {P15_HOMOZYGOTE_SUSPECT_FILENAME, 0.1, Relation.SIBLING, 0.000106949},
            // Cousin relationships
            {P15_HETEROZYGOTE_SUSPECT_FILENAME, 0.0, Relation.COUSIN, 13.31954},
            {P15_HOMOZYGOTE_SUSPECT_FILENAME, 0.0, Relation.COUSIN, 0.0001158341},
            {P15_HETEROZYGOTE_SUSPECT_FILENAME, 0.1, Relation.COUSIN, 8.078027},
            {P15_HOMOZYGOTE_SUSPECT_FILENAME, 0.1, Relation.COUSIN, 0.0001007556},
            // Half-sibling relationships
            {P15_HETEROZYGOTE_SUSPECT_FILENAME, 0.0, Relation.HALF_SIBLING, 9.734498},
            {P15_HOMOZYGOTE_SUSPECT_FILENAME, 0.0, Relation.HALF_SIBLING, 0.00008574356},
            {P15_HETEROZYGOTE_SUSPECT_FILENAME, 0.1, Relation.HALF_SIBLING, 6.347674},
            {P15_HOMOZYGOTE_SUSPECT_FILENAME, 0.1, Relation.HALF_SIBLING, 0.00008733625},
            // Grandparent relationships
            {P15_HETEROZYGOTE_SUSPECT_FILENAME, 0.0, Relation.GRANDPARENT_GRANDCHILD, 9.734498},
            {P15_HOMOZYGOTE_SUSPECT_FILENAME, 0.0, Relation.GRANDPARENT_GRANDCHILD, 0.00008574356},
            {P15_HETEROZYGOTE_SUSPECT_FILENAME, 0.1, Relation.GRANDPARENT_GRANDCHILD, 6.347674},
            {P15_HOMOZYGOTE_SUSPECT_FILENAME, 0.1, Relation.GRANDPARENT_GRANDCHILD, 0.00008733625},
            // Uncle/Aunt relationships
            {P15_HETEROZYGOTE_SUSPECT_FILENAME, 0.0, Relation.AUNT_UNCLE_NIECE_NEPHEW, 9.734498},
            {P15_HOMOZYGOTE_SUSPECT_FILENAME, 0.0, Relation.AUNT_UNCLE_NIECE_NEPHEW, 0.00008574356},
            {P15_HETEROZYGOTE_SUSPECT_FILENAME, 0.1, Relation.AUNT_UNCLE_NIECE_NEPHEW, 6.347674},
            {P15_HOMOZYGOTE_SUSPECT_FILENAME, 0.1, Relation.AUNT_UNCLE_NIECE_NEPHEW, 0.00008733625},};
        return Arrays.asList(data);
    }

    @Rule
    public TestRule watcher = new TestWatcher() {
        @Override
        protected void starting(final Description description) {
            System.out.println("Starting test: " + description.getMethodName());
        }
    };

    private final ConfigurationData _config;
    private final double _expected;

    public ExampleP15Test(final String suspectFilename, final double theta, final Relation relation, final double expected) {
        final PopulationStatistics popStats = readPopulationStatistics(P15_POPULATION_STATISTICS_FILENAME);
        final Collection<Sample> replicates = readReplicates(P15_SAMPLE_FILENAME);
        final Collection<Sample> suspectSamples = readProfiles(P15_PATH + suspectFilename);
        final Hypothesis prosecution = new Hypothesis("Prosecution", 0, popStats, 0.05, 0, theta);
        final Hypothesis defense = new Hypothesis("Defense", 1, popStats, 0.05, 0.1, theta);

        for (final Sample s : suspectSamples) {
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
    public void p15Test() throws InterruptedException, TimeoutException {
        final SplitDropThreadPool instance = new SplitDropThreadPool();
        instance.doAnalysis(_config);
        final LikelihoodRatio result = instance.getLikelihoodRatio();

        assertEquals(new BigDecimal(_expected).setScale(5, RoundingMode.HALF_UP).doubleValue(), result.getOverallRatio().getRatio(), 0.00001);
    }
}
