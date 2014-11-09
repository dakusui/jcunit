package com.github.dakusui.jcunit.experimentals.fsm;

import com.github.dakusui.jcunit.core.Checks;

public class Scenario<SUT> {
  public final State<SUT>  given;
  public final Action<SUT> when;
  public final Args        with;

  public Scenario(State<SUT> given, Action<SUT> when, Args with) {
    Checks.checknotnull(given);
    Checks.checknotnull(when);
    Checks.checknotnull(with);
    this.given = given;
    this.when = when;
    this.with = with;
  }

  public Object perform(SUT sut) throws Throwable {
    return when.perform(sut, this.with);
  }

  public boolean canFollow(Scenario previous) {
    Checks.checknotnull(previous);
    return previous.then() == this.given;
  }

  public State<SUT> then() {
    return this.given.expectation(this.when, this.with).state;
  }
}
