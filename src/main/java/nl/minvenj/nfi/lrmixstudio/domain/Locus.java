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
import java.util.Objects;

public class Locus {

    private final String name;
    private final ArrayList<Allele> alleles = new ArrayList();
    private boolean homozygote = false;
    private Sample sample;
    private int hashCode;
    private final int id;
    private boolean _treatedAsHomozygote;
    private static final ArrayList<String> registeredLoci = new ArrayList<>();

    public static synchronized int getId(final String name) {
        final int index = registeredLoci.indexOf(name);
        if (index >= 0) {
            return index;
        }

        registeredLoci.add(name);
        return registeredLoci.indexOf(name);
    }

    public Locus(final String name) {
        this.name = name;
        this.id = getId(name);
        updateHashCode();
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public void setSample(final Sample sample) {
        this.sample = sample;
    }

    public Sample getSample() {
        return sample;
    }

    public String getSourceFile() {
        if (sample != null) {
            return sample.getSourceFile();
        }
        return "";
    }

    public String getSampleId() {
        if (sample != null) {
            return sample.getId();
        }
        return "";
    }

    public void setHomozygote(final boolean homozygote) {
        this.homozygote = homozygote;
    }

    public boolean isHomozygote() {
        return homozygote;
    }

    public void addAllele(final Allele allele) {
        // Let the allele know which locus it belongs to
        allele.setLocus(this);

        // If there is already an equal allele in this sample, we have a homozygote on our hands!
        setHomozygote(alleles.contains(allele));
        alleles.add(allele);
        updateHashCode();
    }

    public int size() {
        return alleles.size();
    }

    public Collection<Allele> getAlleles() {
        return alleles;
    }

    public boolean hasAllele(final String allele) {
        for (final Allele myAllele : alleles) {
            if (myAllele.getAllele().equals(allele)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(final Object other) {
        return (other instanceof Locus) && ((Locus) other).hashCode == hashCode;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    private void updateHashCode() {
        int hash = 7;
        hash = 83 * hash + Objects.hashCode(this.name);
        hash = 83 * hash + Objects.hashCode(this.alleles);
        hashCode = hash;
    }

    /**
     * Sets the flag that indicates that this locus was treated as a homozygote due to the fact that the profile only contained a single allele at this locus.
     */
    public void setTreatedAsHomozygote() {
        _treatedAsHomozygote = true;
    }

    /**
     * @return true if the locus is treated as homozygote due to the fact that the sample file only contained a single allele at this locus. false if the locus
     * in the sample file contained 2 alleles, or if the locus is part of a mix profile (in which case single alleles are not duplicated, and in fact duplicate
     * alleles are actively removed)
     */
    public boolean isTreatedAsHomozygote() {
        return _treatedAsHomozygote;
    }

    public void removeDuplicateAlleles() {
        final ArrayList<Integer> toRemove = new ArrayList<>();
        for (int refIdx = 0; refIdx < alleles.size(); refIdx++) {
            final Allele ref = alleles.get(refIdx);
            for (int idx = refIdx + 1; idx < alleles.size(); idx++) {
                if (!toRemove.contains(idx)) {
                    final Allele toTest = alleles.get(idx);
                    if (toTest.equals(ref)) {
                        toRemove.add(idx);
                    }
                }
            }
        }

        for (int removeIndex = toRemove.size() - 1; removeIndex >= 0; removeIndex--) {
            alleles.remove((int) toRemove.get(removeIndex));
            _treatedAsHomozygote = false;
        }
    }
}

