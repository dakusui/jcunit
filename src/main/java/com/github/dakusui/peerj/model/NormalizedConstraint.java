package com.github.dakusui.peerj.model;

import com.github.dakusui.jcunit8.factorspace.Constraint;

import java.util.function.Function;

public interface NormalizedConstraint extends Constraint {
  String toText(Function<String, String> factorNameToParameterName);
}
