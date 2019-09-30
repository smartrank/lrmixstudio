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
package nl.minvenj.nfi.lrmixstudio.gui.tabs.sensitivity;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Priority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.minvenj.nfi.lrmixstudio.domain.Allele;
import nl.minvenj.nfi.lrmixstudio.domain.Contributor;
import nl.minvenj.nfi.lrmixstudio.domain.Hypothesis;
import nl.minvenj.nfi.lrmixstudio.domain.LikelihoodRatio;
import nl.minvenj.nfi.lrmixstudio.domain.Locus;
import nl.minvenj.nfi.lrmixstudio.domain.LocusProbabilities;
import nl.minvenj.nfi.lrmixstudio.domain.Relatedness;
import nl.minvenj.nfi.lrmixstudio.domain.Sample;
import nl.minvenj.nfi.lrmixstudio.gui.ApplicationSettings;
import nl.minvenj.nfi.lrmixstudio.gui.LRmixStudio;
import nl.minvenj.nfi.lrmixstudio.gui.SessionData;
import nl.minvenj.nfi.lrmixstudio.gui.tabs.analysis.AnalysisProgressListenerImpl;
import nl.minvenj.nfi.lrmixstudio.model.AnalysisProgressListener;
import nl.minvenj.nfi.lrmixstudio.model.DropoutEstimation;

public class DropoutEstimationProgressListener implements AnalysisProgressListener {

    private static final Logger LOG = LoggerFactory.getLogger(AnalysisProgressListenerImpl.class);
    private Thread _guiUpdater;
    private int _iteration;
    private int _maxIteration = 1;
    private final SensitivityAnalysisProgressGui _gui;
    private int _hypoCounter;
    private final SessionData _session;
    private long _start;

    private final BigDecimal _dropoutFrom;
    private final BigDecimal _dropoutTo;
    private final Boolean _varyDefenseUnknowns;
    private final Boolean _varyProsecutionUnknowns;
    private final BigDecimal _dropin;
    private String _personsOfInterest;
    private final HashMap<String, int[]> _allCounts;
    private boolean _interrupted;

    public DropoutEstimationProgressListener(final SessionData session, final SensitivityAnalysisProgressGui gui, final Collection<Sample> personsOfIterest, final BigDecimal dropoutFrom, final BigDecimal dropoutTo, final BigDecimal dropin, final Boolean varyProsecutionUnknowns, final Boolean varyDefenseUnknowns) {
        _gui = gui;
        _allCounts = new HashMap<>();
        _hypoCounter = 0;
        _session = session;

        _personsOfInterest = "";
        for (final Sample s : personsOfIterest) {
            _personsOfInterest += "," + s.getId();
        }
        _personsOfInterest = _personsOfInterest.replaceFirst(",", "");

        _varyDefenseUnknowns = varyDefenseUnknowns;
        _varyProsecutionUnknowns = varyProsecutionUnknowns;
        _dropoutFrom = dropoutFrom;
        _dropoutTo = dropoutTo;
        _dropin = dropin;
    }

    @Override
    public void analysisStarted() {
        _guiUpdater = new Thread() {
            @Override
            public void run() {
                initLogger();
                logHeader();
                final long start = System.currentTimeMillis();
                try {
                    while (true) {
                        final int percentDone = (_iteration * 100) / _maxIteration + 1;
                        _gui.setDropoutProgress(percentDone);
                        final long timeLeft = ((System.currentTimeMillis() - start) / percentDone + 1) * (100 - percentDone);
                        _gui.setDropoutTimeLeft(timeLeft + 1000);
                        sleep(50);
                    }
                }
                catch (final InterruptedException ex) {
                }
                finally {
                    logResults();
                    logFooter();
                    resetLogger();
                }
            }
        };
        _guiUpdater.start();
    }

    @Override
    public void analysisFinished(final LikelihoodRatio lr) {
        _guiUpdater.interrupt();
        _session.setStatusMessage("Dropout Estimation completed.");
    }

    @Override
    public void hypothesisStarted(final Hypothesis hypothesis) {
    }

    @Override
    public void hypothesisFinished(final Hypothesis hypothesis, final LocusProbabilities probabilities) {
        _hypoCounter++;
    }

    @Override
    public synchronized void locusStarted(final Hypothesis hypothesis, final String locusName, final long jobsize) {
    }

    @Override
    public void locusFinished(final Hypothesis hypothesis, final String locusName, final Double locusProbability) {
    }

    public void setIteration(final int current, final Hypothesis hypo, final ArrayList<Double> results, final int max) {
        _iteration = max * _hypoCounter + current;
        _maxIteration = max * 2;
        int[] counts = _allCounts.get(hypo.getId());
        if (counts == null) {
            counts = new int[100];
        }
        for (final Double result : results) {
            counts[(int) (result * 100)]++;
        }
        _allCounts.put(hypo.getId(), counts);
    }

    @Override
    public void analysisFinished(final Throwable e) {
        if (!(e instanceof InterruptedException)) {
            LOG.info("Analysis encountered exception:", e);
        }
        else {
            _interrupted = true;
        }
        _guiUpdater.interrupt();
    }

    protected void logHeader() {
        _start = System.currentTimeMillis();
        LOG.info(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
        LOG.info("  LRmixStudio v {}", ApplicationSettings.getProgramVersion());

        if (LRmixStudio.class.getSigners() != null) {
            for (final Object signer : LRmixStudio.class.getSigners()) {
                if (signer instanceof X509Certificate) {
                    final X509Certificate cert = (X509Certificate) signer;
                    String dn = cert.getSubjectDN().getName();
                    String ca = cert.getIssuerDN().getName();
                    dn = dn.substring(dn.indexOf("CN=") + 3);
                    dn = dn.substring(0, dn.indexOf(", "));
                    ca = ca.substring(ca.indexOf("CN=") + 3);
                    ca = ca.substring(0, ca.indexOf(", "));
                    LOG.info("  Signature information: {} certified by {}", dn, ca);
                }
                else {
                    LOG.info("  Signature information: Unknown certificate type {}", signer.getClass().getName());
                }
            }
        }
        else {
            LOG.info("  Signature information: Unsigned");
        }

        LOG.info("  Analysis type: Dropout Estimation");
        LOG.info("  Analysis started by {} on {}", System.getProperty("user.name"), getHostName());
        LOG.info("  Case number: {}", _session.getCaseNumber());
        LOG.info("  Person(s) of interest:       {}", _personsOfInterest);
        LOG.info("  Prosecution Unknowns varied: {}", _varyProsecutionUnknowns ? "Yes" : "No");
        LOG.info("  Defense Unknowns varied:     {}", _varyDefenseUnknowns ? "Yes" : "No");
        LOG.info("  Dropout varies from {} to {}", _dropoutFrom, _dropoutTo);
        LOG.info("  Number of threads: {}", _session.getThreadCount());
        LOG.info("  Max memory: {} bytes, {} MB", Runtime.getRuntime().maxMemory(), Runtime.getRuntime().maxMemory() / 1048576);
        LOG.info("  Java version: {}", System.getProperty("java.version"));
        LOG.info("  Java home: {}", System.getProperty("java.home"));
        LOG.info("=================");
        LOG.info("  Statistics file: {}", _session.getStatistics().getFileName());
        LOG.info("  Statistics file hash: {}", _session.getStatistics().getFileHash());
        LOG.info("  Rare Allele Frequency: {}", _session.getStatistics().getRareAlleleFrequency());
        LOG.info("=================");
        LOG.info("Loaded replicates:");
        for (final Sample sample : _session.getActiveReplicates()) {
            LOG.info("  {} loaded from '{}' file hash {}", sample.getId(), sample.getSourceFile(), sample.getSourceFileHash());
        }
        final Collection<String> enabledLoci = _session.getEnabledLoci();

        LOG.info("=================");
        LOG.info("Loaded profiles:");
        for (final Sample sample : _session.getActiveProfiles()) {
            LOG.info("  {} loaded from '{}' file hash {}", sample.getId(), sample.getSourceFile(), sample.getSourceFileHash());
            final ArrayList<String> homozygotizedLoci = new ArrayList<>();
            for (final Locus locus : sample.getLoci()) {
                if (locus.isTreatedAsHomozygote() && enabledLoci.contains(locus.getName())) {
                    homozygotizedLoci.add(locus.getName());
                }
            }
            if (!homozygotizedLoci.isEmpty()) {
                String locusDescription = homozygotizedLoci.toString().replaceAll("[\\[\\]]", "");
                final int lastComma = locusDescription.lastIndexOf(",");
                if (lastComma > 0) {
                    locusDescription = locusDescription.substring(0, lastComma) + " and" + locusDescription.substring(lastComma + 1);
                }
                LOG.info("    Note: Locus {} contained a single allele. This allele was duplicated and the locus evaluated as homozygotic.", locusDescription);
            }
        }
        LOG.info("=================");
        LOG.info("Enabled loci: {}", enabledLoci);

        final Collection<Allele> rareAlleles = _session.getRareAlleles();
        LOG.info("=================");
        if (!rareAlleles.isEmpty()) {
            LOG.info("The following alleles were detected as rare:");
            for (final Allele a : rareAlleles) {
                LOG.info("  {} at locus {} of {}", a.getAllele(), a.getLocus().getName(), a.getLocus().getSample().getId());
            }
            LOG.info("These alleles have been assigned the following frequency: {}", _session.getProsecution().getPopulationStatistics().getRareAlleleFrequency());
        }
        else {
            LOG.info("No rare alleles detected");
        }

        logHypothesis(_session.getProsecution());
        logHypothesis(_session.getDefense());
    }

    private void logFooter() {
        final long runningTime = System.currentTimeMillis() - _start;
        LOG.info("=================");
        LOG.info("  Analysis Completed");
        LOG.info("  Running time: {}", SessionData.formatDuration(runningTime));
    }

    private void logHypothesis(final Hypothesis hypothesis) {
        LOG.info("=================");
        LOG.info("Hypothesis {}", hypothesis.getId());
        LOG.info("  Contributors {}", hypothesis.getContributors());
        LOG.info("  Non-Contributors {}", hypothesis.getNonContributors());
        LOG.info("  Unknowns {}", hypothesis.getUnknownCount());
        if (hypothesis.getRelatedness().getRelation() == Relatedness.Relation.NONE) {
            LOG.info("  No related unknown contributor");
        }
        else {
            LOG.info("  Related unknown contributor: {} of {}", hypothesis.getRelatedness().getRelation(), hypothesis.getRelatedness().getRelative().getId());
        }
        LOG.info("  Unknown Dropout {}", new BigDecimal(hypothesis.getUnknownDropoutProbability()).setScale(2, RoundingMode.HALF_UP));
        LOG.info("  Dropin {}", _dropin);
        LOG.info("  Theta {}", new BigDecimal(hypothesis.getThetaCorrection()).setScale(2, RoundingMode.HALF_UP));
    }

    private void resetLogger() {
        final FileAppender caseAppender = (FileAppender) org.apache.log4j.Logger.getLogger("CaseLogger").getAppender("CaseAppender");

        if (caseAppender != null) {
            caseAppender.setThreshold(Priority.FATAL);
            caseAppender.setFile("LRmixStudio.log");
            caseAppender.activateOptions();
        }
    }

    private void initLogger() {
        final FileAppender caseAppender = (FileAppender) org.apache.log4j.Logger.getLogger("CaseLogger").getAppender("CaseAppender");
        if (caseAppender != null) {
            final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
            final String logFileName = ApplicationSettings.getCaseFilesPath() + File.separatorChar + "logs" + File.separatorChar
                    + _session.getCaseNumber() + "-"
                    + "DropoutEstimation-" + _personsOfInterest + "-"
                    + "Tr-" + getTraceIDs() + "-"
                    + describeHypothesis(_session.getProsecution()) + "-"
                    + describeHypothesis(_session.getDefense()) + "-"
                    + sdf.format(new Date()) + ".log";

            caseAppender.setFile(logFileName);
            caseAppender.setThreshold(Priority.INFO);
            caseAppender.activateOptions();
        }
    }

    private String getTraceIDs() {
        String traceIDs = "";
        for (final Sample replicate : _session.getCurrentReport().getReplicates()) {
            final String repId = replicate.getId().replaceAll("Rep\\d+$", "");
            if (!traceIDs.contains(repId)) {
                if (!traceIDs.isEmpty()) {
                    traceIDs += ",";
                }
                traceIDs += repId;
            }
        }
        return traceIDs;
    }

    private String describeHypothesis(final Hypothesis hypo) {
        final StringBuilder sb = new StringBuilder();

        sb.append("H").append(hypo.getId().substring(0, 1).toLowerCase()).append("-");
        boolean first = true;
        for (final Contributor c : hypo.getContributors()) {
            if (!first) {
                sb.append(",");
            }
            first = false;
            sb.append(c.getSample().getId());
        }
        if (hypo.getUnknownCount() > 0) {
            if (!first) {
                sb.append(",");
            }
            sb.append(hypo.getUnknownCount()).append("U");
            if (hypo.getRelatedness().getRelation() != Relatedness.Relation.NONE) {
                sb.append("-").append(hypo.getRelatedness().toString().replaceAll("(.{4}).* of ", "$1-"));
            }
        }
        return sb.toString();
    }

    private String getHostName() {
        try {
            return InetAddress.getLocalHost().getCanonicalHostName();
        }
        catch (final UnknownHostException ex) {
            return ex.getClass().getName() + " - " + ex.getMessage();
        }
    }

    private String addPadding(final String value, final int length) {
        if (length < 0) {
            return ("                                        " + value).substring(value.length() + 40 + length);
        }
        return (value + "                                        ").substring(0, length);
    }

    private String formatCounts(final int[] values) {
        return Arrays.toString(values).replaceAll("[\\[\\]]", "");
    }

    private void logResults() {
        LOG.info("================= Results =================");
        if (_interrupted) {
            LOG.info("  Analysis was interrupted. The following results are are partial!");
            LOG.info("              | Counts for iterations yielding the observed number of alleles listed per dropout value");
            LOG.info("  Hypothesis  | 0.00, 0.01, 0.02, ... 0.99");
            LOG.info("  ------------+-----------------------------------------------------------------------------------------------------------------------------");
            LOG.info("  Prosecution | {}", formatCounts(_allCounts.get("Prosecution")));
            LOG.info("  Defense     | {}", formatCounts(_allCounts.get("Defense")));
            LOG.info("  ------------+-----------------------------------------------------------------------------------------------------------------------------");
        }
        else {
            final DropoutEstimation dropoutEstimation = _session.getCurrentReport().getSensitivityAnalysisResults().getDropoutEstimation();
            if (dropoutEstimation != null) {
                LOG.info("              | Dropout values yielding the observed number of alleles");
                LOG.info("  Hypothesis  | Minimum (5%) | Maximum (95%) | Absolute Counts per dropout (0.00, 0.01, 0.02, ... 0.99)");
                LOG.info("  ------------+--------------+---------------+------------------------------------------------------------------------------------------------");
                LOG.info("  Prosecution |  {}  |   {}  | {}", addPadding("" + dropoutEstimation.getProsecutionMinimum(), -10), addPadding("" + dropoutEstimation.getProsecutionMaximum(), -10), formatCounts(_allCounts.get("Prosecution")));
                LOG.info("  Defense     |  {}  |   {}  | {}", addPadding("" + dropoutEstimation.getDefenseMinimum(), -10), addPadding("" + dropoutEstimation.getDefenseMaximum(), -10), formatCounts(_allCounts.get("Defense")));
                LOG.info("  ------------+--------------+---------------+------------------------------------------------------------------------------------------------");
                LOG.info("  Summary:");
                LOG.info("    Number of replicates:           {}", dropoutEstimation.getReplicateCount());
                LOG.info("    Total number of alleles:        {}", dropoutEstimation.getAlleleCount());
                LOG.info("    Number of iterations:           {}", dropoutEstimation.getIterations());
                LOG.info("    Minimum matching dropout (5%):  {}", dropoutEstimation.getMinimum());
                LOG.info("    Maximum matching dropout (95%): {}", dropoutEstimation.getMaximum());
            }
            else {
                LOG.info("  No results available.");
            }
        }
    }
}
