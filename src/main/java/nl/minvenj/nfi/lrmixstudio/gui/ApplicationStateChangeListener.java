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

public interface ApplicationStateChangeListener {

    public static enum APP_STATE {

        WAIT_SAMPLE, WAIT_PROFILES, WAIT_HYPOTHESIS, READY_FOR_OVERVIEW, READY_FOR_ANALYSIS, ANALYSIS_RUNNING(true, "Running analysis"), SENSITIVITY_ANALYSIS_RUNNING(true, "Running Sensitivity Analysis"), DROPOUT_ESTIMATION_RUNNING(true, "Running Dropout Estimation"), NONCONTRIBUTOR_TEST_RUNNING(true, "Running Non-contributor Test"), EXPORT_RUNNING(true, "Exporting report");
        private boolean active = false;
        private String description;

        private APP_STATE() {
            this.description = "Waiting for input";
            this.active = false;
        }

        private APP_STATE(String description) {
            this.description = description;
            this.active = false;
        }

        private APP_STATE(boolean active, String description) {
            this.description = description;
            this.active = active;
        }

        public boolean isActive() {
            return active;
        }

        String getDescription() {
            return description;
        }
    };

    public void applicationStateChanged(APP_STATE newState);
}
