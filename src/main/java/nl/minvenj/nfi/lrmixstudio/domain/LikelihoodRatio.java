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

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains the likelihood ratio for two hypotheses accessible per locus and as
 * an overall value
 */
public class LikelihoodRatio {

    private static Logger LOG = LoggerFactory.getLogger(LikelihoodRatio.class);
    private HashMap<String, Ratio> ratios = new LinkedHashMap<>();

    public LikelihoodRatio() {
    }

    public LikelihoodRatio(Iterable<String> enabledLoci) {
        LOG.debug("Setting enabled Loci: {}", enabledLoci);
        for (String locusName : enabledLoci) {
            ratios.put(locusName, null);
        }
    }

    public synchronized void add(LocusProbabilities prosecution, LocusProbabilities defense) {
        LOG.debug("Adding probabilities. Prosecution: {} and Defense: {}", prosecution, defense);
        if (prosecution == null) {
            throw new IllegalArgumentException("Prosecution locus probabilities are null!");
        }
        if (defense == null) {
            throw new IllegalArgumentException("Defense locus probabilities are null!");
        }
        for (String locus : prosecution.getLoci()) {
            ratios.put(locus, new Ratio(locus, prosecution.getLocusProbability(locus), defense.getLocusProbability(locus)));
        }
    }

    public Iterable<String> getLoci() {
        return ratios.keySet();
    }

    /**
     * Gets the Likelihood ratio for the given locus
     *
     * @param locus The name of the requested locus
     * @return A Double containing the ratio, or null if the requested locus is
     * not present in the results.
     */
    public Ratio getRatio(String locus) {
        if (!ratios.containsKey(locus)) {
            return null;
        }
        return ratios.get(locus);
    }

    public void putRatio(Ratio ratio) {
        ratios.put(ratio.getLocusName(), ratio);
    }

    public Ratio getOverallRatio() {
        Double prD = 1.0;
        Double prP = 1.0;
        Double r = 1.0;
        for (Ratio ratio : ratios.values()) {
            if (ratio.getRatio() != null) {
                r *= ratio.getRatio();
            }
            if (ratio.getDefenseProbability() != null) {
                prD *= ratio.getDefenseProbability();
            }
            if (ratio.getProsecutionProbability() != null) {
                prP *= ratio.getProsecutionProbability();
            }
        }
        return new Ratio("Overall", prP, prD, r);
    }

    /**
     * @return A Collection of Ratio objects, each containing a locus name and a
     * likelihood ratio for that locus
     */
    public Collection<Ratio> getRatios() {
        return ratios.values();
    }
}
