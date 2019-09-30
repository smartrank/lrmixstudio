/*
 * Copyright (c) 2014, Netherlands Forensic Institute
 * All rights reserved.
 */
package nl.minvenj.nfi.lrmixstudio.model.splitdrop.threadpool.genotype;

import nl.minvenj.nfi.lrmixstudio.domain.Hypothesis;

/**
 *
 * @author dejong
 */
public class GenotypeProbabilityCalculatorFactory {

    /**
     * Private constructor to stop people from creating instances of this class.
     */
    private GenotypeProbabilityCalculatorFactory() {
    }

    /**
     * Factory method for GenotypeProbabilityCalculator implementations.
     *
     * @param hypothesis The current hypothesis
     * @return An implementation of the GenotypeProbabilityCalculator interface
     * for unrelated unknowns
     */
    public static GenotypeProbabilityCalculator getUnrelatedGenotypeProbabilityCalculator(Hypothesis hypothesis) {
        if (hypothesis.getThetaCorrection() == 0) {
            return new HardyWeinbergGenotypeProbabilityCalculator(hypothesis);
        }
        return new SplitDropGenotypeProbabilityCalculator(hypothesis);
    }

    /**
     * Factory method for GenotypeProbabilityCalculator implementations.
     *
     * @param hypothesis The current hypothesis
     * @return An implementation of the GenotypeProbabilityCalculator interface
     * for related unknowns as specified in the hypothesis
     */
    public static GenotypeProbabilityCalculator getRelatedGenotypeProbabilityCalculator(Hypothesis hypothesis) {
        if (hypothesis.getThetaCorrection() == 0) {
            return getRelatedGenotypeProbabilityCalculatorNoTheta(hypothesis);
        }
        return getRelatedGenotypeProbabilityCalculatorWithTheta(hypothesis);
    }

    /**
     * Creates a GenotypeProbabilityCalculator for cases where theta == 0
     *
     * @param hypothesis the current hypothesis
     * @return A GenotypeProbabilityCalculator
     */
    private static GenotypeProbabilityCalculator getRelatedGenotypeProbabilityCalculatorNoTheta(Hypothesis hypothesis) {
        switch (hypothesis.getRelatedness().getRelation()) {
            case NONE:
                return getUnrelatedGenotypeProbabilityCalculator(hypothesis);
            case PARENT_CHILD:
                return new ParentChildGenotypeProbabilityCalculatorNoTheta(hypothesis);
            case SIBLING:
                return new SiblingGenotypeProbabilityCalculatorNoTheta(hypothesis);
            case COUSIN:
                return new CousinsGenotypeProbabilityCalculatorNoTheta(hypothesis);
            case AUNT_UNCLE_NIECE_NEPHEW:
            case GRANDPARENT_GRANDCHILD:
            case HALF_SIBLING:
                // Note: the equations for half-siblings and uncle/aunt/nephew/niece are identical to those of the grandparent/grandchild relation.
                return new GrandparentGenotypeProbabilityCalculatorNoTheta(hypothesis);
        }
        throw new IllegalArgumentException("Relation " + hypothesis.getRelatedness().getRelation() + " is not yet supported when theta is zero!");
    }

    /**
     * Creates a GenotypeProbabilityCalculator for cases where theta != 0
     *
     * @param hypothesis the current hypothesis
     * @return A GenotypeProbabilityCalculator
     */
    private static GenotypeProbabilityCalculator getRelatedGenotypeProbabilityCalculatorWithTheta(Hypothesis hypothesis) {
        switch (hypothesis.getRelatedness().getRelation()) {
            case NONE:
                return getUnrelatedGenotypeProbabilityCalculator(hypothesis);
            case PARENT_CHILD:
                return new ParentChildGenotypeProbabilityCalculatorWithTheta(hypothesis);
            case SIBLING:
                return new SiblingGenotypeProbabilityCalculatorWithTheta(hypothesis);
            case COUSIN:
                return new CousinsGenotypeProbabilityCalculatorWithTheta(hypothesis);
            case AUNT_UNCLE_NIECE_NEPHEW:
            case GRANDPARENT_GRANDCHILD:
            case HALF_SIBLING:
                // Note: the equations for half-siblings and uncle/aunt/nephew/niece are identical to those of the grandparent/grandchild relation.
                return new GrandparentGenotypeProbabilityCalculatorWithTheta(hypothesis);
        }
        throw new IllegalArgumentException("Relation " + hypothesis.getRelatedness().getRelation() + " is not yet supported when theta is not zero!");
    }
}
