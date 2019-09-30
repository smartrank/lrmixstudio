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
package nl.minvenj.nfi.lrmixstudio.gui.tabs.profilesummary;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.Printable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventObject;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.minvenj.nfi.lrmixstudio.domain.Allele;
import nl.minvenj.nfi.lrmixstudio.domain.Locus;
import nl.minvenj.nfi.lrmixstudio.domain.Sample;
import nl.minvenj.nfi.lrmixstudio.gui.ApplicationSettings;
import nl.minvenj.nfi.lrmixstudio.gui.ApplicationStateChangeListener;
import nl.minvenj.nfi.lrmixstudio.gui.SessionData;
import nl.minvenj.nfi.lrmixstudio.model.ConfigurationDataElement;

/**
 *
 * @author dejong
 */
public class ProfileSummaryTable extends JTable {

    private static final Logger LOG = LoggerFactory.getLogger(ProfileSummaryTable.class);
    public static final Color ODD_ROW_COLOR = ApplicationSettings.getOverviewOddRowColor();
    public static final Color EVEN_ROW_COLOR = ApplicationSettings.getOverviewEvenRowColor();
    public static final Color REPLICATE_ROW_COLOR = ApplicationSettings.getOverviewReplicateRowColor();
    public static final Color REPLICATE_ROW_TEXT_COLOR = ApplicationSettings.getOverviewReplicateRowTextColor();
    private final ArrayList<Sample> _replicates = new ArrayList<>();
    private final ArrayList<Sample> _profiles = new ArrayList<>();
    private AlleleDecorator _decorator = new DefaultDecorator();
    private SessionData _session;
    private PropertyChangeListener _alleleCountListener;

    @Override
    public DefaultTableModel getModel() {
        return (DefaultTableModel) super.getModel();
    }

    public void setAlleleCountListener(PropertyChangeListener listener) {
        _alleleCountListener = listener;
    }

    private void updateTableModel() {
        LOG.debug("Updating table model");
        ProfileSummaryTableModel model = new ProfileSummaryTableModel();

        getTableHeader().setResizingAllowed(true);

        model.addColumn("Select");
        model.addColumn("Name");
        model.addColumn("Replicate");

        setModel(model);
        setShowGrid(ApplicationSettings.isOverviewGridShown());

        setDefaultRenderer(Boolean.class, new BooleanRenderer());
        setDefaultEditor(Boolean.class, new BooleanEditor());
        setDefaultRenderer(Sample.class, new DefaultTableCellRendererImpl(_session));
        setDefaultRenderer(String.class, new DefaultTableCellRendererImpl(_session));

        for (Sample profile : _profiles) {
            model.addColumn(profile.getId());
        }
        model.addColumn("Distinct Alleles");

        _decorator.resetCount();
        int maxCount = 0;

        for (Sample replicate : _replicates) {
            LOG.debug("Adding replicate {}", replicate.getId());
            ArrayList rowData = new ArrayList();
            rowData.add(replicate.isEnabled());
            rowData.add(replicate);
            model.addRow(rowData.toArray());

            for (String locusName : _session.getAllLoci()) {
                LOG.debug("Adding Locus {}", locusName);
                Locus locus = replicate.getLocus(locusName);
                rowData = new ArrayList();
                if (locus == null) {
                    locus = new Locus(locusName);
                    locus.setSample(replicate);
                }
                ArrayList<Allele> uniqueAlleles = new ArrayList<>();
                rowData.add(_session.isLocusEnabled(locus.getName()));
                rowData.add(locus);

                for (Allele a : locus.getAlleles()) {
                    if (!uniqueAlleles.contains(a)) {
                        uniqueAlleles.add(a);
                    }
                }

                rowData.add(_decorator.apply(replicate, replicate, locus.getName(), locus.getAlleles().toString().replaceAll("[\\[\\]\\,\\']*", "")));
                for (Sample profile : _profiles) {
                    LOG.debug("Adding profile {}", profile);
                    Locus profileLocus = profile.getLocus(locus.getName());
                    if (profileLocus != null) {
                        rowData.add(_decorator.apply(replicate, profile, profileLocus.getName(), profileLocus.getAlleles().toString().replaceAll("[\\[\\]\\,\\']*", "")));
                    }
                    else {
                        rowData.add("");
                    }
                }
                LOG.debug("Unique Alleles: {}", uniqueAlleles);
                if (replicate.isEnabled() && Boolean.parseBoolean(rowData.get(0).toString())) {
                    rowData.add(uniqueAlleles.size());
                    if (uniqueAlleles.size() > maxCount) {
                        maxCount = uniqueAlleles.size();
                    }
                }
                else {
                    rowData.add(-uniqueAlleles.size());
                }
                LOG.debug("Rowdata = {}", rowData);
                model.addRow(rowData.toArray());
            }
        }
        _alleleCountListener.propertyChange(new PropertyChangeEvent(this, "alleleCount", 0, _decorator.getHighlightedAlleleCount()));

        for (int idx = 0; idx < model.getRowCount(); idx++) {
            if (model.getValueAt(idx, model.getColumnCount() - 1) instanceof Integer) {
                Integer i = (Integer) model.getValueAt(idx, model.getColumnCount() - 1);
                String count = i.toString().replaceAll("-", "");
                if (i == maxCount) {
                    model.setValueAt("<html>" + _decorator.highlight(count, i >= 0), idx, model.getColumnCount() - 1);
                }
                else {
                    if (i < 0) {
                        model.setValueAt("<html>" + _decorator.disable(count), idx, model.getColumnCount() - 1);
                    }
                    else {
                        model.setValueAt("<html>" + count, idx, model.getColumnCount() - 1);
                    }
                }
            }
        }

        TableColumn col = getColumnModel().getColumn(0);
        col.setMaxWidth(50);
        col.setResizable(false);
    }

    public void addReplicates(Collection<Sample> replicates) {
        _replicates.addAll(replicates);
        updateTableModel();
    }

    public void addProfiles(Collection<Sample> profiles) {
        _profiles.addAll(profiles);
        updateTableModel();
    }

    public void clear() {
        _replicates.clear();
        _profiles.clear();
        getModel().setRowCount(0);
    }

    /**
     * Sets the _decorator used to highlight certain alleles in the table
     *
     * @param alleleDecorator The _decorator that will supply the code to
     *                        highlight alleles
     *
     */
    void setDecorator(AlleleDecorator decorator) {
        if (decorator == null) {
            this._decorator = new DefaultDecorator();
        }
        else {
            this._decorator = decorator;
        }
        updateTableModel();
    }

    public void setContext(SessionData session) {
        LOG.debug("setContext");
        this._session = session;
        setModel(new ProfileSummaryTableModel());
    }

    @Override
    public Printable getPrintable(PrintMode printMode, MessageFormat headerFormat, MessageFormat footerFormat) {
        Printable p = super.getPrintable(printMode, null, footerFormat);
        return new ProfileSummaryPrintable(p, headerFormat.toPattern(), _decorator.getDescription().replaceAll("\\<[^\\<]*\\>", ""));
    }

    public void update() {
        updateTableModel();
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        if (e.getType() == TableModelEvent.UPDATE) {
            if (getModel().getRowCount() > 0) {
                try {
                    Object obj = getValueAt(e.getFirstRow(), 1);
                    if (obj instanceof Locus) {
                        Boolean isSelected = (Boolean) getValueAt(e.getFirstRow(), 0);
                        String locusName = getValueAt(e.getFirstRow(), 1).toString();
                        _session.setLocusEnabled(locusName, isSelected);
                        for (int idx = 0; idx < getModel().getRowCount(); idx++) {
                            if (getValueAt(idx, 1).toString().equalsIgnoreCase(locusName) && getValueAt(idx, 0) != isSelected) {
                                setValueAt(isSelected, idx, 0);
                            }
                        }
                        // If the user disables all loci, we should not allow them to continue
                        if (_session.getEnabledLoci().isEmpty()) {
                            _session.setApplicationState(ApplicationStateChangeListener.APP_STATE.READY_FOR_OVERVIEW);
                        }
                        else {
                            if (!_session.getActiveProfiles().isEmpty()) {
                                checkMissingLoci();
                            }
                        }
                    }
                    if (obj instanceof Sample) {
                        Boolean isSelected = (Boolean) getValueAt(e.getFirstRow(), 0);
                        Sample s = (Sample) getValueAt(e.getFirstRow(), 1);
                        s.setEnabled(isSelected);
                        _session.fireUpdated(ConfigurationDataElement.ACTIVEREPLICATES);
                        if (_session.getActiveReplicates().isEmpty()) {
                            _session.setApplicationState(ApplicationStateChangeListener.APP_STATE.READY_FOR_OVERVIEW);
                        }
                        else {
                            if (!_session.getActiveProfiles().isEmpty()) {
                                checkMissingLoci();
                            }
                        }
                    }
                }
                catch (ArrayIndexOutOfBoundsException aoobe) {
                    LOG.warn("", aoobe);
                }
            }
            super.tableChanged(e);
            repaint();
        }
        else {
            super.tableChanged(e);
        }
    }

    private void checkMissingLoci() {
        if (_session != null && !_session.getActiveReplicates().isEmpty() && !_session.getActiveProfiles().isEmpty()) {
            // Check if all loci in the replicates feature in all profiles
            for (Sample replicate : _session.getActiveReplicates()) {
                for (Locus replicateLocus : replicate.getLoci()) {
                    String locusName = replicateLocus.getName();
                    for (Sample profile : _session.getActiveProfiles()) {
                        if (_session.isLocusValid(locusName) && _session.isLocusEnabled(locusName)) {
                            Locus profileLocus = profile.getLocus(locusName);
                            if (profileLocus == null) {
                                LOG.debug("Profile {} does not contain locus {}", profile, locusName);
                                _session.setStatusMessage("Profile " + profile.getId() + " does not contain locus " + locusName + ". You cannot continue unless the locus is disabled.");
                                _session.setApplicationState(ApplicationStateChangeListener.APP_STATE.READY_FOR_OVERVIEW);
                                return;
                            }
                        }
                    }
                }
            }
            _session.setStatusMessage("");
            _session.setApplicationState(ApplicationStateChangeListener.APP_STATE.READY_FOR_ANALYSIS);
        }
    }

    public class ProfileSummaryTableModel extends DefaultTableModel {

        private int _highlightedAlleleCount;

        public void setHighlightedAlleleCount(int count) {
            _highlightedAlleleCount = count;
        }

        public int getHighlightedAlleleCount() {
            return _highlightedAlleleCount;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 0) {
                return Boolean.class;
            }
            if (columnIndex == 1) {
                return Sample.class;
            }
            return String.class;
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            Object o = getValueAt(row, 1);
            if (o instanceof Locus) {
                return column == 0 && _session.isLocusValid(((Locus) o).getName());
            }
            return column == 0;
        }
    }

    private class DefaultTableCellRendererImpl extends DefaultTableCellRenderer {

        private final SessionData session;

        public DefaultTableCellRendererImpl(final SessionData session) {
            this.session = session;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (table.getValueAt(row, 1) instanceof Sample) {
                setBackground(REPLICATE_ROW_COLOR);
                setForeground(REPLICATE_ROW_TEXT_COLOR);
            }
            else {
                Locus locus = (Locus) table.getValueAt(row, 1);
                setBackground(row % 2 == 0 ? EVEN_ROW_COLOR : ODD_ROW_COLOR);
                if (session.isLocusEnabled(locus.getName()) && locus.getSample().isEnabled()) {
                    setForeground(Color.BLACK);
                }
                else {
                    setForeground(Color.LIGHT_GRAY);
                }
            }
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }

    private class BooleanEditor extends JCheckBox implements TableCellEditor {

        private final Border _noFocusBorder = new EmptyBorder(1, 1, 1, 1);
        private JTable _table;
        private int _row;

        public BooleanEditor() {
            super();
            setBorderPainted(true);
            setBorder(_noFocusBorder);
            setOpaque(true);
            addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    _table.setValueAt(isSelected(), _row, 0);
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            _table = table;
            _row = row;
            if (table.getValueAt(row, 1) instanceof Sample) {
                setHorizontalAlignment(JLabel.LEFT);
                setBackground(REPLICATE_ROW_COLOR);
                setEnabled(true);
            }
            else {
                setHorizontalAlignment(JLabel.CENTER);
                Locus locus = (Locus) table.getValueAt(row, 1);
                setBackground(row % 2 == 0 ? EVEN_ROW_COLOR : ODD_ROW_COLOR);
                setEnabled(locus.getSample().isEnabled() && _session.isLocusValid(locus.getName()));
            }
            setSelected(value != null && ((Boolean) value));
            return this;
        }

        @Override
        public Object getCellEditorValue() {
            return isSelected();
        }

        @Override
        public boolean isCellEditable(EventObject anEvent) {
            return true;
        }

        @Override
        public boolean shouldSelectCell(EventObject anEvent) {
            return false;
        }

        @Override
        public boolean stopCellEditing() {
            return true;
        }

        @Override
        public void cancelCellEditing() {
        }

        @Override
        public void addCellEditorListener(CellEditorListener l) {
        }

        @Override
        public void removeCellEditorListener(CellEditorListener l) {
        }
    }

    private class BooleanRenderer extends JCheckBox implements TableCellRenderer {

        private final Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

        public BooleanRenderer() {
            super();
            setBorderPainted(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            if (table.getValueAt(row, 1) instanceof Sample) {
                setHorizontalAlignment(JLabel.LEFT);
                setBackground(REPLICATE_ROW_COLOR);
                setEnabled(true);
                setSelected((value != null && ((Boolean) value)));
            }
            else {
                setHorizontalAlignment(JLabel.CENTER);
                Locus locus = (Locus) table.getValueAt(row, 1);
                boolean locusValid = _session.isLocusValid(locus.getName());
                setBackground(row % 2 == 0 ? EVEN_ROW_COLOR : ODD_ROW_COLOR);
                setEnabled(locus.getSample().isEnabled() && locusValid);
                if (!locusValid) {
                    setToolTipText(_session.getLocusStatus(locus.getName()));
                    setSelected(false);
                }
                else {
                    setToolTipText(null);
                    setSelected((value != null && ((Boolean) value)));
                }
            }

            if (hasFocus) {
                setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
            }
            else {
                setBorder(noFocusBorder);
            }

            return this;
        }
    }
}
