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

import static nl.minvenj.nfi.lrmixstudio.model.SensitivityAnalysisResults.RangeType.LR;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRDataSourceProvider;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JasperReport;
import nl.minvenj.nfi.lrmixstudio.domain.Allele;
import nl.minvenj.nfi.lrmixstudio.domain.Hypothesis;
import nl.minvenj.nfi.lrmixstudio.domain.LikelihoodRatio;
import nl.minvenj.nfi.lrmixstudio.domain.Locus;
import nl.minvenj.nfi.lrmixstudio.domain.LocusProbabilities;
import nl.minvenj.nfi.lrmixstudio.domain.PopulationStatistics;
import nl.minvenj.nfi.lrmixstudio.domain.Sample;
import nl.minvenj.nfi.lrmixstudio.gui.SessionData;
import nl.minvenj.nfi.lrmixstudio.io.PopulationStatisticsReader;
import nl.minvenj.nfi.lrmixstudio.io.SampleReader;
import nl.minvenj.nfi.lrmixstudio.model.AnalysisReport;
import nl.minvenj.nfi.lrmixstudio.model.DropoutEstimation;
import nl.minvenj.nfi.lrmixstudio.model.NonContributorTestResults;
import nl.minvenj.nfi.lrmixstudio.model.SensitivityAnalysisResults;

public class JasperDataSourceProvider implements JRDataSourceProvider {

    public JasperDataSourceProvider() {
        System.out.println("JasperDataSourceProvider()");
    }

    @Override
    public boolean supportsGetFieldsOperation() {
        return true;
    }

    @Override
    public JRDataSource create(final JasperReport jr) throws JRException {
        System.out.println("JRDataSource.create(" + jr.getName() + ")");
        final ArrayList<AnalysisReport> list = new ArrayList<>();
        list.add(new AnalysisReport() {
            private Hypothesis defense;
            private Hypothesis prosecution;
            private Collection<Sample> replicates;
            private Collection<Sample> profiles;
            private PopulationStatistics populationStatistics;

            {
                try {
                    populationStatistics = new PopulationStatisticsReader("Dummy Population Statistics", JasperDataSourceProvider.class.getResourceAsStream("DummyPopulationStatistics.csv")).getStatistics();
                    profiles = new SampleReader("DummyProfiles.csv", JasperDataSourceProvider.class.getResourceAsStream("DummyProfiles.csv"), false).getSamples();
                    replicates = new SampleReader("DummyReplicates.csv", JasperDataSourceProvider.class.getResourceAsStream("DummyReplicates.csv"), true).getSamples();

                    defense = new Hypothesis("DefenseHypothesis", populationStatistics);
                    defense.setDropInProbability(0.05);
                    defense.setThetaCorrection(0.1);
                    defense.setUnknownCount(1);
                    defense.setUnknownDropoutProbability(0.1);

                    prosecution = new Hypothesis("ProsecutionHypothesis", populationStatistics);
                    prosecution.setDropInProbability(0.05);
                    prosecution.setThetaCorrection(0.1);
                    prosecution.setUnknownCount(0);
                    prosecution.setUnknownDropoutProbability(0.1);

                    boolean first = true;
                    for (final Sample sample : profiles) {
                        if (first) {
                            defense.addNonContributor(sample, 0.1);
                            first = false;
                        } else {
                            defense.addContributor(sample, 0.1);
                        }
                        prosecution.addContributor(sample, 0.1);
                    }
                } catch (final Exception ex) {
                    System.out.println("Klabats die arme kikker." + ex);
                    Logger.getLogger(JasperDataSourceProvider.class.getName()).log(Level.SEVERE, null, ex);
                }

            }

            @Override
            public LikelihoodRatio getLikelihoodRatio() {
                final LikelihoodRatio lr = new LikelihoodRatio();
                final LocusProbabilities pros = new LocusProbabilities();
                pros.addLocusProbability("DummyLocus 1", .5);
                pros.addLocusProbability("DummyLocus 2", .4);
                pros.addLocusProbability("DummyLocus 3", .3);
                final LocusProbabilities def = new LocusProbabilities();
                def.addLocusProbability("DummyLocus 1", .6);
                def.addLocusProbability("DummyLocus 2", .4);
                def.addLocusProbability("DummyLocus 3", .3);
                lr.add(pros, def);
                return lr;
            }

            @Override
            public Hypothesis getDefenseHypothesis() {
                return defense;
            }

            @Override
            public Hypothesis getProsecutionHypothesis() {
                return prosecution;
            }

            @Override
            public long getStartTime() {
                return System.currentTimeMillis() - 1000;
            }

            @Override
            public long getStopTime() {
                return System.currentTimeMillis() + 1000;
            }

            @Override
            public boolean isSucceeded() {
                return true;
            }

            @Override
            public Throwable getException() {
                return null;
            }

            @Override
            public String getCaseNumber() {
                return "Dummy Case Number";
            }

            @Override
            public String getProgramVersion() {
                return "Dummy Version";
            }

            @Override
            public SensitivityAnalysisResults getSensitivityAnalysisResults() {
                final SensitivityAnalysisResults results = new SensitivityAnalysisResults();
                results.addRange(LR, "Theta 0.05", new double[][]{{0, 10}, {0.3, 6}, {0.6, 3}, {0.9, 11}});
                results.addRange(LR, "Theta 0.01", new double[][]{{0, 50}, {0.3, 40}, {0.6, 30}, {0.9, 41}});
                final DropoutEstimation dropout = new DropoutEstimation();
                dropout.setValues("Prosecution", new BigDecimal(0.3), new BigDecimal(0.6));
                dropout.setValues("Defense", new BigDecimal(0.5), new BigDecimal(0.7));
                dropout.setAlleleCount(20);
                dropout.setReplicateCount(4);
                dropout.setIterations(1000);
                results.addRange(LR, "Theta 0", new double[][]{{0, 100}, {0.1, 50}, {0.2, 10}, {0.3, 61}});
                results.setDropoutEstimation(dropout);
                return results;
            }

            @Override
            public PopulationStatistics getPopulationStatistics() {
                return populationStatistics;
            }

            @Override
            public Collection<Sample> getReplicates() {
                return replicates;
            }

            @Override
            public Collection<Sample> getProfiles() {
                return profiles;
            }

            @Override
            public int getGuid() {
                return 0;
            }

            @Override
            public NonContributorTestResults getNonContributorTestResults() {
                final NonContributorTestResults results = new NonContributorTestResults("Dummy");
                results.setOriginalLR(20F);
                results.setMinimum(-150F);
                results.setOnePercent(-120F);
                results.setFiftyPercent(-30F);
                results.setNinetyninePercent(10F);
                results.setMaximum(15F);
                return results;
            }

            @Override
            public String getRareAlleleFrequency() {
                return populationStatistics.getRareAlleleFrequency();
            }

            @Override
            public Collection getRareAlleles() {
                final Collection<Allele> rareAlleles = new ArrayList<>();
                for (final Sample sample : replicates) {
                    if (sample.isEnabled()) {
                        for (final Locus locus : sample.getLoci()) {
                            for (final Allele allele : locus.getAlleles()) {
                                if (getProsecutionHypothesis().getPopulationStatistics().isRareAllele(allele)) {
                                    rareAlleles.add(allele);
                                }
                            }
                        }
                    }
                }

                for (final Sample sample : profiles) {
                    if (sample.isEnabled()) {
                        for (final Locus locus : sample.getLoci()) {
                            for (final Allele allele : locus.getAlleles()) {
                                if (getProsecutionHypothesis().getPopulationStatistics().isRareAllele(allele)) {
                                    rareAlleles.add(allele);
                                }
                            }
                        }
                    }
                }
                return rareAlleles;
            }

            @Override
            public boolean isExported() {
                return false;
            }

            @Override
            public void addProcessingTime(final long processingTime) {
            }

            @Override
            public long getProcessingTime() {
                return 0;
            }

            @Override
            public Collection<String> getEnabledLoci() {
                return new ArrayList<>();
            }

            @Override
            public Collection<String> getDisabledLoci() {
                return new ArrayList<>();
            }

            @Override
            public boolean isDropoutCompatible(final AnalysisReport currentReport) {
                return false;
            }

            @Override
            public boolean isSensitivityCompatible(final AnalysisReport currentReport) {
                return false;
            }

            @Override
            public void setSensitivityAnalysisResults(final SensitivityAnalysisResults sensitivityAnalysisResults) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public String getLogfileName() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void setLogfileName(final String name) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        });

        return new JasperDataSource(new SessionData(), "Dummy remarks", list);
    }

    @Override
    public void dispose(final JRDataSource jrds) throws JRException {
        System.out.println("JasperDataSourceProvider.dispose(" + jrds.toString() + ")");
    }

    @Override
    public JRField[] getFields(final JasperReport jr) throws JRException, UnsupportedOperationException {
        return JasperDataSource.getFields();
    }
}
