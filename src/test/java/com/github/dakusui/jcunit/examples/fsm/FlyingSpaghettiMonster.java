package com.github.dakusui.jcunit.examples.fsm;

import com.github.dakusui.jcunit.core.Checks;

public class FlyingSpaghettiMonster {
  private String dish  = null;
  public String cook(String dish) {
    Checks.checknotnull(dish);
    this.dish = dish;
    return String.format("Cooking %s", dish);
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
}
