package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.Checks;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;

public class Expectation<SUT> {
  public final State<SUT> state;
  public final Matcher    returnedValue;
  public final Matcher    thrownException;

  private Expectation(State<SUT> state,
      Matcher returnedValue,
      Matcher thrownException) {
    this.state = state;
    this.returnedValue = returnedValue;
    this.thrownException = thrownException;
  }

  public Expectation(Matcher thrownException) {
    this((State<SUT>) State.VOID,
        CoreMatchers.is(FSMFactors.VOID),
        Checks.checknotnull(thrownException));
  }

  public Expectation(State<SUT> state,
      Matcher returnedValue) {
    this(Checks.checknotnull(state), Checks.checknotnull(returnedValue), null);
  }

  @Override
  public String toString() {
    if (this.state == State.VOID)
      return String.format("%s:%s is thrown", this.state, this.returnedValue);
    return String.format("%s:%s is returned", this.state, this.returnedValue);
  }

}
