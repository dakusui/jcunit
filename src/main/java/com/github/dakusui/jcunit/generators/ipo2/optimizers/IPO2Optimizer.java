package com.github.dakusui.jcunit.generators.ipo2.optimizers;

import com.github.dakusui.jcunit.constraints.ConstraintManager;
import com.github.dakusui.jcunit.core.ValueTuple;
import com.github.dakusui.jcunit.generators.ipo2.Factors;
import com.github.dakusui.jcunit.generators.ipo2.LeftTuples;
import com.github.dakusui.jcunit.generators.ipo2.Tuple;

import java.util.List;
import java.util.Map;

/**
 * Created by hiroshi on 6/30/14.
 */
public interface IPO2Optimizer {
  public Tuple fillInMissingFactors(Tuple tuple,
      LeftTuples leftTuples,
      ConstraintManager constraintManager,
      Factors factors);

  /**
   * An extension point.
   * Called by 'vg' process.
   * Chooses the best tuple to assign the factor and its level from the given tests.
   *
   * @param found A list of cloned tuples. (candidates)
   */
  public Tuple chooseBestTuple(
      List<Tuple> found, LeftTuples leftTuples,
      String factorName, Object level);

  public Object chooseBestValue(String factorName, Object[] factorLevels,
      Tuple tuple, LeftTuples leftTuples);
}
