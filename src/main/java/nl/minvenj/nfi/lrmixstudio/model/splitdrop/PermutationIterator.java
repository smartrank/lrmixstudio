/*
 * Copyright (c) 2014, Netherlands Forensic Institute
 * All rights reserved.
 */
package nl.minvenj.nfi.lrmixstudio.model.splitdrop;

import java.util.Iterator;

/**
 *
 * @author dejong
 */
public interface PermutationIterator extends Iterator {

    /**
     * Accessor for the next permutation.
     *
     * @return A Permutation object representing the next permutation of loci,
     * or null if no more permutations are available.
     */
    @Override
    Permutation next();

    /**
     * Accessor for the number of permutations in the iterator.
     *
     * @return A long representing the total number of permutations in the
     * iterator.
     */
    long size();

}
