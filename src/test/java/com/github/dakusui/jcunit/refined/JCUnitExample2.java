package com.github.dakusui.jcunit.refined;

import com.github.dakusui.jcunit.constraints.ConstraintManagerBase;
import com.github.dakusui.jcunit.core.Generator;
import com.github.dakusui.jcunit.core.JCUnit;
import com.github.dakusui.jcunit.core.Tuple;
import com.github.dakusui.jcunit.core.factor.FactorField;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.matchers.LessThan;

import static org.junit.Assert.assertThat;

@RunWith(JCUnit.class)
@Generator(constraintManager = JCUnitExample2.CM.class)
public class JCUnitExample2 {
  @FactorField//(intLevels = {100})
  public int a;
  @FactorField//(intLevels = {0})
  public int b;
  @FactorField
  public int c;

  @Test
  public void test() {
    QuadraticEquationResolver.Solutions s = new QuadraticEquationResolver(a, b,
        c).resolve();
    assertThat(a * s.x1 * s.x1 + b * s.x1 + c, new LessThan<Double>(0.01));
    assertThat(a * s.x2 * s.x2 + b * s.x2 + c, new LessThan<Double>(0.01));
  }

  public static class CM extends ConstraintManagerBase {

    @Override public boolean check(Tuple tuple) {
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
