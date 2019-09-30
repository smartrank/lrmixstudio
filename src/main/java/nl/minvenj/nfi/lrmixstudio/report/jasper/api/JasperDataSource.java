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
package nl.minvenj.nfi.lrmixstudio.report.jasper.api;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import nl.minvenj.nfi.lrmixstudio.domain.Locus;
import nl.minvenj.nfi.lrmixstudio.domain.Ratio;
import nl.minvenj.nfi.lrmixstudio.domain.Relatedness;
import nl.minvenj.nfi.lrmixstudio.domain.Sample;
import nl.minvenj.nfi.lrmixstudio.gui.SessionData;
import nl.minvenj.nfi.lrmixstudio.model.AnalysisReport;
import nl.minvenj.nfi.lrmixstudio.model.DropoutEstimation;
import nl.minvenj.nfi.lrmixstudio.model.NonContributorTestResults;
import nl.minvenj.nfi.lrmixstudio.model.SensitivityAnalysisResults;
import nl.minvenj.nfi.lrmixstudio.model.SensitivityAnalysisResults.Point;
import nl.minvenj.nfi.lrmixstudio.model.SensitivityAnalysisResults.Range;

public class JasperDataSource implements JRDataSource {

    private static final Logger LOG = LoggerFactory.getLogger(JasperDataSource.class);
    public static final String LOGO = "Logo";
    public static final String RATIOS = "Ratios";
    public static final String OVERALL_RATIO = "OverallRatio";
    public static final String PROSECUTION_UNKNOWNS = "ProsecutionUnknowns";
    public static final String PROSECUTION_CONTRIBUTORS = "ProsecutionContributors";
    public static final String DEFENSE_CONTRIBUTORS = "DefenseContributors";
    public static final String DEFENSE_UNKNOWNS = "DefenseUnknowns";
    public static final String CASE_NUMBER = "CaseNumber";
    public static final String USER_NAME = "UserName";
    public static final String DATE_TIME = "DateTime";
    public static final String PROGRAM_VERSION = "ProgramVersion";
    public static final String THETA_CORRECTION = "ThetaCorrection";
    public static final String DROP_IN_PROBABILITY = "DropInProbability";
    public static final String PROSECUTION_UNKNOWNS_DROPOUT_PROBABILITY = "ProsecutionUnknownsDropoutProbability";
    public static final String DEFENSE_UNKNOWNS_DROPOUT_PROBABILITY = "DefenseUnknownsDropoutProbability";
    public static final String SENSITIVITY_ANALYSIS_RESULTS = "SensitivityAnalysisResults";
    public static final String SENSITIVITY_ANALYSIS_GRAPH = "SensitivityAnalysisGraph";
    public static final String POPULATION_STATISTICS = "PopulationStatistics";
    public static final String POPULATION_STATISTICS_FILE_NAME = "PopulationStatisticsFileName";
    public static final String REPLICATELOCI = "ReplicateLoci";
    public static final String REFERENCEPROFILELOCI = "ReferenceProfileLoci";
    public static final String REFERENCEPROFILESNOTES = "ReferenceProfilesNotes";
    public static final String DISABLED_LOCI = "DisabledLoci";
    public static final String REPLICATES = "Replicates";
    public static final String DISABLED_REPLICATES = "DisabledReplicates";
    public static final String DISABLED_REFERENCEPROFILES = "DisabledReferenceProfiles";
    public static final String PROFILES = "ReferenceProfiles";
    public static final String NONCONTRIBUTOR_TEST_RESULTS = "NonContributorTestResults";
    public static final String NONCONTRIBUTOR_TEST_GRAPH = "NonContributorTestGraph";
    public static final String DROPOUT_ESTIMATION = "DropoutEstimation";
    public static final String RARE_ALLELES = "RareAlleles";
    public static final String RARE_ALLELE_FREQUENCY = "RareAlleleFrequency";
    public static final String REMARKS = "Remarks";
    public static final String RELATEDNESS = "Relatedness";
    public static final String TRACEIDS = "TraceIDs";
    private final Collection<AnalysisReport> _reports;
    private final Iterator<AnalysisReport> iterator;
    private AnalysisReport currentReport;
    private final SessionData _session;
    private final String _remarks;

    public JasperDataSource(final SessionData session, final String remarks, final Collection<AnalysisReport> reports) {
        _reports = reports;
        _remarks = remarks;
        iterator = _reports.iterator();
        _session = session;
    }

    @Override
    public boolean next() throws JRException {
        if (iterator.hasNext()) {
            currentReport = iterator.next();
            return true;
        }
        return false;
    }

    private static final ArrayList<JRField> fields = new ArrayList<>();

    {
        fields.add(new JasperField(LOGO, "Organisation Logo", InputStream.class));
        fields.add(new JasperField(CASE_NUMBER, "The case number", String.class));
        fields.add(new JasperField(USER_NAME, "The name of the user running the analysis", String.class));
        fields.add(new JasperField(DATE_TIME, "The date and time when the analysis was run", String.class));
        fields.add(new JasperField(PROGRAM_VERSION, "The version of LRmixStudio used to generate the report", String.class));

        fields.add(new JasperField(REPLICATES, "The replicates", Collection.class));
        fields.add(new JasperField(DISABLED_REPLICATES, "Replicates that are present in the input files but disabled for this analysis", Collection.class));
        fields.add(new JasperField(PROFILES, "The reference profiles", Collection.class));
        fields.add(new JasperField(DISABLED_REFERENCEPROFILES, "Reference profiles that are present in the input files but disabled for this analysis", Collection.class));

        fields.add(new JasperField(THETA_CORRECTION, "The value for the Theta correction", Double.class));
        fields.add(new JasperField(DROP_IN_PROBABILITY, "The probability of Drop-In", Double.class));
        fields.add(new JasperField(POPULATION_STATISTICS_FILE_NAME, "The file from which the population statistics (a.k.a. the Allele Frequencies) were read.", String.class));
        fields.add(new JasperField(RARE_ALLELES, "The alleles that were detected as rare", Collection.class));
        fields.add(new JasperField(RARE_ALLELE_FREQUENCY, "The frequency assigned to the alleles that were detected as rare", String.class));

        fields.add(new JasperField(REPLICATELOCI, "The contents of the replicates", Collection.class));
        fields.add(new JasperField(REFERENCEPROFILELOCI, "The contents of the reference profiles", Collection.class));
        fields.add(new JasperField(REFERENCEPROFILESNOTES, "Any remarks concerning the reference profiles", Collection.class));
        fields.add(new JasperField(DISABLED_LOCI, "The loci that were disabled", Collection.class));

        fields.add(new JasperField(PROSECUTION_UNKNOWNS, "The number of unknowns in the prosecution hypothesis", Integer.class));
        fields.add(new JasperField(PROSECUTION_CONTRIBUTORS, "The contributors according to the prosecution hypothesis", Collection.class));
        fields.add(new JasperField(PROSECUTION_UNKNOWNS_DROPOUT_PROBABILITY, "The probability of Drop-Out for the unknowns according to the prosecution hypothesis", Double.class));

        fields.add(new JasperField(DEFENSE_UNKNOWNS, "The number of unknowns in the defense hypothesis", Integer.class));
        fields.add(new JasperField(DEFENSE_CONTRIBUTORS, "The contributors according to the defense hypothesis", Collection.class));
        fields.add(new JasperField(DEFENSE_UNKNOWNS_DROPOUT_PROBABILITY, "The probability of Drop-Out for the unknowns according to the defense hypothesis", Double.class));

        fields.add(new JasperField(RATIOS, "The Likelihood Ratios", Collection.class));
        fields.add(new JasperField(OVERALL_RATIO, "The overall Likelihood Ratio over all loci", BigDecimal.class));
        fields.add(new JasperField(SENSITIVITY_ANALYSIS_RESULTS, "The results of the sensitivity analysis", Collection.class));
        fields.add(new JasperField(SENSITIVITY_ANALYSIS_GRAPH, "The results of the sensitivity analysis as an image", InputStream.class));
        fields.add(new JasperField(NONCONTRIBUTOR_TEST_RESULTS, "The results of the non-contributor test", NonContributorTestResults.class));
        fields.add(new JasperField(DROPOUT_ESTIMATION, "The results of the dropout estimation", DropoutEstimation.class));

        fields.add(new JasperField(REMARKS, "Any remarks that the reporting officer chooses to add to the report", String.class));
        fields.add(new JasperField(TRACEIDS, "A string describing the trace or traces under analysis", String.class));
        fields.add(new JasperField(RELATEDNESS, "A description of the relation between one unknown and a known profile", Relatedness.class));
    }

    @Override
    public Object getFieldValue(final JRField jrf) throws JRException {
        switch (jrf.getName()) {
            case LOGO:
                try {
                    return new FileInputStream("report/logo.png");
                } catch (final FileNotFoundException ex) {
                    return null;
                }
            case USER_NAME:
                return System.getProperty("user.name");
            case DATE_TIME:
                final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                return sdf.format(new Date(currentReport.getStartTime()));
            case REMARKS:
                return _remarks;
            case RELATEDNESS:
                return currentReport.getDefenseHypothesis().getRelatedness();
            case RATIOS:
                if (currentReport.getLikelihoodRatio() == null) {
                    return null;
                }
                final ArrayList<Ratio> orderedResults = new ArrayList<>();
                for (final String locus : currentReport.getEnabledLoci()) {
                    orderedResults.add(currentReport.getLikelihoodRatio().getRatio(locus));
                }
                return orderedResults;
            case OVERALL_RATIO:
                if (currentReport.getLikelihoodRatio() == null) {
                    return null;
                }
                final Double ratio = currentReport.getLikelihoodRatio().getOverallRatio().getRatio();
                if (ratio.isInfinite() || ratio.isNaN()) {
                    return null;
                }
                return new BigDecimal(ratio);
            case PROSECUTION_UNKNOWNS:
                return currentReport.getProsecutionHypothesis().getUnknownCount();
            case PROSECUTION_CONTRIBUTORS:
                return currentReport.getProsecutionHypothesis().getContributors();
            case DEFENSE_CONTRIBUTORS:
                return currentReport.getDefenseHypothesis().getContributors();
            case DEFENSE_UNKNOWNS:
                return currentReport.getDefenseHypothesis().getUnknownCount();
            case CASE_NUMBER:
                return currentReport.getCaseNumber();
            case PROGRAM_VERSION:
                return currentReport.getProgramVersion();
            case THETA_CORRECTION:
                return currentReport.getProsecutionHypothesis().getThetaCorrection();
            case DROP_IN_PROBABILITY:
                return currentReport.getProsecutionHypothesis().getDropInProbability();
            case PROSECUTION_UNKNOWNS_DROPOUT_PROBABILITY:
                return currentReport.getProsecutionHypothesis().getUnknownDropoutProbability();
            case DEFENSE_UNKNOWNS_DROPOUT_PROBABILITY:
                return currentReport.getDefenseHypothesis().getUnknownDropoutProbability();
            case SENSITIVITY_ANALYSIS_RESULTS:
                final ArrayList<SensitivityAnalysisResults.Point> points = new ArrayList<>();
                for (final Range range : currentReport.getSensitivityAnalysisResults().getRanges()) {
                    trimPointsByX(range, points);
                }
                return points;
            case SENSITIVITY_ANALYSIS_GRAPH:
                try {
                    if (currentReport.getSensitivityAnalysisResults().getGraphImage() == null) {
                        return new FileInputStream("report/logo.png");
                    }
                    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(currentReport.getSensitivityAnalysisResults().getGraphImage(), "png", baos);
                    baos.flush();
                    final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                    baos.close();
                    return bais;
                } catch (final Exception e) {
                    LOG.error("Error getting graph for " + SENSITIVITY_ANALYSIS_GRAPH + ": " + e);
                    return null;
                }
            case POPULATION_STATISTICS:
                return currentReport.getPopulationStatistics();
            case POPULATION_STATISTICS_FILE_NAME:
                return currentReport.getPopulationStatistics().getFileName();
            case RARE_ALLELES:
                return currentReport.getRareAlleles();
            case RARE_ALLELE_FREQUENCY:
                return currentReport.getRareAlleleFrequency();
            case REPLICATELOCI:
                final ArrayList<Locus> replicateLoci = new ArrayList<>();
                for (final String locusName : currentReport.getEnabledLoci()) {
                    for (final Sample sample : currentReport.getReplicates()) {
                        final Locus locus = sample.getLocus(locusName);
                        if (locus != null) {
                            replicateLoci.add(locus);
                        }
                    }
                }
                return replicateLoci;
            case REFERENCEPROFILELOCI:
                final ArrayList<Locus> referenceProfileLoci = new ArrayList<>();
                for (final String locusName : currentReport.getEnabledLoci()) {
                    for (final Sample sample : currentReport.getProfiles()) {
                        final Locus locus = sample.getLocus(locusName);
                        if (locus != null) {
                            referenceProfileLoci.add(locus);
                        }
                    }
                }
                return referenceProfileLoci;
            case REFERENCEPROFILESNOTES:
                return composeReferenceProfileNotes();
            case REPLICATES:
                return currentReport.getReplicates();
            case DISABLED_REPLICATES:
                final ArrayList<Sample> disabledReplicates = new ArrayList<>(_session.getAllReplicates());
                for (final Sample sample : currentReport.getReplicates()) {
                    disabledReplicates.remove(sample);
                }
                return disabledReplicates;
            case DISABLED_REFERENCEPROFILES:
                final ArrayList<Sample> disabledReferenceProfiles = new ArrayList<>(_session.getAllProfiles());
                for (final Sample sample : currentReport.getProfiles()) {
                    disabledReferenceProfiles.remove(sample);
                }
                return disabledReferenceProfiles;
            case DISABLED_LOCI:
                return currentReport.getDisabledLoci();
            case PROFILES:
                return currentReport.getProfiles();
            case DROPOUT_ESTIMATION:
                return currentReport.getSensitivityAnalysisResults().getDropoutEstimation();
            case NONCONTRIBUTOR_TEST_GRAPH:
                try {
                    if (currentReport.getNonContributorTestResults() == null || currentReport.getNonContributorTestResults().getGraphImage() == null) {
                        return null;
                    }
                    ByteArrayInputStream bais = null;
                    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                        final NonContributorTestResults nonContributorTestResults = currentReport.getNonContributorTestResults();
                        if (nonContributorTestResults != null) {
                            ImageIO.write(nonContributorTestResults.getGraphImage(), "png", baos);
                            baos.flush();
                            bais = new ByteArrayInputStream(baos.toByteArray());
                        }
                    }
                    return bais;
                } catch (final Exception e) {
                    LOG.error("Error getting graph for " + NONCONTRIBUTOR_TEST_GRAPH + ": " + e, e);
                    return null;
                }
            case NONCONTRIBUTOR_TEST_RESULTS:
                return currentReport.getNonContributorTestResults();
            case TRACEIDS:
                String traceIDs = "";
                for (final Sample replicate : currentReport.getReplicates()) {
                    final String repId = replicate.getId().replaceAll("Rep\\d+$", "");
                    if (!traceIDs.contains(repId)) {
                        if (!traceIDs.isEmpty()) {
                            traceIDs += ", ";
                        }
                        traceIDs += repId;
                    }
                }
                return traceIDs;
            default:
                LOG.error("Unknown data element: {}", jrf.getName());
                System.out.println("Unknown data element: " + jrf.getName());
                return null;
        }
    }

    /**
     * @return
     */
    private Object composeReferenceProfileNotes() {
        final ArrayList<String> notes = new ArrayList<>();

        final Collection<String> enabledLoci = currentReport.getEnabledLoci();
        for (final Sample s : currentReport.getProfiles()) {
            final ArrayList<String> loci = new ArrayList<>();
            for (final Locus locus : s.getLoci()) {
                if (enabledLoci.contains(locus.getName()) && (locus.isTreatedAsHomozygote())) {
                    loci.add(locus.getName());
                }
            }
            if (loci.size() > 0) {
                notes.add(s.getId() + " loci converted to homozygotic: " + loci.toString().replace("[\\[\\]]", ""));
            }
        }

        return notes;
    }

    /**
     * Removes those points that are not on 0.1 intervals
     *
     * @param range The range containing the points
     * @param points The collection of points that will be reported in the
     * output
     */
    private void trimPointsByX(final Range range, final ArrayList<Point> points) {
        final List<Point> rangePoints = range.getPoints();
        for (int idx = 0; idx < range.getPoints().size(); idx++) {
            final Point point = rangePoints.get(idx);
            if (idx == 0 || idx == rangePoints.size() - 1 || point.getX().remainder(new BigDecimal("0.1")).compareTo(BigDecimal.ZERO) == 0) {
                points.add(point);
            }
        }
    }

    public static JRField[] getFields() {
        return fields.toArray(new JRField[fields.size()]);
    }
}
