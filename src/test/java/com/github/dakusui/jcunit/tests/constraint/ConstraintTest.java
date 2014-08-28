package com.github.dakusui.jcunit.tests.constraint;

import com.github.dakusui.jcunit.constraint.ConstraintManager;
import com.github.dakusui.jcunit.constraint.ConstraintObserver;
import com.github.dakusui.jcunit.constraint.constraintmanagers.TypedConstraintManager;
import com.github.dakusui.jcunit.core.FactorField;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.FactorLoader;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ConstraintTest {
  public static class TestClass {
    @FactorField(intLevels = {1024})
    public int f1;
  }

  @Test
  public void testConstraintManager() throws UndefinedSymbol {
    ConstraintManager manager = new TypedConstraintManager<TestClass>() {
      @Override protected boolean check(TestClass o, Tuple tuple)
          throws UndefinedSymbol {
        return true;
      }

      @Override protected List<TestClass> getViolationTestObjects() {
        List<TestClass> ret = new LinkedList<TestClass>();
        TestClass t = new TestClass();
        t.f1 = 128;
        ret.add(t);
        return ret;
      }
    };

    manager.setFactors(loadFactorsFromClass(TestClass.class));
    assertEquals(1, manager.getFactors().size());
    assertEquals(1, manager.getFactors().get("f1").levels.size());
    assertEquals(1024, manager.getFactors().get("f1").levels.get(0));

    ConstraintObserver observer = new ConstraintObserver() {
      @Override public void implicitConstraintFound(Tuple constraint) {
      }
    };
    manager.addObserver(observer);
    assertEquals(1, manager.observers().size());

    manager.removeObservers(observer);
    assertEquals(0, manager.observers().size());

    assertEquals(true,
        manager.check(new Tuple.Builder().put("f1", 100).build()));


    assertEquals(128, manager.getViolations().get(0).get("f1"));
  }

  private Factors loadFactorsFromClass(Class<?> testClass) {
    Factors.Builder b = new Factors.Builder();
    for (Field f : Utils.getAnnotatedFields(testClass, FactorField.class)) {
      Factor factor = new FactorLoader(f).getFactor();
      b.add(factor);
    }
    return b.build();
  }
}
