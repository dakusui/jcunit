package com.github.dakusui.jcunit.examples.fsm.turnstile;

public class Turnstile {
  static enum State {
    Locked,
    Unlocked
  }

  State state = State.Locked;

  public void coin() {
    this.state = State.Unlocked;
  }

  public void pass() throws Exception {
    if (this.state != State.Locked) throw new Exception("Hello");
    this.state = State.Locked;
  }
}
