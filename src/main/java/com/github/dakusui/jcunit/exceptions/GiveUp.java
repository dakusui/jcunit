package com.github.dakusui.jcunit.exceptions;

import com.github.dakusui.jcunit.core.tuples.Tuple;

public class GiveUp extends JCUnitException {
  private final Tuple tuple;

  public GiveUp(Tuple tuple) {
    super("Giving up", null);
    this.tuple = tuple;
  }

  public Tuple getTuple() {
    return this.tuple;
  }
}
