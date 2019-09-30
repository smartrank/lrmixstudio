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

import java.util.HashMap;

import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/**
 * A table that renders rows in alternate colours
 */
public class ZebraTable extends JTable {

    /**
     * Private proxy class for the return value of getTableColumn
     */
    private class ZebraTableColumnProxy extends TableColumn {

        private final TableColumn proxy;

        protected ZebraTableColumnProxy(TableColumn proxy) {
            this.proxy = proxy;
        }

        @Override
        public void setCellEditor(TableCellEditor editor) {
            TableCellEditor newEditor = editor;
            if (!(editor instanceof ZebraTableCellEditor)) {
                newEditor = new ZebraTableCellEditor(editor, getName());
            }
            proxy.setCellEditor(newEditor);
        }

        @Override
        public void setCellRenderer(TableCellRenderer renderer) {
            TableCellRenderer newRenderer = renderer;
            if (!(renderer instanceof ZebraTableCellRenderer)) {
                newRenderer = new ZebraTableCellRenderer(renderer, getName());
            }

            proxy.setCellRenderer(newRenderer);
        }
    }

    public ZebraTable() {
        setShowGrid(false);
        setRowSelectionAllowed(false);
        getModel().addTableModelListener(this);
    }

    @Override
    public void setDefaultRenderer(Class<?> columnClass, TableCellRenderer renderer) {
        TableCellRenderer newRenderer = renderer;
        if (!(renderer instanceof ZebraTableCellRenderer)) {
            newRenderer = new ZebraTableCellRenderer(renderer, getName());
        }

        super.setDefaultRenderer(columnClass, newRenderer);
    }

    @Override
    public void setDefaultEditor(Class<?> columnClass, TableCellEditor editor) {
        TableCellEditor newEditor = editor;
        if (!(editor instanceof ZebraTableCellEditor)) {
            newEditor = new ZebraTableCellEditor(editor, getName());
        }
        super.setDefaultEditor(columnClass, newEditor);
    }

    @Override
    public TableColumn getColumn(Object identifier) {
        return new ZebraTableColumnProxy(super.getColumn(identifier));
    }

    @Override
    public void setName(String name) {
        super.setName(name);
        createDefaultEditors();
        createDefaultRenderers();
        TableCellRenderer headerCellRenderer = getTableHeader().getDefaultRenderer();
        if (!(headerCellRenderer instanceof ZebraTableCellRenderer)) {
            getTableHeader().setDefaultRenderer(new ZebraTableCellRenderer(headerCellRenderer, null));
        }
    }

    @Override
    protected void createDefaultEditors() {
        super.createDefaultEditors();
        HashMap wrappedEditors = new HashMap();
        for (Object key : defaultEditorsByColumnClass.keySet()) {
            wrappedEditors.put(key, new ZebraTableCellEditor((TableCellEditor) defaultEditorsByColumnClass.get(key), getName()));
        }
        defaultEditorsByColumnClass.clear();
        defaultEditorsByColumnClass.putAll(wrappedEditors);
    }

    @Override
    protected void createDefaultRenderers() {
        super.createDefaultRenderers();
        HashMap wrappedRenderers = new HashMap();
        for (Object key : defaultRenderersByColumnClass.keySet()) {
            wrappedRenderers.put(key, new ZebraTableCellRenderer((TableCellRenderer) defaultRenderersByColumnClass.get(key), getName()));
        }
        defaultRenderersByColumnClass.clear();
        defaultRenderersByColumnClass.putAll(wrappedRenderers);
    }
}
