package com.github.dakusui.jcunit8.extras.abstracter;

import com.github.dakusui.jcunit8.factorspace.Constraint;

import java.util.function.Function;

public interface AbstractConstraint extends Constraint {
  String signature(Function<String, String> factorNameEncoder);
}
