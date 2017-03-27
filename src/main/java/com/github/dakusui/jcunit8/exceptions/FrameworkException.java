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

  public static FrameworkException unexpectedByDesign() {
    throw new FrameworkException("Unexpected by design") {
    };
  }

  public static FrameworkException unexpectedByDesign(Throwable t) {
    throw new FrameworkException("Unexpected by design", t) {
    };
  }
}
