package com.github.dakusui.jcunit.examples.fsm;

import com.github.dakusui.jcunit.fsm.*;

import java.util.List;

public class FlyingSpaghettiMonsterTest {
  @FSMSpec
  public static enum Spec implements State<FlyingSpaghettiMonster> {
    I {
      @Override
      public Expectation<FlyingSpaghettiMonster> expectation(Action action, Args args) {
        return null;
      }

      @Override
      public boolean matches(FlyingSpaghettiMonster flyingSpaghettiMonster) {
        return false;
      }
    };
  }
}
