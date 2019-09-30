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

import java.awt.Graphics;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

import com.lowagie.text.Font;

/**
 *
 * @author dejong
 */
public class ProfileSummaryPrintable implements Printable {
    
    private final Printable delegate;
    private final String subTitle;
    private final String title;

    public ProfileSummaryPrintable(Printable delegate, String title, String subTitle) {
        this.delegate = delegate;
        this.title = title;
        this.subTitle = subTitle;
    }
    
    @Override
    public int print(final Graphics graphics, final PageFormat pageFormat, final int pageIndex) throws PrinterException {
        // Find offset and height of header line
        final int height = graphics.getFontMetrics().getHeight();
        /*
         * We'll use a copy of the graphics to print the table to. This protects
         * us against changes that the delegate printable could make to the graphics
         * object.
         */
        Graphics gCopy = graphics.create();
        
        PageFormat format = new PageFormat() {
            @Override
            public double getImageableHeight() {
                return pageFormat.getImageableHeight() - height * 2 - 10;
            }
            
            @Override
            public double getImageableWidth() {
                return pageFormat.getImageableWidth();
            }
            
            @Override
            public double getImageableX() {
                return pageFormat.getImageableX();
            }
            
            @Override
            public double getImageableY() {
                return pageFormat.getImageableY() + height * 2 + 10;
            }
        };


        // Print the table into the shrunken area
        int retVal = delegate.print(gCopy, format, pageIndex);

        // Dispose of the copy of the Graphics context
        gCopy.dispose();

        // if there's no pages left, return
        if (retVal == NO_SUCH_PAGE) {
            return retVal;
        }

        // Draw the header line
        graphics.setFont(graphics.getFont().deriveFont(Font.BOLD));
        graphics.drawString(title, (int) pageFormat.getImageableX(), (int) pageFormat.getImageableY() + height + 2);
        graphics.setFont(graphics.getFont().deriveFont(Font.ITALIC));
        graphics.drawString(subTitle, (int) pageFormat.getImageableX(), (int) pageFormat.getImageableY() + height * 2 + 2);
        return retVal;
    }
}
