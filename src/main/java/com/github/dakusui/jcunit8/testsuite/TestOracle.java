package com.github.dakusui.jcunit8.testsuite;

import com.github.dakusui.jcunit.core.tuples.Tuple;

public interface TestOracle extends Runnable {
  boolean shouldInvoke(Tuple tuple);
}
