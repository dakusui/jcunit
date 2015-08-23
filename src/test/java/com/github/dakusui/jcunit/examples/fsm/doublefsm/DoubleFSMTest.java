package com.github.dakusui.jcunit.examples.fsm.doublefsm;

import com.github.dakusui.jcunit.core.FactorField;
import com.github.dakusui.jcunit.core.JCUnit;
import com.github.dakusui.jcunit.core.Param;
import com.github.dakusui.jcunit.examples.fsm.turnstile.Turnstile;
import com.github.dakusui.jcunit.examples.fsm.turnstile.TurnstileTest;
import com.github.dakusui.jcunit.fsm.*;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JCUnit.class)
public class DoubleFSMTest {
  @FactorField(
      levelsProvider = FSMLevelsProvider.class,
      providerParams = {
          @Param("turnstileFSM1"),
          @Param("setUp"),
      }
  )
  public ScenarioSequence<Turnstile> setUp1;

  @FactorField(
      levelsProvider = FSMLevelsProvider.class,
      providerParams = {
          @Param("turnstileFSM1"),
          @Param("main"),
      }
  )
  public ScenarioSequence<Turnstile> main1;

  @FactorField(
      levelsProvider = FSMLevelsProvider.class,
      providerParams = {
          @Param("turnstileFSM2"),
          @Param("setUp"),
      }
  )
  public ScenarioSequence<Turnstile> setUp2;

  @FactorField(
      levelsProvider = FSMLevelsProvider.class,
      providerParams = {
          @Param("turnstileFSM2"),
          @Param("main"),
      }
  )
  public ScenarioSequence<Turnstile> main2;

  public static FSM<Turnstile> turnstileFSM1() {
    return FSMUtils.createFSM(TurnstileTest.Spec.class, 2);
  }

  public static FSM<Turnstile> turnstileFSM2() {
    return FSMUtils.createFSM(TurnstileTest.Spec.class, 2);
  }

  @Test
  public void test() throws Throwable {
    Turnstile sut = new Turnstile();
    FSMUtils.performScenarioSequence(null, ScenarioSequence.Type.setUp, this.setUp1, sut, Story.SILENT_OBSERVER);
    FSMUtils.performScenarioSequence(null, ScenarioSequence.Type.setUp, this.setUp2, sut, Story.SILENT_OBSERVER);
    FSMUtils.performScenarioSequence(null, ScenarioSequence.Type.main, this.main1, sut, Story.SIMPLE_OBSERVER);
    FSMUtils.performScenarioSequence(null, ScenarioSequence.Type.main, this.main2, sut, Story.SIMPLE_OBSERVER);
  }
}
