package com.github.dakusui.jcunit.regex;

import com.github.dakusui.jcunit.core.tuples.Tuple;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.github.dakusui.jcunit.regex.RegexToFactorListTranslator.VOID;

public class Composer {
  private final String prefix;
  private final Expr   topLevel;
  private final Map<String, Expr> exprs;

  public Composer(String prefix, Expr topLevel) {
    this.prefix = prefix;
    this.topLevel = topLevel;
    this.exprs = createMap(this.topLevel);
  }

  public List<Object> compose(Tuple tuple) {
    ComposerVisitor visitor = new ComposerVisitor(tuple, this.exprs);
    this.topLevel.accept(visitor);
    return visitor.out;
  }

  private Map<String, Expr> createMap(Expr top) {
    final Map<String, Expr> ret = new HashMap<String, Expr>();
    top.accept(new Expr.Visitor() {
      @Override
      public void visit(Expr.Alt exp) {
        ret.put(Composer.this.composeKey(exp), exp);
        for (Expr each : exp.getChildren()) {
          each.accept(this);
        }
      }

      @Override
      public void visit(Expr.Cat exp) {
        ret.put(Composer.this.composeKey(exp), exp);
        for (Expr each : exp.getChildren()) {
          each.accept(this);
        }
      }

      @Override
      public void visit(Expr.Leaf exp) {
        ret.put(Composer.this.composeKey(exp), exp);
      }
    });
    return ret;
  }

  private String composeKey(Expr expr) {
    return RegexToFactorListTranslator.composeKey(this.prefix, expr.id());
  }

  private class ComposerVisitor implements Expr.Visitor {
    private final Tuple             tuple;
    private final Map<String, Expr> exprs;
    public List<Object> out = new LinkedList<Object>();

    private ComposerVisitor(Tuple tuple, Map<String, Expr> exprs) {
      this.tuple = tuple;
      this.exprs = exprs;
    }

    @Override
    public void visit(Expr.Alt expr) {
      //noinspection ConstantConditions
      Object values = tuple.get(composeKey(expr));
      if (VOID.equals(values))
        return;
      for (Object each : (List) values) {
        if (each instanceof Reference) {
          this.exprs.get(((Reference) each).key).accept(this);
        } else {
          out.add(each);
        }
      }
    }

    @Override
    public void visit(Expr.Cat expr) {
      for (Expr each : expr.getChildren()) {
        each.accept(this);
      }
    }

    @Override
    public void visit(Expr.Leaf expr) {
      out.add(expr.value());
    }
  }
}
