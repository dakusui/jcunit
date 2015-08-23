package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.exceptions.NestableException;
import org.hamcrest.Matcher;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Expectation<SUT> {
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
      public boolean check(FSMContext context, Object item) {
        return this.matcher.matches(item);
      }

      @Override
      public String format() {
        return this.matcher.toString();
      }
    }

    class FSM implements Checker {
      private final Story.Observer observer;
      String fsmName;

      public FSM(String fsmName, Story.Observer observer) {
        Checks.checknotnull(observer);
        this.fsmName = fsmName;
        this.observer = observer;
      }

      @Override
      public boolean check(FSMContext context, Object item) {
        Checks.checknotnull(context);
        Checks.checkcond(context.hasStory(fsmName));
        if (!context.isAlreadyPerformed(fsmName)) {
          context.lookupStory(fsmName).perform(context, item, this.observer);
        }
        return true;
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
     * @param context A context on which this check is performed.
     * @param item An item to be checked.
     */
    boolean check(FSMContext context, Object item);

    String format();
  }


  /**
   * Expected state.
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

  public Expectation(
      Type type,
      State<SUT> state,
      Matcher matcher) {
    this(type, state, new Checker.MatcherBased(matcher));
  }

  public Expectation(
      Type type,
      State<SUT> state,
      Checker checker) {
    Checks.checknotnull(type);
    Checks.checknotnull(state);
    Checks.checknotnull(checker);
    this.type = type;
    this.state = state;
    this.checker = checker;
  }

  public Result checkThrownException(FSMContext context, SUT sut, Throwable thrownException) {
    Checks.checknotnull(sut);
    //noinspection ThrowableResultOfMethodCallIgnored
    Checks.checknotnull(thrownException);
    Result.Builder b = new Result.Builder("Expectation was not satisfied");
    if (this.type != Type.EXCEPTION_THROWN) {
      b.addFailedReason(String.format("Exception was expected to be thrown but not. (%s)", this.checker.format()));
    }
    if (!this.checker.check(context, thrownException)) {
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

  public Result checkReturnedValue(FSMContext context, SUT sut, Object returnedValue) {
    Checks.checknotnull(sut);
    Result.Builder b = new Result.Builder(String.format("Expectation: [%s] was not satisfied", this));
    if (this.type != Type.VALUE_RETURNED) {
      b.addFailedReason(String.format("Exception was expected not to be thrown but it was. (%s)", this.checker.format()));
    }
    if (!this.checker.check(context, returnedValue)) {
      b.addFailedReason(
          String.format("'%s' is expected to be %s but '%s' was thrown.", this.checker.format(), this.type, returnedValue)
      );
    }
    if (!this.state.check(sut)) {
      b.addFailedReason(
          String.format("'%s' is expected to be in '%s' state but not.", sut, this.state)
      );
    }
    return b.build();
  }

  @Override
  public String toString() {
    if (this.type == Type.EXCEPTION_THROWN)
      return String.format("Status is '%s' and %s is thrown", this.state, this.checker.format());
    return String.format("Status is '%s' and %s is returned", this.state, this.checker.format());
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
  }


  public static class Result extends NestableException {
    private final List<Reason> failedReasons;

    public Result(String message, List<Reason> failedReasons) {
      super(message);
      this.failedReasons = Collections.unmodifiableList(failedReasons);
      for (Reason each : this.getFailedReasons()) {
        if (each.t != null)
          this.addChild(each.t);
      }
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
        ret += ":[";
        boolean isFirst = true;
        for (Reason each : this.failedReasons) {
          if (!isFirst) {
            ret += ",";
          }
          ret += each.message;
          isFirst = false;
        }
        ret += "]";
      }
      return ret;
    }

    public List<Reason> getFailedReasons() {
      return this.failedReasons;
    }

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
  }
}
