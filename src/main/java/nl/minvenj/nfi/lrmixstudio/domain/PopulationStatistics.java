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
package nl.minvenj.nfi.lrmixstudio.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PopulationStatistics {

    private final ArrayList<String> loci;
    private final ArrayList<String> allelesForLocus;
    private final String fileName;

    public static final double DEFAULT_FREQUENCY = 0.001;
    private String fileHash;
    private final double[][] probs = new double[100][1024];
    private final boolean[][] rare = new boolean[100][1024];
    private final boolean[] _locusInitialized = new boolean[100];
    private static final Logger LOG = LoggerFactory.getLogger(PopulationStatistics.class);
    private double _rareAlleleFrequency;

    public PopulationStatistics(String fileNamez) {
        loci = new ArrayList<>();
        allelesForLocus = new ArrayList<>();
        for (int idx = 0; idx < rare.length; idx++) {
            Arrays.fill(rare[idx], true);
        }
        fileName = fileNamez;
        _rareAlleleFrequency = DEFAULT_FREQUENCY;
    }

    /**
     * @return The frequency of rare alleles (i.e. alleles that are not recorded
     * in the population statistics file)
     */
    public String getRareAlleleFrequency() {
        return "" + _rareAlleleFrequency;
    }

    /**
     * Adds a statistic entry (i.e. the observed frequency of an allele at a
     * certain locus) to the population statistics.
     *
     * @param locusName The name of the locus
     * @param alleleName The name of the allele
     * @param probability The probability that the allele is observed at the
     * locus
     */
    public void addStatistic(String locusName, String alleleName, BigDecimal probability) {
        String normalizedAllele = Allele.normalize(alleleName);
        if (!loci.contains(locusName)) {
            loci.add(locusName);
        }
        int locusId = Locus.getId(locusName);
        int alleleId = Allele.getId(normalizedAllele);
        probs[locusId][alleleId] = probability.doubleValue();
        rare[locusId][alleleId] = false;
        _locusInitialized[locusId] = true;
        allelesForLocus.add(locusName + normalizedAllele);
    }

    private Double getProbability(int locusId, int alleleId) {
        if (rare[locusId][alleleId]) {
            return _rareAlleleFrequency;
        }
        return probs[locusId][alleleId];
    }

    public Double getProbability(Locus locus, Allele allele) {
        return getProbability(locus.getId(), allele.getId());
    }

    public Double getProbability(String locusId, String allele) {
        return getProbability(Locus.getId(locusId), Allele.getId(allele));
    }

    public Collection<String> getAlleles(String id) {
        ArrayList<String> alleles = new ArrayList<>();
        for (String locus : allelesForLocus) {
            if (locus.startsWith(id)) {
                alleles.add(locus.substring(id.length()));
            }
        }
        return alleles;
    }

    public String getFileName() {
        return fileName;
    }

    public Collection<String> getLoci() {
        return Collections.unmodifiableCollection(loci);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PopulationStatistics other = (PopulationStatistics) obj;
        if (!Objects.equals(this.loci, other.loci)) {
            return false;
        }
        if (!Objects.equals(this.fileName, other.fileName)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + Objects.hashCode(this.loci);
        hash = 83 * hash + Objects.hashCode(this.fileName);
        hash = 83 * hash + ("" + _rareAlleleFrequency).hashCode();
        return hash;
    }

    /**
     * Sets the file hash
     *
     * @param fileHash The hash to set
     */
    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }

    /**
     * @return the fileHash
     */
    public String getFileHash() {
        return fileHash;
    }

    @Override
    public String toString() {
        return fileName + " " + fileHash;
    }

    /**
     * Determines if the supplied allele is rare (i.e. is not recorded) in the
     * currently opened statistics file.
     *
     * @param allele The allele to query
     * @return true if the supplied allele is rare
     */
    public boolean isRareAllele(Allele allele) {
        return rare[allele.getLocus().getId()][allele.getId()];
    }

    /**
     * Sets the frequency for rare alleles (i.e. alleles that are not present in
     * the population frequency table)
     *
     * @param rareAlleleFrequency the frequency for rare alleles.
     */
    public void setRareAlleleFrequency(Double rareAlleleFrequency) {
        _rareAlleleFrequency = rareAlleleFrequency;
    }

    /**
     * Determines if the locus named in the locusName argument is present in the
     * population statistics.
     *
     * @param locusName The name of the locus to query for.
     *
     * @return true if the locus is present in the population statistics
     */
    public boolean isPresent(String locusName) {
        int id = Locus.getId(locusName);
        return id < _locusInitialized.length && _locusInitialized[id];
    }
}
