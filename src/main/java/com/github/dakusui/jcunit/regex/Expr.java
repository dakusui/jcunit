package com.github.dakusui.jcunit.regex;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.dakusui.jcunit.core.utils.Checks.checkcond;
import static com.github.dakusui.jcunit.core.utils.Checks.checknotnull;
import static com.github.dakusui.jcunit.core.utils.Utils.transform;
import static java.lang.String.format;
import static java.util.Arrays.asList;

public interface Expr {
  void accept(Visitor visitor);

  String id();

  enum Utils {
    ;

    static String str(Object value) {
      return value == null ?
          null :
          value.toString();
    }
  }

  abstract class Base implements Expr {
    private static final AtomicInteger counter = new AtomicInteger(0);
    private final String id;

    Base() {
      this.id = format("%s-%d", this.getClass().getSimpleName().toLowerCase(), counter.getAndIncrement());
    }

    public String id() {
      return id;
    }
  }

  class Leaf extends Base implements Expr {
    private final Object value;

    Leaf(Object value) {
      this.value = value;
    }

    public void accept(Visitor visitor) {
      visitor.visit(this);
    }

    Object value() {
      return this.value;
    }

    @Override
    public String toString() {
      return Utils.str(this.value());
    }
  }

  class Cat extends Composite implements Expr {
    static final Cat EMPTY = new Cat(Collections.<Expr>emptyList());

    Cat(List<Expr> children) {
      super(children);
    }

    public void accept(Visitor visitor) {
      visitor.visit(this);
    }
  }

  class Alt extends Composite implements Expr {
    Alt(List<Expr> children) {
      super(children);
    }

    public void accept(Visitor visitor) {
      visitor.visit(this);
    }
  }

  class Rep extends Base implements Expr {
    private final Expr child;
    private final int  min;
    private final int  max;

    Rep(Expr child, int min, int max) {
      checkcond(min >= 0 && min <= max);
      this.child = checknotnull(child);
      this.min = min;
      this.max = max;
    }

    public void accept(Visitor visitor) {
      visitor.visit(this);
    }

    @Override
    public String toString() {
      return format("%s:%s{%s,%s}",
          this.getClass().getSimpleName().toLowerCase(),
          Utils.str(this.child),
          this.min,
          this.max
      );
    }

    Expr getChild() {
      return child;
    }

    int getMin() {
      return this.min;
    }

    int getMax() {
      return this.max;
    }
  }

  abstract class Composite extends Base implements Expr {
    private final List<Expr> children;

    Composite(List<Expr> children) {
      this.children = children;
    }

    List<Expr> getChildren() {
      return this.children;
    }

    public String toString() {
      return format("%s:%s", this.getClass().getSimpleName().toLowerCase(), this.getChildren());
    }
  }

  interface Visitor {
    void visit(Alt exp);

    void visit(Cat exp);

    void visit(Rep exp);

    void visit(Leaf leaf);
  }

  enum Factory {
    ;

    public static Expr cat(Object... exps) {
      return new Cat(transform(asList(exps), new com.github.dakusui.jcunit.core.utils.Utils.Form<Object, Expr>() {
        public Expr apply(Object in) {
          if (in instanceof Expr) {
            return (Expr) in;
          }
          return new Leaf(in);
        }
      }));
    }

    public static Expr alt(Object... exps) {
      return new Alt(transform(asList(exps), new com.github.dakusui.jcunit.core.utils.Utils.Form<Object, Expr>() {
        public Expr apply(Object in) {
          if (in instanceof Expr) {
            return (Expr) in;
          }
          return new Leaf(in);
        }
      }));
    }

    public static Expr rep(Object exp, int min, int max) {
      return new Rep(exp instanceof Expr ? (Expr) exp : new Leaf(exp), min, max);
    }
  }
}
