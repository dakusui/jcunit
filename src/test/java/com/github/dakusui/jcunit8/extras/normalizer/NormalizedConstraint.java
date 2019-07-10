package com.github.dakusui.jcunit8.extras.normalizer;

import com.github.dakusui.jcunit8.extras.generators.ActsPredicate;
import com.github.dakusui.jcunit8.factorspace.Constraint;

import java.util.function.Function;

public interface NormalizedConstraint extends Constraint, ActsPredicate {
  default String signature(Function<String, String> factorNameEncoder) {
    throw new UnsupportedOperationException();
  }
}
