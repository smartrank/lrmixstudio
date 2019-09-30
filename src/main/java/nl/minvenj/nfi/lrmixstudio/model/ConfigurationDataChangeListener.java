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
 * This interface is implemented by objects that are to be notified of changes
 * in the configuration data.
 */
public interface ConfigurationDataChangeListener {

    /**
     * This method gets called for all listeners if the configuration data has
     * changed TODO: pass information on what changed?
     *
     * @param element A {@link ConfigurationData.ConfigurationDataElement} class
     * indicating what element changed
     */
    public void dataChanged(ConfigurationDataElement element);
}
