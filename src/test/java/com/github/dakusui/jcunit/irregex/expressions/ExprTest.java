package com.github.dakusui.jcunit.irregex.expressions;


import com.github.dakusui.jcunit.core.utils.StringUtils;
import com.github.dakusui.jcunit.framework.TestCase;
import com.github.dakusui.jcunit.framework.TestSuite;
import com.github.dakusui.jcunit.regex.Composer;
import com.github.dakusui.jcunit.regex.Expr;
import com.github.dakusui.jcunit.regex.Printer;
import com.github.dakusui.jcunit.regex.RegexTestSuiteBuilder;
import org.junit.Test;

import java.io.PrintStream;

import static com.github.dakusui.jcunit.regex.Expr.Factory.*;
import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class ExprTest {
  @Test
  public void factorSpaceBuilder() {
    RegexTestSuiteBuilder regexTestSuiteBuilder = new RegexTestSuiteBuilder();
    Expr expr = cat(
        alt("Hello",
            alt("hello",
                cat("hello", "!")
            )),
        cat("world", ","), rep("everyone", 0, 1));

    expr.accept(regexTestSuiteBuilder);
    System.out.println(regexTestSuiteBuilder.toString());
    expr.accept(new Printer(System.out));
    System.out.println();
    TestSuite testSuite = regexTestSuiteBuilder.buildTestSuite();
    printTestSuite(testSuite);
    for (TestCase each : testSuite) {
      System.out.print(each.getCategory() + ":");
      Composer composer = new Composer("aPrefix", each.getTuple(), regexTestSuiteBuilder.terms);
      expr.accept(composer);
      System.out.println(StringUtils.join(" ", composer.out));
    }
  }

  @Test
  public void factorSpaceBuilderSimple() {
    Expr expr;
    RegexTestSuiteBuilder regexTestSuiteBuilder = new RegexTestSuiteBuilder();
    (expr = alt("Hello", "hello"))
        .accept(regexTestSuiteBuilder);
    expr.accept(new Printer(System.out));
    TestSuite testSuite = regexTestSuiteBuilder.buildTestSuite();
    assertThat(testSuite.get(0).getTuple().toString(), containsString("[Hello]"));
    assertThat(testSuite.get(1).getTuple().toString(), containsString("[hello]"));
    assertEquals(2, testSuite.size());
  }

  @Test
  public void factorSpaceBuilderSimple2() {
    Expr expr;
    RegexTestSuiteBuilder regexTestSuiteBuilder = new RegexTestSuiteBuilder();
    (expr = cat(alt("Hello", "hello"), alt("Hi", "HI")))
        .accept(regexTestSuiteBuilder);
    expr.accept(new Printer(System.out));
    TestSuite testSuite = regexTestSuiteBuilder.buildTestSuite();
    printTestSuite(testSuite);
    assertThat(testSuite.get(0).getTuple().toString(), containsString("[Hello]"));
    assertThat(testSuite.get(0).getTuple().toString(), containsString("[Hi]"));
    assertThat(testSuite.get(1).getTuple().toString(), containsString("[Hello]"));
    assertThat(testSuite.get(1).getTuple().toString(), containsString("[HI]"));
    assertThat(testSuite.get(2).getTuple().toString(), containsString("[hello]"));
    assertThat(testSuite.get(2).getTuple().toString(), containsString("[Hi]"));
    assertThat(testSuite.get(3).getTuple().toString(), containsString("[hello]"));
    assertThat(testSuite.get(3).getTuple().toString(), containsString("[HI]"));
    assertEquals(4, testSuite.size());
  }

  @Test
  public void factorSpaceBuilderNested() {
    Expr expr;
    RegexTestSuiteBuilder regexTestSuiteBuilder = new RegexTestSuiteBuilder();
    (expr = cat(alt("Hello", alt("Hi", "HI")), "howdy"))
        .accept(regexTestSuiteBuilder);
    expr.accept(new Printer(System.out));
    TestSuite testSuite = regexTestSuiteBuilder.buildTestSuite();
    printTestSuite(testSuite);
  }

  @Test
  public void factorSpaceBuilderNested2() {
    Expr expr;
    RegexTestSuiteBuilder regexTestSuiteBuilder = new RegexTestSuiteBuilder();
    (expr = cat(alt("Hello", cat(alt("Hi", "HI")), "howdy")))
        .accept(regexTestSuiteBuilder);
    System.out.println(regexTestSuiteBuilder.toString());
    System.out.println();
    expr.accept(new Printer(System.out));
    System.out.println(regexTestSuiteBuilder.buildTestSuite());
  }

  @Test
  public void factorSpaceBuilderNested3() {
    Expr expr;
    RegexTestSuiteBuilder regexTestSuiteBuilder = new RegexTestSuiteBuilder();
    (expr = cat("Hello", cat("Hi", "!")))
        .accept(regexTestSuiteBuilder);
    System.out.println(regexTestSuiteBuilder.toString());
    System.out.println();
    expr.accept(new Printer(System.out));
    System.out.println(regexTestSuiteBuilder.buildTestSuite());
  }


  @Test
  public void factorSpaceBuilderNested4() {
    Expr expr;
    RegexTestSuiteBuilder regexTestSuiteBuilder = new RegexTestSuiteBuilder();
    (expr = cat(alt("Hello", cat(alt("Hi", "HI")), "howdy")))
        .accept(regexTestSuiteBuilder);
    System.out.println(regexTestSuiteBuilder.toString());
    System.out.println();
    expr.accept(new Printer(System.out));
    System.out.println(regexTestSuiteBuilder.buildTestSuite());
  }

  @Test
  public void factorSpaceBuilderRep() {
    Expr expr;
    RegexTestSuiteBuilder regexTestSuiteBuilder = new RegexTestSuiteBuilder();
    (expr = rep("Hello", 0, 1))
        .accept(regexTestSuiteBuilder);
    System.out.println(regexTestSuiteBuilder.toString());
    expr.accept(new Printer(System.out));
  }

  @Test
  public void factorSpaceBuilderRep2() {
    Expr expr;
    RegexTestSuiteBuilder regexTestSuiteBuilder = new RegexTestSuiteBuilder();
    (expr = rep(cat("Hello", alt("World", "WORLD")), 1, 2))
        .accept(regexTestSuiteBuilder);
    System.out.println(regexTestSuiteBuilder.toString());
    System.out.println();
    expr.accept(new Printer(System.out));
    System.out.println(regexTestSuiteBuilder.buildTestSuite());
  }

  @Test
  public void factorSpaceBuilderRep3() {
    Expr expr;
    RegexTestSuiteBuilder regexTestSuiteBuilder = new RegexTestSuiteBuilder();
    (expr = cat(rep("reset", 0, 1), rep(alt("update1", "update2"), 2, 3), alt("digest0", "digest1")))
        .accept(regexTestSuiteBuilder);
    System.out.println(regexTestSuiteBuilder.toString());
    System.out.println();
    expr.accept(new Printer(System.out));
    System.out.println(regexTestSuiteBuilder.buildTestSuite());
  }

  @Test
  public void factorSpaceBuilderRep4() {
    Expr expr;
    RegexTestSuiteBuilder regexTestSuiteBuilder = new RegexTestSuiteBuilder();
    (expr = rep(alt("update1", "update2"), 2, 3))
        .accept(regexTestSuiteBuilder);
    System.out.println(regexTestSuiteBuilder.toString());
    System.out.println();
    expr.accept(new Printer(System.out));
    System.out.println(regexTestSuiteBuilder.buildTestSuite());
  }

  @Test
  public void factorSpaceBuilderRep5() {
    Expr expr;
    RegexTestSuiteBuilder regexTestSuiteBuilder = new RegexTestSuiteBuilder();
    (expr = cat(rep("reset", 0, 1), rep(alt("update1", "update2"), 1, 2)))
        .accept(regexTestSuiteBuilder);
    System.out.println(regexTestSuiteBuilder.toString());
    System.out.println();
    expr.accept(new Printer(System.out));
    System.out.println(regexTestSuiteBuilder.buildTestSuite());
  }

  private void printTestSuite(TestSuite testSuite) {
    PrintStream ps = System.out;
    int i = 0;
    for (TestCase each : testSuite) {
      ps.println(format("%4d:%s:%s", i++, each.getCategory(), each.getTuple()));
    }
  }
}
