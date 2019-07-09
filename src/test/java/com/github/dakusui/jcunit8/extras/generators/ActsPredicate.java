package com.github.dakusui.jcunit8.extras.generators;


import java.util.function.Function;

public interface ActsPredicate {
  String toText(Function<String, String> factorNameToParameterName);
}
