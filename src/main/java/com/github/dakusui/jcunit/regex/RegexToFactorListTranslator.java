package com.github.dakusui.jcunit.regex;

import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.utils.Utils;
import com.github.dakusui.jcunit.framework.TestSuite;
import com.github.dakusui.jcunit.regex.Expr.Leaf;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.github.dakusui.jcunit.core.utils.Utils.concatenate;
import static com.github.dakusui.jcunit.core.utils.Utils.filter;
import static java.lang.String.format;
import static java.util.Collections.singletonList;

public class RegexToFactorListTranslator implements Expr.Visitor {
  protected static final Object VOID = new Object() {
    @Override
    public String toString() {
      return "(VOID)";
    }
  };
  /**
   * A mapping from factor names to terms held by composite (alt/cat) expressions.
   */
  public final    Map<String, List<Value>> terms;
  protected final String                   prefix;
  protected       Context                  context;
  private final   Expr                     topLevelExpression;

  public RegexToFactorListTranslator(String prefix, Expr topLevelExpression) {
    this.topLevelExpression = topLevelExpression;
    this.terms = new LinkedHashMap<String, List<Value>>();
    this.prefix = prefix;
    this.context = new Context.Impl(this.prefix, null);
  }

  @Override
  public void visit(Expr.Alt expr) {
    Context original = this.context;
    original.add(expr);
    this.context = createContext(expr.id());
    try {
      for (Expr each : expr.getChildren()) {
        each.accept(this);
      }
    } finally {
      this.terms.put(composeKey(this.prefix, this.context.name()), this.context.values());
      this.context = original;
    }
  }

  @Override
  public void visit(Expr.Cat expr) {
    Context original = this.context;
    original.add(expr);
    this.context = createContext(expr.id());
    try {
      for (Expr each : expr.getChildren()) {
        each.accept(this);
      }
    } finally {
      this.terms.put(composeKey(this.prefix, this.context.name()), this.context.values());
      this.context = original;
    }
  }

  @Override
  public void visit(Leaf leaf) {
    this.context.add(leaf);
  }

  @Override
  public void visit(Expr.Empty empty) {
    this.context.add(empty);
  }

  public Factors buildFactors() {
    this.topLevelExpression.accept(this);
    final Factors.Builder builder = new Factors.Builder();
    for (String eachKey : this.terms.keySet()) {
      Factor.Builder b = new Factor.Builder(eachKey);
      if (isReferencedByAltDirectlyOrIndirectly(eachKey) || isAlt(eachKey)) {
        b.addLevel(VOID);
      }
      if (isAlt(eachKey)) {
        for (Value eachValue : this.terms.get(eachKey)) {
          b.addLevel(resolveIfImmediate(eachValue));
          //          b.addLevel(this.resolve(eachValue));
        }
      } else /* , that is, if (isCat(eachKey)) */ {
        List<Object> work = new LinkedList<Object>();
        for (Value eachValue : this.terms.get(eachKey)) {
          work.addAll(this.resolve(eachValue));
        }
        b.addLevel(work);
      }
      if (b.size() > 1 || (b.size() == 1 && composeKey(this.prefix, this.topLevelExpression.id()).equals(eachKey))) {
        builder.add(b.build());
      }
    }
    return builder.build();
  }

  public List<TestSuite.Predicate> buildConstraints(List<Factor> factors) {
    List<TestSuite.Predicate> ret = new LinkedList<TestSuite.Predicate>();
    for (final Factor each : factors) {
      final List<String> referrers = Utils.transform(getReferringFactors(each, factors), new Utils.Form<Factor, String>() {
        @Override
        public String apply(Factor in) {
          return in.name;
        }
      });
      if (referrers.isEmpty())
        continue;
      final String referee = each.name;
      final String tag = format("constraint(%s->%s)", referrers, referee);
      ret.add(new TestSuite.Predicate(
          tag,
          concatenate(referrers, referee).toArray(new String[referrers.size() + 1])) {
        @Override
        public boolean apply(Tuple in) {
          for (String eachReferrer : referrers) {
            Object referrerValue = in.get(eachReferrer);
            if (!VOID.equals(referrerValue) && !filter(((List) referrerValue), new Utils.Predicate() {
              @Override
              public boolean apply(Object in) {
                return in instanceof Reference && ((Reference) in).key.equals(referee);
              }
            }).isEmpty()) {
              return !VOID.equals(in.get(referee));
            }
          }
          return VOID.equals(in.get(referee));
        }
      });
    }
    return ret;
  }

  private List<Factor> getReferringFactors(Factor referred, List<Factor> factors) {
    List<Factor> ret = new LinkedList<Factor>();
    outer:
    for (Factor each : factors) {
      if (each == referred)
        continue;
      for (Object eachLevel : each.levels) {
        if (eachLevel instanceof List) {
          for (Object eachElement : (List) eachLevel) {
            if (eachElement instanceof Reference) {
              if (referred.name.equals(((Reference) eachElement).key)) {
                ret.add(each);
                continue outer;
              }
            }
          }
        }
      }
    }
    return ret;
  }

  static String composeKey(String prefix, String id) {
    return format("REGEX:%s:%s", prefix, id);
  }

  private Context createContext(String factorName) {
    return new Context.Impl(this.prefix, factorName);
  }

  private boolean isAlt(String key) {
    return key.startsWith("REGEX:" + this.prefix + ":alt-") ||
        key.startsWith("REGEX:" + this.prefix + ":rep-");
  }

  private boolean isReferencedByAltDirectlyOrIndirectly(final String key) {
    for (Map.Entry<String, List<Value>> each : this.terms.entrySet()) {
      if (isAlt(each.getKey())) {
        if (!filter(each.getValue(), new Utils.Predicate<Value>() {
          @Override
          public boolean apply(Value in) {
            return in instanceof Reference && ((Reference) in).key.equals(key);
          }
        }).isEmpty()) {
          return true;
        }
      }
    }
    return false;
  }

  private List<Object> resolveIfImmediate(Value value) {
    if (value instanceof Immediate)
      return resolveImmediate(value);
    return singletonList((Object) value);
  }

  private List<Object> resolve(Value value) {
    return resolve(new LinkedList<Object>(), value);
  }

  private List<Object> resolveImmediate(Value value) {
    return singletonList(((Immediate) value).value);
  }

  private List<Object> resolve(List<Object> values, Value value) {
    if (value instanceof Immediate) {
      values.add(resolveImmediate(value));
    } else {
      String key = ((Reference) value).key;
      if (isCat(key)) {
        for (Value each : this.terms.get(key)) {
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

    List<Value> values();

    String name();

    class Impl implements Context {
      final List<Value> seq;

      final         String name;
      private final String prefix;

      Impl(String prefix, String name) {
        this.prefix = prefix;
        this.name = name;
        this.seq = new LinkedList<Value>();
      }

      Value toValue(Expr expr) {
        Value value;
        if (expr instanceof Leaf) {
          value = new Immediate(((Leaf) expr).value());
        } else {
          value = new Reference(composeKey(this.prefix, expr.id()));
        }
        return value;
      }

      @Override
      public List<Value> values() {
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

}
