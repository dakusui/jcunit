package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.utils.Checks;
import com.github.dakusui.jcunit.fsm.spec.FsmSpec;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;

import static java.lang.String.format;

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
  public final  State<SUT>    state;

  /**
   * A checker which verifies a returned value or a thrown exception.
   */
  private final OutputChecker checker;

  protected Expectation(
      String fsmName,
      OutputType type,
      State<SUT> state,
      OutputChecker checker
  ) {
    Checks.checknotnull(type);
    Checks.checknotnull(state);
    Checks.checknotnull(checker);
    this.fsmName = fsmName;
    this.state = state;
    this.checker = checker;
  }

  public OutputType getType() {
    return this.checker.getType();
  }

  @Override
  public String toString() {
    return format("state of '%s' is '%s' and %s %s %s", this.fsmName, this.state, this.checker.getType().name, this.checker.getType().entityType(), this.checker.toString());
  }

  public static class Builder<SUT> {
    private final FiniteStateMachine<SUT> fsm;
    private final String                  fsmName;
    private       OutputType              type;
    private       OutputChecker           checker;
    private       State<SUT>              state;

    Builder(
        String fsmName,
        FiniteStateMachine<SUT> fsm
    ) {
      this.fsm = fsm;
      this.fsmName = fsmName;
    }

    public Builder<SUT> invalid(FsmSpec<SUT> state) {
      return invalid(state, Throwable.class);
    }

    public Builder<SUT> invalid(FsmSpec<SUT> state, Class<? extends Throwable> klass) {
      Checks.checknotnull(state);
      this.type = OutputType.EXCEPTION_THROWN;
      this.state = chooseState(state);
      this.checker = new OutputChecker.MatcherBased(OutputType.EXCEPTION_THROWN, CoreMatchers.instanceOf(klass));
      return this;
    }

    public Builder<SUT> valid(FsmSpec<SUT> state) {
      return valid(state, CoreMatchers.anything());
    }

    public Builder<SUT> valid(FsmSpec<SUT> state, Object returnedValue) {
      return valid(state, CoreMatchers.is(returnedValue));
    }

    public Builder<SUT> valid(FsmSpec<SUT> state, Matcher matcher) {
      Checks.checknotnull(matcher);
      return valid(state, new OutputChecker.MatcherBased(OutputType.VALUE_RETURNED, matcher));
    }

    public Builder<SUT> valid(FsmSpec<SUT> state, OutputChecker checker) {
      Checks.checknotnull(state);
      Checks.checknotnull(checker);
      this.type = OutputType.VALUE_RETURNED;
      this.state = chooseState(state);
      this.checker = checker;
      return this;
    }

    private State<SUT> chooseState(StateChecker<SUT> stateChecker) {
      Checks.checknotnull(fsm);
      Checks.checknotnull(stateChecker);
      for (State<SUT> each : fsm.states()) {
        if (((State.Base) each).stateSpec == stateChecker)
          return each;
      }
      Checks.checkcond(false, "No state for '%s' was found.", stateChecker);
      return null;
    }

    public Expectation<SUT> build() {
      return new Expectation<>(
          this.fsmName,
          this.type,
          this.state,
          this.checker
      );
    }
  }
}
