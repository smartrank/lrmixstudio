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

import nl.minvenj.nfi.lrmixstudio.domain.Sample;

/**
 *
 * @author dejong
 */
public class DefaultDecorator extends AlleleDecorator {

    public DefaultDecorator() {
        super("Nothing", null);
    }

    @Override
    public String apply(Sample replicate, Sample sample, String locusId, String alleles) {
        return alleles;
    }
}
