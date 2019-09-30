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

import nl.minvenj.nfi.lrmixstudio.domain.Locus;
import nl.minvenj.nfi.lrmixstudio.domain.Sample;
import nl.minvenj.nfi.lrmixstudio.gui.SessionData;

/**
 *
 * @author dejong
 */
class ReplicateAndProfileMatchingAllelesDecorator extends AlleleDecorator {

    private Sample target;

    public ReplicateAndProfileMatchingAllelesDecorator(SessionData session, Sample target) {
        super("<html>Matching alleles in the <b>replicate</b> and <b>" + target.getId(), session);
        this.target = target;
    }

    @Override
    public String apply(Sample replicate, Sample sample, String locusId, String alleles) {
        boolean enabled = replicate.isEnabled() && getSession().isLocusEnabled(locusId);

        if (!sample.equals(target) && !sample.equals(replicate)) {
            return "<html>" + disable(alleles);
        }

        if (sample.getLocus(locusId) == null || replicate.getLocus(locusId) == null) {
            if (enabled) {
                return "<html>" + alleles;
            }
            return "<html>" + disable(alleles);
        }

        String[] individualAlleles = alleles.split(" ");
        String decorated = "";
        if (sample.equals(target)) {
            decorated = compareSamples(replicate.getLocus(locusId), individualAlleles, enabled);
        } else {
            decorated = compareSamples(target.getLocus(locusId), individualAlleles, enabled);
        }

        if (enabled) {
            return "<html>" + decorated;
        }
        return "<html>" + disable(decorated);
    }

    private String compareSamples(Locus locus, String[] alleles, boolean enabled) {
        StringBuilder builder = new StringBuilder("");
        for (String allele : alleles) {
            if (locus != null && locus.hasAllele(allele)) {
                builder.append(highlight(allele, enabled)).append(" ");
            } else {
                builder.append(allele).append(" ");
            }
        }
        return builder.toString();
    }
}
