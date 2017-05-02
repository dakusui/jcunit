package com.github.dakusui.jcunit8.tests.components.utils;

import com.github.dakusui.jcunit8.core.Utils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UtilsTest {
  @Test
  public void givenString$whenApplyPrinter$thenNotBroken() {
    assertEquals("Hello", Utils.printer().apply("Hello"));
  }

  @Test
  public void givenString$whenPrint$thenNotBroken() {
    assertEquals("Hello", Utils.print("Hello"));
    assertEquals("Hello2", Utils.print("Hello2"));
  }
}
