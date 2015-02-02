package com.github.dakusui.jcunit.examples.fsm.turnstile;

public class Turnstile {
  State state = State.Locked;

  public void coin() {
    this.state = State.Unlocked;
  }

  public void pass() throws Exception {
    if (this.state != State.Locked)
      throw new RuntimeException();
    this.state = State.Locked;
  }

  static enum State {
    Locked,
    Unlocked
  }
}
