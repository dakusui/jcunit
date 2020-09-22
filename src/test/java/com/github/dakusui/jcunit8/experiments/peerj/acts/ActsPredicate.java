package com.github.dakusui.jcunit8.experiments.peerj.acts;


import java.util.function.Function;

public interface ActsPredicate {
  String toText(Function<String, String> factorNameToParameterName);
}
