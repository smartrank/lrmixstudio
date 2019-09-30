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

import java.awt.Color;
import java.awt.Component;
import java.util.EventObject;

import javax.swing.JSpinner;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZebraTableCellEditor implements TableCellEditor {

    private static final Logger LOG = LoggerFactory.getLogger(ZebraTableCellEditor.class);
    private final TableCellEditor proxyEditor;
    private final String name;

    public ZebraTableCellEditor(TableCellEditor proxy, String name) {
        this.proxyEditor = proxy;
        this.name = name;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        Component component = proxyEditor.getTableCellEditorComponent(table, value, isSelected, row, column);
        Color evenRowColor = ApplicationSettings.getEvenRowColor(name);
        Color oddRowColor = ApplicationSettings.getOddRowColor(name);

        if (!table.isEnabled()) {
            evenRowColor = toGrayScale(evenRowColor);
            oddRowColor = toGrayScale(oddRowColor);
        }
        
        component.setEnabled(table.isEnabled());
        
        if (component instanceof JSpinner) {
            ((DefaultEditor) ((JSpinner) component).getEditor()).getTextField().setBackground((row % 2) == 0 ? evenRowColor : oddRowColor);
        } else {
            component.setBackground((row % 2) == 0 ? evenRowColor : oddRowColor);
        }
        return component;
    }

    private Color toGrayScale(Color color) {
        int component = color.getRed();
        if (color.getBlue() < component) {
            component = color.getBlue();
        }
        if (color.getGreen() < component) {
            component = color.getGreen();
        }
        return new Color(component, component, component);
    }

    @Override
    public Object getCellEditorValue() {
        return proxyEditor.getCellEditorValue();
    }

    @Override
    public boolean isCellEditable(EventObject anEvent) {
        return proxyEditor.isCellEditable(anEvent);
    }

    @Override
    public boolean shouldSelectCell(EventObject anEvent) {
        return proxyEditor.shouldSelectCell(anEvent);
    }

    @Override
    public boolean stopCellEditing() {
        return proxyEditor.stopCellEditing();
    }

    @Override
    public void cancelCellEditing() {
        proxyEditor.cancelCellEditing();
    }

    @Override
    public void addCellEditorListener(CellEditorListener l) {
        proxyEditor.addCellEditorListener(l);
    }

    @Override
    public void removeCellEditorListener(CellEditorListener l) {
        proxyEditor.removeCellEditorListener(l);
    }
}
