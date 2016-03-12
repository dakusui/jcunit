package com.github.dakusui.jcunit.tests.plugins.levelsproviders;

import com.github.dakusui.jcunit.plugins.levelsproviders.LevelsProvider;
import com.github.dakusui.jcunit.plugins.levelsproviders.SimpleLevelsProvider;
import com.github.dakusui.jcunit.runners.core.RunnerContext;
import com.github.dakusui.jcunit.runners.standard.JCUnit;
import com.github.dakusui.jcunit.runners.standard.annotations.FactorField;
import com.github.dakusui.jcunit.runners.standard.annotations.Value;
import com.github.dakusui.jcunit.testutils.UTUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class SimpleLevelsProviderTest {
  static public class Struct {
    int a;
    int b;
    int c;

    Struct(int a, int b, int c) {
      this.a = a;
      this.b = b;
      this.c = c;
    }

    @Override
    public int hashCode() {
      return this.toString().hashCode();
    }

    @Override
    public boolean equals(Object another) {
      return another instanceof Struct && super.equals(another);
    }

    @Override
    public String toString() {
      return String.format("(%d,%d,%d)", a, b, c);
    }

  }

  public static class SimpleLevelsProvider1 extends SimpleLevelsProvider {
    @Override
    protected Struct[] values() {
      return new Struct[] {
          new Struct(1, 1, 1),
          new Struct(2, 2, 2),
          new Struct(3, 3, 3)
      };
    }
  }

  @RunWith(JCUnit.class)
  public static class VerifySimpleLevelsProvider1 {
    @FactorField(levelsProvider = SimpleLevelsProvider1.class)
    public Struct struct;

    @BeforeClass
    public static void beforeAll() {
      UTUtils.configureStdIOs();
    }

    @Test
    public void testSimpleLevelsProvider1() {
      UTUtils.stdout().println(this.struct);
    }
  }

  public static class ConfiguredLevelsProvider extends LevelsProvider.Base {
    private final int count;

    public ConfiguredLevelsProvider(
        @Param(source = Param.Source.CONFIG) int count,
        @Param(source = Param.Source.CONTEXT, contextKey = RunnerContext.Key.TEST_CLASS) Class<?> testClass
    ) {
      assertThat(count, is(10));
      assertThat(testClass.getCanonicalName(), is(VerifyConfiguredLevelsProvider.class.getCanonicalName()));
      this.count = count;
    }

    @Override
    public int size() {
      return this.count;
    }

    @Override
    public Struct get(int n) {
      return new Struct(n, n, n);
    }
  }

  @RunWith(JCUnit.class)
  public static class VerifyConfiguredLevelsProvider {
    @FactorField(
        levelsProvider = ConfiguredLevelsProvider.class
        , args = @Value("10")
    )
    public Struct struct;

    @BeforeClass
    public static void beforeAll() {
      UTUtils.configureStdIOs();
    }

    @Test
    public void testSimpleLevelsProvider1() {
      UTUtils.stdout().println(this.struct);
    }
  }

  @Test
  public void testSimpleLevelsProvider1() {
    Result result = JUnitCore.runClasses(VerifySimpleLevelsProvider1.class);
    assertEquals(true, result.wasSuccessful());
    assertEquals(3, result.getRunCount());
    assertEquals(0, result.getIgnoreCount());
    assertEquals(0, result.getFailureCount());
  }

  @Test
  public void testConfiguredLevelsProvider() {
    Result result = JUnitCore.runClasses(VerifyConfiguredLevelsProvider.class);
    assertEquals(true, result.wasSuccessful());
    assertEquals(10, result.getRunCount());
    assertEquals(0, result.getIgnoreCount());
    assertEquals(0, result.getFailureCount());
  }
}
