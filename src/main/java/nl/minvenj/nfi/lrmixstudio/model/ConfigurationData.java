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
package nl.minvenj.nfi.lrmixstudio.model;

import static nl.minvenj.nfi.lrmixstudio.model.ConfigurationDataElement.ACTIVEPROFILES;
import static nl.minvenj.nfi.lrmixstudio.model.ConfigurationDataElement.ACTIVEREPLICATES;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.minvenj.nfi.lrmixstudio.domain.Allele;
import nl.minvenj.nfi.lrmixstudio.domain.DisabledLocus;
import nl.minvenj.nfi.lrmixstudio.domain.Hypothesis;
import nl.minvenj.nfi.lrmixstudio.domain.Locus;
import nl.minvenj.nfi.lrmixstudio.domain.PopulationStatistics;
import nl.minvenj.nfi.lrmixstudio.domain.Sample;
import nl.minvenj.nfi.lrmixstudio.gui.ApplicationSettings;

/**
 * A class storing the configuration data to be passed to the mathematical
 * model.
 */
public class ConfigurationData implements Cloneable {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationData.class);
    private Hypothesis defense;
    private final Collection<Sample> profiles = new ArrayList<>();
    private Hypothesis prosecution;
    private final Collection<Sample> replicates = new ArrayList<>();
    private PopulationStatistics _statistics;
    private String caseNumber;
    private String programVersion;
    private String modelName;
    private final ArrayList<ConfigurationDataChangeListener> listeners = new ArrayList<>();
    private final LinkedHashMap<String, LocusEx> allLoci = new LinkedHashMap<>();
    private boolean locusListDirty = false;
    private final HashMap<Integer, AnalysisReport> reports = new HashMap<>();
    private int _threadCount = Runtime.getRuntime().availableProcessors();
    private Double _rareAlleleFrequency = PopulationStatistics.DEFAULT_FREQUENCY;

    /**
     * This class represents a locus in the list of _enabled loci
     */
    private class LocusEx implements Cloneable {

        private final String _id;
        private boolean _enabled;
        private boolean _valid;
        private String _statusDesc;

        public LocusEx(final String id) {
            _id = id;
            _enabled = true;
            _valid = true;
            _statusDesc = "";
        }

        private LocusEx(final String id, final boolean enabled, final boolean valid, final String statusDesc) {
            _id = id;
            _enabled = enabled;
            _valid = valid;
            _statusDesc = statusDesc;
        }

        /**
         * @return the _id
         */
        public String getId() {
            return _id;
        }

        /**
         * @return true if this locus is enabled
         */
        public boolean isEnabled() {
            return _enabled;
        }

        /**
         * @return true if this locus is valid an can be enabled
         */
        public boolean isValid() {
            return _valid;
        }

        private void setEnabled(final boolean enabled) {
            LOG.debug("Locus {} setEnabled({})", _id, enabled);
            _enabled = enabled;
        }

        @Override
        public String toString() {
            return _id;
        }

        @Override
        protected LocusEx clone() {
            return new LocusEx(_id, _enabled, _valid, _statusDesc);
        }

        private void setValid(final boolean valid, final String statusDesc) {
            _valid = valid;
            _statusDesc = statusDesc;
        }

        private String getStatusDesc() {
            return _statusDesc;
        }
    }

    /**
     * Creates a new empty configuration data object
     */
    public ConfigurationData() {
    }

    /**
     * Creates a new configuration data object by copying an existing instance.
     * Since this method performs a deep copy, the new instance is completely
     * independent of the original. Changes made to one do not affect the other.
     *
     * @param config The original configuration to copy
     */
    public ConfigurationData(final ConfigurationData config) {
        if (config.getDefense() != null) {
            defense = config.getDefense().copy();
        }
        if (config.getProsecution() != null) {
            prosecution = config.getProsecution().copy();
        }
        _rareAlleleFrequency = config._rareAlleleFrequency;
        this.profiles.addAll(config.getAllProfiles());
        this.replicates.addAll(config.getAllReplicates());
        this.modelName = config.getMathematicalModelName();
        _statistics = config.getStatistics();
        this._threadCount = config._threadCount;
        for (final String le : config.allLoci.keySet()) {
            this.allLoci.put(le, config.allLoci.get(le).clone());
        }
        LOG.debug("Cloning configuration {} to {}", config.hashCode(), hashCode());
    }

    /**
     * Add a listener class to be notified if anything in the configuration data
     * changes.
     *
     * @param listener A ConfigurationDataChangeListener instance
     */
    public void addDataChangeListener(final ConfigurationDataChangeListener listener) {
        listeners.add(listener);
    }

    /**
     * @return the defense hypothesis
     */
    public Hypothesis getDefense() {
        return defense;
    }

    /**
     * Sets a new defense hypothesis and fires the dataChanged event on all
     * configured listeners.
     *
     * @param defense the defense hypothesis to set
     */
    public void setDefense(final Hypothesis defense) {
        this.defense = defense;
        notifyListeners(ConfigurationDataElement.DEFENSE);
    }

    /**
     * @return A Collection of Sample classes representing all loaded profiles
     *         (both activated and deactivated profiles are returned) . Note
     *         that subsequent calls to this method will return a new Collection
     *         object each time.
     */
    public Collection<Sample> getAllProfiles() {
        return Collections.unmodifiableCollection(profiles);
    }

    /**
     * @return A Collection of Sample classes representing the active profiles
     *         (deactivated profiles are filtered out!). Note that subsequent
     *         calls to this method will return a new Collection object each
     *         time.
     */
    public Collection<Sample> getActiveProfiles() {
        final ArrayList<Sample> returnValue = new ArrayList();

        for (final Sample sample : profiles) {
            if (sample.isEnabled()) {
                returnValue.add(sample);
            }
        }

        return Collections.unmodifiableCollection(returnValue);
    }

    /**
     * Adds the supplied list of samples to the list of profiles
     *
     * @param profiles A collection of Sample classes representing profiles.
     */
    public void addProfiles(final Collection<Sample> profiles) {
        this.profiles.addAll(profiles);
        locusListDirty = true;
        notifyListeners(ConfigurationDataElement.PROFILES);
    }

    /**
     * @return the prosecution hypothesis
     */
    public Hypothesis getProsecution() {
        return prosecution;
    }

    /**
     * @param prosecution the prosecution hypothesis to set
     */
    public void setProsecution(final Hypothesis prosecution) {
        this.prosecution = prosecution;
        if (this.prosecution.getPopulationStatistics() != null) {
            this.prosecution.getPopulationStatistics().setRareAlleleFrequency(_rareAlleleFrequency);
        }

        notifyListeners(ConfigurationDataElement.PROSECUTION);
    }

    /**
     * @return a collection containing samples that represent all active
     *         replicates
     */
    public Collection<Sample> getActiveReplicates() {
        final ArrayList<Sample> returnValue = new ArrayList();

        for (final Sample sample : replicates) {
            if (sample.isEnabled()) {
                returnValue.add(sample);
            }
        }

        return Collections.unmodifiableCollection(returnValue);
    }

    /**
     * @return a collection containing samples that represent all replicates
     *         (both active and inactive)
     */
    public Collection<Sample> getAllReplicates() {
        return Collections.unmodifiableCollection(replicates);
    }

    /**
     * Adds the samples in the supplied collection to the list of replicates
     */
    public void addReplicates(final Collection<Sample> replicates) {
        this.replicates.addAll(replicates);
        locusListDirty = true;
        notifyListeners(ConfigurationDataElement.REPLICATES);
    }

    /**
     * @return the configured PopulationStatistics
     */
    public PopulationStatistics getStatistics() {
        return _statistics;
    }

    /**
     * @param statistics Sets the PopulationStatistics object used for getting
     *                   allele probabilities
     */
    public void setStatistics(final PopulationStatistics statistics) {
        LOG.debug("Statistics set to {}", statistics);
        _statistics = statistics;
        if (statistics != null) {
            _rareAlleleFrequency = Double.parseDouble(ApplicationSettings.getRareAlleleFrequency(statistics));
            _statistics.setRareAlleleFrequency(_rareAlleleFrequency);
        }
        locusListDirty = true;
        notifyListeners(ConfigurationDataElement.STATISTICS);
    }

    /**
     * @return the caseNumber
     */
    public String getCaseNumber() {
        return caseNumber == null ? "" : caseNumber;
    }

    /**
     * @return the programVersion
     */
    public String getProgramVersion() {
        return programVersion;
    }

    /**
     * @param caseNumber the caseNumber to set
     */
    public void setCaseNumber(final String caseNumber) {
        if (caseNumber == null) {
            this.caseNumber = "";
        }
        else {
            this.caseNumber = caseNumber;
        }
        notifyListeners(ConfigurationDataElement.CASENUMBER);
    }

    /**
     * @param programVersion the programVersion to set
     */
    public void setProgramVersion(final String programVersion) {
        this.programVersion = programVersion;
    }

    /**
     * Clears all settings, and notifies all registered listeners.
     */
    public void clear() {
        reports.clear();
        defense = null;
        prosecution = null;
        profiles.clear();
        replicates.clear();
        caseNumber = "";
        allLoci.clear();
        locusListDirty = true;
        notifyListeners(ConfigurationDataElement.DEFENSE);
        notifyListeners(ConfigurationDataElement.PROSECUTION);
        notifyListeners(ConfigurationDataElement.PROFILES);
        notifyListeners(ConfigurationDataElement.REPLICATES);
        notifyListeners(ConfigurationDataElement.STATISTICS);
        notifyListeners(ConfigurationDataElement.CASENUMBER);
    }

    /**
     * @return the getName of the mathematical model to be used for the
     *         analysis.
     */
    public String getMathematicalModelName() {
        return modelName;
    }

    /**
     * Sets the getName of the mathematical model to be used for analysis.
     *
     * @param modelName The getName of the model.
     */
    public void setMathematicalModelName(final String modelName) {
        this.modelName = modelName;
        notifyListeners(ConfigurationDataElement.MODELNAME);
    }
    private boolean notifying = false;

    /**
     * Notifies all registered {@link ConfigurationDataChangeListener} classes
     * of a change in the settings
     */
    private void notifyListeners(final ConfigurationDataElement element) {
        if (notifying) {
            return;
        }
        notifying = true;
        for (final ConfigurationDataChangeListener listener : listeners) {
            listener.dataChanged(element);
        }
        notifying = false;
    }

    /**
     * Can be called by any class to suggest that the configured
     * ConfigurationDataChangeListeners are notified of a change in the
     * indicated element.
     *
     * @param configurationDataElement The element to be passed on to the
     *                                 configured listeners.
     */
    public void fireUpdated(final ConfigurationDataElement configurationDataElement) {
        if (configurationDataElement == ACTIVEPROFILES || configurationDataElement == ACTIVEREPLICATES) {
            locusListDirty = true;
        }
        notifyListeners(configurationDataElement);
    }

    /**
     * @return A collection of Strings containing the getNames of all _enabled
     *         loci
     */
    public Collection<String> getEnabledLoci() {
        initLocusList();
        final ArrayList<String> enabledLoci = new ArrayList<>();
        for (final LocusEx locus : allLoci.values()) {
            if (locus.isValid() && locus.isEnabled()) {
                enabledLoci.add(locus.getId());
            }
        }
        LOG.debug("Enabled Loci: {}", enabledLoci);
        return Collections.unmodifiableList(enabledLoci);
    }

    /**
     * @return a list of {@link DisabledLocus} classes describing the disabled loci, or an empty list if no loci were disabled
     */
    public List<DisabledLocus> getDisabledLoci() {
        initLocusList();
        final List<DisabledLocus> disabledLoci = new ArrayList<>();
        for (final LocusEx locus : allLoci.values()) {
            if (locus.isValid()) {
                if (!locus.isEnabled())
                    disabledLoci.add(new DisabledLocus(locus.getId(), "Manually disabled"));
            }
            else {
                final String msg = locus.getStatusDesc().replaceAll("Locus '.+' is ", "");
                disabledLoci.add(new DisabledLocus(locus.getId(), msg.substring(0, 1).toUpperCase() + msg.substring(1)));
            }
        }

        return disabledLoci;
    }

    /**
     * @return A collection of Strings containing all loci (_enabled and
     *         disabled)
     */
    public Collection<String> getAllLoci() {
        initLocusList();
        final ArrayList<String> retval = new ArrayList<>();
        for (final String locus : allLoci.keySet()) {
            retval.add(locus);
        }
        return Collections.unmodifiableCollection(retval);
    }

    /**
     * Enables or disables a locus
     *
     * @param name    The getName of the target locus
     * @param enabled true if the locus is to be _enabled
     */
    public void setLocusEnabled(final String name, final boolean enabled) {
        LOG.debug("Locus {} setLocusEnabled {}", name, enabled);
        if (isLocusValid(name)) {
            final LocusEx locus = allLoci.get(name);
            if (locus.isEnabled() != enabled) {
                locus.setEnabled(enabled);
                notifyListeners(ConfigurationDataElement.ACTIVELOCI);
            }
        }
        else {
            if (enabled) {
                throw new IllegalArgumentException("Locus '" + name + "' is not valid, and cannot be enabled.");
            }
        }
    }

    /**
     * @param name the getName of the target locus
     *
     * @return true if the getNamed locus is _enabled
     */
    public boolean isLocusEnabled(final String name) {
        initLocusList();
        return isLocusValid(name) && allLoci.get(name).isEnabled();
    }

    /**
     * Obtains the status of the named locus. More specifically will contain the
     * reason why a locus is deemed invalid.
     *
     * @param locusName The name of the locus to query
     *
     * @return a String containing the reason why a locus is not valid, or an
     *         empty string if the locus is not invalid.
     */
    public String getLocusStatus(final String name) {
        final LocusEx locusEx = allLoci.get(name);
        if (locusEx == null) {
            return "Locus " + name + " is not known.";
        }
        return locusEx.getStatusDesc();
    }

    /**
     * Queries the list of loci to see if the supplied getName is known for a
     * locus
     *
     * @param name the getName to check
     *
     * @return
     */
    public boolean isLocusValid(final String name) {
        initLocusList();
        return allLoci.containsKey(name) && allLoci.get(name).isValid();
    }

    /**
     * @return the number of unique alleles in the active replicates. If more
     *         than one replicate is loaded, the average allele count over all
     *         active replicates is returned.
     */
    public int getObservedAlleleCount() {
        // Count the unique alleles in the samples, take the average over all replicates
        int observedAlleleCount = 0;
        final Collection<Sample> activeReplicates = getActiveReplicates();
        for (final Sample replicate : activeReplicates) {
            int alleleCount = 0;
            for (final String locusName : getEnabledLoci()) {
                final Locus locus = replicate.getLocus(locusName);
                if (locus != null) {
                    alleleCount += locus.getAlleles().size();
                }
            }
            LOG.debug("Replicate {} has {} alleles", replicate, alleleCount);
            observedAlleleCount += alleleCount;
        }

        // Take the average over all replicates
        if (!activeReplicates.isEmpty()) {
            LOG.debug(observedAlleleCount + " / " + activeReplicates.size() + " = " + observedAlleleCount / activeReplicates.size());
            observedAlleleCount = observedAlleleCount / activeReplicates.size();
        }
        return observedAlleleCount;
    }

    private static void placeInOrder(final Collection<Locus> inputList, final ArrayList<String> output) {
        int idx = 0;
        for (final Locus locus : inputList) {
            if (!output.contains(locus.getName())) {
                output.add(idx, locus.getName());
                idx++;
            }
            else {
                idx = output.indexOf(locus.getName()) + 1;
            }
        }
    }

    /**
     * Initializes the list of loci.
     */
    private synchronized void initLocusList() {
        if (locusListDirty) {
            final LinkedHashMap<String, LocusEx> oldList = new LinkedHashMap<>(allLoci);

            final ArrayList<String> inOrderList = new ArrayList<>();
            for (final Sample s : getActiveReplicates()) {
                placeInOrder(s.getLoci(), inOrderList);
            }

            final ArrayList<String> replicateLoci = new ArrayList<>(inOrderList);

            final ArrayList<String> profileLoci = new ArrayList<>();

            for (final Sample s : getActiveProfiles()) {
                placeInOrder(s.getLoci(), inOrderList);
            }

            allLoci.clear();
            for (final String locusName : inOrderList) {
                LocusEx locusEx = oldList.get(locusName);
                String msg = "";
                boolean isValid = true;

                if (!replicateLoci.contains(locusName)) {
                    isValid = false;
                    msg = "Locus '" + locusName + "' is not present in any of the replicates";
                }
                else {
                    for (final Sample s : getActiveProfiles()) {
                        final Locus referenceLocus = s.getLocus(locusName);
                        if (referenceLocus == null) {
                            isValid = false;
                            msg = "Locus '" + locusName + "' is not present in '" + s.getId() + "'";
                        }
                        else {
                            if (referenceLocus.size() == 0) {
                                isValid = false;
                                if (msg.isEmpty())
                                    msg = "Locus '" + locusName + "' is empty in '" + s.getId() + "'";
                                else
                                    msg += ", '" + s.getId() + "'";
                            }
                        }
                    }
                    msg = msg.replaceFirst(",([^,]+)$", " and$1");

                    if (getStatistics() != null && !getStatistics().isPresent(locusName)) {
                        isValid = false;
                        msg = "Locus '" + locusName + "' is not present in the population statistics";
                    }
                }

                if (locusEx == null) {
                    locusEx = new LocusEx(locusName, true, isValid, msg);
                }
                else {
                    locusEx.setValid(isValid, msg);
                }

                LOG.debug(locusName + " enabled:" + locusEx.isEnabled() + ", valid:" + locusEx.isValid() + ", status: " + msg);

                allLoci.put(locusName, locusEx);
            }
            locusListDirty = false;
        }
    }

    /**
     * Adds a report to the list of reports stored in this object. If a report
     * already exists for the base hypotheses of the new report, the existing
     * report is enriched with the information in the new report.
     *
     * @param report the report to add
     */
    public void addReport(final AnalysisReport report) {
        for (final AnalysisReport eq : getEquivalentReportsForSensitivityAnalysis(report)) {
            if (!eq.getSensitivityAnalysisResults().getRanges().isEmpty()) {
                report.setSensitivityAnalysisResults(eq.getSensitivityAnalysisResults());
                break;
            }
        }

        if (reports.containsKey(report.getGuid())) {
            ((AnalysisReportImpl) reports.get(report.getGuid())).enrich(report);
        }
        else {
            reports.put(report.getGuid(), report);
        }
    }

    /**
     * @return A report based on the currently configured hypotheses as stored
     *         in the configuration. If no report exists for the current set of
     *         hypotheses, a new report object is created. If prosecution or
     *         defense hypotheses have not yet been initialized, null is
     *         returned.
     */
    public AnalysisReport getCurrentReport() {
        if (getProsecution() == null || getDefense() == null) {
            return null;
        }
        final AnalysisReportImpl report = new AnalysisReportImpl(this);
        final AnalysisReport retval = reports.get(report.getGuid());
        if (retval == null) {
            addReport(report);
            return report;
        }
        return retval;
    }

    /**
     * @return An unmodifiable collection of all reports currently stored.
     */
    public Collection<AnalysisReport> getReports() {
        return Collections.unmodifiableCollection(reports.values());
    }

    public int getThreadCount() {
        return ApplicationSettings.isValidationMode() ? 1 : _threadCount;
    }

    public void setThreadCount(final int threadCount) {
        if (!ApplicationSettings.isValidationMode()) {
            _threadCount = threadCount;
        }
    }

    public double getRareAlleleFrequency() {
        return _rareAlleleFrequency;
    }

    public void setRareAlleleFrequency(final Double freq) {
        _rareAlleleFrequency = freq;
        if (_statistics != null) {
            _statistics.setRareAlleleFrequency(_rareAlleleFrequency);
        }
        notifyListeners(ConfigurationDataElement.RARE_ALLELES_FREQUENCY);
    }

    /**
     * @return A collection of Alleles that have been detected as rare in the
     *         current population.
     */
    public Collection<Allele> getRareAlleles() {
        final Collection<Allele> rareAlleles = new ArrayList<>();
        for (final Sample sample : replicates) {
            if (sample.isEnabled()) {
                for (final Locus locus : sample.getLoci()) {
                    if (isLocusEnabled(locus.getName())) {
                        for (final Allele allele : locus.getAlleles()) {
                            if (_statistics.isRareAllele(allele)) {
                                rareAlleles.add(allele);
                            }
                        }
                    }
                }
            }
        }

        for (final Sample sample : profiles) {
            if (sample.isEnabled()) {
                for (final Locus locus : sample.getLoci()) {
                    if (isLocusEnabled(locus.getName())) {
                        for (final Allele allele : locus.getAlleles()) {
                            if (_statistics.isRareAllele(allele)) {
                                rareAlleles.add(allele);
                            }
                        }
                    }
                }
            }
        }
        return rareAlleles;
    }

    public Iterable<AnalysisReport> getEquivalentReportsForSensitivityAnalysis(final AnalysisReport currentReport) {
        final ArrayList<AnalysisReport> equivalentReports = new ArrayList<>();
        for (final AnalysisReport report : reports.values()) {
            if (report.isSensitivityCompatible(currentReport)) {
                equivalentReports.add(report);
            }
        }
        return equivalentReports;
    }

    public Iterable<AnalysisReport> getEquivalentReportsForDropoutEstimate(final AnalysisReport currentReport) {
        final ArrayList<AnalysisReport> equivalentReports = new ArrayList<>();
        for (final AnalysisReport report : reports.values()) {
            if (report.isDropoutCompatible(currentReport)) {
                equivalentReports.add(report);
            }
        }
        return equivalentReports;
    }
}
