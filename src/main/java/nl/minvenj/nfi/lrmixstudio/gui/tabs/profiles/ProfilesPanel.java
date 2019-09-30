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
package nl.minvenj.nfi.lrmixstudio.gui.tabs.profiles;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

import nl.minvenj.nfi.lrmixstudio.domain.Locus;
import nl.minvenj.nfi.lrmixstudio.domain.Sample;
import nl.minvenj.nfi.lrmixstudio.gui.tabs.replicates.ReplicatesPanel;
import nl.minvenj.nfi.lrmixstudio.gui.tabs.replicates.ReplicatesTable;
import nl.minvenj.nfi.lrmixstudio.model.ConfigurationDataElement;

/**
 *
 * @author dejong
 */
public class ProfilesPanel extends ReplicatesPanel {

    public ProfilesPanel() {
        super();
        addProfileButton.setText("Add profile...");
        addProfileButton.setVisible(true);
        clearButton.setVisible(false);
        caseLabel.setVisible(false);
        caseNumber.setVisible(false);
        loadLogButton.setVisible(false);
        _samplesTable.setName("profiles");
        _detailTable.setName("profileAlleles");
        ignoreDuplicateAlleles = false;
        _detailTable.setSession(session);
    }

    @Override
    protected void addSamples(final Collection<Sample> samples) {
        boolean foundSingleAllele = false;
        String homofied = "";
        for (final Iterator<Sample> sampleIterator = samples.iterator(); !foundSingleAllele && sampleIterator.hasNext();) {
            final Sample sample = sampleIterator.next();
            for (final Iterator<Locus> locusIterator = sample.getLoci().iterator(); !foundSingleAllele && locusIterator.hasNext();) {
                final Locus locus = locusIterator.next();
                foundSingleAllele |= (locus.size() == 1);
                if (foundSingleAllele) {
                    homofied += sample.getId() + ", ";
                }
            }
        }

        if (foundSingleAllele) {
            homofied = homofied.substring(0, homofied.length() - 2);
            final int idx = homofied.lastIndexOf(",");
            if (idx > 0) {
                homofied = homofied.substring(0, idx) + " and" + homofied.substring(idx + 1);
            }
        }

        if (foundSingleAllele && JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(this, "<html>At least one locus in <b>" + homofied + "</b> contains only one allele.<br>Do you want to convert these loci to homozygotic?", "LRMixStudio", JOptionPane.YES_NO_OPTION)) {
            for (final Sample sample : samples) {
                for (final Locus locus : sample.getLoci()) {
                    if (locus.size() == 1) {
                        locus.addAllele(locus.getAlleles().iterator().next());
                        locus.setTreatedAsHomozygote();
                    }
                }
            }
        }

        session.addProfiles(samples);
    }

    private void showWarningPanel() {
        boolean needWarningPanel = false;

        for (final Iterator<Sample> sampleIterator = session.getActiveProfiles().iterator(); !needWarningPanel && sampleIterator.hasNext();) {
            final Sample sample = sampleIterator.next();
            for (final Iterator<Locus> locusIterator = sample.getLoci().iterator(); !needWarningPanel && locusIterator.hasNext();) {
                final Locus locus = locusIterator.next();
                needWarningPanel = locus.isTreatedAsHomozygote();
            }
        }
        _warningPanel.setVisible(needWarningPanel);
    }

    @Override
    protected ReplicatesTable getSamplesTable() {
        return new ProfilesTable();
    }

    @Override
    protected String getChooserDialogTitle() {
        return "Please select a file containing profile data";
    }

    @Override
    protected ConfigurationDataElement getConfigurationElement() {
        return ConfigurationDataElement.ACTIVEPROFILES;
    }

    @Override
    public void dataChanged(final ConfigurationDataElement target) {
       switch (target) {
            case PROFILES:
                ((DefaultTableModel) _samplesTable.getModel()).setRowCount(0);
                for (final Sample sample : session.getAllProfiles()) {
                    ((DefaultTableModel) _samplesTable.getModel()).addRow(new Object[]{sample.isEnabled(), sample, sample.getSourceFile().substring(sample.getSourceFile().lastIndexOf(File.separator) + 1)});
                }
                if (session.getActiveProfiles().isEmpty()) {
                    session.setApplicationState(APP_STATE.WAIT_PROFILES);
                }
                else {
                    session.setApplicationState(APP_STATE.READY_FOR_ANALYSIS);
                }
                showWarningPanel();
                break;
            case ACTIVEPROFILES:
                if (session.getActiveProfiles().isEmpty()) {
                    session.setApplicationState(APP_STATE.WAIT_PROFILES);
                }
                else {
                    session.setApplicationState(APP_STATE.READY_FOR_ANALYSIS);
                }
                showWarningPanel();
                break;
        }
    }

    @Override
    public void applicationStateChanged(final APP_STATE newState) {
        setEnabled(!newState.isActive() && newState != APP_STATE.WAIT_SAMPLE);
    }

}
