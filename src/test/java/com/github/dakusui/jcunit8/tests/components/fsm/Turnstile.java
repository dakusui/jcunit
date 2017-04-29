package com.github.dakusui.jcunit8.tests.components.fsm;

public class Turnstile {
  boolean opened;

  @SuppressWarnings("unused")
  public void coin() {
    if (!opened)
      opened = true;
    else
      throw new IllegalStateException();
  }

  @SuppressWarnings("unused")
  public void pass() {
    if (opened)
      opened = false;
    else
      throw new IllegalStateException();
  }

  static class Broken extends Turnstile {
    public void coin() {
      throw new IllegalStateException();
    }
  }
}
