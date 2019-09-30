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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;

import org.jfree.chart.ChartColor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.LegendItemSource;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.renderer.xy.DefaultXYItemRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.DefaultXYDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.miginfocom.swing.MigLayout;
import nl.minvenj.nfi.lrmixstudio.domain.Hypothesis;
import nl.minvenj.nfi.lrmixstudio.domain.Sample;
import nl.minvenj.nfi.lrmixstudio.gui.ApplicationStateChangeListener;
import nl.minvenj.nfi.lrmixstudio.gui.SessionData;
import nl.minvenj.nfi.lrmixstudio.gui.ZebraTable;
import nl.minvenj.nfi.lrmixstudio.model.AnalysisReport;
import nl.minvenj.nfi.lrmixstudio.model.ConfigurationDataChangeListener;
import nl.minvenj.nfi.lrmixstudio.model.ConfigurationDataElement;
import nl.minvenj.nfi.lrmixstudio.model.DropoutEstimation;
import nl.minvenj.nfi.lrmixstudio.model.SensitivityAnalysisResults;
import nl.minvenj.nfi.lrmixstudio.model.SensitivityAnalysisResults.Range;
import nl.minvenj.nfi.lrmixstudio.model.SensitivityAnalysisResults.RangeType;
import javax.swing.JTabbedPane;
import javax.swing.JSplitPane;

public class SensitivityAnalysisPanel extends javax.swing.JPanel implements ConfigurationDataChangeListener, SensitivityAnalysisProgressGui, ApplicationStateChangeListener {

    private static final double DEFAULT_DROPOUT_TO = 0.99d;
    private static final double DEFAULT_DROPOUT_FROM = 0.00d;
    private static final int DEFAULT_DROPOUT_ESTIMATION_STEPS = 99;
    private static final int DEFAULT_DROPOUT_STEPS = 99;
    private static final int DEFAULT_DROPOUT_ESTMATION_ITERATIONS = 1000;

    public static final String DEFENSE_UNKNOWN_CONTRIBUTORS = "Defense Unknown Contributors";
    public static final String PROSECUTION_UNKNOWN_CONTRIBUTORS = "Prosecution Unknown Contributors";
    private static final Logger LOG = LoggerFactory.getLogger(SensitivityAnalysisPanel.class);
    private SessionData session;
    private Thread analysisThread;

    private List<String> extractSubTitle(final List<String> subTitle, final String id) {
        final String[] components = id.split("\\. ");
        final ArrayList<String> idComponents = new ArrayList<>();
        boolean first = true;
        for (final String component : components) {
            if (first) {
                idComponents.add(component.substring(0, component.indexOf(" ")).trim());
                idComponents.add((component.substring(component.indexOf(" ")) + (component.endsWith(".") ? "" : ".")).trim());
                first = false;
            }
            else {
                idComponents.add((component + (component.endsWith(".") ? "" : ".")).trim());
            }
        }
        if (subTitle == null) {
            return idComponents;
        }

        // Any element of the subtitle that is not in the current id gets removed from the subtitle
        final ArrayList<String> approvedSubtitleComponents = new ArrayList<>();
        for (final String subtitleComponent : subTitle) {
            if (idComponents.contains(subtitleComponent)) {
                approvedSubtitleComponents.add(subtitleComponent);
            }
        }
        return approvedSubtitleComponents;
    }

    private String extractSeriesName(final List<String> subTitle, final String id) {
        String retval = id;
        for (final String subtitleComponent : subTitle) {
            retval = retval.replace(subtitleComponent, "");
        }
        return capitalize(retval.replaceAll("\\.(\\D)", "\\. $1").replaceAll("\\s+", " "));
    }

    private String buildSubTitleString(final List<String> subTitle) {
        if (subTitle == null) {
            return "";
        }
        final StringBuilder builder = new StringBuilder();
        for (final String subtitleComponent : subTitle) {
            builder.append(capitalize(subtitleComponent)).append(" ");
        }
        return builder.toString();
    }

    private String capitalize(final String string) {
        final String trimmed = string.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        return string.trim().substring(0, 1).toUpperCase() + string.trim().substring(1);
    }

    private void updateSettings() {
        if (session.getDefense() != null && session.getProsecution() != null) {
            sensitivityAnalysisDropin.setValue(session.getDefense().getDropInProbability());
            dropoutEstimationDropin.setValue(session.getDefense().getDropInProbability());
            theta.setValue(session.getDefense().getThetaCorrection());
            ((DefaultTableModel) sensitivityAnalysisTable.getModel()).setRowCount(0);
            for (final Sample sample : session.getActiveProfiles()) {
                final boolean varyThis = session.getDefense().isContributor(sample) != session.getProsecution().isContributor(sample);
                ((DefaultTableModel) sensitivityAnalysisTable.getModel()).addRow(new Object[]{varyThis, sample, sample.getSourceFile()});
            }
            if (session.getDefense().getUnknownCount() > 0) {
                ((DefaultTableModel) sensitivityAnalysisTable.getModel()).addRow(new Object[]{true, DEFENSE_UNKNOWN_CONTRIBUTORS});
            }
            if (session.getProsecution().getUnknownCount() > 0) {
                ((DefaultTableModel) sensitivityAnalysisTable.getModel()).addRow(new Object[]{true, PROSECUTION_UNKNOWN_CONTRIBUTORS});
            }
            plotResults();
        }
    }

    private class SensitivityAnalysisRangeChangeListener implements ChangeListener {

        private final SpinnerNumberModel _toModel;
        private final SpinnerNumberModel _fromModel;
        private final SpinnerNumberModel _stepsModel;

        public SensitivityAnalysisRangeChangeListener(final JSpinner from, final JSpinner to, final JSpinner steps) {
            _toModel = (SpinnerNumberModel) to.getModel();
            _fromModel = (SpinnerNumberModel) from.getModel();
            _stepsModel = (SpinnerNumberModel) steps.getModel();
        }

        @Override
        public void stateChanged(final ChangeEvent e) {

            _fromModel.setMaximum(_toModel.getNumber().doubleValue() - _toModel.getStepSize().doubleValue());
            _toModel.setMinimum(_fromModel.getNumber().doubleValue() + _fromModel.getStepSize().doubleValue());
            final double intervalSteps = (_toModel.getNumber().doubleValue() - _fromModel.getNumber().doubleValue()) / 0.01;
            final int newMax = new BigDecimal(intervalSteps).setScale(2, RoundingMode.HALF_UP).intValue();

            boolean scaleCurrentValue = false;
            if (_stepsModel.getMaximum().compareTo(_stepsModel.getNumber()) == 0) {
                scaleCurrentValue = true;
            }
            _stepsModel.setMaximum(newMax);
            if (scaleCurrentValue || _stepsModel.getMaximum().compareTo(_stepsModel.getNumber()) < 0) {
                _stepsModel.setValue(_stepsModel.getMaximum());
            }
        }

    }

    /**
     * Creates new form SensitivityAnalysisPanel
     */
    public SensitivityAnalysisPanel() {
        LOG.info("SensitivityAnalysisPanel");
        initComponents();

        try {
            final InputStream is = getClass().getResourceAsStream("icon.png");
            final BufferedImage imgOrg = ImageIO.read(is);
            final Image imgScaled = imgOrg.getScaledInstance(140, 149, Image.SCALE_SMOOTH);
            final Icon icon = new ImageIcon(imgScaled);
            iconLabel.setIcon(icon);
        }
        catch (final IOException ex) {
            LOG.warn("Could not read icon!", ex);
        }
        catch (final IllegalArgumentException iae) {
            LOG.warn("Could not read icon!", iae);
        }
        setLayout(new MigLayout("", "[701px,grow]", "[210px:210px][grow]"));

        dropoutFrom.addChangeListener(new SensitivityAnalysisRangeChangeListener(dropoutFrom, dropoutTo, dropoutSteps));
        dropoutTo.addChangeListener(new SensitivityAnalysisRangeChangeListener(dropoutFrom, dropoutTo, dropoutSteps));
        dropoutEstimationFrom.addChangeListener(new SensitivityAnalysisRangeChangeListener(dropoutEstimationFrom, dropoutEstimationTo, dropoutEstimationSteps));
        dropoutEstimationTo.addChangeListener(new SensitivityAnalysisRangeChangeListener(dropoutEstimationFrom, dropoutEstimationTo, dropoutEstimationSteps));
        add(topPanel, "cell 0 0,grow");
        
        splitPane = new JSplitPane();
        splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        add(splitPane, "cell 0 1,grow");
        splitPane.setLeftComponent(settingsTabbedPane);
        splitPane.setRightComponent(chartSplitPane);
    }

    public void setContext(final SessionData sessionData) {
        session = sessionData;
        session.addDataChangeListener(this);
        session.addStateChangeListener(this);
        locusComboBox.removeAllItems();
        locusComboBox.addItem("All Loci");
        for (final String locusName : session.getAllLoci()) {
            locusComboBox.addItem(locusName);
        }
        chartSplitPane.setDividerLocation(0.8);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane2 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        settingsTabbedPane = new javax.swing.JTabbedPane();
        final javax.swing.JPanel sensitivitySettingsPanel = new javax.swing.JPanel();
        final javax.swing.JLabel dropoutVariationLabel = new javax.swing.JLabel();
        dropoutFrom = new javax.swing.JSpinner();
        final javax.swing.JLabel toLabel = new javax.swing.JLabel();
        dropoutTo = new javax.swing.JSpinner();
        final javax.swing.JLabel inLabel = new javax.swing.JLabel();
        dropoutSteps = new javax.swing.JSpinner();
        final javax.swing.JLabel dropinLabel = new javax.swing.JLabel();
        sensitivityAnalysisDropin = new javax.swing.JSpinner();
        final javax.swing.JLabel thetaLabel = new javax.swing.JLabel();
        theta = new javax.swing.JSpinner();
        sensitivityAnalysisButton = new javax.swing.JButton();
        locusComboBox = new javax.swing.JComboBox();
        stepsAtLabel = new javax.swing.JLabel();
        sensitivityStopButton = new javax.swing.JButton();
        sensitivityProgressBar = new javax.swing.JProgressBar();
        sensitivityTimeLeft = new javax.swing.JLabel();
        final javax.swing.JPanel estimationSettingsPanel = new javax.swing.JPanel();
        dropoutEstimationButton = new javax.swing.JButton();
        final javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
        dropoutEstimationIterations = new javax.swing.JSpinner();
        final javax.swing.JLabel jLabel16 = new javax.swing.JLabel();
        dropoutEstimationDropin = new javax.swing.JSpinner();
        final javax.swing.JLabel jLabel6 = new javax.swing.JLabel();
        dropoutEstimationFrom = new javax.swing.JSpinner();
        final javax.swing.JLabel jLabel13 = new javax.swing.JLabel();
        dropoutEstimationTo = new javax.swing.JSpinner();
        final javax.swing.JLabel jLabel17 = new javax.swing.JLabel();
        dropoutEstimationSteps = new javax.swing.JSpinner();
        jLabel4 = new javax.swing.JLabel();
        dropoutEstimationProgressBar = new javax.swing.JProgressBar();
        dropoutStopButton = new javax.swing.JButton();
        dropoutEstimationTimeLeft = new javax.swing.JLabel();
        chartSplitPane = new javax.swing.JSplitPane();
        chartSplitPane.setContinuousLayout(true);
        chartSplitPane.setResizeWeight(1.0);
        plotPanel = new ChartPanel(ChartFactory.createXYAreaChart("Sensitivity Analysis", "Dropout Probability", "Likelihood Ratio (Log10)", new DefaultXYDataset()));
        graphControlPanel = new javax.swing.JPanel();
        topPanel = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        sensitivityAnalysisTable = new ZebraTable();
        iconLabel = new javax.swing.JLabel();
        final javax.swing.JPanel setDropoutPanel = new javax.swing.JPanel();
        setDropoutSpinner = new javax.swing.JSpinner();
        final javax.swing.JButton setDropoutButton = new javax.swing.JButton();
        final javax.swing.JLabel setDropoutLabel = new javax.swing.JLabel();
        setDropoutStatusLabel = new javax.swing.JLabel();

        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane2.setViewportView(jTable2);

        dropoutVariationLabel.setText("Drop-Out variation");

        dropoutFrom.setModel(new javax.swing.SpinnerNumberModel(DEFAULT_DROPOUT_FROM, 0.0d, 0.99d, 0.01d));

        toLabel.setText("to");

        dropoutTo.setModel(new javax.swing.SpinnerNumberModel(DEFAULT_DROPOUT_TO, 0.01d, 0.99d, 0.01d));

        inLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        inLabel.setText("in");
        inLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        dropoutSteps.setModel(new javax.swing.SpinnerNumberModel(DEFAULT_DROPOUT_STEPS, 1, 99, 1));

        dropinLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        dropinLabel.setText("Drop In");

        sensitivityAnalysisDropin.setModel(new javax.swing.SpinnerNumberModel(0.05d, 0.0d, 1.0d, 0.01d));

        thetaLabel.setText("Theta");

        theta.setModel(new javax.swing.SpinnerNumberModel(0.01d, 0.0d, 1.0d, 0.01d));

        sensitivityAnalysisButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/play16.png"))); // NOI18N
        sensitivityAnalysisButton.setText("Run");
        sensitivityAnalysisButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final java.awt.event.ActionEvent evt) {
                sensitivityAnalysisButtonActionPerformed(evt);
            }
        });

        stepsAtLabel.setText("steps at locus");

        sensitivityStopButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/stop16.png"))); // NOI18N
        sensitivityStopButton.setText("Stop");
        sensitivityStopButton.setEnabled(false);
        sensitivityStopButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final java.awt.event.ActionEvent evt) {
                sensitivityStopButtonActionPerformed(evt);
            }
        });

        sensitivityProgressBar.setString("");

        sensitivityTimeLeft.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);

        final javax.swing.GroupLayout gl_sensitivitySettingsPanel = new javax.swing.GroupLayout(sensitivitySettingsPanel);
        sensitivitySettingsPanel.setLayout(gl_sensitivitySettingsPanel);
        gl_sensitivitySettingsPanel.setHorizontalGroup(
            gl_sensitivitySettingsPanel.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(gl_sensitivitySettingsPanel.createSequentialGroup()
                .addContainerGap()
                .addGroup(gl_sensitivitySettingsPanel.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(dropoutVariationLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(dropinLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(gl_sensitivitySettingsPanel.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, gl_sensitivitySettingsPanel.createSequentialGroup()
                        .addComponent(sensitivityAnalysisDropin, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(thetaLabel))
                    .addGroup(gl_sensitivitySettingsPanel.createSequentialGroup()
                        .addComponent(dropoutFrom, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(toLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dropoutTo, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(inLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(gl_sensitivitySettingsPanel.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(theta, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dropoutSteps, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(gl_sensitivitySettingsPanel.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(gl_sensitivitySettingsPanel.createSequentialGroup()
                        .addComponent(stepsAtLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(locusComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 51, Short.MAX_VALUE)
                        .addComponent(sensitivityStopButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sensitivityAnalysisButton))
                    .addGroup(gl_sensitivitySettingsPanel.createSequentialGroup()
                        .addComponent(sensitivityProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sensitivityTimeLeft, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        gl_sensitivitySettingsPanel.setVerticalGroup(
            gl_sensitivitySettingsPanel.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(gl_sensitivitySettingsPanel.createSequentialGroup()
                .addContainerGap()
                .addGroup(gl_sensitivitySettingsPanel.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dropoutVariationLabel)
                    .addComponent(dropoutFrom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dropoutTo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(toLabel)
                    .addComponent(inLabel)
                    .addComponent(dropoutSteps, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(stepsAtLabel)
                    .addComponent(locusComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sensitivityStopButton, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sensitivityAnalysisButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(gl_sensitivitySettingsPanel.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(gl_sensitivitySettingsPanel.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(dropinLabel)
                        .addComponent(thetaLabel)
                        .addComponent(theta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(sensitivityTimeLeft, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(sensitivityAnalysisDropin)
                    .addComponent(sensitivityProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(21, Short.MAX_VALUE))
        );

        settingsTabbedPane.addTab("Sensitivity Analysis Settings", sensitivitySettingsPanel);

        dropoutEstimationButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/play16.png"))); // NOI18N
        dropoutEstimationButton.setText("Run");
        dropoutEstimationButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final java.awt.event.ActionEvent evt) {
                dropoutEstimationButtonActionPerformed(evt);
            }
        });

        jLabel1.setText("Iterations");

        dropoutEstimationIterations.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(DEFAULT_DROPOUT_ESTMATION_ITERATIONS), Integer.valueOf(1), null, Integer.valueOf(100)));

        jLabel16.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel16.setText("Drop In");

        dropoutEstimationDropin.setModel(new javax.swing.SpinnerNumberModel(0.05d, 0.0d, 1.0d, 0.01d));

        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel6.setText("Drop-Out variation");

        dropoutEstimationFrom.setModel(new javax.swing.SpinnerNumberModel(DEFAULT_DROPOUT_FROM, 0.0d, 0.99d, 0.01d));

        jLabel13.setText("to");

        dropoutEstimationTo.setModel(new javax.swing.SpinnerNumberModel(DEFAULT_DROPOUT_TO, 0.01d, 0.99d, 0.01d));

        jLabel17.setText("in");

        dropoutEstimationSteps.setModel(new javax.swing.SpinnerNumberModel(DEFAULT_DROPOUT_ESTIMATION_STEPS, 1, 99, 1));

        jLabel4.setText("steps");

        dropoutEstimationProgressBar.setString("");

        dropoutStopButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/stop16.png"))); // NOI18N
        dropoutStopButton.setText("Stop");
        dropoutStopButton.setEnabled(false);
        dropoutStopButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final java.awt.event.ActionEvent evt) {
                dropoutStopButtonActionPerformed(evt);
            }
        });

        dropoutEstimationTimeLeft.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);

        final javax.swing.GroupLayout gl_estimationSettingsPanel = new javax.swing.GroupLayout(estimationSettingsPanel);
        estimationSettingsPanel.setLayout(gl_estimationSettingsPanel);
        gl_estimationSettingsPanel.setHorizontalGroup(
            gl_estimationSettingsPanel.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(gl_estimationSettingsPanel.createSequentialGroup()
                .addContainerGap()
                .addGroup(gl_estimationSettingsPanel.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, 94, Short.MAX_VALUE)
                    .addComponent(jLabel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(gl_estimationSettingsPanel.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(dropoutEstimationFrom, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dropoutEstimationDropin, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(gl_estimationSettingsPanel.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(gl_estimationSettingsPanel.createSequentialGroup()
                        .addComponent(jLabel13)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dropoutEstimationTo, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel17))
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(gl_estimationSettingsPanel.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(dropoutEstimationIterations)
                    .addComponent(dropoutEstimationSteps, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(gl_estimationSettingsPanel.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(gl_estimationSettingsPanel.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(dropoutStopButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dropoutEstimationButton))
                    .addGroup(gl_estimationSettingsPanel.createSequentialGroup()
                        .addComponent(dropoutEstimationProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, 319, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dropoutEstimationTimeLeft, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        gl_estimationSettingsPanel.setVerticalGroup(
            gl_estimationSettingsPanel.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, gl_estimationSettingsPanel.createSequentialGroup()
                .addContainerGap()
                .addGroup(gl_estimationSettingsPanel.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(gl_estimationSettingsPanel.createSequentialGroup()
                        .addGroup(gl_estimationSettingsPanel.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(dropoutEstimationFrom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(dropoutEstimationTo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel13)
                            .addComponent(jLabel17)
                            .addComponent(dropoutEstimationSteps, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4)
                            .addComponent(jLabel6))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(gl_estimationSettingsPanel.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(dropoutEstimationDropin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel16)
                            .addComponent(jLabel1)
                            .addComponent(dropoutEstimationIterations, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(gl_estimationSettingsPanel.createSequentialGroup()
                        .addGroup(gl_estimationSettingsPanel.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(dropoutEstimationButton)
                            .addComponent(dropoutStopButton))
                        .addGroup(gl_estimationSettingsPanel.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(gl_estimationSettingsPanel.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(dropoutEstimationTimeLeft, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(gl_estimationSettingsPanel.createSequentialGroup()
                                .addGap(1, 1, 1)
                                .addComponent(dropoutEstimationProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(26, Short.MAX_VALUE))
        );

        settingsTabbedPane.addTab("Dropout Estimation Settings", estimationSettingsPanel);

        plotPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        plotPanel.setMinimumSize(new java.awt.Dimension(100, 100));
        plotPanel.setPreferredSize(new java.awt.Dimension(400, 400));

        final javax.swing.GroupLayout plotPanelLayout = new javax.swing.GroupLayout(plotPanel);
        plotPanel.setLayout(plotPanelLayout);
        plotPanelLayout.setHorizontalGroup(
            plotPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        plotPanelLayout.setVerticalGroup(
            plotPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 264, Short.MAX_VALUE)
        );

        chartSplitPane.setLeftComponent(plotPanel);

        chartSplitPane.setRightComponent(graphControlPanel);
        graphControlPanel.setLayout(new MigLayout("", "[649px,grow]", "[grow][]"));

        _rangeTableScrollPane = new JScrollPane();
        graphControlPanel.add(_rangeTableScrollPane, "cell 0 0,grow");

        _rangeTable = new JTable();
        _rangeTableScrollPane.setViewportView(_rangeTable);
        _rangeTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        _rangeTable.setFillsViewportHeight(true);
        _rangeTable.setModel(new DefaultTableModel(
                                                   new Object[][]{
                                                       {null, null},
                                                   },
                                                   new String[]{
                                                       "Show", "Range Title"
                                                   }) {
            Class[] columnTypes = new Class[]{
                Boolean.class, Object.class
            };

            @Override
            public Class getColumnClass(final int columnIndex) {
                return columnTypes[columnIndex];
            }

            boolean[] columnEditables = new boolean[]{
                true, false
            };

            @Override
            public boolean isCellEditable(final int row, final int column) {
                return columnEditables[column];
            }

            @Override
            public void setValueAt(final Object aValue, final int row, final int column) {
                super.setValueAt(aValue, row, column);
                if (column == 0) {
                    updateGraph();
                }
            }

        });

        final JPanel buttonPanel = new JPanel();
        graphControlPanel.add(buttonPanel, "cell 0 1,grow");
        buttonPanel.setLayout(new MigLayout("", "[][]", "[][]"));

        final JButton selectLRsButton = new JButton("Select LRs Only");
        selectLRsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                selectOnlyLRs();
            }
        });
        buttonPanel.add(selectLRsButton, "flowx,cell 0 0");

        final JButton deleteButton = new JButton("Select All");
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                selectAllRanges();
            }
        });
        deleteButton.setActionCommand("");
        buttonPanel.add(deleteButton, "cell 0 0");
        _rangeTable.getColumnModel().getColumn(0).setResizable(false);
        _rangeTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        _rangeTable.getColumnModel().getColumn(0).setMinWidth(40);
        _rangeTable.getColumnModel().getColumn(0).setMaxWidth(40);

        sensitivityAnalysisTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Vary Dropout", "Profile"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Boolean.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                true, false
            };

            @Override
            public Class getColumnClass(final int columnIndex) {
                return types [columnIndex];
            }

            @Override
            public boolean isCellEditable(final int rowIndex, final int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        sensitivityAnalysisTable.setFillsViewportHeight(true);
        sensitivityAnalysisTable.setName("sensitivitySettings"); // NOI18N
        sensitivityAnalysisTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane4.setViewportView(sensitivityAnalysisTable);
        if (sensitivityAnalysisTable.getColumnModel().getColumnCount() > 0) {
            sensitivityAnalysisTable.getColumnModel().getColumn(0).setMinWidth(80);
            sensitivityAnalysisTable.getColumnModel().getColumn(0).setPreferredWidth(80);
            sensitivityAnalysisTable.getColumnModel().getColumn(0).setMaxWidth(80);
            sensitivityAnalysisTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        }

        iconLabel.setMaximumSize(new java.awt.Dimension(140, 149));
        iconLabel.setMinimumSize(new java.awt.Dimension(140, 149));
        iconLabel.setPreferredSize(new java.awt.Dimension(140, 149));

        setDropoutPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        setDropoutSpinner.setModel(new javax.swing.SpinnerNumberModel(0.0d, 0.0d, 1.0d, 0.01d));

        setDropoutButton.setText("Set");
        setDropoutButton.setToolTipText("Sets the entered dropout value for the profiles selected above.");
        setDropoutButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final java.awt.event.ActionEvent evt) {
                setDropoutButtonActionPerformed(evt);
            }
        });

        setDropoutLabel.setText("Set dropout of selected profiles to:");

        final javax.swing.GroupLayout setDropoutPanelLayout = new javax.swing.GroupLayout(setDropoutPanel);
        setDropoutPanel.setLayout(setDropoutPanelLayout);
        setDropoutPanelLayout.setHorizontalGroup(
            setDropoutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(setDropoutPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(setDropoutLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(setDropoutSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(setDropoutButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(setDropoutStatusLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        setDropoutPanelLayout.setVerticalGroup(
            setDropoutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(setDropoutPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(setDropoutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(setDropoutStatusLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, setDropoutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(setDropoutLabel)
                        .addComponent(setDropoutSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(setDropoutButton)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        final javax.swing.GroupLayout gl_topPanel = new javax.swing.GroupLayout(topPanel);
        topPanel.setLayout(gl_topPanel);
        gl_topPanel.setHorizontalGroup(
            gl_topPanel.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(gl_topPanel.createSequentialGroup()
                .addContainerGap()
                .addComponent(iconLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(gl_topPanel.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane4)
                    .addComponent(setDropoutPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        gl_topPanel.setVerticalGroup(
            gl_topPanel.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(gl_topPanel.createSequentialGroup()
                .addContainerGap()
                .addGroup(gl_topPanel.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(gl_topPanel.createSequentialGroup()
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(setDropoutPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(iconLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        chartSplitPane.setDividerLocation(0.5);

        EventQueue.invokeLater(new Runnable() {/* (non-Javadoc)
                                               * @see java.lang.Runnable#run()
                                               */
            @Override
            public void run() {
                chartSplitPane.setDividerLocation(0.5);

            }
        });
    }// </editor-fold>//GEN-END:initComponents

    /**
     *
     */
    protected void selectAllRanges() {
        final DefaultTableModel tmodel = (DefaultTableModel) _rangeTable.getModel();
        for (int row = 0; row < tmodel.getRowCount(); row++) {
            tmodel.setValueAt(true, row, 0);
        }
        updateGraph();
    }

    /**
     *
     */
    protected void selectOnlyLRs() {
        final DefaultTableModel tmodel = (DefaultTableModel) _rangeTable.getModel();
        for (int row = 0; row < tmodel.getRowCount(); row++) {
            final SensitivityAnalysisResults.Range range = (Range) tmodel.getValueAt(row, 1);
            tmodel.setValueAt(range.getRangeType() == RangeType.LR, row, 0);
        }
        updateGraph();
    }

    /**
     *
     */
    protected void updateGraph() {
        try {
            final AnalysisReport currentReport = session.getCurrentReport();
            if (currentReport != null) {
                final SensitivityAnalysisResults sar = session.getCurrentReport().getSensitivityAnalysisResults();
                List<String> subTitle = null;

                final ChartPanel panel = (ChartPanel) plotPanel;
                final ArrayList<IntervalMarker> intervalMarkers = new ArrayList<>();

                final LegendItemCollection legendItemCollection = new LegendItemCollection();

                final DefaultXYDataset dataSet = new DefaultXYDataset();
                for (final SensitivityAnalysisResults.Range range : sar.getRanges()) {
                    subTitle = extractSubTitle(subTitle, range.getDescription());
                }

                final DefaultTableModel tmodel = (DefaultTableModel) _rangeTable.getModel();
                final int[] seriesIdxToRow = new int[tmodel.getRowCount()];
                int seriesCount = 0;
                for (int row = 0; row < tmodel.getRowCount(); row++) {
                    if ((boolean) tmodel.getValueAt(row, 0)) {
                        final SensitivityAnalysisResults.Range range = (Range) tmodel.getValueAt(row, 1);
                        final double[][] series = new double[2][range.getPoints().size()];
                        int idx = 0;
                        for (final SensitivityAnalysisResults.Point point : range.getPoints()) {
                            series[0][idx] = point.getX().doubleValue();
                            series[1][idx++] = point.getY().doubleValue();
                        }
                        dataSet.addSeries(range.getId(), series);
                        seriesIdxToRow[seriesCount++] = row;
                    }
                }

                final DropoutEstimation dropoutEstimation = sar.getDropoutEstimation();
                if (dropoutEstimation != null) {
                    intervalMarkers.add(new IntervalMarker(dropoutEstimation.getMinimum().doubleValue(), dropoutEstimation.getMaximum().doubleValue(), Color.BLUE, new BasicStroke(0.1f), Color.BLUE, new BasicStroke(0.5f), 0.1f));
                    legendItemCollection.add(new LegendItem("Dropout Estimation " + dropoutEstimation.getMinimum() + " ~ " + dropoutEstimation.getMaximum(), Color.BLUE));
                }

                panel.setChart(ChartFactory.createXYLineChart("Sensitivity Analysis", "Dropout Probability", "Log 10", dataSet));
                panel.getChart().getXYPlot().setRenderer(new DefaultXYItemRenderer() {
                    @Override
                    public boolean getItemShapeVisible(final int series, final int item) {
                        return item == 0 || item == dataSet.getItemCount(series) - 1 || item == dataSet.getItemCount(series) / 2 ? super.getItemShapeVisible(series, item) : false;
                    }

                    @Override
                    public Paint getSeriesPaint(final int seriesIdx) {
                        switch (((Range) tmodel.getValueAt(seriesIdxToRow[seriesIdx], 1)).getRangeType()) {
                            case LR:
                                return Color.RED;
                            case P_DEFENSE:
                                return Color.BLUE;
                            case P_PROSECUTION:
                                return new Color(0, 200, 0);
                            default:
                                return super.getSeriesPaint(seriesIdx);
                        }
                    }
                });

                final LegendItemSource lis = new LegendItemSource() {
                    @Override
                    public LegendItemCollection getLegendItems() {
                        return legendItemCollection;
                    }
                };

                for (final IntervalMarker marker : intervalMarkers) {
                    panel.getChart().getXYPlot().addDomainMarker(marker);
                }

                panel.getChart().addSubtitle(0, new TextTitle(buildSubTitleString(subTitle)));

                if (panel.getChart().getXYPlot().getDataset().getSeriesCount() == 1) {
                    panel.getChart().removeLegend();
                }
                panel.getChart().addLegend(new LegendTitle(lis));
                panel.getChart().setBorderVisible(false);
                panel.getChart().getXYPlot().setDomainPannable(true);
                panel.getChart().getXYPlot().setRangePannable(true);

                panel.getChart().getXYPlot().setRangeCrosshairVisible(true);
                panel.getChart().getXYPlot().setDomainCrosshairVisible(true);

                panel.getChart().getXYPlot().setDomainGridlinesVisible(true);
                panel.getChart().getXYPlot().setRangeGridlinesVisible(true);
                panel.getChart().getXYPlot().setRangeGridlinePaint(Color.BLACK);
                panel.getChart().getXYPlot().setDomainGridlinePaint(Color.BLACK);
                panel.getChart().getXYPlot().getDomainAxis().setRange(0, 1);
                panel.getChart().getPlot().setBackgroundPaint(ChartColor.WHITE);
                panel.getChart().fireChartChanged();

                if (panel.getChart().getXYPlot().getDataset().getSeriesCount() > 0) {
                    final JFreeChart clone = (JFreeChart) panel.getChart().clone();
                    clone.removeLegend();
                    clone.setSubtitles(new ArrayList());
                    sar.setPreview(clone.createBufferedImage(400, 400, 300, 300, null));
                    sar.setGraphImage(panel.getChart().createBufferedImage(1500, 1500, 500, 500, null));
                }
            }
        }
        catch (final Exception e) {
            LOG.error("Error updating sensitivity plot", e);
        }
    }

    private void sensitivityAnalysisButtonActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sensitivityAnalysisButtonActionPerformed

        final ArrayList<Sample> personsOfInterest = new ArrayList<>();
        final DefaultTableModel model = (DefaultTableModel) sensitivityAnalysisTable.getModel();
        boolean varyDefenseUnknowns = false;
        boolean varyProsecutionUnknowns = false;
        for (int idx = 0; idx < model.getRowCount(); idx++) {
            final Boolean varyDropout = (Boolean) model.getValueAt(idx, 0);
            if (varyDropout) {
                final Object current = model.getValueAt(idx, 1);
                if (current instanceof Sample) {
                    personsOfInterest.add((Sample) current);
                }
                else {
                    if (current.toString().equalsIgnoreCase(PROSECUTION_UNKNOWN_CONTRIBUTORS)) {
                        varyProsecutionUnknowns = true;
                    }
                    else {
                        if (current.toString().equalsIgnoreCase(DEFENSE_UNKNOWN_CONTRIBUTORS)) {
                            varyDefenseUnknowns = true;
                        }
                    }
                }
            }
        }

        LOG.info("Starting Sensitivity Analysis");
        final BigDecimal roundedDropoutTo = new BigDecimal((Double) dropoutTo.getModel().getValue()).round(new MathContext(2));
        final BigDecimal roundedDropoutFrom = new BigDecimal((Double) dropoutFrom.getModel().getValue()).round(new MathContext(2));
        final BigDecimal roundedTheta = new BigDecimal((Double) theta.getModel().getValue()).round(new MathContext(2));
        final BigDecimal roundedDropin = new BigDecimal((Double) sensitivityAnalysisDropin.getModel().getValue()).round(new MathContext(2));

        if (analysisThread != null) {
            analysisThread.interrupt();
        }

        analysisThread = new SensitivityAnalysis(
                session,
                (String) locusComboBox.getSelectedItem(),
                personsOfInterest, roundedDropoutFrom, roundedDropoutTo,
                new BigDecimal((Integer) dropoutSteps.getModel().getValue()), roundedDropin, roundedTheta,
                new SensitivityAnalysisProgressListener(session, this, (String) locusComboBox.getSelectedItem(), personsOfInterest, roundedDropoutFrom, roundedDropoutTo, roundedDropin, roundedTheta, varyDefenseUnknowns, varyProsecutionUnknowns),
                varyDefenseUnknowns,
                varyProsecutionUnknowns);
        analysisThread.start();
    }//GEN-LAST:event_sensitivityAnalysisButtonActionPerformed

    private void sensitivityStopButtonActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sensitivityStopButtonActionPerformed
        if (analysisThread != null && analysisThread.isAlive()) {
            analysisThread.interrupt();
        }
        else {
            session.setApplicationState(APP_STATE.READY_FOR_ANALYSIS);
        }
    }//GEN-LAST:event_sensitivityStopButtonActionPerformed

    private void dropoutStopButtonActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dropoutStopButtonActionPerformed
        if (analysisThread != null && analysisThread.isAlive()) {
            analysisThread.interrupt();
        }
        else {
            session.setApplicationState(APP_STATE.READY_FOR_ANALYSIS);
        }
    }//GEN-LAST:event_dropoutStopButtonActionPerformed

    private void dropoutEstimationButtonActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dropoutEstimationButtonActionPerformed
        final ArrayList<Sample> personsOfInterest = new ArrayList<>();
        final DefaultTableModel sampleTableModel = (DefaultTableModel) sensitivityAnalysisTable.getModel();
        boolean varyDefenseUnknowns = false;
        boolean varyProsecutionUnknowns = false;

        for (int idx = 0; idx < sampleTableModel.getRowCount(); idx++) {
            final Boolean varyDropout = (Boolean) sampleTableModel.getValueAt(idx, 0);
            if (varyDropout) {
                final Object current = sampleTableModel.getValueAt(idx, 1);
                if (current instanceof Sample) {
                    personsOfInterest.add((Sample) current);
                }
                else {
                    if (current.toString().equalsIgnoreCase(PROSECUTION_UNKNOWN_CONTRIBUTORS)) {
                        varyProsecutionUnknowns = true;
                    }
                    else {
                        if (current.toString().equalsIgnoreCase(DEFENSE_UNKNOWN_CONTRIBUTORS)) {
                            varyDefenseUnknowns = true;
                        }
                    }
                }
            }
        }

        final BigDecimal roundedDropoutTo = new BigDecimal((Double) dropoutEstimationTo.getModel().getValue()).round(new MathContext(2));
        final BigDecimal roundedDropoutFrom = new BigDecimal((Double) dropoutEstimationFrom.getModel().getValue()).round(new MathContext(2));
        final BigDecimal roundedDropin = new BigDecimal((Double) dropoutEstimationDropin.getModel().getValue()).round(new MathContext(2));

        if (analysisThread != null && analysisThread.isAlive()) {
            analysisThread.interrupt();
        }

        LOG.info("Starting Dropout estimation");
        analysisThread = new DropoutEstimator(
                session,
                personsOfInterest,
                roundedDropoutFrom,
                roundedDropoutTo,
                new BigDecimal((Integer) dropoutEstimationSteps.getModel().getValue()),
                roundedDropin,
                new DropoutEstimationProgressListener(session, this, personsOfInterest, roundedDropoutFrom, roundedDropoutTo, roundedDropin, varyProsecutionUnknowns, varyDefenseUnknowns),
                (Integer) dropoutEstimationIterations.getValue(),
                varyDefenseUnknowns,
                varyProsecutionUnknowns);
        analysisThread.start();
    }//GEN-LAST:event_dropoutEstimationButtonActionPerformed

    private void setDropoutButtonActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setDropoutButtonActionPerformed

        final DefaultTableModel sampleTableModel = (DefaultTableModel) sensitivityAnalysisTable.getModel();
        final Double newDropout = (Double) setDropoutSpinner.getValue();
        final Hypothesis defense = session.getDefense();
        final Hypothesis prosecution = session.getProsecution();

        final ArrayList<Integer> variedIndices = new ArrayList<>();

        for (int idx = 0; idx < sampleTableModel.getRowCount(); idx++) {
            final Boolean varyDropout = (Boolean) sampleTableModel.getValueAt(idx, 0);
            if (varyDropout) {
                variedIndices.add(idx);
                final Object current = sampleTableModel.getValueAt(idx, 1);
                if (current instanceof Sample) {
                    defense.getContributor((Sample) current).setDropoutProbability(newDropout);
                    prosecution.getContributor((Sample) current).setDropoutProbability(newDropout);
                }
                else {
                    if (current.toString().equalsIgnoreCase(PROSECUTION_UNKNOWN_CONTRIBUTORS)) {
                        prosecution.setUnknownDropoutProbability(newDropout);
                    }
                    else {
                        if (current.toString().equalsIgnoreCase(DEFENSE_UNKNOWN_CONTRIBUTORS)) {
                            defense.setUnknownDropoutProbability(newDropout);
                        }
                    }
                }
            }
        }

        if (!variedIndices.isEmpty()) {
            session.setDefense(defense);
            session.setProsecution(prosecution);
            setDropoutStatusLabel.setText("Dropout set for selected profiles.");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(500);
                        for (int idx = 0; idx < sampleTableModel.getRowCount(); idx++) {
                            sampleTableModel.setValueAt(variedIndices.contains(idx), idx, 0);
                        }
                        Thread.sleep(2000);
                        setDropoutStatusLabel.setText("");
                    }
                    catch (final InterruptedException ex) {
                    }
                }
            }).start();
        }
    }//GEN-LAST:event_setDropoutButtonActionPerformed
    private javax.swing.JButton dropoutEstimationButton;
    private javax.swing.JSpinner dropoutEstimationDropin;
    private javax.swing.JSpinner dropoutEstimationFrom;
    private javax.swing.JSpinner dropoutEstimationIterations;
    private javax.swing.JProgressBar dropoutEstimationProgressBar;
    private javax.swing.JSpinner dropoutEstimationSteps;
    private javax.swing.JLabel dropoutEstimationTimeLeft;
    private javax.swing.JSpinner dropoutEstimationTo;
    private javax.swing.JSpinner dropoutFrom;
    private javax.swing.JSpinner dropoutSteps;
    private javax.swing.JButton dropoutStopButton;
    private javax.swing.JSpinner dropoutTo;
    private javax.swing.JLabel iconLabel;
    private javax.swing.JLabel stepsAtLabel;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel graphControlPanel;
    private javax.swing.JPanel topPanel;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JSplitPane chartSplitPane;
    private javax.swing.JTable jTable2;
    private javax.swing.JComboBox locusComboBox;
    private javax.swing.JPanel plotPanel;
    private javax.swing.JButton sensitivityAnalysisButton;
    private javax.swing.JSpinner sensitivityAnalysisDropin;
    private javax.swing.JTable sensitivityAnalysisTable;
    private javax.swing.JProgressBar sensitivityProgressBar;
    private javax.swing.JButton sensitivityStopButton;
    private javax.swing.JLabel sensitivityTimeLeft;
    private javax.swing.JSpinner setDropoutSpinner;
    private javax.swing.JLabel setDropoutStatusLabel;
    private javax.swing.JSpinner theta;
    private JTable _rangeTable;
    private JScrollPane _rangeTableScrollPane;
    private boolean _triggerGraphUpdate;
    private boolean _triggerDropoutUpdate;
    private JTabbedPane settingsTabbedPane;
    private JSplitPane splitPane;
    // End of variables declaration//GEN-END:variables

    @Override
    public void dataChanged(final ConfigurationDataElement element) {
        LOG.debug("dataChanged({})", element);
        switch (element) {
            case STATISTICS:
                // Trigger a refresh of the main window's tab control
                session.setApplicationState(session.getApplicationState());
                break;
            case DEFENSE:
            case PROSECUTION:
            case ACTIVEPROFILES:
            case ACTIVEREPLICATES:
            case PROFILES:
            case REPLICATES:
                updateSettings();
                locusComboBox.removeAllItems();
                locusComboBox.addItem("All Loci");
                for (final String locusName : session.getEnabledLoci()) {
                    locusComboBox.addItem(locusName);
                }
                break;
        }
    }

    @Override
    public synchronized void updateGraph(final Collection<SensitivityAnalysisResults.Point> points) {
        if (session.getApplicationState() != APP_STATE.READY_FOR_ANALYSIS) {
            final ChartPanel panel = (ChartPanel) plotPanel;
            final DefaultXYDataset dataSet = (DefaultXYDataset) panel.getChart().getXYPlot().getDataset();
            int idx = 0;
            final double[][] series = new double[2][points.size()];
            for (final SensitivityAnalysisResults.Point point : points) {
                series[0][idx] = point.getX().doubleValue();
                series[1][idx++] = point.getY().doubleValue();
            }
            synchronized (dataSet) {
                dataSet.removeSeries("Current Analysis");
                dataSet.addSeries("Current Analysis", series);
            }
            panel.setChart(ChartFactory.createXYLineChart("Sensitivity Analysis", "Dropout Probability", "Log 10", dataSet));
            panel.getChart().getXYPlot().setRenderer(new DefaultXYItemRenderer() {
                @Override
                public Paint getSeriesPaint(final int series) {
                    synchronized (dataSet) {
                        return getPlot().getDataset().getSeriesKey(series).equals("Current Analysis") ? Color.RED : Color.LIGHT_GRAY;
                    }
                }

                @Override
                public boolean getItemShapeVisible(final int series, final int item) {
                    synchronized (dataSet) {
                        return item == 0 || item == dataSet.getItemCount(series) - 1 ? super.getItemShapeVisible(series, item) : false;
                    }
                }
            });

            panel.getChart().setBorderVisible(false);

            final NumberAxis domain = (NumberAxis) panel.getChart().getXYPlot().getDomainAxis();
            domain.setRange(0.00, 1.00);
            domain.setTickUnit(new NumberTickUnit(0.1));
            domain.setVerticalTickLabels(true);

            panel.getChart().getXYPlot().setDomainGridlinesVisible(true);
            panel.getChart().getXYPlot().setRangeGridlinesVisible(true);
            panel.getChart().getXYPlot().setRangeGridlinePaint(Color.BLACK);
            panel.getChart().getXYPlot().setDomainGridlinePaint(Color.BLACK);
            panel.getChart().getXYPlot().getDomainAxis().setRange(0, 1);

            panel.getChart().getPlot().setBackgroundPaint(ChartColor.WHITE);
            panel.getChart().fireChartChanged();
        }
    }

    public void plotResults() {
        try {
            if (session.getStatistics() == null) {
                return;
            }
            final AnalysisReport currentReport = session.getCurrentReport();
            if (currentReport != null) {
                final SensitivityAnalysisResults sar = session.getCurrentReport().getSensitivityAnalysisResults();
                List<String> subTitle = null;
                for (final SensitivityAnalysisResults.Range range : sar.getRanges()) {
                    subTitle = extractSubTitle(subTitle, range.getDescription());
                }

                final DefaultTableModel tmodel = (DefaultTableModel) _rangeTable.getModel();
                tmodel.setRowCount(0);

                for (final SensitivityAnalysisResults.Range range : sar.getRanges()) {
                    int idx = 0;
                    final double[][] series = new double[2][range.getPoints().size()];
                    for (final SensitivityAnalysisResults.Point point : range.getPoints()) {
                        series[0][idx] = point.getX().doubleValue();
                        series[1][idx++] = point.getY().doubleValue();
                    }
                    final String seriesName = extractSeriesName(subTitle, range.getDescription());
                    if (range.getRangeType() == RangeType.LR) {
                        tmodel.addRow(new Object[]{true, range});
                    }
                    else {
                        tmodel.addRow(new Object[]{false, range});
                    }
                    range.setId(seriesName);
                }
                _rangeTable.setModel(tmodel);

                updateGraph();
            }
        }
        catch (final Exception e) {
            LOG.error("Error updating sensitivity plot", e);
        }
    }

    @Override
    public void applicationStateChanged(final APP_STATE newState) {
        switch (newState) {
            case WAIT_SAMPLE:
                setDefaults();
                setEnabled(false);
                break;
            case SENSITIVITY_ANALYSIS_RUNNING:
                setEnabled(false);
                enableControls(false);
                sensitivityTimeLeft.setVisible(true);
                sensitivityStopButton.setEnabled(true);
                sensitivityAnalysisButton.setEnabled(false);
                _triggerGraphUpdate = true;
                break;
            case DROPOUT_ESTIMATION_RUNNING:
                setEnabled(false);
                enableControls(false);
                dropoutEstimationProgressBar.setVisible(true);
                dropoutEstimationTimeLeft.setVisible(true);
                dropoutStopButton.setEnabled(true);
                _triggerDropoutUpdate = true;
                break;
            case READY_FOR_ANALYSIS:
                if (session.getStatistics() != null) {
                    setEnabled(true);
                    enableControls(true);
                    sensitivityProgressBar.setVisible(false);
                    dropoutEstimationProgressBar.setVisible(false);
                    dropoutEstimationTimeLeft.setVisible(false);
                    sensitivityTimeLeft.setVisible(false);
                    sensitivityStopButton.setEnabled(false);
                    sensitivityAnalysisButton.setEnabled(true);
                    dropoutEstimationButton.setEnabled(true);
                    dropoutStopButton.setEnabled(false);
                    dropoutEstimationButton.setText("Run Dropout Estimation for " + session.getObservedAlleleCount() + " alleles");
                    if (_triggerGraphUpdate) {
                        _triggerGraphUpdate = false;
                        plotResults();
                    }
                    if (_triggerDropoutUpdate) {
                        _triggerDropoutUpdate = false;
                        updateDropoutRange();
                    }
                }
                break;
            default:
                setEnabled(false);
                break;
        }
    }

    private void updateDropoutRange() {
        try {
            final AnalysisReport currentReport = session.getCurrentReport();
            if (currentReport != null) {
                final SensitivityAnalysisResults sar = session.getCurrentReport().getSensitivityAnalysisResults();
                final ChartPanel panel = (ChartPanel) plotPanel;
                final LegendItemCollection legendItemCollection = new LegendItemCollection();
                final DropoutEstimation dropoutEstimation = sar.getDropoutEstimation();

                if (dropoutEstimation != null) {
                    panel.getChart().getXYPlot().clearDomainMarkers();
                    panel.getChart().getXYPlot().addDomainMarker(new IntervalMarker(dropoutEstimation.getMinimum().doubleValue(), dropoutEstimation.getMaximum().doubleValue(), Color.BLUE, new BasicStroke(0.1f), Color.BLUE, new BasicStroke(0.5f), 0.1f));
                    legendItemCollection.add(new LegendItem("Dropout Estimation " + dropoutEstimation.getMinimum() + " ~ " + dropoutEstimation.getMaximum(), Color.BLUE));
                }

                final LegendItemSource lis = new LegendItemSource() {
                    @Override
                    public LegendItemCollection getLegendItems() {
                        return legendItemCollection;
                    }
                };

                panel.getChart().removeLegend();
                panel.getChart().addLegend(new LegendTitle(lis));
                panel.getChart().fireChartChanged();

                if (panel.getChart().getXYPlot().getDataset().getSeriesCount() > 0) {
                    final JFreeChart clone = (JFreeChart) panel.getChart().clone();
                    clone.removeLegend();
                    clone.setSubtitles(new ArrayList());
                    sar.setPreview(clone.createBufferedImage(400, 400, 300, 300, null));
                    sar.setGraphImage(panel.getChart().createBufferedImage(1500, 1500, 500, 500, null));
                }
            }
        }
        catch (final Exception e) {
            LOG.error("Error updating sensitivity plot", e);
        }
    }

    private void setDefaults() {
        dropoutFrom.setValue(DEFAULT_DROPOUT_FROM);
        dropoutTo.setValue(DEFAULT_DROPOUT_TO);
        dropoutSteps.setValue(DEFAULT_DROPOUT_STEPS);

        dropoutEstimationFrom.setValue(DEFAULT_DROPOUT_FROM);
        dropoutEstimationTo.setValue(DEFAULT_DROPOUT_TO);
        dropoutEstimationSteps.setValue(DEFAULT_DROPOUT_ESTIMATION_STEPS);
        dropoutEstimationIterations.setValue(DEFAULT_DROPOUT_ESTMATION_ITERATIONS);
        if (locusComboBox.getItemCount() > 0)
            locusComboBox.setSelectedIndex(0);
        setDropoutSpinner.setValue(0.00d);
    }

    private void enableControls(final boolean b) {
        enableComponent(this, b);
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

    private String formatTime(final long ms) {
        final int hours = Math.abs((int) (ms / 3600000));
        final int minutes = Math.abs((int) ((ms / 1000 - hours * 3600) / 60));
        final int seconds = Math.abs((int) ((ms / 1000 - hours * 3600) % 60));
        return String.format("%1$02d:%2$02d:%3$02d", hours, minutes, seconds);
    }

    @Override
    public void setSensitivityProgress(final int currentPercentage, final int overallPercentage) {
        if (sensitivityProgressBar.isVisible()) {
            sensitivityProgressBar.setMaximum(100);
            sensitivityProgressBar.setValue(currentPercentage);
        }
        session.setProgress(overallPercentage);
    }

    @Override
    public void setDropoutProgress(final int percentage) {
        dropoutEstimationProgressBar.setMaximum(100);
        dropoutEstimationProgressBar.setValue(percentage);
        dropoutEstimationProgressBar.setStringPainted(true);
        session.setProgress(percentage);
    }

    @Override
    public void setSensitivityTimeLeft(final long current, final long overall) {
        sensitivityTimeLeft.setText((sensitivityProgressBar.isVisible() ? formatTime(current) + " / " : "") + (formatTime(overall)));
    }

    @Override
    public void setDropoutTimeLeft(final long timeLeft) {
        dropoutEstimationTimeLeft.setText(timeLeft > 0 ? formatTime(timeLeft) : "");
    }
}
