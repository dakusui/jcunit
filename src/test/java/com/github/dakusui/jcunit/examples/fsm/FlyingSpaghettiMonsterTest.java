package com.github.dakusui.jcunit.examples.fsm;

import com.github.dakusui.jcunit.core.FSMProvider;
import com.github.dakusui.jcunit.fsm.*;
import org.hamcrest.CoreMatchers;

public class FlyingSpaghettiMonsterTest {
  public static enum Spec implements FSMSpec<FlyingSpaghettiMonster> {
    @State I {
      @Override
      public boolean check(FlyingSpaghettiMonster flyingSpaghettiMonster) {
        return flyingSpaghettiMonster.isReady();
      }

      @Action
      public Expectation<FlyingSpaghettiMonster> cook(FSM<FlyingSpaghettiMonster> fsm, String dish) {
        return FSMUtils.valid(fsm, COOKED, CoreMatchers.startsWith("Cooking"));
      }
    },
    @State COOKED {
      @Override
      public boolean check(FlyingSpaghettiMonster flyingSpaghettiMonster) {
        return flyingSpaghettiMonster.isReady();
      }
    },;

    @Action
    public Expectation<FlyingSpaghettiMonster> cook(FSM<FlyingSpaghettiMonster> fsm, String dish) {
      return FSMUtils.invalid();
    }

    @Action
    public Expectation<FlyingSpaghettiMonster> eat(FSM<FlyingSpaghettiMonster> fsm) {
      return FSMUtils.invalid();
    }

  }

  @FSMProvider
  public static FSM flyingSpaghettiMonsterFSM() {
    return FSMUtils.createFSM(Spec.class);
  }
}
