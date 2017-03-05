package com.github.dakusui.jcunit.tests.examples.fsm.concurrent;

import com.github.dakusui.jcunit.runners.standard.annotations.FactorField;
import com.github.dakusui.jcunit.runners.standard.JCUnit;
import com.github.dakusui.jcunit.examples.models.turnstile.Turnstile;
import com.github.dakusui.jcunit.examples.fsm.turnstile.TurnstileExample.Spec;
import com.github.dakusui.jcunit.fsm.FSMLevelsProvider;
import com.github.dakusui.jcunit.fsm.FSMUtils;
import com.github.dakusui.jcunit.fsm.ScenarioSequence;
import com.github.dakusui.jcunit.fsm.Story;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JCUnit.class)
public class ConcurrentTurnstileExample {
  @FactorField(levelsProvider = FSMLevelsProvider.class)
  public Story<Turnstile, Spec> t1;
  @FactorField(levelsProvider = FSMLevelsProvider.class)
  public Story<Turnstile, Spec> t2;

  @Test(timeout = 100)
  public void test1() {
    ScenarioSequence.Observer.Factory observerFactory = ScenarioSequence.Observer.Factory.ForSilent.INSTANCE;
    FSMUtils.performStoriesConcurrently(
        this,
        new Story.Request.ArrayBuilder()
            .add("t1", new Turnstile(), observerFactory)
            .add("t2", new Turnstile(), observerFactory)
            .build()
    );
  }
}
