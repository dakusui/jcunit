package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;
import org.hamcrest.Matcher;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * An interface that models checking process for an output of a method
 * (a returned value/thrown exception).
 */
public interface OutputChecker {
  /**
   * Checks if this object should be performed for a given scenario type.
   */
  boolean shouldBeCheckedFor(Story.Stage stage);

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
  }

  class MatcherBased extends Base implements OutputChecker {
    private final Matcher matcher;

    public MatcherBased(Output.Type type, Matcher matcher) {
      super(type);
      this.matcher = Checks.checknotnull(matcher);
    }

    @Override
    public <SUT, T> Result check(Story.Context<SUT, T> context, Output output, ScenarioSequence.Observer observer) {
      return new Result(
          this.matcher.matches(Checks.checknotnull(output).value),
          Utils.format(
              "Expectation: %s%nActual:%s",
              this.type.describeExpectation(this.matcher),
              this.type.describeMismatch(output.value, output.type)
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
      AccessedSymbols accessedSymbols = new ForInputHistory.AccessedSymbols(context.inputHistory);
      String expectation;
      boolean passed;
      try {
        Object expect = computeExpectation(accessedSymbols);
        passed = this.evaluate(expect, Checks.checknotnull(output).value);
        expectation = describeExpectation(accessedSymbols, expect);
      } catch (UndefinedSymbol e) {
        passed = false;
        expectation = Utils.format("failed to compute expectation since following symbols are not found in input history: %s", e.missingSymbols);
      }
      return new Result(
          passed,
          Utils.format(
              "Expectation: %s%nActual:      %s",
              expectation,
              this.type.describeMismatch(output.value, output.type)
          )
      );
    }


    protected boolean evaluate(Object expectation, Object actual) {
      return Utils.eq(expectation, actual);
    }

    /**
     * In case you are overriding {@code evaluate} method, you should override
     * this method, too.
     */
    protected String predicate() {
      return "is equal to";
    }

    protected abstract Object computeExpectation(InputHistory inputHistory) throws UndefinedSymbol;

    public String toString() {
       return Utils.format(
           "%s %s#computeExpectation(...)",
           predicate(),
           Utils.getSimpleClassName(this)
           );
    }


    private String describeExpectation(AccessedSymbols accessedSymbols, Object expect) {
      String expectation;
      expectation = this.type.describeExpectation(
          Utils.format(
              "%s '%s' (%s#computeExpectation(%s))",
              predicate(),
              expect,
              this,
              Utils.join(",", accessedSymbols.accessedSymbols.toArray())
          )
      );
      return expectation;
    }

    class AccessedSymbols implements InputHistory {
      private final InputHistory base;
      private final List<String> accessedSymbols;

      AccessedSymbols(InputHistory base) {
        this.base = Checks.checknotnull(base);
        this.accessedSymbols = new ArrayList<String>(base.size());
      }

      @Override
      public <T> void add(String name, T data) {
        throw new UnsupportedOperationException();
      }

      @Override
      public boolean has(String name) {
        accessedSymbols.add(name);
        return base.has(name);
      }

      @Override
      public <E> Record<E> get(String name) throws UndefinedSymbol {
        accessedSymbols.add(name);
        return base.get(name);
      }

      @Override
      public Iterable<String> recordNames() {
        final Iterator<String> base = AccessedSymbols.this.base.recordNames().iterator();
        return new Iterable<String>() {
          @Override
          public Iterator<String> iterator() {
            return new Iterator<String>() {
              @Override
              public boolean hasNext() {
                return base.hasNext();
              }

              @Override
              public String next() {
                String ret = base.next();
                accessedSymbols.add(ret);
                return ret;
              }

              @Override
              public void remove() {
                throw new UnsupportedOperationException();
              }
            };
          }
        };
      }

      @Override
      public int size() {
        return base.size();
      }

      @Override
      public String toString() {
        boolean firstTime = true;
        StringBuilder b = new StringBuilder("{");
        for (String each : this.accessedSymbols) {
          if (!firstTime)
            b.append(",");
          firstTime = false;
          b.append(each);
          b.append(":");
          try {
            b.append(this.base.get(each));
          } catch (UndefinedSymbol e) {
            b.append("(unknown)");
          }
        }
        b.append("}");
        return b.toString();
      }
    }
  }
}
