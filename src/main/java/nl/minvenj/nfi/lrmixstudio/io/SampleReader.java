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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.minvenj.nfi.lrmixstudio.domain.Allele;
import nl.minvenj.nfi.lrmixstudio.domain.Locus;
import nl.minvenj.nfi.lrmixstudio.domain.Sample;

/**
 * This class reads the contents of a Comma Separated File and attempts to
 * interpret this as a collection of DNA profiles.
 */
public class SampleReader {

    private static final Logger LOG = LoggerFactory.getLogger(SampleReader.class);
    private static final Pattern ALLELE_PATTERN = Pattern.compile("allele\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern HEIGHT_PATTERN = Pattern.compile("height\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
    private static final String HEIGHT = "Height";
    private static final String OFF_LADDER_ALLELE = "OL";

    private final LinkedHashMap<String, Sample> _samples = new LinkedHashMap<>();
    private String _caseNumber = "";
    private String _fileName;
    private String _fileHash;

    /**
     * Constructor
     *
     * @param fileName The name of the CSV file (used only for informational
     * purposes, as the actual data is read from the supplied input stream.
     * @param is An {@link InputStream}
     * @param treatAsMixProfile true if samples are to be treated as mix
     *                          profiles, in which case duplicate alleles are
     *                          ignored. false if samples are to be treated as
     *                          reference profiles., in which case duplicate
     *                          alleles are not ignored and any locus with a
     *                          single allele is treated as homozygotic.
     *
     * @throws FileNotFoundException If the file could not be found
     * @throws IOException If there was an error reading from the file
     */
    public SampleReader(final String fileName, final InputStream is, final boolean treatAsMixProfile) throws FileNotFoundException, IOException {
        final CSVReader reader = new CSVReader(is, false);
        readFile(fileName, reader, treatAsMixProfile);
    }

    /**
     * Attempts to open the named file, read the contents and parse as a
     * sequence of DNA samples
     *
     * @param fileName The getName of the input file
     * @param treatAsMixProfile true if duplicate alleles at any given     * locus should be ignored.
     * @throws FileNotFoundException If the file was not found
     * @throws IOException If there was an error reading from the file
     */
    public SampleReader(final String fileName, final boolean treatAsMixProfile) throws FileNotFoundException, IOException {
        final CSVReader reader = new CSVReader(fileName);
        readFile(fileName, reader, treatAsMixProfile);
    }

    /**
     * Attempts to open and read the supplied file and parse the contents as a
     * sequence of DNA samples
     *
     * @param inputFile The input file
     * @param treatAsMixProfile true if duplicate alleles at any given     * locus should be ignored.
     * @throws FileNotFoundException If the file does not exist
     * @throws IOException If there was an error reading from the file
     * @throws NullPointerException If the file parameter was null
     */
    public SampleReader(final File inputFile, final boolean treatAsMixProfile) throws FileNotFoundException, IOException {
        if (inputFile == null) {
            throw new NullPointerException("Input File was null");
        }
        if (!inputFile.exists()) {
            throw new FileNotFoundException(inputFile.getAbsolutePath());
        }
        final CSVReader reader = new CSVReader(inputFile);
        readFile(inputFile.getAbsolutePath(), reader, treatAsMixProfile);
    }

    /**
     * Determines the file type and reads the samples within.
     *
     * @param fileName the getName of the file. Used for inclusion into the
     * Sample objects, so could be any distinguishing string. Does not need to
     * be unique.
     * @param reader A CSVReader for getting the file contents
     * @param treatAsMixProfile true if duplicate alleles at any given     * locus should be ignored.
     * @throws IOException If there was an error reading from the file.
     */
    private void readFile(final String fileName, final CSVReader reader, final boolean treatAsMixProfile) throws IOException {
        LOG.debug("Reading file '" + fileName + "'");
        final String[] headers = reader.readFields();

        // Determine file type.
        if (isGeneMapperFile(headers)) {
            readGenemapperFile(headers, fileName, reader, treatAsMixProfile);
        }
        else {
            if (isEDnaFile(headers)) {
                readEDnaFile(headers, fileName, reader, treatAsMixProfile);
            }
            else {
                readLRMixFile(headers, fileName, reader, treatAsMixProfile);
            }
        }

        for (final Sample sample : _samples.values()) {
            sample.setSourceFileHash(reader.getFileHash());
        }
        _fileName = fileName;
        _fileHash = reader.getFileHash();
    }

    private static boolean isGeneMapperFile(final String[] headers) {
        return headers[0].equalsIgnoreCase("Sample File");
    }

    private static boolean isEDnaFile(final String[] headers) {
        final String[] eDnaHeaders = {"UD1", "UD2", "UD3", "Panel", "Marker", "Dye", "Allele 1", "Height 1", "Allele 2", "Height 2"};
        return Arrays.equals(headers, eDnaHeaders);
    }

    /**
     * @return A collection of {@link Sample} objects representing the samples
     * in the file
     */
    public Collection<Sample> getSamples() {
        return _samples.values();
    }

    /**
     * @return The case number for the samples stored in the file. Note that the
     * LRMix format does not store case number information. For this filetype,
     * this method returns an empty string.
     */
    public String getCaseNumber() {
        return _caseNumber;
    }

    /**
     * @return The name of the file from which the samples were read
     */
    public String getFileName() {
        return _fileName;
    }

    /**
     * @return The hash of the input data
     */
    public String getFileHash() {
        return _fileHash;
    }

    /**
     * Reads a Genemapper file
     *
     * @param fileName The getName of the file. used as distinguishing feature
     * between samples from various files. Needs not be unique.
     * @param reader A CSVReader for getting the file contents
     * @param treatAsMixProfile true if duplicate alleles at any given     * locus should be ignored.
     * @throws IOException if an error occurs reading from the file
     */
    private void readGenemapperFile(final String[] headers, final String fileName, final CSVReader reader, final boolean treatAsMixProfile) throws IOException {
        LOG.debug("Reading Genemapper file");
        // Read lines and store loci and alleles in samples until no more line available
        String[] fields;
        int replicateId = 1;
        boolean hasReplicates = false;
        while ((fields = reader.readFields()) != null) {
            if (fields.length > 0) {
                // Get sample name in order to extract the sample id and case number
                final String sampleName = fields[1];
                final String baseId = checkCaseNumber(sampleName);

                // We may have a file containing replicates but we may also have a file containing profiles
                // In the first case, we will see a number of samples with the same name. In this case we should append some replicate counter.
                // If we have seen this sample ID before...
                if (!hasReplicates && _samples.containsKey(baseId)) {
                    // Remove the sample from the list
                    final Sample remove = _samples.remove(baseId);
                    // Change the name of the sample by appending a replicate counter
                    remove.setId(baseId + "_Rep" + replicateId++);
                    // Put the sample back into the list under its new name
                    _samples.put(remove.getId(), remove);
                    // Signal that we have a file containing replicates, so we do not have to perform this check more than once
                    hasReplicates = true;
                }

                final Sample sample = new Sample(baseId + (hasReplicates ? "_Rep" + replicateId++ : ""), fileName);

                int fieldIndex = 2;
                while (fieldIndex < fields.length) {
                    final String locusName = headers[fieldIndex].toUpperCase();
                    final String[] alleles = reader.parse(fields[fieldIndex++]);
                    String[] heights = null;

                    if (fieldIndex < fields.length && HEIGHT.equalsIgnoreCase(headers[fieldIndex])) {
                        heights = reader.parse(fields[fieldIndex++]);
                    }

                    final Locus locus = new Locus(locusName);
                    for (int alleleIndex = 0; alleleIndex < alleles.length; alleleIndex++) {
                        final String allele = alleles[alleleIndex].trim();
                        if (allele.length() > 0 && !allele.toUpperCase().startsWith(OFF_LADDER_ALLELE) && (!treatAsMixProfile || !locus.hasAllele(allele))) {
                            locus.addAllele(new Allele(allele, heights == null ? 0.0F : Float.parseFloat(heights[alleleIndex])));
                        }
                    }

                    if (!treatAsMixProfile && locus.size() == 1) {
                        locus.addAllele(locus.getAlleles().iterator().next());
                    }

                    sample.addLocus(locus);
                }

                _samples.put(sample.getId(), sample);
            }
        }
    }

    private String checkCaseNumber(final String sampleName) throws IllegalArgumentException {
        final String[] components = sampleName.split("_");
        String baseId = sampleName;
        if (components.length == 3) {
            if (_caseNumber.isEmpty()) {
                _caseNumber = components[1];
            }
            else {
                if (!_caseNumber.equalsIgnoreCase(components[1])) {
                    throw new IllegalArgumentException("Different case numbers present in this file: " + _caseNumber + " and " + components[1]);
                }
            }
            baseId = components[0] + "-" + components[2];
        }
        return baseId;
    }

    private void readEDnaFile(final String[] headers, final String fileName, final CSVReader reader, final boolean treatAsMixProfile) throws IOException {
        LOG.debug("Reading eDNA file");

        // Read lines and store loci and alleles in samples until no more line available
        String[] fields;
        while ((fields = reader.readFields()) != null) {
            if (fields.length > 0) {
                final String sampleName = fields[0].trim() + "-" + fields[2].trim();

                final String cn = fields[1];
                if (_caseNumber.isEmpty()) {
                    _caseNumber = cn;
                }
                else {
                    if (!_caseNumber.equalsIgnoreCase(cn)) {
                        throw new IllegalArgumentException("Different case numbers present in this file: " + _caseNumber + " and " + cn);
                    }
                }

                final String locusName = fields[4].toUpperCase().trim();

                Sample sample = _samples.get(sampleName);
                if (sample == null) {
                    sample = new Sample(sampleName, fileName);
                    _samples.put(sampleName, sample);
                }

                final Locus locus = new Locus(locusName);
                final HashMap<String, Allele> createdAlleles = new HashMap<>();

                // Create alleles and add them to the locus
                for (int idx = 6; idx < fields.length; idx++) {
                    final String alleleValue = fields[idx];
                    final String columnName = headers[idx];

                    // Do not add Off Ladder alleles
                    if (alleleValue.toUpperCase().startsWith(OFF_LADDER_ALLELE)) {
                        continue;
                    }

                    final Matcher heightMatcher = HEIGHT_PATTERN.matcher(columnName);
                    if (heightMatcher.matches()) {
                        final String alleleIdx = heightMatcher.group(1);
                        final Allele companionAllele = createdAlleles.get(alleleIdx);
                        if (companionAllele != null) {
                            companionAllele.setPeak(Float.parseFloat(alleleValue));
                        }
                    }

                    final Matcher alleleMatcher = ALLELE_PATTERN.matcher(columnName);
                    if (alleleMatcher.matches()) {
                        // If the locus does not already contain the allele, or the caller want to see duplicates
                        // then add the allele to the current locus
                        if (!treatAsMixProfile || !locus.hasAllele(alleleValue)) {
                            final String alleleIdx = alleleMatcher.group(1);
                            final Allele allele = new Allele(alleleValue);
                            createdAlleles.put(alleleIdx, allele);
                            locus.addAllele(allele);
                        }
                    }
                }

                if (!treatAsMixProfile && locus.size() == 1) {
                    locus.addAllele(locus.getAlleles().iterator().next());
                }

                // Add locus to the sample
                sample.addLocus(locus);
            }
        }
    }

    /**
     * Reads a file in LRMix format
     *
     * @param fileName The getName of the file. used as distinguishing feature
     * between samples from various files. Needs not be unique.
     * @param reader A CSVReader for getting the file contents
     * @param treatAsMixProfile true if duplicate alleles at any given     * locus should be ignored.
     * @throws IOException
     */
    private void readLRMixFile(final String[] headers, final String fileName, final CSVReader reader, final boolean treatAsMixProfile) throws IOException {
        LOG.debug("Reading LRMix file");
        // Read lines and store loci and alleles in samples until no more line available
        String[] fields;
        while ((fields = reader.readFields()) != null) {
            if (fields.length > 0) {
                final String sampleName = checkCaseNumber(fields[0]);
                final String locusName = fields[1].toUpperCase();

                Sample sample = _samples.get(sampleName);
                if (sample == null) {
                    sample = new Sample(sampleName, fileName);
                    _samples.put(sampleName, sample);
                }

                final Locus locus = new Locus(locusName);
                final HashMap<String, Allele> createdAlleles = new HashMap<>();

                // Create alleles and add them to the locus
                for (int idx = 2; idx < fields.length; idx++) {
                    final String alleleValue = fields[idx];
                    final String columnName = headers[idx];

                    // Do not add Off Ladder alleles
                    if (alleleValue.toUpperCase().startsWith(OFF_LADDER_ALLELE)) {
                        continue;
                    }

                    final Matcher heightMatcher = HEIGHT_PATTERN.matcher(columnName);
                    if (heightMatcher.matches()) {
                        final String alleleIdx = heightMatcher.group(1);
                        final Allele companionAllele = createdAlleles.get(alleleIdx);
                        if (companionAllele != null) {
                            companionAllele.setPeak(Float.parseFloat(alleleValue));
                        }
                    }

                    final Matcher alleleMatcher = ALLELE_PATTERN.matcher(columnName);
                    if (alleleMatcher.matches()) {
                        // If the locus does not already contain the allele, or the caller want to see duplicates
                        // then add the allele to the current locus
                        if (!treatAsMixProfile || !locus.hasAllele(alleleValue)) {
                            final String alleleIdx = alleleMatcher.group(1);
                            final Allele allele = new Allele(alleleValue);
                            createdAlleles.put(alleleIdx, allele);
                            locus.addAllele(allele);
                        }
                    }
                }

                // Add locus to the sample
                sample.addLocus(locus);
            }
        }
    }
}
