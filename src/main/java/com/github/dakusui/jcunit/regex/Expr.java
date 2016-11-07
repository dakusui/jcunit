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

  class Rep extends Cat {
    static final Cat EMPTY = new Cat(Collections.<Expr>emptyList());

    Rep(Expr child, int min, int max) {
      super(createChildren(child, min, max));
    }

    private static List<Expr> createChildren(Expr child, int min, int max) {
      checknotnull(child);
      checkcond(min <= max);
      List<Expr> ret = new LinkedList<Expr>();
      for (int i = 0; i < min; i++) {
        ret.add(cloneIfAlt(child));
      }
      repeat(ret, child, max - min);
      return ret;
    }

    private static Expr repeat(List<Expr> exprs, Expr expr, int times) {
      if (times == 0) {
        return EMPTY;
      }
      exprs.add(new Alt(asList(cloneIfAlt(expr), repeat(exprs, expr, times - 1))));
      return exprs.get(exprs.size() - 1);
    }

    private static Expr cloneIfAlt(Expr cur) {
      if (cur instanceof Alt) {
        return new Alt(transform(((Alt) cur).getChildren(), new Form<Expr, Expr>() {
          @Override
          public Expr apply(Expr in) {
            return cloneIfAlt(in);
          }
        }));
      }
      return cur;
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

    void visit(Leaf leaf);
  }

  enum Factory {
    ;

    public static Expr cat(Object... exps) {
      return new Cat(transform(asList(exps), new Form<Object, Expr>() {
        public Expr apply(Object in) {
          if (in instanceof Expr) {
            return (Expr) in;
          }
          return new Leaf(in);
        }
      }));
    }

    public static Expr alt(Object... exps) {
      return new Alt(transform(asList(exps), new Form<Object, Expr>() {
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
