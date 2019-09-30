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
package nl.minvenj.nfi.lrmixstudio.model.splitdrop;

import nl.minvenj.nfi.lrmixstudio.domain.Locus;

public class Permutation {
    private final Locus[] _loci;
    private final int _permutationFactor;
    
    public String toLogString(Locus[] loci) {
        String retval = "";
        for (Locus locus : loci) {
            retval += locus.getAlleles() + "; ";
        }
        return "[" + retval.substring(0, retval.length() - 2) + "]";
    }

    Permutation(Locus[] loci, int permutationFactor) {
        _loci = loci;
        _permutationFactor = permutationFactor;
    }

    public Locus[] getLoci() {
        return _loci;
    }

    public int getPermutationFactor() {
        return _permutationFactor;
    }
}
