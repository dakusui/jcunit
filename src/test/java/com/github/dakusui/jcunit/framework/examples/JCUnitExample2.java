package com.github.dakusui.jcunit.framework.examples;

import com.github.dakusui.jcunit.constraint.Constraint;
import com.github.dakusui.jcunit.constraint.ConstraintManagerBase;
import com.github.dakusui.jcunit.core.*;
import com.github.dakusui.jcunit.core.factor.FactorField;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.generators.IPO2TestCaseGenerator;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertThat;
import com.github.dakusui.jcunit.core.Param.Type;
import org.mockito.internal.matchers.LessThan;

@RunWith(JCUnit.class)
@TestCaseGeneration(
    generator = @Generator(
        value = IPO2TestCaseGenerator.class,
        params = {
            @Param(type = Type.Int, array = false, value = { "3" })
        }),
    constraint = @Constraint(
        value = JCUnitExample2.CM.class,
        params = {
        }))
public class JCUnitExample2 {
  @Rule
  public TestName name = new TestName();
  @FactorField(intLevels = { 0, 1, 2, -1, -2, 100, -100, 10000, -10000 })
  public int      a;
  @FactorField(intLevels = { 0, 1, 2, -1, -2, 100, -100, 10000, -10000 })
  public int      b;
  @FactorField(intLevels = { 0, 1, 2, -1, -2, 100, -100, 10000, -10000 })
  public int      c;

  @Test
  public void test() {
    System.out.println(String.format("%s", name.getMethodName()));
    QuadraticEquationResolver.Solutions s = new QuadraticEquationResolver(a, b,
        c).resolve();
    assertThat(String.format("(a,b,c)=(%d,%d,%d)", a, b, c),
        a * s.x1 * s.x1 + b * s.x1 + c, new LessThan<Double>(0.01));
    assertThat(String.format("(a,b,c)=(%d,%d,%d)", a, b, c),
        a * s.x2 * s.x2 + b * s.x2 + c, new LessThan<Double>(0.01));
  }

  public static class CM extends ConstraintManagerBase {

    @Override
    public boolean check(Tuple tuple) {
      if (!tuple.containsKey("a") || !tuple.containsKey("b") || !tuple
          .containsKey("c")) {
        return true;
      }
      int a = (Integer) tuple.get("a");
      int b = (Integer) tuple.get("b");
      int c = (Integer) tuple.get("c");
      return a != 0 && b * b - 4 * c * a >= 0;
    }
  }
}
