package com.github.dakusui.jcunit.tests.features.fsm.outputchecking;

/**
 * An accumulator FSM.
 * This class is reflectively instantiated and used.
 */
public class Accumulator {
  private int value;

  @SuppressWarnings("unused")
  public void add(int a) {
    this.value += a;
  }

  @SuppressWarnings("unused")
  public int get() {
    return this.value;
  }
}
