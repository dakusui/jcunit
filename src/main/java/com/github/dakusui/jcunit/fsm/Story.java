package com.github.dakusui.jcunit.fsm;

public class Story<SUT> {
  public interface Observer {
    <SUT> void startSequence(ScenarioSequence.ContextType contextTypeType, ScenarioSequence<SUT> seq);

    <SUT> void run(ScenarioSequence.ContextType contextType, Scenario<SUT> scenario, SUT sut);

    <SUT> void passed(ScenarioSequence.ContextType contextType, Scenario<SUT> scenario, SUT sut);

    <SUT> void failed(ScenarioSequence.ContextType contextType, Scenario<SUT> scenario, SUT sut);

    <SUT> void endSequence(ScenarioSequence.ContextType contextType, ScenarioSequence<SUT> seq);

    <SUT> void skipSequence(ScenarioSequence.ContextType contextType, ScenarioSequence seq);
  }

  public static final Observer SILENT_OBSERVER = new Observer() {
    @Override
    public void startSequence(ScenarioSequence.ContextType contextTypeType, ScenarioSequence seq) {
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
    public void endSequence(ScenarioSequence.ContextType contextType, ScenarioSequence seq) {
    }

    @Override
    public void skipSequence(ScenarioSequence.ContextType contextType, ScenarioSequence seq) {
    }
  };

  public static final Observer SIMPLE_OBSERVER = new Observer() {
    @Override
    public void startSequence(ScenarioSequence.ContextType contextType, ScenarioSequence scenarioSequence) {
      System.out.printf("Starting(%s):%s\n", contextType, scenarioSequence);
    }

    @Override
    public void run(ScenarioSequence.ContextType contextType, Scenario scenario, Object o) {
      System.out.printf("  Running(%s):%s expecting %s\n", contextType, scenario, scenario.then());
    }

    @Override
    public void passed(ScenarioSequence.ContextType contextType, Scenario scenario, Object o) {
      System.out.printf("  Passed(%s)\n", contextType);
    }

    @Override
    public void failed(ScenarioSequence.ContextType contextType, Scenario scenario, Object o) {
      System.out.printf("  Failed(%s)\n", contextType);
    }

    @Override
    public void endSequence(ScenarioSequence.ContextType contextType, ScenarioSequence seq) {
      System.out.printf("End(%s)\n", contextType);
    }

    @Override
    public void skipSequence(ScenarioSequence.ContextType contextType, ScenarioSequence seq) {
      System.out.printf("Skip(%s)\n", contextType);
    }
  };

  private final ScenarioSequence<SUT> setUp;
  private final ScenarioSequence<SUT> main;

  public Story(ScenarioSequence<SUT> setUp, ScenarioSequence<SUT> main) {
    this.setUp = setUp;
    this.main = main;
  }

  public void perform(SUT sut, Observer observer) {
    this.setUp.perform(ScenarioSequence.ContextType.setUp, sut, observer);
    this.main.perform(ScenarioSequence.ContextType.main, sut, observer);
  }
}
