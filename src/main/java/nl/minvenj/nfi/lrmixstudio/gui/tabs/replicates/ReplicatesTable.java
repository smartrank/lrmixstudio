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

import java.awt.Color;
import java.util.Collection;
import java.util.HashMap;

import javax.swing.table.DefaultTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.minvenj.nfi.lrmixstudio.domain.Allele;
import nl.minvenj.nfi.lrmixstudio.domain.Locus;
import nl.minvenj.nfi.lrmixstudio.domain.Sample;
import nl.minvenj.nfi.lrmixstudio.gui.SessionData;
import nl.minvenj.nfi.lrmixstudio.gui.ZebraTable;
import nl.minvenj.nfi.lrmixstudio.gui.tabs.profiles.ProfilesTable;

public class ReplicatesTable extends ZebraTable {
    public static final Color ODD_ROW_COLOR = new Color(245, 255, 255);
    public static final Color EVEN_ROW_COLOR = Color.WHITE;

    private static final Logger LOG = LoggerFactory.getLogger(ProfilesTable.class);

    private final HashMap<String, Integer> _rowIndices = new HashMap<>();
    private SessionData _session;


    public void setSession(final SessionData session) {
        _session = session;
    }

    public void clear()
    {
        final DefaultTableModel model = new DefaultTableModel() {
            @Override
            public Class<?> getColumnClass(final int columnIndex) {
                return String.class;
            }

            @Override
            public boolean isCellEditable(final int row, final int column) {
                return false;
            }
        };
        getTableHeader().setReorderingAllowed(false);
        setModel(model);

        model.addColumn("<html><i>Locus");
        _rowIndices.clear();
    }

    public void setSamples(final Collection<Sample> samples) {
        LOG.debug("Adding samples in {}", samples);
        clear();
        for (final Sample sample : samples) {
            addSample(sample);
        }
        repaint();
    }

    public void addSample(final Sample sample) {
        LOG.debug("Adding sample {}", sample.getId());
        final DefaultTableModel model = (DefaultTableModel) getModel();
        model.addColumn("<html><b>" + sample);

        for (final String locusName : _session.getAllLoci()) {
            final Locus locus = sample.getLocus(locusName);
            String value = "";

            if (locus != null) {
                for (final Allele allele : locus.getAlleles()) {
                    value += (value.isEmpty() ? "" : getAlleleSeparator()) + allele.getAllele();
                }

                if (locus.isTreatedAsHomozygote()) {
                    value = value.replace(getAlleleSeparator(), getAlleleSeparator() + "<span style='color: white;background-color:red;font-weight: bold;'>&nbsp;") + "&nbsp;";
                }
            }
            value = "<html>" + value;

            if (_rowIndices.containsKey(locusName)) {
                final Integer rowIdx = _rowIndices.get(locusName);
                model.setValueAt(value, rowIdx, model.getColumnCount() - 1);
            }
            else {
                model.addRow(new Object[]{locusName});
                model.setValueAt(value, model.getRowCount() - 1, model.getColumnCount() - 1);
                _rowIndices.put(locusName, model.getRowCount() - 1);
            }
        }
    }

    protected String getAlleleSeparator() {
        return " ";
    }
}
