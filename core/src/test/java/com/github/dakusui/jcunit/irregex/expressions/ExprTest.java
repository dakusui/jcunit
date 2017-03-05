package com.github.dakusui.jcunit.irregex.expressions;


import com.github.dakusui.jcunit.core.utils.StringUtils;
import com.github.dakusui.jcunit.framework.TestCase;
import com.github.dakusui.jcunit.framework.TestSuite;
import com.github.dakusui.jcunit.regex.*;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class ExprTest {
  private Expr.Factory exprFactory = new Expr.Factory();

  private Expr cat(Object... args) {
    return exprFactory.cat(asList(args));
  }

  private Expr alt(Object... args) {
    return exprFactory.alt(asList(args));
  }

  private Expr rep(Object arg, int min, int max) {
    return exprFactory.rep(arg, min, max);
  }

  @Test
  public void factorSpaceBuilder() {
    Expr expr = cat(
        alt("Hello",
            alt("hello",
                cat("hello", "!")
            )),
        cat("world", ","), rep("everyone", 1, 3));
    RegexTestSuiteBuilder regexTestSuiteBuilder = new RegexTestSuiteBuilder("aPrefix", expr);

    //    expr.accept(regexTestSuiteBuilder);
    System.out.println(regexTestSuiteBuilder.toString());
    expr.accept(new Printer(System.out));
    System.out.println();
    TestSuite testSuite = regexTestSuiteBuilder.buildTestSuite();
    RegexTestUtils.printTestSuite(testSuite);
    for (TestCase each : testSuite) {
      System.out.println(StringUtils.join(" ", compose(regexTestSuiteBuilder.terms, expr, each)));
    }
  }

  @Test
  public void factorSpaceBuilderSimple2() {
    Expr expr;
    expr = cat(alt("Hello", "hello"), alt("Hi", "HI"));
    RegexTestSuiteBuilder regexTestSuiteBuilder = new RegexTestSuiteBuilder("aPrefix", expr);
    //    ()
    //        .accept(regexTestSuiteBuilder);
    expr.accept(new Printer(System.out));
    TestSuite testSuite = regexTestSuiteBuilder.buildTestSuite();
    RegexTestUtils.printTestSuite(testSuite);
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
    expr = cat(alt("Hello", alt("Hi", "HI")), "howdy");
    RegexTestSuiteBuilder regexTestSuiteBuilder = new RegexTestSuiteBuilder("aPrefix", expr);
    //    (expr = cat(alt("Hello", alt("Hi", "HI")), "howdy"))
    //        .accept(regexTestSuiteBuilder);
    expr.accept(new Printer(System.out));
    TestSuite testSuite = regexTestSuiteBuilder.buildTestSuite();
    for (TestCase each : testSuite) {
      System.out.println(StringUtils.join(" ", compose(regexTestSuiteBuilder.terms, expr, each)));
    }
  }

  @Test
  public void factorSpaceBuilderNested2() {
    Expr expr;
    expr = cat(alt("Hello", cat(alt("Hi", "HI")), "howdy"));
    RegexTestSuiteBuilder regexTestSuiteBuilder = new RegexTestSuiteBuilder("aPrefix", expr);
    //    (expr = cat(alt("Hello", cat(alt("Hi", "HI")), "howdy")))
    //        .accept(regexTestSuiteBuilder);
    System.out.println(regexTestSuiteBuilder.toString());
    System.out.println();
    expr.accept(new Printer(System.out));
    System.out.println();
    for (TestCase each : regexTestSuiteBuilder.buildTestSuite()) {
      System.out.println(StringUtils.join(" ", compose(regexTestSuiteBuilder.terms, expr, each)));
    }
  }

  @Test
  public void factorSpaceBuilderNested3() {
    Expr expr;
    expr = cat("Hello", cat("Hi", "!"));
    RegexTestSuiteBuilder regexTestSuiteBuilder = new RegexTestSuiteBuilder("aPrefix", expr);
    //    (expr = cat("Hello", cat("Hi", "!")))
    //        .accept(regexTestSuiteBuilder);
    System.out.println(regexTestSuiteBuilder.toString());
    System.out.println();
    expr.accept(new Printer(System.out));
    System.out.println();
    for (TestCase each : regexTestSuiteBuilder.buildTestSuite()) {
      System.out.println(StringUtils.join(" ", compose(regexTestSuiteBuilder.terms, expr, each)));
    }
  }


  @Test
  public void factorSpaceBuilderNested4() {
    Expr expr;
    expr = cat(alt("Hello", cat(alt("Hi", "HI")), "howdy"));
    RegexTestSuiteBuilder regexTestSuiteBuilder = new RegexTestSuiteBuilder("aPrefix", expr);
    //    (expr = cat(alt("Hello", cat(alt("Hi", "HI")), "howdy")))
    //        .accept(regexTestSuiteBuilder);
    System.out.println(regexTestSuiteBuilder.toString());
    System.out.println();
    expr.accept(new Printer(System.out));
    System.out.println();
    for (TestCase each : regexTestSuiteBuilder.buildTestSuite()) {
      System.out.println(StringUtils.join(" ", compose(regexTestSuiteBuilder.terms, expr, each)));
    }
  }

  @Test
  public void factorSpaceBuilderRep() {
    Expr expr;
    expr = rep("Hello", 0, 1);
    RegexTestSuiteBuilder regexTestSuiteBuilder = new RegexTestSuiteBuilder("aPrefix", expr);
    //    (expr = rep("Hello", 0, 1))
    //        .accept(regexTestSuiteBuilder);
    System.out.println(regexTestSuiteBuilder.toString());
    expr.accept(new Printer(System.out));
    System.out.println();
    for (TestCase each : regexTestSuiteBuilder.buildTestSuite()) {
      System.out.println(StringUtils.join(" ", compose(regexTestSuiteBuilder.terms, expr, each)));
    }

  }

  @Test
  public void factorSpaceBuilderRep2() {
    Expr expr;
    expr = rep(cat("Hello", alt("World", "WORLD")), 1, 2);
    RegexTestSuiteBuilder regexTestSuiteBuilder = new RegexTestSuiteBuilder("aPrefix", expr);
    //    (expr = rep(cat("Hello", alt("World", "WORLD")), 1, 2))
    //        .accept(regexTestSuiteBuilder);
    System.out.println(regexTestSuiteBuilder.toString());
    System.out.println();
    expr.accept(new Printer(System.out));
    System.out.println();
    for (TestCase each : regexTestSuiteBuilder.buildTestSuite()) {
      System.out.println(StringUtils.join(" ", compose(regexTestSuiteBuilder.terms, expr, each)));
    }
  }

  @Test
  public void factorSpaceBuilderRep2b() {
    Expr expr;
    expr = rep(alt("World", "WORLD"), 2, 3);
    RegexTestSuiteBuilder regexTestSuiteBuilder = new RegexTestSuiteBuilder("aPrefix", expr);
    //    (expr = rep(alt("World", "WORLD"), 2, 3))
    //        .accept(regexTestSuiteBuilder);
    System.out.println(regexTestSuiteBuilder.toString());
    System.out.println();
    expr.accept(new Printer(System.out));
    System.out.println();
    for (TestCase each : regexTestSuiteBuilder.buildTestSuite()) {
      System.out.println(StringUtils.join(" ", compose(regexTestSuiteBuilder.terms, expr, each)));
    }
  }

  @Test
  public void factorSpaceBuilderRep3() {
    Expr expr;
    expr = cat(rep("reset", 0, 1), rep(alt("update1", "update2"), 2, 3), alt("digest0", "digest1"));
    RegexTestSuiteBuilder regexTestSuiteBuilder = new RegexTestSuiteBuilder("aPrefix", expr);
    //    (expr = cat(rep("reset", 0, 1), rep(alt("update1", "update2"), 2, 3), alt("digest0", "digest1")))
    //        .accept(regexTestSuiteBuilder);
    System.out.println(regexTestSuiteBuilder.toString());
    System.out.println();
    expr.accept(new Printer(System.out));
    System.out.println();
    for (TestCase each : regexTestSuiteBuilder.buildTestSuite()) {
      System.out.println(StringUtils.join(" ", compose(regexTestSuiteBuilder.terms, expr, each)));
    }
  }

  @Test
  public void factorSpaceBuilderRep4() {
    Expr expr;
    expr = rep(alt("update1", "update2"), 2, 3);
    RegexTestSuiteBuilder regexTestSuiteBuilder = new RegexTestSuiteBuilder("aPrefix", expr);
    //    (expr = rep(alt("update1", "update2"), 2, 3))
    //        .accept(regexTestSuiteBuilder);
    System.out.println(regexTestSuiteBuilder.toString());
    System.out.println();
    expr.accept(new Printer(System.out));
    System.out.println();
    for (TestCase each : regexTestSuiteBuilder.buildTestSuite()) {
      System.out.println(StringUtils.join(" ", compose(regexTestSuiteBuilder.terms, expr, each)));
    }
  }

  @Test
  public void factorSpaceBuilderRep5() {
    Expr expr;
    expr = cat(rep("reset", 0, 1), rep(alt("update1", "update2"), 1, 2));
    RegexTestSuiteBuilder regexTestSuiteBuilder = new RegexTestSuiteBuilder("aPrefix", expr);
    //    (expr = cat(rep("reset", 0, 1), rep(alt("update1", "update2"), 1, 2)))
    //        .accept(regexTestSuiteBuilder);
    System.out.println(regexTestSuiteBuilder.toString());
    System.out.println();
    expr.accept(new Printer(System.out));
    System.out.println();
    for (TestCase testCase : regexTestSuiteBuilder.buildTestSuite()) {
      System.out.println(StringUtils.join(" ", compose(regexTestSuiteBuilder.terms, expr, testCase)));
    }
  }

  @Test
  public void factorSpaceBuilderRep6() {
    Expr expr;
    expr = cat(rep(rep("reset", 0, 1), 0, 1), rep(alt("update1", "update2"), 1, 2));
    RegexTestSuiteBuilder regexTestSuiteBuilder = new RegexTestSuiteBuilder("aPrefix", expr);
    //    (expr = cat(rep(rep("reset", 0, 1), 0, 1), rep(alt("update1", "update2"), 1, 2)))
    //        .accept(regexTestSuiteBuilder);
    System.out.println(regexTestSuiteBuilder.toString());
    System.out.println();
    expr.accept(new Printer(System.out));
    for (TestCase testCase : regexTestSuiteBuilder.buildTestSuite()) {
      System.out.println(StringUtils.join(" ", compose(regexTestSuiteBuilder.terms, expr, testCase)));
    }
  }

  private List<Object> compose(Map<String, List<Value>> terms, Expr expr, TestCase each) {
    return new Composer("aPrefix", expr).compose(each.getTuple());
    /*
    List<Object> out;
    Composer.ComposerVisitor composerVisitor = new Composer.ComposerVisitor("aPrefix", each.getTuple(), terms, exprs);
    expr.accept(composerVisitor);
    out = composerVisitor.out;
    return out;
    */
  }
}
