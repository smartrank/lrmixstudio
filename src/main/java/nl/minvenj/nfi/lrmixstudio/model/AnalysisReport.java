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

import java.util.Collection;

import nl.minvenj.nfi.lrmixstudio.domain.Allele;
import nl.minvenj.nfi.lrmixstudio.domain.DisabledLocus;
import nl.minvenj.nfi.lrmixstudio.domain.Hypothesis;
import nl.minvenj.nfi.lrmixstudio.domain.LikelihoodRatio;
import nl.minvenj.nfi.lrmixstudio.domain.PopulationStatistics;
import nl.minvenj.nfi.lrmixstudio.domain.Sample;

public interface AnalysisReport {

    public LikelihoodRatio getLikelihoodRatio();

    public Hypothesis getDefenseHypothesis();

    public Hypothesis getProsecutionHypothesis();

    public long getStartTime();

    public long getStopTime();

    public boolean isSucceeded();

    public Throwable getException();

    public String getCaseNumber();

    public String getProgramVersion();

    public SensitivityAnalysisResults getSensitivityAnalysisResults();

    public PopulationStatistics getPopulationStatistics();

    public Collection<Sample> getReplicates();

    public Collection<Sample> getProfiles();

    public int getGuid();

    public NonContributorTestResults getNonContributorTestResults();

    /**
     * @return The frequency assigned to rare alleles
     */
    public String getRareAlleleFrequency();

    /**
     * @return A collection Allele objects for the alleles that were detected as
     * rare (i.e. not present in the population statistics)
     */
    public Collection<Allele> getRareAlleles();

    /**
     * @return true if the report has been exported
     */
    public boolean isExported();

    public long getProcessingTime();

    public void addProcessingTime(long processingTime);

    public Collection<String> getEnabledLoci();

    public Collection<DisabledLocus> getDisabledLoci();

    public boolean isDropoutCompatible(AnalysisReport currentReport);

    public boolean isSensitivityCompatible(AnalysisReport currentReport);

    public void setSensitivityAnalysisResults(SensitivityAnalysisResults sensitivityAnalysisResults);

    public String getLogfileName();

    public void setLogfileName(String name);
}
