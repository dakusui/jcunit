package com.github.dakusui.jcunit.framework.examples;

import com.github.dakusui.jcunit.core.FactorField;

public class Struct {
  @SuppressWarnings("unused")
  @FactorField(intLevels = {123,456})
  public int a;
  @SuppressWarnings("unused")
  @FactorField(stringLevels = {"A", "B", "C"})
  private String b;

  public String toString() {
    return a + ";" + b;
  }
}
