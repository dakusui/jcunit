package com.github.dakusui.jcunit.regex;

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
  public void visit(Expr.Rep exp) {
    exp.getChild().accept(this);
    boolean first = true;
    ps.print("{");
    for (int each : exp.getTimes()) {
      if (!first) {
        ps.print(",");
      }
      ps.print(each);
      first = false;
    }
    ps.print("}");
  }

  @Override
  public void visit(Expr.Leaf leaf) {
    ps.print(leaf.value());
  }
}
