package com.github.dakusui.jcunitx;

import org.junit.Test;

public class JUnit5Test {
  @Test
  public void testMethod() {
    System.out.println("hello junit5");
    new Exception().printStackTrace();
  }
}
