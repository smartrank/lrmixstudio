/*
 * Copyright (c) 2013, Netherlands Forensic Institute
 * All rights reserved.
 */
package nl.minvenj.nfi.lrmixstudio.gui.tabs.sensitivity;

import java.awt.EventQueue;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.RejectedExecutionException;

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
import nl.minvenj.nfi.lrmixstudio.gui.SessionData;
import nl.minvenj.nfi.lrmixstudio.model.AnalysisProgressListener;
import nl.minvenj.nfi.lrmixstudio.model.SensitivityAnalysisResults.Point;

/**
 *
 * @author dejong
 */
public class SensitivityAnalysisProgressListener implements AnalysisProgressListener {

    private static final Logger LOG = LoggerFactory.getLogger(SensitivityAnalysisProgressListener.class);
    private final Thread guiUpdater;
    private int iteration;
    private int maxIteration;
    private final SensitivityAnalysisProgressGui _gui;
    private long detailCurrent;
    private long detailSize;
    private final long _overallStartTime;
    private long _mostRecentFinishTime;
    private long detailStartTime;
    private final SessionData _session;

    private final HashMap<String, Long> jobSizes = new HashMap<>();
    private final HashMap<String, Long> jobCounts = new HashMap<>();
    private final Collection<Point> _currentPoints = new ArrayList<>();
    private boolean _drawGraph;
    private long _start;
    private String _personsOfInterest;
    private final BigDecimal _dropoutFrom;
    private final BigDecimal _dropoutTo;
    private final Boolean _varyDefenseUnknowns;
    private final Boolean _varyProsecutionUnknowns;
    private final BigDecimal _theta;
    private final BigDecimal _dropin;
    private final String _targetLocus;

    public SensitivityAnalysisProgressListener(final SessionData session, final SensitivityAnalysisProgressGui gui, final String targetLocus, final Collection<Sample> personsOfIterest, final BigDecimal dropoutFrom, final BigDecimal dropoutTo, final BigDecimal dropin, final BigDecimal theta, final Boolean varyProsecutionUnknowns, final Boolean varyDefenseUnknowns) {
        _gui = gui;
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
        _theta = theta;
        _dropin = dropin;
        _targetLocus = targetLocus;

        _overallStartTime = System.currentTimeMillis();
        _mostRecentFinishTime = _overallStartTime;
        guiUpdater = new Thread() {
            @Override
            public void run() {
                initLogger();
                logHeader();
                try {
                    while (!isInterrupted()) {
                        final int overallPercentDone = maxIteration > 0 ? (iteration * 100) / maxIteration : 0;
                        final int detailPercentDone = detailSize > 0 ? (int) ((detailCurrent * 100) / detailSize) : 0;
                        final long now = System.currentTimeMillis();
                        final long overallTimeLeft = ((100 - overallPercentDone) * (_mostRecentFinishTime - _overallStartTime)) / (overallPercentDone + 1) - (now - _mostRecentFinishTime);
                        final long detailTimeLeft = ((100 - detailPercentDone) * (now - detailStartTime)) / (detailPercentDone + 1);
                        _gui.setSensitivityTimeLeft(detailTimeLeft + 1000, overallTimeLeft + 1000);
                        _gui.setSensitivityProgress(detailPercentDone, overallPercentDone);
                        if (_drawGraph) {
                            try {
                                EventQueue.invokeAndWait(new Runnable() {
                                    @Override
                                    public void run() {
                                        _gui.updateGraph(_currentPoints);
                                    }
                                });
                            }
                            catch (final InvocationTargetException ex) {
                                // Write the error message to the application log (not the analysis log)
                                LoggerFactory.getLogger(LRmixStudio.class).error("There was an error updating the dropout graph.", ex);
                            }
                            _drawGraph = false;
                        }
                        sleep(100);
                    }
                }
                catch (final InterruptedException ex) {
                    _session.setApplicationState(ApplicationStateChangeListener.APP_STATE.READY_FOR_ANALYSIS);
                }
                finally {
                    logFooter();
                    resetLogger();
                }
            }
        };
        guiUpdater.start();
    }

    public void analysisFinished() {
        guiUpdater.interrupt();
        _session.setStatusMessage("Sensitivity Analysis completed.");
    }

    @Override
    public void analysisStarted() {
        detailCurrent = 0;
        detailSize = 0;
        detailStartTime = System.currentTimeMillis();
    }

    @Override
    public void analysisFinished(final LikelihoodRatio lr) {
        iteration++;
        _mostRecentFinishTime = System.currentTimeMillis();
    }

    @Override
    public void hypothesisStarted(final Hypothesis hypothesis) {
    }

    @Override
    public void hypothesisFinished(final Hypothesis hypothesis, final LocusProbabilities probabilities) {
    }

    @Override
    public synchronized void locusStarted(final Hypothesis hypothesis, final String locusName, final long jobsize) {
        detailSize += jobsize;
        final String id = hypothesis.getId() + locusName;
        Long size = jobSizes.get(id);
        if (size == null) {
            size = new Long(0);
        }
        size += jobsize;
        jobSizes.put(id, size);

        Long count = jobCounts.get(id);
        if (count == null) {
            count = new Long(0);
        }
        count++;
        jobCounts.put(id, count);
    }

    @Override
    public synchronized void locusFinished(final Hypothesis hypothesis, final String locusName, final Double locusProbability) {
        final String id = hypothesis.getId() + locusName;
        detailCurrent += jobSizes.get(id) / jobCounts.get(id);
    }

    public void setIteration(final int current, final int max) {
        iteration = current;
        maxIteration = max;
    }

    public void setResult(final BigDecimal dropout, final LikelihoodRatio lr) {
        synchronized (LOG) {
            LOG.info("  {} {} {} {} {}",
                    addPadding("" + dropout, 10),
                    addPadding("" + lr.getOverallRatio().getProsecutionProbability(), 30),
                    addPadding("" + lr.getOverallRatio().getDefenseProbability(), 30),
                    addPadding("" + lr.getOverallRatio().getRatio(), 30),
                    Math.log10(lr.getOverallRatio().getRatio()));
        }
    }

    public void setPoints(final Collection<Point> points) {
        if (!_drawGraph) {
            _currentPoints.clear();
            _currentPoints.addAll(points);
            _drawGraph = true;
        }
    }

    @Override
    public void analysisFinished(final Throwable e) {
        LOG.debug("Analysis finished with exception", e);
        if (!(e instanceof InterruptedException) && !(e.getCause() instanceof InterruptedException) && !(e instanceof RejectedExecutionException)) {
            LOG.error("Analysis Finished!", e);
            _session.setErrorMessage(e.getMessage());
        }
        guiUpdater.interrupt();
    }

    private void logHeader() {
        synchronized (LOG) {
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

            LOG.info("  Analysis type: Sensitivity Analysis");
            LOG.info("  Analysis started by {} on {}", System.getProperty("user.name"), getHostName());
            LOG.info("  Case number: {}", _session.getCaseNumber());
            LOG.info("  Person(s) of interest: {}", _personsOfInterest);
            LOG.info("  Prosecution Unknowns varied: {}", _varyProsecutionUnknowns ? "Yes" : "No");
            LOG.info("  Defense Unknowns varied: {}", _varyDefenseUnknowns ? "Yes" : "No");
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

            LOG.info("Enabled loci: {}", _targetLocus.equalsIgnoreCase("All loci") ? enabledLoci : _targetLocus);

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

            LOG.info("================= Results =================");
            LOG.info("  DropOut    Pr(E|Hp)                       Pr(E|Hd)                       LR                             log10(LR)");
            LOG.info("  ---------------------------------------------------------------------------------------------------------------------------------");
        }
    }

    private void logFooter() {
        final long runningTime = System.currentTimeMillis() - _start;
        LOG.info("=================");
        LOG.info("  Analysis Completed");
        LOG.info("  Running time: {}", _session.formatDuration(runningTime));
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
        LOG.info("  Theta {}", _theta);
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
                    + "Sensitivity-" + _personsOfInterest + "-"
                    + "Tr-" + getTraceIDs() + "-"
                    + describeHypothesis(_session.getProsecution()) + "-"
                    + describeHypothesis(_session.getDefense()) + "-"
                    + "T-" + _theta + "-"
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
        final String retval = value + "                                        ";
        return retval.substring(value.startsWith("-") ? 1 : 0, length);
    }
}
