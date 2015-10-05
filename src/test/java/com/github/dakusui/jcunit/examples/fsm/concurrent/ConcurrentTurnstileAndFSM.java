package com.github.dakusui.jcunit.examples.fsm.concurrent;

import com.github.dakusui.jcunit.core.FactorField;
import com.github.dakusui.jcunit.core.JCUnit;
import com.github.dakusui.jcunit.core.Param;
import com.github.dakusui.jcunit.examples.fsm.flyingspaghettimonster.FlyingSpaghettiMonster;
import com.github.dakusui.jcunit.examples.fsm.flyingspaghettimonster.FlyingSpaghettiMonsterTest;
import com.github.dakusui.jcunit.examples.fsm.turnstile.Turnstile;
import com.github.dakusui.jcunit.examples.fsm.turnstile.TurnstileTest;
import com.github.dakusui.jcunit.fsm.FSMLevelsProvider;
import com.github.dakusui.jcunit.fsm.FSMUtils;
import com.github.dakusui.jcunit.fsm.ScenarioSequence;
import com.github.dakusui.jcunit.fsm.Story;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JCUnit.class)
public class ConcurrentTurnstileAndFSM {
  @FactorField(levelsProvider = FSMLevelsProvider.class)
  public Story<Turnstile, TurnstileTest.Spec>                           turnstile;
  @FactorField(levelsProvider = FSMLevelsProvider.class, providerParams = @Param("2"))
  public Story<FlyingSpaghettiMonster, FlyingSpaghettiMonsterTest.Spec> fsm;

  @Test(timeout = 1000)
  public void test() {
    ScenarioSequence.Observer.Factory observerFactory = ScenarioSequence.Observer.Factory.ForSilent.INSTANCE;
    FSMUtils.performStoriesConcurrently(
        this,
        new Story.Request.ArrayBuilder()
            .add("turnstile", new Turnstile(), observerFactory)
            .add("fsm", new FlyingSpaghettiMonster(), observerFactory)
            .build()
    );
  }
}
