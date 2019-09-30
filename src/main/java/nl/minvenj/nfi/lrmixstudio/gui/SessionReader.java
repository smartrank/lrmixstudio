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
package nl.minvenj.nfi.lrmixstudio.gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import nl.minvenj.nfi.lrmixstudio.domain.PopulationStatistics;
import nl.minvenj.nfi.lrmixstudio.domain.Relatedness;
import nl.minvenj.nfi.lrmixstudio.domain.Relatedness.Relation;
import nl.minvenj.nfi.lrmixstudio.io.SampleReader;

class SessionReader {

    private static final String KEY_STATISTICS_FILE_HASH = "Statistics file hash:";
    private static final String KEY_STATISTICS_FILE = "Statistics file:";
    private static final String KEY_SECTION_HEADER = "===========";
    private static final String KEY_LOADED_FROM = "loaded from";
    private static final String KEY_HYPOTHESIS = "Hypothesis";
    private static final String KEY_CONTRIBUTORS = "Contributors";
    private static final String KEY_UNKNOWNS = "Unknowns";
    private static final String KEY_UNKNOWNS_DROPOUT = "Unknown Dropout";
    private static final String KEY_DROPIN = "Dropin";
    private static final String KEY_THETA = "Theta";
    private static final String KEY_CASE_NUMBER = "Case number:";
    private static final String KEY_RELATED_UNKNOWN = "Related unknown contributor:";
    private static final String KEY_RARE_ALLELE_FREQUENCY = "Rare Allele Frequency:";
    private static final String KEY_EVALUATE_AS_HOMOZYGOTE = "locus evaluated as homozygotic";

//    private static final String REGEX_RAREALLELE_SECTION2 = "No rare alleles detected";
//    private static final String REGEX_RAREALLELE_SECTION1 = "The following alleles were detected as rare:";
//    private static final String REGEX_LOCUS_SECTION = "Enabled loci.*";
//    private static final String REGEX_DEFENSEHYPOTHESIS_SECTION = "Hypothesis Defense";
//    private static final String REGEX_PROSECUTIONHYPOTHESIS_SECTION = "Hypothesis Prosecution";
//    private static final String REGEX_REFERENCEPROFILES_SECTION = "Loaded.* profiles:";
//    private static final String REGEX_REPLICATES_SECTION = "Loaded replicates:";
//    private static final String REGEX_STATISTICS_SECTION = "Statistics file: .*";
    private static final String REGEX_GENERAL_SECTION = "\\d{4}/\\d{2}/\\d{2} \\d{2}:\\d{2}:\\d{2}";

    private final HashMap<String, String> _fileHashes = new HashMap<>();
    private final ArrayList<String> _replicateFiles = new ArrayList<>();
    private final ArrayList<String> _enabledReplicates = new ArrayList<>();
    private final ArrayList<String> _enabledReferenceProfiles = new ArrayList<>();
    private final ArrayList<String> _referenceProfileFiles = new ArrayList<>();
    private final HashMap<String, BigDecimal> _prosecutionDropouts = new HashMap<>();
    private final Properties _defenseProperties = new Properties();
    private final HashMap<String, BigDecimal> _defenseDropouts = new HashMap<>();
    private final Properties _prosecutionProperties = new Properties();
    private String _populationStatisticsFile;
    private String _caseNumber;
    private final Collection<String> _enabledLoci = new ArrayList<>();
    private Double _rareAlleleFrequency = PopulationStatistics.DEFAULT_FREQUENCY;
    private final ArrayList<String> _samplesTreatedAsHomozygote = new ArrayList<>();

    /**
     * Reads and parses the contents of the supplied logfile, extracting the
     * relevant configuration elements.
     *
     * @param selectedFile The logfile to read
     * @throws FileNotFoundException If the supplied file could not be found
     * @throws IOException If there was an error reading from the supplied file
     */
    void read(final File selectedFile) throws FileNotFoundException, IOException {
        final BufferedReader reader = new BufferedReader(new FileReader(selectedFile));

        readGeneralSection(reader);
        readStatisticsSection(reader);
        readSampleSection(_enabledReplicates, _replicateFiles, reader);
        readSampleSection(_enabledReferenceProfiles, _referenceProfileFiles, reader);
        readLocusSection(reader);
        readRareAllelesSection(reader);
        readHypothesisSection("Prosecution", _prosecutionDropouts, _prosecutionProperties, reader);
        readHypothesisSection("Defense", _defenseDropouts, _defenseProperties, reader);
    }

    /**
     * @return A collection of all files from which replicates were read
     */
    Iterable<String> getReplicateFiles() {
        return _replicateFiles;
    }

    Iterable<String> getReferenceProfileFiles() {
        return _referenceProfileFiles;
    }

    boolean checkFileHash(final SampleReader sampleReader) throws Exception {
        final String recordedHash = _fileHashes.get(sampleReader.getFileName());
        return (recordedHash != null && recordedHash.equalsIgnoreCase(sampleReader.getFileHash()));
    }

    boolean checkFileHash(final PopulationStatistics stats) {
        final String recordedHash = _fileHashes.get(stats.getFileName());
        return (recordedHash != null && recordedHash.equalsIgnoreCase(stats.getFileHash()));
    }

    String getPopulationStatisticsFile() {
        return _populationStatisticsFile;
    }

    public String getCaseNumber() {
        return _caseNumber;
    }

    double getProsecutionTheta() {
        return Double.parseDouble(_prosecutionProperties.getProperty(KEY_THETA));
    }

    int getProsecutionUnknowns() {
        return Integer.parseInt(_prosecutionProperties.getProperty(KEY_UNKNOWNS));
    }

    double getProsecutionUnknownDropout() {
        return Double.parseDouble(_prosecutionProperties.getProperty(KEY_UNKNOWNS_DROPOUT));
    }

    double getProsecutionDropin() {
        return Double.parseDouble(_prosecutionProperties.getProperty(KEY_DROPIN));
    }

    double getProsecutionDropout(final String id) {
        final BigDecimal dropout = _prosecutionDropouts.get(id);
        if (dropout == null) {
            return -1;
        }
        return dropout.doubleValue();
    }

    double getDefenseTheta() {
        return Double.parseDouble(_defenseProperties.getProperty(KEY_THETA));
    }

    int getDefenseUnknowns() {
        return Integer.parseInt(_defenseProperties.getProperty(KEY_UNKNOWNS));
    }

    double getDefenseUnknownDropout() {
        return Double.parseDouble(_defenseProperties.getProperty(KEY_UNKNOWNS_DROPOUT));
    }

    double getDefenseDropin() {
        return Double.parseDouble(_defenseProperties.getProperty(KEY_DROPIN));
    }

    Relatedness.Relation getDefenseUnknownsRelation() {
        final String related = _defenseProperties.getProperty(KEY_RELATED_UNKNOWN);
        if (related == null) {
            return Relation.NONE;
        }
        final String[] comp = related.split(" ");
        return Relation.fromDescription(comp[0]);
    }

    String getDefenseUnknownsRelationSampleName() {
        final String related = _defenseProperties.getProperty(KEY_RELATED_UNKNOWN);
        if (related == null) {
            return "";
        }
        final String[] comp = related.split(" ");
        return comp[2];
    }

    double getDefenseDropout(final String id) {
        final BigDecimal dropout = _defenseDropouts.get(id);
        if (dropout == null) {
            return -1;
        }
        return dropout.doubleValue();
    }

    /**
     * Reads the contents of the general section of the log. This contains data
     * such as the user who initiated the analysis, the date and time, the
     * machine name and java version.
     *
     * @param reader A BufferedReader from which to read the data
     */
    private void readGeneralSection(final BufferedReader reader) {
        try {
            String line = reader.readLine();
            if (line == null || !line.matches(REGEX_GENERAL_SECTION)) {
                throw new IllegalArgumentException("Not a valid logfile!");
            }

            while (line != null && !line.startsWith(KEY_SECTION_HEADER)) {
                line = line.trim();
                if (line.startsWith(KEY_CASE_NUMBER)) {
                    _caseNumber = line.substring(KEY_CASE_NUMBER.length() + 1).trim();
                }
                line = reader.readLine();
            }
        } catch (final IOException ex) {
            throw new IllegalArgumentException("Error reading from file", ex);
        }
    }

    /**
     * Reads the contents of the section describing the population frequencies
     * file from the log. This contains the filename of the statistics file and
     * the file hash.
     *
     * @param reader A BufferedReader from which to read the data
     * @return false if the end of the file was reached
     */
    private boolean readStatisticsSection(final BufferedReader reader) {
        try {
            String hash = "";
            String line = reader.readLine();
            while (line != null && !line.startsWith(KEY_SECTION_HEADER)) {
                line = line.trim();
                if (line.startsWith(KEY_STATISTICS_FILE)) {
                    _populationStatisticsFile = line.substring(KEY_STATISTICS_FILE.length()).trim();
                }

                if (line.startsWith(KEY_STATISTICS_FILE_HASH)) {
                    hash = line.substring(KEY_STATISTICS_FILE_HASH.length()).trim();
                }

                if (line.startsWith(KEY_RARE_ALLELE_FREQUENCY)) {
                    _rareAlleleFrequency = Double.parseDouble(line.substring(KEY_RARE_ALLELE_FREQUENCY.length()).trim());
                }

                line = reader.readLine();
            }
            _fileHashes.put(_populationStatisticsFile, hash);
            return (line != null);
        } catch (final IOException ex) {
            throw new IllegalArgumentException("Error reading from file", ex);
        }
    }

    /**
     * Reads the contents of one of the sections containing replicate or
     * reference profile data
     *
     * @param reader the reader supplying the data
     * @return false if the end of the file was reached
     */
    private boolean readSampleSection(final List<String> enabledSamples, final Collection<String> fileNames, final BufferedReader reader) {
        try {
            String line = reader.readLine();
            String currentSampleName = null;
            while (line != null && !line.startsWith(KEY_SECTION_HEADER)) {
                line = line.trim();
                if (line.contains(KEY_LOADED_FROM)) {
                    final String sampleName = line.substring(0, line.indexOf(KEY_LOADED_FROM)).trim();
                    final String fileName = line.substring(line.indexOf("'") + 1, line.lastIndexOf("'"));
                    final String hash = line.substring(line.lastIndexOf(" ") + 1);
                    _fileHashes.put(fileName, hash);
                    if (!fileNames.contains(fileName)) {
                        fileNames.add(fileName);
                    }
                    enabledSamples.add(sampleName);
                    currentSampleName = sampleName;
                } else {
                    if (line.contains(KEY_EVALUATE_AS_HOMOZYGOTE) && currentSampleName != null) {
                        _samplesTreatedAsHomozygote.add(currentSampleName);
                    }
                    else {
                        // Unexpected line. IGNORE?
                    }
                }

                line = reader.readLine();
            }
            return (line != null);
        } catch (final IOException ex) {
            throw new IllegalArgumentException("Error reading from file", ex);
        }
    }

    /**
     * Reads the contents of the section describing the enabled loci
     *
     * @param reader the reader supplying the data
     * @return false if the end of the file was reached
     */
    private boolean readLocusSection(final BufferedReader reader) {
        try {
            String line = reader.readLine();
            while (line != null && !line.startsWith(KEY_SECTION_HEADER)) {
                // Read enabled loci and disable all others
                if (line.startsWith("Enabled loci: ")) {
                    final String[] enabledLoci = line.substring("Enabled loci: ".length()).replaceAll("[ \\[\\]]", "").split(",");
                    _enabledLoci.addAll(Arrays.asList(enabledLoci));
                }
                line = reader.readLine();
            }
            return (line != null);
        } catch (final IOException ex) {
            throw new IllegalArgumentException("Error reading from file", ex);
        }
    }

    /**
     * Reads the contents of the section describing the rare alleles
     *
     * @param reader the reader supplying the data
     * @return false if the end of the file was reached
     */
    private boolean readRareAllelesSection(final BufferedReader reader) {
        try {
            String line = reader.readLine();
            while (line != null && !line.startsWith(KEY_SECTION_HEADER)) {
                // Do nothing.
                line = reader.readLine();
            }
            return (line != null);
        } catch (final IOException ex) {
            throw new IllegalArgumentException("Error reading from file", ex);
        }
    }

    /**
     * Reads the contents of the section describing the prosecution hypothesis
     *
     * @param reader the reader supplying the data
     */
    private boolean readHypothesisSection(final String signature, final HashMap<String, BigDecimal> dropouts, final Properties properties, final BufferedReader reader) {
        try {
            String line = reader.readLine();
            while (line != null && !line.startsWith(KEY_SECTION_HEADER)) {
                line = line.trim();
                if (line.startsWith(KEY_HYPOTHESIS)) {
                    final String name = line.substring(KEY_HYPOTHESIS.length()).trim();
                    if (!signature.equalsIgnoreCase(name)) {
                        throw new IllegalArgumentException("Expected '" + signature + "' but found '" + name + "'");
                    }
                }
                if (line.startsWith(KEY_CONTRIBUTORS)) {
                    final String[] elements = line.substring(KEY_CONTRIBUTORS.length() + 1).replaceAll("[\\[\\]]", "").split(",");
                    for (final String contributor : elements) {
                        if (!contributor.isEmpty()) {
                            final String id = contributor.substring(0, contributor.indexOf("(")).trim();
                            final BigDecimal dropout = new BigDecimal(contributor.substring(contributor.indexOf("(") + 1, contributor.indexOf(")")));
                            dropouts.put(id, dropout);
                        }
                    }
                }
                if (line.startsWith(KEY_UNKNOWNS)) {
                    properties.setProperty(KEY_UNKNOWNS, line.substring(line.lastIndexOf(" ")).trim());
                }
                if (line.startsWith(KEY_UNKNOWNS_DROPOUT)) {
                    properties.setProperty(KEY_UNKNOWNS_DROPOUT, line.substring(line.lastIndexOf(" ")).trim());
                }
                if (line.startsWith(KEY_DROPIN)) {
                    properties.setProperty(KEY_DROPIN, line.substring(line.lastIndexOf(" ")).trim());
                }
                if (line.startsWith(KEY_THETA)) {
                    properties.setProperty(KEY_THETA, line.substring(line.lastIndexOf(" ")).trim());
                }
                if (line.startsWith(KEY_RELATED_UNKNOWN)) {
                    properties.setProperty(KEY_RELATED_UNKNOWN, line.substring(KEY_RELATED_UNKNOWN.length()).trim());
                }

                line = reader.readLine();
            }
            return (line != null);
        } catch (final IOException ex) {
            throw new IllegalArgumentException("Error reading from file", ex);
        }
    }

    /**
     * Returns whether the sample with the supplied ID is enabled
     *
     * @param id The ID of the sample in question
     * @return true if the sample is enabled
     */
    boolean isSampleEnabled(final String id) {
        return _enabledReplicates.contains(id) || _enabledReferenceProfiles.contains(id);
    }

    /**
     * Gets the hash for the supplied filename
     *
     * @param fileName The name of the file for which to get the file hash
     * @return the hash for the supplied file, or an empty string if the file
     * was not found
     */
    String getFileHash(final String fileName) {
        final String hash = _fileHashes.get(fileName);
        return hash == null ? "" : hash;
    }

    boolean hasProsecution() {
        return !_prosecutionProperties.isEmpty();
    }

    boolean hasDefense() {
        return !_defenseProperties.isEmpty();
    }

    boolean isLocusEnabled(final String locus) {
        return _enabledLoci.contains(locus);
    }

    Double getRareAlleleFrequency() {
        return _rareAlleleFrequency;
    }

    /**
     * @param id
     * @return
     */
    public boolean isSampleTreatedAsHomozygote(final String id) {
        return _samplesTreatedAsHomozygote.contains(id);
    }

}
