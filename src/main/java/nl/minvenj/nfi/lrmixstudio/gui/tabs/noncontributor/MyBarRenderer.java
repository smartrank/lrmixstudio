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
package nl.minvenj.nfi.lrmixstudio.gui.tabs.noncontributor;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;

import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRendererState;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.CategoryDataset;
import org.jfree.ui.RectangleEdge;

import nl.minvenj.nfi.lrmixstudio.model.NonContributorTestResults;

/**
 *
 * @author dejong
 */
public class MyBarRenderer extends BarRenderer {

    private final double[] _values;

    MyBarRenderer(NonContributorTestResults performanceTestResults) {
        _values = new double[6];
        if (performanceTestResults != null) {
            if (performanceTestResults.getOriginalLR() != null) {
                _values[0] = performanceTestResults.getOriginalLR();
            }
            if (performanceTestResults.getMinimum() != null) {
                _values[1] = performanceTestResults.getMinimum();
            }
            if (performanceTestResults.getOnePercent() != null) {
                _values[2] = performanceTestResults.getOnePercent();
            }
            if (performanceTestResults.getFiftyPercent() != null) {
                _values[3] = performanceTestResults.getFiftyPercent();
            }
            if (performanceTestResults.getNinetyninePercent() != null) {
                _values[4] = performanceTestResults.getNinetyninePercent();
            }
            if (performanceTestResults.getMaximum() != null) {
                _values[5] = performanceTestResults.getMaximum();
            }
        }
        setBaseItemLabelFont(new Font("SansSerif", Font.BOLD, 12));
        setBaseOutlinePaint(Color.black);
        setBaseItemLabelsVisible(true);
        setDrawBarOutline(true);
        setBaseItemLabelPaint(Color.black);
        setShadowVisible(false);
    }

    @Override
    public void drawItem(final Graphics2D g2, final CategoryItemRendererState state, final Rectangle2D dataArea, final CategoryPlot plot, final CategoryAxis domainAxis, final ValueAxis rangeAxis, final CategoryDataset dataset, final int row, final int column, final int pass) {
        setPaint(column == 0 ? Color.RED : Color.GRAY);
        setBarPainter(new StandardBarPainter() {
            @Override
            public void paintBar(Graphics2D g2, BarRenderer renderer, int row, int column, RectangularShape bar, RectangleEdge base) {
                if (!(Double.isInfinite(_values[column]) || Double.isNaN(_values[column]))) {
                    super.paintBar(g2, renderer, row, column, bar, base);
                }
            }
        });
        setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator() {
            @Override
            public String generateLabel(CategoryDataset dataset, int row, int column) {
                if (Double.isInfinite(_values[column]) || Double.isNaN(_values[column])) {
                    return "" + _values[column];
                }
                return super.generateLabel(dataset, row, column);
            }
        });
        super.drawItem(g2, state, dataArea, plot, domainAxis, rangeAxis, dataset, row, column, pass);
    }
}
