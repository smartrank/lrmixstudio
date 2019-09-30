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
package nl.minvenj.nfi.lrmixstudio.gui.profileeditor;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.minvenj.nfi.lrmixstudio.gui.ApplicationSettings;
import nl.minvenj.nfi.lrmixstudio.gui.SessionData;
import nl.minvenj.nfi.lrmixstudio.gui.ZebraTable;
import nl.minvenj.nfi.lrmixstudio.io.SampleWriter;

public class ProfileEditor extends javax.swing.JDialog {

    private static final Logger LOG = LoggerFactory.getLogger(ProfileEditor.class);
    private boolean _ok;
    private File _file;
    private SessionData _session;
    private boolean _canSave;
    private final boolean _replicateMode;

    /**
     * Creates new form ProfileEditor
     */
    public ProfileEditor(java.awt.Frame parent, boolean modal, boolean replicateMode) {
        super(parent, modal);
        initComponents();

        _replicateMode = replicateMode;
        _canSave = false;
        _ok = false;

        profileName.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                doUpdate();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                doUpdate();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                doUpdate();
            }

            private void doUpdate() {
                _canSave = !profileName.getText().isEmpty() && !locusTableIsEmpty();
                updateSaveButtonStatus();
            }
        });

        locusTable.getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                _canSave = !profileName.getText().isEmpty() && !locusTableIsEmpty();
                updateSaveButtonStatus();
            }
        });

        locusTable.setDefaultEditor(String.class, new LocusTableCellEditor(locusTable));

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cancelButtonActionPerformed(null);
            }
        });

        if (_replicateMode) {
            ((DefaultTableModel) locusTable.getModel()).addColumn("Allele 3");
            ((DefaultTableModel) locusTable.getModel()).addColumn("Allele 4");
            ((DefaultTableModel) locusTable.getModel()).addColumn("Allele 5");
            ((DefaultTableModel) locusTable.getModel()).addColumn("Allele 6");
            ((DefaultTableModel) locusTable.getModel()).addColumn("Allele 7");
            ((DefaultTableModel) locusTable.getModel()).addColumn("Allele 8");
        }
    }

    private void updateSaveButtonStatus() {
        _saveButton.setEnabled(_canSave);
        _saveButton.setToolTipText(_canSave ? "Click here to save the profile" : "Please enter a profile name");
    }

    public void setContext(SessionData session) {
        _session = session;
        ArrayList<String> allLoci = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResource("loci.properties").openStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.startsWith("#")) {
                    allLoci.add(line);
                }
            }
        }
        catch (IOException ex) {
            LOG.error("Cannot load default locus list!", ex);
        }
        for (String locusName : allLoci) {
            ((DefaultTableModel) locusTable.getModel()).addRow(new Object[]{locusName});
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        profileName = new javax.swing.JTextField();
        _saveButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        locusTable = new ZebraTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Profile Editor");

        jLabel1.setText("Name");

        _saveButton.setText("Save");
        _saveButton.setEnabled(false);
        _saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _saveButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        locusTable.setModel(new javax.swing.table.DefaultTableModel(
            new String [] {
                "Locus", "Allele 1", "Allele 2"
            }, 0
        ) {

            public Class getColumnClass(int columnIndex) {
                return String.class;
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return columnIndex!=0;
            }
        });
        locusTable.setName("profileEditor"); // NOI18N
        jScrollPane2.setViewportView(locusTable);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 410, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(cancelButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(_saveButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(profileName, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(profileName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 301, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_saveButton)
                    .addComponent(cancelButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void _saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__saveButtonActionPerformed
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select the filename for the " + profileName.getText() + (_replicateMode ? "replicate" : " profile"));
        chooser.setCurrentDirectory(new File(ApplicationSettings.getCaseFilesPath()));
        chooser.setSelectedFile(new File(profileName.getText() + ".csv"));
        if (JFileChooser.APPROVE_OPTION == chooser.showSaveDialog(this)) {
            try {
                if (chooser.getSelectedFile().exists()
                        && JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(this, "Are you sure you want to overwrite '" + chooser.getSelectedFile().getName() + "'?", "LRmixStudio", JOptionPane.OK_CANCEL_OPTION)) {
                    return;
                }
                this._file = chooser.getSelectedFile();

                SampleWriter.write(_file, profileName.getText(), ((DefaultTableModel) this.locusTable.getModel()).getDataVector());

                _ok = true;
                dispose();
            }
            catch (IOException ex) {
                LOG.error("Error saving file", ex);
                _session.setErrorMessage(ex.getMessage());
            }
        }
    }//GEN-LAST:event__saveButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        if (_canSave
                && JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(this, "Are you sure you want close the editor without saving?", "LRmixStudio", JOptionPane.OK_CANCEL_OPTION)) {
            return;
        }

        dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /*
         * Set the Nimbus look and feel
         */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the
         * default look and feel. For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        }
        catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ProfileEditor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ProfileEditor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ProfileEditor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ProfileEditor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /*
         * Create and display the dialog
         */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                ProfileEditor dialog = new ProfileEditor(new javax.swing.JFrame(), true, false);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton _saveButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable locusTable;
    private javax.swing.JTextField profileName;
    // End of variables declaration//GEN-END:variables

    public boolean isOk() {
        return _ok;
    }

    public File getFile() {
        return _file;
    }

    private boolean locusTableIsEmpty() {
        DefaultTableModel model = (DefaultTableModel) locusTable.getModel();
        boolean isEmpty = true;
        for (int row = 0; isEmpty && row < model.getRowCount(); row++) {
            String value = "" + model.getValueAt(row, 1) + model.getValueAt(row, 2);
            isEmpty &= value.isEmpty() || value.equalsIgnoreCase("null") || value.equalsIgnoreCase("nullnull");
        }
        return isEmpty;
    }
}
