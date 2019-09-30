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
package nl.minvenj.nfi.lrmixstudio.report.jasper;

import static java.lang.Thread.sleep;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.ProgressMonitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.fill.JRExpressionEvalException;
import net.sf.jasperreports.swing.JRViewer;
import nl.minvenj.nfi.lrmixstudio.gui.ApplicationStateChangeListener;
import nl.minvenj.nfi.lrmixstudio.gui.SessionData;
import nl.minvenj.nfi.lrmixstudio.model.AnalysisReport;
import nl.minvenj.nfi.lrmixstudio.report.jasper.api.JasperDataSource;

public class ReportGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(ReportGenerator.class);

    private class WatchDogThread extends Thread {

        private final Thread _thread;
        private final ProgressMonitor _progressMonitor;

        public WatchDogThread(ProgressMonitor monitor, Thread thread) {
            _progressMonitor = monitor;
            _thread = thread;
        }

        @Override
        public void run() {
            _thread.start();
            while (_thread.isAlive()) {
                if (_progressMonitor.isCanceled()) {
                    _thread.interrupt();
                }
                try {
                    sleep(100);
                } catch (InterruptedException ex) {
                    _thread.interrupt();
                }
            }
        }
    }

    private class GeneratorThread extends Thread {

        private final ProgressMonitor _monitor;
        private final String _filename;
        private final Collection<AnalysisReport> _reports;
        private final SessionData _session;
        private final JFrame _parent;
        private final String _remarks;

        public GeneratorThread(ProgressMonitor monitor, JFrame parent, SessionData session, Collection<AnalysisReport> reports, String remarks, String fileName) {
            _monitor = monitor;
            _filename = fileName;
            _reports = reports;
            _session = session;
            _parent = parent;
            _remarks = remarks;
        }

        @Override
        public void run() {

            try {
                _session.setApplicationState(ApplicationStateChangeListener.APP_STATE.EXPORT_RUNNING);
                _monitor.setProgress(1);
                _monitor.setNote("Loading report definition");
                LOG.debug("Attempting to load report definition");
                InputStream reportStream = new FileInputStream("report/LRmixStudio.jrxml");

                Map<String, Object> parameters = new HashMap<>();

                _monitor.setProgress(2);
                _monitor.setNote("Compiling report definition");
                LOG.debug("Compiling report");
                JasperReport jReport = JasperCompileManager.compileReport(reportStream);

                _monitor.setProgress(3);
                _monitor.setNote("Filling report");
                LOG.debug("Filling report");
                JasperPrint print = JasperFillManager.fillReport(jReport, parameters, new JasperDataSource(_session, _remarks, _reports));

                // Always export to pdf, regardless of what the user does in the dialog
                _monitor.setProgress(4);
                _monitor.setNote("Saving report file");
                LOG.debug("Saving report to {}", _filename);
                JasperExportManager.exportReportToPdfFile(print, _filename);

                // Close the progress monitor 
                _monitor.setProgress(_monitor.getMaximum());

                // Open the saved file in the configured viewer
                if (Desktop.isDesktopSupported()) {
                    File file = new File(_filename);
                    try {
                        Desktop.getDesktop().open(file);
                    } catch (IOException ioe) {
                        // No configured viewer for PDF files. Revert to JRViewer.
                        showPdf(print);
                    }
                } else {
                    showPdf(print);
                }
            } catch (JRExpressionEvalException ex) {
                LOG.error("Error evaluation expression in the report", ex);
                _session.setErrorMessage("The report definition file contains an error. See the logfile for details.");
            } catch (JRException ex) {
                LOG.error("Error building report", ex);
                _session.setErrorMessage(ex.getMessage());
            } catch (Throwable t) {
                LOG.error("There was an error creating the report:", t);
                _session.setErrorMessage(t.getMessage());
            } finally {
                // Close the progress monitor 
                _monitor.setProgress(_monitor.getMaximum());

                _session.setApplicationState(ApplicationStateChangeListener.APP_STATE.READY_FOR_ANALYSIS);
            }
        }

        private void showPdf(JasperPrint print) {
            JFrame frame = new JFrame(_filename);
            frame.setAlwaysOnTop(true);
            frame.add(new JRViewer(print));
            frame.setSize(_parent.getSize());
            frame.setVisible(true);
            frame.setLocationRelativeTo(_parent);
        }
    }

    public void generate(JFrame parent, SessionData session, Collection<AnalysisReport> reports, String remarks, String fileName) {
        ProgressMonitor monitor = new ProgressMonitor(parent, "Exporting report", "", 0, 5);
        monitor.setMillisToPopup(0);
        monitor.setMillisToDecideToPopup(0);
        new WatchDogThread(monitor, new GeneratorThread(monitor, parent, session, reports, remarks, fileName)).start();
    }
}
