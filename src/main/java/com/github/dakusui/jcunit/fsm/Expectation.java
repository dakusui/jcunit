package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.fsm.spec.FSMSpec;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

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
   * Expected type. {@code VALUE_RETURNED} or {@code EXCEPTION_THROWN}.
   */
  private final Type                         type;
  /**
   * A checker which verifies a returned value or a thrown exception.
   */
  private final Checker                      checker;
  /**
   * A list of input history collectors.
   */
  public final  List<InputHistory.Collector> collectors;


  protected Expectation(
      String fsmName,
      Type type,
      State<SUT> state,
      Checker checker,
      List<InputHistory.Collector> collectors
  ) {
    Checks.checknotnull(type);
    Checks.checknotnull(state);
    Checks.checknotnull(checker);
    this.fsmName = fsmName;
    this.type = type;
    this.state = state;
    this.checker = checker;
    this.collectors = Collections.unmodifiableList(collectors);
  }

  public <T> Result checkThrownException(Story.Context<SUT, T> context, Throwable thrownException, ScenarioSequence.Observer observer) {
    Checks.checknotnull(context);
    //noinspection ThrowableResultOfMethodCallIgnored
    Checks.checknotnull(thrownException);
    Result.Builder b = new Result.Builder("Expectation was not satisfied");
    if (this.type == Type.VALUE_RETURNED) {
      b.addFailedReason(String.format(
              "Exception was not expected but %s was thrown. ",
              thrownException.getClass().getSimpleName()),
          thrownException);
    }
    if (!this.checker.check(context, thrownException, observer)) {
      b.addFailedReason(
          String.format(
              "Expected %s value/exception: %s but '%s' was thrown. (%s)",
              this.type,
              this.checker.format(),
              thrownException,
              thrownException.getMessage()),
          thrownException
      );
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
    if (this.type == Type.EXCEPTION_THROWN) {
      b.addFailedReason(Utils.format("Exception was expected to be thrown but it was not. "));
    }
    ////
    // Only when type is 'MAIN', returned FSM value will be checked.
    if (checker.shouldBeCheckedFor(stage)) {
      if (!this.checker.check(context, returnedValue, observer)) {
        b.addFailedReason(
            Utils.format(
                "Expected %s value/exception: %s but '%s' was returned and input history was as follows.: %s",
                this.type,
                this.checker.format(),
                returnedValue,
                context.inputHistory
            )
        );
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
    if (this.type == Type.EXCEPTION_THROWN)
      return Utils.format("status of '%s' is '%s' and %s is thrown", this.fsmName, this.state, this.checker.format());
    return Utils.format("status of '%s' is '%s' and %s is returned", this.fsmName, this.state, this.checker.format());
  }

  /**
   * Types of an expectation. Each element represents an expectation of its
   * relevant method.
   */
  public enum Type {
    /**
     * Expects that an exception is thrown of the method.
     */
    EXCEPTION_THROWN {
      @Override
      public String toString() {
        return "thrown";
      }
    },
    /**
     * Expects that a value is returned of the method.
     * No exception is thrown, in other words.
     */
    VALUE_RETURNED {
      @Override
      public String toString() {
        return "returned";
      }
    }
  }

  public static class Builder<SUT> extends InputHistory.CollectorHolder<Builder<SUT>> {
    private final FSM<SUT>   fsm;
    private final String     fsmName;
    private       Type       type;
    private       Checker    checker;
    private       State<SUT> state;

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
      this.type = Type.EXCEPTION_THROWN;
      this.state = chooseState(state);
      this.checker = new Checker.MatcherBased(CoreMatchers.instanceOf(klass));
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
      return valid(state, new Expectation.Checker.MatcherBased(matcher));
    }

    public Builder<SUT> valid(FSMSpec<SUT> state, Expectation.Checker checker) {
      Checks.checknotnull(state);
      Checks.checknotnull(checker);
      this.type = Type.VALUE_RETURNED;
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

  /**
   * An interface that models checking process for a returned value/thrown exception.
   */
  public interface Checker {
    /**
     * Checks if this object should be performed for a given scenario type.
     */
    boolean shouldBeCheckedFor(Story.Stage stage);

    /**
     * Checks the {@code item} matches the criterion that this object defines.
     * {@code true} will be returned if it does, {@code false} otherwise.
     *
     * @param context  A context in which this check is performed.
     * @param value    A value to be checked. (Returned object or thrown exception by a method)
     * @param observer An observer to which the checking result will be reported.
     */
    <SUT, T> boolean check(Story.Context<SUT, T> context, Object value, ScenarioSequence.Observer observer);

    /**
     * Formats this object to a human readable string.
     */
    String format();

    abstract class Base implements Checker {
      @Override
      public boolean shouldBeCheckedFor(Story.Stage stage) {
        return true;
      }
    }

    abstract class Simple extends Checker.Base {
      @Override
      public <SUT, T> boolean check(Story.Context<SUT, T> context, Object value, ScenarioSequence.Observer observer) {
        return check(context.inputHistory, value);
      }

      protected abstract boolean check(InputHistory inputHistory, Object value);

      @Override
      public String format() {
        return String.format(
            "%s@%s",
            this.getClass().getSimpleName(),
            System.identityHashCode(this)
        );
      }
    }

    class MatcherBased extends Base implements Checker {
      private final Matcher matcher;

      public MatcherBased(Matcher matcher) {
        this.matcher = Checks.checknotnull(matcher);
      }

      @Override
      public <SUT, T> boolean check(Story.Context<SUT, T> context, Object value, ScenarioSequence.Observer observer) {
        return this.matcher.matches(value);
      }

      @Override
      public String format() {
        return this.matcher.toString();
      }
    }

    class FSM extends Base implements Checker {
      String fsmName;

      public FSM(String fsmName) {
        Checks.checknotnull(fsmName);
        this.fsmName = fsmName;
      }

      @Override
      public <SUT, T> boolean check(Story.Context<SUT, T> context, Object value, ScenarioSequence.Observer observer) {
        Checks.checknotnull(context);
        Story story = context.lookUpFSMStory(this.fsmName);
        if (!Checks.checknotnull(story).isPerformed()) {
          //noinspection unchecked
          Story.Performer.Default.INSTANCE.perform(
              story,
              context.testObject,
              new SUTFactory.Dummy(value),
              FSMUtils.Synchronizer.DUMMY, observer.createChild(this.fsmName)
          );
        }
        return true;
      }

      @Override
      public boolean shouldBeCheckedFor(Story.Stage stage) {
        return stage == Story.Stage.MAIN;
      }

      @Override
      public String format() {
        return String.format("FSM:%s", fsmName);
      }
    }

  }

}
