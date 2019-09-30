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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Objects;

public class DropoutEstimation {

    private BigDecimal _minimum;
    private BigDecimal _maximum;

    private final HashMap<String, BigDecimal> _minimumValues = new HashMap<>();
    private final HashMap<String, BigDecimal> _maximumValues = new HashMap<>();
    private boolean _isValid;
    private int _alleleCount;
    private int _replicateCount;
    private int _iterations;

    public DropoutEstimation() {
        _minimum = BigDecimal.ONE;
        _maximum = BigDecimal.ZERO;
    }

    public boolean isValid() {
        return _isValid;
    }

    public BigDecimal getMaximum() {
        return _maximum;
    }

    public BigDecimal getMinimum() {
        return _minimum;
    }

    public BigDecimal getProsecutionMinimum() {
        return _minimumValues.get("Prosecution");
    }

    public BigDecimal getDefenseMinimum() {
        return _minimumValues.get("Defense");
    }

    public BigDecimal getProsecutionMaximum() {
        return _maximumValues.get("Prosecution");
    }

    public BigDecimal getDefenseMaximum() {
        return _maximumValues.get("Defense");
    }

    public void setAlleleCount(int alleleCount) {
        _alleleCount = alleleCount;
    }

    public int getAlleleCount() {
        return _alleleCount;
    }

    public void setReplicateCount(int replicateCount) {
        _replicateCount = replicateCount;
    }

    public int getReplicateCount() {
        return _replicateCount;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DropoutEstimation) {
            DropoutEstimation other = (DropoutEstimation) obj;
            return getMinimum().equals(other.getMinimum()) && getMaximum().equals(other.getMaximum());
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(_minimum);
        hash = 97 * hash + Objects.hashCode(_maximum);
        return hash;
    }

    @Override
    public String toString() {
        return "Min: " + _minimum + " Max: " + _maximum;
    }

    public void setValues(String hypothesisName, BigDecimal minimum, BigDecimal maximum) {
        if (_minimum.compareTo(minimum) > 0) {
            _minimum = minimum.setScale(2, RoundingMode.HALF_UP);
        }

        if (_maximum.compareTo(maximum) < 0) {
            _maximum = maximum.setScale(2, RoundingMode.HALF_UP);
        }
        _minimumValues.put(hypothesisName, minimum.setScale(2, RoundingMode.HALF_UP));
        _maximumValues.put(hypothesisName, maximum.setScale(2, RoundingMode.HALF_UP));

        _isValid = _minimumValues.size() == 2 && _maximumValues.size() == 2;
    }

    public void setIterations(int iterations) {
        _iterations = iterations;
    }

    public int getIterations() {
        return _iterations;
    }
}
