package com.github.dakusui.jcunit8.examples.beforesandafters;

import com.github.dakusui.jcunit8.factorspace.Parameter;
import com.github.dakusui.jcunit8.runners.helpers.Parameters;
import com.github.dakusui.jcunit8.runners.junit4.JCUnit8;
import com.github.dakusui.jcunit8.runners.junit4.annotations.AfterTestCase;
import com.github.dakusui.jcunit8.runners.junit4.annotations.BeforeTestCase;
import com.github.dakusui.jcunit8.runners.junit4.annotations.From;
import com.github.dakusui.jcunit8.runners.junit4.annotations.ParameterSource;
import org.junit.*;
import org.junit.runner.RunWith;

@RunWith(JCUnit8.class)
public class BeforeAfter {
  @ParameterSource
  public Parameter.Factory a() {
    return Parameters.simple("A1", "A2");
  }

  @ParameterSource
  public Parameter.Factory b() {
    return Parameters.simple("B1", "B2");
  }

  @ParameterSource
  public Parameter.Factory c() {
    return Parameters.simple("C1", "C2");
  }

  @ParameterSource
  public Parameter.Factory d() {
    return Parameters.simple("D1", "D2");
  }

  @ParameterSource
  public Parameter.Factory e() {
    return Parameters.simple("E1", "E2");
  }

  @ParameterSource
  public Parameter.Factory f() {
    return Parameters.simple("F1", "F2");
  }

  @BeforeClass
  public static void beforeClass() {
    System.out.println("beforeClass");
  }

  @BeforeTestCase
  public static void beforeTestCase(@From("a") String a) {
    System.out.println("  beforeTestCase:" + a);
  }

  @Before
  public void before(@From("b") String b) {
    System.out.println("    before:" + b);
  }

  @Test
  public void test1(@From("c") String c) {
    System.out.println("      test1:" + c);
  }

  @Test
  public void test2(@From("d") String d) {
    System.out.println("      test2:" + d);
  }

  @After
  public void after(@From("e") String e) {
    System.out.println("    after:" + e);
  }

  @AfterTestCase
  public static void afterTestCase(@From("f") String f) {
    System.out.println("  afterTestCase:" + f);
  }

  @AfterClass
  public static void afterClass() {
    System.out.println("afterClass");
  }
}
