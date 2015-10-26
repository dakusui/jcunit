package com.github.dakusui.jcunit.examples.theories;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;
import com.github.dakusui.jcunit.plugins.constraintmanagers.ConstraintManagerBase;
import com.github.dakusui.jcunit.plugins.generators.IPO2TupleGenerator;
import com.github.dakusui.jcunit.runners.standard.annotations.Constraint;
import com.github.dakusui.jcunit.runners.standard.annotations.Generator;
import com.github.dakusui.jcunit.runners.standard.annotations.Value;
import com.github.dakusui.jcunit.runners.theories.TheoriesWithJCUnit;
import com.github.dakusui.jcunit.runners.theories.annotations.Name;
import com.github.dakusui.jcunit.runners.theories.annotations.TupleGeneration;
import com.github.dakusui.jcunit.ututils.UTUtils;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.FromDataPoints;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

@RunWith(TheoriesWithJCUnit.class)
public class TheoriesExample2 {
  @DataPoints("posInt")
  public static int[] positiveIntegers() {
    return new int[] {
        1, 2, 3
    };
  }

  @DataPoints("negInt")
  public static int[] negativeIntegers() {
    return new int[] {
        -1, -2, -3
    };
  }

  @DataPoints("posLong")
  public static long[] posLongs() {
    return new long[] {
        100, 200, 300
    };
  }

  @DataPoints("negLong")
  public static long[] negLongs() {
    return new long[] {
        -100, -200, -300
    };
  }

  public static class CM extends ConstraintManagerBase {
    @Override
    public boolean check(Tuple tuple) throws UndefinedSymbol {
      if (!tuple.containsKey("a"))
        throw new UndefinedSymbol("a");
      if (!tuple.containsKey("b"))
        throw new UndefinedSymbol("b");
      return (Integer) tuple.get("a") + (Integer) tuple.get("b") == 0;
    }
  }

  @Theory
  @TupleGeneration(
      generator = @Generator(value = IPO2TupleGenerator.class, params = { @Value("3") }),
      constraint = @Constraint(CM.class)
  )
  public void test1(
      @FromDataPoints("posInt") @Name("a") int a,
      @FromDataPoints("negInt") @Name("b") int b,
      @FromDataPoints("posLong") @Name("c") long c,
      @FromDataPoints("negLong") @Name("d") long d
  ) throws Exception {
    UTUtils.stdout().printf("a=%s, b=%s, c=%d, d=%d%n", a, b, c, d);
  }

  @Theory
  @TupleGeneration(generator = @Generator(value = IPO2TupleGenerator.class, params = { @Value("2") }))
  public void test2(
      @FromDataPoints("posInt") int a,
      @FromDataPoints("negInt") int b
  ) throws Exception {
    UTUtils.stdout().printf("a=%s, b=%s%n", a, b);
  }
}
