/**
 * Copyright (C) 2014 Netherlands Forensic Institute
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
package nl.minvenj.nfi.lrmixstudio.gui.tabs.reports;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.ListModel;
import javax.swing.event.ListDataListener;

import nl.minvenj.nfi.lrmixstudio.model.AnalysisReport;

/**
 *
 * @author dejong
 */
public class ReportListModel implements ListModel {

    private final ArrayList<ReportDetailPanel> backing = new ArrayList<>();

    public ReportListModel(Collection<AnalysisReport> reports, boolean fullMode) {
        if (reports != null) {
            for (AnalysisReport report : reports) {
                if (report.getLikelihoodRatio() != null && report.isSucceeded()) {
                    backing.add(new ReportDetailPanel(report, fullMode));
                    Collections.sort(backing, new Comparator<ReportDetailPanel>() {

                        @Override
                        public int compare(ReportDetailPanel o1, ReportDetailPanel o2) {
                            if (o1.getReport().getStartTime() > o2.getReport().getStartTime()) {
                                return -1;
                            }
                            if (o1.getReport().getStartTime() == o2.getReport().getStartTime()) {
                                return 0;
                            }
                            return 1;
                        }
                    });
                }
            }
        }
    }

    @Override
    public int getSize() {
        return backing.size();
    }

    @Override
    public Object getElementAt(int index) {
        return backing.get(index);
    }

    @Override
    public void addListDataListener(ListDataListener l) {
    }

    @Override
    public void removeListDataListener(ListDataListener l) {
    }

    public Iterable<ReportDetailPanel> getReportPanels() {
        return Collections.unmodifiableCollection(backing);
    }
}
