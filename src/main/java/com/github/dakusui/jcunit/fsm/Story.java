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

  public void perform(FSMContext fsmContext) {
  }

  private void performScenarioSequence(ScenarioSequence scenarioSequence, Object sut) {
    Checks.checknotnull(scenarioSequence);
    Reporter reporter = SILENT_REPORTER;
    ScenarioSequence.ContextType contextType = ScenarioSequence.ContextType.main;
    Checks.checknotnull(reporter);
    reporter.startStory(contextType, scenarioSequence);
    try {
      for (int i = 0; i < scenarioSequence.size(); i++) {
        Scenario<SUT> each = scenarioSequence.get(i);

        performScenario((SUT) sut, reporter, contextType, each);
      }
    } finally {
      reporter.endStory(contextType, scenarioSequence);
    }
  }

  private void performScenario(SUT sut, Reporter reporter, ScenarioSequence.ContextType contextType, Scenario<SUT> each) {
    Expectation.Result result = null;
    reporter.run(contextType, each, sut);
    FSMContext context = null;
    try {
      Object r = each.perform(sut);
      ////
      // each.perform(sut) didn't throw an exception
      //noinspection unchecked,ThrowableResultOfMethodCallIgnored
      result = each.then().checkReturnedValue(context, sut, r);
    } catch (Throwable t) {
      //noinspection unchecked,ThrowableResultOfMethodCallIgnored
      result = each.then().checkThrownException(context, sut, t);
    } finally {
      if (result != null) {
        if (result.isSuccessful())
          reporter.passed(contextType, each, sut);
        else
          reporter.failed(contextType, each, sut);
        result.throwIfFailed();
      }
    }
  }

  public interface Reporter {
    <SUT> void startStory(ScenarioSequence.ContextType contextTypeType, ScenarioSequence<SUT> seq);

    <SUT> void run(ScenarioSequence.ContextType contextType, Scenario<SUT> scenario, SUT sut);

    <SUT> void passed(ScenarioSequence.ContextType contextType, Scenario<SUT> scenario, SUT sut);

    <SUT> void failed(ScenarioSequence.ContextType contextType, Scenario<SUT> scenario, SUT sut);

    <SUT> void endStory(ScenarioSequence.ContextType contextType, ScenarioSequence<SUT> seq);
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
