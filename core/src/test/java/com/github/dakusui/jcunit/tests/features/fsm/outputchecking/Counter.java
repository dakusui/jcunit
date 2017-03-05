package com.github.dakusui.jcunit.tests.features.fsm.outputchecking;

/**
 * A counter FSM.
 * This class is reflectively instantiated and used.
 */
public class Counter {
  private int count;

  public Counter() {
    this(0);
  }

  public Counter(int i) {
    this.count = i;
  }

  @SuppressWarnings("unused")
  public void increment() {
    this.count++;
  }

  @SuppressWarnings("unused")
  public int get() {
    return this.count;
  }
}
