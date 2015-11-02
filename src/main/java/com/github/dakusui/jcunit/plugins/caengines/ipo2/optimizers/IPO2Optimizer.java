package com.github.dakusui.jcunit.plugins.caengines.ipo2.optimizers;

import com.github.dakusui.jcunit.plugins.constraints.Constraint;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.tuples.Tuples;

import java.util.List;

public interface IPO2Optimizer {
  Tuple fillInMissingFactors(Tuple tuple,
      Tuples leftTuples,
      Constraint constraint,
      Factors factors);

  /**
   * An extension point.
   * Called by 'vg' process.
   * Chooses the best tuple to assign the factor and its level from the given tests.
   *
   * @param found A list of cloned tuples. (candidates)
   */
  Tuple chooseBestTuple(
      List<Tuple> found, Tuples leftTuples,
      String factorName, Object level);

  Object chooseBestValue(String factorName, Object[] factorLevels,
      Tuple tuple, Tuples leftTuples);
}
