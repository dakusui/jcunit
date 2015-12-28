package com.github.dakusui.jcunit.examples.inputhistory;

import com.github.dakusui.jcunit.examples.models.modifiedmealymachine.ExampleM4;
import com.github.dakusui.jcunit.fsm.*;
import com.github.dakusui.jcunit.runners.standard.JCUnit;
import com.github.dakusui.jcunit.runners.standard.annotations.FactorField;
import com.github.dakusui.jcunit.testutils.UTUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.github.dakusui.jcunit.fsm.SUTFactory.Base.$;

public class DefaultInputHistoryExample {

  @RunWith(JCUnit.class)
  public static class AllPassing {
    @FactorField(levelsProvider = FSMLevelsProvider.class)
    public Story<ExampleM4.Sut, ExampleM4.CorrectSpec> paramstory;

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
          new SUTFactory.Simple<ExampleM4.Sut>(
              ExampleM4.Sut.class,
              $(String.class, "prefix"),
              $(Integer.TYPE, 1),
              $(int[].class, ii)
          ).addCollector(new InputHistory.Collector.Default("method")),
          new ScenarioSequence.Observer.Factory.ForSimple(UTUtils.stdout())
      );
    }
  }

  @RunWith(JCUnit.class)
  public static class Failing {
    @FactorField(levelsProvider = FSMLevelsProvider.class)
    public Story<ExampleM4.Sut, ExampleM4.IncorrectSpec> paramstory;

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
          new SUTFactory.Simple<ExampleM4.Sut>(
              ExampleM4.Sut.class,
              $(String.class, "prefix"),
              $(int.class, 1),
              $(int[].class, ii)
          ).addCollector(new InputHistory.Collector.Default("method")),
          new ScenarioSequence.Observer.Factory.ForSimple(UTUtils.stdout())
      );
    }
  }
}
