package com.github.dakusui.jcunit8.exceptions;

import java.util.List;
import java.util.function.Supplier;

import static java.lang.String.format;

/**
 * An exception that indicates a bug in JCUnit's framework.
 */
public class FrameworkException extends BaseException {
  private FrameworkException(String format) {
    super(format);
  }

  @SuppressWarnings("WeakerAccess")
  public static void checkCondition(boolean b, Supplier<String> messageSupplier) {
    if (!b)
      throw new FrameworkException(messageSupplier.get());
  }

  public static void checkStrengthIsInRange(int strength, List<String> attributeNames) {
    checkCondition(
        0 < strength && strength <= attributeNames.size(),
        () -> format("Given strength '%s' is not in appropriate range (0, %d]", strength, attributeNames.size())
    );
  }

  public static FrameworkException unexpectedByDesign() {
    return new FrameworkException("Unexpected by design");
  }
}
