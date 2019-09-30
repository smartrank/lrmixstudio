/**
 * Copyright (C) 2013-2015 Netherlands Forensic Institute
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a level of relatedness between an unknown contributor and a known
 * profile.
 *
 * @author dejong
 */
public class Relatedness {

    private static final Logger LOG = LoggerFactory.getLogger(Relatedness.class);
    private Sample _relative;
    private Relation _relation;

    public Relatedness() {
        _relation = Relation.NONE;
        _relative = null;
    }

    public void setRelation(Relation relation) {
        LOG.debug("Setting relation to {}", relation);
        _relation = relation;
    }

    public Relation getRelation() {
        return _relation;
    }

    public void setRelative(Sample relative) {
        LOG.debug("Setting relative to {}", relative);
        _relative = relative;
    }

    public Sample getRelative() {
        return _relative;
    }

    @Override
    public String toString() {
        if (_relation == Relation.NONE) {
            return _relation.toString();
        }
        return _relation + " of " + _relative.getId();
    }

    /**
     * Describes the relation defined in this Relatedness object to the named
     * sample.
     *
     * @param sampleName The name of the sample to describe the relatedness
     *                   with.
     *
     * @return A string describing the relatedness settings for the named
     *         sample.
     */
    public String toString(String sampleName) {
        if (_relation == Relation.NONE || !_relative.getId().equalsIgnoreCase(sampleName)) {
            return sampleName;
        }
        return _relation + " of " + sampleName;
    }

    /**
     * An enumeration for the various levels of relatedness.
     */
    public static enum Relation {

        NONE("No relation"), PARENT_CHILD("Parent/Child"), SIBLING("Sibling"), HALF_SIBLING("Half-sibling"), GRANDPARENT_GRANDCHILD("Grandparent/Grandchild"), AUNT_UNCLE_NIECE_NEPHEW("Uncle/Nephew"), COUSIN("Cousin");
        private final String _description;

        private Relation(String description) {
            _description = description;
        }

        public static Relation fromDescription(String desc) {
            return valueOf(desc.toUpperCase().replaceAll("[/-]", "_"));
        }

        @Override
        public String toString() {
            return _description;
        }
    }
}
