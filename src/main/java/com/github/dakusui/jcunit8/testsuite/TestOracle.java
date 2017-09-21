package com.github.dakusui.jcunit8.testsuite;

import com.github.dakusui.jcunit.core.tuples.Tuple;

import java.lang.annotation.Annotation;
import java.util.function.Consumer;

public interface TestOracle extends Consumer<Tuple> {
  boolean shouldInvoke(Tuple tuple);

  String getName();

  interface ForJUnit4 extends TestOracle {
    Annotation[] annotations();
  }
}
