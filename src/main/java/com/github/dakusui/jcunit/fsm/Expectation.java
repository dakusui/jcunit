package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.fsm.spec.FSMSpec;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.*;

/**
 * This class represents what a model of SUT expects for FSM.
 *
 * @param <SUT> A type of SUT.
 */
public class Expectation<SUT> {
  /**
   * A name of FSM from which this object is derived.
   */
  private final String fsmName;

  /**
   * Expected state after an action is performed.
   */
  public final  State<SUT>                   state;
  /**
   * A checker which verifies a returned value or a thrown exception.
   */
  private final OutputChecker                checker;
  /**
   * A list of input history collectors.
   */
  public final  List<InputHistory.Collector> collectors;


  protected Expectation(
      String fsmName,
      Output.Type type,
      State<SUT> state,
      OutputChecker checker,
      List<InputHistory.Collector> collectors
  ) {
    Checks.checknotnull(type);
    Checks.checknotnull(state);
    Checks.checknotnull(checker);
    this.fsmName = fsmName;
    this.state = state;
    this.checker = checker;
    this.collectors = Collections.unmodifiableList(collectors);
  }

  public <T> Result checkThrownException(Story.Context<SUT, T> context, Throwable thrownException, ScenarioSequence.Observer observer) {
    Checks.checknotnull(context);
    //noinspection ThrowableResultOfMethodCallIgnored
    Checks.checknotnull(thrownException);
    Result.Builder b = new Result.Builder("Expectation was not satisfied");
    OutputChecker.Result r = this.checker.check(
        context,
        new Output(Output.Type.EXCEPTION_THROWN, thrownException),
        observer);
    if (!r.isSuccessful()) {
      b.addFailedReason(r.getDescription());
    }
    if (!this.state.check(context.sut)) {
      b.addFailedReason(
          Utils.format("FSM '%s' is expected to be in '%s' state but not.(fsm='%s')", this.fsmName, this.state, context.sut)
      );
    }
    return b.build();
  }

  public <T> Result checkReturnedValue(Story.Context<SUT, T> context, Object returnedValue, Story.Stage stage, ScenarioSequence.Observer observer) {
    Checks.checknotnull(context);
    Result.Builder b = new Result.Builder("Expectation was not satisfied");
    ////
    // Only when type is 'MAIN', returned FSM value will be checked.
    if (checker.shouldBeCheckedFor(stage)) {
      OutputChecker.Result r = this.checker.check(
          context,
          new Output(Output.Type.VALUE_RETURNED, returnedValue),
          observer);
      if (!r.isSuccessful()) {
        b.addFailedReason(Utils.format(r.getDescription()));
      }
    }
    if (!this.state.check(context.sut)) {
      b.addFailedReason(
          Utils.format("FSM '%s' is expected to be in '%s' state but not.(fsm='%s')", this.fsmName, this.state, context.sut)
      );
    }
    return b.build();
  }

  @Override
  public String toString() {
    return Utils.format("state of '%s' is '%s' and %s %s %s", this.fsmName, this.state, this.checker.getType().name, this.checker.getType().entityType(), this.checker.toString());
  }

  public static class Builder<SUT> extends InputHistory.CollectorHolder<Builder<SUT>> {
    private final FSM<SUT>      fsm;
    private final String        fsmName;
    private       Output.Type   type;
    private       OutputChecker checker;
    private       State<SUT>    state;

    Builder(
        String fsmName,
        FSM<SUT> fsm
    ) {
      this.fsm = fsm;
      this.fsmName = fsmName;
    }

    public Builder<SUT> invalid() {
      return this.invalid(IllegalArgumentException.class);
    }

    public Builder<SUT> invalid(Class<? extends Throwable> klass) {
      Checks.checknotnull(klass);
      //noinspection unchecked
      return this.invalid((FSMSpec<SUT>) FSMSpec.VOID, klass);
    }

    public Builder<SUT> invalid(FSMSpec<SUT> state, Class<? extends Throwable> klass) {
      Checks.checknotnull(state);
      this.type = Output.Type.EXCEPTION_THROWN;
      this.state = chooseState(state);
      this.checker = new OutputChecker.MatcherBased(Output.Type.EXCEPTION_THROWN, CoreMatchers.instanceOf(klass));
      return this;
    }

    public Builder<SUT> valid(FSMSpec<SUT> state) {
      return valid(state, CoreMatchers.anything());
    }

    public Builder<SUT> valid(FSMSpec<SUT> state, Object returnedValue) {
      return valid(state, CoreMatchers.is(returnedValue));
    }

    public Builder<SUT> valid(FSMSpec<SUT> state, Matcher matcher) {
      Checks.checknotnull(matcher);
      return valid(state, new OutputChecker.MatcherBased(Output.Type.VALUE_RETURNED, matcher));
    }

    public Builder<SUT> valid(FSMSpec<SUT> state, OutputChecker checker) {
      Checks.checknotnull(state);
      Checks.checknotnull(checker);
      this.type = Output.Type.VALUE_RETURNED;
      this.state = chooseState(state);
      this.checker = checker;
      return this;
    }

    private State<SUT> chooseState(StateChecker<SUT> stateChecker) {
      Checks.checknotnull(fsm);
      Checks.checknotnull(stateChecker);
      if (stateChecker == FSMSpec.VOID) {
        //noinspection unchecked
        return (State<SUT>) State.VOID;
      }
      for (State<SUT> each : fsm.states()) {
        if (((State.Base) each).stateSpec == stateChecker)
          return each;
      }
      Checks.checkcond(false, "No state for '%s' was found.", stateChecker);
      return null;
    }

    public Expectation<SUT> build() {
      return new Expectation<SUT>(
          this.fsmName,
          this.type,
          this.state,
          this.checker,
          this.collectors
      );
    }
  }

  /**
   * A class that represents a result of verification.
   */
  public static class Result extends AssertionError {
    private final List<FailedReason> failedFailedReasons;

    public Result(String message, List<FailedReason> failedFailedReasons) {
      super(message);
      this.failedFailedReasons = Collections.unmodifiableList(failedFailedReasons);
    }

    public boolean isSuccessful() {
      return this.failedFailedReasons.isEmpty();
    }

    public void throwIfFailed() {
      if (!this.isSuccessful()) {
        this.fillInStackTrace();
        throw this;
      }
    }

    @Override
    public String getMessage() {
      String ret = super.getMessage();
      if (!failedFailedReasons.isEmpty()) {
        ret += String.format(": [%s]", Utils.join(",", this.failedFailedReasons.toArray()));
      }
      return ret;
    }

    @Override
    public void printStackTrace(PrintStream ps) {
      Checks.checknotnull(ps);
      for (FailedReason each : this.failedFailedReasons) {
        ps.println(each.message);
        if (each.t != null) {
          each.t.printStackTrace(ps);
        }
      }
    }

    @Override
    public void printStackTrace(PrintWriter pw) {
      Checks.checknotnull(pw);
      for (FailedReason each : this.failedFailedReasons) {
        pw.println(each.message);
        if (each.t != null) {
          each.t.printStackTrace(pw);
        }
      }
    }

    static class Builder {
      private List<FailedReason> failures = new LinkedList<FailedReason>();
      private String message;

      Builder(String message) {
        this.message = message;
      }

      Builder addFailedReason(String message) {
        Checks.checknotnull(message);
        return this.addFailedReason(message, null);
      }

      Builder addFailedReason(String message, Throwable t) {
        this.failures.add(new FailedReason(message, t));
        return this;
      }

      Result build() {
        return new Result(message, failures);
      }
    }

    static class FailedReason {
      private final String    message;
      private final Throwable t;

      FailedReason(String message, Throwable t) {
        this.message = message;
        this.t = t;
      }

      public String toString() {
        return this.message;
      }
    }
  }

}
