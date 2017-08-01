package com.github.dakusui.jcunit8.tests.features.seed;

import com.github.dakusui.jcunit8.factorspace.Parameter;
import com.github.dakusui.jcunit8.runners.junit4.JCUnit8;
import com.github.dakusui.jcunit8.runners.junit4.annotations.Condition;
import com.github.dakusui.jcunit8.runners.junit4.annotations.ConfigureWith;
import com.github.dakusui.jcunit8.runners.junit4.annotations.From;
import com.github.dakusui.jcunit8.runners.junit4.annotations.ParameterSource;
import org.junit.Test;
import org.junit.runner.RunWith;

import static java.util.Arrays.asList;
import static org.junit.Assert.fail;

@RunWith(JCUnit8.class)
public class SeedFeatureTestBase {
  interface Ca {
    @Condition(constraint = true)
    default boolean aIsNotEqualToB(
        @From("a") int a,
        @From("b") int b
    ) {
      return a != b;
    }
  }

  interface Cb {
    @Condition(constraint = true)
    default boolean bIsNotEqualToC(
        @From("b") int b,
        @From("c") int c
    ) {
      return b != c;
    }
  }

  @ParameterSource
  public Parameter.Simple.Factory<Integer> a() {
    return Parameter.Simple.Factory.of(asList(0, 1));
  }

  @ParameterSource
  public Parameter.Simple.Factory<Integer> b() {
    return Parameter.Simple.Factory.of(asList(0, 1));
  }

  @ParameterSource
  public Parameter.Simple.Factory<Integer> c() {
    return Parameter.Simple.Factory.of(asList(0, 1));
  }

  @ConfigureWith(TestConfig.ConfigWithSa.class)
  public static class SeedForSimpleParametersWithConstraint extends SeedFeatureTestBase {
    @Condition(constraint = true)
    public boolean bIsNotEqualToC(
        @From("b") int b,
        @From("c") int c
    ) {
      return b != c;
    }


    @Test
    public void test(
        @From("a") int a,
        @From("b") int b,
        @From("c") int c
    ) {
      String msg = String.format("a=%d,b=%d,c=%d%n", a, b, c);
      System.out.print(msg);
      fail(msg);
    }
  }

  @ConfigureWith(TestConfig.ConfigWithSaAndSb.class)
  public static class SeedsForSimpleParameters extends SeedFeatureTestBase {

    @Test
    public void test(
        @From("a") int a,
        @From("b") int b,
        @From("c") int c
    ) {
      String msg = String.format("a=%d,b=%d,c=%d%n", a, b, c);
      System.out.print(msg);
      fail(msg);
    }
  }

  public static class SeedsForSimpleParametersWithConstraint extends SeedsForSimpleParameters {
    @Condition(constraint = true)
    public boolean ifBIs0ThenBIsNotEqualToC(
        @From("b") int b,
        @From("c") int c
    ) {
      return b != 0 || b != c;
    }
  }
}
