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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public final class LocusProbabilities {

    private final HashMap<String, Double> probabilities = new HashMap<>();

    public void addLocusProbability(final String locus, final Double probability) {
        synchronized (probabilities) {
            probabilities.put(locus, probability);
        }
    }

    /**
     * @return A collection of strings containing the names for all loci for
     * which a probability is known in this object
     */
    public Collection<String> getLoci() {
        synchronized (probabilities) {
            return new ArrayList(probabilities.keySet());
        }
    }

    /**
     * Get the calculated probability for the named locus
     *
     * @param locus The name of the locus for which the probability is to be
     * retrieved
     * @return A Double containing the probability, or null if the probability
     * is not present.
     */
    public Double getLocusProbability(final String locus) {
        return probabilities.get(locus);
    }

    /**
     * Get the overall probability based on the the locus probabilities
     * contained in this object.
     *
     * @return The product of all locus probabilities
     */
    public Double getGlobalProbability() {
        Double retval = new Double(1);
        for (Double locus : probabilities.values()) {
            retval *= locus;
        }
        return retval;
    }

    @Override
    public String toString() {
        return probabilities.toString();
    }
}
