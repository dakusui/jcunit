package com.github.dakusui.jcunitx.tests.components.utils;

import com.github.dakusui.jcunitx.utils.Utils;
import com.github.dakusui.jcunitx.testutils.UTUtils;
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
