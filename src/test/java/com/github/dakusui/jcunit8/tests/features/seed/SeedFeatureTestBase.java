package com.github.dakusui.jcunit8.tests.features.seed;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.Parameter;
import com.github.dakusui.jcunit8.runners.junit4.JCUnit8;
import com.github.dakusui.jcunit8.runners.junit4.annotations.Condition;
import com.github.dakusui.jcunit8.runners.junit4.annotations.ConfigureWith;
import com.github.dakusui.jcunit8.runners.junit4.annotations.From;
import com.github.dakusui.jcunit8.runners.junit4.annotations.ParameterSource;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static java.util.Arrays.asList;

@RunWith(JCUnit8.class)
public class SeedFeatureTestBase {
  static final List<Tuple> testCases = Collections.synchronizedList(new LinkedList<>());

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

  @Test
  public void test(
      @From("a") int a,
      @From("b") int b,
      @From("c") int c
  ) {
    String msg = String.format("a=%d,b=%d,c=%d%n", a, b, c);
    System.out.print(msg);
    testCases.add(Tuple.builder()
        .put("a", a)
        .put("b", b)
        .put("c", c)
        .build());
  }

  @ConfigureWith(TestConfig.SeedNone$NegativeTestEnabled.class)
  public static class T00 extends SeedFeatureTestBase {
    // Ca
    @Condition(constraint = true)
    public boolean aIsEqualToB(
        @From("a") int a,
        @From("b") int b
    ) {
      return a == b;
    }

    // Cb
    @Condition(constraint = true)
    public boolean bIsNotEqualToC(
        @From("b") int b,
        @From("c") int c
    ) {
      return b != c;
    }
  }

  @ConfigureWith(TestConfig.SeedSa$NegativeTestDisabled.class)
  public static class T01 extends SeedFeatureTestBase {
    // Ca
    @Condition(constraint = true)
    public boolean aIsEqualToB(
        @From("a") int a,
        @From("b") int b
    ) {
      return a == b;
    }

  }

  @ConfigureWith(TestConfig.SeedSa$NegativeTestEnabled.class)
  public static class T02 extends SeedFeatureTestBase {
    // Ca
    @Condition(constraint = true)
    public boolean aIsEqualToB(
        @From("a") int a,
        @From("b") int b
    ) {
      return a == b;
    }
  }

  @ConfigureWith(TestConfig.SeedSa$NegativeTestEnabled.class)
  public static class T03 extends SeedFeatureTestBase {
    // Cb
    @Condition(constraint = true)
    public boolean bIsNotEqualToC(
        @From("b") int b,
        @From("c") int c
    ) {
      return b != c;
    }
  }

  @ConfigureWith(TestConfig.SeedSaAndSb$NegativeEnabled.class)
  public static class T04 extends SeedFeatureTestBase {
    // Ca
    @Condition(constraint = true)
    public boolean aIsEqualToB(
        @From("a") int a,
        @From("b") int b
    ) {
      return a == b;
    }

    // Cb
    @Condition(constraint = true)
    public boolean bIsNotEqualToC(
        @From("b") int b,
        @From("c") int c
    ) {
      return b != c;
    }
  }

  @ConfigureWith(TestConfig.SeedSb$NegativeEnabled.class)
  public static class T05 extends SeedFeatureTestBase {
    // Cb
    @Condition(constraint = true)
    public boolean bIsNotEqualToC(
        @From("b") int b,
        @From("c") int c
    ) {
      return b != c;
    }
  }
}
