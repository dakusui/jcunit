package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.fsm.spec.FSMSpec;

import java.io.PrintStream;

public class Story<S extends FSMSpec<SUT>, SUT extends Object> {
  private final String  name;
  private       boolean performed;

  public interface Observer {
    Observer SILENT = new Observer() {
      @Override
      public Observer createChild(String childName) {
        return this;
      }

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
      public void failed(ScenarioSequence.Type type, Scenario scenario, Object o, Expectation.Result result) {
      }

      @Override
      public void endSequence(ScenarioSequence.Type type, ScenarioSequence seq) {
      }

      @Override
      public void skipSequence(ScenarioSequence.Type type, ScenarioSequence seq) {
      }
    };

    <SUT> void startSequence(ScenarioSequence.Type type, ScenarioSequence<SUT> seq);

    <SUT> void run(ScenarioSequence.Type type, Scenario<SUT> scenario, SUT sut);

    <SUT> void passed(ScenarioSequence.Type type, Scenario<SUT> scenario, SUT sut);

    <SUT> void failed(ScenarioSequence.Type type, Scenario<SUT> scenario, SUT sut, Expectation.Result result);

    <SUT> void endSequence(ScenarioSequence.Type type, ScenarioSequence<SUT> seq);

    <SUT> void skipSequence(ScenarioSequence.Type type, ScenarioSequence<SUT> seq);

    Observer createChild(String childName);

    interface Factory {
      Observer createObserver(String fsmName);
      class ForSilent implements Factory {
        @Override
        public Observer createObserver(String fsmName) {
          return SILENT;
        }
      }

      class ForSimple implements Factory {
        @Override
        public Observer createObserver(String fsmName) {
          return createSimpleObserver(fsmName);
        }
      }
    }
  }

  public static final Observer createSimpleObserver(String fsmName) {
    return createSimpleObserver(fsmName, System.out);
  }

  public static final Observer createSimpleObserver(String fsmName, final PrintStream ps) {
    return createSimpleObserver(fsmName, System.out, 0);
  }

  private static final Observer createSimpleObserver(final String fsmName, final PrintStream ps, final int generation) {
    Checks.checknotnull(ps);
    return new Observer() {
      private String indent(int level) {
        return new String(new char[2 * level]).replace("\0", " ");
      }

      @Override
      public Observer createChild(String childName) {
        return createSimpleObserver(childName, ps, generation + 1);
      }

      @Override
      public void startSequence(ScenarioSequence.Type type, ScenarioSequence scenarioSequence) {
        ps.printf("%sStarting(%s#%s):%s\n", indent(generation), fsmName, type, scenarioSequence);
      }

      @Override
      public void run(ScenarioSequence.Type type, Scenario scenario, Object o) {
        ps.printf("%sRunning(%s#%s):%s expecting %s\n", indent(generation + 1), fsmName, type, scenario, scenario.then());
      }

      @Override
      public void passed(ScenarioSequence.Type type, Scenario scenario, Object o) {
        ps.printf("%sPassed(%s#%s)\n", indent(generation + 1), fsmName, type);
      }

      @Override
      public void failed(ScenarioSequence.Type type, Scenario scenario, Object o, Expectation.Result result) {
        ps.printf("%sFailed(%s#%s): %s\n", indent(generation + 1), fsmName, type, result.getMessage());
      }

      @Override
      public void endSequence(ScenarioSequence.Type type, ScenarioSequence seq) {
        ps.printf("%sEnd(%s#%s)\n", indent(generation), fsmName, type);
      }

      @Override
      public void skipSequence(ScenarioSequence.Type type, ScenarioSequence seq) {
        ps.printf("%sSkip(%s#%s)\n", indent(generation), fsmName, type);
      }
    };
  }

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

  public <T> void perform(T context, SUT sut, Observer observer) {
    this.performed = true;
    this.setUp.perform(context, name, ScenarioSequence.Type.setUp, sut, observer);
    this.main.perform(context, name, ScenarioSequence.Type.main, sut, observer);
  }

  public boolean isPerformed() {
    return this.performed;
  }
}
