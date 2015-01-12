package com.github.dakusui.jcunit.examples.fsm;

import com.github.dakusui.jcunit.fsm.FSMSpec;
import com.github.dakusui.jcunit.fsm.FSMSpec.Initial;
import com.github.dakusui.jcunit.fsm.FSMSpec.Transition;

public class FlyingSpaghettiMonsterTest {
  @FSMSpec
  public static enum FlyingSpaghettiMonsterSpec {
    @Initial
    I {
      @Override
      public FlyingSpaghettiMonsterSpec order() {
        return ORDERED;
      }
    },
    ORDERED,
    COOKED,
    SERVED,
    DONE;

    @Transition
    public FlyingSpaghettiMonsterSpec order() {
      throw new IllegalStateException();
    }

    @Transition
    public FlyingSpaghettiMonsterSpec cook() {
      throw new IllegalStateException();
    }

    @Transition
    public FlyingSpaghettiMonsterSpec serve() {
      throw new IllegalStateException();
    }

    @Transition
    public FlyingSpaghettiMonsterSpec eat() {
      throw new IllegalStateException();
    }

    @Transition
    public FlyingSpaghettiMonsterSpec cancel() {
      throw new IllegalStateException();
    }
  }
}
