package com.github.dakusui.jcunit.tests.generators;

import com.github.dakusui.jcunit.constraint.constraintmanagers.ConstraintManagerBase;
import com.github.dakusui.jcunit.core.*;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.tuples.TupleUtils;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;
import com.github.dakusui.jcunit.generators.SimpleTupleGenerator;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

public class ForSimpleTupleGeneratorTest {
  @RunWith(JCUnit.class)
  @TupleGeneration(
      generator = @Generator(SimpleTupleGenerator.class),
      constraint = @Constraint(TestClass1.CM.class)
  )
  public static class TestClass1 {
    public static class CM extends ConstraintManagerBase {
      @Override
      public boolean check(Tuple tuple) throws UndefinedSymbol {
        if (!tuple.containsKey("f1")) throw new UndefinedSymbol();
        return !"hello".equals(tuple.get("f1"));
      }
    }
    @SuppressWarnings("unused")
    @FactorField(stringLevels = {"Hello", "world", "hello"})
    public String f1;
    @SuppressWarnings("unused")
    @FactorField(stringLevels = {"X", "Y", "Z"})
    public String f2;

    @Test
    public void test() {
      System.out.println(this);
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
    assertEquals(5, result.getRunCount());
  }
}
