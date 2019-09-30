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

import java.awt.Component;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import org.jfree.chart.ChartColor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.miginfocom.swing.MigLayout;
import nl.minvenj.nfi.lrmixstudio.domain.Sample;
import nl.minvenj.nfi.lrmixstudio.gui.ApplicationSettings;
import nl.minvenj.nfi.lrmixstudio.gui.ApplicationStateChangeListener;
import nl.minvenj.nfi.lrmixstudio.gui.ProgressGui;
import nl.minvenj.nfi.lrmixstudio.gui.SessionData;
import nl.minvenj.nfi.lrmixstudio.gui.ZebraTable;
import nl.minvenj.nfi.lrmixstudio.model.AnalysisReport;
import nl.minvenj.nfi.lrmixstudio.model.ConfigurationDataChangeListener;
import nl.minvenj.nfi.lrmixstudio.model.ConfigurationDataElement;
import nl.minvenj.nfi.lrmixstudio.model.NonContributorTestResults;

/**
 * This class implements the GUI for the non-contributor test (formerly known as
 * the Performance Test).
 */
public class NonContributorTestPanel extends javax.swing.JPanel implements ConfigurationDataChangeListener, ProgressGui, ApplicationStateChangeListener {

    private static final Logger LOG = LoggerFactory.getLogger(NonContributorTestPanel.class);
    private SessionData session;
    private Thread analysisThread;

    /**
     * Creates new form SensitivityAnalysisPanel
     */
    public NonContributorTestPanel() {
        LOG.info("NonContributorTestPanel");
        initComponents();

        // LRDYN-87 Add a table model change listener that will ensure that only one
        // sample can be selected as person of interest at any one time.
        samplesTable.getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(final TableModelEvent e) {
                if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == 0 && e.getFirstRow() == e.getLastRow()) {
                    final DefaultTableModel model = (DefaultTableModel) samplesTable.getModel();
                    final int row = e.getFirstRow();
                    final Boolean varyDropout = (Boolean) model.getValueAt(row, 0);
                    if (varyDropout) {
                        for (int idx = 0; idx < model.getRowCount(); idx++) {
                            if (idx != row) {
                                model.setValueAt(Boolean.FALSE, idx, 0);
                            }
                        }
                    }
                }
            }
        });

        try {
            final InputStream is = getClass().getResourceAsStream("icon.png");
            final BufferedImage imgOrg = ImageIO.read(is);
            final Image imgScaled = imgOrg.getScaledInstance(140, 149, Image.SCALE_SMOOTH);
            final Icon icon = new ImageIcon(imgScaled);
            jPanel1.setLayout(new MigLayout("", "[140px][115px][6px][75px][6px][71px][6px][41px,grow]", "[149px,grow][25px][]"));
            iconLabel.setIcon(icon);
            jPanel1.add(iconLabel, "cell 0 0,alignx left,aligny top");
            jPanel1.add(jLabel4, "cell 0 1,alignx right,aligny center");
            jPanel1.add(iterationsSpinner, "cell 1 1,growx,aligny center");
            jPanel1.add(stopTestButton, "cell 3 1,alignx left,aligny top");
            jPanel1.add(runTestButton, "cell 5 1,alignx left,aligny top");
            jPanel1.add(testProgressBar, "cell 7 1,grow");
            jPanel1.add(jScrollPane4, "cell 1 0 7 1,grow");

            final JCheckBox logAllResultsCheckBox = new JCheckBox("Create a logfile with detailed results", ApplicationSettings.isLogAllNonConLRs());
            logAllResultsCheckBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    ApplicationSettings.setLogAllNonConLRs(logAllResultsCheckBox.isSelected());
                }
            });
            jPanel1.add(logAllResultsCheckBox, "cell 1 2 3 1");
        } catch (final IOException ex) {
            LOG.warn("Could not read icon!", ex);
        } catch (final IllegalArgumentException iae) {
            LOG.warn("Could not read icon!", iae);
        }
    }

    public void setContext(final SessionData sessionData) {
        session = sessionData;
        session.addDataChangeListener(this);
        session.addStateChangeListener(this);
        dataChanged(ConfigurationDataElement.ACTIVELOCI);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel1.setAlignmentY(Component.TOP_ALIGNMENT);
        jScrollPane4 = new javax.swing.JScrollPane();
        jScrollPane4.setAlignmentY(Component.TOP_ALIGNMENT);
        samplesTable = new ZebraTable();
        jLabel4 = new javax.swing.JLabel();
        iterationsSpinner = new javax.swing.JSpinner();
        stopTestButton = new javax.swing.JButton();
        runTestButton = new javax.swing.JButton();
        testProgressBar = new javax.swing.JProgressBar();
        iconLabel = new javax.swing.JLabel();
        testResultsPanel = new ChartPanel(ChartFactory.createBarChart("Non-contributor Test", "", "Likelihood Ratio (Log10)", new DefaultCategoryDataset()));

        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel1.setPreferredSize(new java.awt.Dimension(559, 0));

        jScrollPane4.setBackground(new java.awt.Color(255, 255, 255));

        samplesTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
            },
            new String [] {
                "Person of Interest", "Profile"
            }
        ) {
            Class<?>[] types = new Class[]{
                java.lang.Boolean.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                true, false
            };

            @Override
            public Class<?> getColumnClass(final int columnIndex) {
                return types [columnIndex];
            }

            @Override
            public boolean isCellEditable(final int rowIndex, final int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        samplesTable.setFillsViewportHeight(true);
        samplesTable.setName("performanceSettings"); // NOI18N
        samplesTable.setRequestFocusEnabled(false);
        samplesTable.setRowSelectionAllowed(false);
        samplesTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane4.setViewportView(samplesTable);
        if (samplesTable.getColumnModel().getColumnCount() > 0) {
            samplesTable.getColumnModel().getColumn(0).setMinWidth(100);
            samplesTable.getColumnModel().getColumn(0).setPreferredWidth(100);
            samplesTable.getColumnModel().getColumn(0).setMaxWidth(100);
            samplesTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        }

        jLabel4.setText("Iterations");

        iterationsSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1000), Integer.valueOf(1), null, Integer.valueOf(1000)));

        stopTestButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/stop16.png"))); // NOI18N
        stopTestButton.setText("Stop");
        stopTestButton.setEnabled(false);
        stopTestButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final java.awt.event.ActionEvent evt) {
                stopTestButtonActionPerformed(evt);
            }
        });

        runTestButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/play16.png"))); // NOI18N
        runTestButton.setText("Run");
        runTestButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final java.awt.event.ActionEvent evt) {
                runTestButtonActionPerformed(evt);
            }
        });

        iconLabel.setPreferredSize(new java.awt.Dimension(140, 149));

        jSplitPane1.setTopComponent(jPanel1);

        final javax.swing.GroupLayout testResultsPanelLayout = new javax.swing.GroupLayout(testResultsPanel);
        testResultsPanel.setLayout(testResultsPanelLayout);
        testResultsPanelLayout.setHorizontalGroup(
            testResultsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 590, Short.MAX_VALUE)
        );
        testResultsPanelLayout.setVerticalGroup(
            testResultsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 304, Short.MAX_VALUE)
        );

        jSplitPane1.setRightComponent(testResultsPanel);

        final javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSplitPane1)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 539, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void runTestButtonActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runTestButtonActionPerformed

        Sample personOfInterest = null;
        final DefaultTableModel model = (DefaultTableModel) samplesTable.getModel();
        for (int idx = 0; idx < model.getRowCount(); idx++) {
            if ((Boolean) model.getValueAt(idx, 0)) {
                personOfInterest = (Sample) model.getValueAt(idx, 1);
            }
        }

        if (personOfInterest == null) {
            session.setErrorMessage("No Person of Interest selected!");
            return;
        }

        LOG.info("Starting Non-Contributor Test");

        analysisThread = new NonContributorTest(
                session,
                personOfInterest,
                (Integer) iterationsSpinner.getModel().getValue(),
                new NonContributorTestProgressListener(this, session, personOfInterest.toString()));
        analysisThread.start();
    }//GEN-LAST:event_runTestButtonActionPerformed

    private void stopTestButtonActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopTestButtonActionPerformed
        if (analysisThread != null && analysisThread.isAlive()) {
            analysisThread.interrupt();
        }
    }//GEN-LAST:event_stopTestButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel iconLabel;
    private javax.swing.JSpinner iterationsSpinner;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JButton runTestButton;
    private javax.swing.JTable samplesTable;
    private javax.swing.JButton stopTestButton;
    private javax.swing.JProgressBar testProgressBar;
    private javax.swing.JPanel testResultsPanel;
    // End of variables declaration//GEN-END:variables

    @Override
    public void dataChanged(final ConfigurationDataElement element) {
        switch (element) {
            case ACTIVEPROFILES:
            case ACTIVEREPLICATES:
            case PROFILES:
            case REPLICATES:
            case DEFENSE:
            case PROSECUTION:
                if (session.getDefense() != null && session.getProsecution()!=null) {
                    ((DefaultTableModel) samplesTable.getModel()).setRowCount(0);
                    // LRDYN-87 This boolean ensures that initially at most one profile is selected.
                    boolean first = true;
                    for (final Sample sample : session.getActiveProfiles()) {
                        if (session.getDefense().isContributor(sample) != session.getProsecution().isContributor(sample)) {
                            ((DefaultTableModel) samplesTable.getModel()).addRow(new Object[]{first, sample});
                            first = false;
                        }
                    }
                    plotResults(null);
                }
                break;
        }
    }

    @Override
    public void plotResults(final Object testResults) {
        try {
            if (session.getStatistics() == null) {
                return;
            }
            NonContributorTestResults nonContributorTestResults = null;
            if (testResults instanceof NonContributorTestResults) {
                nonContributorTestResults = (NonContributorTestResults) testResults;
            } else {
                final AnalysisReport currentReport = session.getCurrentReport();
                if (currentReport != null) {
                    nonContributorTestResults = currentReport.getNonContributorTestResults();
                }
            }

            final DefaultCategoryDataset dataSet = new DefaultCategoryDataset();
            final JFreeChart chart = ChartFactory.createBarChart("Non-contributor test results", "log10(LRrandom) characteristics", "Likelihood Ratio (log10)", dataSet);
            if (nonContributorTestResults != null) {
                final String rangeName = "Replacing " + nonContributorTestResults.getDescription() + ". " + nonContributorTestResults.getIterations() + " iterations.";
                safeAddValue(dataSet, nonContributorTestResults.getOriginalLR(), "POI", rangeName);
                safeAddValue(dataSet, nonContributorTestResults.getMinimum(), "Min", rangeName);
                safeAddValue(dataSet, nonContributorTestResults.getOnePercent(), "1%", rangeName);
                safeAddValue(dataSet, nonContributorTestResults.getFiftyPercent(), "50%", rangeName);
                safeAddValue(dataSet, nonContributorTestResults.getNinetyninePercent(), "99%", rangeName);
                safeAddValue(dataSet, nonContributorTestResults.getMaximum(), "Max", rangeName);
            }
            chart.getCategoryPlot().setNoDataMessage("No non-contributor test results yet!");
            chart.getCategoryPlot().setDomainGridlinesVisible(false);
            chart.getCategoryPlot().setRangeGridlinesVisible(nonContributorTestResults != null);
            chart.getCategoryPlot().setRangeGridlinePaint(ChartColor.DARK_GRAY);
            chart.getCategoryPlot().setDomainGridlinePaint(ChartColor.DARK_GRAY);
            final ValueAxis rangeAxis = chart.getCategoryPlot().getRangeAxis(0);
            rangeAxis.setUpperMargin(0.1);
            rangeAxis.setLowerMargin(0.1);
            chart.getPlot().setBackgroundPaint(ChartColor.WHITE);

            chart.removeLegend();

            if (nonContributorTestResults != null) {
                chart.addSubtitle(0, new TextTitle("Replacing " + nonContributorTestResults.getDescription() + " with randomly generated profiles over " + nonContributorTestResults.getIterations() + " iterations."));
                chart.addSubtitle(1, new TextTitle(String.format("LRs>1: %d%% (%d).     LRs>LR(POI): %d%% (%d).",
                        nonContributorTestResults.getPercentageOver1(), nonContributorTestResults.getOver1Count(),
                        nonContributorTestResults.getPercentageOverOriginalLR(), nonContributorTestResults.getOverOriginalCount())));
            }
            chart.getCategoryPlot().setRenderer(new MyBarRenderer(nonContributorTestResults));
            chart.fireChartChanged();

            ((ChartPanel) testResultsPanel).setChart(chart);
            chart.fireChartChanged();
            final JFreeChart clone = (JFreeChart) ((ChartPanel) testResultsPanel).getChart().clone();
            clone.removeLegend();
            clone.setSubtitles(new ArrayList<>());
            if (nonContributorTestResults != null) {
                nonContributorTestResults.setPreview(clone.createBufferedImage(400, 400, 300, 300, null));
                nonContributorTestResults.setGraphImage(((ChartPanel) testResultsPanel).getChart().createBufferedImage(1500, 1500, 500, 500, null));
            }
        } catch (final Exception e) {
            LOG.error("Error updating sensitivity plot", e);
        }
    }

    private void safeAddValue(final DefaultCategoryDataset dataSet, final Double value, final String description, final String rangeName) {
        if (value == null) {
            return;
        }
        if (value.isInfinite() || value.isNaN()) {
            dataSet.addValue(0.0, rangeName, description);
        } else {
            dataSet.addValue(value, rangeName, description);
        }
    }

    @Override
    public void setLocusResult(final String locus, final String format) {
    }

    @Override
    public void setOverallLikelyhoodRatio(final String format) {
    }

    @Override
    public void setTimeLeft(final String string) {
    }

    @Override
    public void setTimeSpent(final String string) {
    }

    @Override
    public void setAnalysisProgress(final int promille) {
        testProgressBar.setMaximum(1000);
        testProgressBar.setValue(promille);
        testProgressBar.setStringPainted(true);
        testProgressBar.setString(promille / 10 + "%");
        session.setProgress(promille / 10);
    }

    @Override
    public void applicationStateChanged(final APP_STATE newState) {
        switch (newState) {
            case NONCONTRIBUTOR_TEST_RUNNING:
                enableControls(false);
                testProgressBar.setVisible(true);
                stopTestButton.setEnabled(true);
                runTestButton.setEnabled(false);
                break;
            case READY_FOR_ANALYSIS:
                enableControls(true);
                testProgressBar.setVisible(false);
                stopTestButton.setEnabled(false);
                runTestButton.setEnabled(true);
                plotResults(null);
                break;
        }
        setEnabled(newState == APP_STATE.READY_FOR_ANALYSIS && session.getStatistics() != null);
    }

    private void enableControls(final boolean b) {
        enableComponent(this, b);
        samplesTable.setEnabled(b);
    }

    private void enableComponent(final JComponent panel, final boolean enabled) {
        panel.setEnabled(enabled);
        for (int componentIndex = 0; componentIndex < panel.getComponentCount(); componentIndex++) {
            final Component c = panel.getComponent(componentIndex);
            if (c instanceof JComponent) {
                enableComponent((JComponent) c, enabled);
            }
        }
    }
}
