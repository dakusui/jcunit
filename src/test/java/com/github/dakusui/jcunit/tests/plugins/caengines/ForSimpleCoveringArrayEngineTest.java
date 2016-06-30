package com.github.dakusui.jcunit.tests.plugins.caengines;

import com.github.dakusui.jcunit.core.utils.Checks;
import com.github.dakusui.jcunit.plugins.constraints.ConstraintChecker;
import com.github.dakusui.jcunit.runners.standard.annotations.*;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.tuples.TupleUtils;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;
import com.github.dakusui.jcunit.plugins.caengines.SimpleCoveringArrayEngine;
import com.github.dakusui.jcunit.runners.standard.JCUnit;
import com.github.dakusui.jcunit.runners.standard.TestCaseUtils;
import com.github.dakusui.jcunit.testutils.UTUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

public class ForSimpleCoveringArrayEngineTest {
  @RunWith(JCUnit.class)
  @GenerateCoveringArrayWith(
      engine = @Generator(SimpleCoveringArrayEngine.class),
      checker = @Checker(TestClass1.CM.class)
  )
  public static class TestClass1 {
    public static class CM extends ConstraintChecker.Base {
      @Override
      public boolean check(Tuple tuple) throws UndefinedSymbol {
        Checks.checksymbols(tuple, "f1");
        return !"hello".equals(tuple.get("f1"));
      }
    }

    @Before
    public void configureStdIOs() {
      UTUtils.configureStdIOs();
    }

    @SuppressWarnings("unused")
    @FactorField(stringLevels = { "Hello", "world", "hello" })
    public String f1;
    @SuppressWarnings("unused")
    @FactorField(stringLevels = { "X", "Y", "Z" })
    public String f2;

    @Test
    public void test() {
      UTUtils.stdout().println(this);
    }

    @Override
    public String toString() {
      return TupleUtils.toString(TestCaseUtils.toTestCase(this));
    }
  }

  @Test
  public void test() {
    Result result = JUnitCore.runClasses(TestClass1.class);
    assertEquals(true, result.wasSuccessful());
    assertEquals(4, result.getRunCount());
  }
}
