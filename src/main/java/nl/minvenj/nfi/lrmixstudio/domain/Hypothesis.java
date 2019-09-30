
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
package nl.minvenj.nfi.lrmixstudio.domain;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Hypothesis {
    
    private static final Logger LOG = LoggerFactory.getLogger(Hypothesis.class);
    private ArrayList<Contributor> _contributors = new ArrayList<>();
    private ArrayList<Contributor> _nonContributors = new ArrayList<>();
    private int _unknownContributors = 0;
    private double _dropInProbability = 0;
    private String _id;
    private PopulationStatistics _populationStatistics;
    private double _thetaCorrection;
    private double _unknownDropoutProbability;
    private final Relatedness _relatedness;

    public Hypothesis(String id, PopulationStatistics popStats) {
        _id = id;
        _populationStatistics = popStats;
        _thetaCorrection = 0;
        _relatedness = new Relatedness();
    }

    public Hypothesis(Hypothesis other) {
        this(other._id, other._unknownContributors, other._populationStatistics, other._dropInProbability, other._unknownDropoutProbability, other._thetaCorrection);
        _contributors = other._contributors;
        _nonContributors = other._nonContributors;
        _relatedness.setRelation(other.getRelatedness().getRelation());
        _relatedness.setRelative(other.getRelatedness().getRelative());
    }

    public Hypothesis(String id, int unknownContributors, PopulationStatistics populationStatistics, double dropInProbability, double unknownDropoutProbability, double thetaCorrection) {
        _id = id;
        _unknownContributors = unknownContributors;
        _populationStatistics = populationStatistics;
        _dropInProbability = dropInProbability;
        _thetaCorrection = thetaCorrection;
        _unknownDropoutProbability = unknownDropoutProbability;
        _relatedness = new Relatedness();
    }

    public Contributor addContributor(Sample contributorSample, double dropOutProbability) {
        LOG.debug("addContributor {} ({})", contributorSample.getId(), dropOutProbability);
        Contributor contributor = new Contributor(contributorSample, dropOutProbability);
        _contributors.add(contributor);
        return contributor;
    }

    public void addNonContributor(Sample contributorSample, double dropOutProbability) {
        _nonContributors.add(new Contributor(contributorSample, dropOutProbability));
    }

    public int getUnknownCount() {
        return _unknownContributors;
    }

    public void setUnknownCount(int unknowns) {
        this._unknownContributors = unknowns;
    }

    public String getId() {
        return _id;
    }

    public Collection<Contributor> getContributors() {
        return _contributors;
    }

    public Collection<Contributor> getNonContributors() {
        return Collections.unmodifiableCollection(_nonContributors);
    }

    /**
     * @return the _populationStatistics
     */
    public PopulationStatistics getPopulationStatistics() {
        if (_populationStatistics == null) {
            _populationStatistics = new PopulationStatistics("Empty");
        }
        return _populationStatistics;
    }

    /**
     * @return the _dropInProbability
     */
    public double getDropInProbability() {
        return _dropInProbability;
    }

    public void setDropInProbability(double dropIn) {
        this._dropInProbability = dropIn;
    }

    public Contributor getContributor(Allele a) {
        for (Contributor contributor : _contributors) {
            if (contributor.getSample().getId().equals(a.getLocus().getSample().getId())) {
                return contributor;
            }
        }
        throw new IllegalArgumentException("Contributor for Allele '" + a.getAllele() + "' not found in hypothesis '" + getId() + "'!");
    }

    public double getThetaCorrection() {
        return _thetaCorrection;
    }

    public void setThetaCorrection(double theta) {
        this._thetaCorrection = theta;
    }

    public double getUnknownDropoutProbability() {
        return _unknownDropoutProbability;
    }

    public void setUnknownDropoutProbability(double dropOut) {
        this._unknownDropoutProbability = dropOut;
    }

    public int getGuid() {
        return toString().hashCode() + Objects.hashCode(_populationStatistics);
    }

    public int getGuidForDropoutEstimation() {
        return toStringForDropoutEstimation().hashCode() + Objects.hashCode(_populationStatistics);
    }

    public int getGuidForSensitivityAnalysis() {
        return toStringForSensitivityAnalysis().hashCode() + Objects.hashCode(_populationStatistics);
    }

    public Contributor getContributor(Sample sample) {
        for (Contributor contributor : _contributors) {
            if (contributor.getSample().getId().equalsIgnoreCase(sample.getId())) {
                return contributor;
            }
        }
        for (Contributor contributor : _nonContributors) {
            if (contributor.getSample().getId().equalsIgnoreCase(sample.getId())) {
                return contributor;
            }
        }
        throw new IllegalArgumentException("Sample '" + sample.getId() + "' is not present in hypothesis '" + getId() + "'!");
    }

    public Hypothesis copy() {
        Hypothesis retval = new Hypothesis(_id, _unknownContributors, _populationStatistics, _dropInProbability, _unknownDropoutProbability, _thetaCorrection);
        for (Contributor contributor : _contributors) {
            retval.addContributor(contributor.getSample(), contributor.getDropoutProbability());
        }
        for (Contributor nonContributor : _nonContributors) {
            retval.addNonContributor(nonContributor.getSample(), nonContributor.getDropoutProbability());
        }
        retval.getRelatedness().setRelation(getRelatedness().getRelation());
        retval.getRelatedness().setRelative(getRelatedness().getRelative());
        return retval;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (!_contributors.isEmpty()) {
            for (Contributor contributor : _contributors) {
                if (builder.length() > 0) {
                    builder.append(", ");
                }
                builder.append(contributor.toString());
            }
        }

        if (getUnknownCount() > 0) {
            if (builder.length() > 0) {
                builder.append(" and ");
            }
            BigDecimal udp = new BigDecimal(_unknownDropoutProbability, MathContext.UNLIMITED).setScale(2, RoundingMode.HALF_UP);
            if (getUnknownCount() != 1) {
                builder.append(getUnknownCount()).append(" unknowns (").append(udp).append(")");
                if (_relatedness.getRelation() != Relatedness.Relation.NONE) {
                    builder.append(" - One unknown is ").append(_relatedness);
                } else {
                    builder.append(" - All unrelated");
                }
            } else {
                builder.append(getUnknownCount()).append(" unknown (").append(udp).append(") - ").append(_relatedness);
            }
        }

        BigDecimal di = new BigDecimal(_dropInProbability, MathContext.UNLIMITED).setScale(2, RoundingMode.HALF_UP);
        BigDecimal tc = new BigDecimal(_thetaCorrection, MathContext.UNLIMITED).setScale(2, RoundingMode.HALF_UP);

        builder.append(", DropIn ").append(di).append(", Theta ").append(tc);

        return builder.toString();
    }

    public String toStringForDropoutEstimation() {
        StringBuilder builder = new StringBuilder();
        if (!_contributors.isEmpty()) {
            for (Contributor contributor : _contributors) {
                if (builder.length() > 0) {
                    builder.append(", ");
                }
                String str = contributor.toString();
                builder.append(str.substring(0, str.indexOf("(")));
            }
        }

        if (getUnknownCount() > 0) {
            if (builder.length() > 0) {
                builder.append(" and ");
            }
            BigDecimal udp = new BigDecimal(_unknownDropoutProbability, MathContext.UNLIMITED).setScale(2, RoundingMode.HALF_UP);
            if (getUnknownCount() != 1) {
                builder.append(getUnknownCount()).append(" unknowns");
                if (_relatedness.getRelation() != Relatedness.Relation.NONE) {
                    builder.append(" - One unknown is ").append(_relatedness);
                }
                else {
                    builder.append(" - All unrelated");
                }
            }
            else {
                builder.append(getUnknownCount()).append(" unknown").append(" - ").append(_relatedness);
            }
        }

        BigDecimal di = new BigDecimal(_dropInProbability, MathContext.UNLIMITED).setScale(2, RoundingMode.HALF_UP);
        BigDecimal tc = new BigDecimal(_thetaCorrection, MathContext.UNLIMITED).setScale(2, RoundingMode.HALF_UP);

        builder.append(", DropIn ").append(di);

        return builder.toString();
    }

    public String toStringForSensitivityAnalysis() {
        StringBuilder builder = new StringBuilder();
        if (!_contributors.isEmpty()) {
            for (Contributor contributor : _contributors) {
                if (builder.length() > 0) {
                    builder.append(", ");
                }
                String str = contributor.toString();
                builder.append(str.substring(0, str.indexOf("(")));
            }
        }

        if (getUnknownCount() > 0) {
            if (builder.length() > 0) {
                builder.append(" and ");
            }
            BigDecimal udp = new BigDecimal(_unknownDropoutProbability, MathContext.UNLIMITED).setScale(2, RoundingMode.HALF_UP);
            if (getUnknownCount() != 1) {
                builder.append(getUnknownCount()).append(" unknowns");
                if (_relatedness.getRelation() != Relatedness.Relation.NONE) {
                    builder.append(" - One unknown is ").append(_relatedness);
                }
                else {
                    builder.append(" - All unrelated");
                }
            }
            else {
                builder.append(getUnknownCount()).append(" unknown").append(" - ").append(_relatedness);
            }
        }

        BigDecimal di = new BigDecimal(_dropInProbability, MathContext.UNLIMITED).setScale(2, RoundingMode.HALF_UP);
        BigDecimal tc = new BigDecimal(_thetaCorrection, MathContext.UNLIMITED).setScale(2, RoundingMode.HALF_UP);

        builder.append(", DropIn ").append(di).append(", Theta ").append(tc);

        return builder.toString();
    }

    /**
     * Investigates whether the given sample is marked as a contributor for this
     * hypothesis
     *
     * @param sample The sample to search for
     * @return true if the sample is marked as a contributor
     */
    public boolean isContributor(Sample sample) {
        for (Contributor contributor : _contributors) {
            if (contributor.getSample().getId().equalsIgnoreCase(sample.getId()) && contributor.getSample().getSourceFile().equalsIgnoreCase(sample.getSourceFile())) {
                return true;
            }
        }
        return false;
    }
    
    public Relatedness getRelatedness() {
        return _relatedness;
    }
}
