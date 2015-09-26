package com.github.dakusui.jcunit.examples.fsm.doublefsm;

import com.github.dakusui.jcunit.core.FactorField;
import com.github.dakusui.jcunit.core.JCUnit;
import com.github.dakusui.jcunit.examples.fsm.turnstile.Turnstile;
import com.github.dakusui.jcunit.examples.fsm.turnstile.TurnstileTest;
import com.github.dakusui.jcunit.fsm.FSMLevelsProvider;
import com.github.dakusui.jcunit.fsm.ScenarioSequence;
import com.github.dakusui.jcunit.fsm.Story;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JCUnit.class)
public class DoubleFSMTest {
  @FactorField(levelsProvider = FSMLevelsProvider.class)
  public Story<TurnstileTest.Spec, Turnstile> fsm1;

  @FactorField(levelsProvider = FSMLevelsProvider.class)
  public Story<TurnstileTest.Spec, Turnstile> fsm2;

  Turnstile sut1 = new Turnstile();
  Turnstile sut2 = new Turnstile();

  @Test
  public void test() throws Throwable {
    this.fsm1.perform(
        this,
        this.sut1,
        ScenarioSequence.Utils.createSimpleObserver("fsm1"));
    this.fsm2.perform(
        this,
        this.sut2,
        ScenarioSequence.Utils.createSimpleObserver("fsm2"));
  }
}
