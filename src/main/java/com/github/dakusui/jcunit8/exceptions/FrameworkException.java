package com.github.dakusui.jcunit8.exceptions;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * An exception that indicates a bug in JCUnit's framework.
 */
public abstract class FrameworkException extends BaseException {
  protected FrameworkException(String format) {
    super(format);
  }

  protected FrameworkException(String format, Throwable t) {
    super(format, t);
  }

  @SuppressWarnings("WeakerAccess")
  public static void checkCondition(boolean b, Function<String, ? extends FrameworkException> exceptionFactory, Supplier<String> messageSupplier) {
    if (!b)
      throw exceptionFactory.apply(messageSupplier.get());
  }

  public static void checkCondition(boolean b, Function<String, ? extends FrameworkException> exceptionFactory) {
    checkCondition(b, exceptionFactory, () -> "Unexpected by design");
  }

  public static void checkCondition(boolean b) {
    checkCondition(b, FrameworkException::unexpectedByDesign);
  }

  public static FrameworkException unexpectedByDesign() {
    throw new FrameworkException("Unexpected by design") {
    };
  }

  public static FrameworkException unexpectedByDesign(Throwable t) {
    if (t instanceof Error)
      throw (Error) t;
    if (t instanceof RuntimeException)
      throw (RuntimeException) t;
    throw new FrameworkException("Unexpected by design", t) {
    };
  }

  public static FrameworkException unexpectedByDesign(String message) {
    throw new FrameworkException(String.format("Unexpected by design:%s", message)) {
    };
  }
}
