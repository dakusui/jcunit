package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.Utils;

public class Output {
  public final Object value;
  public final Type   type;

  public Output(Type type, Object value) {
    this.type = Checks.checknotnull(type);
    this.value =
        this.type == Type.VALUE_RETURNED
            ? value
            : Checks.checknotnull(value);
  }

  /**
   * Types of an expectation. Each element represents an expectation of its
   * relevant method.
   */
  public enum Type {
    /**
     * Expects that an exception is thrown of the method.
     */
    EXCEPTION_THROWN("thrown") {
      public String entityType() {
        return "exception";
      }
    },
    /**
     * Expects that a value is returned of the method.
     * No exception is thrown, in other words.
     */
    VALUE_RETURNED("returned") {
      @Override
      public String toString() {
        return "returned";
      }

      public String entityType() {
        return "value";
      }
    };

    private final String name;

    Type(String name) {
      this.name = name;
    }

    abstract protected String entityType();

    public String describeExpectation(Object matcher) {
      return Utils.format(
          ////
          // EXPECTATION:
          // "returned" / "thrown"
          // "value"    / "exception"
          // {matcher.toString()} (e.g. "is 'Hello'")
          "%s %s %s",
          this.name,
          this.entityType(),
          matcher
      );
    }

    public String describeMismatch(Object output, Type outputType) {
      return Utils.format(
          ////
          // MISMATCH:
          // "returned" / "thrown"
          // "value"    / "exception"
          // was
          "%s %s was '%s'",
          Checks.checknotnull(outputType).name,
          outputType.entityType(),
          output
      );
    }
  }
}
