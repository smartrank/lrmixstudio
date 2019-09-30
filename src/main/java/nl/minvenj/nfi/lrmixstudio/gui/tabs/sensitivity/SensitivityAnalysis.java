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

import static nl.minvenj.nfi.lrmixstudio.model.SensitivityAnalysisResults.RangeType.LR;
import static nl.minvenj.nfi.lrmixstudio.model.SensitivityAnalysisResults.RangeType.P_DEFENSE;
import static nl.minvenj.nfi.lrmixstudio.model.SensitivityAnalysisResults.RangeType.P_PROSECUTION;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.RejectedExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.minvenj.nfi.lrmixstudio.domain.Contributor;
import nl.minvenj.nfi.lrmixstudio.domain.Hypothesis;
import nl.minvenj.nfi.lrmixstudio.domain.LikelihoodRatio;
import nl.minvenj.nfi.lrmixstudio.domain.Ratio;
import nl.minvenj.nfi.lrmixstudio.domain.Sample;
import nl.minvenj.nfi.lrmixstudio.gui.ApplicationStateChangeListener;
import nl.minvenj.nfi.lrmixstudio.gui.SessionData;
import nl.minvenj.nfi.lrmixstudio.model.AnalysisReportImpl;
import nl.minvenj.nfi.lrmixstudio.model.LRMathModel;
import nl.minvenj.nfi.lrmixstudio.model.LRMathModelFactory;
import nl.minvenj.nfi.lrmixstudio.model.SensitivityAnalysisResults.Point;

public class SensitivityAnalysis extends Thread {

    private static final Logger LOG = LoggerFactory.getLogger(SensitivityAnalysis.class);
    private final Collection<Sample> personsOfInterest;
    private final SensitivityAnalysisProgressListener progress;
    private final SessionData session;
    private final BigDecimal dropoutFrom;
    private final BigDecimal dropoutTo;
    private final BigDecimal _dropoutSteps;
    private final boolean varyDefenseUnknowns;
    private final boolean varyProsecutionUnknowns;
    private final BigDecimal dropin;
    private final BigDecimal theta;
    private final AnalysisReportImpl masterReport;
    private String targetLocus = null;
    private LRMathModel mathematicalModel = null;

    public SensitivityAnalysis(final SessionData session, final String targetLocus, final Collection<Sample> personsOfInterest, final BigDecimal dropoutFrom, final BigDecimal dropoutTo, final BigDecimal dropoutSteps, final BigDecimal dropin, final BigDecimal theta, final SensitivityAnalysisProgressListener sensitivityAnalysisProgressListener, final boolean varyDefenseUnknowns, final boolean varyProsecutionUnknowns) {
        this.masterReport = (AnalysisReportImpl) session.getCurrentReport();
        this.session = new SessionData(session);
        final Collection<String> enabledLoci = this.session.getEnabledLoci();
        if (enabledLoci.contains(targetLocus)) {
            this.targetLocus = targetLocus;
            for (final String locus : enabledLoci) {
                this.session.setLocusEnabled(locus, locus.equalsIgnoreCase(targetLocus));
            }
        }

        this.personsOfInterest = personsOfInterest;
        this.progress = sensitivityAnalysisProgressListener;
        this.dropoutFrom = dropoutFrom;
        this.dropoutTo = dropoutTo;
        _dropoutSteps = dropoutSteps;
        this.dropin = dropin;
        this.theta = theta;
        this.varyDefenseUnknowns = varyDefenseUnknowns;
        this.varyProsecutionUnknowns = varyProsecutionUnknowns;
    }

    @Override
    public void interrupt() {
        if (mathematicalModel != null) {
            mathematicalModel.interrupt();
        }
        super.interrupt();
    }

    @Override
    public void run() {
        session.setApplicationState(ApplicationStateChangeListener.APP_STATE.SENSITIVITY_ANALYSIS_RUNNING);
        masterReport.analysisStarted();
        try {
            final ArrayList<Point> lrPoints = new ArrayList<>();
            final ArrayList<Point> defenceProbabilityPoints = new ArrayList<>();
            final ArrayList<Point> prosecutionProbabilityPoints = new ArrayList<>();
            progress.setPoints(lrPoints);

            final int steps = _dropoutSteps.intValue();
            int step = 0;
            BigDecimal stepSize = dropoutTo.subtract(dropoutFrom);
            stepSize = stepSize.divide(_dropoutSteps, new MathContext(2, RoundingMode.HALF_UP)).setScale(2, RoundingMode.HALF_UP);
            LOG.info("Stepsize = {}", stepSize);
            if (stepSize.round(new MathContext(2, RoundingMode.HALF_UP)).compareTo(new BigDecimal("0.01")) < 0) {
                stepSize = new BigDecimal("0.01");
            }

            boolean workback = false;

            for (BigDecimal dropout = dropoutFrom; dropout.compareTo(dropoutTo) <= 0; dropout = dropout.add(stepSize).stripTrailingZeros()) {
                for (final Sample sample : personsOfInterest) {
                    session.getDefense().getContributor(sample).setDropoutProbability(dropout.doubleValue());
                    session.getProsecution().getContributor(sample).setDropoutProbability(dropout.doubleValue());
                }

                if (varyDefenseUnknowns) {
                    session.getDefense().setUnknownDropoutProbability(dropout.doubleValue());
                }
                if (varyProsecutionUnknowns) {
                    session.getProsecution().setUnknownDropoutProbability(dropout.doubleValue());
                }

                session.getProsecution().setDropInProbability(dropin.doubleValue());
                session.getProsecution().setThetaCorrection(theta.doubleValue());
                session.getDefense().setDropInProbability(dropin.doubleValue());
                session.getDefense().setThetaCorrection(theta.doubleValue());

                progress.setIteration(step++, steps);
                mathematicalModel = LRMathModelFactory.getMathematicalModel(session.getMathematicalModelName());
                mathematicalModel.addProgressListener(progress);
                final LikelihoodRatio lr = mathematicalModel.doAnalysis(session);
                masterReport.addProcessingTime(session.getCurrentReport().getProcessingTime());
                progress.setResult(dropout, lr);

                final Ratio overallRatio = lr.getOverallRatio();
                if (overallRatio.getRatio() != 0 && !overallRatio.getRatio().isInfinite() && !overallRatio.getRatio().isNaN()) {
                    lrPoints.add(new Point(null, dropout, new BigDecimal(Math.log10(overallRatio.getRatio()), new MathContext(7, RoundingMode.HALF_UP))));
                    progress.setPoints(lrPoints);
                }
                else {
                    // If we reach this point, the LR is a non-representable value (NaN, Infinity or 0) so alter the value of dropout by 0.01 and retry.
                    // This to avoid a gap at the start or end of the graph (or at least make the gap as small as possible).
                    if (dropout.compareTo(dropoutTo) == 0) {
                        workback = true;
                    }
                    if (workback) {
                        if (dropout.equals(dropoutTo.subtract(stepSize))) {
                            break;
                        }
                        // We are at the end of the graph. slowly move back until we get a representable result or we encounter the last 'proper' step value
                        dropout = dropout.subtract(new BigDecimal("0.01")).subtract(stepSize);
                    }
                    else {
                        // We are at the start of the graph. Slowly move forward until we get a representable result
                        dropout = dropout.add(new BigDecimal("0.01")).subtract(stepSize);
                    }
                    continue;
                }

                BigDecimal defenseProbability = BigDecimal.ONE;
                BigDecimal prosecutionProbability = BigDecimal.ONE;

                for (final Ratio ratio : lr.getRatios()) {
                    defenseProbability = defenseProbability.multiply(new BigDecimal(ratio.getDefenseProbability()));
                    prosecutionProbability = prosecutionProbability.multiply(new BigDecimal(ratio.getProsecutionProbability()));
                }
                try {
                    defenseProbability = new BigDecimal(Math.log10(defenseProbability.doubleValue()));
                    prosecutionProbability = new BigDecimal(Math.log10(prosecutionProbability.doubleValue()));
                    defenceProbabilityPoints.add(new Point(null, dropout, defenseProbability));
                    prosecutionProbabilityPoints.add(new Point(null, dropout, prosecutionProbability));
                } catch (final NumberFormatException nfe) {
                    LOG.debug("Ignoring sensitivity graph at dropout {} because the defence probability = {} and prosecution probability is {}", dropout, defenceProbabilityPoints, prosecutionProbability);
                }

                // If the next dropout value would exceed the upper limit (and we are not currently at this limit) then ensure that the next
                // iteration will use the upper limit for dropout, thereby avoiding a gap at the end of the graph
                if (dropout.add(stepSize).compareTo(dropoutTo) > 0 && dropout.compareTo(dropoutTo) < 0) {
                    dropout = dropoutTo.subtract(stepSize);
                }
            }

            final StringBuilder rangeName = new StringBuilder(" varying dropout of ");
            final StringBuilder contributorNames = new StringBuilder();
            final Iterator<Sample> poiIter = personsOfInterest.iterator();
            while (poiIter.hasNext()) {
                final Sample sample = poiIter.next();
                contributorNames.append(" ").append(sample.getId());
            }
            if (varyDefenseUnknowns && varyProsecutionUnknowns) {
                contributorNames.append(" all_unknowns");
            } else {
                if (varyDefenseUnknowns) {
                    contributorNames.append(" defense_unknowns");
                }
                if (varyProsecutionUnknowns) {
                    contributorNames.append(" prosecution_unknowns");
                }
            }

            String contributorNameString = contributorNames.toString().trim();
            contributorNameString = contributorNameString.replaceAll(" ", "\\, ");
            contributorNameString = contributorNameString.replaceAll("_", " ");
            final int lastIndex = contributorNameString.lastIndexOf(",");
            if (lastIndex >= 0) {
                contributorNameString = contributorNameString.substring(0, lastIndex) + " and" + contributorNameString.substring(lastIndex + 1);
            }

            if (lrPoints.isEmpty()) {
                final Hypothesis hp = session.getProsecution();
                final Hypothesis hd = session.getDefense();
                final ArrayList<String> samplesPossiblyCausingInfinity = new ArrayList<>();
                for (final Contributor c : hp.getContributors()) {
                    final String id = c.getSample().getId();
                    if (c.getDropoutProbability() == 0.0 && !contributorNameString.contains(id) && !samplesPossiblyCausingInfinity.contains(id)) {
                        samplesPossiblyCausingInfinity.add(id);
                    }
                }
                for (final Contributor c : hd.getContributors()) {
                    final String id = c.getSample().getId();
                    if (c.getDropoutProbability() == 0.0 && !contributorNameString.contains(id) && !samplesPossiblyCausingInfinity.contains(id)) {
                        samplesPossiblyCausingInfinity.add(id);
                    }
                }

                String msg;
                if (samplesPossiblyCausingInfinity.size() > 0) {
                    msg = "<html>The Sensitivity Analysis did not result in any numerical results.<br>"
                        + "This is usually caused by setting the dropout probability to 0.00 for a contributor that requires dropout in order to explain the evidence.<br>"
                        + "You may want to check the dropout probability of <b>" + samplesPossiblyCausingInfinity.toString().replaceAll("[\\[\\]]", "").replaceAll(",", "</b>\\, <b>");
                    final int lastComma = msg.lastIndexOf(",");
                    if (lastComma >= 0) {
                        msg = msg.substring(0, lastComma) + " and" + msg.substring(lastComma + 1);
                    }
                    msg += "</b>.";
                }
                else {
                    msg = "<html>The Sensitivity Analysis did not result in any numerical results.<br>All LRs were either 0, Infinity or not representable as a number.<br>Please check your hypotheses.";
                }

                throw new Exception(msg);
            }

            rangeName.append(contributorNameString);
            rangeName.append(".");
            if (targetLocus != null) {
                rangeName.append(" Locus ").append(targetLocus).append(".");
            }
            else {
                rangeName.append(" All Loci.");
            }

            String dropInString = dropin.toPlainString();
            if (dropInString.length() > 4) {
                dropInString = dropInString.substring(0, 4);
            }
            rangeName.append(" DropIn ").append(dropInString).append(".");

            String thetaString = theta.toPlainString();
            if (thetaString.length() > 4) {
                thetaString = thetaString.substring(0, 4);
            }
            rangeName.append(" Theta ").append(thetaString).append(".");

            masterReport.getSensitivityAnalysisResults().addRange(LR, "Log10(LR)" + rangeName.toString(), lrPoints);
            masterReport.getSensitivityAnalysisResults().addRange(P_PROSECUTION, "log10(Pr(E|Hp))" + rangeName.toString(), prosecutionProbabilityPoints);
            masterReport.getSensitivityAnalysisResults().addRange(P_DEFENSE, "log10(Pr(E|Hd))" + rangeName.toString(), defenceProbabilityPoints);
            masterReport.analysisFinished(masterReport.getLikelihoodRatio());
            progress.analysisFinished();
        } catch (final Exception ex) {
            if (ex instanceof InterruptedException || ex instanceof RejectedExecutionException) {
                if (mathematicalModel != null) {
                    mathematicalModel.interrupt();
                }
                LOG.info("Sensitivity Analysis Interrupted!");
            } else {
                LOG.error("Error in Sensitivity Analysis", ex);
            }
            progress.analysisFinished(ex);
        } finally {
            session.setApplicationState(ApplicationStateChangeListener.APP_STATE.READY_FOR_ANALYSIS);
        }
    }
}
