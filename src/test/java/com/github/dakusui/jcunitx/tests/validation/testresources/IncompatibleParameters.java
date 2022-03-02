package com.github.dakusui.jcunitx.tests.validation.testresources;

import com.github.dakusui.jcunitx.metamodel.parameters.SimpleParameter;
import com.github.dakusui.jcunitx.runners.junit4.JCUnit8;
import com.github.dakusui.jcunitx.runners.junit4.JUnit4_13Workaround;
import com.github.dakusui.jcunitx.runners.junit4.annotations.From;
import com.github.dakusui.jcunitx.runners.junit4.annotations.ParameterSource;
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
    public SimpleParameter.Descriptor<Integer> a() {
      return SimpleParameter.Descriptor.of(singletonList(100));
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
    public SimpleParameter.Descriptor<Integer> a() {
      return SimpleParameter.Descriptor.of(singletonList(null));
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
    public SimpleParameter.Descriptor<Integer> a() {
      return SimpleParameter.Descriptor.of(singletonList(1));
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
    public SimpleParameter.Descriptor<Integer> a() {
      return SimpleParameter.Descriptor.of(singletonList(null));
    }

    @SuppressWarnings("unused") // 'a' is used by test
    @Test
    public void testMethod(@From("a") int a) {
    }
  }

}
