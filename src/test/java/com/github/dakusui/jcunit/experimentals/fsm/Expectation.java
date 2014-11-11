package com.github.dakusui.jcunit.experimentals.fsm;

import org.hamcrest.Matcher;

public class Expectation<SUT> {
  public final State<SUT>   state;
  public final Matcher returnedValue;
  public final Matcher thrownException;

  public Expectation(State<SUT> state,
      Matcher returnedValue,
      Matcher thrownException) {
    this.state = state;
    this.returnedValue = returnedValue;
    this.thrownException = thrownException;
  }
}
