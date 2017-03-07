package com.github.dakusui.jcunit.testutils.behaviour;

import com.github.dakusui.jcunit.core.utils.Checks;

public class TestScenario<SUT> {
  public final Given<SUT> given;
  public final When<SUT>  when;
  public final Then       then;

  public TestScenario(Given<SUT> given, When<SUT> when, Then then) {
    this.given = Checks.checknotnull(given);
    this.when = Checks.checknotnull(when);
    this.then = Checks.checknotnull(then);
  }

  public void execute() {
    SUT sut = this.prepare();
    Output output = this.perform(sut);
    this.thenCheckOutput(output);
  }

  protected SUT prepare() {
    return this.given.prepare();
  }

  protected Output perform(SUT sut) {
    try {
      Object value = this.when.perform(sut);
      if (value == When.VOID) {
        return Output.voidReturned();
      } else {
        return Output.valueReturned(value);
      }
    } catch (Error error) {
      // Application that throws an error as its function is out of scope.
      throw error;
    } catch (Throwable throwable) {
      return Output.exceptionThrown(throwable);
    }
  }

  protected void thenCheckOutput(Output output) {
    this.then.assertOutput(output);
  }

  public interface Given<SUT> {
    SUT prepare();
  }

  public interface When<SUT> {
    Object VOID = new Object();

    Object perform(SUT sut) throws Throwable;
  }

  public interface Then {
    void assertOutput(Output output);
  }

  public static class Output {
    public final Output.Type type;
    public final Object      output;

    public enum Type {
      EXCEPTION_THROWN,
      VALUE_RETURNED,
      VOID
    }

    private Output(Output.Type type, Object output) {
      this.type = Checks.checknotnull(type);
      this.output = output; // output can be null.
    }

    public static Output exceptionThrown(Throwable t) {
      return new Output(Output.Type.EXCEPTION_THROWN, Checks.checknotnull(t));
    }

    public static Output valueReturned(Object value) {
      return new Output(Output.Type.VALUE_RETURNED, value);
    }

    public static Output voidReturned() {
      return new Output(Output.Type.VOID, null);
    }
  }
}
