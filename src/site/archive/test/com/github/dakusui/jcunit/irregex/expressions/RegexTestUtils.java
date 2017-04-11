package com.github.dakusui.jcunit.irregex.expressions;

import com.github.dakusui.jcunit.framework.TestCase;
import com.github.dakusui.jcunit.framework.TestSuite;
import com.github.dakusui.jcunit.regex.Expr;

import java.io.PrintStream;

public enum RegexTestUtils {
  ;

  static void printTestSuite(TestSuite testSuite) {
    PrintStream ps = System.out;
    int i = 0;
    for (TestCase each : testSuite) {
      ps.println(String.format("%4d:%s:%s", i++, each.getCategory(), each.getTuple()));
    }
  }

  static class ExprTreePrinter implements Expr.Visitor {
    private final InternalNodeFormatter formatter;
    int indent = 0;

    public ExprTreePrinter(InternalNodeFormatter formatter) {
      this.formatter = formatter;
    }

    @Override
    public void visit(Expr.Alt exp) {
      System.out.println(indentString() + formatter.format(exp));
      indent++;
      try {
        for (Expr eachChild : exp.getChildren()) {
          eachChild.accept(this);
        }
      } finally {
        indent--;
      }
    }

    @Override
    public void visit(Expr.Cat exp) {
      System.out.println(indentString() + formatter.format(exp));
      indent++;
      try {
        for (Expr eachChild : exp.getChildren()) {
          eachChild.accept(this);
        }
      } finally {
        indent--;
      }
    }

    @Override
    public void visit(Expr.Leaf leaf) {
      System.out.println(indentString() + "leaf:'" + leaf + "'");
    }

    @Override
    public void visit(Expr.Empty empty) {
      System.out.println(indentString() + "empty");
    }

    String indentString() {
      String ret = "";
      for (int i = 0; i < indent; i++) {
        ret += "  ";
      }
      return ret;
    }

    interface InternalNodeFormatter {
      String format(Expr.Composite expr);
    }
  }
}
