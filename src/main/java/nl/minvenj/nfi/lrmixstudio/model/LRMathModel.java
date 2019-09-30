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
package nl.minvenj.nfi.lrmixstudio.model;

import java.util.concurrent.TimeoutException;

import nl.minvenj.nfi.lrmixstudio.domain.LikelihoodRatio;

public interface LRMathModel {

    /**
     * @return The name of the mathematical model
     */
    public String getId();

    /**
     * Obtains the result of the processing
     *
     * @return
     * @throws InterruptedException
     * @throws TimeoutException
     */
    public LikelihoodRatio getLikelihoodRatio() throws InterruptedException, TimeoutException;

    /**
     * Performs an analysis on the configuration data. The method returns when
     * the analysis is complete.
     *
     * @param config A {@link ConfigurationData} object containing the input for
     * the analysis
     * @return a {@link LikelihoodRatio} object containing the result of the
     * analysis
     * @throws InterruptedException if processing was interrupted
     */
    public LikelihoodRatio doAnalysis(ConfigurationData config) throws InterruptedException;

    /**
     * Starts an analysis on the configuration data. The method returns when the
     * analysis is started. Progress of the analysis can be obtained by
     * supplying {@link AnalysisProgressListener} classes using the
     * {@link #addProgressListener(nl.minvenj.nfi.lrmixstudio.model.AnalysisProgressListener)}
     * method. }
     *
     * @param config A {@link ConfigurationData} object containing the input for
     * the analysis
     */
    public void startAnalysis(ConfigurationData config);

    /**
     * Starts a sensitivity analysis on the supplied configuration data.
     *
     * @param config a {@link ConfigurationData} object describing the input
     * data for the analysis
     */
    public void doSensitivityAnalysis(ConfigurationData config);

    /**
     * Starts a sensitivity analysis on the supplied configuration data.
     *
     * @param config a {@link ConfigurationData} object describing the input
     * data for the analysis
     */
    public void doPerformanceAnalysis(ConfigurationData config);

    /**
     * Adds a progress listener to the model. A listener is notified of relevant
     * processing events.
     *
     * @param listener A class implementing the {@link AnalysisProgressListener}
     * interface.
     */
    public void addProgressListener(AnalysisProgressListener listener);

    /**
     * Interrupts the currently running analysis.
     */
    public void interrupt();
}
