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
package nl.minvenj.nfi.lrmixstudio.gui.tabs.replicates;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.minvenj.nfi.lrmixstudio.domain.Sample;
import nl.minvenj.nfi.lrmixstudio.gui.ApplicationSettings;
import nl.minvenj.nfi.lrmixstudio.gui.ApplicationStateChangeListener;
import nl.minvenj.nfi.lrmixstudio.gui.LostTimeCheck;
import nl.minvenj.nfi.lrmixstudio.gui.PathResolver;
import nl.minvenj.nfi.lrmixstudio.gui.SessionData;
import nl.minvenj.nfi.lrmixstudio.gui.ZebraTable;
import nl.minvenj.nfi.lrmixstudio.gui.profileeditor.ProfileEditor;
import nl.minvenj.nfi.lrmixstudio.io.SampleReader;
import nl.minvenj.nfi.lrmixstudio.model.ConfigurationDataChangeListener;
import nl.minvenj.nfi.lrmixstudio.model.ConfigurationDataElement;

/**
 * This class shows the tab that loads the replicate files.
 *
 * @author dejong
 */
public class ReplicatesPanel extends javax.swing.JPanel implements ConfigurationDataChangeListener, ApplicationStateChangeListener {

    private static final Logger LOG = LoggerFactory.getLogger(ReplicatesPanel.class);
    protected SessionData session;
    protected boolean ignoreDuplicateAlleles;

    public ReplicatesPanel() {
        LOG.info("ReplicatesPanel");
        initComponents();
        ignoreDuplicateAlleles = true;
        addProfileButton.setText("Add replicate");
        caseLabel.setForeground(Color.RED);
        try {
            final InputStream is = getClass().getResourceAsStream("icon.png");
            final BufferedImage imgOrg = ImageIO.read(is);
            final Image imgScaled = imgOrg.getScaledInstance(140, 149, Image.SCALE_SMOOTH);
            final Icon icon = new ImageIcon(imgScaled);
            jLabel1.setIcon(icon);
        } catch (final IOException ex) {
            LOG.warn("Could not read icon!", ex);
        } catch (final IllegalArgumentException iae) {
            LOG.warn("Could not read icon!", iae);
        }

        this.caseNumber.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(final DocumentEvent e) {
                doUpdate();
            }

            @Override
            public void removeUpdate(final DocumentEvent e) {
                doUpdate();
            }

            @Override
            public void changedUpdate(final DocumentEvent e) {
                doUpdate();
            }

            private void doUpdate() {
                session.setCaseNumber(caseNumber.getText());
            }
        });

        _samplesTable.setName("replicates");
        _samplesTable.setFillsViewportHeight(true);
        _detailTable.setName("replicateAlleles");

        _samplesTable.getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(final TableModelEvent e) {
                _detailTable.clear();
                for (int idx = 0; idx < _samplesTable.getRowCount(); idx++) {
                    final Sample sample = (Sample) _samplesTable.getValueAt(idx, 1);
                    sample.setEnabled((Boolean) _samplesTable.getValueAt(idx, 0));
                    if (sample.isEnabled()) {
                        _detailTable.addSample(sample);
                    }
                }
                session.fireUpdated(getConfigurationElement());
            }
        });

        final DropTarget dropTarget = new DropTarget(_samplesTable, new DropTargetAdapter() {
            @Override
            public void drop(final DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(dtde.getDropAction());
                    final Collection<File> droppedFiles = (Collection<File>) dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    loadFiles(droppedFiles.toArray(new File[droppedFiles.size()]));
                    dtde.dropComplete(true);
                } catch (UnsupportedFlavorException | IOException ex) {
                    LOG.info("Error reading dropped files", ex);
                    dtde.dropComplete(false);
                }
            }
        });
    }

    public void setContext(final SessionData session) {
        LOG.debug("setContext({})", session);
        this.session = session;
        this.session.addDataChangeListener(this);
        this.session.addStateChangeListener(this);
        _detailTable.clear();
        _detailTable.setSession(session);
        this.caseNumber.grabFocus();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        _detailPanel = new JPanel();
        loadFileButton = new javax.swing.JButton();
        _samplesScrollPane = new javax.swing.JScrollPane();
        _samplesTable = new ZebraTable();
        addProfileButton = new javax.swing.JButton();
        caseLabel = new javax.swing.JLabel();
        caseNumber = new javax.swing.JTextField();
        clearButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        loadLogButton = new javax.swing.JButton();
        _detailScrollPane = new javax.swing.JScrollPane();
        _detailTable = getSamplesTable();

        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jPanel1.setMinimumSize(new Dimension(0, 200));

        loadFileButton.setText("Load from file...");
        loadFileButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final java.awt.event.ActionEvent evt) {
                loadFileButtonActionPerformed(evt);
            }
        });

        _samplesTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Active", "Sample", "Source File"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Boolean.class, java.lang.Object.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                true, false, false
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
        _samplesTable.setFillsViewportHeight(true);
        _samplesTable.getTableHeader().setReorderingAllowed(false);
        _samplesScrollPane.setViewportView(_samplesTable);
        if (_samplesTable.getColumnModel().getColumnCount() > 0) {
            _samplesTable.getColumnModel().getColumn(0).setMinWidth(60);
            _samplesTable.getColumnModel().getColumn(0).setPreferredWidth(60);
            _samplesTable.getColumnModel().getColumn(0).setMaxWidth(60);
            _samplesTable.getColumnModel().getColumn(1).setPreferredWidth(100);
            _samplesTable.getColumnModel().getColumn(2).setPreferredWidth(300);
        }

        addProfileButton.setText("Add profile...");
        addProfileButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final java.awt.event.ActionEvent evt) {
                addProfileButtonActionPerformed(evt);
            }
        });

        caseLabel.setText("Case Number");

        clearButton.setText("Restart");
        clearButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final java.awt.event.ActionEvent evt) {
                clearButtonActionPerformed(evt);
            }
        });

        loadLogButton.setText("Restore session from Log");
        loadLogButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final java.awt.event.ActionEvent evt) {
                loadLogButtonActionPerformed(evt);
            }
        });

        final javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(Alignment.TRAILING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(jPanel1Layout.createParallelGroup(Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addComponent(caseLabel)
                            .addPreferredGap(ComponentPlacement.RELATED)
                            .addComponent(caseNumber, GroupLayout.PREFERRED_SIZE, 158, GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(ComponentPlacement.RELATED, 14, Short.MAX_VALUE)
                            .addComponent(loadLogButton)
                            .addPreferredGap(ComponentPlacement.RELATED)
                            .addComponent(clearButton)
                            .addPreferredGap(ComponentPlacement.RELATED)
                            .addComponent(addProfileButton)
                            .addPreferredGap(ComponentPlacement.RELATED)
                            .addComponent(loadFileButton))
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addComponent(jLabel1, GroupLayout.PREFERRED_SIZE, 140, GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(ComponentPlacement.RELATED)
                            .addComponent(_samplesScrollPane, GroupLayout.DEFAULT_SIZE, 540, Short.MAX_VALUE)))
                    .addContainerGap())
            );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(Alignment.TRAILING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(jPanel1Layout.createParallelGroup(Alignment.LEADING)
                        .addComponent(_samplesScrollPane, GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addComponent(jLabel1, GroupLayout.PREFERRED_SIZE, 149, GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addGroup(jPanel1Layout.createParallelGroup(Alignment.BASELINE)
                        .addComponent(loadFileButton)
                        .addComponent(addProfileButton)
                        .addComponent(caseLabel)
                        .addComponent(caseNumber, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(clearButton)
                        .addComponent(loadLogButton))
                    .addGap(51))
        );
        jPanel1.setLayout(jPanel1Layout);

        jSplitPane1.setTopComponent(jPanel1);

        _detailTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Locus", "Title 2", "Title 3", "Title 4"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class
            };

            @Override
            public Class getColumnClass(final int columnIndex) {
                return types [columnIndex];
            }
        });
        _detailTable.setFillsViewportHeight(true);
        _detailScrollPane.setViewportView(_detailTable);

        _warningPanel = new JPanel();
        _warningPanel.setMinimumSize(new Dimension(10, 100));
        _warningPanel.setBorder(new LineBorder(Color.RED, 2));
        _warningPanel.setVisible(false);
        _warningPanel.setLayout(new BoxLayout(_warningPanel, BoxLayout.X_AXIS));

        _warningLabel = new JLabel("<html><b>Loci containing a single allele are interpreted as being homozygote. See the <span style=\"color:white;background-color:red;font-weight:bold\">&nbsp;marked&nbsp;</span>&nbsp;alleles.");
        _warningLabel.setIcon(new ImageIcon(ReplicatesPanel.class.getResource("/resources/icon_alert.gif")));
        _warningPanel.add(_warningLabel);

        _detailPanel.setLayout(new BorderLayout(0, 0));

        _detailPanel.add(_detailScrollPane, BorderLayout.CENTER);
        _detailPanel.add(_warningPanel, BorderLayout.SOUTH);

        jSplitPane1.setRightComponent(_detailPanel);

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
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 562, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void loadFileButtonActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadFileButtonActionPerformed
        final JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(getChooserDialogTitle());
        chooser.setCurrentDirectory(new File(ApplicationSettings.getCaseFilesPath()));
        chooser.setMultiSelectionEnabled(true);
        chooser.setAcceptAllFileFilterUsed(true);
        chooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(final File f) {
                // Do not show the files loaded as sample
                for (final Sample sample : session.getAllReplicates()) {
                    if (f.getAbsolutePath().equalsIgnoreCase(sample.getSourceFile())) {
                        return false;
                    }
                }
                // Do not show the files already loaded as profiles
                for (final Sample sample : session.getAllProfiles()) {
                    if (f.getAbsolutePath().equalsIgnoreCase(sample.getSourceFile())) {
                        return false;
                    }
                }
                return true;
            }

            @Override
            public String getDescription() {
                return "Files not already loaded";
            }
        });

        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
            loadFiles(chooser.getSelectedFiles());
        }
    }//GEN-LAST:event_loadFileButtonActionPerformed

    private void addProfileButtonActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addProfileButtonActionPerformed
        Component parent = getParent();
        while (parent != null && !(parent instanceof Frame)) {
            parent = parent.getParent();
        }
        final ProfileEditor editor = new ProfileEditor((Frame) parent, true, ignoreDuplicateAlleles);
        editor.setContext(session);
        editor.setLocationRelativeTo(this);
        editor.setVisible(true);
        if (editor.isOk()) {
            try {
                final SampleReader reader = new SampleReader(editor.getFile(), ignoreDuplicateAlleles);
                final Collection<Sample> samples = reader.getSamples();
                addSamples(samples);
            } catch (final IOException ioe) {
                LOG.error(ioe.getMessage());
            }
        }

    }//GEN-LAST:event_addProfileButtonActionPerformed

    private void clearButtonActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearButtonActionPerformed
        if (JOptionPane.OK_OPTION == LostTimeCheck.performRestartCheck(session, this)) {
            session.clear();
        }
    }//GEN-LAST:event_clearButtonActionPerformed

    private void loadLogButtonActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadLogButtonActionPerformed
        session.clear();
        // Select a log to restore from
        final JFileChooser chooser = new JFileChooser(ApplicationSettings.getCaseFilesPath());
        chooser.setMultiSelectionEnabled(false);
        chooser.setAcceptAllFileFilterUsed(true);
        chooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(final File f) {
                return f.isDirectory() || (f.getName().matches(".*\\.log"));
            }

            @Override
            public String getDescription() {
                return "LRmixStudio LOG files (*.log)";
            }
        });

        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
            loadLog(chooser.getSelectedFile());
        }
    }//GEN-LAST:event_loadLogButtonActionPerformed

    private void loadLog(final File logFile) {
        session.restore(new PathResolver(this), logFile);
        caseNumber.grabFocus();
        updateApplicationState();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    protected javax.swing.JButton addProfileButton;
    protected javax.swing.JLabel caseLabel;
    protected javax.swing.JTextField caseNumber;
    protected javax.swing.JButton clearButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane _samplesScrollPane;
    private javax.swing.JSplitPane jSplitPane1;
    protected javax.swing.JButton loadFileButton;
    protected javax.swing.JButton loadLogButton;
    protected ReplicatesTable _detailTable;
    protected javax.swing.JTable _samplesTable;
    protected JPanel _warningPanel;
    protected JPanel _detailPanel;
    private JLabel _warningLabel;
    private JScrollPane _detailScrollPane;
    // End of variables declaration//GEN-END:variables

    /**
     * Adds the supplied samples to the relevant list of samples (either the
     * replicates or the reference profiles depending on implementation)
     *
     * @param samples A Collection of Sample classes
     */
    protected void addSamples(final Collection<Sample> samples) {
        session.addReplicates(samples);
    }

    protected ReplicatesTable getSamplesTable() {
        return new ReplicatesTable();
    }

    protected String getChooserDialogTitle() {
        return "Please select a file containing sample data";
    }

    protected ConfigurationDataElement getConfigurationElement() {
        return ConfigurationDataElement.ACTIVEREPLICATES;
    }

    @Override
    public void dataChanged(final ConfigurationDataElement target) {
        switch (target) {
            case CASENUMBER:
                if (!caseNumber.getText().equalsIgnoreCase(session.getCaseNumber())) {
                    caseNumber.setText(session.getCaseNumber());
                }
                updateApplicationState();
                break;
            case ACTIVELOCI:
            case REPLICATES:
            case ACTIVEREPLICATES:
                ((DefaultTableModel) _samplesTable.getModel()).setRowCount(0);
                for (final Sample sample : session.getAllReplicates()) {
                    ((DefaultTableModel) _samplesTable.getModel()).addRow(new Object[]{sample.isEnabled(), sample, sample.getSourceFile().substring(sample.getSourceFile().lastIndexOf(File.separator) + 1)});
                }
                updateApplicationState();
                break;
        }
    }

    protected void updateApplicationState() {
        if (session.getActiveReplicates().isEmpty() || session.getCaseNumber().isEmpty()) {
            session.setApplicationState(APP_STATE.WAIT_SAMPLE);
        } else if (session.getActiveProfiles().isEmpty()) {
            session.setApplicationState(APP_STATE.WAIT_PROFILES);
        } else {
            session.setApplicationState(APP_STATE.READY_FOR_ANALYSIS);
        }
    }

    @Override
    public void applicationStateChanged(final APP_STATE newState) {
        if (session.getActiveReplicates().isEmpty() || session.getCaseNumber().isEmpty()) {
            caseLabel.setForeground(Color.RED);
            caseNumber.grabFocus();
        } else if (session.getActiveProfiles().isEmpty()) {
            caseLabel.setForeground(Color.BLACK);
        } else {
            caseLabel.setForeground(Color.BLACK);
        }
        setEnabled(!newState.isActive());
    }

    private void loadFiles(final File[] selectedFiles) {
        final ArrayList<String> alreadyLoadedFiles = new ArrayList<>();
        for (final File file : selectedFiles) {
            try {
                boolean alreadyLoadedAsReplicate = false;
                boolean alreadyLoadedAsProfile = false;

                for (final Sample sample : session.getAllReplicates()) {
                    if (file.getAbsolutePath().equalsIgnoreCase(sample.getSourceFile())) {
                        alreadyLoadedAsReplicate = true;
                    }
                }
                // Do not show the files already loaded as profiles
                for (final Sample sample : session.getAllProfiles()) {
                    if (file.getAbsolutePath().equalsIgnoreCase(sample.getSourceFile())) {
                        alreadyLoadedAsProfile = true;
                    }
                }

                if ((alreadyLoadedAsReplicate || alreadyLoadedAsProfile) && !userWantsToLoadFileAgain(file, alreadyLoadedAsReplicate, alreadyLoadedAsProfile, ignoreDuplicateAlleles)) {
                    LOG.info("Skipping file  '{}' because it is already loaded.", file.getAbsolutePath());
                    alreadyLoadedFiles.add(file.getAbsolutePath());
                    continue;
                }

                ApplicationSettings.setCaseFilesPath(file.getParent());

                final SampleReader sampleReader = new SampleReader(file.getAbsolutePath(), ignoreDuplicateAlleles);
                final Collection<Sample> samples = sampleReader.getSamples();

                // If the file contains a case number check it against the number from any already loaded files
                if (sampleReader.getCaseNumber().isEmpty()) {
                    // Sample file did not contain any case number. If the case number field is empty, set it to the name of the parent folder
                    if (session.getCaseNumber().isEmpty()) {
                        session.setCaseNumber(new File(file.getParent()).getName());
                    }
                } else {
                    // Sample file contained a case number If it matches the number in the session ask for confirmation before overwriting
                    if (session.getCaseNumber().isEmpty()
                            || (!session.getCaseNumber().equalsIgnoreCase(sampleReader.getCaseNumber())
                            && JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(
                                    this,
                                    "The file you loaded contains case number '"
                                    + sampleReader.getCaseNumber()
                                    + "' but the case number is currently set to '"
                                    + session.getCaseNumber()
                                    + "'\nDo you want to use the number from the file?", "LRmixStudio", JOptionPane.YES_NO_OPTION))) {
                        session.setCaseNumber(sampleReader.getCaseNumber());
                    }
                }
                addSamples(samples);
            } catch (final Throwable t) {
                LOG.error("There was an error loading the file", t);
                JOptionPane.showMessageDialog(this, "<html>" + t + "<br>" + file.getAbsolutePath());
            }
        }
        if (!alreadyLoadedFiles.isEmpty()) {
            final StringBuilder builder = new StringBuilder(alreadyLoadedFiles.size() > 1 ? "<html>The following files were skipped because they are already loaded:<br>" : "<html>The following file was skipped because it is already loaded:<br>");
            for (final String fileName : alreadyLoadedFiles) {
                builder.append(fileName).append("<br>");
            }
            JOptionPane.showMessageDialog(this, builder.toString());
        }
    }

    private boolean userWantsToLoadFileAgain(final File f, final boolean alreadyReplicate, final boolean alreadyProfile, final boolean useAsReplicate) {
        String msg = "The file '" + f.getName() + "' is already loaded as ";
        if (alreadyReplicate) {
            msg += "replicate";
            if (alreadyProfile) {
                msg += " and ";
            }
        }
        if (alreadyProfile) {
            msg += "profile";
        }
        if ((useAsReplicate && alreadyReplicate) || (!useAsReplicate && alreadyProfile)) {
            msg += ".\nDo you want to use the file as another ";
        }
        else {
            msg += ".\nDo you also want to use the file as ";
        }
        if (useAsReplicate) {
            msg += "replicate";
        }
        else {
            msg += "profile";
        }
        msg += "?";
        return JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(this, msg, "LRmixStudio", JOptionPane.YES_NO_OPTION);
    }
}
