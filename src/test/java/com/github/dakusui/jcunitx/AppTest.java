package com.github.dakusui.jcunitx;

import com.github.dakusui.jcunitx.annotations.Combinatorial;
import org.junit.Test;

public class AppTest {
  @Test
  public void test1() {
    System.out.println("hello!");
    new Exception().printStackTrace();
  }

  @Test
  @Combinatorial
  public void test2() {
    App.main();
  }
}
