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
package nl.minvenj.nfi.lrmixstudio.gui;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.minvenj.nfi.lrmixstudio.model.ConfigurationDataChangeListener;
import nl.minvenj.nfi.lrmixstudio.model.ConfigurationDataElement;

public class LRmixStudio extends javax.swing.JFrame implements ConfigurationDataChangeListener, ApplicationStateChangeListener {

    private static final Logger LOG = LoggerFactory.getLogger(LRmixStudio.class);
    private static final long serialVersionUID = 8586701967626279048L;
    private static ArrayList<Component> _fontListeners = new ArrayList<>();
    private static int _fontSize = ApplicationSettings.getFontSize();
    private final SessionData _session = new SessionData();
    private final ArrayList<Image> _icons = new ArrayList<>();
    private final TrayIconHandler _trayIconHandler;
    private final int _fileMenuSize;
    private JMenu _viewMenu;
    private JMenuItem _fontsLargerMenuItem;
    private JMenuItem _fontsSmallerMenuItem;


    /**
     * Creates new form LRmix Studio
     */
    public LRmixStudio() {
        try (final FileInputStream fis = new FileInputStream("log4j.properties")) {
            PropertyConfigurator.configure(fis);
            LOG.info("Logging configured from {}", new File("log4j.properties").getAbsolutePath());
        } catch (final IOException ex) {
            String userHomeLog4jProperties = new File(new File(System.getProperty("user.home"), ".lrmixstudio"), "log4j.properties").getAbsolutePath();
            try (final FileInputStream fis = new FileInputStream(userHomeLog4jProperties)){
                PropertyConfigurator.configure(fis);
                LOG.info("Logging configured from {}", userHomeLog4jProperties);
            }
            catch (IOException e1) {
            PropertyConfigurator.configure(getClass().getResourceAsStream("/log4j.properties"));
                LOG.info("Logging configured from /log4j.properties in jar file");
        }
        }
        LOG.info("Starting LRmix Studio v {}", ApplicationSettings.getProgramVersion());

        initComponents();

        _session.setProgramVersion(ApplicationSettings.getProgramVersion());
        _session.addDataChangeListener(this);

        _trayIconHandler = new TrayIconHandler(this, _session);

        final DropTarget dropTarget = new DropTarget(this, new DropTargetAdapter() {
            @SuppressWarnings("unchecked")
            @Override
            public void drop(final DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(dtde.getDropAction());
                    final Collection<File> droppedFiles = (Collection<File>) dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    if (droppedFiles.size() > 1) {
                        _session.setErrorMessage("Please only drop a single file!");
                    } else {
                        loadLog(droppedFiles.iterator().next());
                    }
                    dtde.dropComplete(true);
                } catch (UnsupportedFlavorException | IOException ex) {
                    LOG.info("Error reading dropped files", ex);
                    dtde.dropComplete(false);
                }
            }
        });

        try {
            samplePanel.setContext(_session);
            profilePanel.setContext(_session);
            profileSummaryPanel.setContext(_session);
            analysisPanel.setContext(_session);
            sensitivityAnalysisPanel.setContext(_session);
            _nonContributorTestPanel.setContext(_session);
            reportsPanel.setContext(_session);
            aboutPanel.setContext(_session);

            int idx = 0;
            String iconName;
            while ((iconName = ApplicationSettings.getIcon(idx++)) != null) {
                LOG.debug("Loading application icon {}", iconName);
                final Image icon = Toolkit.getDefaultToolkit().getImage(getClass().getResource(iconName));
                _icons.add(icon);
            }
            setIconImages(_icons);
        } catch (final Throwable t) {
            LOG.error("Error initializing the application:", t);
        }

        fileMenu.setVisible(ApplicationSettings.isAdvancedMode());
        settingsMenu.setVisible(ApplicationSettings.isAdvancedMode());

        _fileMenuSize = fileMenu.getItemCount();
        updateMRUList();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) {
                if (JOptionPane.OK_OPTION == LostTimeCheck.performExitCheck(_session, (Component) e.getSource())) {
                    System.exit(0);
                }
            }
        });

        // This to allow the WindowStateListener to actually block the window from
        // closing if more than a configurable amount of processing time remains unexported.
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        // Note that this class should be added as state change listener AFTER all panels are added. This so that panels can respond to a state change before the main GUI is updated
        _session.addStateChangeListener(this);
    }

    private void updateMRUList() {
        // Trim MRU entries from file menu
        while (fileMenu.getItemCount() > _fileMenuSize) {
            fileMenu.remove(_fileMenuSize);
        }

        // Populate Most Recently Used list
        for (final String mruSession : ApplicationSettings.getMostRecentlyUsed()) {
            fileMenu.add(new JMenuItem(mruSession)).addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    if (_session.restore(new PathResolver(((JComponent) e.getSource()).getParent()), new File(mruSession))) {
                        ApplicationSettings.addMostRecentlyUsed(mruSession);
                    } else {
                        ApplicationSettings.removeMostRecentlyUsed(mruSession);
                }
                    updateMRUList();
            }
            }
            );
        }
    }

    private boolean loadLog(final File logFile) {
        return _session.restore(new PathResolver(this), logFile);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainTabbedPane = new javax.swing.JTabbedPane();
        samplePanel = new nl.minvenj.nfi.lrmixstudio.gui.tabs.replicates.ReplicatesPanel();
        profilePanel = new nl.minvenj.nfi.lrmixstudio.gui.tabs.profiles.ProfilesPanel();
        profileSummaryPanel = new nl.minvenj.nfi.lrmixstudio.gui.tabs.profilesummary.ProfileSummaryPanel();
        analysisPanel = new nl.minvenj.nfi.lrmixstudio.gui.tabs.analysis.AnalysisPanel();
        sensitivityAnalysisPanel = new nl.minvenj.nfi.lrmixstudio.gui.tabs.sensitivity.SensitivityAnalysisPanel();
        _nonContributorTestPanel = new nl.minvenj.nfi.lrmixstudio.gui.tabs.noncontributor.NonContributorTestPanel();
        reportsPanel = new nl.minvenj.nfi.lrmixstudio.gui.tabs.reports.ReportsPanel();
        aboutPanel = new nl.minvenj.nfi.lrmixstudio.gui.tabs.about.AboutPanel();
        mainMenuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        newSessionMenuItem = new javax.swing.JMenuItem();
        openSessionMenuItem = new javax.swing.JMenuItem();
        saveSessionMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        settingsMenu = new javax.swing.JMenu();
        settingsMenuItem = new javax.swing.JMenuItem();
        _viewMenu = new javax.swing.JMenu();
        helpMenu = new javax.swing.JMenu();
        manualMenuItem = new javax.swing.JMenuItem();
        aboutMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("LRmix Studio");

        mainTabbedPane.addTab("Sample Files", samplePanel);
        mainTabbedPane.addTab("Reference Files", profilePanel);
        mainTabbedPane.addTab("Profile Summary", profileSummaryPanel);
        mainTabbedPane.addTab("Analysis", analysisPanel);
        mainTabbedPane.addTab("Sensitivity Analysis", sensitivityAnalysisPanel);
        mainTabbedPane.addTab("Non-contributor Test", _nonContributorTestPanel);
        mainTabbedPane.addTab("Reports", reportsPanel);
        mainTabbedPane.addTab("About", aboutPanel);

        fileMenu.setText("File");

        newSessionMenuItem.setText("New session");
        newSessionMenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final java.awt.event.ActionEvent evt) {
                newSessionMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(newSessionMenuItem);

        openSessionMenuItem.setText("Load session...");
        openSessionMenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final java.awt.event.ActionEvent evt) {
                openSessionMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(openSessionMenuItem);

        saveSessionMenuItem.setText("Save session...");
        saveSessionMenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final java.awt.event.ActionEvent evt) {
                saveSessionMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(saveSessionMenuItem);
        fileMenu.add(jSeparator1);

        mainMenuBar.add(fileMenu);

        settingsMenu.setText("Settings");

        settingsMenuItem.setText("Application settings...");
        settingsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final java.awt.event.ActionEvent evt) {
                settingsMenuItemActionPerformed(evt);
            }
        });
        settingsMenu.add(settingsMenuItem);

        mainMenuBar.add(settingsMenu);

        _viewMenu.setText("View");
        _viewMenu.setMnemonic(KeyEvent.VK_V);

        _fontsLargerMenuItem = new JMenuItem("Fonts larger");
        _fontsLargerMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                _fontSize++;
                updateFontSize();
            }
        });
        _viewMenu.add(_fontsLargerMenuItem);

        _fontsSmallerMenuItem = new JMenuItem("Fonts smaller");
        _fontsSmallerMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                if (_fontSize > 1)
                    _fontSize--;
                updateFontSize();
            }
        });
        _viewMenu.add(_fontsSmallerMenuItem);
        mainMenuBar.add(_viewMenu);

        helpMenu.setText("Help");

        manualMenuItem.setText("View Manual");
        manualMenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final java.awt.event.ActionEvent evt) {
                manualMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(manualMenuItem);

        aboutMenuItem.setText("About LRmix Studio");
        aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final java.awt.event.ActionEvent evt) {
                aboutMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(aboutMenuItem);

        mainMenuBar.add(helpMenu);

        setJMenuBar(mainMenuBar);

        final javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 782, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainTabbedPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 629, Short.MAX_VALUE)
        );

        mainTabbedPane.getAccessibleContext().setAccessibleName("");

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void aboutMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutMenuItemActionPerformed
        mainTabbedPane.setSelectedIndex(mainTabbedPane.getTabCount() - 1);
    }//GEN-LAST:event_aboutMenuItemActionPerformed

    private void manualMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manualMenuItemActionPerformed
        if (Desktop.isDesktopSupported()) {
            final File file = new File("manual.pdf");
            if (!file.exists()) {
                LOG.error("Manual file 'manual.pdf' could not be found in the application folder!");
                JOptionPane.showMessageDialog(this, "<html>Error displaying the manual:<br><i>The manual file could not be found!", "LRmix Studio Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                Desktop.getDesktop().open(file);
            } catch (final IOException ioe) {
                LOG.error("Error showing manual!", ioe);
                // No configured viewer for PDF files.
                JOptionPane.showMessageDialog(this, "<html>Your system cannot display <b>PDF</b> files!<br><i>Please install a PDF viewer to view the manual.", "LRmix Studio Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_manualMenuItemActionPerformed

    private void openSessionMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openSessionMenuItemActionPerformed
        if (JOptionPane.OK_OPTION == LostTimeCheck.performRestartCheck(_session, this)) {
            final JFileChooser chooser = new JFileChooser(ApplicationSettings.getCaseFilesPath());
            chooser.setAcceptAllFileFilterUsed(true);
            chooser.setFileFilter(new FileFilter() {
                @Override
                public boolean accept(final File f) {
                    return f.isDirectory() || f.getName().matches(".*\\.LRmixStudioSession");
                }

                @Override
                public String getDescription() {
                    return "LRmix Studio saved sessions (*.LRmixStudioSession)";
                }
            });
            chooser.addChoosableFileFilter(new FileFilter() {
                @Override
                public boolean accept(final File f) {
                    return f.isDirectory() || f.getName().matches("LRmixStudio.*\\.log");
                }

                @Override
                public String getDescription() {
                    return "LRmix Studio log files (LRmixStudio*.log)";
                }
            });

            if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
                if (_session.restore(new PathResolver(this), chooser.getSelectedFile())) {
                    ApplicationSettings.addMostRecentlyUsed(chooser.getSelectedFile().getAbsolutePath());
                    updateMRUList();
                }
            }
        }
    }//GEN-LAST:event_openSessionMenuItemActionPerformed

    private void saveSessionMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveSessionMenuItemActionPerformed
        final JFileChooser chooser = new JFileChooser(ApplicationSettings.getCaseFilesPath());
        chooser.setAcceptAllFileFilterUsed(true);
        chooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(final File f) {
                return f.isDirectory() || f.getName().matches(".*\\.LRmixStudioSession");
            }

            @Override
            public String getDescription() {
                return "LRmix Studio saved sessions (*.LRmixStudioSession)";
            }
        });

        if (JFileChooser.APPROVE_OPTION == chooser.showSaveDialog(this)) {

            String fileName = chooser.getSelectedFile().getAbsoluteFile().getAbsolutePath();
            if (!fileName.matches(".+\\..+")) {
                fileName += ".LRmixStudioSession";
            }
            if (_session.save(fileName)) {
                ApplicationSettings.addMostRecentlyUsed(fileName);
                updateMRUList();
            }
        }
    }//GEN-LAST:event_saveSessionMenuItemActionPerformed

    private void newSessionMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newSessionMenuItemActionPerformed
        if (JOptionPane.OK_OPTION == LostTimeCheck.performRestartCheck(_session, this)) {
            _session.clear();
        }
    }//GEN-LAST:event_newSessionMenuItemActionPerformed

    private void settingsMenuItemActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_settingsMenuItemActionPerformed
        final SettingsDialog dlg = new SettingsDialog(this, true);
        dlg.setVisible(true);
        updateMRUList();
    }//GEN-LAST:event_settingsMenuItemActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(final String args[]) {
        try {
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            LOG.error("Error setting system look and feel", ex);
        }

        /* Create and display the form */
        final LRmixStudio lRmixStudio = new LRmixStudio();
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                lRmixStudio.setVisible(true);
            }
        });

        if (args.length > 0) {
            java.awt.EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (lRmixStudio.loadLog(new File(args[0]))) {
                        lRmixStudio.mainTabbedPane.setSelectedIndex(3);
                    }
                }
            });
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private nl.minvenj.nfi.lrmixstudio.gui.tabs.noncontributor.NonContributorTestPanel _nonContributorTestPanel;
    private javax.swing.JMenuItem aboutMenuItem;
    private nl.minvenj.nfi.lrmixstudio.gui.tabs.about.AboutPanel aboutPanel;
    private nl.minvenj.nfi.lrmixstudio.gui.tabs.analysis.AnalysisPanel analysisPanel;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JMenuBar mainMenuBar;
    private javax.swing.JTabbedPane mainTabbedPane;
    private javax.swing.JMenuItem manualMenuItem;
    private javax.swing.JMenuItem newSessionMenuItem;
    private javax.swing.JMenuItem openSessionMenuItem;
    private nl.minvenj.nfi.lrmixstudio.gui.tabs.profiles.ProfilesPanel profilePanel;
    private nl.minvenj.nfi.lrmixstudio.gui.tabs.profilesummary.ProfileSummaryPanel profileSummaryPanel;
    private nl.minvenj.nfi.lrmixstudio.gui.tabs.reports.ReportsPanel reportsPanel;
    private nl.minvenj.nfi.lrmixstudio.gui.tabs.replicates.ReplicatesPanel samplePanel;
    private javax.swing.JMenuItem saveSessionMenuItem;
    private nl.minvenj.nfi.lrmixstudio.gui.tabs.sensitivity.SensitivityAnalysisPanel sensitivityAnalysisPanel;
    private javax.swing.JMenu settingsMenu;
    private javax.swing.JMenuItem settingsMenuItem;
    // End of variables declaration//GEN-END:variables

    @Override
    public void dataChanged(final ConfigurationDataElement element) {
        switch (element) {
            case CASENUMBER:
                if (_session.getCaseNumber().isEmpty()) {
                    setTitle("LRmixStudio");
                } else {
                    setTitle("LRmixStudio - " + _session.getCaseNumber());
                }
                LOG.debug("Window title set to '{}'", getTitle());
                break;
            case ERROR_MESSAGE:
                LOG.error("Showing error message: {}", _session.getErrorMessage());
                JOptionPane.showMessageDialog(rootPane, _session.getErrorMessage(), "LRmixStudio v" + ApplicationSettings.getProgramVersion(), JOptionPane.ERROR_MESSAGE);
                break;
            default:
                // Nothing to do for other elements
        }
    }

    @Override
    public void applicationStateChanged(final APP_STATE newState) {
        LOG.debug("Application state: {}", newState);

        switch (newState) {
            case WAIT_SAMPLE:
                mainTabbedPane.setSelectedIndex(0);
                break;
            default:
                // Nothing to do for other states
        }

        // Enable or disable the tabs in the main tab pane
        for (int tabIdx = 0; tabIdx < mainTabbedPane.getTabCount(); tabIdx++) {
            mainTabbedPane.setEnabledAt(tabIdx, mainTabbedPane.getComponentAt(tabIdx).isEnabled());
        }

        mainMenuBar.setEnabled(!newState.isActive());
        for (int idx = 0; idx < mainMenuBar.getComponentCount(); idx++) {
            mainMenuBar.getComponent(idx).setEnabled(mainMenuBar.isEnabled());
        }
    }

    /**
     * @param zebraTable
     */
    public static void addFontChangeListener(final ZebraTable zebraTable) {
        _fontListeners.add(zebraTable);
        zebraTable.setFont(zebraTable.getFont().deriveFont(new Float(_fontSize)));
    }

    private void updateFontSize() {
        ApplicationSettings.setFontSize(_fontSize);
        for (final Component listener : _fontListeners) {
            listener.setFont(listener.getFont().deriveFont(new Float(_fontSize)));
        }
    }

}
