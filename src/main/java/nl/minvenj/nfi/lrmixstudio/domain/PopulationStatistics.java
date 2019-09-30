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

    private static final Logger LOG = LoggerFactory.getLogger(PopulationStatistics.class);
    public static final double DEFAULT_FREQUENCY = 0.001;

    private final ArrayList<String> _loci;
    private final ArrayList<String> _allelesForLocus;
    private final ArrayList<String> _compoundAlleles;
    private final String _fileName;
    private final double[][] _probs = new double[100][1024];
    private final boolean[][] _rare = new boolean[100][1024];
    private final boolean[] _locusInitialized = new boolean[100];

    private String _fileHash;
    private double _rareAlleleFrequency;

    public PopulationStatistics(final String fileNamez) {
        _loci = new ArrayList<>();
        _allelesForLocus = new ArrayList<>();
        _compoundAlleles = new ArrayList<>();
        for (int idx = 0; idx < _rare.length; idx++) {
            Arrays.fill(_rare[idx], true);
        }
        _fileName = fileNamez;
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
    public void addStatistic(final String locusName, final String alleleName, final BigDecimal probability) {
        final String normalizedAllele = Allele.normalize(alleleName);
        if (!_loci.contains(locusName)) {
            _loci.add(locusName);
        }
        final int locusId = Locus.getId(locusName);
        final int alleleId = Allele.getId(normalizedAllele);
        _probs[locusId][alleleId] = probability.doubleValue();
        _rare[locusId][alleleId] = false;
        _locusInitialized[locusId] = true;
        _allelesForLocus.add(locusName + normalizedAllele);
    }

    private Double getProbability(final int locusId, final int alleleId) {
        if (_rare[locusId][alleleId]) {
            return _rareAlleleFrequency;
        }
        return _probs[locusId][alleleId];
    }

    public Double getProbability(final Locus locus, final Allele allele) {
        return getProbability(locus.getId(), allele.getId());
    }

    public Double getProbability(final String locusId, final String allele) {
        return getProbability(Locus.getId(locusId), Allele.getId(allele));
    }

    public Collection<String> getAlleles(final String id) {
        final ArrayList<String> alleles = new ArrayList<>();
        for (final String locus : _allelesForLocus) {
            if (locus.startsWith(id)) {
                final String alleleName = locus.substring(id.length());
                if (!_compoundAlleles.contains(alleleName)) {
                    alleles.add(alleleName);
                }
            }
        }
        return alleles;
    }

    public String getFileName() {
        return _fileName;
    }

    public Collection<String> getLoci() {
        return Collections.unmodifiableCollection(_loci);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PopulationStatistics other = (PopulationStatistics) obj;
        if (!Objects.equals(this._loci, other._loci)) {
            return false;
        }
        if (!Objects.equals(this._fileName, other._fileName)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + Objects.hashCode(this._loci);
        hash = 83 * hash + Objects.hashCode(this._fileName);
        hash = 83 * hash + ("" + _rareAlleleFrequency).hashCode();
        return hash;
    }

    /**
     * Sets the file hash
     *
     * @param fileHash The hash to set
     */
    public void setFileHash(final String fileHash) {
        this._fileHash = fileHash;
    }

    /**
     * @return the fileHash
     */
    public String getFileHash() {
        return _fileHash;
    }

    @Override
    public String toString() {
        return _fileName + " " + _fileHash;
    }

    /**
     * Determines if the supplied allele is rare (i.e. is not recorded) in the
     * currently opened statistics file.
     *
     * @param allele The allele to query
     * @return true if the supplied allele is rare
     */
    public boolean isRareAllele(final Allele allele) {
        return _rare[allele.getLocus().getId()][allele.getId()];
    }

    /**
     * Sets the frequency for rare alleles (i.e. alleles that are not present in
     * the population frequency table)
     *
     * @param rareAlleleFrequency the frequency for rare alleles.
     */
    public void setRareAlleleFrequency(final Double rareAlleleFrequency) {
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
    public boolean isPresent(final String locusName) {
        final int id = Locus.getId(locusName);
        return id < _locusInitialized.length && _locusInitialized[id];
    }

    /**
     * Adds a compound statistic to the table. A compound statistic can help in optimizing the number of permutations
     * for unknown contributors by clustering the frequency of unobserved alleles into a single compound allele.
     * These compound alleles should not be used when iterating over the alleles in the statistics (e.g. for generating random profiles).
     *
     * @param locusName the name of the locus for which to add a compound allele
     * @param alleleName the name of the allele
     * @param probability the frequency of the compound allele
     */
    public void addCompoundStatistic(final String locusName, final String alleleName, final BigDecimal probability) {
        addStatistic(locusName, alleleName, probability);
        if (!_compoundAlleles.contains(alleleName)) {
            _compoundAlleles.add(alleleName);
        }
    }
}
