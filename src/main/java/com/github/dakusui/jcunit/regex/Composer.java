package com.github.dakusui.jcunit.regex;

import com.github.dakusui.jcunit.core.tuples.Tuple;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Composer implements Expr.Visitor {
  private final String                                         prefix;
  private final Tuple                                          tuple;
  private final Map<String, List<RegexTestSuiteBuilder.Value>> terms;
  public List<Object> out = new LinkedList<Object>();

  public Composer(String prefix, Tuple tuple, Map<String, List<RegexTestSuiteBuilder.Value>> terms) {
    this.prefix = prefix;
    this.tuple = tuple;
    this.terms = terms;
  }

  @Override
  public void visit(Expr.Alt exp) {
    String key = RegexToFactorListTranslator.composeKey(this.prefix, exp.id());
    //noinspection ConstantConditions
    for (Object eachElement : (List) tuple.get(key)) {
      if (eachElement instanceof RegexTestSuiteBuilder.Reference) {
        chooseChild((RegexTestSuiteBuilder.Reference) eachElement, exp.getChildren()).accept(this);
      } else {
        out.add(eachElement);
      }
    }
  }

  @Override
  public void visit(Expr.Cat exp) {
    for (Expr each : exp.getChildren()) {
      each.accept(this);
    }
  }

  @Override
  public void visit(Expr.Rep exp) {
    String key = ((RegexTestSuiteBuilder.Reference)this.terms.get(RegexToFactorListTranslator.composeKey(this.prefix, exp.id())).get(0)).key;
    for (Object eachElement : (List) tuple.get(key)) {
      if (eachElement instanceof RegexTestSuiteBuilder.Reference) {
        chooseChild((RegexTestSuiteBuilder.Reference) eachElement, Collections.singletonList(exp.getChild())).accept(this);
      } else {
        out.add(eachElement);
      }
    }
  }

  @Override
  public void visit(Expr.Leaf leaf) {
    out.add(leaf.value());
  }

  private Expr chooseChild(RegexTestSuiteBuilder.Reference element, List<Expr> children) {
    for (Expr each : children) {
      if (RegexToFactorListTranslator.composeKey(this.prefix, each.id()).equals(element.key)) {
        return each;
      }
    }
    throw new RuntimeException();
  }

}
