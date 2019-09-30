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
package nl.minvenj.nfi.lrmixstudio.gui.tabs.profilesummary;

import java.util.ArrayList;

import nl.minvenj.nfi.lrmixstudio.domain.Allele;
import nl.minvenj.nfi.lrmixstudio.domain.Locus;
import nl.minvenj.nfi.lrmixstudio.domain.Sample;
import nl.minvenj.nfi.lrmixstudio.gui.SessionData;

/**
 * This allele decorator applies decoration to all alleles in a replicate that
 * are not found in any of the reference profiles.
 *
 * @author dejong
 */
public class SampleOnlyAllelesDecorator extends AlleleDecorator {

    public SampleOnlyAllelesDecorator(SessionData session) {
        super("<html>Alleles in the <b>replicate</b> that are not present in the <b>reference profiles</b>", session);
    }

    @Override
    public String apply(Sample replicate, Sample sample, String locusId, String alleles) {
        // If the sample is not a replicate, do nothing
        if (!replicate.equals(sample) || alleles.isEmpty()) {
            return alleles;
        }

        boolean enabled = replicate.isEnabled() && getSession().isLocusEnabled(locusId);

        String[] individualAlleles = alleles.split(" ");
        // Build a list of all alleles at this locus in the active profiles
        ArrayList<String> allAlleles = new ArrayList<>();
        for (Sample profile : getSession().getActiveProfiles()) {
            Locus locus = profile.getLocus(locusId);
            if (locus != null) {
                for (Allele profileAllele : locus.getAlleles()) {
                    String a = profileAllele.getAllele().replaceAll("'", "");
                    if (!allAlleles.contains(a)) {
                        allAlleles.add(a);
                    }
                }
            }
        }

        // Iterate over all alleles to see if it occurs in any of the profiles
        StringBuilder builder = new StringBuilder("");
        for (String allele : individualAlleles) {
            if (allAlleles.contains(allele)) {
                // Add the allele without decoration to the output
                builder.append(allele).append(" ");
            } else {
                // Add the allele with decoration to the output
                builder.append(highlight(allele, enabled)).append(" ");
            }
        }

        // Show as disabled if either the replicate or the locus is disabled
        if (!enabled) {
            return "<html>" + disable(builder.toString());
        }

        return "<html>" + builder.toString();
    }
}
