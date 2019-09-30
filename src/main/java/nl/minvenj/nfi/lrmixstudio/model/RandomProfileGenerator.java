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

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Collection;

import nl.minvenj.nfi.lrmixstudio.domain.Allele;
import nl.minvenj.nfi.lrmixstudio.domain.Locus;
import nl.minvenj.nfi.lrmixstudio.domain.PopulationStatistics;
import nl.minvenj.nfi.lrmixstudio.domain.Sample;

public class RandomProfileGenerator {

    protected final SecureRandom rnd;
    private final PopulationStatistics stats;
    private final Collection<String> _enabledLoci;

    public RandomProfileGenerator(Collection<String> enabledLoci, PopulationStatistics stats) {
        this(enabledLoci, stats, new SecureRandom());
    }

    public RandomProfileGenerator(Collection<String> enabledLoci, PopulationStatistics stats, SecureRandom rnd) {
        this.stats = stats;
        this.rnd = rnd;
        _enabledLoci = enabledLoci;
    }

    public Sample getRandomSample() throws NoSuchAlgorithmException {
        Sample sample = new Sample("RandomSample");

        for (String locusName : _enabledLoci) {
            Locus locus = new Locus(locusName);

            locus.addAllele(getRandomAllele(locusName));
            locus.addAllele(getRandomAllele(locusName));

            sample.addLocus(locus);
        }

        return sample;
    }

    /**
     * Generates a random allele for the getNamed locus
     *
     * @param locusName The getName of the target locus
     * @return a randomly generated allele
     */
    public Allele getRandomAllele(String locusName) {

        while (true) {
            double randomValue = rnd.nextDouble();
            double threshold = 0.0;

            for (String allele : stats.getAlleles(locusName)) {
                threshold += stats.getProbability(locusName, allele).doubleValue();
                if (randomValue <= threshold) {
                    return new Allele(allele);
                }
            }
        }
    }
}
