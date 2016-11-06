package com.github.dakusui.jcunit.regex;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.utils.Utils.Form;
import com.github.dakusui.jcunit.regex.RegexToFactorListTranslator.Reference;
import com.github.dakusui.jcunit.regex.RegexToFactorListTranslator.Value;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.github.dakusui.jcunit.core.utils.Utils.transform;
import static com.github.dakusui.jcunit.regex.RegexToFactorListTranslator.VOID;
import static java.lang.String.format;

public class Composer implements Expr.Visitor {
  private final String                   prefix;
  private final Tuple                    tuple;
  private final Map<String, List<Value>> terms;
  public List<Object> out = new LinkedList<Object>();

  public Composer(String prefix, Tuple tuple, Map<String, List<Value>> terms) {
    this.prefix = prefix;
    this.tuple = tuple;
    this.terms = terms;
  }

  @Override
  public void visit(Expr.Alt expr) {
    String key = composeKey(expr);
    //noinspection ConstantConditions
    Object values = tuple.get(key);
    if (VOID.equals(values))
      return;
    for (Object eachElement : (List) values) {
      if (eachElement instanceof Reference) {
        chooseChild((Reference) eachElement, expr.getChildren()).accept(this);
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
  public void visit(Expr.Leaf leaf) {
    out.add(leaf.value());
  }

  private Expr chooseChild(Reference element, List<Expr> children) {
    for (Expr each : children) {
      if (composeKey(each).equals(element.key) || ((this.terms.get(composeKey(each)).size() == 1) && this.terms.get(composeKey(each)).get(0).equals(element.key))) {
        return each;
      }
    }
    throw new RuntimeException(format("%s didn't match any of %s ",
        element.key,
        transform(children, new Form<Expr, String>() {
          @Override
          public String apply(Expr in) {
            return format("REGEX:%s:%s", prefix, in.id());
          }
        })));
  }

  private String composeKey(Expr expr) {
    return RegexToFactorListTranslator.composeKey(this.prefix, expr.id());
  }
}
