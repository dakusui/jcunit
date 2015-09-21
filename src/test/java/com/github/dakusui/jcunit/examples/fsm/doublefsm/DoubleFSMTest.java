package com.github.dakusui.jcunit.examples.fsm.doublefsm;

import com.github.dakusui.jcunit.core.FactorField;
import com.github.dakusui.jcunit.core.JCUnit;
import com.github.dakusui.jcunit.examples.fsm.turnstile.Turnstile;
import com.github.dakusui.jcunit.examples.fsm.turnstile.TurnstileTest;
import com.github.dakusui.jcunit.fsm.FSMContext;
import com.github.dakusui.jcunit.fsm.FSMLevelsProvider;
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
        new FSMContext.Builder().add("fsm1", this.fsm1).build(),
        this.sut1,
        Story.SIMPLE_OBSERVER);
    this.fsm2.perform(
        new FSMContext.Builder().add("fsm2", this.fsm2).build(),
        this.sut2,
        Story.SIMPLE_OBSERVER);
  }
}
