/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.minvenj.nfi.lrmixstudio.io;

import nl.minvenj.nfi.lrmixstudio.io.CSVReader;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import static nl.minvenj.nfi.lrmixstudio.io.SampleReaderTest.FILENAME_LRMIX;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author dejong
 */
public class CSVReaderTest {
    public static final String[] HEADER_FIELDS = new String[]{"SampleName","Marker","Allele1","Allele2","Allele3","Allele4","Allele5","Allele6","Allele7","Allele8"};
    public static final String[] RECORD_1_FIELDS = new String[]{"AAFI2047NL#01Rep1","D10S1248","13","15","16","17"};
    public static final String[] RECORD_2_FIELDS = new String[]{"AAFI2047NL#01Rep1","vWA","17","18"};
    
    public CSVReaderTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    private HashMap<String,String> fileContents = new HashMap<>();
    
    @Before
    public void setUp() {
        URL inputUrl = getClass().getResource(FILENAME_LRMIX);
        assertNotNull("Input File '"+FILENAME_LRMIX+"' not found!", inputUrl);
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(inputUrl.getFile()));
            String line = reader.readLine();
            while(line!=null)
            {
                int idx = line.indexOf(",");
                if(idx>=0)
                {
                    idx = line.indexOf(",", idx+1);
                }
                if(idx>=0)
                {
                    if(fileContents.put(line.substring(0, idx), line)!=null)
                        fail("File '"+FILENAME_LRMIX+"' is corrupt: Multiple declarations of " + line.substring(0, idx));
                }
                line = reader.readLine();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SampleReaderTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SampleReaderTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of readFields method, of class CSVReader.
     */
    @Test
    public void testReadFields() throws Exception {
        System.out.println("readFields");
        CSVReader instance = new CSVReader("sampleProfile.csv");
        
        // read header fields
        String[] expResult = HEADER_FIELDS;
        String[] result = instance.readFields();
        assertArrayEquals("Evaluating headers", expResult, result);
        
        expResult = RECORD_1_FIELDS;
        result = instance.readFields();
        assertArrayEquals("Evaluating record 1", expResult, result);

        expResult = RECORD_2_FIELDS;
        result = instance.readFields();
        assertArrayEquals("Evaluating record 2", expResult, result);
    }

    /**
     * Test of parse method, of class CSVReader.
     */
    @Test
    public void testParse() {
        System.out.println("parse");
        String csvText = "Field1,Field2,,Field3";
        CSVReader instance = new CSVReader(new ByteArrayInputStream("".getBytes()), true);
        String[] expResult = new String[]{"Field1", "Field2", "", "Field3"};
        String[] result = instance.parse(csvText);
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of getFileHash method, of class CSVReader.
     */
    @Test
    public void testGetFileHash() throws FileNotFoundException, MalformedURLException, IOException {
        System.out.println("getFileHash");
        CSVReader instance = new CSVReader("sampleProfile.csv");
        while (instance.readFields() != null) {
            // Don't care what we read for this test
        }
        String expResult = "SHA-1/9CEA72B81D1AA8D7D204744FD89A7652F5B52C27";
        String result = instance.getFileHash();
        assertEquals(expResult, result);
    }
}