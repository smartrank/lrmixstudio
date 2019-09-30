package nl.minvenj.nfi.lrmixstudio.model.splitdrop.validation;

import static org.junit.Assert.fail;
import static org.junit.Assume.assumeNotNull;
import static org.junit.Assume.assumeTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import nl.minvenj.nfi.lrmixstudio.domain.Hypothesis;
import nl.minvenj.nfi.lrmixstudio.domain.LikelihoodRatio;
import nl.minvenj.nfi.lrmixstudio.domain.Ratio;
import nl.minvenj.nfi.lrmixstudio.domain.Sample;
import nl.minvenj.nfi.lrmixstudio.io.PopulationStatisticsReader;
import nl.minvenj.nfi.lrmixstudio.io.SampleReader;
import nl.minvenj.nfi.lrmixstudio.model.ConfigurationData;
import nl.minvenj.nfi.lrmixstudio.model.LRMathModel;
import nl.minvenj.nfi.lrmixstudio.model.LRMathModelFactory;

/**
 *
 * @author dejong
 */
public class ValidationTest {

    private static HashMap<String, ValidationCase> _validationCases;
    private static HashMap<String, ExpectedResult> _expectedResults;

    public static class ValidationCase extends ConfigurationData {

        private final String _caseId;
        private final String _caseName;
        private final String _sample;
        private final String _known1;
        private final String _known2;
        private final String _known3;
        private final String _known4;
        private final String _suspect;
        private final String _unknownCountP;
        private final String _unknownCountD;
        private final String _unknownDropoutP;
        private final String _unknownDropoutD;
        private final String _dropoutKnown1;
        private final String _dropoutKnown2;
        private final String _dropoutKnown3;
        private final String _dropoutKnown4;
        private final String _dropoutSuspect;
        private final String _dropin;
        private final String _theta;

        private ValidationCase(String line) {
            // Read csv data and fill the case object
            String[] fields = line.split(",");
            _caseId = fields[0];
            _caseName = fields[1];
            _sample = fields[2];
            _known1 = fields[3];
            _known2 = fields[4];
            _known3 = fields[5];
            _known4 = fields[6];
            _suspect = fields[7];
            _unknownCountP = fields[8];
            _unknownCountD = fields[9];
            _unknownDropoutP = fields[10];
            _unknownDropoutD = fields[11];
            _dropoutKnown1 = fields[12];
            _dropoutKnown2 = fields[13];
            _dropoutKnown3 = fields[14];
            _dropoutKnown4 = fields[15];
            _dropoutSuspect = fields[16];
            _dropin = fields[17];
            _theta = fields[18];

            try {
                setCaseNumber(_caseId);
                
                PopulationStatisticsReader popStatsReader = new PopulationStatisticsReader("Allele frequencies to use in DyNAmix.csv", getClass().getResourceAsStream("data/Allele frequencies to use in DyNAmix.csv"));
                setStatistics(popStatsReader.getStatistics());
                setRareAlleleFrequency(1.0 / (2 * 2085));
                
                SampleReader replicateReader = new SampleReader(_sample, getClass().getResourceAsStream("data/" + _caseName + "/" + _sample), true);
                addReplicates(replicateReader.getSamples());
                
                Hypothesis pros = new Hypothesis("Prosecution", Integer.parseInt(_unknownCountP), getStatistics(), new BigDecimal(_dropin).doubleValue(), new BigDecimal(_unknownDropoutP).doubleValue(), new BigDecimal(_theta).doubleValue());
                setProsecution(pros);
                
                Hypothesis def = new Hypothesis("Defense", Integer.parseInt(_unknownCountD), getStatistics(), new BigDecimal(_dropin).doubleValue(), new BigDecimal(_unknownDropoutD).doubleValue(), new BigDecimal(_theta).doubleValue());
                setDefense(def);
                
                SampleReader suspectReader = new SampleReader(_suspect, getClass().getResourceAsStream("data/" + _caseName + "/" + _suspect), false);
                addProfiles(suspectReader.getSamples());
                for (Sample suspectSample : suspectReader.getSamples()) {
                    pros.addContributor(suspectSample, new BigDecimal(_dropoutSuspect).doubleValue());
                    def.addNonContributor(suspectSample, new BigDecimal(_dropoutSuspect).doubleValue());
                }
                
                addKnown(_known1, _dropoutKnown1);
                addKnown(_known2, _dropoutKnown2);
                addKnown(_known3, _dropoutKnown3);
                addKnown(_known4, _dropoutKnown4);
                
            } catch (IOException ex) {
                Logger.getLogger(ValidationTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        private String getCaseId() {
            return _caseId;
        }

        private void addKnown(String known, String dropoutKnown) throws IOException {
            if (!known.equals("0")) {
                SampleReader knownReader = new SampleReader(known, getClass().getResourceAsStream("data/" + _caseName + "/" + known), false);
                for (Sample knownSample : knownReader.getSamples()) {
                    getProsecution().addContributor(knownSample, new BigDecimal(dropoutKnown).doubleValue());
                    getDefense().addContributor(knownSample, new BigDecimal(dropoutKnown).doubleValue());
                }
            }
        }
    }

    public static class ExpectedResult {

        private final String _caseId;
        private final Ratio _ratio;

        private ExpectedResult(String line) {
            String[] fields = line.split(",");
            _caseId = fields[0];
            try {
                _ratio = new Ratio(fields[2], new BigDecimal(fields[3]).doubleValue(), new BigDecimal(fields[4]).doubleValue());
            } catch (NumberFormatException nfe) {
                throw new RuntimeException(line, nfe);
            }
        }

        private String getCaseId() {
            return _caseId;
        }

        private String getLocus() {
            return _ratio.getLocusName();
        }

        public Ratio getRatio() {
            return _ratio;
        }
    }


    public ValidationTest() {
    }

    private void runCase(String caseId) {
        try {
            ValidationCase currentCase = _validationCases.get(caseId);
            assumeNotNull(currentCase);

            if (currentCase.getProsecution().getUnknownCount() > 2) {
                assumeTrue(Runtime.getRuntime().availableProcessors() > 8);
            }
            if (currentCase.getProsecution().getUnknownCount() > 3) {
                assumeTrue(Runtime.getRuntime().availableProcessors() > 16);
            }

            LRMathModel model = LRMathModelFactory.getMathematicalModel(LRMathModelFactory.getDefaultModelName());
            LikelihoodRatio lr = model.doAnalysis(currentCase);
            for (Ratio ratio : lr.getRatios()) {
                ExpectedResult expected = _expectedResults.get(caseId + ratio.getLocusName().toUpperCase());
                if (expected == null) {
                    fail("Result for case " + caseId + ", locus " + ratio.getLocusName() + " not found!");
                }
                Assert.assertEquals(1, expected.getRatio().getProsecutionProbability() / ratio.getProsecutionProbability(), 0.00000001);
                Assert.assertEquals(1, expected.getRatio().getDefenseProbability() / ratio.getDefenseProbability(), 0.00000001);
                Assert.assertEquals(1, expected.getRatio().getRatio() / ratio.getRatio(), 0.00000001);
            }
            System.out.println("Result: " + lr.getOverallRatio().getRatio());
        } catch (InstantiationException | IllegalAccessException | InterruptedException ex) {
            System.out.println(ex);
        }
    }

    @BeforeClass
    public static void setUpClass() {
        try {
            _validationCases = new HashMap<>();
            _expectedResults = new HashMap<>();
            InputStream caseDescriptions = ValidationTest.class.getResourceAsStream("Validation_data_description_feb2013.csv");
            InputStream expectedResults = ValidationTest.class.getResourceAsStream("Validation_data_LRMix_results_march2013_2.csv");

            BufferedReader caseReader = new BufferedReader(new InputStreamReader(caseDescriptions));
            BufferedReader resultsReader = new BufferedReader(new InputStreamReader(expectedResults));

            // Skip header line for case descriptions
            String line = caseReader.readLine();
            while ((line = caseReader.readLine()) != null) {
                if (!line.matches("\\,*")) {
                    ValidationCase newCase = new ValidationCase(line);
                    _validationCases.put(newCase.getCaseId(), newCase);
                }
            }

            // Skip header line for case results
            line = resultsReader.readLine();
            while ((line = resultsReader.readLine()) != null) {
                if (!line.matches("\\,*")) {
                    line = line.replaceAll("\\\"(\\d+),(\\d+e?-?\\d*)\\\"", "$1\\.$2");
                    ExpectedResult newResult = new ExpectedResult(line);
                    _expectedResults.put(newResult.getCaseId() + newResult.getLocus().toUpperCase(), newResult);
                }
            }

        } catch (IOException ex) {
            System.out.println();
        }
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testCase01() {
        System.out.println("Case 1");
        runCase("1");
    }

    @Test
    public void testCase02() {
        System.out.println("Case 2");
        runCase("2");
    }

    @Test
    public void testCase03() {
        System.out.println("Case 3");
        runCase("3");
    }

    @Test
    public void testCase04() {
        System.out.println("Case 4");
        runCase("4");
    }

    @Test
    public void testCase05() {
        System.out.println("Case 5");
        runCase("5");
    }

    @Test
    public void testCase06() {
        System.out.println("Case 6");
        runCase("6");
    }

    @Test
    public void testCase07() {
        System.out.println("Case 7");
        runCase("7");
    }

    @Test
    public void testCase08() {
        System.out.println("Case 8");
        runCase("8");
    }

    @Test
    public void testCase09() {
        System.out.println("Case 9");
        runCase("9");
    }

    @Test
    public void testCase10() {
        System.out.println("Case 10");
        runCase("10");
    }

    @Test
    public void testCase11() {
        System.out.println("Case 11");
        runCase("11");
    }

    @Test
    public void testCase12() {
        System.out.println("Case 12");
        runCase("12");
    }

    @Test
    public void testCase13() {
        System.out.println("Case 13");
        runCase("13");
    }

    @Test
    public void testCase14() {
        System.out.println("Case 14");
        runCase("14");
    }

    @Test
    public void testCase15() {
        System.out.println("Case 15");
        runCase("15");
    }

    @Test
    public void testCase16() {
        System.out.println("Case 16");
        runCase("16");
    }

    @Test
    public void testCase17() {
        System.out.println("Case 17");
        runCase("17");
    }

    @Test
    public void testCase18() {
        System.out.println("Case 18");
        runCase("18");
    }

    @Test
    public void testCase19() {
        System.out.println("Case 19");
        runCase("19");
    }

    @Test
    public void testCase20() {
        System.out.println("Case 20");
        runCase("20");
    }

    @Test
    public void testCase21() {
        System.out.println("Case 21");
        runCase("21");
    }

    @Test
    public void testCase22() {
        System.out.println("Case 22");
        runCase("22");
    }

    @Test
    public void testCase23() {
        System.out.println("Case 23");
        runCase("23");
    }

    @Test
    public void testCase24() {
        System.out.println("Case 24");
        runCase("24");
    }

    @Test
    public void testCase25() {
        System.out.println("Case 25");
        runCase("25");
    }

    @Test
    public void testCase26() {
        System.out.println("Case 26");
        runCase("26");
    }

    @Test
    public void testCase27() {
        System.out.println("Case 27");
        runCase("27");
    }

    @Test
    public void testCase28() {
        System.out.println("Case 28");
        runCase("28");
    }

    @Test
    public void testCase29() {
        System.out.println("Case 29");
        runCase("29");
    }

    @Test
    public void testCase30() {
        System.out.println("Case 30");
        runCase("30");
    }

    @Test
    public void testCase31() {
        System.out.println("Case 31");
        runCase("31");
    }

    @Test
    public void testCase32() {
        System.out.println("Case 32");
        runCase("32");
    }

    @Test
    public void testCase33() {
        System.out.println("Case 33");
        runCase("33");
    }

    @Test
    public void testCase34() {
        System.out.println("Case 34");
        runCase("34");
    }

    @Test
    public void testCase35() {
        System.out.println("Case 35");
        runCase("35");
    }

    @Test
    public void testCase36() {
        System.out.println("Case 36");
        runCase("36");
    }

    @Test
    public void testCase37() {
        System.out.println("Case 37");
        runCase("37");
    }

    @Test
    public void testCase38() {
        System.out.println("Case 38");
        runCase("38");
    }

    @Test
    public void testCase39() {
        System.out.println("Case 39");
        runCase("39");
    }

    @Test
    public void testCase40() {
        System.out.println("Case 40");
        runCase("40");
    }

    @Test
    public void testCase41() {
        System.out.println("Case 41");
        runCase("41");
    }

    @Test
    public void testCase42() {
        System.out.println("Case 42");
        runCase("42");
    }

    @Test
    public void testCase43() {
        System.out.println("Case 43");
        runCase("43");
    }

    @Test
    public void testCase44() {
        System.out.println("Case 44");
        runCase("44");
    }

    @Test
    public void testCase45() {
        System.out.println("Case 45");
        runCase("45");
    }

    @Test
    public void testCase46() {
        System.out.println("Case 46");
        runCase("46");
    }

    @Test
    public void testCase47() {
        System.out.println("Case 47");
        runCase("47");
    }

    @Test
    public void testCase48() {
        System.out.println("Case 48");
        runCase("48");
    }

    @Test
    public void testCase49() {
        System.out.println("Case 49");
        runCase("49");
    }

    @Test
    public void testCase50() {
        System.out.println("Case 50");
        runCase("50");
    }

    @Test
    public void testCase51() {
        System.out.println("Case 51");
        runCase("51");
    }

    @Test
    public void testCase52() {
        System.out.println("Case 52");
        runCase("52");
    }

    @Test
    public void testCase53() {
        System.out.println("Case 53");
        runCase("53");
    }

    @Test
    public void testCase54() {
        System.out.println("Case 54");
        runCase("54");
    }

    @Test
    public void testCase55() {
        System.out.println("Case 55");
        runCase("55");
    }

    @Test
    public void testCase56() {
        System.out.println("Case 56");
        runCase("56");
    }

    @Test
    public void testCase57() {
        System.out.println("Case 57");
        runCase("57");
    }

    @Test
    public void testCase58() {
        System.out.println("Case 58");
        runCase("58");
    }

    @Test
    public void testCase59() {
        System.out.println("Case 59");
        runCase("59");
    }

    @Test
    public void testCase60() {
        System.out.println("Case 60");
        runCase("60");
    }

    @Test
    public void testCase61() {
        System.out.println("Case 61");
        runCase("61");
    }

    @Test
    public void testCase62() {
        System.out.println("Case 62");
        runCase("62");
    }

    @Test
    public void testCase63() {
        System.out.println("Case 63");
        runCase("63");
    }

    @Test
    public void testCase64() {
        System.out.println("Case 64");
        runCase("64");
    }

    @Test
    public void testCase65() {
        System.out.println("Case 65");
        runCase("65");
    }

    @Test
    public void testCase66() {
        System.out.println("Case 66");
        runCase("66");
    }

    @Test
    public void testCase67() {
        System.out.println("Case 67");
        runCase("67");
    }

    @Test
    public void testCase68() {
        System.out.println("Case 68");
        runCase("68");
    }

    @Test
    public void testCase69() {
        System.out.println("Case 69");
        runCase("69");
    }

    @Test
    public void testCase70() {
        System.out.println("Case 70");
        runCase("70");
    }

    @Test
    public void testCase71() {
        System.out.println("Case 71");
        runCase("71");
    }

    @Test
    public void testCase72() {
        System.out.println("Case 72");
        runCase("72");
    }

    @Test
    public void testCase73() {
        System.out.println("Case 73");
        runCase("73");
    }

    @Test
    public void testCase74() {
        System.out.println("Case 74");
        runCase("74");
    }

    @Test
    public void testCase75() {
        System.out.println("Case 75");
        runCase("75");
    }

    @Test
    public void testCase76() {
        System.out.println("Case 76");
        runCase("76");
    }

    @Test
    public void testCase77() {
        System.out.println("Case 77");
        runCase("77");
    }
}
