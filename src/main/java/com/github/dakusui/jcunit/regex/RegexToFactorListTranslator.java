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

public class RegexToFactorListTranslator implements Expr.Visitor {
  protected static final Object VOID = new Object() {
    @Override
    public String toString() {
      return "(VOID)";
    }
  };
  public final    Map<String, List<Value>> terms;
  protected final String                   prefix;
  protected       Context                  context;

  public RegexToFactorListTranslator(String prefix) {
    this.terms = new LinkedHashMap<String, List<Value>>();
    this.prefix = prefix;
    this.context = new Context.Impl(this.prefix, null);
  }

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

  public void visit(Leaf leaf) {
    this.context.add(leaf);
  }

  public Factors buildFactors() {
    final Factors.Builder builder = new Factors.Builder();
    for (String eachKey : this.terms.keySet()) {
      if (isAlt(eachKey)) {
        Factor.Builder b = new Factor.Builder(eachKey);
        for (Value eachValue : this.terms.get(eachKey)) {
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

  private boolean isAlt(String eachKey) {
    return eachKey.startsWith("REGEX:" + this.prefix + ":alt-");
  }

  private boolean isReferenced(final String key) {
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

  private List<Object> resolve(List<Object> values, Value value) {
    if (value instanceof Immediate) {
      values.add(((Immediate) value).value);
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
