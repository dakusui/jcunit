package com.github.dakusui.jcunit8.experiments.join.acts;


import java.util.function.Function;

public interface ActsPredicate {
  String toText(Function<String, String> factorNameToParameterName);
}
