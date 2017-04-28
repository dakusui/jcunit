package com.github.dakusui.jcunit.regex;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Collections.singletonList;

public abstract class RegexTranslator implements Expr.Visitor {
  /**
   * A mapping from factor names to terms held by composite (alt/cat) expressions.
   */
  public final    Map<String, List<Value>> terms;
  private final   String                   prefix;
  protected final Expr                     topLevelExpression;
  protected       Context                  context;

  public RegexTranslator(Expr topLevelExpression, String prefix) {
    this.topLevelExpression = topLevelExpression;
    this.prefix = prefix;
    this.context = new Context.Impl(this.prefix, null);
    this.terms = new LinkedHashMap<>();
  }

  static String composeKey(String prefix, String id) {
    return format("REGEX:%s:%s", prefix, id);
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
  public void visit(Expr.Leaf leaf) {
    this.context.add(leaf);
  }

  @Override
  public void visit(Expr.Empty empty) {
    this.context.add(empty);
  }

  protected boolean isTopLevel(String eachKey) {
    return composeKey(this.prefix, this.topLevelExpression.id()).equals(eachKey);
  }

  private Context createContext(String factorName) {
    return new Context.Impl(this.prefix, factorName);
  }

  protected boolean isAlt(String key) {
    return key.startsWith("REGEX:" + this.prefix + ":alt-") ||
        key.startsWith("REGEX:" + this.prefix + ":rep-");
  }

  protected boolean isReferencedByAltDirectlyOrIndirectly(final String key) {
    for (Map.Entry<String, List<Value>> each : this.terms.entrySet()) {
      if (isAlt(each.getKey())) {
        if (each.getValue().stream().anyMatch(in -> in instanceof Reference && ((Reference) in).key.equals(key))) {
          return true;
        }
      }
    }
    return false;
  }

  protected List<Object> resolveIfImmediate(Value value) {
    if (value instanceof Immediate)
      return resolveImmediate(value);
    return singletonList(value);
  }

  protected List<Object> resolve(Value value) {
    return resolve(new LinkedList<>(), value);
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
        this.seq = new LinkedList<>();
      }

      Value toValue(Expr expr) {
        Value value;
        if (expr instanceof Expr.Leaf) {
          value = new Immediate(((Expr.Leaf) expr).value());
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
