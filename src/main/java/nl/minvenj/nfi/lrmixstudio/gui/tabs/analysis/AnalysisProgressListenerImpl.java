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
package nl.minvenj.nfi.lrmixstudio.gui.tabs.analysis;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

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
import nl.minvenj.nfi.lrmixstudio.domain.Ratio;
import nl.minvenj.nfi.lrmixstudio.domain.Relatedness.Relation;
import nl.minvenj.nfi.lrmixstudio.domain.Sample;
import nl.minvenj.nfi.lrmixstudio.gui.ApplicationSettings;
import nl.minvenj.nfi.lrmixstudio.gui.ApplicationStateChangeListener;
import nl.minvenj.nfi.lrmixstudio.gui.LRmixStudio;
import nl.minvenj.nfi.lrmixstudio.gui.ProgressGui;
import nl.minvenj.nfi.lrmixstudio.gui.SessionData;
import nl.minvenj.nfi.lrmixstudio.model.AnalysisProgressListener;

/**
 *
 * @author dejong
 */
public class AnalysisProgressListenerImpl implements AnalysisProgressListener {

    private class MyLong {

        private long value = 0;

        public MyLong(final long v) {
            value = v;
        }

        private void set(final long longValue) {
            value = longValue;
        }

        private long longValue() {
            return value;
        }

        private void incrementAndGet() {
            value++;
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(AnalysisProgressListenerImpl.class);
    private final AtomicBoolean summaryLogged = new AtomicBoolean(false);
    private final Map<String, LocusProbabilities> probabilities = new HashMap<>();
    private long start;
    private long estimatedEndTime;
    private long totalSize = 0;
    private long expectedSize = 0;
    private final MyLong currentSize = new MyLong(0);
    private final HashMap<String, AtomicLong> locusCounts = new HashMap<>();
    private final HashMap<String, ArrayList<Long>> locusSizes = new HashMap<>();
    private final ArrayList<String> _intermediateResults = new ArrayList<>();
    private final Thread guiUpdater = new Thread() {
        private String formatTime(final long ms) {
            final int hours = Math.abs((int) ms / 3600000);
            final int minutes = Math.abs((int) ((ms / 1000 - hours * 3600) / 60));
            final int seconds = Math.abs((int) ((ms / 1000 - hours * 3600) % 60));
            return String.format("%1s%2$02d:%3$02d:%4$02d", ms < 0 ? "-" : "", hours, minutes, seconds);
        }

        @Override
        public void run() {
            try {
                while (true) {
                    if (totalSize > 0) {
                        final int promille = (int) ((currentSize.longValue() * 1000) / totalSize);
                        _session.setProgress(promille / 10);
                        final long processingTime = System.currentTimeMillis() - start;

                        if (estimatedEndTime != 0) {
                            gui.setTimeLeft(formatTime(estimatedEndTime - processingTime));
                        }
                        gui.setTimeSpent(formatTime(processingTime));
                        gui.setAnalysisProgress(promille);

                        final LocusProbabilities def = probabilities.get("Defense");
                        final LocusProbabilities pros = probabilities.get("Prosecution");

                        for (final String locus : def.getLoci()) {
                            final Double pp = pros.getLocusProbability(locus);
                            final Double pd = def.getLocusProbability(locus);

                            String decoration = "<font color=blue>Intermediate result: ";
                            if (locusCounts.get(locus) == null) {
                                decoration = "<b><font color=green>Final result: ";
                            }
                            if (pp != null && pd != null) {
                                gui.setLocusResult(locus, "<html>" + decoration + _session.formatNumber(pp / pd));
                            }
                        }
                    }
                    sleep(10);
                }
            } catch (final InterruptedException ex) {
            }
        }
    };
    private final ProgressGui gui;
    private final SessionData _session;

    /**
     *
     * @param session
     * @param gui
     */
    public AnalysisProgressListenerImpl(final SessionData session, final ProgressGui gui) {
        this.gui = gui;
        this._session = session;
        this.probabilities.put("Defense", new LocusProbabilities());
        this.probabilities.put("Prosecution", new LocusProbabilities());

    }

    private String getHostName() {
        try {
            return InetAddress.getLocalHost().getCanonicalHostName();
        } catch (final UnknownHostException ex) {
            return ex.getClass().getName() + " - " + ex.getMessage();
        }
    }

    @Override
    public void analysisStarted() {
        start = System.currentTimeMillis();
        estimatedEndTime = 0;
        _session.setApplicationState(ApplicationStateChangeListener.APP_STATE.ANALYSIS_RUNNING);
        initLogger();
        guiUpdater.start();
        initLogger();
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

        LOG.info("  Case number: {}", _session.getCaseNumber());
        LOG.info("  Analysis type: Likelihood Ratio Calculation");
        LOG.info("  Analysis started by {} on {}", System.getProperty("user.name"), getHostName());
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
        LOG.info("Loaded reference profiles:");
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

    private String addPadding(final String value, final int length) {
        final String retval = value + "                                        ";
        return retval.substring(value.startsWith("-") ? 1 : 0, length);
    }

    @Override
    public void analysisFinished(final LikelihoodRatio lr) {
        guiUpdater.interrupt();
        try {
            guiUpdater.join();
        } catch (final InterruptedException ex) {
            // Do nothing. We already know the thread was interrupted...
        }
        if (ApplicationSettings.isValidationMode()) {
            LOG.info("=================");
            LOG.info("Intermediate results:");
            Collections.sort(_intermediateResults);
            for (final String result : _intermediateResults) {
                LOG.info(result);
            }
        }

        double prEP = 1;
        double prED = 1;
        LOG.info("=========== Log10(LR) vs. Pr(D) ===========");
        LOG.info("  Locus      Pr(E|Hp)                       Pr(E|Hd)                       LR                             log10(LR)");
        LOG.info("  ---------------------------------------------------------------------------------------------------------------------------------");
        for (final String locus : _session.getEnabledLoci()) {
            for (final Ratio ratio : lr.getRatios()) {
                if (ratio.getLocusName().equalsIgnoreCase(locus)) {
                    LOG.info("  {} {} {} {} {}",
                            addPadding(locus, 10),
                            addPadding(ratio.getProsecutionProbability().toString(), 30),
                            addPadding(ratio.getDefenseProbability().toString(), 30),
                            addPadding(ratio.getRatio().toString(), 30),
                            Math.log10(ratio.getRatio()));
                    prEP *= ratio.getProsecutionProbability();
                    prED *= ratio.getDefenseProbability();
                    gui.setLocusResult(locus, _session.formatNumber(ratio.getRatio()));
                }
            }
        }
        LOG.info("  ---------------------------------------------------------------------------------------------------------------------------------");
        LOG.info("  {} {} {} {} {}", addPadding("Product", 10), addPadding("" + prEP, 30), addPadding("" + prED, 30), addPadding("" + lr.getOverallRatio().getRatio(), 30), Math.log10(lr.getOverallRatio().getRatio()));
        final Double overallRatio = lr.getOverallRatio().getRatio();
        gui.setOverallLikelyhoodRatio(_session.formatNumber(overallRatio));

        final long runningTime = System.currentTimeMillis() - start;
        final long replicatePerMs = totalSize / (runningTime + 1);
        LOG.info("=================");
        LOG.info("  Analysis Completed");
        LOG.info("  Running time: {} ms", runningTime);
        LOG.info("  Total number of calculations: {}", totalSize);
        LOG.info("  Average processing speed: {} operations per ms", replicatePerMs);
        _session.setStatusMessage("Analysis completed.");
        resetLogger();
        _session.setApplicationState(ApplicationStateChangeListener.APP_STATE.READY_FOR_ANALYSIS);

        if (overallRatio.isInfinite() || overallRatio.isNaN()) {
            String msg;
            msg = "<html>The analysis did not result in a numerical results.<br>"
                + "This may be caused by:<UL>"
                + "<LI>setting the dropout probability to 0.00 for a contributor that requires dropout in order to explain the evidence.</LI>"
                + "<LI>setting dropin to 0.00 when in reality dropin is required to explain the evidence.</LI></UL>";
            _session.setErrorMessage(msg);
        }
    }

    @Override
    public void hypothesisStarted(final Hypothesis hypothesis) {
    }

    @Override
    public void hypothesisFinished(final Hypothesis hypothesis, final LocusProbabilities probabilities) {
    }

    @Override
    public synchronized void locusStarted(final Hypothesis hypothesis, final String locusName, final long jobsize) {
        totalSize += jobsize;
        final AtomicLong count = locusCounts.get(locusName);
        if (count == null) {
            locusCounts.put(locusName, new AtomicLong(1));
        } else {
            count.incrementAndGet();
        }

        ArrayList<Long> curLocusSizes = locusSizes.get(locusName);
        if (curLocusSizes == null) {
            curLocusSizes = new ArrayList<>();
            locusSizes.put(locusName, curLocusSizes);
        }
        curLocusSizes.add(jobsize);
    }

    @Override
    public void locusFinished(final Hypothesis hypothesis, final String locusName, final Double locusProbability) {
        if (ApplicationSettings.isValidationMode()) {
            _intermediateResults.add(String.format("  %s = %s", addPadding(hypothesis.getId() + "." + locusName, 20), locusProbability));
        }
        final LocusProbabilities prob = probabilities.get(hypothesis.getId());
        Double current = prob.getLocusProbability(locusName);
        if (current == null) {
            current = locusProbability;
        } else {
            current += locusProbability;
        }
        prob.addLocusProbability(locusName, current);

        final AtomicLong count = locusCounts.get(locusName);
        if ((count != null) && count.decrementAndGet() == 0) {
            locusCounts.remove(locusName);
        }

        // A locus was finished. Work out where we should be regarding the current size
        final ArrayList<Long> curLocusSize = locusSizes.get(locusName);
        if (curLocusSize != null) {
            expectedSize += curLocusSize.remove(0);
        }

        if (expectedSize > currentSize.longValue()) {
            currentSize.set(expectedSize);
        }

        // If the current locus was finished, update the estimated time left (not always because this causes a VERY jittery estimate value)
        if (count == null || count.intValue() == 0) {
            final long runningTime = System.currentTimeMillis() - start;
            final long replicatePerMs = currentSize.longValue() / (runningTime + 1);
            if (replicatePerMs > 0) {
                estimatedEndTime = totalSize / replicatePerMs;
            } else {
                estimatedEndTime = System.currentTimeMillis();
            }
        }
    }

    @Override
    public void analysisFinished(final Throwable e) {
        final boolean logged = summaryLogged.getAndSet(true);
        if (!logged) {
            if (ApplicationSettings.isValidationMode()) {
                LOG.info("=================");
                LOG.info("Intermediate results:");
                Collections.sort(_intermediateResults);
                for (final String result : _intermediateResults) {
                    LOG.info(result);
                }
            }

            boolean logHeader = true;
            LOG.info("=========== Log10(LR) vs. Pr(D) ===========");
            LOG.info("  Analysis was interrupted before all calculations were completed.");
            LOG.info("  Listed below are the loci for which calculation were completed.");
            LOG.info("");
            for (final String locusName : _session.getEnabledLoci()) {
                // Only log final results
                if (locusCounts.get(locusName) == null) {
                    if (logHeader) {
                        LOG.info("  Locus      Pr(E|Hp)                       Pr(E|Hd)                       LR                             log10(LR)");
                        LOG.info("  ---------------------------------------------------------------------------------------------------------------------------------");
                        logHeader = false;
                    }
                    final double defenseProbability = probabilities.get("Defense").getLocusProbability(locusName);
                    final double prosecutionProbability = probabilities.get("Prosecution").getLocusProbability(locusName);
                    final double ratio = prosecutionProbability / defenseProbability;
                    LOG.info("  {} {} {} {} {}",
                            addPadding(locusName, 10),
                            addPadding("" + prosecutionProbability, 30),
                            addPadding("" + defenseProbability, 30),
                            addPadding("" + ratio, 30),
                            Math.log10(ratio));
                }
            }
            if (logHeader) {
                LOG.info("  Calculations were not completed for any locus!");
            }
            LOG.info("=================");
            if (e instanceof InterruptedException) {
                LOG.error("Analysis interrupted!");
            } else {
                LOG.error("Analysis finished with exception!", e);
                _session.setErrorMessage("Analysis failed: " + e.getMessage());
            }

            final long runningTime = System.currentTimeMillis() - start;
            final long replicatePerMs = currentSize.longValue() / (runningTime + 1);
            LOG.info("Running time: {} ms", runningTime);
            LOG.info("Total number of calculations:    {}", totalSize);
            LOG.info("Executed number of calculations: {}", currentSize.longValue());
            LOG.info("Average processing speed: {} operations per ms", replicatePerMs);
            resetLogger();
            guiUpdater.interrupt();
            _session.setApplicationState(ApplicationStateChangeListener.APP_STATE.READY_FOR_ANALYSIS);
        }
    }

    private void logHypothesis(final Hypothesis hypothesis) {
        LOG.info("=================");
        LOG.info("Hypothesis {}", hypothesis.getId());
        LOG.info("  Contributors {}", hypothesis.getContributors());
        LOG.info("  Non-Contributors {}", hypothesis.getNonContributors());
        LOG.info("  Unknowns {}", hypothesis.getUnknownCount());
        if (hypothesis.getRelatedness().getRelation() == Relation.NONE) {
            LOG.info("  No related unknown contributor");
        } else {
            LOG.info("  Related unknown contributor: {} of {}", hypothesis.getRelatedness().getRelation(), hypothesis.getRelatedness().getRelative().getId());
        }
        LOG.info("  Unknown Dropout {}", new BigDecimal(hypothesis.getUnknownDropoutProbability()).setScale(2, RoundingMode.HALF_UP));
        LOG.info("  Dropin {}", new BigDecimal(hypothesis.getDropInProbability()).setScale(2, RoundingMode.HALF_UP));
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
                    + "Tr-" + getTraceIDs() + "-"
                    + describeHypothesis(_session.getProsecution()) + "-"
                    + describeHypothesis(_session.getDefense()) + "-"
                    + sdf.format(new Date()) + ".log";

            _session.getCurrentReport().setLogfileName(logFileName);
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
            if (hypo.getRelatedness().getRelation() != Relation.NONE) {
                sb.append("-").append(hypo.getRelatedness().toString().replaceAll("(.{4}).* of ", "$1-"));
            }
        }
        return sb.toString();
    }
}
