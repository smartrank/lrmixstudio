/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.minvenj.nfi.lrmixstudio.io;

import nl.minvenj.nfi.lrmixstudio.io.PopulationStatisticsReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.minvenj.nfi.lrmixstudio.domain.PopulationStatistics;
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
public class PopulationStatisticsReaderTest {
    
    public static final String FILENAME = "samplePopulationStatistics.csv";
    public PopulationStatisticsReaderTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    public HashMap<String,String> fileContents = new HashMap<>();
    
    @Before
    public void setUp() {
        URL inputUrl = getClass().getResource(FILENAME);
        assertNotNull("Input File '"+FILENAME+"' not found!", inputUrl);
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(inputUrl.getFile()));
            String line = reader.readLine();
            while(line!=null)
            {
                int idx = line.indexOf(",");
                if(idx>=0)
                {
                    if(fileContents.put(line.substring(0, idx), line)!=null)
                        fail("File '"+FILENAME+"' is corrupt: Multiple declarations of " + line.substring(0, idx));
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
     * Test of getStatistics method, of class PopulationStatisticsReader.
     */
    @Test
    public void testGetStatistics() throws IOException {
        System.out.println("getStatistics");
        PopulationStatisticsReader instance = new PopulationStatisticsReader(FILENAME);
        PopulationStatistics result = instance.getStatistics();
        assertNotNull(result);
        assertEquals("SHA-1/CB7923E3B3F4BED5462FCF1A7521E02EAA880EF7", result.getFileHash());
    }


}