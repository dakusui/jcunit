package com.github.dakusui.jcunit8.exceptions;

import static java.lang.String.format;

/**
 * Indicates user's artifact (Typically test classes, annotated with
 * {@literal @}{@code Runwith(JCUnit.class)}), is invalid.
 */
public class TestDefinitionException extends BaseException {
  protected TestDefinitionException(String message) {
    super(message);
  }

  public static TestDefinitionException testClassIsInvalid(Class testClass) {
    throw new TestDefinitionException(format("User test class '%s' is invalid.", testClass.getCanonicalName()));
  }

  public static TestDefinitionException fsmDoesNotHaveRouteToSpecifiedState(Object destinationState, String fsmName, Object fsmModel) {
    throw new TestDefinitionException(String.format("No route to '%s' was found on FSM:'%s'(%s)", destinationState, fsmName, fsmModel));
  }
}
