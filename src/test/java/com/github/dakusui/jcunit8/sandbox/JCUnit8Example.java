package com.github.dakusui.jcunit8.sandbox;

import com.github.dakusui.jcunit8.examples.executionsequence.ExampleParameterSpace;
import com.github.dakusui.jcunit8.runners.junit4.JCUnit8;
import com.github.dakusui.jcunit8.runners.junit4.annotations.*;
import com.github.dakusui.jcunit8.testsuite.TestSuite;
import org.junit.*;
import org.junit.runner.JUnitCore;
import org.junit.runner.RunWith;

@RunWith(JCUnit8.class)
@ConfigureWith(parameterSpace = ExampleParameterSpace.class)
public class JCUnit8Example {
  @Condition
  public boolean aIsGreaterThanOrEqualTo$b(@From("a") int a, @From("b") int b) {
    return a >= b;
  }

  @BeforeClass
  public static void beforeClass(@From("@suite") TestSuite testSuite) {
    System.out.println("beforeClass:size=" + testSuite.size());
  }

  @BeforeTestCase
  public static void beforeTestCase(@From("a") int a, @From("b") int b, @From("c") int c) {
    System.out.printf("  beforeTestCase:(a=%d,b=%d,c=%d)%n", a, b, c);
  }

  @Before
  public void before(@From("a") int a) {
    System.out.printf("    before:(a=%d)%n", a);
  }

  @Test
  public void whenRunTest1$thenLooksGood(@From("a") int a, @From("b") int b, @From("c") int c) {
    System.out.printf("      whenRunTest1$thenLooksGood(a=%d,b=%d,c=%d)%n", a, b, c);
  }

  @Test
  @Given("aIsGreaterThanOrEqualTo$b")
  public void whenRunTest2$thenLooksGood(@From("a") int a, @From("b") int b) {
    System.out.printf("      whenRunTest2$thenLooksGood(a=%d,b=%d)%n", a, b);
  }

  @After
  public void after(@From("b") int b, @From("c") int c) {
    System.out.printf("    after:(b=%d,c=%d)%n", b, c);
  }

  @AfterTestCase
  public static void afterTestCase(@From("a") int a, @From("b") int b) {
    System.out.printf("  afterTestCase:(a=%d,b=%d)%n", a, b);
  }

  @AfterClass
  public static void afterClass(@From("@suite") TestSuite suite) {
    System.out.println("afterClass:suite=" + suite);
  }

  public static void main(String... args) {
    new JUnitCore().run(JCUnit8Example.class);
  }
}
