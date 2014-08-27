package com.github.dakusui.jcunit.tests.core;

import com.github.dakusui.jcunit.core.Utils;
import org.junit.Test;

public class UtilsTest {
  @Test(expected = NullPointerException.class)
  public void testCheckNotNullMethod1() {
    Utils.checknotnull(null);
  }

  @Test(expected = NullPointerException.class)
  public void testCheckNotNullMethod2() {
    Utils.checknotnull(null, "hello:%s", "b");
  }

  @Test(expected = NullPointerException.class)
  public void testCheckNotNullMethod3() {
    Utils.checknotnull(null, null);
  }

  @Test(expected = IllegalStateException.class)
  public void testCheckCondMethod1() {
    Utils.checkcond(false);
  }

  @Test(expected = IllegalStateException.class)
  public void testCheckCondMethod2() {
    Utils.checkcond(false, "hello:%s", "a");
  }

  @Test(expected = IllegalStateException.class)
  public void testCheckCondMethod3() {
    Utils.checkcond(false, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCheckParamMethod1() {
    Utils.checkparam(false);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCheckParamMethod2() {
    Utils.checkparam(false, "hello:%s", "c");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCheckParamMethod3() {
    Utils.checkparam(false, null);
  }
}
