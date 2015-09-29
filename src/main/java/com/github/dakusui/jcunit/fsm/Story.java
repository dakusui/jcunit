package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.fsm.spec.FSMSpec;

import java.io.Serializable;

public class Story<S extends FSMSpec<SUT>, SUT extends Object> implements Serializable {
  private final     String  name;
  transient private boolean performed;

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

  public int hashCode() {
    return this.name.hashCode();
  }

  @Override
  public boolean equals(Object another) {
    if (another instanceof Story) {
      Story anotherStory = ((Story)another);
      return this.name.equals(anotherStory.name) && this.setUp.equals(anotherStory.setUp) && this.main.equals(anotherStory.main);
    }
    return false;
  }

}
