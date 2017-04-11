package com.github.dakusui.jcunit.examples.models.modifiedmealymachine;

public class ModifiedMealyMachine {
  boolean init = false;
  String s;

  public ModifiedMealyMachine(
      String s,
      // This parameter is only for illustration of how to call a constructor with parameters
      @SuppressWarnings("unused") int i,
      // This parameter is only for illustration of how to call a constructor with parameters
      @SuppressWarnings("unused") int[] j
  ) {
    this.s = s + ":";
  }

  @SuppressWarnings("unused")
  public void method(String s) {
    init = true;
    this.s = this.s + s + ":";
  }

  public String getS() {
    if (!init)
      throw new IllegalStateException();
    return this.s;
  }
}
