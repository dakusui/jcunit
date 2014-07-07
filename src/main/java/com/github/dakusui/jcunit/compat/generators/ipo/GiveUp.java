package com.github.dakusui.jcunit.compat.generators.ipo;

import com.github.dakusui.jcunit.exceptions.JCUnitRuntimeException;
import com.github.dakusui.jcunit.core.Tuple;

/**
 * Created by hiroshi on 6/29/14.
 */
public class GiveUp extends JCUnitRuntimeException {
  private final Tuple tuple;

  public GiveUp(Tuple tuple) {
    super("Giving up", null);
    this.tuple = tuple;
  }

  public Tuple getTuple() {
    return this.tuple;
  }
}
