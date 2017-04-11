package com.github.dakusui.jcunit8.exceptions;

import com.github.dakusui.jcunit.core.tuples.Tuple;

import java.util.List;

import static java.lang.String.format;

/**
 * Indicates user's artifact (Typically test classes, annotated with
 * {@literal @}{@code Runwith(JCUnit.class)}), is invalid.
 */
public class TestDefinitionException extends BaseException {
  private TestDefinitionException(String message) {
    super(message);
  }

  private TestDefinitionException(String message, Throwable e) {
    super(message, e);
  }

  public static TestDefinitionException testClassIsInvalid(Class testClass) {
    throw new TestDefinitionException(format("User test class '%s' is invalid.", testClass.getCanonicalName()));
  }

  public static TestDefinitionException fsmDoesNotHaveRouteToSpecifiedState(Object destinationState, String fsmName, Object fsmModel) {
    throw new TestDefinitionException(String.format("No route to '%s' was found on FSM:'%s'(%s)", destinationState, fsmName, fsmModel));
  }

  public static TestDefinitionException failedToCover(String factorName, List<Object> factorLevels, Tuple tuple) {
    throw new TestDefinitionException(String.format("Factor '%s' doesn't have any valid level '%s' for tuple '%s'", factorName, factorLevels, tuple));
  }

  public static TestDefinitionException failedToInstantiateSut(Throwable e, String fmt, Object... args) {
    throw new TestDefinitionException(String.format(fmt, args), e);
  }
}
