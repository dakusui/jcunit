package com.github.dakusui.jcunit.fsm;

public class Story<SUT> {
  public interface Observer {
    <SUT> void startSequence(ScenarioSequence.Type type, ScenarioSequence<SUT> seq);

    <SUT> void run(ScenarioSequence.Type type, Scenario<SUT> scenario, SUT sut);

    <SUT> void passed(ScenarioSequence.Type type, Scenario<SUT> scenario, SUT sut);

    <SUT> void failed(ScenarioSequence.Type type, Scenario<SUT> scenario, SUT sut);

    <SUT> void endSequence(ScenarioSequence.Type type, ScenarioSequence<SUT> seq);

    <SUT> void skipSequence(ScenarioSequence.Type type, ScenarioSequence seq);
  }

  public static final Observer SILENT_OBSERVER = new Observer() {
    @Override
    public void startSequence(ScenarioSequence.Type type, ScenarioSequence seq) {
    }

    @Override
    public void run(ScenarioSequence.Type type, Scenario scenario, Object o) {
    }

    @Override
    public void passed(ScenarioSequence.Type type, Scenario scenario, Object o) {
    }

    @Override
    public void failed(ScenarioSequence.Type type, Scenario scenario, Object o) {
    }

    @Override
    public void endSequence(ScenarioSequence.Type type, ScenarioSequence seq) {
    }

    @Override
    public void skipSequence(ScenarioSequence.Type type, ScenarioSequence seq) {
    }
  };

  public static final Observer SIMPLE_OBSERVER = new Observer() {
    @Override
    public void startSequence(ScenarioSequence.Type type, ScenarioSequence scenarioSequence) {
      System.out.printf("Starting(%s):%s\n", type, scenarioSequence);
    }

    @Override
    public void run(ScenarioSequence.Type type, Scenario scenario, Object o) {
      System.out.printf("  Running(%s):%s expecting %s\n", type, scenario, scenario.then());
    }

    @Override
    public void passed(ScenarioSequence.Type type, Scenario scenario, Object o) {
      System.out.printf("  Passed(%s)\n", type);
    }

    @Override
    public void failed(ScenarioSequence.Type type, Scenario scenario, Object o) {
      System.out.printf("  Failed(%s)\n", type);
    }

    @Override
    public void endSequence(ScenarioSequence.Type type, ScenarioSequence seq) {
      System.out.printf("End(%s)\n", type);
    }

    @Override
    public void skipSequence(ScenarioSequence.Type type, ScenarioSequence seq) {
      System.out.printf("Skip(%s)\n", type);
    }
  };

  private final ScenarioSequence<SUT> setUp;
  private final ScenarioSequence<SUT> main;

  public Story(ScenarioSequence<SUT> setUp, ScenarioSequence<SUT> main) {
    this.setUp = setUp;
    this.main = main;
  }

  public void perform(FSMContext context, SUT sut, Observer observer) {
    this.setUp.perform(context, ScenarioSequence.Type.setUp, sut, observer);
    this.main.perform(context, ScenarioSequence.Type.main, sut, observer);
  }
}
