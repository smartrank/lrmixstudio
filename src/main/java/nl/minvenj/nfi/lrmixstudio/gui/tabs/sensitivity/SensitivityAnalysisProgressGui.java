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
package nl.minvenj.nfi.lrmixstudio.gui.tabs.sensitivity;

import java.util.Collection;

import nl.minvenj.nfi.lrmixstudio.model.SensitivityAnalysisResults.Point;

/**
 * This interface describes the methods called by the SensitivityAnalysis
 * progress listener to report its progress
 */
public interface SensitivityAnalysisProgressGui {

    /**
     * Sets the percentage complete value for the current iteration of a
     * sensitivity analysis
     *
     * @param currentPercentage The percentage completed of the current
     * iteration
     * @param overallPercentage The percentage completed of the entire analysis
     */
    public void setSensitivityProgress(int currentPercentage, int overallPercentage);

    /**
     * Sets the percentage complete value for the dropout estimation
     *
     * @param percentDone The percentage completed
     */
    public void setDropoutProgress(int percentDone);

    /**
     * Sets the estimated time left for the current dropout estimation to
     * complete.
     *
     * @param timeLeft The estimated number of milliseconds required to complete
     * the current dropout estimation
     */
    public void setDropoutTimeLeft(long timeLeft);

    /**
     * Sets the estimated time left for the current sensitivity analysis to
     * complete.
     *
     * @param current the estimated number for millisecond required to finish
     * the current iteration of the sensitivity analysis
     * @param overall The estimated number of milliseconds required to complete
     * the entire sensitivity analysis
     */
    public void setSensitivityTimeLeft(long current, long overall);

    /**
     * Updates the sensitivity graph by drawing the supplied collection of
     * points
     *
     * @param points The points to draw.
     */
    public void updateGraph(Collection<Point> points);
}
