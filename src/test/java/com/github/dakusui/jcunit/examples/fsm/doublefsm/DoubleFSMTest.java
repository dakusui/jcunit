package com.github.dakusui.jcunit.examples.fsm.doublefsm;

import com.github.dakusui.jcunit.core.FactorField;
import com.github.dakusui.jcunit.core.JCUnit;
import com.github.dakusui.jcunit.core.Param;
import com.github.dakusui.jcunit.examples.fsm.turnstile.Turnstile;
import com.github.dakusui.jcunit.examples.fsm.turnstile.TurnstileTest;
import com.github.dakusui.jcunit.fsm.FSM;
import com.github.dakusui.jcunit.fsm.FSMLevelsProvider;
import com.github.dakusui.jcunit.fsm.FSMUtils;
import com.github.dakusui.jcunit.fsm.Story;
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
  public Story<Turnstile> setUp1;

  @FactorField(
      levelsProvider = FSMLevelsProvider.class,
      providerParams = {
          @Param("turnstileFSM1"),
          @Param("main"),
      }
  )
  public Story<Turnstile> main1;

  @FactorField(
      levelsProvider = FSMLevelsProvider.class,
      providerParams = {
          @Param("turnstileFSM2"),
          @Param("setUp"),
      }
  )
  public Story<Turnstile> setUp2;

  @FactorField(
      levelsProvider = FSMLevelsProvider.class,
      providerParams = {
          @Param("turnstileFSM2"),
          @Param("main"),
      }
  )
  public Story<Turnstile> main2;

  public static FSM<Turnstile> turnstileFSM1() {
    return FSMUtils.createFSM(TurnstileTest.Spec.class, 2);
  }

  public static FSM<Turnstile> turnstileFSM2() {
    return FSMUtils.createFSM(TurnstileTest.Spec.class, 2);
  }

  @Test
  public void test() throws Throwable {
    Turnstile sut = new Turnstile();
    FSMUtils.performStory(this.setUp1, sut, Story.SILENT_REPORTER);
    FSMUtils.performStory(this.setUp2, sut, Story.SILENT_REPORTER);
    FSMUtils.performStory(this.main1, sut, Story.SIMPLE_REPORTER);
    FSMUtils.performStory(this.main2, sut, Story.SIMPLE_REPORTER);
  }
}
