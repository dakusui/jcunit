package com.github.dakusui.jcunit8.examples.executionsequence;

import com.github.dakusui.jcunit8.factorspace.Parameter;
import com.github.dakusui.jcunit8.runners.helpers.ParameterUtils;
import com.github.dakusui.jcunit8.runners.junit4.annotations.Condition;
import com.github.dakusui.jcunit8.runners.junit4.annotations.From;
import com.github.dakusui.jcunit8.runners.junit4.annotations.ParameterSource;

public class ExampleParameterSpace {
  @Condition(constraint = true)
  public boolean bIsGreaterThanC(@From("b") int b, @From("c") int c) {
    return b > c;
  }

  @ParameterSource
  public Parameter.Factory a() {
    return ParameterUtils.simple(1, 2);
  }

  @ParameterSource
  public Parameter.Factory b() {
    return ParameterUtils.simple(1, 2);
  }

  @ParameterSource
  public Parameter.Factory c() {
    return ParameterUtils.simple(1, 2);
  }
}