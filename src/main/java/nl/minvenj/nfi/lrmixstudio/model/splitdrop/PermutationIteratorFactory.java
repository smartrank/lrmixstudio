/*
 * Copyright (c) 2014, Netherlands Forensic Institute
 * All rights reserved.
 */

package nl.minvenj.nfi.lrmixstudio.model.splitdrop;

import java.util.Collection;

import nl.minvenj.nfi.lrmixstudio.domain.Hypothesis;
import nl.minvenj.nfi.lrmixstudio.domain.Locus;
import nl.minvenj.nfi.lrmixstudio.domain.Relatedness.Relation;

/**
 *
 * @author dejong
 */
public class PermutationIteratorFactory {

    public static PermutationIterator getPermutationIterator(Hypothesis hypothesis, Collection<Locus> loci, int idx) {
        if (hypothesis.getRelatedness().getRelation() == Relation.NONE) {
            return new PermutationIteratorPlain(hypothesis.getUnknownCount(), loci, idx);
        }
        return new PermutationIteratorRelatedness(hypothesis.getUnknownCount(), loci, idx);
    }

    private PermutationIteratorFactory() {
    }

}
