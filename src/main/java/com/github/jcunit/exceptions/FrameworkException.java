package com.github.jcunit.exceptions;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * An exception that indicates a bug in JCUnit's framework.
 */
public class FrameworkException extends BaseException {
  protected FrameworkException(String format) {
    super(format);
  }

  @SuppressWarnings("WeakerAccess")
  public static void checkCondition(boolean b, Function<String, ? extends FrameworkException> exceptionFactory, Supplier<String> messageSupplier) {
    if (!b)
      throw exceptionFactory.apply(messageSupplier.get());
  }

  public static <T> T check(T value, Predicate<T> check) {
    if (check.test(value))
      return value;
    throw new FrameworkException("Unexpected by design");
  }

  public static <T> T check(T value, Predicate<T> check, Supplier<String> messageSupplier) {
    if (check.test(value))
      return value;
    throw new FrameworkException(messageSupplier.get());
  }

  public static void checkCondition(boolean b, Function<String, ? extends FrameworkException> exceptionFactory) {
    checkCondition(b, exceptionFactory, () -> "Unexpected by design");
  }

  public static void checkCondition(boolean b) {
    checkCondition(b, FrameworkException::unexpectedByDesign);
  }

  public static FrameworkException unexpectedByDesign() {
    throw new FrameworkException("Unexpected by design");
  }

  public static FrameworkException unexpectedByDesign(String message) {
    throw new FrameworkException(String.format("Unexpected by design:%s", message));
  }
}
