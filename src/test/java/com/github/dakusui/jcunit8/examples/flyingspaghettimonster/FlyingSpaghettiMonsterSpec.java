package com.github.dakusui.jcunit8.examples.flyingspaghettimonster;

import com.github.dakusui.jcunit.fsm.Expectation;
import com.github.dakusui.jcunit.fsm.spec.ActionSpec;
import com.github.dakusui.jcunit.fsm.spec.FsmSpec;
import com.github.dakusui.jcunit.fsm.spec.StateSpec;

public enum FlyingSpaghettiMonsterSpec implements FsmSpec<FlyingSpaghettiMonster> {
  @StateSpec I {
    @Override
    public boolean check(FlyingSpaghettiMonster flyingSpaghettiMonster) {
      return false;
    }
  },
  @StateSpec COOKING {
    @Override
    public boolean check(FlyingSpaghettiMonster flyingSpaghettiMonster) {
      return false;
    }
  },
  @StateSpec EATING {
    @Override
    public boolean check(FlyingSpaghettiMonster flyingSpaghettiMonster) {
      return false;
    }
  },
  @StateSpec FLYING {
    @Override
    public boolean check(FlyingSpaghettiMonster flyingSpaghettiMonster) {
      return false;
    }

    @ActionSpec
    public Expectation<FlyingSpaghettiMonster> land(Expectation.Builder<FlyingSpaghettiMonster> builder) {
      return builder.valid(I).build();
    }
  };

  @ActionSpec
  public Expectation<FlyingSpaghettiMonster> cook(Expectation.Builder<FlyingSpaghettiMonster> builder) {
    return builder.invalid().build();
  }

  @ActionSpec
  public Expectation<FlyingSpaghettiMonster> eat(Expectation.Builder<FlyingSpaghettiMonster> builder) {
    return builder.invalid().build();
  }

  @ActionSpec
  public Expectation<FlyingSpaghettiMonster> land(Expectation.Builder<FlyingSpaghettiMonster> builder) {
    return builder.invalid().build();
  }

  @ActionSpec
  public Expectation<FlyingSpaghettiMonster> fly(Expectation.Builder<FlyingSpaghettiMonster> builder) {
    return builder.invalid().build();
  }
}

