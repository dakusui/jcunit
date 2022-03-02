package com.github.dakusui.jcunitx.tests.features.seed;

import com.github.dakusui.jcunitx.core.AArray;
import com.github.dakusui.jcunitx.metamodel.parameters.SimpleParameter;
import com.github.dakusui.jcunitx.runners.junit4.JCUnit8;
import com.github.dakusui.jcunitx.runners.junit4.JUnit4_13Workaround;
import com.github.dakusui.jcunitx.runners.junit4.annotations.Condition;
import com.github.dakusui.jcunitx.runners.junit4.annotations.ConfigureWith;
import com.github.dakusui.jcunitx.runners.junit4.annotations.From;
import com.github.dakusui.jcunitx.runners.junit4.annotations.ParameterSource;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static java.util.Arrays.asList;

@RunWith(JCUnit8.class)
public abstract class SeedFeatureTestBase extends JUnit4_13Workaround {
  static final List<AArray> testCases = Collections.synchronizedList(new LinkedList<>());

  @ParameterSource
  public SimpleParameter.Descriptor<Integer> a() {
    return SimpleParameter.Descriptor.of(asList(0, 1));
  }

  @ParameterSource
  public SimpleParameter.Descriptor<Integer> b() {
    return SimpleParameter.Descriptor.of(asList(0, 1));
  }

  @ParameterSource
  public SimpleParameter.Descriptor<Integer> c() {
    return SimpleParameter.Descriptor.of(asList(0, 1));
  }

  @Test
  public void test(
      @From("a") int a,
      @From("b") int b,
      @From("c") int c
  ) {
    String msg = String.format("a=%d,b=%d,c=%d%n", a, b, c);
    System.out.print(msg);
    testCases.add(AArray.builder()
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
