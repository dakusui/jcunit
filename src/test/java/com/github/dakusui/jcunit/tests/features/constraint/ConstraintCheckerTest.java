package com.github.dakusui.jcunit.tests.features.constraint;

import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.reflect.ReflectionUtils;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;
import com.github.dakusui.jcunit.plugins.constraints.ConstraintChecker;
import com.github.dakusui.jcunit.plugins.constraints.TypedConstraintChecker;
import com.github.dakusui.jcunit.runners.standard.JCUnit;
import com.github.dakusui.jcunit.runners.standard.annotations.Checker;
import com.github.dakusui.jcunit.runners.standard.annotations.FactorField;
import com.github.dakusui.jcunit.runners.standard.annotations.GenerateCoveringArrayWith;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ConstraintCheckerTest {
  private Factors loadFactorsFromClass(Class<?> testClass) {
    Factors.Builder b = new Factors.Builder();
    for (Field f : ReflectionUtils.getAnnotatedFields(testClass, FactorField.class)) {
      Factor factor = FactorField.FactorFactory.INSTANCE.createFromField(f);
      b.add(factor);
    }
    return b.build();
  }

  public static class TestClass {
    @FactorField(intLevels = { 1024 })
    public int f1;
  }

  @Test
  public void testConstraintManager() throws UndefinedSymbol {
    ConstraintChecker manager = new TypedConstraintChecker<TestClass>() {
      @Override
      protected boolean check(TestClass o, Tuple tuple)
          throws UndefinedSymbol {
        return true;
      }

      @Override
      protected List<TestClass> getViolationTestObjects() {
        List<TestClass> ret = new LinkedList<TestClass>();
        TestClass t = new TestClass();
        t.f1 = 128;
        ret.add(t);
        return ret;
      }
    };

    Factors factors = loadFactorsFromClass(TestClass.class);
    assertEquals(1, factors.size());
    assertEquals(1, factors.get("f1").levels.size());
    assertEquals(1024, factors.get("f1").levels.get(0));

    assertEquals(true,
        manager.check(new Tuple.Builder().put("f1", 100).build()));

    assertEquals(128, manager.getViolations().get(0).get("f1"));
  }

  public static class CM extends TypedConstraintChecker<TestClass2> {
    @Override
    protected boolean check(TestClass2 o, Tuple tuple) throws UndefinedSymbol {
      return false;
    }
  }

  @RunWith(JCUnit.class)
  @GenerateCoveringArrayWith(checker = @Checker(CM.class))
  public static class TestClass2 {
    @FactorField(intLevels = { 1, 2, 3 })
    public int f;
    /**
     * This field is used in tests reflectively.
     */
    @SuppressWarnings("unused")
    @FactorField(intLevels = { 1, 2, 3 })
    public int g;
  }

  @Test
  public void givenCMthatRejectsAnyTestCase$whenJCUnitGeneratesTestCase$thenWhatHappens() {
    Result result = JUnitCore.runClasses(TestClass2.class);
    assertEquals(1, result.getRunCount());
    assertEquals(false, result.wasSuccessful());
    assertEquals("No runnable methods", result.getFailures().get(0).getMessage());
  }
}
