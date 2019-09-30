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
package nl.minvenj.nfi.lrmixstudio.model.splitdrop.threadpool;

import nl.minvenj.nfi.lrmixstudio.domain.Hypothesis;

class LocusProbability {
    private final String _locusName;
    private final Hypothesis _hypothesis;
    private double _value;

    LocusProbability(Hypothesis hypothesis, String locusName) {
        _hypothesis = hypothesis;
        _locusName = locusName;
        _value = 0;
    }

    void setValue(double value) {
        _value = value;
    }

    void addValue(double otherValue) {
        _value += otherValue;
    }

    Double getValue() {
        return _value;
    }

    Hypothesis getHypothesis() {
        return _hypothesis;
    }

    String getLocusName() {
        return _locusName;
    }

}
