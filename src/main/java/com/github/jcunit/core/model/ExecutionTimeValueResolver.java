package com.github.jcunit.core.model;

import com.github.jcunit.core.tuples.Tuple;
import com.github.jcunit.factorspace.Parameter;
import com.github.jcunit.testsuite.TestCase;

/**
 * // @formatter:off
 * // @formatter:on
 */
public
interface ExecutionTimeValueResolver<T, E> {
  default E resolve(T generationTimeValue, TestCase testCase) {
    return resolve(generationTimeValue, testCase.getTestData());
  }

  E resolve(T generationTimeValue, Tuple testData);

  interface Factory<P extends Parameter<T>, T, E> {
    ExecutionTimeValueResolver<T, E> create();
  }
}
