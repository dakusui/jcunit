package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;

/**
 * An interface that models checking process for an output of a method
 * (a returned value/thrown exception).
 */
public interface OutputChecker {
  /**
   * Checks if this object should be performed for a given scenario type.
   */
  boolean shouldBeCheckedFor(Story.Stage stage);

  Output.Type getType();

  /**
   * Checks the {@code item} matches the criterion that this object defines.
   * {@code true} will be returned if it does, {@code false} otherwise.
   *
   * @param context  A context in which this check is performed.
   * @param output   An output to be checked. (Output can be either returned object or thrown exception by a method)
   * @param observer An observer to which the checking result will be reported.
   */
  <SUT, T> Result check(Story.Context<SUT, T> context, Output output, ScenarioSequence.Observer observer);


  class Result {
    private final String  description;
    private final boolean successful;

    public Result(boolean successful, String description) {
      this.successful = successful;
      this.description = successful
          ? description
          : Checks.checknotnull(description, "If the checking is not successful, description of the failure needs to be provided.");
    }

    public boolean isSuccessful() {
      return successful;
    }

    public String getDescription() {
      return this.description;
    }
  }

  abstract class Base implements OutputChecker {
    public final Output.Type type;

    public Base(Output.Type type) {
      this.type = Checks.checknotnull(type);
    }

    @Override
    public boolean shouldBeCheckedFor(Story.Stage stage) {
      return true;
    }

    @Override
    public Output.Type getType() {
      return this.type;
    }
  }

  class MatcherBased extends Base implements OutputChecker {
    private final Matcher matcher;

    /**
     * Creates an object of this class.
     *
     * @param type expected output type.
     * @param matcher expectation for output.
     */
    public MatcherBased(Output.Type type, Matcher matcher) {
      super(type);
      this.matcher = Checks.checknotnull(matcher);
    }

    @Override
    public <SUT, T> Result check(
        Story.Context<SUT, T> context,
        Output output,
        ScenarioSequence.Observer observer) {
      if (this.type != output.type) {
        if (output.type == Output.Type.EXCEPTION_THROWN) {
          Checks.checkcond(output.value instanceof Throwable);
          return new Result(
              false,
              Utils.format(
                  "An exception (%s:[%s]) was thrown unexpectedly.",
                  output.value,
                  ((Throwable) output.value).getMessage()
              )
          );
        } else if (output.type == Output.Type.VALUE_RETURNED) {
          return new Result(
              false,
              Utils.format(
                  "A value (%s) was returned unexpectedly. (The method should have failed with %s)",
                  output.value,
                  matcher
              )
          );
        } else {
          Checks.checkcond(false, "Unknown type '%s' was given.", output.type);
        }
      }
      return new Result(
          this.matcher.matches(Checks.checknotnull(output).value),
          Utils.format(
              "Expectation: %s%nActual:%s",
              this.type.describeExpectation(this.matcher),
              this.type.describeMismatch(output)
          )
      );
    }

    @Override
    public String toString() {
      return this.matcher.toString();
    }
  }

  class FSM extends Base implements OutputChecker {
    String fsmName;

    public FSM(String fsmName) {
      ////
      // It doesn't make sense to create an FSM for an exception.
      super(Output.Type.VALUE_RETURNED);
      Checks.checknotnull(fsmName);
      this.fsmName = fsmName;
    }

    @Override
    public <SUT, T> Result check(Story.Context<SUT, T> context, Output output, ScenarioSequence.Observer observer) {
      Checks.checknotnull(context);
      Story story = context.lookUpFSMStory(this.fsmName);
      if (!Checks.checknotnull(story).isPerformed()) {
        //noinspection unchecked
        Story.Performer.Default.INSTANCE.perform(
            story,
            context.testObject,
            new SUTFactory.Dummy(Checks.checknotnull(output).value),
            ////
            // Synchronization happens only at the top level.
            FSMUtils.Synchronizer.DUMMY, observer.createChild(this.fsmName)
        );
      }
      ////
      // Unless nested story is passing, this path will not be executed.
      // In other words, true should always be returned.
      return new Result(true, null);
    }

    @Override
    public boolean shouldBeCheckedFor(Story.Stage stage) {
      return stage == Story.Stage.MAIN;
    }
  }

  abstract class ForInputHistory extends Base {
    public ForInputHistory(Output.Type type) {
      super(type);
    }

    @Override
    public <SUT, T> Result check(
        final Story.Context<SUT, T> context,
        Output output,
        ScenarioSequence.Observer observer) {
      InputHistory.Accessed accessedSymbols = new InputHistory.Accessed(context.inputHistory);
      String expectation;
      boolean passed;
      try {
        Object expect = computeExpectation(accessedSymbols);
        Matcher matcher = Checks.checknotnull(this.createMatcher(expect));
        passed = matcher.matches(Checks.checknotnull(output).value);
        expectation = describeExpectation(accessedSymbols, matcher);
      } catch (UndefinedSymbol e) {
        passed = false;
        expectation = Utils.format("failed to compute expectation since following symbols are not found in input history: %s", e.missingSymbols);
      }
      return new Result(
          passed,
          Utils.format(
              "Expectation: %s%nActual:      %s",
              expectation,
              this.type.describeMismatch(output)
          )
      );
    }

    private String describeExpectation(InputHistory.Accessed accessed, Matcher matcher) {
      String expectation;
      expectation = this.type.describeExpectation(
          Utils.format(
              "%s (%s#computeExpectation(%s))",
              matcher,
              this,
              Utils.join(",", accessed.symbols.toArray())
          )
      );
      return expectation;
    }

    protected Matcher createMatcher(Object expectation) {
      return CoreMatchers.is(expectation);
    }

    protected abstract Object computeExpectation(InputHistory inputHistory) throws UndefinedSymbol;

    public String toString() {
      return String.format(
          "%s %s#computeExpectation(...)",
          createMatcher("...").toString(),
          Utils.toString(this)
      );
    }
  }
}
