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

import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZebraTableCellRenderer implements TableCellRenderer {

    private static final Logger LOG = LoggerFactory.getLogger(ZebraTableCellRenderer.class);
    private final TableCellRenderer proxyRenderer;
    private final String name;

    public ZebraTableCellRenderer(TableCellRenderer proxy, String name) {
        this.proxyRenderer = proxy;
        this.name = name;
    }

    public ZebraTableCellRenderer(TableCellEditor proxy, String name) {
        this.proxyRenderer = null;
        this.name = name;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component component = proxyRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        Color evenRowColor = ApplicationSettings.getEvenRowColor(name);
        Color oddRowColor = ApplicationSettings.getOddRowColor(name);

        if (!table.isEnabled()) {
            evenRowColor = toGrayScale(evenRowColor);
            oddRowColor = toGrayScale(oddRowColor);
        }

        component.setEnabled(table.isEnabled());

        if (name != null) {
            if (component instanceof JSpinner) {
                ((JSpinner.DefaultEditor) ((JSpinner) component).getEditor()).getTextField().setBackground((row % 2) == 0 ? evenRowColor : oddRowColor);
            } else {
                component.setBackground((row % 2) == 0 ? evenRowColor : oddRowColor);
            }
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
}
