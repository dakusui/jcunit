package com.github.dakusui.jcunit8.tests.components.utils;

import com.github.dakusui.jcunit8.core.Utils;
import com.github.dakusui.jcunit8.testutils.UTUtils;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UtilsTest {
  @Before
  public void before() {
    UTUtils.configureStdIOs();
  }

  @Test
  public void givenString$whenPrint$thenNotBroken() {
    assertEquals("Hello", Utils.print("Hello"));
    assertEquals("Hello2", Utils.print("Hello2"));
  }
}
