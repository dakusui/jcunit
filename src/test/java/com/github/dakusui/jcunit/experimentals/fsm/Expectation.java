package com.github.dakusui.jcunit.experimentals.fsm;

import org.hamcrest.Matcher;

public class Expectation {
  public final State   state;
  public final Matcher returnedValue;
  public final Matcher thrownException;

  public Expectation(State state,
      Matcher returnedValue,
      Matcher thrownException) {
    this.state = state;
    this.returnedValue = returnedValue;
    this.thrownException = thrownException;
  }
}
