package com.github.dakusui.jcunit.generators.ipo;

import com.github.dakusui.jcunit.core.ValueTuple;
import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.jcunit.exceptions.JCUnitRuntimeException;

/**
 * Created by hiroshi on 6/29/14.
 */
public class GiveUp extends JCUnitRuntimeException {
  private final ValueTuple<String, Object> tuple;

  public GiveUp(ValueTuple<String, Object> tuple) {
    super("Giving up", null);
    this.tuple = tuple;
  }

  public ValueTuple<String, Object> getTuple() {
    return this.tuple;
  }
}
