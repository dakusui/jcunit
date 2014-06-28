package com.github.dakusui.jcunit.generators.ipo;

import com.github.dakusui.jcunit.core.ValueTuple;

/**
 * Created by hiroshi on 14/06/25.
 */
public class ConstraintViolationException extends RuntimeException {
  private ValueTuple<String, Object> violation;

  public ValueTuple<String, Object> getViolation() {
    return violation;
  }
}
