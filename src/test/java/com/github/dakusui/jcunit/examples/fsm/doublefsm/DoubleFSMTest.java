package com.github.dakusui.jcunit.examples.fsm.doublefsm;

import com.github.dakusui.jcunit.core.FactorField;
import com.github.dakusui.jcunit.core.JCUnit;
import com.github.dakusui.jcunit.core.Param;
import com.github.dakusui.jcunit.examples.fsm.turnstile.Turnstile;
import com.github.dakusui.jcunit.examples.fsm.turnstile.TurnstileTest;
import com.github.dakusui.jcunit.fsm.FSM;
import com.github.dakusui.jcunit.fsm.FSMLevelsProvider;
import com.github.dakusui.jcunit.fsm.FSMUtils;
import com.github.dakusui.jcunit.fsm.ScenarioSequence;
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
    FSMUtils.performScenarioSequence(this.setUp1, sut, ScenarioSequence.SILENT_REPORTER);
    FSMUtils.performScenarioSequence(this.setUp2, sut, ScenarioSequence.SILENT_REPORTER);
    FSMUtils.performScenarioSequence(this.main1, sut, ScenarioSequence.SIMPLE_REPORTER);
    FSMUtils.performScenarioSequence(this.main2, sut, ScenarioSequence.SIMPLE_REPORTER);
  }
}
