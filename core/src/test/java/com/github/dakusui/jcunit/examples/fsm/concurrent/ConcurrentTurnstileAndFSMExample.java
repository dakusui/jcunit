package com.github.dakusui.jcunit.examples.fsm.concurrent;

import com.github.dakusui.jcunit.runners.standard.annotations.FactorField;
import com.github.dakusui.jcunit.runners.standard.JCUnit;
import com.github.dakusui.jcunit.runners.standard.annotations.Value;
import com.github.dakusui.jcunit.examples.models.flyingspaghettimonster.FlyingSpaghettiMonster;
import com.github.dakusui.jcunit.examples.fsm.flyigspaghettimonster.FlyingSpaghettiMonsterExample;
import com.github.dakusui.jcunit.examples.models.turnstile.Turnstile;
import com.github.dakusui.jcunit.examples.fsm.turnstile.TurnstileExample;
import com.github.dakusui.jcunit.fsm.*;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JCUnit.class)
public class ConcurrentTurnstileAndFSMExample {
  @FactorField(levelsProvider = FSMLevelsProvider.class)
  public Story<Turnstile, TurnstileExample.Spec>                           turnstile;
  @FactorField(levelsProvider = FSMLevelsProvider.class, args = @Value("2"))
  public Story<FlyingSpaghettiMonster, FlyingSpaghettiMonsterExample.Spec> fsm;

  @Test(timeout = 1000)
  public void test() {
    ScenarioSequence.Observer.Factory observerFactory = ScenarioSequence.Observer.Factory.ForSilent.INSTANCE;
    FSMUtils.performStoriesConcurrently(
        this,
        new Story.Request.ArrayBuilder()
            ////
            // Simpler way to give SUT to FSM/JCUnit
            .add("turnstile", new Turnstile(), observerFactory)
                ////
                // More strict way to give SUT to FSM/JCUnit. This style allows you to
                // collect parameter values given to your SUT during a test.
                // Refer to FSMParamTest for an example.
            .add("fsm", new SUTFactory.Simple<FlyingSpaghettiMonster>(
                FlyingSpaghettiMonster.class
            ), observerFactory)
            .build()
    );
  }
}
