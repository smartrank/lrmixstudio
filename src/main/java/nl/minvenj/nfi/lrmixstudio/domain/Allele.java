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
import java.util.Objects;

/**
 * Represents a single allele
 */
public class Allele {

    private final String allele;
    private float peak;
    private Locus locus;
    private final int hashCode;
    private final int id;
    private static final ArrayList<String> registeredAlleles = new ArrayList<>();

    public static synchronized int getId(String allele) {
        String normalizedAllele = normalize(allele);
        int index = registeredAlleles.indexOf(normalizedAllele);
        if (index >= 0) {
            return index;
        }

        registeredAlleles.add(normalizedAllele);
        return registeredAlleles.indexOf(normalizedAllele);
    }

    public static int getRegisteredAlleleCount() {
        return registeredAlleles.size();
    }

    public static String normalize(String allele) {
        return allele.replaceAll("\\.0$", "");
    }

    /**
     * Constructs a new allele with the specified identifier
     *
     * @param allele The value of this allele
     */
    public Allele(String allele) {
        this(allele, 0);
    }

    /**
     * Constructs a new allele with the specified identifier and peak
     *
     * @param allele The identifier of this allele
     * @param peak The peak value of this allele
     */
    public Allele(String allele, float peak) {
        this.allele = allele.replaceAll("\\.0$", "");
        this.hashCode = 23 * 7 + Objects.hashCode(this.allele);
        id = getId(allele);
        this.peak = peak;
    }

    /**
     * Sets the parent locus of this allele
     *
     * @param locus The parent locus
     */
    public void setLocus(Locus locus) {
        this.locus = locus;
    }

    /**
     * Gets the parent locus for this allele
     *
     * @return a {@link Locus} class representing the
     */
    public Locus getLocus() {
        return locus;
    }

    /**
     * @return <b>true</b> if this allele is part of a homozygote locus
     */
    public boolean isHomozygote() {
        if (locus != null) {
            return locus.isHomozygote();
        }
        return false;
    }

    /**
     * @return the allele identifier for this allele
     */
    public String getAllele() {
        return allele;
    }

    /**
     * @return An integer identifying this allele
     */
    public int getId() {
        return id;
    }

    /**
     * @return the peak value of this allele
     */
    public float getPeak() {
        return peak;
    }

    /**
     * @return The string representation of this allele. Essentially this is the
     * allele identifier suffixed with a single quote if the allele is part of a
     * homozygote locus
     */
    @Override
    public String toString() {
        return getAllele() + (isHomozygote() ? "'" : "");
    }

    /**
     * Compares this allele to another object and returns true if the objects
     * are equal.
     *
     * @param other The object to compare with.
     * @return true if the other object is an allele and has the same identifier
     */
    @Override
    public boolean equals(Object other) {
        try {
            return (((Allele) other).hashCode == hashCode);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    /**
     * Sets the peak value for this allele.
     *
     * @param peakValue a float containing the peak value
     */
    public void setPeak(float peakValue) {
        peak = peakValue;
    }
}
