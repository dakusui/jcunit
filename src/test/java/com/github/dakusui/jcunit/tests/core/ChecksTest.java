package com.github.dakusui.jcunit.tests.core;

import com.github.dakusui.jcunit.core.Checks;
import org.junit.Test;

public class ChecksTest {
  @Test(expected = NullPointerException.class)
  public void testCheckNotNullMethod1() {
    Checks.checknotnull(null);
  }

  @Test(expected = NullPointerException.class)
  public void testCheckNotNullMethod2() {
    Checks.checknotnull(null, "hello:%s", "b");
  }

  @Test(expected = NullPointerException.class)
  public void testCheckNotNullMethod3() {
    Checks.checknotnull(null, null);
  }

  @Test(expected = IllegalStateException.class)
  public void testCheckCondMethod1() {
    Checks.checkcond(false);
  }

  @Test(expected = IllegalStateException.class)
  public void testCheckCondMethod2() {
    Checks.checkcond(false, "hello:%s", "a");
  }

  @Test(expected = IllegalStateException.class)
  public void testCheckCondMethod3() {
    Checks.checkcond(false, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCheckParamMethod1() {
    Checks.checkparam(false);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCheckParamMethod2() {
    Checks.checkparam(false, "hello:%s", "c");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCheckParamMethod3() {
    Checks.checkparam(false, null);
  }
}
