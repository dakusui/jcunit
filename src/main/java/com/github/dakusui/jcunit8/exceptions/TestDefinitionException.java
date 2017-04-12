package com.github.dakusui.jcunit8.exceptions;

import com.github.dakusui.jcunit.core.tuples.Tuple;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

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

  public static TestDefinitionException wrap(Throwable t) {
    throw new TestDefinitionException(String.format("Test definition is not valid: %s", t.getMessage()), t);
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

  public static <T> T checkValue(T value, Predicate<T> check) {
    return checkValue(value, check, "'%s' is not valid", value);
  }

  public static <T> T checkValue(T value, Predicate<T> check, Supplier<String> messageComposer) {
    if (check.test(value))
      return value;
    throw new TestDefinitionException(messageComposer.get());
  }

  public static <T> T checkValue(T value, Predicate<T> check, String fmt, Object... args) {
    return checkValue(value, check, () -> String.format(fmt, args));
  }

  public static Supplier<TestDefinitionException> sutDoesNotHaveSpecifiedMethod(Class<?> klass, String name, List args) {
    return new Supplier<TestDefinitionException>() {
      @Override
      public TestDefinitionException get() {
        throw new TestDefinitionException(String.format("SUT '%s' does not have a method %s/%d (%s)", klass.getCanonicalName(), name, args.size(), args));
      }
    };
  }
}
