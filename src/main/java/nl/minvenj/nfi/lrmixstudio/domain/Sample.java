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
package nl.minvenj.nfi.lrmixstudio.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Sample class represents a DNA sample containing a number of Loci which in
 * turn contain a number of alleles.
 */
public class Sample {

    private static final Logger LOG = LoggerFactory.getLogger(Sample.class);
    private String id;
    private boolean enabled;
    private HashMap<String, Locus> loci;
    private final String sourceFile;
    private String sourceFileHash = null;
    private Locus _locus;

    /**
     * Creates a new DNASample object
     *
     * @param id The id of the sample
     */
    public Sample(String id, String sourceFile) {
        this.loci = new LinkedHashMap<>();
        this.id = id;
        this.enabled = true;
        this.sourceFile = sourceFile;
    }

    /**
     * Creates a new DNASample object
     *
     * @param id The id of the sample
     */
    public Sample(String id) {
        this.id = id;
        this.enabled = true;
        this.sourceFile = null;
    }

    /**
     * @return The getName of the sample
     */
    public String getId() {
        return id;
    }

    public void addLocus(Locus locus) {
        if (_locus == null && loci == null) {
            _locus = locus;
            _locus.setSample(this);
        } else {
            if (loci == null) {
                loci = new LinkedHashMap<>();
            }
            if (_locus != null) {
                loci.put(_locus.getName(), _locus);
                _locus = null;
            }
            loci.put(locus.getName(), locus);
            locus.setSample(this);
        }
    }

    public int size() {
        if (loci == null) {
            if (_locus == null) {
                return 0;
            }
            return 1;
        }
        return loci.size();
    }

    public Locus getLocus(String id) {
        if (_locus == null) {
            if (loci == null) {
                return null;
            }
            return loci.get(id);
        }
        if (_locus.getName().equals(id)) {
            return _locus;
        }
        return null;
    }

    public Collection<Locus> getLoci() {
        if (loci != null) {
            return loci.values();
        }
        List<Locus> retval = new ArrayList<>();
        if (_locus != null) {
            retval.add(_locus);
        }
        return retval;
    }

    @Override
    public String toString() {
        return getId();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        LOG.debug("Sample {} setEnabled({})", id, enabled);
        this.enabled = enabled;
    }

    /**
     * @return the sourceFile
     */
    public String getSourceFile() {
        return sourceFile;
    }

    /**
     * Sets the file hash of the source file
     *
     * @param fileHash A String containing the hash of the source file
     */
    public void setSourceFileHash(String fileHash) {
        this.sourceFileHash = fileHash;
    }

    /**
     * @return A String containing the hash over the contents of the source file
     */
    public String getSourceFileHash() {
        return sourceFileHash;
    }

    /**
     * Sets a new ID for this sample. Useful when reading Genemapper files for
 samples where the getNames for all replicates may be the same, and we have
 to add a postfix.
     *
     * @param id The new ID of this sample
     */
    public void setId(String id) {
        this.id = id;
    }
}
