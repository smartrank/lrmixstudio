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

import nl.minvenj.nfi.lrmixstudio.domain.Hypothesis;
import nl.minvenj.nfi.lrmixstudio.domain.LikelihoodRatio;
import nl.minvenj.nfi.lrmixstudio.domain.LocusProbabilities;

/**
 * This interface can be implemented by classes that need to be notified of the
 * current status of an analysis run.
 */
public interface AnalysisProgressListener {

    /**
     * Called when the analysis is started
     */
    public void analysisStarted();

    /**
     * Called when the analysis completes successfully.
     *
     * @param lr The resulting {@link LikelihoodRatio} object
     */
    public void analysisFinished(LikelihoodRatio lr);

    /**
     * Called when the analysis terminates with an exception
     *
     * @param e The exception that caused the analysis to terminate
     */
    public void analysisFinished(Exception e);

    /**
     * Called when analysis starts for a given hypothesis.
     *
     * @param hypothesis The {@link Hypothesis} that is currently under
     * investigation
     */
    public void hypothesisStarted(Hypothesis hypothesis);

    /**
     * Called when processing terminates successfully for a given hypothesis.
     *
     * @param hypothesis The {@link Hypothesis} for which processing was
     * completed.
     * @param probability The {@link LocusProbabilities} object resulting from
     * the calculations.
     */
    public void hypothesisFinished(Hypothesis hypothesis, LocusProbabilities probability);

    /**
     * Called when processing starts for a given locus.
     *
     * @param hypothesis The {@link Hypothesis} for which processing is starting
     * @param locusName The name of the locus for which processing is starting
     * @param jobsize The (estimated) number of calculations to be performed
     */
    public void locusStarted(Hypothesis hypothesis, String locusName, long jobsize);

    /**
     * Called when processing finishes for a given locus under a given
     * hypothesis.
     *
     * @param hypothesis The {@link Hypothesis} for which processing is starting
     * @param locusName The name of the locus for which processing is starting
     * @param locusProbability The resulting probability of the named locus
     * under the given hypothesis.
     */
    public void locusFinished(Hypothesis hypothesis, String locusName, Double locusProbability);
}
