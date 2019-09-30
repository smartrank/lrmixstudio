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

/**
 * This enum contains values used to denote specific data elements in calls to
 * the dataChanged method of the ConfigurationDataChangeListener interface.
 */
public enum ConfigurationDataElement {

    PROSECUTION, DEFENSE, PROFILES, ACTIVEPROFILES, REPLICATES, ACTIVEREPLICATES, STATISTICS, RARE_ALLELES_FREQUENCY, CASENUMBER, MODELNAME, ACTIVELOCI, STATUS_MESSAGE, ERROR_MESSAGE, PERCENTREADY, SENSITIVITYANALYSISRESULTS
}
