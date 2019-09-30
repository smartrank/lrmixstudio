/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.minvenj.nfi.lrmixstudio.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import nl.minvenj.nfi.lrmixstudio.domain.Allele;
import nl.minvenj.nfi.lrmixstudio.domain.Locus;
import nl.minvenj.nfi.lrmixstudio.domain.Sample;

/**
 *
 * @author dejong
 */
public class SampleReaderTest {
    public static final String FILENAME_LRMIX = "sampleProfile.csv";
    public static final String FILENAME_LRMIX_EMPTYLINES = "sampleProfile_emptyLines.csv";
    public static final String FILENAME_LRMIX_DUPLICATE_ALLELES = "sampleProfile_duplicateAlleles.csv";
    public static final String FILENAME_GENEMAPPER = "genemapperSample.txt";
    public static final String FILENAME_GENEMAPPER_EMPTYLINES = "genemapperSample_emptyLines.txt";
    public static final String FILENAME_EDNA = "eDNASample.csv";
    public static final String FILENAME_LRMIX_V2 = "5716083_RAAK1234NL.txt";
    public static final String FILENAME_LRMIX_V2_EMPTYLINES = "5716083_RAAK1234NL_emptyLines.txt";

    public SampleReaderTest() {
    }
    
    HashMap<String,String> fileContents = new HashMap<>();
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }

    private void prepareFileContents(String fileName) {
        URL inputUrl = getClass().getResource(fileName);
        assertNotNull("Input File '" + fileName + "' not found!", inputUrl);
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(inputUrl.getFile()));
            String line = reader.readLine();
            while (line != null) {
                int idx = line.indexOf(",");
                if (idx >= 0) {
                    idx = line.indexOf(",", idx + 1);
                }
                if (idx >= 0) {
                    if (fileContents.put(line.substring(0, idx).toUpperCase(), line) != null) {
                        fail("File '" + fileName + "' is corrupt: Multiple declarations of " + line.substring(0, idx));
                    }
                }
                line = reader.readLine();
            }
        }
        catch (FileNotFoundException ex) {
            Logger.getLogger(SampleReaderTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }
        catch (IOException ex) {
            Logger.getLogger(SampleReaderTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getSamples method, of class SampleReader.
     */
    @Test
    public void testLRmix() throws FileNotFoundException, IOException {
        System.out.println("testLRmix");
        prepareFileContents(FILENAME_LRMIX);
        SampleReader instance = new SampleReader(FILENAME_LRMIX, false);
        Collection<Sample>result = instance.getSamples();
        
        assertNotNull(result);
        assertEquals(2,result.size());
        
        // Test the profiles
        for(Sample sample : result)
        {
            testSample(sample);
        }
        
        // All samples were tested. The fileContents map should only contain a single entry (the header line)
        assertEquals(1,fileContents.size());
    }

    /**
     * Test of getSamples method, of class SampleReader.
     */
    @Test
    public void testLRmixEmptyLines() throws FileNotFoundException, IOException {
        System.out.println("testLRmixEmptyLines");
        prepareFileContents(FILENAME_LRMIX_EMPTYLINES);
        SampleReader instance = new SampleReader(FILENAME_LRMIX_EMPTYLINES, false);
        Collection<Sample> result = instance.getSamples();

        assertNotNull(result);
        assertEquals(2, result.size());

        // Test the profiles
        for (Sample sample : result) {
            testSample(sample);
        }

        // All samples were tested. The fileContents map should only contain a single entry (the header line)
        assertEquals(1, fileContents.size());
    }

    /**
     * Test of getSamples method, of class SampleReader.
     */
    @Test
    public void testGetSamplesDuplicateAllele() throws FileNotFoundException, IOException {
        System.out.println("testGetSamplesDuplicateAllele");
        SampleReader instance = new SampleReader(FILENAME_LRMIX_DUPLICATE_ALLELES, true);
        Collection<Sample> result = instance.getSamples();

        assertNotNull(result);
        assertEquals(2, result.size());

        // None of the samples should contain duplicate alleles
        for (Sample sample : result) {
            for (Locus locus : sample.getLoci()) {
                ArrayList<String> encounteredAlleles = new ArrayList<>();
                for (Allele allele : locus.getAlleles()) {
                    encounteredAlleles.indexOf(allele.getAllele());
                    Assert.assertEquals("Duplicate Allele '" + allele + "' not ignored in " + sample.getId() + "." + locus.getName() + ":" + locus.getAlleles(), -1, encounteredAlleles.indexOf(allele.getAllele()));
                    encounteredAlleles.add(allele.getAllele());
                }
            }
        }
    }

    @Test
    public void testLRmixV2() throws IOException {
        System.out.println("testLRmixV2");
        prepareFileContents(FILENAME_LRMIX_V2);
        SampleReader instance = new SampleReader(FILENAME_LRMIX_V2, true);
        Collection<Sample> result = instance.getSamples();
        
        assertNotNull(result);
        assertEquals(1, result.size());
        
        Sample sample = result.iterator().next();
        testSampleV2(sample);
    }

    @Test
    public void testLRmixV2EmptyLines() throws IOException {
        System.out.println("testLRmixV2EmptyLines");
        prepareFileContents(FILENAME_LRMIX_V2_EMPTYLINES);
        SampleReader instance = new SampleReader(FILENAME_LRMIX_V2_EMPTYLINES, true);
        Collection<Sample> result = instance.getSamples();

        assertNotNull(result);
        assertEquals(1, result.size());

        Sample sample = result.iterator().next();
        testSampleV2(sample);
    }

    @Test
    public void testEDNA() throws IOException {
        System.out.println("testEDNA");
        SampleReader instance = new SampleReader(FILENAME_EDNA, true);
        Collection<Sample> result = instance.getSamples();

        assertNotNull(result);
        assertEquals(1, result.size());

        Sample sample = result.iterator().next();
        assertEquals("casenumber", instance.getCaseNumber());
        assertEquals("sampleid-sin", sample.getId());
        testLocus(sample, "D10S1248", "14");
        testLocus(sample, "VWA", "16", "19");
        testLocus(sample, "D16S539", "11");
        testLocus(sample, "D2S1338", "19", "21");
        testLocus(sample, "AMEL", "X", "Y");
        testLocus(sample, "D8S1179", "10", "12");
        testLocus(sample, "D21S11", "29", "31.2");
        testLocus(sample, "D18S51", "15", "18");
        testLocus(sample, "D22S1045", "15");
        testLocus(sample, "D19S433", "13", "14");
        testLocus(sample, "TH01", "6", "7");
        testLocus(sample, "FGA", "21", "22");
        testLocus(sample, "D2S441", "11", "14");
        testLocus(sample, "D3S1358", "16", "17");
        testLocus(sample, "D1S1656", "13", "19.3");
        testLocus(sample, "D12S391", "18", "19");
        assertEquals("Sample has unexpected number of loci!", 16, sample.size());
    }

    private void testLocus(Sample sample, String locusName, String... alleles) {
        Locus locus = sample.getLocus(locusName.toUpperCase().replaceAll(" ", ""));
        assertNotNull("Expected locus not found: " + locusName, locus);
        for (String allele : alleles) {
            assertTrue(locus.hasAllele(allele));
        }
        assertEquals(alleles.length, locus.size());
    }

    private void testSample(Sample sample) {
        assertNotNull(sample);
        assertNotNull(sample.getId());
        
        Collection<Locus> loci = sample.getLoci();
        assertNotNull(loci);
        
        for(Locus locus : loci)
        {
            String sourceLine = fileContents.remove((sample.getId() + "," + locus.getName()).toUpperCase());
            assertNotNull("Sourceline for " + sample.getId() + "," + locus.getName() + " not found in " + sample.getSourceFile(), sourceLine);

            Collection<Allele> alleles = locus.getAlleles();
            assertNotNull("Allele collection is null for " + sample.getId() + "," + locus.getName(), alleles);
            
            StringBuilder alleleNumberCsv = new StringBuilder();
            for (Allele allele : alleles) {
                alleleNumberCsv.append(",").append(allele.getAllele());
            }
            
            assertTrue(sourceLine + " did not contain alleles " + alleleNumberCsv.toString(), sourceLine.indexOf(alleleNumberCsv.toString()) > 0);
        }
    }

    private void testSampleV2(Sample sample) {
        assertNotNull(sample);
        assertNotNull(sample.getId());

        Collection<Locus> loci = sample.getLoci();
        assertNotNull(loci);

        for (Locus locus : loci) {
            String sourceLine = fileContents.remove((sample.getId() + "," + locus.getName()).toUpperCase());
            assertNotNull("Sourceline for " + sample.getId() + "," + locus.getName() + " not found in " + sample.getSourceFile(), sourceLine);

            Collection<Allele> alleles = locus.getAlleles();
            assertNotNull("Allele collection is null for " + sample.getId() + "," + locus.getName(), alleles);

            StringBuilder alleleHeightCsv = new StringBuilder();
            for (Allele allele : alleles) {
                alleleHeightCsv.append(",").append(allele.getAllele()).append(",").append(("" + allele.getPeak()).replaceAll("\\.0", "").replaceAll("(\\d)0{5,}[1-9]+", "$1"));
            }

            assertTrue(sourceLine + " did not contain alleles " + alleleHeightCsv.toString(), sourceLine.indexOf(alleleHeightCsv.toString()) > 0);
        }
    }

    /**
     * Test of getCaseNumber method, of class SampleReader.
     */
    @Test
    public void testGetCaseNumber() throws FileNotFoundException, IOException {
        System.out.println("getCaseNumber");
        SampleReader instance = new SampleReader(FILENAME_GENEMAPPER, false);
        String expResult = "20130809113";
        String result = instance.getCaseNumber();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetSampleNameWithReplicateID() throws FileNotFoundException, IOException {
        System.out.println("testGetSampleNameWithReplicateID");
        SampleReader instance = new SampleReader(FILENAME_GENEMAPPER, false);
        Collection<Sample> samples = instance.getSamples();
        assertNotNull("Samples collection was null!", samples);
        assertTrue("Expected 1 sample, but found " + samples.size(), samples.size() == 1);
        Sample sample = samples.iterator().next();
        assertEquals("Sample had an unexpected name: " + sample.getId(), "4550468-AAGG2102NL#01", sample.getId());
    }

    @Test
    public void testGeneMapperEmptyLines() throws IOException {
        System.out.println("testGeneMapperEmptyLines");
        prepareFileContents(FILENAME_GENEMAPPER_EMPTYLINES);
        SampleReader instance = new SampleReader(FILENAME_GENEMAPPER_EMPTYLINES, true);
        Collection<Sample> result = instance.getSamples();

        assertNotNull(result);
        assertEquals(1, result.size());

        Sample sample = result.iterator().next();
        assertEquals("Sample had an unexpected name: " + sample.getId(), "4550468-AAGG2102NL#01", sample.getId());
    }
}
