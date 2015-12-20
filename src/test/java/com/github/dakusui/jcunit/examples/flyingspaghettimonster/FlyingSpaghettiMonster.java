package com.github.dakusui.jcunit.examples.flyingspaghettimonster;

import com.github.dakusui.jcunit.core.Checks;

public class FlyingSpaghettiMonster {
  private String dish  = null;
  public String cook(String pasta, String sauce) {
    Checks.checknotnull(pasta);
    this.dish = pasta;
    return String.format("Cooking %s %s", pasta, sauce);
  }

  public String eat() {
    if (dish != null) {
      return String.format("%s is yummy!", this.dish);
    }
    throw new IllegalStateException();
  }

  public boolean isReady() {
    return dish != null;
  }

  public String toString() {
    return String.format("%s@%s(%s)", this.getClass().getSimpleName(), System.identityHashCode(this), this.isReady());
  }
}
