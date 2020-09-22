package com.github.dakusui.peerj.acts;


import java.util.function.Function;

public interface ActsPredicate {
  String toText(Function<String, String> factorNameToParameterName);
}
