package com.github.dakusui.jcunit.examples.fsm.concurrent;

import com.github.dakusui.jcunit.core.FactorField;
import com.github.dakusui.jcunit.core.JCUnit;
import com.github.dakusui.jcunit.examples.fsm.turnstile.Turnstile;
import com.github.dakusui.jcunit.examples.fsm.turnstile.TurnstileTest.Spec;
import com.github.dakusui.jcunit.fsm.FSMLevelsProvider;
import com.github.dakusui.jcunit.fsm.FSMUtils;
import com.github.dakusui.jcunit.fsm.Story;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JCUnit.class)
public class ConcurrentTurnstileTest {
  @FactorField(levelsProvider = FSMLevelsProvider.class)
  public Story<Turnstile, Spec> t1;
  @FactorField(levelsProvider = FSMLevelsProvider.class)
  public Story<Turnstile, Spec> t2;

  @Test//(timeout = 100)
  public void test1() {
    FSMUtils.performStoriesConcurrently(
        this,
        new Story.Request.ArrayBuilder()
            .add("t1", new Turnstile())
            .add("t2", new Turnstile())
            .build()
    );
  }
}
