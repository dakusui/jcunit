package com.github.dakusui.jcunit.core.factor;

import com.github.dakusui.jcunit.core.tuples.Tuple;

import java.util.List;

/**
 * An interface for levels providers which generate levels synthesizing from
 * other factor(s) in generated test cases.
 *
 * @param <T> Type of the factor.
 */
public interface FunctionallyDependentLevelsProvider<T> extends LevelsProvider<T> {
  T apply(Tuple tuple);
}
