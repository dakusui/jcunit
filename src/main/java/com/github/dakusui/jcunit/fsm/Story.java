package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.Checks;

import java.util.HashMap;
import java.util.Map;

public class Story<SUT> {
  public static final Reporter SILENT_REPORTER = new Reporter() {
    @Override
    public void startStory(ScenarioSequence.ContextType contextTypeType, ScenarioSequence seq) {
    }

    @Override
    public void run(ScenarioSequence.ContextType contextType, Scenario scenario, Object o) {
    }

    @Override
    public void passed(ScenarioSequence.ContextType contextType, Scenario scenario, Object o) {
    }

    @Override
    public void failed(ScenarioSequence.ContextType contextType, Scenario scenario, Object o) {
    }

    @Override
    public void endStory(ScenarioSequence.ContextType contextType, ScenarioSequence seq) {

    }
  };
  public static final Reporter SIMPLE_REPORTER = new Reporter() {
    @Override
    public void startStory(ScenarioSequence.ContextType contextTypeType, ScenarioSequence scenarioSequence) {
      System.out.printf("Starting:%s\n", scenarioSequence);
    }

    @Override
    public void run(ScenarioSequence.ContextType contextType, Scenario scenario, Object o) {
      System.out.printf("  Running:%s expecting %s\n", scenario, scenario.then());
    }

    @Override
    public void passed(ScenarioSequence.ContextType contextType, Scenario scenario, Object o) {
      System.out.println("  Passed");
    }

    @Override
    public void failed(ScenarioSequence.ContextType contextType, Scenario scenario, Object o) {
      System.out.println("  Failed");
    }

    @Override
    public void endStory(ScenarioSequence.ContextType contextType, ScenarioSequence seq) {
      System.out.println("End");
    }
  };

  public void perform() {
  }

  public interface Reporter<SUT> {
    void startStory(ScenarioSequence.ContextType contextTypeType, ScenarioSequence<SUT> seq);

    void run(ScenarioSequence.ContextType contextType, Scenario<SUT> scenario, SUT sut);

    void passed(ScenarioSequence.ContextType contextType, Scenario<SUT> scenario, SUT sut);

    void failed(ScenarioSequence.ContextType contextType, Scenario<SUT> scenario, SUT sut);

    void endStory(ScenarioSequence.ContextType contextType, ScenarioSequence<SUT> seq);
  }

  public static class Builder<SUT> {
    private final Map<String, ScenarioSequence<SUT>> mains  = new HashMap<String, ScenarioSequence<SUT>>();
    private       Map<String, ScenarioSequence<SUT>> setUps = new HashMap<String, ScenarioSequence<SUT>>();

    public Builder<SUT> add(String name, ScenarioSequence<SUT> main, ScenarioSequence<SUT> setUp) {
      Checks.checknotnull(name);
      Checks.checknotnull(main);
      Checks.checknotnull(setUp);
      this.mains.put(name, main);
      this.setUps.put(name, setUp);
      return this;
    }

    public Story build() {
      return new Story();
    }
  }
}
