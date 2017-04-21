package com.github.dakusui.jcunit8.tests.components.regex;

import com.github.dakusui.jcunit.regex.Expr;

import java.io.PrintStream;

public class Printer implements Expr.Visitor {
  private final PrintStream ps;

  public Printer(PrintStream ps) {
    this.ps = ps;
  }

  @Override
  public void visit(Expr.Alt exp) {
    ps.print("(");
    try {
      boolean first = true;
      for (Expr each : exp.getChildren()) {
        if (!first) {
          ps.print("|");
        }
        each.accept(this);
        first = false;
      }
    } finally {
      ps.print(")");
    }
  }

  @Override
  public void visit(Expr.Cat exp) {
    boolean first = true;
    for (Expr each : exp.getChildren()) {
      if (!first) {
        ps.print(" ");
      }
      each.accept(this);
      first = false;
    }
  }

  @Override
  public void visit(Expr.Leaf leaf) {
    ps.print(leaf.value());
  }

  @Override
  public void visit(Expr.Empty empty) {
    ps.print(empty);
  }
}
