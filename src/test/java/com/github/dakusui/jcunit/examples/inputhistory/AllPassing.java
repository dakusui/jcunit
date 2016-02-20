package com.github.dakusui.jcunit.examples.inputhistory;

import com.github.dakusui.jcunit.examples.models.modifiedmealymachine.CorrectSpec;
import com.github.dakusui.jcunit.examples.models.modifiedmealymachine.ModifiedMealyMachine;
import com.github.dakusui.jcunit.fsm.*;
import com.github.dakusui.jcunit.runners.standard.JCUnit;
import com.github.dakusui.jcunit.runners.standard.annotations.FactorField;
import com.github.dakusui.jcunit.testutils.UTUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.github.dakusui.jcunit.fsm.SUTFactory.Base.$;

@RunWith(JCUnit.class)
public class AllPassing {
  @FactorField(levelsProvider = FSMLevelsProvider.class)
  public Story<ModifiedMealyMachine, CorrectSpec> paramstory;

  @Before
  public void before() {
    UTUtils.configureStdIOs();
  }

  @Test
  public void test() {
    int[] ii = new int[0];
    FSMUtils.performStory(
        this,
        "paramstory",
        ////
        // Illustrating how to access constructor with various types of parameters.
        new SUTFactory.Simple<ModifiedMealyMachine>(
            ModifiedMealyMachine.class,
            $(String.class, "prefix").as("method@param-0"),
            $(int.class, 1),
            $(int[].class, ii)
        ),
        new ScenarioSequence.Observer.Factory.ForSimple(UTUtils.stdout())
    );
  }
}
