package com.github.dakusui.jcunit.fsm;

public class Story<SUT> {
  public static final Observer SILENT_OBSERVER = new Observer() {
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
  public static final Observer SIMPLE_OBSERVER = new Observer() {
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

  public interface Observer {
    <SUT> void startStory(ScenarioSequence.ContextType contextTypeType, ScenarioSequence<SUT> seq);

    <SUT> void run(ScenarioSequence.ContextType contextType, Scenario<SUT> scenario, SUT sut);

    <SUT> void passed(ScenarioSequence.ContextType contextType, Scenario<SUT> scenario, SUT sut);

    <SUT> void failed(ScenarioSequence.ContextType contextType, Scenario<SUT> scenario, SUT sut);

    <SUT> void endStory(ScenarioSequence.ContextType contextType, ScenarioSequence<SUT> seq);
  }

  private final ScenarioSequence<SUT> setUp;
  private final ScenarioSequence<SUT> main;

  public Story(ScenarioSequence<SUT> setUp, ScenarioSequence<SUT> main) {
    this.setUp = setUp;
    this.main = main;
  }

  public void perform(SUT sut) {
    this.setUp.perform(ScenarioSequence.ContextType.setUp, sut, null);
    this.main.perform(ScenarioSequence.ContextType.main, sut, null);
  }
}
