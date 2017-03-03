package com.github.dakusui.jcunit.regex;

import com.github.dakusui.jcunit.core.utils.Utils.Form;

import java.util.Collections;
import java.util.LinkedList;
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
    private final String id;

    Base(AtomicInteger counter) {
      this.id = format("%s-%d", this.getClass().getSimpleName().toLowerCase(), counter.getAndIncrement());
    }

    public String id() {
      return id;
    }
  }

  class Leaf extends Base implements Expr {
    private final Object value;

    Leaf(AtomicInteger counter, Object value) {
      super(counter);
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
    Cat(AtomicInteger counter, List<Expr> children) {
      super(counter, children);
    }

    public void accept(Visitor visitor) {
      visitor.visit(this);
    }
  }

  class Alt extends Composite implements Expr {
    Alt(AtomicInteger counter, List<Expr> children) {
      super(counter, children);
    }

    public void accept(Visitor visitor) {
      visitor.visit(this);
    }
  }

  class Rep extends Cat {
    static final Cat EMPTY = new Cat(new AtomicInteger(0), Collections.<Expr>emptyList());
    final private int max;
    final private int min;

    Rep(AtomicInteger counter, Expr child, int min, int max) {
      super(counter, createChildren(counter, child, min, max));
      this.min = min;
      this.max = max;
    }

    @Override
    public String toString() {
      return format("%s(%s,%s)", super.toString(), this.min, this.max);
    }

    private static List<Expr> createChildren(AtomicInteger counter, Expr child, int min, int max) {
      checknotnull(child);
      checkcond(min <= max);
      List<Expr> ret = new LinkedList<Expr>();
      for (int i = 0; i < min; i++) {
        ret.add(cloneIfAlt(counter, child));
      }
      repeat(counter, ret, child, max - min);
      return ret;
    }

    private static Expr repeat(AtomicInteger counter, List<Expr> exprs, Expr expr, int times) {
      if (times == 0) {
        return EMPTY;
      }
      exprs.add(new Alt(counter, asList(cloneIfAlt(counter, expr), repeat(counter, exprs, expr, times - 1))));
      return exprs.get(exprs.size() - 1);
    }

    private static Expr cloneIfAlt(final AtomicInteger counter, Expr cur) {
      if (cur instanceof Alt) {
        return new Alt(counter, transform(((Alt) cur).getChildren(), new Form<Expr, Expr>() {
          @Override
          public Expr apply(Expr in) {
            return cloneIfAlt(counter, in);
          }
        }));
      }
      return cur;
    }
  }

  abstract class Composite extends Base implements Expr {
    private final List<Expr> children;

    Composite(AtomicInteger counter, List<Expr> children) {
      super(counter);
      this.children = children;
    }

    public List<Expr> getChildren() {
      return this.children;
    }

    public String toString() {
      return format("%s:%s", name(), this.getChildren());
    }

    public String name() {
      return this.getClass().getSimpleName().toLowerCase();
    }
  }

  interface Visitor {
    void visit(Alt exp);

    void visit(Cat exp);

    void visit(Leaf leaf);
  }

  class Factory {
    private final AtomicInteger counter;

    public Factory() {
      this.counter = new AtomicInteger(1);
    }

    public Expr leaf(Object value) {
      return new Leaf(counter, value);
    }

    public Expr cat(List exps) {
      return new Cat(this.counter, transform(exps, new Form<Object, Expr>() {
        public Expr apply(Object in) {
          if (in instanceof Expr) {
            return (Expr) in;
          }
          return new Leaf(counter, in);
        }
      }));
    }

    public Expr alt(List exps) {
      return new Alt(counter, transform(exps, new Form<Object, Expr>() {
        public Expr apply(Object in) {
          if (in instanceof Expr) {
            return (Expr) in;
          }
          return new Leaf(counter, in);
        }
      }));
    }

    public Expr rep(Object exp, int min, int max) {
      return new Rep(counter, exp instanceof Expr ? (Expr) exp : new Leaf(counter, exp), min, max);
    }
  }
}
