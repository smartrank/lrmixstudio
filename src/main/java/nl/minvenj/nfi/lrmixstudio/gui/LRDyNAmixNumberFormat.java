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
package nl.minvenj.nfi.lrmixstudio.gui;

import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;

public class LRDyNAmixNumberFormat extends NumberFormat {

    @Override
    public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
        try {
            String ds = "" + number;
            if ("NaN".equalsIgnoreCase(ds) || "Infinity".equalsIgnoreCase(ds)) {
                toAppendTo.append(ds);
                return toAppendTo;
            }

            int decimals = 4;
            int idxExp = ds.indexOf('E');
            if (idxExp < 0) {
                idxExp = ds.length();
            }
            int idxDot = ds.indexOf('.');
            if (idxDot < 0) {
                idxDot = idxExp;
            } else if ((idxExp - idxDot) < decimals) {
                decimals = (idxExp - idxDot) - 1;
            }
            toAppendTo.append(ds.substring(0, idxDot + decimals + 1)).append(ds.substring(idxExp));
        } catch (Exception e) {
            toAppendTo.append("" + number);
        }
        return toAppendTo;
    }

    @Override
    public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Number parse(String source, ParsePosition parsePosition) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
