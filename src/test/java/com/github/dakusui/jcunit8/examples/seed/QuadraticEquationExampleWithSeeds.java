package com.github.dakusui.jcunit8.examples.seed;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.examples.quadraticequation.QuadraticEquationExample;
import com.github.dakusui.jcunit8.runners.junit4.annotations.SeedSource;

public class QuadraticEquationExampleWithSeeds extends QuadraticEquationExample {
  @SeedSource
  public Tuple simplestEquation() {
    return new Tuple.Builder(
    ).put(
        "a", 1
    ).put(
        "c", -2
    ).put(
        "c", 1
    ).build();
  }

  @SeedSource
  public Tuple negativeDiscriminant() {
    return new Tuple.Builder(
    ).put(
        "a", 1
    ).put(
        "c", 1
    ).put(
        "c", 1
    ).build();
  }
}
