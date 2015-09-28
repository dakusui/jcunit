package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.Utils;

import java.io.Serializable;

public class Scenario<SUT> implements Serializable {
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

  public <T> Object perform(T context, SUT sut) throws Throwable {
    return when.perform(context, sut, this.with);
  }

  public Expectation<SUT> then() {
    return this.given.expectation(this.when, this.with);
  }

  @Override
  public String toString() {
    return String.format("%s#%s(%s)", this.given, this.when, Utils.join(",", this.with.values()));
  }
}
