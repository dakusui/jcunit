package com.github.dakusui.jcunit8.examples.beforesandafters;

import com.github.dakusui.jcunit8.factorspace.Parameter;
import com.github.dakusui.jcunit8.runners.helpers.ParameterUtils;
import com.github.dakusui.jcunit8.runners.junit4.JCUnit8;
import com.github.dakusui.jcunit8.runners.junit4.annotations.*;
import com.github.dakusui.jcunit8.testsuite.TestOracle;
import com.github.dakusui.jcunit8.testsuite.TestSuite;
import org.junit.*;
import org.junit.runner.RunWith;

import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@RunWith(JCUnit8.class)
public class BeforeAfter {
  @ParameterSource
  public Parameter.Factory a() {
    return ParameterUtils.simple("A1", "A2");
  }

  @ParameterSource
  public Parameter.Factory b() {
    return ParameterUtils.simple("B1", "B2");
  }

  @ParameterSource
  public Parameter.Factory c() {
    return ParameterUtils.simple("C1", "C2");
  }

  @ParameterSource
  public Parameter.Factory d() {
    return ParameterUtils.simple("D1", "D2");
  }

  @ParameterSource
  public Parameter.Factory e() {
    return ParameterUtils.simple("E1", "E2");
  }

  @ParameterSource
  public Parameter.Factory f() {
    return ParameterUtils.simple("F1", "F2");
  }

  @Condition
  public boolean aIsA2(@From(("a")) String a) {
    return "A2".equals(a);
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
  @Given("aIsA2")
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
  public static void afterClass(@From("@suite") TestSuite suite) {
    System.out.println("afterClass:[");
    Stream.concat(
        Stream.of(
            suite.getScenario().oracles().stream().map(TestOracle::getName).map(s -> String.format("<%s>", s)).collect(toList())
        ),
        suite.stream().map(
            testCase -> suite.getScenario().oracles().stream().map(oracle -> oracle.shouldInvoke().test(testCase.getTestInput())).collect(toList())
        )
    ).forEach(
        objects -> {
          System.out.print("  ");
          objects.forEach(
              e -> System.out.printf("%-20s", e)
          );
          System.out.println();
        }
    );
    System.out.println("]");
  }
}
