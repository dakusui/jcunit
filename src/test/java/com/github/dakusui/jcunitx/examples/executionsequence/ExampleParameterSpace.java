package com.github.dakusui.jcunitx.examples.executionsequence;

import com.github.dakusui.jcunitx.metamodel.Parameter;
import com.github.dakusui.jcunitx.runners.helpers.ParameterUtils;
import com.github.dakusui.jcunitx.runners.junit4.annotations.Condition;
import com.github.dakusui.jcunitx.runners.junit4.annotations.From;
import com.github.dakusui.jcunitx.runners.junit4.annotations.ParameterSource;

public class ExampleParameterSpace {
  @Condition(constraint = true)
  public boolean bIsGreaterThanC(@From("b") int b, @From("c") int c) {
    return b > c;
  }

  @ParameterSource
  public Parameter.Descriptor<?> a() {
    return ParameterUtils.simple(1, 2);
  }

  @ParameterSource
  public Parameter.Descriptor<?> b() {
    return ParameterUtils.simple(1, 2);
  }

  @ParameterSource
  public Parameter.Descriptor<?> c() {
    return ParameterUtils.simple(1, 2);
  }
}