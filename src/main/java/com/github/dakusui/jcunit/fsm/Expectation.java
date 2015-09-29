package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.fsm.spec.FSMSpec;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Expectation<SUT> {
  public static class Builder<SUT> {
    private final FSM<SUT>   fsm;
    private final String     fsmName;
    private       Type       type;
    private       Checker    checker;
    private       State<SUT> state;

    Builder(String fsmName, FSM<SUT> fsm) {
      this.fsm = fsm;
      this.fsmName = fsmName;
    }

    public Builder<SUT> invalid() {
      return this.invalid(IllegalArgumentException.class);
    }

    public Builder<SUT> invalid(Class<? extends Throwable> klass) {
      Checks.checknotnull(klass);
      //noinspection unchecked
      return this.invalid((FSMSpec<SUT>)FSMSpec.VOID, klass);
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
        if (((SimpleFSM.SimpleFSMState) each).stateSpec == stateChecker)
          return each;
      }
      Checks.checkcond(false, "No state for '%s' was found.", stateChecker);
      return null;
    }

    public Expectation<SUT> build() {
      return new Expectation<SUT>(this.fsmName, this.type, this.state, this.checker);
    }
  }

  /**
   * A name of FSM from which this object is derived.
   */
  private final String fsmName;

  /**
   * Expected state after an action is performed.
   */
  public final  State<SUT> state;
  /**
   * Expected type. {@code VALUE_RETURNED} or {@code EXCEPTION_THROWN}.
   */
  private final Type       type;
  /**
   * A checker which verifies a returned value or a thrown exception.
   */
  private final Checker    checker;

  protected Expectation(
      String fsmName,
      Type type,
      State<SUT> state,
      Checker checker) {
    Checks.checknotnull(type);
    Checks.checknotnull(state);
    Checks.checknotnull(checker);
    this.fsmName = fsmName;
    this.type = type;
    this.state = state;
    this.checker = checker;
  }

  public <T> Result checkThrownException(T context, SUT sut, Throwable thrownException, ScenarioSequence.Observer observer) {
    Checks.checknotnull(sut);
    //noinspection ThrowableResultOfMethodCallIgnored
    Checks.checknotnull(thrownException);
    Result.Builder b = new Result.Builder("Expectation was not satisfied");
    if (this.type != Type.EXCEPTION_THROWN) {
      b.addFailedReason(String.format(
          "Exception was not expected to be thrown but %s was thrown. (%s)",
          thrownException.getClass().getSimpleName(),
          this.checker.format()),
          thrownException);
    }
    if (!this.checker.check(context, thrownException, observer)) {
      b.addFailedReason(
          String.format("'%s' is expected to be %s but '%s' was thrown. (%s)", this.checker.format(), this.type, thrownException, thrownException.getMessage()),
          thrownException
      );
    }
    if (!this.state.check(sut)) {
      b.addFailedReason(
          String.format("'%s' is expected to be in '%s' state but not.", sut, this.state)
      );
    }
    return b.build();
  }

  public <T> Result checkReturnedValue(T context, SUT sut, Object returnedValue, ScenarioSequence.Observer observer) {
    Checks.checknotnull(sut);
    Result.Builder b = new Result.Builder("Expectation was not satisfied");
    if (this.type != Type.VALUE_RETURNED) {
      b.addFailedReason(String.format("Exception was expected not to be thrown but it was. (%s)", this.checker.format()));
    }
    if (!this.checker.check(context, returnedValue, observer)) {
      b.addFailedReason(
          String.format("'%s' is expected to be %s but '%s' was returned.", this.checker.format(), this.type, returnedValue)
      );
    }
    if (!this.state.check(sut)) {
      b.addFailedReason(
          String.format("FSM '%s' is expected to be in '%s' state but not.(actual='%s')", this.fsmName, this.state, sut)
      );
    }
    return b.build();
  }

  @Override
  public String toString() {
    if (this.type == Type.EXCEPTION_THROWN)
      return String.format("status of '%s' is '%s' and %s is thrown", this.fsmName, this.state, this.checker.format());
    return String.format("status of '%s' is '%s' and %s is returned", this.fsmName, this.state, this.checker.format());
  }

  public enum Type {
    EXCEPTION_THROWN {
      @Override
      public String toString() {
        return "thrown";
      }
    },
    VALUE_RETURNED {
      @Override
      public String toString() {
        return "returned";
      }
    }
  }

  static class Reason {
    private final String    message;
    private final Throwable t;

    Reason(String message, Throwable t) {
      this.message = message;
      this.t = t;
    }

    public String toString() {
      return this.message;
    }
  }


  public static class Result extends AssertionError {
    static class Builder {
      private List<Reason> failures = new LinkedList<Reason>();
      private String message;

      Builder(String message) {
        this.message = message;
      }

      Builder addFailedReason(String message) {
        Checks.checknotnull(message);
        return this.addFailedReason(message, null);
      }

      Builder addFailedReason(String message, Throwable t) {
        this.failures.add(new Reason(message, t));
        return this;
      }

      Result build() {
        return new Result(message, failures);
      }
    }

    private final List<Reason> failedReasons;

    public Result(String message, List<Reason> failedReasons) {
      super(message);
      this.failedReasons = Collections.unmodifiableList(failedReasons);
    }

    public boolean isSuccessful() {
      return this.failedReasons.isEmpty();
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
      if (!failedReasons.isEmpty()) {
        ret += String.format(": [%s]", Utils.join(",", this.failedReasons.toArray()));
      }
      return ret;
    }

    @Override
    public void printStackTrace(PrintStream ps) {
      Checks.checknotnull(ps);
      for (Reason each : this.failedReasons) {
        ps.println(each.message);
        if (each.t != null) {
          each.t.printStackTrace(ps);
        }
      }
    }

    @Override
    public void printStackTrace(PrintWriter pw) {
      Checks.checknotnull(pw);
      for (Reason each : this.failedReasons) {
        pw.println(each.message);
        if (each.t != null) {
          each.t.printStackTrace(pw);
        }
      }
    }
  }

  /**
   * An interface that models checking process for a returned value/thrown exception.
   */
  public interface Checker {
    class MatcherBased implements Checker {
      private final Matcher matcher;

      public MatcherBased(Matcher matcher) {
        this.matcher = Checks.checknotnull(matcher);
      }

      @Override
      public <T> boolean check(T context, Object item, ScenarioSequence.Observer observer) {
        return this.matcher.matches(item);
      }

      @Override
      public String format() {
        return this.matcher.toString();
      }
    }

    class FSM implements Checker {
      String fsmName;

      public FSM(String fsmName) {
        Checks.checknotnull(fsmName);
        this.fsmName = fsmName;
      }

      @Override
      public <T> boolean check(T context, Object item, ScenarioSequence.Observer observer) {
        Checks.checknotnull(context);
        Story story = lookupStory(context, this.fsmName);
        if (!Checks.checknotnull(story).isPerformed()) {
          story.perform(context, item, observer.createChild(this.fsmName));
        }
        return true;
      }

      private static Story<?, ?> lookupStory(Object context, String fsmName) {
        Checks.checknotnull(context);
        Checks.checknotnull(fsmName);
        Field f;
        try {
          f = context.getClass().getField(fsmName);
          return (Story<?, ?>) Checks.checknotnull(f).get(context);
        } catch (NoSuchFieldException e) {
          Checks.rethrow(e);
        } catch (IllegalAccessException e) {
          Checks.rethrow(e);
        }
        Checks.checkcond(false, "This path shouldn't be executed.");
        assert false;
        return null;
      }

      @Override
      public String format() {
        return String.format("FSM:%s", fsmName);
      }
    }

    /**
     * Checks the {@code item} matches the criterion that this object defines.
     * {@code true} will be returned if it does, {@code false} otherwise.
     *
     * @param context  A context on which this check is performed.
     * @param item     An item to be checked.
     * @param observer An observer to which the checking result will be reported.
     */
    <T> boolean check(T context, Object item, ScenarioSequence.Observer observer);

    String format();
  }
}
