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
import java.util.Collection;

import nl.minvenj.nfi.lrmixstudio.domain.Locus;
import nl.minvenj.nfi.lrmixstudio.domain.Sample;
import nl.minvenj.nfi.lrmixstudio.gui.SessionData;

/**
 *
 * @author dejong
 */
class InterProfileMatchingAllelesDecorator extends AlleleDecorator {

    private Collection<Sample> _targets;

    public InterProfileMatchingAllelesDecorator(SessionData session, Collection<Sample> targets) {
        super("<html>Alleles shared between all reference profiles", session);
        _targets = new ArrayList<>(targets);
    }

    @Override
    public String apply(Sample replicate, Sample sample, String locusId, String alleles) {
        boolean enabled = replicate.isEnabled() && getSession().isLocusEnabled(locusId);

        if (!_targets.contains(sample)) {
            return "<html>" + disable(alleles);
        }

        // Iterate over all alleles to see if it occurs in any of the profiles
        String[] individualAlleles = alleles.split(" ");
        StringBuilder builder = new StringBuilder("");
        for (String allele : individualAlleles) {
            if (isPresent(sample, locusId, allele)) {
                builder.append(highlight(allele, enabled));
            } else {
                builder.append(allele);
            }
            builder.append(" ");
        }

        if (enabled) {
            return "<html>" + builder.toString();
        }

        return "<html>" + disable(builder.toString());
    }

    private boolean isPresent(Sample sample, String locusName, String allele) {
        for (Sample target : _targets) {
            if (target.equals(sample)) {
                continue;
            }
            Locus locus = target.getLocus(locusName);
            if (locus == null || !locus.hasAllele(allele)) {
                return false;
            }
        }
        return true;
    }
}
