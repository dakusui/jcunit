package com.github.dakusui.jcunit.regex;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.regex.RegexToFactorListTranslator.Reference;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.github.dakusui.jcunit.regex.RegexToFactorListTranslator.composeKey;

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
    String key = composeKey(this.prefix, exp.id());
    //noinspection ConstantConditions
    for (Object eachElement : (List) tuple.get(key)) {
      if (eachElement instanceof Reference) {
        chooseChild((Reference) eachElement, exp.getChildren()).accept(this);
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
    String key = ((Reference)this.terms.get(
        composeKey(this.prefix, exp.id()))
        .get(0)).key;
    for (Object eachElement : (List) tuple.get(key)) {
      if (eachElement instanceof Reference) {
        chooseChild((Reference) eachElement, Collections.singletonList(exp.getChild())).accept(this);
      } else {
        out.add(eachElement);
      }
    }
  }

  @Override
  public void visit(Expr.Leaf leaf) {
    out.add(leaf.value());
  }

  private Expr chooseChild(Reference element, List<Expr> children) {
    for (Expr each : children) {
      if (composeKey(this.prefix, each.id()).equals(element.key)) {
        return each;
      }
    }
    throw new RuntimeException();
  }

}
