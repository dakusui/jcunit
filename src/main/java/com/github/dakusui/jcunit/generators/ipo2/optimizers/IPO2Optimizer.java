package com.github.dakusui.jcunit.generators.ipo2.optimizers;

import com.github.dakusui.jcunit.constraints.ConstraintManager;
import com.github.dakusui.jcunit.core.ValueTuple;
import com.github.dakusui.jcunit.generators.ipo2.LeftTuples;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by hiroshi on 6/30/14.
 */
public interface IPO2Optimizer {
  public Object chooseBestValue(String factorName, Object[] factorLevels,
      ValueTuple<String, Object> tuple, LeftTuples leftTuples);
  public ValueTuple<String, Object> fillInMissingFactors(ValueTuple tuple,
      LinkedHashMap<String, Object[]> missingFactors, LeftTuples leftTuples,
      ConstraintManager<String, Object> constraintManager);
  /**
   * An extension point.
   * Called by 'vg' process.
   * Chooses the best tuple to assign the factor and its level from the given tests.
   *
   * @param found A list of cloned tuples. (candidates)
   */
  public ValueTuple<String, Object> chooseBestTuple(
      List<ValueTuple<String, Object>> found, LeftTuples leftTuples,
      String factorName, Object level);
}
