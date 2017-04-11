package com.github.dakusui.jcunit.tests.modules.runners;

import com.github.dakusui.jcunit.runners.standard.CompositeFrameworkMethod;
import com.github.dakusui.jcunit.runners.standard.FrameworkMethodUtils;
import org.junit.Test;
import org.junit.runners.model.FrameworkMethod;

import static org.junit.Assert.assertEquals;

public class CompositeFrameworkMethodTest {
  @Test
  public void testOr0() throws Throwable {
    FrameworkMethod m = new CompositeFrameworkMethod.Builder(CompositeFrameworkMethod.Mode.Or)
        .build();
    assertEquals(false, m.invokeExplosively(this));
  }

  @Test
  public void testOr1() throws Throwable {
    FrameworkMethod m = new CompositeFrameworkMethod.Builder(CompositeFrameworkMethod.Mode.Or)
        .addMethod(false, createFrameworkMethod(true))
        .build();
    assertEquals(true, m.invokeExplosively(this));
  }

  @Test
  public void testOr2_bothFalse() throws Throwable {
    FrameworkMethod m = new CompositeFrameworkMethod.Builder(CompositeFrameworkMethod.Mode.Or)
        .addMethod(true, createFrameworkMethod(true))
        .addMethod(false, createFrameworkMethod(false))
        .build();
    assertEquals(false, m.invokeExplosively(this));
  }

  @Test
  public void testOr2_oneFalse() throws Throwable {
    FrameworkMethod m = new CompositeFrameworkMethod.Builder(CompositeFrameworkMethod.Mode.Or)
        .addMethod(false, createFrameworkMethod(true))
        .addMethod(false, createFrameworkMethod(false))
        .build();
    assertEquals(true, m.invokeExplosively(this));
  }


  @Test
  public void testAnd0() throws Throwable {
    FrameworkMethod m = new CompositeFrameworkMethod.Builder(CompositeFrameworkMethod.Mode.And)
        .addMethod(false, createFrameworkMethod(true))
        .build();
    assertEquals(true, m.invokeExplosively(this));
  }

  @Test
  public void testAnd1() throws Throwable {
    FrameworkMethod m = new CompositeFrameworkMethod.Builder(CompositeFrameworkMethod.Mode.And)
        .addMethod(false, createFrameworkMethod(true))
        .build();
    assertEquals(true, m.invokeExplosively(this));
  }

  @Test
  public void testAnd2() throws Throwable {
    FrameworkMethod m = new CompositeFrameworkMethod.Builder(CompositeFrameworkMethod.Mode.And)
        .addMethod(false, createFrameworkMethod(true))
        .addMethod(false, createFrameworkMethod(false))
        .build();
    assertEquals(false, m.invokeExplosively(this));
  }

  @Test
  public void testAnd2_negate() throws Throwable {
    FrameworkMethod m = new CompositeFrameworkMethod.Builder(CompositeFrameworkMethod.Mode.And)
        .addMethod(true, createFrameworkMethod(true))
        .addMethod(false, createFrameworkMethod(true))
        .build();
    assertEquals(false, m.invokeExplosively(this));
  }

  FrameworkMethod createFrameworkMethod(final boolean value) {
    return new FrameworkMethod(FrameworkMethodUtils.JCUnitFrameworkMethod.DUMMY_METHOD) {
      @Override
      public Object invokeExplosively(Object target, final Object... params) {
        return value;
      }
    };
  }
}
