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

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.EventObject;

import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.CellEditorListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

public class LocusTableCellEditor extends JTextField implements TableCellEditor, DocumentListener, KeyListener {

    private final JTable _parent;

    private int _row;
    private int _column;

    public LocusTableCellEditor(JTable parent) {
        _parent = parent;
        getDocument().addDocumentListener(this);
        addKeyListener(this);
        _parent.addKeyListener(this);
    }

    @Override
    public void keyTyped(KeyEvent e) {
        if ((e.getKeyChar() >= '0' && e.getKeyChar() <= '9') || e.getKeyChar() == '.') {
            // Do nothing and let the framework add the character to the editor
        }
        else {
            // Consume the event to stop the event from being added to the editor
            e.consume();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyChar() == KeyEvent.VK_TAB || e.getKeyChar() == KeyEvent.VK_ENTER) {
            if (e.isShiftDown()) {
                _column--;
            } else {
                _column++;
            }

            if (_column > _parent.getColumnCount() - 1) {
                _row++;
                _column = 1;
            }

            if (_column < 1) {
                _row--;
                _column = _parent.getColumnCount() - 1;
            }

            _parent.changeSelection(_row, _column, true, true);
            _parent.editCellAt(_row, _column);
        }
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        updateTable();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        updateTable();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        updateTable();
    }

    private void updateTable() {
        _parent.setValueAt(getText(), _row, _column);
        ((DefaultTableModel) _parent.getModel()).fireTableDataChanged();
    }

    @Override
    public Object getCellEditorValue() {
        return getText();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        _row = row;
        _column = column;
        _parent.grabFocus();
        final Object cellValue = _parent.getValueAt(row, column);
        if (cellValue == null) {
            setText("");
        } else {
            setText("" + cellValue);
        }
        return this;
    }

    @Override
    public boolean shouldSelectCell(EventObject anEvent) {
        return true;
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

    @Override
    public boolean isCellEditable(EventObject anEvent) {
        return true;
    }

}
