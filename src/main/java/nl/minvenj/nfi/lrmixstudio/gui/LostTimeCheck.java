/**
 * Copyright (C) 2014 Netherlands Forensic Institute
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

import java.awt.Component;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.minvenj.nfi.lrmixstudio.model.AnalysisReport;

/**
 * A class that checks the un-exported reports on shutdown of the application.
 * If the total calculation time that was required to generate these reports
 * exceeds a certain configurable threshold, a warning is displayed allowing the
 * user to cancel the closing operation and export the reports.
 */
public class LostTimeCheck {

    private static final Logger LOG = LoggerFactory.getLogger(LostTimeCheck.class);

    private LostTimeCheck() {
    }

    public static int performExitCheck(final SessionData session, final Component parent) {
        return perform("Exiting", session, parent);
    }

    public static int performRestartCheck(final SessionData session, final Component parent) {
        return perform("Restarting", session, parent);
    }

    private static int perform(final String action, final SessionData session, final Component parent) {
        long totalTime = 0;
        int reportCount = 0;
        for (AnalysisReport report : session.getReports()) {
            if (report.isSucceeded() && !report.isExported() && report.getProcessingTime() > 0) {
                totalTime += report.getProcessingTime();
                reportCount++;
            }
        }
        if (totalTime > ApplicationSettings.getLostTimeThreshold()) {
            String time = "";
            int seconds = (int) (totalTime / 1000) % 60;
            int minutes = (int) (totalTime / 60000) % 60;
            int hours = (int) (totalTime / 3600000);
            if (hours > 0) {
                time += hours + " hour" + (hours > 1 ? "s " : " ");
            }
            if (minutes > 0) {
                if (hours > 0 && seconds == 0) {
                    time += "and ";
                }
                time += minutes + " minute" + (minutes > 1 ? "s " : " ");
            }
            if (seconds > 0) {
                if (hours > 0 || minutes > 0) {
                    time += "and ";
                }
                time += seconds + " second" + (seconds > 1 ? "s " : " ");
            }

            if (time.isEmpty()) {
                time = "less than 1 second ";
            }

            LOG.debug("Total calculation time for all unexported reports = {} ms = {}. Asking the user if they are sure about {}.", totalTime, time, action.toLowerCase());

            return JOptionPane.showConfirmDialog(
                    parent,
                    "<html>There " + (reportCount > 1 ? "are" : "is") + " currently <b>" + reportCount + "</b> unexported report" + (reportCount > 1 ? "s" : "") + " representing <b>" + time + "</b>of calculation time."
                    + "<br>" + action + " the application now will <b>discard</b> those reports."
                    + "<br>Click <i>OK</i> to " + action.substring(0, action.length() - 3).toLowerCase() + " and discard the reports, or click <i>Cancel</i> to return without " + action.toLowerCase() + ".",
                    "Are you sure you want to " + action.substring(0, action.length() - 3).toLowerCase() + "?", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        }
        return JOptionPane.OK_OPTION;
    }
}
