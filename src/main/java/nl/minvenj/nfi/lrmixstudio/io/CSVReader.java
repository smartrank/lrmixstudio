/**
 * Copyright (C) 2013, 2014 Netherlands Forensic Institute
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package nl.minvenj.nfi.lrmixstudio.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class reads CSV (Comma Separated Value) files. Note that for simplicity,
 * the class also handles Tab Separated files.
 */
public class CSVReader {

    private static final Logger LOG = LoggerFactory.getLogger(CSVReader.class);
    private static String[] separators = {",", "\t"};
    private final HashingReader hashingReader;
    private final BufferedReader reader;
    private final boolean returnEmptyFields;

    /**
     * Constructor creating a CSV reader object that does not return empty
     * fields.
     *
     * @param fileName The name of the source file
     * @throws FileNotFoundException If the source file could not be found
     * @throws MalformedURLException If the name of the source file was not
     * properly formatted
     */
    public CSVReader(String fileName) throws FileNotFoundException, MalformedURLException {
        this(fileName, false);
    }

    /**
     * Constructor
     *
     * @param fileName The name of the source file
     * @param returnEmptyfields If true, the reader will return empty field
     * found in the input file as empty strings. If false, the reader will skip
     * empty fields.
     * @throws FileNotFoundException If the source file could not be found
     * @throws MalformedURLException If the name of the source file was not
     * properly formatted
     */
    public CSVReader(String fileName, boolean returnEmptyfields) throws FileNotFoundException, MalformedURLException {
        this(new FileReader(resolveFileName(fileName)), returnEmptyfields);
    }

    /**
     * Constructor. Empty fields are not returned.
     *
     * @param inputFile A File object representing the input file
     * @throws FileNotFoundException If the file does not exist
     */
    public CSVReader(File inputFile) throws FileNotFoundException {
        this(inputFile, false);
    }

    /**
     * Constructor
     *
     * @param inputFile A File object representing the input file
     * @param returnEmptyFields true if empty fields are to be returned by the
     * {@link:getFields} method.
     * @throws FileNotFoundException If the file does not exist
     */
    public CSVReader(File inputFile, boolean returnEmptyFields) throws FileNotFoundException {
        this(new FileReader(inputFile), returnEmptyFields);
        LOG.info("Reading file {}", inputFile.getAbsoluteFile());
    }

    /**
     * Constructor
     *
     * @param is An InputStream opened on the input file
     * @param returnEmptyFields true
     */
    public CSVReader(InputStream is, boolean returnEmptyFields) {
        this(new InputStreamReader(is), returnEmptyFields);
    }

    /**
     * Private utility constructor
     *
     * @param reader the BufferedReader from which to read.
     * @param returnEmptyFields true if empty fields are to be returned
     */
    private CSVReader(Reader reader, boolean returnEmptyFields) {
        this.hashingReader = new HashingReader(reader);
        this.reader = new BufferedReader(hashingReader);
        this.returnEmptyFields = returnEmptyFields;
    }

    private static String resolveFileName(String fileName) {
        String resolvedFileName;
        URL url = CSVReader.class.getResource(fileName);
        if (url == null) {
            URI uri = new File(fileName).toURI();
            try {
                resolvedFileName = URLDecoder.decode(uri.getRawPath(), "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                LOG.warn("UTF-8 is unsupported. You will have to live with encoded URLs");
                resolvedFileName = uri.getRawPath();
            }
        } else {
            resolvedFileName = url.getFile();
        }
        LOG.info("Reading file {}", resolvedFileName);
        return resolvedFileName;
    }

    /**
     * Parses a string containing CSV or TSV text
     *
     * @param csvText The input string. Should contain text in CSV or TSV format
     * @return An array of String objects containing the field values
     */
    public String[] parse(String csvText) {
        LOG.trace(csvText);
        String separator = determineSeparator(csvText);

        StringTokenizer tokenizer = new StringTokenizer(csvText, separator, true);
        ArrayList<String> fields = new ArrayList<>();
        boolean foundToken = false;
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (token.equals(separator)) {
                if (!foundToken && returnEmptyFields) {
                    fields.add("");
                } else {
                    foundToken = false;
                }
            } else {
                fields.add(token.trim().replaceAll("\"", ""));
                foundToken = true;
            }
        }
        return fields.toArray(new String[fields.size()]);
    }

    /**
     * Reads the next line from the input file and converts to an array of
     * strings
     *
     * @return An array of strings containing the fields found in the next line
     * of the input file, or null if the end of the file is reached.
     * @throws IOException If there was an error reading from the file.
     */
    public String[] readFields() throws IOException {
        String line;
        do {
            line = reader.readLine();
            if (line == null) {
                LOG.info("File hash = {}", getFileHash());
                return null;
            }
        } while (line.length() == 0);

        return parse(line);
    }

    /**
     * Obtains a signature value for the input file. The general form of this
     * string is ALGORITHM/HASHVALUE, where ALGORITHM is the name of the
     * algorithm used (either XOR or SHA-1) and the value is the hex of the
     * generated hash.
     *
     * @return A String containing the signature of the file contents
     */
    public String getFileHash() {
        return hashingReader.getHash();
    }

    /**
     * Determines the separator used for the string. The separator in use is
     * defined as the first separator to occur in the string
     *
     * @param csvText The string for which to determine the separator
     */
    private String determineSeparator(String csvText) {
        LOG.debug("Determining separator for string {}", csvText);
        int separatorIndex = Integer.MAX_VALUE;
        String separator = null;
        for (int idx = 0; idx < separators.length; idx++) {
            if (csvText.contains(separators[idx])) {
                int occurrenceIndex = csvText.indexOf(separators[idx]);
                LOG.debug("Found separator '{}' at index {}", separators[idx], occurrenceIndex);
                if (occurrenceIndex < separatorIndex) {
                    separator = separators[idx];
                }
            }
        }
        if (separator == null) {
            throw new IllegalArgumentException("Unknown file format! Only Comma Separated and Tab Separated files are supported.");
        }
        return separator;
    }
}
