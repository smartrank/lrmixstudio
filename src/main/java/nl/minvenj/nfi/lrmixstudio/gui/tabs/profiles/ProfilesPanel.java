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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

        final Map<String, String> emptyLoci = new HashMap<>();
        final List<Locus> singleAlleleLoci = new ArrayList<>();
        String singleAlleleSamples = "";

        // Iterate over loci in new samples
        for (final Sample sample : samples) {
            for (final Locus locus : sample.getLoci()) {
                // Record and disable locus if empty or null
                if (locus.size() == 0) {
                    emptyLoci.put(sample.getId(), emptyLoci.getOrDefault(sample.getId(), "") + ", " + locus.getName());
                    session.setLocusEnabled(locus.getName(), false);
                }

                // Record locus if it has a single allele
                if (locus.size() == 1) {
                    singleAlleleLoci.add(locus);
                    if (!singleAlleleSamples.contains(sample.getId())) {
                        singleAlleleSamples += (singleAlleleSamples.isEmpty() ? "" : ", ") + sample.getId();
                    }
                }
            }
        }

        // Ask user if single-allele loci are to be converted to homozygotic
        if (!singleAlleleLoci.isEmpty() && JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(this,
                                                                                                  "<html>At least one locus in <b>" + replaceLastCommaWithAnd(singleAlleleSamples) + "</b> contains only one allele.<br>Do you want to convert these loci to homozygotic?",
                                                                                                  "LRMixStudio", JOptionPane.YES_NO_OPTION)) {
            //  Convert single-allele loci to homozygotic if user clicks yes
            for (final Locus locus : singleAlleleLoci) {
                locus.addAllele(locus.getAlleles().iterator().next());
                locus.setTreatedAsHomozygote();
            }
        }

        // Notify user of disabled loci due to empty of null
        if (emptyLoci.size() > 0) {
            String prefix = "<html>Empty locus was detected:<br>";
            String postfix = "</UL>This locus will be disabled in the analysis.";
            String locusDescriptions = "";
            for (final String sampleName : emptyLoci.keySet()) {
                String loci = emptyLoci.get(sampleName).toString().substring(2);
                final int idx = loci.lastIndexOf(",");
                if (idx > 0) {
                    loci = loci.substring(0, idx) + "</b> and<b>" + loci.substring(idx + 1);
                    prefix = "<html>Empty loci were detected:<br>";
                    postfix = "These loci will be disabled in the analysis.";
                }

                locusDescriptions += "<b>" + loci + "</b> in " + sampleName + "<BR>";
            }
            JOptionPane.showMessageDialog(this, prefix + locusDescriptions + postfix, "LRMixStudio", JOptionPane.WARNING_MESSAGE);
        }

        session.addProfiles(samples);
    }

    /**
     * Replaces the last comma in a string with the word 'and'.
     *
     * @param stringWhichMayContainCommas
     * @return a String with the last comma replaced by the word 'and'
     */
    private String replaceLastCommaWithAnd(final String stringWhichMayContainCommas) {
        final String expanded = stringWhichMayContainCommas;
        return expanded.replaceAll("^(.*), (\\w+)$", "$1 and $2");
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
                if (session.getActiveProfiles().isEmpty() || session.getEnabledLoci().isEmpty()) {
                    session.setApplicationState(APP_STATE.WAIT_PROFILES);
                }
                else {
                    session.setApplicationState(APP_STATE.READY_FOR_ANALYSIS);
                }
                showWarningPanel();
                break;
            case ACTIVEPROFILES:
                if (session.getActiveProfiles().isEmpty() || session.getEnabledLoci().isEmpty()) {
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
