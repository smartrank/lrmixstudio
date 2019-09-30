/**
 * Copyright (C) 2018 Netherlands Forensic Institute
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

/**
 * Represents a disabled locus.
 */
public class DisabledLocus {
    private final String _name;
    private final String _reason;

    /**
     * Constructor.
     * @param name the name of the locus
     * @param reason the reason why the locus is disabled
     */
    public DisabledLocus(final String name, final String reason) {
        _name = name;
        _reason = reason;
    }

    /**
     * @return the name of the disabled locus
     */
    public String getName() {
        return _name;
    }

    /**
     * @return the reason why the locus is disabled
     */
    public String getReason() {
        return _reason;
    }
}
