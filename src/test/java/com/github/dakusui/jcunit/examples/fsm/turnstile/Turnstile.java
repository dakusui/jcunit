package com.github.dakusui.jcunit.examples.fsm.turnstile;

public class Turnstile {
  State state = State.Locked;

  @SuppressWarnings("unused") // Reflectively invoked by tests
  public void coin() {
    this.state = State.Unlocked;
  }

  @SuppressWarnings("unused") // Reflectively invoked by tests
  public void pass() throws Exception {
    if (this.state != State.Locked)
      throw new RuntimeException();
    this.state = State.Locked;
  }

  enum State {
    Locked,
    Unlocked
  }
}
