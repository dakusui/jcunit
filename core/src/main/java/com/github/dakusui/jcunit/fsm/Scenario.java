package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.utils.Checks;
import com.github.dakusui.jcunit.core.utils.StringUtils;

import java.io.Serializable;

public class Scenario<SUT> implements Serializable {
  public final State<SUT>  given;
  public final Action<SUT> when;
  public final Args        with;

  public Scenario(State<SUT> given, Action<SUT> when, Args with) {
    this.given = Checks.checknotnull(given);
    this.when = Checks.checknotnull(when);
    this.with = Checks.checknotnull(with);
  }

  public Object perform(SUT sut) throws Throwable {
    return when.perform(sut, this.with);
  }

  public Expectation<SUT> then() {
    return this.given.expectation(this.when, this.with);
  }

  @Override
  public String toString() {
    return String.format("%s#%s(%s)", this.given, this.when, StringUtils.join(",", this.with.values()));
  }
}
