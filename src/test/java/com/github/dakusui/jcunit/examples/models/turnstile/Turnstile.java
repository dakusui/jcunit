package com.github.dakusui.jcunit.examples.models.turnstile;

public class Turnstile {
  public State state = State.Locked;

  @SuppressWarnings("unused") // Reflectively invoked by tests
  public void coin() throws InterruptedException {
    Thread.sleep(1);
    this.state = State.Unlocked;
  }

  @SuppressWarnings("unused") // Reflectively invoked by tests
  public void pass() throws Exception {
    Thread.sleep(1);
    if (this.state != State.Locked)
      throw new RuntimeException();
    this.state = State.Locked;
  }

  public enum State {
    Locked,
    Unlocked
  }
}
