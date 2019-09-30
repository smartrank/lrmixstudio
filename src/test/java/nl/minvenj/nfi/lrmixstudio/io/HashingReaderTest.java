/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.minvenj.nfi.lrmixstudio.io;

import nl.minvenj.nfi.lrmixstudio.io.HashingReader;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
public class HashingReaderTest {
    public static final String TEST_DATA = "This is line 1\nThis is line 2\nThis is line 3";
    public static final String BOGUS_DATA = "This is line 1\nThis is line 2\nThis is line 3\n";
    
    public HashingReaderTest() {
    }

    @BeforeClass
    public static void setUpClass() {
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

    /**
     * Test of getHash method, of class HashingReader.
     */
    @Test
    public void testGetHash() throws NoSuchAlgorithmException, IOException {
        System.out.println("getHash");
        final String[] hexDigits = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};

        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        HashingReader instance = new HashingReader(new InputStreamReader(new ByteArrayInputStream(TEST_DATA.getBytes())));
        BufferedReader reader = new BufferedReader(instance);
        while (reader.readLine() != null) {
            // Do nothing. We don't care about the values we read in this test.
        }

        digest.update(TEST_DATA.getBytes());
        byte[] digestValue = digest.digest();

        String expResult = digest.getAlgorithm() + "/";
        for (int idx = 0; idx < digestValue.length; idx++) {
            expResult += hexDigits[(digestValue[idx] >> 4) & 0x0F] + hexDigits[digestValue[idx] & 0x0F];
        }

        digest.reset();
        digest.update(BOGUS_DATA.getBytes());
        digestValue = digest.digest();
        String unexpResult = digest.getAlgorithm() + "/";
        for (int idx = 0; idx < digestValue.length; idx++) {
            unexpResult += hexDigits[(digestValue[idx] >> 4) & 0x0F] + hexDigits[digestValue[idx] & 0x0F];
        }

        String result = instance.getHash();
        assertEquals(expResult, result);
        assertFalse(unexpResult.equalsIgnoreCase(result));
    }

    /**
     * Test of read method, of class HashingReader.
     */
    @Test
    public void testRead() throws Exception {
        System.out.println("read");
        char[] cbuf = new char[1024];
        int off = 0;
        int len = 1024;
        HashingReader instance = new HashingReader(new InputStreamReader(new ByteArrayInputStream(TEST_DATA.getBytes())));
        int expResult = 44;
        int result = instance.read(cbuf, off, len);
        assertEquals(expResult, result);
        char[] resultData = new char[result];
        System.arraycopy(cbuf, 0, resultData, 0, result);
        assertArrayEquals(TEST_DATA.toCharArray(), resultData);
    }

    /**
     * Test of close method, of class HashingReader.
     */
    @Test
    public void testClose() throws Exception {
        System.out.println("close");
        InputStreamReader is = new InputStreamReader(new ByteArrayInputStream(TEST_DATA.getBytes()));
        HashingReader instance = new HashingReader(is);
        instance.close();
        try {
            is.read();
        } catch (IOException ioe) {
            assertEquals("Stream closed", ioe.getMessage());
        }
    }
}