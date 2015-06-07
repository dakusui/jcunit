package com.github.dakusui.jcunit.fsm;

public class Story<SUT> {
  public static final Reporter SILENT_REPORTER = new Reporter() {
    @Override
    public void startStory(ContextType contextTypeType, ScenarioSequence seq) {
    }

    @Override
    public void run(ContextType contextType, Scenario scenario, Object o) {
    }

    @Override
    public void passed(ContextType contextType, Scenario scenario, Object o) {
    }

    @Override
    public void failed(ContextType contextType, Scenario scenario, Object o) {
    }

    @Override
    public void endStory(ContextType contextType, ScenarioSequence seq) {

    }
  };
  public static final Reporter SIMPLE_REPORTER = new Reporter() {
    @Override
    public void startStory(ContextType contextTypeType, ScenarioSequence scenarioSequence) {
      System.out.printf("Starting:%s\n", scenarioSequence);
    }

    @Override
    public void run(ContextType contextType, Scenario scenario, Object o) {
      System.out.printf("  Running:%s expecting %s\n", scenario, scenario.then());
    }

    @Override
    public void passed(ContextType contextType, Scenario scenario, Object o) {
      System.out.println("  Passed");
    }

    @Override
    public void failed(ContextType contextType, Scenario scenario, Object o) {
      System.out.println("  Failed");
    }

    @Override
    public void endStory(ContextType contextType, ScenarioSequence seq) {
      System.out.println("End");
    }
  };
  public final ScenarioSequence<SUT> setUp;
  public final ScenarioSequence<SUT> main;

  public Story(ScenarioSequence<SUT> setUp, ScenarioSequence<SUT> main) {
    this.setUp = setUp;
    this.main = main;
  }

  public void perform() {

  }

  public enum ContextType {
    setUp,
    main,
    optional
  }
  public interface Reporter<SUT> {
    void startStory(ContextType contextTypeType, ScenarioSequence<SUT> seq);

    void run(ContextType contextType, Scenario<SUT> scenario, SUT sut);

    void passed(ContextType contextType, Scenario<SUT> scenario, SUT sut);

    void failed(ContextType contextType, Scenario<SUT> scenario, SUT sut);

    void endStory(ContextType contextType, ScenarioSequence<SUT> seq);
  }

  public static class Builder<SUT> {
    private ScenarioSequence<SUT> main;
    private ScenarioSequence<SUT> setUp;

    public Builder<SUT> main(ScenarioSequence<SUT> main) {
      this.main = main;
      return this;
    }

    public Builder<SUT> setUp(ScenarioSequence<SUT> setUp) {
      this.setUp = setUp;
      return this;
    }

    public Story build() {
      return new Story(main, setUp);
    }
  }
}
