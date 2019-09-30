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

import java.awt.EventQueue;
import java.io.File;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.minvenj.nfi.lrmixstudio.io.PopulationStatisticsReader;

public class AlleleFrequencyDocumentListener implements DocumentListener {
    private static final Logger LOG = LoggerFactory.getLogger(AlleleFrequencyDocumentListener.class);
    private final ProgressGui gui;
    private final SessionData session;
    private final JTextField _parent;

    /**
     *
     * @param session
     * @param parent
     * @param gui
     */
    public AlleleFrequencyDocumentListener(SessionData session, JTextField parent, ProgressGui gui) {
        this.session = session;
        this.gui = gui;
        _parent = parent;
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        try {
            alleleFrequencyFilenameChanged(e.getDocument().getText(0, e.getDocument().getLength()));
        } catch (BadLocationException ex) {
            LOG.error("{}", ex);
        }
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        try {
            alleleFrequencyFilenameChanged(e.getDocument().getText(0, e.getDocument().getLength()));
        } catch (BadLocationException ex) {
            LOG.error("{}", ex);
        }
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        try {
            alleleFrequencyFilenameChanged(e.getDocument().getText(0, e.getDocument().getLength()));
        } catch (BadLocationException ex) {
            LOG.error("{}", ex);
        }
    }

    private void alleleFrequencyFilenameChanged(final String fileName) {
        try {
            if (fileName.isEmpty()) {
                return;
            }
            PopulationStatisticsReader popStatCsv = new PopulationStatisticsReader(fileName);
            ApplicationSettings.setAlleleFrequenciesPath(fileName);
            session.setStatistics(popStatCsv.getStatistics());
        } catch (Exception ex) {
            session.setErrorMessage("<html>Cannot load population statistics file <b>" + new File(fileName).getName() + "</b>:<br>" + ex);
            ApplicationSettings.setAlleleFrequenciesPath("");
            EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    _parent.setText("");
                }

            });
        }
    }
}
