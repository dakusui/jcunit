package com.github.dakusui.jcunit8.testsuite;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import org.junit.Test;
import org.junit.runners.model.FrameworkMethod;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public interface TestOracle extends
    Function<Tuple, TestOracle.Result>,
    Predicate<TestOracle.Result> {
  interface Result {
    enum Exit {
      RETURNING_VALUE,
      THROWING_EXCEPTION
    }

    Exit exitedWith();

    <V> V value();
  }

  boolean shouldInvoke(Tuple tuple);

  String getName();

}
