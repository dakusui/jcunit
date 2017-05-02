package com.github.dakusui.jcunit8.tests.components.regex;

import com.github.dakusui.jcunit.regex.Expr;

enum RegexTestUtils {
  ;

  static class ExprTreePrinter implements Expr.Visitor {
    private final InternalNodeFormatter formatter;
    int indent = 0;

    ExprTreePrinter(InternalNodeFormatter formatter) {
      this.formatter = formatter;
    }

    @Override
    public void visit(Expr.Alt exp) {
      com.github.dakusui.jcunit8.core.Utils.out().println(indentString() + formatter.format(exp));
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
      com.github.dakusui.jcunit8.core.Utils.out().println(indentString() + formatter.format(exp));
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
      com.github.dakusui.jcunit8.core.Utils.out().println(indentString() + "leaf:'" + leaf + "'");
    }

    @Override
    public void visit(Expr.Empty empty) {
      com.github.dakusui.jcunit8.core.Utils.out().println(indentString() + "empty");
    }

    String indentString() {
      StringBuilder ret = new StringBuilder();
      for (int i = 0; i < indent; i++) {
        ret.append("  ");
      }
      return ret.toString();
    }

    interface InternalNodeFormatter {
      String format(Expr.Composite expr);
    }
  }
}
