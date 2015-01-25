package com.github.dakusui.jcunit.core.factor;

import com.github.dakusui.jcunit.core.tuples.Tuple;

import java.util.List;

/**
 * @param <T> Type of the factor.
 */
public interface FactorMapper<T> {
  String factorName();
  T apply(Tuple tuple);
}
