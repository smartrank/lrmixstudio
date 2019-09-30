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

import java.awt.Color;

import nl.minvenj.nfi.lrmixstudio.domain.Sample;
import nl.minvenj.nfi.lrmixstudio.gui.ApplicationSettings;
import nl.minvenj.nfi.lrmixstudio.gui.SessionData;

/**
 * This class serves as abstract base class to allele decorators for the Profile
 * Summary table.
 *
 * @author dejong
 */
public abstract class AlleleDecorator {

    private final String description;
    private final SessionData session;
    private int _highlightedAlleleCount;

    /**
     * Constructor
     *
     * @param description A textual description to appear in the decorator
     * selection combo box
     * @param session The current session
     */
    public AlleleDecorator(String description, SessionData session) {
        this.description = description;
        this.session = session;
    }

    /**
     * @return A textual description to appear in the decorator selection combo
     * box
     */
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return description;
    }

    /**
     * Applies the decorator to applicable alleles in the supplied string
     *
     * @param replicate The current replicate
     * @param referenceProfile The current reference profile
     * @param locusId The ID of the current locus
     * @return A String containing HTML markup decorating applicable alleles.
     */
    public abstract String apply(Sample replicate, Sample referenceProfile, String locusId, String alleles);

    /**
     * @return The current session
     */
    protected SessionData getSession() {
        return session;
    }

    private String toHtmlColor(Color c, boolean enabled) {
        int red = c.getRed();
        int green = c.getGreen();
        int blue = c.getBlue();

        if (!enabled) {
            int avg = (red + green + blue) / 3;
            if (avg < 0x00C0) {
                avg = 0x00c0;
            }
            red = avg;
            green = avg;
            blue = avg;
        }

        String redString = "0" + Integer.toHexString(red);
        redString = redString.substring(redString.length() - 2).toUpperCase();
        String greenString = "0" + Integer.toHexString(green);
        greenString = greenString.substring(greenString.length() - 2).toUpperCase();
        String blueString = "0" + Integer.toHexString(blue);
        blueString = blueString.substring(blueString.length() - 2).toUpperCase();
        return redString + greenString + blueString;
    }

    protected String getHighlightStyle(boolean enabled) {
        String style = "";
        if (ApplicationSettings.isSetHighlightColor()) {
            style += "color: #" + toHtmlColor(ApplicationSettings.getHighlightColor(), enabled) + ";";
        }
        if (ApplicationSettings.isSetHighlightBackgroundColor()) {
            style += "background-color: #" + toHtmlColor(ApplicationSettings.getHighlightBackgroundColor(), enabled) + ";";
        }
        if (ApplicationSettings.getHighlightBold()) {
            style += "font-weight: bold;";
        }
        if (ApplicationSettings.getHighlightItalic()) {
            style += "font-style: italic;";
        }
        if (ApplicationSettings.getHighlightUnderlined()) {
            style += "text-decoration: underline;";
        }
        return style;
    }

    protected String highlight(String allele, boolean enabled) {
        _highlightedAlleleCount++;
        return "<span style=\"" + getHighlightStyle(enabled) + "\">&nbsp;" + allele + "&nbsp;</span>";
    }

    protected String disable(String allele) {
        return "<span style=\"color: #c0c0c0;\">" + allele + "</span>";
    }

    public void resetCount() {
        _highlightedAlleleCount = 0;
    }

    public int getHighlightedAlleleCount() {
        return _highlightedAlleleCount;
    }
}
