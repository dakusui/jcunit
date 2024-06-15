package com.github.dakusui.jcunit8.tests.validation.testresources;

import com.github.dakusui.jcunit8.factorspace.Parameter.Simple;
import com.github.dakusui.jcunit8.runners.junit4.JUnit4_13Workaround;
import com.github.dakusui.jcunit8.runners.junit4.annotations.From;
import com.github.dakusui.jcunit8.runners.junit4.annotations.ParameterSource;
import org.junit.Test;
import org.junit.runner.RunWith;

import static java.util.Collections.singletonList;

public class IncompatibleParameters {
  /**
   * This is an "example" class, intended to be executed by a "real" test class.
   */
  @SuppressWarnings("NewClassNamingConvention")
  @RunWith(JCUnit8.class)
  public static class IncompatibleType extends JUnit4_13Workaround {
    @ParameterSource
    public Simple.Factory<Integer> a() {
      return Simple.Factory.of(singletonList(100));
    }

    @SuppressWarnings("unused") // 'a' is used by test
    @Test
    public void testMethod(@From("a") String a) {
    }
  }

  /**
   * This is an "example" class, intended to be executed by a "real" test class.
   */
  @SuppressWarnings("NewClassNamingConvention")
  @RunWith(JCUnit8.class)
  public static class CompatibleNullValue extends JUnit4_13Workaround {
    @ParameterSource
    public Simple.Factory<Integer> a() {
      return Simple.Factory.of(singletonList(null));
    }

    @SuppressWarnings("unused") // 'a' is used by test
    @Test
    public void testMethod(@From("a") Integer a) {
    }
  }

  /**
   * This is an "example" class, intended to be executed by a "real" test class.
   */
  @SuppressWarnings("NewClassNamingConvention")
  @RunWith(JCUnit8.class)
  public static class IncompatiblePrimitiveType extends JUnit4_13Workaround {
    @ParameterSource
    public Simple.Factory<Integer> a() {
      return Simple.Factory.of(singletonList(1));
    }

    @SuppressWarnings("unused") // 'a' is used by test
    @Test
    public void testMethod(@From("a") boolean a) {
    }
  }


  /**
   * This is an "example" class, intended to be executed by a "real" test class.
   */
  @SuppressWarnings("NewClassNamingConvention")
  @RunWith(JCUnit8.class)
  public static class IncompatibleNullValue extends JUnit4_13Workaround {
    @ParameterSource
    public Simple.Factory<Integer> a() {
      return Simple.Factory.of(singletonList(null));
    }

    @SuppressWarnings("unused") // 'a' is used by test
    @Test
    public void testMethod(@From("a") int a) {
    }
  }

}
