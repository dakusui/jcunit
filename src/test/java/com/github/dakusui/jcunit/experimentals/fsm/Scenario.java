package com.github.dakusui.jcunit.experimentals.fsm;

public class Scenario<SUT> {
  public final State<SUT> given;
  public final Action<SUT> when;
  public final Args with;
  public final State<SUT> then;

  public Scenario(State<SUT> given, Action<SUT> when, Args with) {
    this.given = given;
    this.when = when;
    this.with = with;
    this.then = null; //given.expect(this.when, this.with).state;
  }
}
