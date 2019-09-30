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
package nl.minvenj.nfi.lrmixstudio.gui.tabs.noncontributor;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

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
import nl.minvenj.nfi.lrmixstudio.gui.ApplicationStateChangeListener;
import nl.minvenj.nfi.lrmixstudio.gui.LRmixStudio;
import nl.minvenj.nfi.lrmixstudio.gui.ProgressGui;
import nl.minvenj.nfi.lrmixstudio.gui.SessionData;
import nl.minvenj.nfi.lrmixstudio.model.AnalysisProgressListener;
import nl.minvenj.nfi.lrmixstudio.model.NonContributorTestResults;

/**
 *
 * @author dejong
 */
class NonContributorTestProgressListener implements AnalysisProgressListener {

    private static final Logger LOG = LoggerFactory.getLogger(NonContributorTestProgressListener.class);
    private final ProgressGui _gui;
    private final SessionData _session;
    private Thread _guiThread;
    private int _progress;
    private long _start;
    private final String _personOfInterest;
    private final ArrayList<Double> _prosecutionProbabilities;
    private final AtomicBoolean _summaryLogged;

    public NonContributorTestProgressListener(final ProgressGui gui, final SessionData session, final String personOfInterest) {
        this._summaryLogged = new AtomicBoolean(false);
        _gui = gui;
        _session = session;
        _prosecutionProbabilities = new ArrayList<>();
        _personOfInterest = personOfInterest.replaceAll("[\\[\\]]", "");
    }

    private String getHostName() {
        try {
            return InetAddress.getLocalHost().getCanonicalHostName();
        } catch (final UnknownHostException ex) {
            return ex.getClass().getName() + " - " + ex.getMessage();
        }
    }

    void analysisStarted(final Integer iterations) {
        _session.setApplicationState(ApplicationStateChangeListener.APP_STATE.NONCONTRIBUTOR_TEST_RUNNING);
        _guiThread = new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        final NonContributorTestResults results = new NonContributorTestResults(_personOfInterest, _progress, _session.getCurrentReport().getLikelihoodRatio(), (Collection<Double>) _prosecutionProbabilities.clone());
                        _gui.setAnalysisProgress((_progress * 1000) / iterations);
                        _gui.plotResults(results);
                        sleep(200);
                    }
                } catch (final InterruptedException ex) {
                }
            }
        };
        _guiThread.start();

        initLogger();
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
                } else {
                    LOG.info("  Signature information: Unknown certificate type {}", signer.getClass().getName());
                }
            }
        } else {
            LOG.info("  Signature information: Unsigned");
        }

        LOG.info("  Analysis type: Non-contributor Test");
        LOG.info("  Analysis started by {} on {}", System.getProperty("user.name"), getHostName());
        LOG.info("  Person of interest: {}", _personOfInterest);
        LOG.info("  Case number: {}", _session.getCaseNumber());
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
        } else {
            LOG.info("No rare alleles detected");
        }

        logHypothesis(_session.getProsecution());
        logHypothesis(_session.getDefense());
    }

    public void analysisFinished(final NonContributorTestResults results) {
        if (!_summaryLogged.getAndSet(true)) {
            _guiThread.interrupt();
            final long runningTime = System.currentTimeMillis() - _start;
            logResults(results);
            LOG.info("  Analysis Completed");
            LOG.info("  Running time: {}", _session.formatDuration(runningTime));
            resetLogger();
            _session.setStatusMessage("Non-contributor Test completed.");
            _session.setApplicationState(ApplicationStateChangeListener.APP_STATE.READY_FOR_ANALYSIS);
        }
    }

    private void logResults(final NonContributorTestResults results) {
        LOG.info("=================");
        LOG.info("  Test results:");
        LOG.info("   Number of iterations: {}", results.getIterations());
        LOG.info("   Log10(LR) for questioned contributor under Hp ({}): {}", _personOfInterest, _session.formatNumber(results.getOriginalLR()));
        LOG.info("   Distribution of LRs for random contributors (Log10):");
        LOG.info("     Minimum: {}", _session.formatNumber(results.getMinimum()));
        LOG.info("          1%: {}", _session.formatNumber(results.getOnePercent()));
        LOG.info("         50%: {}", _session.formatNumber(results.getFiftyPercent()));
        LOG.info("         99%: {}", _session.formatNumber(results.getNinetyninePercent()));
        LOG.info("     Maximum: {}", _session.formatNumber(results.getMaximum()));
        LOG.info("   LRs>1:      {}% ({} of {})", results.getPercentageOver1(), results.getOver1Count(), results.getIterations());
        LOG.info("   LRs>LR_POI: {}% ({} of {})", results.getPercentageOverOriginalLR(), results.getOverOriginalCount(), results.getIterations());
        LOG.info("=================");
    }

    public void iterationDone(final Double prosecutionProbability) {
        _prosecutionProbabilities.add(prosecutionProbability);
        _progress++;
    }

    @Override
    public void analysisStarted() {
    }

    @Override
    public void analysisFinished(final LikelihoodRatio lr) {
    }

    @Override
    public void analysisFinished(final Throwable e) {
        if (!_summaryLogged.getAndSet(true)) {
            _guiThread.interrupt();
            LOG.error(e.getMessage(), e);
            _session.setErrorMessage(e);
            _session.setApplicationState(ApplicationStateChangeListener.APP_STATE.READY_FOR_ANALYSIS);
        }
    }

    public void analysisFinished(final Exception e, final NonContributorTestResults results) {
        if (!_summaryLogged.getAndSet(true)) {
            final long runningTime = System.currentTimeMillis() - _start;
            _guiThread.interrupt();
            logResults(results);
            LOG.info("  Analysis terminated with an error after {} ms", runningTime);
            LOG.error("  The following error was encountered:", e);
            resetLogger();
            _session.setErrorMessage(e);
            _session.setApplicationState(ApplicationStateChangeListener.APP_STATE.READY_FOR_ANALYSIS);
        }
    }

    @Override
    public void hypothesisStarted(final Hypothesis hypothesis) {
    }

    @Override
    public void hypothesisFinished(final Hypothesis hypothesis, final LocusProbabilities probability) {
    }

    @Override
    public void locusStarted(final Hypothesis hypothesis, final String locusName, final long jobsize) {
    }

    @Override
    public void locusFinished(final Hypothesis hypothesis, final String locusName, final Double locusProbability) {
    }

    private void logHypothesis(final Hypothesis hypothesis) {
        LOG.info("=================");
        LOG.info("Hypothesis {}", hypothesis.getId());
        LOG.info("  Contributors {}", hypothesis.getContributors());
        LOG.info("  Unknowns {}", hypothesis.getUnknownCount());
        if (hypothesis.getRelatedness().getRelation() == Relatedness.Relation.NONE) {
            LOG.info("  No related unknown contributor");
        }
        else {
            LOG.info("  Related unknown contributor: {} of {}", hypothesis.getRelatedness().getRelation(), hypothesis.getRelatedness().getRelative().getId());
        }
        LOG.info("  Unknown Dropout {}", hypothesis.getUnknownDropoutProbability());
        LOG.info("  Dropin {}", hypothesis.getDropInProbability());
        LOG.info("  Theta {}", hypothesis.getThetaCorrection());
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
                    + "NonContributor-" + _personOfInterest + "-"
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
}
