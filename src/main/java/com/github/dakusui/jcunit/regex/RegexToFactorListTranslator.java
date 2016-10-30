package com.github.dakusui.jcunit.regex;

import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.utils.Checks;
import com.github.dakusui.jcunit.core.utils.Utils;
import com.github.dakusui.jcunit.framework.TestSuite;

import java.util.*;

import static java.lang.String.format;

public class RegexToFactorListTranslator implements Expr.Visitor {
  protected static final Object VOID = new Object() {
    @Override
    public String toString() {
      return "(VOID)";
    }
  };
  public final    Map<String, List<RegexTestSuiteBuilder.Value>> terms;
  protected final String                                         prefix;
  protected       Context                                        context;

  public RegexToFactorListTranslator(String prefix) {
    this.terms = new HashMap<String, List<RegexTestSuiteBuilder.Value>>();
    this.prefix = prefix;
    this.context = new Context.Impl(this.prefix, null);
  }

  public void visit(Expr.Alt expr) {
    Context original = this.context;
    original.add(expr);
    this.context = createSimpleContext(expr.id());
    try {
      for (Expr each : expr.getChildren()) {
        each.accept(this);
      }
    } finally {
      this.terms.put(composeKey(this.prefix, this.context.name()), this.context.values());
      this.context = original;
    }
  }

  public void visit(Expr.Cat expr) {
    Context original = this.context;
    original.add(expr);
    this.context = createSimpleContext(expr.id());
    try {
      for (Expr each : expr.getChildren()) {
        each.accept(this);
      }
    } finally {
      this.terms.put(composeKey(this.prefix, this.context.name()), this.context.values());
      this.context = original;
    }
  }

  public void visit(Expr.Rep exp) {
    List<Expr> exprs = new LinkedList<Expr>();
    for (int i : exp.getTimes()) {
      List<Expr> repeatedExprs = new LinkedList<Expr>();
      for (int j = 0; j < i; j++) {
        repeatedExprs.add(exp.getChild());
      }
      exprs.add(new Expr.Cat(repeatedExprs));
    }
    if (!exprs.isEmpty()) {
      Expr expr;
      (expr = new Expr.Alt(exprs)).accept(this);
      this.terms.put(
          composeKey(this.prefix, exp.id()),
          Collections.singletonList((RegexTestSuiteBuilder.Value) new RegexTestSuiteBuilder.Reference(composeKey(this.prefix, expr.id())))
      );
    }
  }

  public void visit(Expr.Leaf leaf) {
    this.context.add(leaf);
  }

  public Factors buildFactors() {
    final Factors.Builder builder = new Factors.Builder();
    for (String eachKey : this.terms.keySet()) {
      if (isAlt(eachKey)) {
        Factor.Builder b = new Factor.Builder(eachKey);
        for (RegexTestSuiteBuilder.Value eachValue : this.terms.get(eachKey)) {
          b.addLevel(this.resolve(new LinkedList<Object>(), eachValue));
        }
        if (isReferenced(eachKey)) {
          b.addLevel(VOID);
        }
        builder.add(b.build());
      }
    }
    return builder.build();
  }

  public List<TestSuite.Predicate> buildConstraints(List<Factor> factors) {
    List<TestSuite.Predicate> ret = new LinkedList<TestSuite.Predicate>();
    for (final Factor eachFactor : factors) {
      for (final Object eachLevel : eachFactor.levels) {
        Checks.checkcond(eachLevel instanceof List || RegexToFactorListTranslator.VOID.equals(eachLevel));
        //noinspection unchecked,ConstantConditions
        if (!RegexToFactorListTranslator.VOID.equals(eachLevel) && !Utils.filter((List) eachLevel, new Utils.Predicate() {
          @Override
          public boolean apply(Object o) {
            return o instanceof RegexTestSuiteBuilder.Reference;
          }
        }).isEmpty()) {
          //noinspection ConstantConditions
          for (final Object eachElement : (List) eachLevel) {
            if (!(eachElement instanceof RegexTestSuiteBuilder.Reference))
              continue;
            final String referee = ((RegexTestSuiteBuilder.Reference) eachElement).key;
            final String referer = eachFactor.name;
            final String tag = String.format("constraint(%s->%s)", referer, referee);

            ret.add(new TestSuite.Predicate(tag, referer, referee) {
              @Override
              public boolean apply(Tuple tuple) {
                if (Utils.eq(tuple.get(referer), eachLevel)) {
                  return !Utils.eq(tuple.get(referee), RegexToFactorListTranslator.VOID);
                }
                return Utils.eq(tuple.get(referee), RegexToFactorListTranslator.VOID);
              }

              @Override
              public String toString() {
                return tag;
              }
            });
          }
        }
      }
    }
    return ret;
  }

  static String composeKey(String prefix, String id) {
    return format("REGEX:%s:%s", prefix, id);
  }

  private Context createSimpleContext(String factorName) {
    return new Context.Impl(this.prefix, factorName);
  }

  private boolean isAlt(String eachKey) {
    return eachKey.startsWith("REGEX:" + this.prefix + ":alt-");
  }

  private boolean isReferenced(final String eachKey) {
    for (Map.Entry<String, List<RegexTestSuiteBuilder.Value>> each : this.terms.entrySet()) {
      if (isAlt(each.getKey())) {
        if (!Utils.filter(each.getValue(), new Utils.Predicate<RegexTestSuiteBuilder.Value>() {
          @Override
          public boolean apply(RegexTestSuiteBuilder.Value in) {
            return in instanceof RegexTestSuiteBuilder.Reference && ((RegexTestSuiteBuilder.Reference) in).key.equals(eachKey);
          }
        }).isEmpty()) {
          return true;
        }
      }
    }
    return false;
  }

  private List<Object> resolve(List<Object> values, RegexTestSuiteBuilder.Value value) {
    if (value instanceof RegexTestSuiteBuilder.Immediate) {
      values.add(((RegexTestSuiteBuilder.Immediate) value).value);
    } else {
      String key = ((RegexTestSuiteBuilder.Reference) value).key;
      if (isCat(key)) {
        for (RegexTestSuiteBuilder.Value each : this.terms.get(key)) {
          resolve(values, each);
        }
      } else {
        values.add(value);
      }
    }
    return values;
  }

  private boolean isCat(String key) {
    return key.startsWith("REGEX:" + this.prefix + ":cat-");
  }

  interface Context {
    void add(Expr value);

    List<RegexTestSuiteBuilder.Value> values();

    String name();

    class Impl implements Context {
      final List<RegexTestSuiteBuilder.Value> seq;

      final         String name;
      private final String prefix;

      Impl(String prefix, String name) {
        this.prefix = prefix;
        this.name = name;
        this.seq = new LinkedList<RegexTestSuiteBuilder.Value>();
      }

      RegexTestSuiteBuilder.Value toValue(Expr expr) {
        RegexTestSuiteBuilder.Value value;
        if (expr instanceof Expr.Leaf) {
          value = new RegexTestSuiteBuilder.Immediate(((Expr.Leaf) expr).value());
        } else {
          value = new RegexTestSuiteBuilder.Reference(composeKey(this.prefix, expr.id()));
        }
        return value;
      }

      @Override
      public List<RegexTestSuiteBuilder.Value> values() {
        return this.seq;
      }

      public String name() {
        return this.name;
      }

      public void add(Expr expr) {
        this.seq.add(toValue(expr));
      }
    }
  }

  public interface Value {
  }

  static class Reference implements Value {
    final String key;

    Reference(String key) {
      this.key = key;
    }

    @Override
    public String toString() {
      return format("Reference:<%s>", this.key);
    }
  }

  static class Immediate implements Value {
    private final Object value;

    Immediate(Object value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return format("Immediate:<%s>", this.value);
    }
  }
}
