package com.github.dakusui.jcunit.generators.ipo2;

import com.github.dakusui.jcunit.core.ValueTuple;

public class Tuple extends ValueTuple<String, Object> {
  @Override
  public Tuple clone() {
    return (Tuple) super.clone();
  }
}
