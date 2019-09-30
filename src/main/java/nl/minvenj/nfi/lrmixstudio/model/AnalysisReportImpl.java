/**
 * Copyright (C) 2014 Netherlands Forensic Institute
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
package nl.minvenj.nfi.lrmixstudio.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.minvenj.nfi.lrmixstudio.domain.Allele;
import nl.minvenj.nfi.lrmixstudio.domain.Hypothesis;
import nl.minvenj.nfi.lrmixstudio.domain.LikelihoodRatio;
import nl.minvenj.nfi.lrmixstudio.domain.Locus;
import nl.minvenj.nfi.lrmixstudio.domain.LocusProbabilities;
import nl.minvenj.nfi.lrmixstudio.domain.PopulationStatistics;
import nl.minvenj.nfi.lrmixstudio.domain.Sample;
import nl.minvenj.nfi.lrmixstudio.model.SensitivityAnalysisResults.Range;

public class AnalysisReportImpl implements AnalysisReport, AnalysisProgressListener {

    private LikelihoodRatio lr;
    private Throwable exception;
    private long stopTime;
    private long startTime;
    private boolean success;
    private boolean _exported;
    private String caseNumber;
    private Hypothesis prosecutionHypothesis;
    private Hypothesis defenseHypothesis;
    private final String programVersion;
    private SensitivityAnalysisResults sensitivityAnalysisResults = new SensitivityAnalysisResults();
    private final Collection<Sample> replicates = new ArrayList<>();
    private final Collection<Sample> profiles = new ArrayList<>();
    private final Collection<String> _enabledLoci = new ArrayList<>();
    private final String _rareAlleleFrequency;
    private static final Logger LOG = LoggerFactory.getLogger(AnalysisReportImpl.class);
    private NonContributorTestResults performanceTestResults;
    private final ConfigurationData _config;
    private long _processingTime;
    private String _logfileName;

    public AnalysisReportImpl(final ConfigurationData config) {
        LOG.debug("Creating new report from {}", config);
        prosecutionHypothesis = config.getProsecution();
        defenseHypothesis = config.getDefense();
        replicates.addAll(config.getActiveReplicates());
        programVersion = config.getProgramVersion();
        caseNumber = config.getCaseNumber();
        profiles.addAll(config.getActiveProfiles());
        _enabledLoci.addAll(config.getEnabledLoci());
        _config = config;
        _rareAlleleFrequency = "" + _config.getRareAlleleFrequency();
    }

    @Override
    public LikelihoodRatio getLikelihoodRatio() {
        return lr;
    }

    @Override
    public Hypothesis getDefenseHypothesis() {
        return defenseHypothesis;
    }

    @Override
    public Hypothesis getProsecutionHypothesis() {
        return prosecutionHypothesis;
    }

    @Override
    public long getStartTime() {
        return startTime;
    }

    @Override
    public long getStopTime() {
        return stopTime;
    }

    @Override
    public boolean isSucceeded() {
        return success;
    }

    @Override
    public Throwable getException() {
        return exception;
    }

    @Override
    public void analysisStarted() {
        startTime = System.currentTimeMillis();
        LOG.debug("Analysis started");
    }

    @Override
    public void analysisFinished(final LikelihoodRatio lr) {
        stopTime = System.currentTimeMillis();
        addProcessingTime(stopTime - startTime);
        if (lr != null) {
            LOG.debug("Analysis Result LR={}", lr.getOverallRatio());
        }
        LOG.debug("Analysis took {} ms", (stopTime - startTime));
        this.lr = lr;
        success = true;
    }

    @Override
    public synchronized void analysisFinished(final Throwable e) {
        // We will receive this call for each thread that was still running.
        // Only add the processing time once.
        if (stopTime == 0) {
            stopTime = System.currentTimeMillis();
            addProcessingTime(stopTime - startTime);
        }
        LOG.debug("Analysis encountered exception: {} - {}", e.getClass().getName(), e.getMessage());
        LOG.debug("Analysis took {} ms", (stopTime - startTime));
        exception = e;
        success = false;
    }

    @Override
    public synchronized void hypothesisStarted(final Hypothesis hypothesis) {
        LOG.debug("Hypothesis {} started", hypothesis.getId());
    }

    @Override
    public synchronized void hypothesisFinished(final Hypothesis hypothesis, final LocusProbabilities probabilities) {
        LOG.debug("Hypothesis {} done: {}", hypothesis.getId(), probabilities);
    }

    @Override
    public void locusStarted(final Hypothesis hypothesis, final String locusName, final long jobsize) {
    }

    @Override
    public void locusFinished(final Hypothesis hypothesis, final String locusName, final Double locusProbability) {
    }

    @Override
    public String getCaseNumber() {
        return caseNumber;
    }

    @Override
    public String getProgramVersion() {
        return programVersion;
    }

    @Override
    public Collection<Sample> getReplicates() {
        return Collections.unmodifiableCollection(replicates);
    }

    @Override
    public Collection<Sample> getProfiles() {
        return Collections.unmodifiableCollection(profiles);
    }

    @Override
    public PopulationStatistics getPopulationStatistics() {
        return prosecutionHypothesis.getPopulationStatistics();
    }

    @Override
    public SensitivityAnalysisResults getSensitivityAnalysisResults() {
        return sensitivityAnalysisResults;
    }

    public void setCaseNumber(final String caseNumber) {
        this.caseNumber = caseNumber;
    }

    public void setDefenseHypothesis(final Hypothesis defenseHypothesis) {
        this.defenseHypothesis = defenseHypothesis;
    }

    public void setProsecutionHypothesis(final Hypothesis prosecutionHypothesis) {
        this.prosecutionHypothesis = prosecutionHypothesis;
    }

    @Override
    public int getGuid() {
        // Generate a unique(ish) number based on the defense and prosecution hypotheses, and the list of enabled loci
        final int guid = getDefenseHypothesis().getGuid() + 2 * getProsecutionHypothesis().getGuid() + 3 * deepHash(_enabledLoci) + 5 * deepHash(replicates);

        return guid;
    }

    public void enrich(final AnalysisReport other) {
        LOG.debug("Enriching report {} with data from report {}", this, other);
        if (getGuid() != other.getGuid()) {
            throw new IllegalArgumentException("Cannot enrich report '" + getGuid() + "' with data from report '" + other.getGuid() + "'. The hypotheses are not compatible!");
        }

        if (other.getLikelihoodRatio() != null) {
            this.lr = other.getLikelihoodRatio();
        }

        for (final Range range : other.getSensitivityAnalysisResults().getRanges()) {
            getSensitivityAnalysisResults().addRange(range);
        }

        if (other.getSensitivityAnalysisResults().getDropoutEstimation() != null) {
            getSensitivityAnalysisResults().setDropoutEstimation(other.getSensitivityAnalysisResults().getDropoutEstimation());
        }

        if (other.getNonContributorTestResults() != null) {
            setPerformanceTestResults(other.getNonContributorTestResults());
        }
        _exported = false;
    }

    @Override
    public NonContributorTestResults getNonContributorTestResults() {
        return performanceTestResults;
    }

    public void setPerformanceTestResults(final NonContributorTestResults testResults) {
        performanceTestResults = testResults;
    }

    @Override
    public String getRareAlleleFrequency() {
        return "" + _rareAlleleFrequency;
    }

    @Override
    public Collection<Allele> getRareAlleles() {
        final Collection<Allele> rareAlleles = new ArrayList<>();
        for (final Sample sample : replicates) {
            if (sample.isEnabled()) {
                for (final Locus locus : sample.getLoci()) {
                    if (_config.isLocusEnabled(locus.getName())) {
                        for (final Allele allele : locus.getAlleles()) {
                            if (getProsecutionHypothesis().getPopulationStatistics().isRareAllele(allele)) {
                                rareAlleles.add(allele);
                                LOG.debug("Allele {} in {} of {} is rare", allele.getAllele(), allele.getLocus(), allele.getLocus().getSampleId());
                            }
                        }
                    }
                }
            }
        }

        for (final Sample sample : profiles) {
            if (sample.isEnabled()) {
                for (final Locus locus : sample.getLoci()) {
                    if (_config.isLocusEnabled(locus.getName())) {
                        for (final Allele allele : locus.getAlleles()) {
                            if (getProsecutionHypothesis().getPopulationStatistics().isRareAllele(allele)) {
                                rareAlleles.add(allele);
                                LOG.debug("Allele {} in {} of {} is rare", allele.getAllele(), allele.getLocus(), allele.getLocus().getSampleId());
                            }
                        }
                    }
                }
            }
        }
        return rareAlleles;
    }

    @Override
    public boolean isExported() {
        return _exported;
    }

    /**
     * Sets a flag indicating whether this report has been exported
     *
     * @param exported true if the report has been exported, false otherwise
     */
    public void setExported(final boolean exported) {
        _exported = exported;
    }

    @Override
    public long getProcessingTime() {
        return _processingTime;
    }

    @Override
    public void addProcessingTime(final long processingTime) {
        LOG.debug("Adding processing time {}", processingTime);
        _processingTime += processingTime;
    }

    @Override
    public Collection<String> getEnabledLoci() {
        return _enabledLoci;
    }

    @Override
    public Collection<String> getDisabledLoci() {
        final ArrayList<String> disabled = new ArrayList<>();
        for (final Sample replicate : _config.getActiveReplicates()) {
            for (final Locus replicateLocus : replicate.getLoci()) {
                final String name = replicateLocus.getName();
                if (_config.isLocusValid(name) && !disabled.contains(name)) {
                    disabled.add(name);
                }
            }
        }
        disabled.removeAll(_enabledLoci);
        return disabled;
    }

    private final int[] primes = new int[]{2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89,
        101, 103, 107, 109, 113, 127, 131, 137, 139, 149, 151, 157, 163, 167, 173, 179, 181,
        191, 193, 197, 199, 211, 223, 227, 229,
        233};

    private int deepHash(final Collection collection) {
        int retval = 0;
        int idx = 0;
        for (final Object s : collection) {
            if (s instanceof Sample) {
                retval += primes[idx++] * ((Sample) s).getId().hashCode();
            } else {
                retval += primes[idx++] * s.hashCode();
            }
        }
        return retval;
    }

    @Override
    public boolean isDropoutCompatible(final AnalysisReport otherReport) {

        // Other report must be the same type
        if (!(otherReport instanceof AnalysisReportImpl)) {
            return false;
        }

        final AnalysisReportImpl other = (AnalysisReportImpl) otherReport;

        // Replicates
        if (!compareCollections(replicates, other.replicates)) {
            return false;
        }

        // Profiles
        if (!compareCollections(profiles, other.profiles)) {
            return false;
        }

        // Enabled Loci
        if (!compareCollections(_enabledLoci, other._enabledLoci)) {
            return false;
        }

        // Hd excluding dropout
        if (defenseHypothesis.getGuidForDropoutEstimation() != other.defenseHypothesis.getGuidForDropoutEstimation()) {
            return false;
        }

        // Hp excluding dropout
        return (prosecutionHypothesis.getGuidForDropoutEstimation() == other.prosecutionHypothesis.getGuidForDropoutEstimation());
    }

    @Override
    public boolean isSensitivityCompatible(final AnalysisReport otherReport) {

        // Other report must be the same type
        if (!(otherReport instanceof AnalysisReportImpl)) {
            return false;
        }

        final AnalysisReportImpl other = (AnalysisReportImpl) otherReport;

        // Replicates
        if (!compareCollections(replicates, other.replicates)) {
            return false;
        }

        // Profiles
        if (!compareCollections(profiles, other.profiles)) {
            return false;
        }

        // Enabled Loci
        if (!compareCollections(_enabledLoci, other._enabledLoci)) {
            return false;
        }

        // Hd excluding dropout
        if (defenseHypothesis.getGuidForSensitivityAnalysis() != other.defenseHypothesis.getGuidForSensitivityAnalysis()) {
            return false;
        }

        // Hp excluding dropout
        return (prosecutionHypothesis.getGuidForSensitivityAnalysis() == other.prosecutionHypothesis.getGuidForSensitivityAnalysis());
    }

    private boolean compareCollections(final Collection<?> theOne, final Collection<?> theOther) {
        return theOne.containsAll(theOther) && theOther.containsAll(theOne);
    }

    @Override
    public void setSensitivityAnalysisResults(final SensitivityAnalysisResults results) {
        sensitivityAnalysisResults = results;
    }

    @Override
    public String getLogfileName() {
        return _logfileName;
    }

    @Override
    public void setLogfileName(final String name) {
        _logfileName = name;
    }
}
