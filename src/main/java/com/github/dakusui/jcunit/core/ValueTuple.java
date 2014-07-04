package com.github.dakusui.jcunit.core;

import java.util.HashMap;

public class ValueTuple<T, U> extends HashMap<T, U> implements Cloneable {

  /**
   * Serial version UID
   */
  private static final long serialVersionUID = 1L;

  @Override
  public ValueTuple<T, U> clone() {
    return (ValueTuple<T, U>) super.clone();
  }
}
