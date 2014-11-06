package com.github.dakusui.jcunit.examples.dfsm;

/**
 * Created by hiroshi on 10/18/14.
 */
public class Turnstile {
  private State state = State.locked;


  public void setState(State state) {
    if (state == null) throw new NullPointerException();
    this.state = state;
  }

  public State getState() {
    return state;
  }

  public static enum Event {
    coin,
    pass
  }

  public static enum State {
    locked,
    unlocked
  }

  public void coin() {
  }

  public String pass() {
    return "Thank you!";
  }

  public boolean isOpened() {
    return false;
  }
}
