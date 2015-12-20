package com.github.dakusui.jcunit.examples.fsm.doublefsm;

import com.github.dakusui.jcunit.runners.standard.annotations.FactorField;
import com.github.dakusui.jcunit.runners.standard.JCUnit;
import com.github.dakusui.jcunit.examples.fsm.turnstile.Turnstile;
import com.github.dakusui.jcunit.examples.fsm.turnstile.TurnstileExample;
import com.github.dakusui.jcunit.fsm.FSMLevelsProvider;
import com.github.dakusui.jcunit.fsm.FSMUtils;
import com.github.dakusui.jcunit.fsm.ScenarioSequence;
import com.github.dakusui.jcunit.fsm.Story;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(JCUnit.class)
public class DoubleFSMExample {
  public static ScenarioSequence.Observer.Factory observerFactory = ScenarioSequence.Observer.Factory.ForSilent.INSTANCE;

  @FactorField(levelsProvider = FSMLevelsProvider.class)
  public Story<Turnstile, TurnstileExample.Spec> fsm1;

  @FactorField(levelsProvider = FSMLevelsProvider.class)
  public Story<Turnstile, TurnstileExample.Spec> fsm2;

  Turnstile sut1 = new Turnstile();
  Turnstile sut2 = new Turnstile();

  @Test
  public void test() throws Throwable {
    assertFalse(fsm1.isPerformed());
    FSMUtils.performStory(this, "fsm1", sut1, observerFactory);
    assertTrue(fsm1.isPerformed());
    assertFalse(fsm2.isPerformed());
    FSMUtils.performStory(this, "fsm2", sut2, observerFactory);
    assertTrue(fsm2.isPerformed());
  }
}
