package com.github.dakusui.jcunitx;

import com.github.dakusui.jcunitx.annotations.CombinatorialTest;
import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;

public class AppTest {
  @Test
  public void test1() {
    System.out.println("hello!");
    new Exception().printStackTrace();
  }

  @Test
  @CombinatorialTest
  public void test2() {
    App.main();
  }

  @ParameterizedTest
  @org.junit.jupiter.params.provider.ValueSource(strings = { "hello" })
  public void test3(String hello) {
    System.out.println(hello);
  }
}
