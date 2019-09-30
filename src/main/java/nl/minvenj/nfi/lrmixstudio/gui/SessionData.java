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

import java.awt.EventQueue;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.minvenj.nfi.lrmixstudio.domain.Hypothesis;
import nl.minvenj.nfi.lrmixstudio.domain.Locus;
import nl.minvenj.nfi.lrmixstudio.domain.PopulationStatistics;
import nl.minvenj.nfi.lrmixstudio.domain.Relatedness;
import nl.minvenj.nfi.lrmixstudio.domain.Sample;
import nl.minvenj.nfi.lrmixstudio.gui.PathResolver.PathResolverAbortedException;
import nl.minvenj.nfi.lrmixstudio.io.PopulationStatisticsReader;
import nl.minvenj.nfi.lrmixstudio.io.SampleReader;
import nl.minvenj.nfi.lrmixstudio.model.ConfigurationData;
import nl.minvenj.nfi.lrmixstudio.model.ConfigurationDataElement;

/**
 * This class extends the {@link ConfigurationData} class to add some
 * GUI-specific items
 */
public class SessionData extends ConfigurationData {

    private static final Logger LOG = LoggerFactory.getLogger(SessionData.class);
    private final SessionData delegate;
    private final ArrayList<ApplicationStateChangeListener> listeners = new ArrayList<>();
    private boolean notifying;
    private ApplicationStateChangeListener.APP_STATE state;
    private String statusMessage;
    private String errorMessage;
    private int progressPercentage;
    private int _threadCount;
    private final NumberFormat _numberFormat;
    private static final String FORMAT = "##0.####";

    /**
     * Creates a new SessionData object
     */
    public SessionData() {
        super();
        delegate = this;
        _numberFormat = new LRDyNAmixNumberFormat();
        _numberFormat.setGroupingUsed(false);
        state = ApplicationStateChangeListener.APP_STATE.WAIT_SAMPLE;
        LOG.debug("Created new SessionData. ApplicationState is {}", state);
    }

    public SessionData(final SessionData session) {
        super(session);
        delegate = session;
        _numberFormat = delegate._numberFormat;
        LOG.debug("Created new SessionData. Delegate is {}", delegate);
    }

    @Override
    public void clear() {
        super.clear();
        statusMessage = "";
        errorMessage = "";
    }

    public void setApplicationState(final ApplicationStateChangeListener.APP_STATE newState) {
        LOG.debug("Setting state to {}", newState);
        delegate.state = newState;

        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (delegate.notifying) {
                    return;
                }
                delegate.notifying = true;

                for (final ApplicationStateChangeListener listener : delegate.listeners) {
                    listener.applicationStateChanged(delegate.state);
                }
                delegate.notifying = false;
            }
        });
    }

    public ApplicationStateChangeListener.APP_STATE getApplicationState() {
        return delegate.state;
    }

    public void addStateChangeListener(final ApplicationStateChangeListener listener) {
        if (!delegate.listeners.contains(listener)) {
            delegate.listeners.add(listener);
            listener.applicationStateChanged(delegate.state);
        }
    }

    public void setErrorMessage(final Throwable t) {
        if (t != null) {
            String msg = "<html><b>" + t.getClass().getName() + "</b>";
            if (t.getMessage() != null) {
                msg += "<br>" + t.getMessage();
            }
            setErrorMessage(msg + "<br>Check the logfile for details.");
        }
    }

    public void setStatusMessage(final String statusMessage) {
        LOG.debug("Setting Status message to {}", statusMessage);
        if (statusMessage == null || !statusMessage.equals(this.statusMessage)) {
            this.statusMessage = statusMessage;
            fireUpdated(ConfigurationDataElement.STATUS_MESSAGE);
        }
    }

    public void setErrorMessage(final String errorMessage) {
        this.errorMessage = errorMessage;
        fireUpdated(ConfigurationDataElement.ERROR_MESSAGE);
    }

    /**
     * @return the statusMessage
     */
    public String getStatusMessage() {
        return statusMessage;
    }

    /**
     * @return the errorMessage
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Sets the percentage ready for the current long-running operation.
     *
     * @param percentage The current percentage ready
     */
    public void setProgress(final int percentage) {
        this.progressPercentage = percentage;
        fireUpdated(ConfigurationDataElement.PERCENTREADY);
    }

    /**
     * @return the percentage ready for the current long-running operation.
     */
    public int getProgress() {
        return progressPercentage;
    }

    /**
     * Restores a session from a log file
     *
     * @param resolver     A PathResolver to allow the user to specify the local
     *                     location of a file mentioned in the logfile. Useful
     *                     when loading other people's logfiles.
     * @param selectedFile The log file to read
     *
     * @return true if session was successfully restored
     */
    public boolean restore(final PathResolver resolver, final File selectedFile) {
        try {
            final SessionReader sessionReader = new SessionReader();
            sessionReader.read(selectedFile);

            // Clear the session
            clear();

            // Set the case number
            setCaseNumber(sessionReader.getCaseNumber());

            // Load Population statistics
            PopulationStatistics stats = null;
            boolean ask = false;
            do {
                final String localStatsFile = resolver.resolve(sessionReader.getPopulationStatisticsFile(), ask);
                final PopulationStatisticsReader populationStatisticsReader = new PopulationStatisticsReader(localStatsFile);
                stats = populationStatisticsReader.getStatistics();
                if (!sessionReader.getFileHash(sessionReader.getPopulationStatisticsFile()).equals(stats.getFileHash())) {
                    LOG.error("File hash for population statistics does not match!\nExpected " + sessionReader.getFileHash(sessionReader.getPopulationStatisticsFile()) + " but found " + stats.getFileHash() + "!");
                    if (ask) {
                        setErrorMessage("The population statistics file you selected does not have the same content as the file mentioned in the log! Please try again...");
                    }
                    stats = null;
                    ask = true;
                }
                else {
                    stats.setRareAlleleFrequency(sessionReader.getRareAlleleFrequency());
                }
            } while (stats == null);

            ApplicationSettings.setAlleleFrequenciesPath(stats.getFileName());
            setStatistics(stats);

            // Load replicates
            boolean atLeastOneReplicateEnabled = false;
            for (final String replicateFile : sessionReader.getReplicateFiles()) {
                SampleReader sampleReader = null;
                ask = false;
                do {
                    sampleReader = new SampleReader(resolver.resolve(replicateFile, ask), true);
                    if (!sessionReader.getFileHash(replicateFile).equals(sampleReader.getFileHash())) {
                        LOG.error("File hash for sample file '{}' does not match! Expected {} but found {}", sampleReader.getFileName(), sessionReader.getFileHash(replicateFile), sampleReader.getFileHash());
                        if (ask) {
                            setErrorMessage("The sample file you selected does not have the same content as the file mentioned in the log! Please try again...");
                        }
                        sampleReader = null;
                        ask = true;
                    }
                } while (sampleReader == null);

                for (final Sample sample : sampleReader.getSamples()) {
                    sample.setEnabled(sessionReader.isSampleEnabled(sample.getId()));
                    atLeastOneReplicateEnabled |= sample.isEnabled();
                }
                addReplicates(sampleReader.getSamples());
                ApplicationSettings.setCaseFilesPath(new File(sampleReader.getFileName()).getParent());
            }

            if (!atLeastOneReplicateEnabled) {
                setErrorMessage("No replicates were enabled.\nPossibly you are loading a logfile that is stored in an old format.\nPlease review the settings on the Sample Files page.");
            }

            // Load samples
            boolean atLeastOneProfileIsEnabled = false;
            for (final String sampleFile : sessionReader.getReferenceProfileFiles()) {

                SampleReader sampleReader;
                ask = false;
                do {
                    sampleReader = new SampleReader(resolver.resolve(sampleFile, ask), false);
                    if (!sessionReader.getFileHash(sampleFile).equals(sampleReader.getFileHash())) {
                        LOG.error("File hash for reference profile file '{}' does not match! Expected {} but found {}", sampleReader.getFileName(), sessionReader.getFileHash(sampleFile), sampleReader.getFileHash());
                        if (ask) {
                            setErrorMessage("The reference profile file you selected does not have the same content as the file mentioned in the log! Please try again...");
                        }
                        sampleReader = null;
                        ask = true;
                    }
                } while (sampleReader == null);

                for (final Sample sample : sampleReader.getSamples()) {
                    sample.setEnabled(sessionReader.isSampleEnabled(sample.getId()));
                    atLeastOneProfileIsEnabled |= sample.isEnabled();

                    for (final Locus locus : sample.getLoci()) {
                        if (locus.size() == 1 && sessionReader.isSampleTreatedAsHomozygote(sample.getId())) {
                            locus.addAllele(locus.getAlleles().iterator().next());
                            locus.setTreatedAsHomozygote();
                        }
                    }
                }

                addProfiles(sampleReader.getSamples());
                ApplicationSettings.setCaseFilesPath(new File(sampleReader.getFileName()).getParent());
            }

            if (!atLeastOneProfileIsEnabled) {
                setErrorMessage("No reference profiles were enabled.\nPossibly you are loading a logfile that is stored in an old format.\nPlease review the settings on the Reference Files page.");
            }

            if (getCaseNumber().isEmpty()) {
                final String selectedFileName = selectedFile.getName();
                if (selectedFileName.matches("LRmixStudio-.+-\\d{8}-\\d{6}\\.log")) {
                    setCaseNumber(selectedFileName.substring(10, selectedFileName.length() - 20));
                }
            }

            // Enable or disable loci
            if (atLeastOneReplicateEnabled) {
                for (final String locus : getAllLoci()) {
                    setLocusEnabled(locus, sessionReader.isLocusEnabled(locus));
                }
            }

            // Create prosecution hypothesis
            Hypothesis prosecutionHypothesis = null;
            if (sessionReader.hasProsecution()) {
                prosecutionHypothesis = new Hypothesis("Prosecution", stats);
                prosecutionHypothesis.setThetaCorrection(sessionReader.getProsecutionTheta());
                prosecutionHypothesis.setUnknownCount(sessionReader.getProsecutionUnknowns());
                prosecutionHypothesis.setUnknownDropoutProbability(sessionReader.getProsecutionUnknownDropout());
                prosecutionHypothesis.setDropInProbability(sessionReader.getProsecutionDropin());
            }

            // Load defense hypothesis
            Hypothesis defenseHypothesis = new Hypothesis("Defense", stats);
            if (sessionReader.hasDefense()) {
                defenseHypothesis = new Hypothesis("Defense", stats);
                defenseHypothesis.setThetaCorrection(sessionReader.getDefenseTheta());
                defenseHypothesis.setUnknownCount(sessionReader.getDefenseUnknowns());
                defenseHypothesis.setUnknownDropoutProbability(sessionReader.getDefenseUnknownDropout());
                defenseHypothesis.setDropInProbability(sessionReader.getDefenseDropin());
                defenseHypothesis.getRelatedness().setRelation(sessionReader.getDefenseUnknownsRelation());
            }

            // Set contributors for each hypothesis
            for (final Sample sample : getAllProfiles()) {
                final double prosecutionDropout = sessionReader.getProsecutionDropout(sample.getId());
                if (prosecutionDropout >= 0) {
                    prosecutionHypothesis.addContributor(sample, prosecutionDropout);
                }
                else {
                    prosecutionHypothesis.addNonContributor(sample, 0);
                }
                final double defenseDropout = sessionReader.getDefenseDropout(sample.getId());
                if (defenseDropout >= 0) {
                    defenseHypothesis.addContributor(sample, defenseDropout);
                }
                else {
                    defenseHypothesis.addNonContributor(sample, 0);
                }

                if (sample.getId().equalsIgnoreCase(sessionReader.getDefenseUnknownsRelationSampleName())) {
                    defenseHypothesis.getRelatedness().setRelative(sample);
                }
            }
            setProsecution(prosecutionHypothesis);
            setDefense(defenseHypothesis);

        }
        catch (final PathResolverAbortedException ex) {
            clear();
            LOG.info("User aborted session restore from '{}'", selectedFile);
            setErrorMessage("Session restore cancelled");
            return false;
        }
        catch (final Exception ex) {
            clear();
            LOG.error("Error restoring session from {}", selectedFile, ex);
            setErrorMessage("Cannot restore this session: " + ex.getMessage());
            return false;
        }
        return true;
    }

    private String getHostName() {
        try {
            return InetAddress.getLocalHost().getCanonicalHostName();
        }
        catch (final UnknownHostException ex) {
            return ex.getClass().getName() + " - " + ex.getMessage();
        }
    }

    public boolean save(final String fileName) {
        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss\n");
            fos.write(sdf.format(new Date()).getBytes());
            fos.write(("  LRmixStudio v " + getProgramVersion() + "\n").getBytes());
            if (getCaseNumber() != null) {
                fos.write(("  Case number: " + getCaseNumber() + "\n").getBytes());
            }
            fos.write(String.format("  Stored session\n").getBytes());
            fos.write(String.format("  File saved by %s on %s\n", System.getProperty("user.name"), getHostName()).getBytes());

            fos.write("=================\n".getBytes());
            if (getStatistics() != null) {
                fos.write(("  Statistics file: " + getStatistics().getFileName() + "\n").getBytes());
                fos.write(("  Statistics file hash: " + getStatistics().getFileHash() + "\n").getBytes());
                fos.write(("  Rare Allele Frequency: " + getRareAlleleFrequency() + "\n").getBytes());
            }
            fos.write("=================\n".getBytes());
            fos.write("Loaded replicates:\n".getBytes());
            for (final Sample replicate : getActiveReplicates()) {
                fos.write(String.format("  %s loaded from '%s' file hash %s\n", replicate.getId(), replicate.getSourceFile(), replicate.getSourceFileHash()).getBytes());
            }
            fos.write("=================\n".getBytes());
            fos.write("Loaded reference profiles:\n".getBytes());
            for (final Sample profile : getActiveProfiles()) {
                fos.write(String.format("  %s loaded from '%s' file hash %s\n", profile.getId(), profile.getSourceFile(), profile.getSourceFileHash()).getBytes());
            }
            fos.write("=================\n".getBytes());
            fos.write(String.format("Enabled loci: %s\n", getEnabledLoci()).getBytes());
            fos.write("=================\n".getBytes());
            fos.write("=================\n".getBytes());
            if (getProsecution() != null) {
                writeHypothesis(fos, getProsecution());
            }
            fos.write("=================\n".getBytes());
            if (getDefense() != null) {
                writeHypothesis(fos, getDefense());
            }
        }
        catch (final FileNotFoundException ex) {
            setErrorMessage(ex.getMessage());
            return false;
        }
        catch (final IOException ex) {
            setErrorMessage(ex.getMessage());
            return false;
        }
        return true;
    }

    public String formatNumber(final Number number) {
        final String retval = number == null ? "null" : _numberFormat.format(number);
        return retval.replaceAll("^(\\d)", " $1")/*
                 * .replaceAll("(\\d)E", "$1 E").replaceAll("E(\\d)", "E $1")
                 */;
    }

    private void writeHypothesis(final FileOutputStream fos, final Hypothesis hypothesis) throws IOException {
        fos.write(String.format("Hypothesis %s\n", hypothesis.getId()).getBytes());
        fos.write(String.format("  Contributors %s\n", hypothesis.getContributors()).getBytes());
        fos.write(String.format("  Non-Contributors %s\n", hypothesis.getNonContributors()).getBytes());
        fos.write(String.format("  Unknowns %s\n", hypothesis.getUnknownCount()).getBytes());
        if (hypothesis.getRelatedness().getRelation() != Relatedness.Relation.NONE) {
            fos.write(String.format("  Related unknown contributor: %s\n", hypothesis.getRelatedness()).getBytes());
        }
        else {
            fos.write("  No related unknown contributor\n".getBytes());
        }
        fos.write(String.format("  Unknown Dropouts %s\n", new DecimalFormat("0.00").format(hypothesis.getUnknownDropoutProbability())).getBytes());
        fos.write(String.format("  Dropin %s\n", new DecimalFormat("0.00").format(hypothesis.getDropInProbability())).getBytes());
        fos.write(String.format("  Theta %s\n", new DecimalFormat("0.00").format(hypothesis.getThetaCorrection())).getBytes());
    }

    public static String formatDuration(final long duration) {
        final long milliseconds = duration % 1000;
        final long seconds = (duration % 60000) / 1000;
        final long minutes = (duration / 60000) % 360000;
        final long hours = duration / 360000;
        final StringBuilder sb = new StringBuilder();
        if (hours > 0) {
            sb.append(hours).append(" hour");
            if (hours > 1) {
                sb.append("s");
            }
        }
        if (minutes > 0) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(minutes).append(" minute");
            if (minutes > 1) {
                sb.append("s");
            }
        }
        if (seconds > 0) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(seconds).append(" second");
            if (seconds > 1) {
                sb.append("s");
            }
        }
        if (milliseconds > 0) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(milliseconds).append(" millisecond");
            if (milliseconds > 1) {
                sb.append("s");
            }
        }
        return sb.toString();
    }

    public static void main(final String[] args) {
        System.out.println(SessionData.formatDuration(1001));
        System.out.println(SessionData.formatDuration(20002));
        System.out.println(SessionData.formatDuration(60003));
        System.out.println(SessionData.formatDuration(63004));
        System.out.println(SessionData.formatDuration(360000));
        System.out.println(SessionData.formatDuration(3600006));
        System.out.println(SessionData.formatDuration(123456));
    }
}
