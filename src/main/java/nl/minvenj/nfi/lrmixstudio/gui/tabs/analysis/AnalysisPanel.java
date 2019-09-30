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

import static nl.minvenj.nfi.lrmixstudio.gui.ApplicationStateChangeListener.APP_STATE.READY_FOR_ANALYSIS;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.minvenj.nfi.lrmixstudio.domain.Contributor;
import nl.minvenj.nfi.lrmixstudio.domain.Hypothesis;
import nl.minvenj.nfi.lrmixstudio.domain.LikelihoodRatio;
import nl.minvenj.nfi.lrmixstudio.domain.Ratio;
import nl.minvenj.nfi.lrmixstudio.domain.Relatedness.Relation;
import nl.minvenj.nfi.lrmixstudio.domain.Sample;
import nl.minvenj.nfi.lrmixstudio.gui.AlleleFrequencyDocumentListener;
import nl.minvenj.nfi.lrmixstudio.gui.ApplicationSettings;
import nl.minvenj.nfi.lrmixstudio.gui.ApplicationStateChangeListener;
import nl.minvenj.nfi.lrmixstudio.gui.ProgressGui;
import nl.minvenj.nfi.lrmixstudio.gui.SessionData;
import nl.minvenj.nfi.lrmixstudio.gui.ZebraTable;
import nl.minvenj.nfi.lrmixstudio.model.AnalysisReport;
import nl.minvenj.nfi.lrmixstudio.model.ConfigurationDataChangeListener;
import nl.minvenj.nfi.lrmixstudio.model.ConfigurationDataElement;
import nl.minvenj.nfi.lrmixstudio.model.LRMathModel;
import nl.minvenj.nfi.lrmixstudio.model.LRMathModelFactory;

/**
 *
 * @author dejong
 */
public class AnalysisPanel extends javax.swing.JPanel implements ProgressGui, ConfigurationDataChangeListener, ApplicationStateChangeListener {

    private static final Logger LOG = LoggerFactory.getLogger(AnalysisPanel.class);
    private SessionData _session;
    private LRMathModel _mathematicalModel;
    private AnalysisGuiListener _listener;

    /**
     * A Generic listener for the GUI components on the AnalysisPanel
     */
    private class AnalysisGuiListener implements TableModelListener, ChangeListener, ActionListener {

        SessionData session;
        private boolean _suspended;

        public AnalysisGuiListener(final SessionData session) {
            this.session = session;
        }

        @Override
        public void tableChanged(final TableModelEvent e) {
            doChange();
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            doChange();
        }

        @Override
        public void stateChanged(final ChangeEvent e) {
            doChange();
        }

        private void doChange() {
            if (!_suspended) {
                boolean enabled = !_prosecutionUnknowns.getValue().toString().equals("0");
                prosecutionUnknownDropoutProbability.setEnabled(enabled);
                prosecutionUnknownDropoutLabel.setEnabled(enabled);

                enabled = !_defenseUnknowns.getValue().toString().equals("0");
                defenseUnknownDropoutProbability.setEnabled(enabled);
                defenseUnknownDropoutLabel.setEnabled(enabled);
                relatedUnknownCheckBox.setEnabled(enabled);
                relatedToCombo.setEnabled(enabled && relatedUnknownCheckBox.isSelected());
                relatedToLabel.setEnabled(enabled && relatedUnknownCheckBox.isSelected());
                relationCombo.setEnabled(enabled && relatedUnknownCheckBox.isSelected());

                try {
                    if (_rareAlleleFrequencyErrorMessage.getText().isEmpty()) {
                        session.setRareAlleleFrequency(Double.parseDouble(_rareAlleleFrequency.getText()));
                    }
                }
                catch (final NumberFormatException nfe) {
                    LOG.debug("Rare Allele Frequency is not a valid number!", nfe);
                }
                session.setMathematicalModelName(LRMathModelFactory.getDefaultModelName());
                session.setThreadCount(((Number) threadSpinner.getModel().getValue()).intValue());
                session.setProsecution(buildHypothesis("Prosecution", prosecutionContributors, _prosecutionUnknowns, prosecutionUnknownDropoutProbability));
                session.setDefense(buildHypothesis("Defense", defenseContributors, _defenseUnknowns, defenseUnknownDropoutProbability, relatedUnknownCheckBox, relationCombo, relatedToCombo));
            }
        }

        public void suspend() {
            _suspended = true;
        }

        public void resume() {
            _suspended = false;
            doChange();
        }
    }

    /**
     * Creates new form AnalysisPanel
     */
    public AnalysisPanel() {
        initComponents();

        // This ensures that when we start/stop the analysis, the fact that the progress bar and information lines become
        // (in)visible does not cause the containing box to resize.
        ((GroupLayout) analysisProgressPanel.getLayout()).setHonorsVisibility(false);

        relationCombo.addItem(Relation.PARENT_CHILD);
        relationCombo.addItem(Relation.SIBLING);
        relationCombo.addItem(Relation.HALF_SIBLING);
        relationCombo.addItem(Relation.GRANDPARENT_GRANDCHILD);
        relationCombo.addItem(Relation.AUNT_UNCLE_NIECE_NEPHEW);
        relationCombo.addItem(Relation.COUSIN);

        _validationModeCheckBox.setVisible(ApplicationSettings.isAdvancedMode());

        final UndoManager undoManager = new UndoManager();
        _rareAlleleFrequency.getDocument().addUndoableEditListener(undoManager);

        _rareAlleleFrequency.getActionMap().put("Undo", new AbstractAction("Undo") {
            @Override
            public void actionPerformed(final ActionEvent evt) {
                try {
                    if (undoManager.canUndo()) {
                        undoManager.undo();
                    }
                }
                catch (final CannotUndoException e) {
                }
            }
        });

        _rareAlleleFrequency.getActionMap().put("Redo", new AbstractAction("Redo") {
            @Override
            public void actionPerformed(final ActionEvent evt) {
                try {
                    if (undoManager.canRedo()) {
                        undoManager.redo();
                    }
                }
                catch (final CannotRedoException e) {
                }
            }
        });

        _rareAlleleFrequency.getInputMap().put(KeyStroke.getKeyStroke("control Z"), "Undo");
        _rareAlleleFrequency.getInputMap().put(KeyStroke.getKeyStroke("control Y"), "Redo");

        analysisProgressBar.setVisible(false);
        timeSpentLabel.setVisible(false);
        timeLeftLabel.setVisible(false);
    }

    public void setContext(final SessionData session) {
        _session = session;
        _session.addDataChangeListener(this);
        _session.addStateChangeListener(this);
        _session.setMathematicalModelName(LRMathModelFactory.getDefaultModelName());
        _session.setThreadCount(Runtime.getRuntime().availableProcessors());

        threadSpinner.setValue(_session.getThreadCount());
        alleleFrequenciesFileName.getDocument().addDocumentListener(new AlleleFrequencyDocumentListener(_session, alleleFrequenciesFileName, this));
        alleleFrequenciesFileName.setText(ApplicationSettings.getAlleleFrequenciesPath());

        setRareAlleleFrequency();

        _validationModeCheckBox.setSelected(ApplicationSettings.isValidationMode());

        _rareAlleleFrequency.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(final DocumentEvent e) {
                verify();
            }

            @Override
            public void removeUpdate(final DocumentEvent e) {
                verify();
            }

            @Override
            public void changedUpdate(final DocumentEvent e) {
                verify();
            }

            private void verify() {
                try {
                    final double d = Double.parseDouble(_rareAlleleFrequency.getText());
                    if (d < 0) {
                        _rareAlleleFrequencyErrorMessage.setText("<html><b><font color=red>Value must be &gt;= zero!");
                        startAnalysisButton.setEnabled(false);
                        return;
                    }
                    if (d >= 1) {
                        _rareAlleleFrequencyErrorMessage.setText("<html><b><font color=red>Value must be &lt; one!");
                        startAnalysisButton.setEnabled(false);
                        return;
                    }
                    _rareAlleleFrequencyErrorMessage.setText("");
                    _session.setRareAlleleFrequency(d);
                    enableStartButton();
                }
                catch (final NumberFormatException nfe) {
                    _rareAlleleFrequencyErrorMessage.setText("<html><b><font color=red>This is not a valid number!");
                    startAnalysisButton.setEnabled(false);
                }
            }
        });

        _listener = new AnalysisGuiListener(_session);
        prosecutionContributors.getModel().addTableModelListener(_listener);
        prosecutionContributors.setRowSelectionAllowed(false);
        prosecutionContributors.getColumn("Dropout Probability").setCellEditor(new DropoutProbabilityCellEditor(_listener));
        prosecutionContributors.getColumn("Dropout Probability").setCellRenderer(new DropoutProbabilityCellRenderer());
        prosecutionContributors.addFocusListener(new ContributorTableFocusListener());

        defenseContributors.getModel().addTableModelListener(_listener);
        defenseContributors.setRowSelectionAllowed(false);
        defenseContributors.getColumn("Dropout Probability").setCellEditor(new DropoutProbabilityCellEditor(_listener));
        defenseContributors.getColumn("Dropout Probability").setCellRenderer(new DropoutProbabilityCellRenderer());
        defenseContributors.addFocusListener(new ContributorTableFocusListener());

        prosecutionUnknownDropoutProbability.addChangeListener(_listener);
        defenseUnknownDropoutProbability.addChangeListener(_listener);
        _prosecutionUnknowns.addChangeListener(_listener);
        _defenseUnknowns.addChangeListener(_listener);
        _dropInProbability.addChangeListener(_listener);
        _thetaCorrection.addChangeListener(_listener);
        _rareAlleleFrequency.addActionListener(_listener);
        threadSpinner.addChangeListener(_listener);

        final DropTarget dropTarget = new DropTarget(alleleFrequenciesFileName, new DropTargetAdapter() {
            @Override
            public void drop(final DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(dtde.getDropAction());
                    final Collection<File> droppedFiles = (Collection<File>) dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    alleleFrequenciesFileName.setText(droppedFiles.iterator().next().getAbsolutePath());
                }
                catch (UnsupportedFlavorException | IOException ex) {
                    LOG.info("Error reading dropped files", ex);
                }
            }
        });
    }

    private void setRareAlleleFrequency() {
        final BigDecimal freq = new BigDecimal(ApplicationSettings.getRareAlleleFrequency(_session.getStatistics()));
        _rareAlleleFrequency.setText(freq.toPlainString());
    }

    /**
     * Updates a table to reflect the contributors of the supplied hypothesis
     *
     * @param hypothesis       The hypothesis to use as source of the data
     * @param contributorTable The table to reflect the state of the hypothesis
     *                         contributors
     */
    private void updateTable(final Hypothesis hypothesis, final JTable table) {
        final DefaultTableModel model = (DefaultTableModel) table.getModel();

        // Clear all checkboxes
        for (int idx = 0; idx < model.getRowCount(); idx++) {
            model.setValueAt(false, idx, 0);
        }

        for (final Contributor contributor : hypothesis.getContributors()) {
            for (int idx = 0; idx < model.getRowCount(); idx++) {
                if (model.getValueAt(idx, 1).toString().equalsIgnoreCase(contributor.getSample().getId())) {
                    model.setValueAt(true, idx, 0);
                    model.setValueAt(contributor.getDropoutProbability(), idx, 2);
                    break;
                }
            }
        }
    }

    /**
     * Resets the GUI controls that define the hypotheses.
     */
    private void resetHypotheses() {
        _listener.suspend();
        _dropInProbability.setValue(0.05);
        _thetaCorrection.setValue(0.01);
        ((DefaultTableModel) prosecutionContributors.getModel()).setRowCount(0);
        _prosecutionUnknowns.setValue(0);
        prosecutionUnknownDropoutProbability.setValue(0.1);
        ((DefaultTableModel) defenseContributors.getModel()).setRowCount(0);
        _defenseUnknowns.setValue(0);
        defenseUnknownDropoutProbability.setValue(0.1);
        _listener.resume();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        defenseGroup = new javax.swing.JPanel();
        jScrollPane7 = new javax.swing.JScrollPane();
        defenseContributors = new ZebraTable();
        defenseUnknownsLabel = new javax.swing.JLabel();
        _defenseUnknowns = new javax.swing.JSpinner();
        defenseUnknownDropoutLabel = new javax.swing.JLabel();
        defenseUnknownDropoutProbability = new javax.swing.JSpinner();
        relatedUnknownCheckBox = new javax.swing.JCheckBox();
        relationCombo = new javax.swing.JComboBox<Relation>();
        relatedToLabel = new javax.swing.JLabel();
        relatedToCombo = new javax.swing.JComboBox<Sample>();
        resultsGroup = new javax.swing.JPanel();
        jScrollPane9 = new javax.swing.JScrollPane();
        resultsTable = new ZebraTable();
        jLabel11 = new javax.swing.JLabel();
        overallLikelyhoodRatio = new javax.swing.JTextField();
        analysisProgressPanel = new javax.swing.JPanel();
        timeSpentLabel = new javax.swing.JLabel();
        analysisProgressBar = new javax.swing.JProgressBar();
        timeLeftLabel = new javax.swing.JLabel();
        timeSpent = new javax.swing.JLabel();
        timeRemaining = new javax.swing.JLabel();
        startAnalysisButton = new javax.swing.JButton();
        stopAnalysis = new javax.swing.JButton();
        showLogButton = new javax.swing.JButton();
        prosecutionGroup = new javax.swing.JPanel();
        jScrollPane10 = new javax.swing.JScrollPane();
        prosecutionContributors = new ZebraTable();
        prosecutionUnknownsLabel = new javax.swing.JLabel();
        _prosecutionUnknowns = new javax.swing.JSpinner();
        prosecutionUnknownDropoutLabel = new javax.swing.JLabel();
        prosecutionUnknownDropoutProbability = new javax.swing.JSpinner();
        parametersGroup = new javax.swing.JPanel();
        alleleFrequenciesLabel = new javax.swing.JLabel();
        alleleFrequenciesFileName = new javax.swing.JTextField();
        browseAlleleFrequencies = new javax.swing.JButton();
        dropInProbabilityLabel = new javax.swing.JLabel();
        _dropInProbability = new javax.swing.JSpinner();
        thetaCorrectionLabel = new javax.swing.JLabel();
        _thetaCorrection = new javax.swing.JSpinner();
        _validationModeCheckBox = new javax.swing.JCheckBox();
        threadSpinner = new javax.swing.JSpinner();
        _maxThreadsLabel = new javax.swing.JLabel();
        _rareAlleleFrequencyLabel = new javax.swing.JLabel();
        _rareAlleleFrequency = new javax.swing.JTextField();
        _rareAlleleFrequencyErrorMessage = new javax.swing.JLabel();

        setBackground(new java.awt.Color(0, 0, 153));

        defenseGroup.setBackground(new java.awt.Color(249, 249, 249));
        defenseGroup.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1), "Defense Hypothesis", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.BELOW_TOP, new java.awt.Font("Vijaya", 0, 18))); // NOI18N

        defenseContributors.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Contributor", "ID", "Dropout Probability"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Boolean.class, java.lang.Object.class, java.lang.Double.class
            };
            boolean[] canEdit = new boolean [] {
                true, false, true
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
        defenseContributors.setFillsViewportHeight(true);
        defenseContributors.setName("defenseContributors"); // NOI18N
        defenseContributors.setRowHeight(20);
        defenseContributors.getTableHeader().setReorderingAllowed(false);
        jScrollPane7.setViewportView(defenseContributors);
        if (defenseContributors.getColumnModel().getColumnCount() > 0) {
            defenseContributors.getColumnModel().getColumn(0).setPreferredWidth(80);
            defenseContributors.getColumnModel().getColumn(2).setPreferredWidth(120);
        }

        defenseUnknownsLabel.setText("Unknown Contributors");

        _defenseUnknowns.setModel(new javax.swing.SpinnerNumberModel(0, 0, 4, 1));
        _defenseUnknowns.setToolTipText("The number of unknown contributors to the sample according to the defense hypothesis");

        defenseUnknownDropoutLabel.setText("Dropout Probability for unknowns");
        defenseUnknownDropoutLabel.setEnabled(false);

        defenseUnknownDropoutProbability.setModel(new javax.swing.SpinnerNumberModel(0.1d, 0.0d, 1.0d, 0.01d));
        defenseUnknownDropoutProbability.setEnabled(false);

        relatedUnknownCheckBox.setText("One of the unknowns is a relative");
        relatedUnknownCheckBox.setOpaque(false);
        relatedUnknownCheckBox.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final java.awt.event.ActionEvent evt) {
                relatedUnknownCheckBoxActionPerformed(evt);
            }
        });

        relationCombo.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final java.awt.event.ActionEvent evt) {
                relationComboActionPerformed(evt);
            }
        });

        relatedToLabel.setText("of");

        relatedToCombo.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final java.awt.event.ActionEvent evt) {
                relatedToComboActionPerformed(evt);
            }
        });

        final javax.swing.GroupLayout defenseGroupLayout = new javax.swing.GroupLayout(defenseGroup);
        defenseGroup.setLayout(defenseGroupLayout);
        defenseGroupLayout.setHorizontalGroup(
            defenseGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(defenseGroupLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(defenseGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(defenseGroupLayout.createSequentialGroup()
                        .addComponent(relatedUnknownCheckBox)
                        .addGap(55, 55, 55))
                    .addGroup(defenseGroupLayout.createSequentialGroup()
                        .addGroup(defenseGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(defenseGroupLayout.createSequentialGroup()
                                .addGroup(defenseGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(defenseUnknownsLabel)
                                    .addComponent(defenseUnknownDropoutLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(defenseGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(_defenseUnknowns, javax.swing.GroupLayout.DEFAULT_SIZE, 62, Short.MAX_VALUE)
                                    .addComponent(defenseUnknownDropoutProbability)))
                            .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addGroup(defenseGroupLayout.createSequentialGroup()
                                .addComponent(relationCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(relatedToLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(relatedToCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addContainerGap())))
        );
        defenseGroupLayout.setVerticalGroup(
            defenseGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(defenseGroupLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 117, Short.MAX_VALUE)
                .addGap(4, 4, 4)
                .addGroup(defenseGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(defenseUnknownsLabel)
                    .addComponent(_defenseUnknowns, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(4, 4, 4)
                .addGroup(defenseGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(defenseUnknownDropoutLabel)
                    .addComponent(defenseUnknownDropoutProbability, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(relatedUnknownCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(defenseGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(relationCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(relatedToLabel)
                    .addComponent(relatedToCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        resultsGroup.setBackground(new java.awt.Color(252, 252, 252));
        resultsGroup.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1), "Results", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.BELOW_TOP, new java.awt.Font("Vijaya", 0, 18))); // NOI18N

        resultsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Locus", "LR"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false
            };

            @Override
            public boolean isCellEditable(final int rowIndex, final int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        resultsTable.setFillsViewportHeight(true);
        resultsTable.setName("analysisResults"); // NOI18N
        jScrollPane9.setViewportView(resultsTable);
        if (resultsTable.getColumnModel().getColumnCount() > 0) {
            resultsTable.getColumnModel().getColumn(0).setPreferredWidth(15);
        }

        jLabel11.setText("Overall Likelihood Ratio");

        overallLikelyhoodRatio.setEditable(false);
        overallLikelyhoodRatio.setFont(new java.awt.Font("Vijaya", 0, 18)); // NOI18N

        analysisProgressPanel.setOpaque(false);

        timeSpentLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        timeSpentLabel.setText("Processing Time");

        analysisProgressBar.setMaximum(1000);

        timeLeftLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        timeLeftLabel.setText("Estimated Time Remaining");

        timeSpent.setBackground(new java.awt.Color(153, 153, 153));
        timeSpent.setFont(timeSpent.getFont().deriveFont(timeSpent.getFont().getSize()+3f));
        timeSpent.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        timeSpent.setText("00:00:00");

        timeRemaining.setBackground(new java.awt.Color(102, 102, 102));
        timeRemaining.setFont(timeRemaining.getFont().deriveFont(timeRemaining.getFont().getSize()+3f));
        timeRemaining.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        timeRemaining.setText("00:00:00");

        final javax.swing.GroupLayout analysisProgressPanelLayout = new javax.swing.GroupLayout(analysisProgressPanel);
        analysisProgressPanel.setLayout(analysisProgressPanelLayout);
        analysisProgressPanelLayout.setHorizontalGroup(
            analysisProgressPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(analysisProgressPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(analysisProgressPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(analysisProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, analysisProgressPanelLayout.createSequentialGroup()
                        .addGroup(analysisProgressPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(timeSpentLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(timeLeftLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(analysisProgressPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(timeSpent, javax.swing.GroupLayout.DEFAULT_SIZE, 72, Short.MAX_VALUE)
                            .addComponent(timeRemaining, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        analysisProgressPanelLayout.setVerticalGroup(
            analysisProgressPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(analysisProgressPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(analysisProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(analysisProgressPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(timeSpentLabel)
                    .addComponent(timeSpent))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(analysisProgressPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(timeLeftLabel)
                    .addComponent(timeRemaining))
                .addContainerGap(54, Short.MAX_VALUE))
        );

        startAnalysisButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/play16.png"))); // NOI18N
        startAnalysisButton.setText("Run");
        startAnalysisButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final java.awt.event.ActionEvent evt) {
                startAnalysisButtonActionPerformed(evt);
            }
        });

        stopAnalysis.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/stop16.png"))); // NOI18N
        stopAnalysis.setText("Stop");
        stopAnalysis.setEnabled(false);
        stopAnalysis.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final java.awt.event.ActionEvent evt) {
                stopAnalysisActionPerformed(evt);
            }
        });

        showLogButton.setText("Show log");
        showLogButton.setEnabled(false);
        showLogButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final java.awt.event.ActionEvent evt) {
                showLogButtonActionPerformed(evt);
            }
        });

        final javax.swing.GroupLayout resultsGroupLayout = new javax.swing.GroupLayout(resultsGroup);
        resultsGroup.setLayout(resultsGroupLayout);
        resultsGroupLayout.setHorizontalGroup(
            resultsGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(resultsGroupLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jScrollPane9, javax.swing.GroupLayout.DEFAULT_SIZE, 479, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(resultsGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, resultsGroupLayout.createSequentialGroup()
                        .addComponent(overallLikelyhoodRatio)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(showLogButton))
                    .addComponent(analysisProgressPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(resultsGroupLayout.createSequentialGroup()
                        .addComponent(jLabel11)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(resultsGroupLayout.createSequentialGroup()
                        .addGap(0, 28, Short.MAX_VALUE)
                        .addComponent(stopAnalysis, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(startAnalysisButton, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        resultsGroupLayout.setVerticalGroup(
            resultsGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(resultsGroupLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(resultsGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(resultsGroupLayout.createSequentialGroup()
                        .addGroup(resultsGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(startAnalysisButton)
                            .addComponent(stopAnalysis))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(resultsGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(overallLikelyhoodRatio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(showLogButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(analysisProgressPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
        );

        prosecutionGroup.setBackground(new java.awt.Color(249, 249, 249));
        prosecutionGroup.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1), "Prosecution Hypothesis", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.BELOW_TOP, new java.awt.Font("Vijaya", 0, 18))); // NOI18N

        prosecutionContributors.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Contributor", "ID", "Dropout Probability"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Boolean.class, java.lang.Object.class, java.lang.Double.class
            };
            boolean[] canEdit = new boolean [] {
                true, false, true
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
        prosecutionContributors.setFillsViewportHeight(true);
        prosecutionContributors.setName("prosecutionContributors"); // NOI18N
        prosecutionContributors.setRowHeight(20);
        prosecutionContributors.getTableHeader().setReorderingAllowed(false);
        jScrollPane10.setViewportView(prosecutionContributors);
        if (prosecutionContributors.getColumnModel().getColumnCount() > 0) {
            prosecutionContributors.getColumnModel().getColumn(0).setPreferredWidth(80);
            prosecutionContributors.getColumnModel().getColumn(2).setPreferredWidth(100);
        }

        prosecutionUnknownsLabel.setText("Unknown Contributors");

        _prosecutionUnknowns.setModel(new javax.swing.SpinnerNumberModel(0, 0, 4, 1));
        _prosecutionUnknowns.setToolTipText("The number of unknown contributors to the sample according to the prosecution hypothesis");

        prosecutionUnknownDropoutLabel.setText("Dropout Probability for unknowns");
        prosecutionUnknownDropoutLabel.setEnabled(false);

        prosecutionUnknownDropoutProbability.setModel(new javax.swing.SpinnerNumberModel(0.1d, 0.0d, 1.0d, 0.01d));
        prosecutionUnknownDropoutProbability.setEnabled(false);

        final javax.swing.GroupLayout prosecutionGroupLayout = new javax.swing.GroupLayout(prosecutionGroup);
        prosecutionGroup.setLayout(prosecutionGroupLayout);
        prosecutionGroupLayout.setHorizontalGroup(
            prosecutionGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(prosecutionGroupLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(prosecutionGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane10, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(prosecutionGroupLayout.createSequentialGroup()
                        .addGroup(prosecutionGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(prosecutionUnknownsLabel)
                            .addComponent(prosecutionUnknownDropoutLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(prosecutionGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(_prosecutionUnknowns, javax.swing.GroupLayout.DEFAULT_SIZE, 63, Short.MAX_VALUE)
                            .addComponent(prosecutionUnknownDropoutProbability))))
                .addContainerGap())
        );
        prosecutionGroupLayout.setVerticalGroup(
            prosecutionGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(prosecutionGroupLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane10, javax.swing.GroupLayout.DEFAULT_SIZE, 175, Short.MAX_VALUE)
                .addGap(4, 4, 4)
                .addGroup(prosecutionGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(prosecutionUnknownsLabel)
                    .addComponent(_prosecutionUnknowns, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(4, 4, 4)
                .addGroup(prosecutionGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(prosecutionUnknownDropoutLabel)
                    .addComponent(prosecutionUnknownDropoutProbability, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        parametersGroup.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1), "Parameters", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.BELOW_TOP, new java.awt.Font("Vijaya", 0, 18))); // NOI18N

        alleleFrequenciesLabel.setText("Allele Frequencies");

        alleleFrequenciesFileName.setEditable(false);

        browseAlleleFrequencies.setText("...");
        browseAlleleFrequencies.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final java.awt.event.ActionEvent evt) {
                browseAlleleFrequenciesActionPerformed(evt);
            }
        });

        dropInProbabilityLabel.setText("Drop-in probability");

        _dropInProbability.setModel(new javax.swing.SpinnerNumberModel(0.05d, 0.0d, 1.0d, 0.01d));

        thetaCorrectionLabel.setText("Theta correction");

        _thetaCorrection.setModel(new javax.swing.SpinnerNumberModel(0.01d, 0.0d, 1.0d, 0.01d));

        _validationModeCheckBox.setText("Validation Mode");
        _validationModeCheckBox.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final java.awt.event.ActionEvent evt) {
                _validationModeCheckBoxActionPerformed(evt);
            }
        });

        threadSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(16), Integer.valueOf(1), null, Integer.valueOf(1)));
        threadSpinner.setToolTipText("<html>This sets the number of threads used by LRmixStudio to perform calculations.<br>Note that setting this too high may cause the system to become unresponsive while calculations are in progress.");

        _maxThreadsLabel.setText("Max Threads");

        _rareAlleleFrequencyLabel.setText("Rare allele frequency");

        _rareAlleleFrequency.setInheritsPopupMenu(true);

        final javax.swing.GroupLayout parametersGroupLayout = new javax.swing.GroupLayout(parametersGroup);
        parametersGroup.setLayout(parametersGroupLayout);
        parametersGroupLayout.setHorizontalGroup(
            parametersGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(parametersGroupLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(parametersGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(parametersGroupLayout.createSequentialGroup()
                        .addComponent(_rareAlleleFrequencyLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(_rareAlleleFrequency, javax.swing.GroupLayout.PREFERRED_SIZE, 235, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(_rareAlleleFrequencyErrorMessage, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(parametersGroupLayout.createSequentialGroup()
                        .addGroup(parametersGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(dropInProbabilityLabel)
                            .addComponent(alleleFrequenciesLabel))
                        .addGap(18, 18, 18)
                        .addGroup(parametersGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(parametersGroupLayout.createSequentialGroup()
                                .addComponent(alleleFrequenciesFileName)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(browseAlleleFrequencies))
                            .addGroup(parametersGroupLayout.createSequentialGroup()
                                .addComponent(_dropInProbability, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(thetaCorrectionLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(_thetaCorrection, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(_maxThreadsLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(threadSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(_validationModeCheckBox))))))
        );
        parametersGroupLayout.setVerticalGroup(
            parametersGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, parametersGroupLayout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(parametersGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(alleleFrequenciesLabel)
                    .addComponent(alleleFrequenciesFileName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(browseAlleleFrequencies))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(parametersGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(_rareAlleleFrequencyErrorMessage, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(parametersGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(_rareAlleleFrequencyLabel)
                        .addComponent(_rareAlleleFrequency, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(parametersGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(parametersGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(thetaCorrectionLabel)
                        .addComponent(_thetaCorrection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(_validationModeCheckBox)
                        .addComponent(threadSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(_maxThreadsLabel))
                    .addGroup(parametersGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(dropInProbabilityLabel)
                        .addComponent(_dropInProbability, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        final javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(prosecutionGroup, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(2, 2, 2)
                .addComponent(defenseGroup, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(parametersGroup, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(resultsGroup, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(prosecutionGroup, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(defenseGroup, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(2, 2, 2)
                .addComponent(parametersGroup, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(resultsGroup, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void browseAlleleFrequenciesActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseAlleleFrequenciesActionPerformed
        final JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Please select the file containing allele frequencies");
        if (ApplicationSettings.getAlleleFrequenciesPath().isEmpty()) {
            chooser.setCurrentDirectory(new File(ApplicationSettings.getCaseFilesPath()));
        }
        else {
            chooser.setCurrentDirectory(new File(ApplicationSettings.getAlleleFrequenciesPath()));
        }
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
            alleleFrequenciesFileName.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }//GEN-LAST:event_browseAlleleFrequenciesActionPerformed

    private void startAnalysisButtonActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startAnalysisButtonActionPerformed
        try {
            ApplicationSettings.setRareAlleleFrequency(_session.getStatistics(), _session.getRareAlleleFrequency());
            overallLikelyhoodRatio.setText("");
            final DefaultTableModel resultModel = (DefaultTableModel) resultsTable.getModel();
            resultModel.setRowCount(0);
            for (final String locus : _session.getEnabledLoci()) {
                resultModel.addRow(new Object[]{locus, "<html><font color=#c0c0c0>Pending"});
            }

            // Check if some samples are non-contributors in both hypotheses. This can happen in two scenarios:
            // 1. The sample is relevant because the subpopulation is deemed
            //    relevant in court and therefore all profiled individuals need
            //    to be taken into account.
            // 2. The user wants to exclude a sample from the evaluation and
            //    mistakenly makes it a non-contributor in the analysis tab instead
            //    of disabling the profile in the Reference Profiles tab.
            // Note that this is only relevant if theta is larger than 0
            String nonContributorNames = "";
            if (_session.getDefense().getThetaCorrection() > 0.0) {
                for (final Sample c : _session.getActiveProfiles()) {
                    if (!_session.getDefense().isContributor(c) && !_session.getProsecution().isContributor(c)) {
                        if (!nonContributorNames.isEmpty()) {
                            nonContributorNames += ",";
                        }
                        nonContributorNames += c.getId();
                    }
                }
            }

            if (!nonContributorNames.isEmpty()) {
                if (JOptionPane.CANCEL_OPTION
                        == JOptionPane.showConfirmDialog(
                                this,
                                "<html>The reference profile" + (nonContributorNames.indexOf(",") >= 0 ? "s" : "")
                                + " '" + nonContributorNames + "' " + (nonContributorNames.indexOf(",") >= 0 ? "are non-contributors" : "is non-contributor") + " in both hypotheses.<br>"
                                + "As Theta is non-zero, the profile" + (nonContributorNames.indexOf(",") >= 0 ? "s" : "") + " will still have an impact on the calculation results.<br>"
                                + "Click OK to continue with the calculations using the current settings, or Cancel to change your settings.<br><br>"
                                + "<small>Note: To disable a reference profile completely, uncheck it in the Reference Profiles tab.", "LRmixStudio Sanity check", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE)) {
                    return;
                }
            }
            _mathematicalModel = LRMathModelFactory.getMathematicalModel(_session.getMathematicalModelName());
            _mathematicalModel.addProgressListener(new AnalysisProgressListenerImpl(_session, this));
            _mathematicalModel.startAnalysis(_session);
        }
        catch (final Exception e) {
            _mathematicalModel.interrupt();
            if (e.getMessage() != null && e.getMessage().length() > 0) {
                _session.setErrorMessage(e.getMessage());
            }
            else {
                _session.setErrorMessage(e.getClass().getName());
            }
            stopAnalysisActionPerformed(null);
            _session.setApplicationState(READY_FOR_ANALYSIS);
            LOG.error("Error starting analysis", e);
        }
    }//GEN-LAST:event_startAnalysisButtonActionPerformed

    private void stopAnalysisActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopAnalysisActionPerformed
        if (_mathematicalModel != null) {
            _mathematicalModel.interrupt();
        }
        final DefaultTableModel model = (DefaultTableModel) resultsTable.getModel();
        int rowIndex = 0;
        while (rowIndex < model.getRowCount()) {
            final String value = "" + model.getValueAt(rowIndex, 1);
            if (value.indexOf("ending") >= 0) {
                model.removeRow(rowIndex);
            }
            else {
                rowIndex++;
            }
        }
    }//GEN-LAST:event_stopAnalysisActionPerformed

    private void relatedUnknownCheckBoxActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_relatedUnknownCheckBoxActionPerformed
        relatedToLabel.setEnabled(relatedUnknownCheckBox.isSelected());
        relationCombo.setEnabled(relatedUnknownCheckBox.isSelected());
        relatedToCombo.setEnabled(relatedUnknownCheckBox.isSelected());

        LOG.debug("Building defense hypothesis");
        _session.setDefense(buildHypothesis("Defense", defenseContributors, _defenseUnknowns, defenseUnknownDropoutProbability, relatedUnknownCheckBox, relationCombo, relatedToCombo));

    }//GEN-LAST:event_relatedUnknownCheckBoxActionPerformed

    private void _validationModeCheckBoxActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event__validationModeCheckBoxActionPerformed
        if (_session != null) {
            ApplicationSettings.setValidationMode(_validationModeCheckBox.isSelected());
            threadSpinner.setValue(_session.getThreadCount());
            threadSpinner.setEnabled(!ApplicationSettings.isValidationMode());
        }
    }//GEN-LAST:event__validationModeCheckBoxActionPerformed

    private void relationComboActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_relationComboActionPerformed
        if (_session != null) {
            LOG.debug("Relation combo changed to {}", relationCombo.getSelectedItem());
            _session.setDefense(buildHypothesis("Defense", defenseContributors, _defenseUnknowns, defenseUnknownDropoutProbability, relatedUnknownCheckBox, relationCombo, relatedToCombo));
        }
    }//GEN-LAST:event_relationComboActionPerformed

    private void relatedToComboActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_relatedToComboActionPerformed
        if (_session != null) {
            LOG.debug("Relative combo changed to {}", relatedToCombo.getSelectedItem());
            _session.setDefense(buildHypothesis("Defense", defenseContributors, _defenseUnknowns, defenseUnknownDropoutProbability, relatedUnknownCheckBox, relationCombo, relatedToCombo));
        }
    }//GEN-LAST:event_relatedToComboActionPerformed

    private void showLogButtonActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showLogButtonActionPerformed
        try {
            Desktop.getDesktop().open(new File(_session.getCurrentReport().getLogfileName()));
        }
        catch (final IOException ex) {
            _session.setErrorMessage(ex);
        }
    }//GEN-LAST:event_showLogButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSpinner _defenseUnknowns;
    private javax.swing.JSpinner _dropInProbability;
    private javax.swing.JLabel _maxThreadsLabel;
    private javax.swing.JSpinner _prosecutionUnknowns;
    private javax.swing.JTextField _rareAlleleFrequency;
    private javax.swing.JLabel _rareAlleleFrequencyErrorMessage;
    private javax.swing.JLabel _rareAlleleFrequencyLabel;
    private javax.swing.JSpinner _thetaCorrection;
    private javax.swing.JCheckBox _validationModeCheckBox;
    private javax.swing.JTextField alleleFrequenciesFileName;
    private javax.swing.JLabel alleleFrequenciesLabel;
    private javax.swing.JProgressBar analysisProgressBar;
    private javax.swing.JPanel analysisProgressPanel;
    private javax.swing.JButton browseAlleleFrequencies;
    private javax.swing.JTable defenseContributors;
    private javax.swing.JPanel defenseGroup;
    private javax.swing.JLabel defenseUnknownDropoutLabel;
    private javax.swing.JSpinner defenseUnknownDropoutProbability;
    private javax.swing.JLabel defenseUnknownsLabel;
    private javax.swing.JLabel dropInProbabilityLabel;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JTextField overallLikelyhoodRatio;
    private javax.swing.JPanel parametersGroup;
    private javax.swing.JTable prosecutionContributors;
    private javax.swing.JPanel prosecutionGroup;
    private javax.swing.JLabel prosecutionUnknownDropoutLabel;
    private javax.swing.JSpinner prosecutionUnknownDropoutProbability;
    private javax.swing.JLabel prosecutionUnknownsLabel;
    private javax.swing.JComboBox<Sample> relatedToCombo;
    private javax.swing.JLabel relatedToLabel;
    private javax.swing.JCheckBox relatedUnknownCheckBox;
    private javax.swing.JComboBox<Relation> relationCombo;
    private javax.swing.JPanel resultsGroup;
    private javax.swing.JTable resultsTable;
    private javax.swing.JButton showLogButton;
    private javax.swing.JButton startAnalysisButton;
    private javax.swing.JButton stopAnalysis;
    private javax.swing.JLabel thetaCorrectionLabel;
    private javax.swing.JSpinner threadSpinner;
    private javax.swing.JLabel timeLeftLabel;
    private javax.swing.JLabel timeRemaining;
    private javax.swing.JLabel timeSpent;
    private javax.swing.JLabel timeSpentLabel;
    // End of variables declaration//GEN-END:variables

    public void setControlsEnabled(final boolean enabled) {
        startAnalysisButton.setEnabled(enabled && _session.getProsecution() != null && _session.getDefense() != null && _session.getStatistics() != null);
        stopAnalysis.setEnabled(!enabled);

        threadSpinner.setEnabled(enabled && !ApplicationSettings.isValidationMode());
        _maxThreadsLabel.setEnabled(enabled && !ApplicationSettings.isValidationMode());
        _validationModeCheckBox.setEnabled(enabled);

        prosecutionContributors.setEnabled(enabled);
        _prosecutionUnknowns.setEnabled(enabled);
        prosecutionUnknownsLabel.setEnabled(enabled);
        prosecutionUnknownDropoutProbability.setEnabled(enabled && !_prosecutionUnknowns.getValue().toString().equals("0"));
        prosecutionUnknownDropoutLabel.setEnabled(enabled && !_prosecutionUnknowns.getValue().toString().equals("0"));

        defenseContributors.setEnabled(enabled);
        _defenseUnknowns.setEnabled(enabled);
        defenseUnknownsLabel.setEnabled(enabled);
        defenseUnknownDropoutProbability.setEnabled(enabled && !_defenseUnknowns.getValue().toString().equals("0"));
        defenseUnknownDropoutLabel.setEnabled(enabled && !_defenseUnknowns.getValue().toString().equals("0"));

        relatedUnknownCheckBox.setEnabled(enabled && !_defenseUnknowns.getValue().toString().equals("0"));
        relatedToCombo.setEnabled(enabled && relatedUnknownCheckBox.isSelected());
        relatedToLabel.setEnabled(enabled && relatedUnknownCheckBox.isSelected());
        relationCombo.setEnabled(enabled && relatedUnknownCheckBox.isSelected());

        alleleFrequenciesLabel.setEnabled(enabled);
        alleleFrequenciesFileName.setEnabled(enabled);
        _rareAlleleFrequency.setEnabled(enabled);
        _rareAlleleFrequencyLabel.setEnabled(enabled);
        browseAlleleFrequencies.setEnabled(enabled);
        dropInProbabilityLabel.setEnabled(enabled);
        _dropInProbability.setEnabled(enabled);
        thetaCorrectionLabel.setEnabled(enabled);
        _thetaCorrection.setEnabled(enabled);

        showLogButton.setEnabled(enabled && _session.getCurrentReport() != null && (_session.getCurrentReport().getLogfileName() != null) && new File(_session.getCurrentReport().getLogfileName()).isFile());
        analysisProgressBar.setVisible(!enabled);
        timeSpentLabel.setVisible(!enabled);
        timeSpent.setVisible(!enabled);
        timeLeftLabel.setVisible(!enabled);
        timeRemaining.setVisible(!enabled);
    }

    private Hypothesis buildHypothesis(final String name, final JTable contributors, final JSpinner unknowns, final JSpinner unknownDropoutProbability) {
        return buildHypothesis(name, contributors, unknowns, unknownDropoutProbability, null, null, null);
    }

    private Hypothesis buildHypothesis(final String name, final JTable contributors, final JSpinner unknowns, final JSpinner unknownDropoutProbability, final JCheckBox relatedUnknown, final JComboBox<Relation> relation, final JComboBox<Sample> relative) {
        final Hypothesis hypothesis = new Hypothesis(name,
                ((SpinnerNumberModel) unknowns.getModel()).getNumber().intValue(),
                _session.getStatistics(),
                ((SpinnerNumberModel) _dropInProbability.getModel()).getNumber().doubleValue(),
                ((SpinnerNumberModel) unknownDropoutProbability.getModel()).getNumber().doubleValue(),
                ((SpinnerNumberModel) _thetaCorrection.getModel()).getNumber().doubleValue());
        for (int idx = 0; idx < contributors.getModel().getRowCount(); idx++) {
            if (((Boolean) (contributors.getValueAt(idx, 0)))) {
                hypothesis.addContributor(
                        (Sample) contributors.getValueAt(idx, 1),
                        new BigDecimal(((Double) contributors.getValueAt(idx, 2)), new MathContext(2, RoundingMode.HALF_UP)).doubleValue());
            }
            else {
                hypothesis.addNonContributor(
                        (Sample) contributors.getValueAt(idx, 1),
                        new BigDecimal(((Double) contributors.getValueAt(idx, 2)), new MathContext(2, RoundingMode.HALF_UP)).doubleValue());
            }
        }
        if (relatedUnknown != null && relatedUnknown.isSelected() && relatedUnknown.isEnabled()) {
            hypothesis.getRelatedness().setRelation((Relation) relation.getSelectedItem());
            hypothesis.getRelatedness().setRelative((Sample) relative.getSelectedItem());
        }
        else {
            hypothesis.getRelatedness().setRelation(Relation.NONE);
            hypothesis.getRelatedness().setRelative(null);
        }

        return hypothesis;
    }

    @Override
    public void setLocusResult(final String locus, final String lr) {
        for (int idx = 0; idx < resultsTable.getRowCount(); idx++) {
            if (resultsTable.getValueAt(idx, 0).equals(locus)) {
                resultsTable.setValueAt(lr, idx, 1);
                return;
            }
        }

        ((DefaultTableModel) resultsTable.getModel()).addRow(new Object[]{locus, lr});
    }

    @Override
    public void setOverallLikelyhoodRatio(final String lr) {
        overallLikelyhoodRatio.setText(lr);
    }

    @Override
    public void setTimeLeft(final String string) {
        if (string.contains("-")) {
            timeRemaining.setForeground(Color.red);
        }
        else {
            timeRemaining.setForeground(Color.black);
        }
        timeRemaining.setText(string.replaceAll("\\-", ""));
    }

    @Override
    public void setTimeSpent(final String string) {
        timeSpent.setText(string);
    }

    @Override
    public void setAnalysisProgress(final int promille) {
        analysisProgressBar.setValue(promille);
        analysisProgressBar.repaint();
    }

    @Override
    public void plotResults(final Object results) {
    }

    @Override
    public void dataChanged(final ConfigurationDataElement element) {
        LOG.debug("dataChanged {}", element);
        switch (element) {
            case STATISTICS:
                // LRDYN-20: Make sure we rebuild the hypothesis objects if the
                // population statistics are (or 'could potentially have been') changed.
                ((DefaultTableModel) prosecutionContributors.getModel()).fireTableDataChanged();
                ((DefaultTableModel) defenseContributors.getModel()).fireTableDataChanged();
                setRareAlleleFrequency();
                if (_session.getStatistics() != null && !alleleFrequenciesFileName.getText().equalsIgnoreCase(_session.getStatistics().getFileName())) {
                    alleleFrequenciesFileName.setText(_session.getStatistics().getFileName());
                }
                // LRDYN-144 ensure the start button is enabled when the population statistics change
                setControlsEnabled(true);
                break;
            case ACTIVEPROFILES:
            case PROFILES:
            case REPLICATES:
                // LRDYN-23 Allow the user to disable specific profiles and reference samples
                final Sample currentRelative = (Sample) relatedToCombo.getSelectedItem();
                relatedToCombo.removeAllItems();

                final ArrayList<Sample> activeProfiles = new ArrayList<>(_session.getActiveProfiles());

                for (int insertRow = 0; insertRow < activeProfiles.size(); insertRow++) {
                    // If sample is already present in the hypotheses, skip it
                    final Sample sample = activeProfiles.get(insertRow);
                    boolean samplePresent = false;
                    for (int rowIdx = 0; !samplePresent && rowIdx < prosecutionContributors.getModel().getRowCount(); rowIdx++) {
                        if ((prosecutionContributors.getModel()).getValueAt(rowIdx, 1).equals(sample)) {
                            samplePresent = true;
                        }
                    }

                    if (!samplePresent) {
                        ((DefaultTableModel) prosecutionContributors.getModel()).insertRow(insertRow, new Object[]{true, sample, 0.1});
                        ((DefaultTableModel) defenseContributors.getModel()).insertRow(insertRow, new Object[]{false, sample, 0.1});
                    }
                    relatedToCombo.addItem(sample);
                }

                // Check if the samples in the contributor tables are still enabled, and remove them if they are not
                for (int rowIdx = prosecutionContributors.getModel().getRowCount() - 1; rowIdx >= 0; rowIdx--) {
                    final Sample sample = (Sample) prosecutionContributors.getModel().getValueAt(rowIdx, 1);
                    if (!_session.getActiveProfiles().contains(sample)) {
                        ((DefaultTableModel) prosecutionContributors.getModel()).removeRow(rowIdx);
                        ((DefaultTableModel) defenseContributors.getModel()).removeRow(rowIdx);
                    }
                }

                if (relatedToCombo.getItemCount() == 0 || !activeProfiles.contains(currentRelative)) {
                    relatedUnknownCheckBox.setSelected(false);
                    relatedUnknownCheckBox.setEnabled(false);
                    relatedToLabel.setEnabled(false);
                    relationCombo.setEnabled(false);
                    relatedToCombo.setEnabled(false);
                    if(relatedToCombo.getItemCount()>0) {
                        relatedToCombo.setSelectedIndex(0);
                    }
                }
                else {
                    relatedToCombo.setSelectedItem(currentRelative);
                }
                break;
            case PROSECUTION:
                enableStartButton();
                final Hypothesis prosecution = _session.getProsecution();
                if (prosecution != null) {
                    _prosecutionUnknowns.getModel().setValue(prosecution.getUnknownCount());
                    prosecutionUnknownDropoutProbability.getModel().setValue(prosecution.getUnknownDropoutProbability());
                    _dropInProbability.getModel().setValue(prosecution.getDropInProbability());
                    _thetaCorrection.getModel().setValue(prosecution.getThetaCorrection());
                    updateTable(prosecution, prosecutionContributors);
                } else {
                    resetHypotheses();
                }
                updateResults();
                break;
            case DEFENSE:
                enableStartButton();
                final Hypothesis defense = _session.getDefense();
                if (defense != null) {
                    _defenseUnknowns.getModel().setValue(defense.getUnknownCount());
                    defenseUnknownDropoutProbability.getModel().setValue(defense.getUnknownDropoutProbability());
                    updateTable(defense, defenseContributors);
                    relatedUnknownCheckBox.setSelected(defense.getRelatedness().getRelation() != Relation.NONE);
                    if (defense.getRelatedness().getRelation() != Relation.NONE) {
                        relationCombo.setSelectedItem(defense.getRelatedness().getRelation());
                        relatedToCombo.setSelectedItem(defense.getRelatedness().getRelative());
                    }
                    relatedToLabel.setEnabled(relatedUnknownCheckBox.isSelected());
                    relationCombo.setEnabled(relatedUnknownCheckBox.isSelected());
                    relatedToCombo.setEnabled(relatedUnknownCheckBox.isSelected());
                } else {
                    resetHypotheses();
                }
                updateResults();
                break;
        }
    }

    private void enableStartButton() {
        startAnalysisButton.setEnabled(_rareAlleleFrequencyErrorMessage.getText().isEmpty() && _session.getStatistics() != null && _session.getProsecution() != null && _session.getDefense() != null);
    }

    private void updateResults() {
        ((DefaultTableModel) resultsTable.getModel()).setRowCount(0);
        overallLikelyhoodRatio.setText("");
        final AnalysisReport currentReport = _session.getCurrentReport();
        if (currentReport != null) {
            showLogButton.setEnabled(currentReport.getLogfileName() != null && new File(currentReport.getLogfileName()).isFile());
            final LikelihoodRatio likelihoodRatio = currentReport.getLikelihoodRatio();
            if (likelihoodRatio != null) {
                for (final String locus : _session.getEnabledLoci()) {
                    final Ratio ratio = likelihoodRatio.getRatio(locus);
                    if (ratio != null) {
                        setLocusResult(ratio.getLocusName(), _session.formatNumber(ratio.getRatio()));
                    }
                    setOverallLikelyhoodRatio(_session.formatNumber(likelihoodRatio.getOverallRatio().getRatio()));
                }
            }
        }
        else {
            showLogButton.setEnabled(false);
        }
    }

    @Override
    public void applicationStateChanged(final APP_STATE newState) {
        switch (newState) {
            case READY_FOR_ANALYSIS:
                setControlsEnabled(true);
                break;
            case ANALYSIS_RUNNING:
                setControlsEnabled(false);
                break;
        }
        setEnabled(newState == APP_STATE.READY_FOR_ANALYSIS);
    }
}
