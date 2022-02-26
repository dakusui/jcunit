package com.github.dakusui.jcunitx.metamodel.parameters.regex;

import com.github.dakusui.jcunitx.core.AArray;
import com.github.dakusui.jcunitx.pipeline.stages.Generator;
import com.github.dakusui.jcunitx.regex.Expr;
import com.github.dakusui.jcunitx.regex.Reference;
import com.github.dakusui.jcunitx.regex.RegexTranslator;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

public class RegexComposer {
  private final String            prefix;
  private final Expr              topLevel;
  private final Map<String, Expr> exprs;

  public RegexComposer(String prefix, Expr topLevel) {
    this.prefix = prefix;
    this.topLevel = topLevel;
    this.exprs = createMap(this.topLevel);
  }

  /**
   * A method to compose a sequence of `String`s that matches the expression given as `topLevel` from `tuple`.
   *
   * @param tuple An internal representation of a sequence matching the `topLevel` expression (`Expr`).
   * @return A sequence matching requested `Expr`.
   */
  public List<String> compose(AArray tuple) {
    ComposerVisitor visitor = new ComposerVisitor(tuple, this.exprs);
    this.topLevel.accept(visitor);
    return splitOnWhiteSpaces(visitor.out);
  }

  private List<String> splitOnWhiteSpaces(List<Object> in) {
    List<String> ret = new LinkedList<>();
    for (Object each : in) {
      String eachString = (String) each;
      if (!eachString.contains(" ")) {
        ret.add(eachString);
      } else {
        ret.addAll(asList(eachString.split(" +")));
      }
    }
    return ret;
  }

  private Map<String, Expr> createMap(Expr top) {
    final Map<String, Expr> ret = new HashMap<>();
    top.accept(new Expr.Visitor() {
      @Override
      public void visit(Expr.Alt exp) {
        ret.put(RegexComposer.this.composeKey(exp), exp);
        for (Expr each : exp.getChildren()) {
          each.accept(this);
        }
      }

      @Override
      public void visit(Expr.Cat exp) {
        ret.put(RegexComposer.this.composeKey(exp), exp);
        for (Expr each : exp.getChildren()) {
          each.accept(this);
        }
      }

      @Override
      public void visit(Expr.Leaf exp) {
        ret.put(RegexComposer.this.composeKey(exp), exp);
      }

      @Override
      public void visit(Expr.Empty exp) {
        ret.put(RegexComposer.this.composeKey(exp), exp);
      }
    });
    return ret;
  }

  private String composeKey(Expr expr) {
    return RegexTranslator.composeKey(this.prefix, expr.id());
  }

  private class ComposerVisitor implements Expr.Visitor {
    private final AArray            tuple;
    private final Map<String, Expr> exprs;
    public        List<Object>      out = new LinkedList<>();

    private ComposerVisitor(AArray tuple, Map<String, Expr> exprs) {
      this.tuple = tuple;
      this.exprs = exprs;
    }

    @Override
    public void visit(Expr.Alt expr) {
      Object values = tuple.get(composeKey(expr));
      if (Generator.VOID.equals(values))
        return;
      //noinspection unchecked
      for (Object each : (List<Object>) values) {
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

    @Override
    public void visit(Expr.Empty empty) {

    }
  }
}
