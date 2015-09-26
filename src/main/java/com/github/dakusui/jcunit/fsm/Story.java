package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.fsm.spec.FSMSpec;

import java.io.PrintStream;

public class Story<S extends FSMSpec<SUT>, SUT extends Object> {
  private final String  name;
  private       boolean performed;

  private final ScenarioSequence<SUT> setUp;
  private final ScenarioSequence<SUT> main;

  public Story(String name, ScenarioSequence<SUT> setUp, ScenarioSequence<SUT> main) {
    Checks.checknotnull(name);
    Checks.checknotnull(setUp);
    Checks.checknotnull(main);
    this.name = name;
    this.setUp = setUp;
    this.main = main;
  }

  public <T> void perform(T context, SUT sut, ScenarioSequence.Observer observer) {
    this.performed = true;
    this.setUp.perform(context, name, ScenarioSequence.Type.setUp, sut, observer);
    this.main.perform(context, name, ScenarioSequence.Type.main, sut, observer);
  }

  public boolean isPerformed() {
    return this.performed;
  }
}
