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
package nl.minvenj.nfi.lrmixstudio.utils;

public class LogUtils {

    /**
     * Private constructor. You're not supposed to create instances of this class.
     */
    private LogUtils() {
    }

    /**
     * Adds padding spaces to the beginning or end of a string to a make it a certain length.
     * If the input string exceeds the supplied length, it truncated.
     *
     * @param value The value to pad
     * @param length The length to which to pad. Leading spaces are added if the length is negative, trailing spaces otherwise.
     *
     * @return a copy of the input string padded to the specified length with trailing spaces.
     */
    public static String addPadding(final String value, final int length) {
        if (length < 0) {
            return ("                                        " + value).substring(value.length() + 40 + length);
        }
        return (value + "                                        ").substring(0, length);
    }

}
