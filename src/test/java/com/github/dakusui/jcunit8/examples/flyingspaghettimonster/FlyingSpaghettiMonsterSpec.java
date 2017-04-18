package com.github.dakusui.jcunit8.examples.flyingspaghettimonster;

import com.github.dakusui.jcunit.fsm.Expectation;
import com.github.dakusui.jcunit.fsm.Parameters;
import com.github.dakusui.jcunit.fsm.spec.ActionSpec;
import com.github.dakusui.jcunit.fsm.spec.FsmSpec;
import com.github.dakusui.jcunit.fsm.spec.ParametersSpec;
import com.github.dakusui.jcunit.fsm.spec.StateSpec;

public enum FlyingSpaghettiMonsterSpec implements FsmSpec<FlyingSpaghettiMonster> {
  @StateSpec I {
    @ActionSpec
    @Override
    public Expectation<FlyingSpaghettiMonster> cook(Expectation.Builder<FlyingSpaghettiMonster> builder, String pasta) {
      return builder.valid(COOKING).build();
    }

    @ActionSpec
    @Override
    public Expectation<FlyingSpaghettiMonster> takeOff(Expectation.Builder<FlyingSpaghettiMonster> builder) {
      return builder.valid(FLYING).build();
    }
  },
  @StateSpec COOKING {
    @ActionSpec
    @Override
    public Expectation<FlyingSpaghettiMonster> serve(Expectation.Builder<FlyingSpaghettiMonster> builder) {
      return builder.valid(COOKED).build();
    }
  },
  @StateSpec COOKED {
    @ActionSpec
    @Override
    public Expectation<FlyingSpaghettiMonster> eat(Expectation.Builder<FlyingSpaghettiMonster> builder) {
      return builder.valid(I).build();
    }
  },

  @StateSpec FLYING {
    @ActionSpec
    @Override
    public Expectation<FlyingSpaghettiMonster> perch(Expectation.Builder<FlyingSpaghettiMonster> builder) {
      return builder.valid(I).build();
    }
  };

  @Override
  public boolean check(FlyingSpaghettiMonster fsm) {
    return true;
  }

  @ParametersSpec
  public static final Parameters cook = new Parameters.Builder("dish")
      .add("peperoncino", "meat sauce")
      .build();

  @ActionSpec
  public Expectation<FlyingSpaghettiMonster> cook(Expectation.Builder<FlyingSpaghettiMonster> builder, String pasta) {
    return builder.invalid(this, Throwable.class).build();
  }


  @ActionSpec
  public Expectation<FlyingSpaghettiMonster> serve(Expectation.Builder<FlyingSpaghettiMonster> builder) {
    return builder.invalid(this, Throwable.class).build();
  }

  @ActionSpec
  public Expectation<FlyingSpaghettiMonster> eat(Expectation.Builder<FlyingSpaghettiMonster> builder) {
    return builder.invalid(this, Throwable.class).build();
  }

  @ActionSpec
  public Expectation<FlyingSpaghettiMonster> perch(Expectation.Builder<FlyingSpaghettiMonster> builder) {
    return builder.invalid(this, Throwable.class).build();
  }

  @ActionSpec
  public Expectation<FlyingSpaghettiMonster> takeOff(Expectation.Builder<FlyingSpaghettiMonster> builder) {
    return builder.invalid(this, Throwable.class).build();
  }
}

