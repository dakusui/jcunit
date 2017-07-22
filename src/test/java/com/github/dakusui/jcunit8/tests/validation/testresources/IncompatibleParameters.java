package com.github.dakusui.jcunit8.tests.validation.testresources;

import com.github.dakusui.jcunit8.factorspace.Parameter.Simple;
import com.github.dakusui.jcunit8.runners.junit4.JCUnit8;
import com.github.dakusui.jcunit8.runners.junit4.annotations.From;
import com.github.dakusui.jcunit8.runners.junit4.annotations.ParameterSource;
import org.junit.Test;
import org.junit.runner.RunWith;

import static java.util.Collections.singletonList;

public class IncompatibleParameters {
  @RunWith(JCUnit8.class)
  public static class IncompatibleType {
    @ParameterSource
    public Simple.Factory<Integer> a() {
      return Simple.Factory.of(singletonList(100));
    }

    @SuppressWarnings("unused") // 'a' is used by test
    @Test
    public void testMethod(@From("a") String a) {
    }
  }

  @RunWith(JCUnit8.class)
  public static class CompatibleNullValue {
    @ParameterSource
    public Simple.Factory<Integer> a() {
      return Simple.Factory.of(singletonList(null));
    }

    @SuppressWarnings("unused") // 'a' is used by test
    @Test
    public void testMethod(@From("a") Integer a) {
    }
  }

  @RunWith(JCUnit8.class)
  public static class IncompatiblePrimitiveType {
    @ParameterSource
    public Simple.Factory<Integer> a() {
      return Simple.Factory.of(singletonList(1));
    }

    @SuppressWarnings("unused") // 'a' is used by test
    @Test
    public void testMethod(@From("a") boolean a) {
    }
  }


  @RunWith(JCUnit8.class)
  public static class IncompatibleNullValue {
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
