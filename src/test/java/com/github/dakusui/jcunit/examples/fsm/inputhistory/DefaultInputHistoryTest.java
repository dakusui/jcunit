package com.github.dakusui.jcunit.examples.fsm.inputhistory;

import com.github.dakusui.jcunit.annotations.FactorField;
import com.github.dakusui.jcunit.core.JCUnit;
import com.github.dakusui.jcunit.fsm.*;
import com.github.dakusui.jcunit.ututils.Metatest;
import com.github.dakusui.jcunit.ututils.UTUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.github.dakusui.jcunit.fsm.SUTFactory.Base.$;
import static com.github.dakusui.jcunit.fsm.SUTFactory.Base.INT_ARRAY_TYPE;

public class DefaultInputHistoryTest {
  @Test
  public void testAllPassing() {
    new AllPassing().runTests();
  }

  @Test
  public void testFailing() {
    new Failing().runTests();
  }

  @RunWith(JCUnit.class)
  public static class AllPassing extends Metatest {
    public AllPassing() {
      super(14, 0, 0);
    }

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
              $(INT_ARRAY_TYPE, ii)
          ).addCollector(new InputHistory.Collector.Default("method")),
          new ScenarioSequence.Observer.Factory.ForSimple(UTUtils.stdout())
      );
    }
  }

  @RunWith(JCUnit.class)
  public static class Failing extends Metatest {
    public Failing() {
      super(14, 8, 0);
    }

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
              $(Integer.TYPE, 1),
              $(INT_ARRAY_TYPE, ii)
          ).addCollector(new InputHistory.Collector.Default("method")),
          new ScenarioSequence.Observer.Factory.ForSimple(UTUtils.stdout())
      );
    }
  }
}
