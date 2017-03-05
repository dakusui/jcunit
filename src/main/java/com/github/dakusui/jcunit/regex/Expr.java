package com.github.dakusui.jcunit.regex;

import com.github.dakusui.jcunit.core.utils.Utils.Form;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.dakusui.jcunit.core.utils.Utils.transform;
import static java.lang.String.format;

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
      this.id = composeId(counter);
    }

    String composeId(AtomicInteger counter) {
      return format("%s-%d", composeName(), counter.getAndIncrement());
    }

    String composeName() {
      return this.getClass().getSimpleName().toLowerCase();
    }

    public String id() {
      return id;
    }
  }

  class Empty extends Base implements Expr {
    static Empty INSTANCE = new Empty(new AtomicInteger(0));

    Empty(AtomicInteger counter) {
      super(counter);
    }

    @Override
    public void accept(Visitor visitor) {
      visitor.visit(this);
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


  class Rep extends Alt {
    private static final Expr EMPTY = Empty.INSTANCE;
    private final int min;
    private final int max;

    Rep(AtomicInteger counter, Expr child, int min, int max) {
      super(counter, createChildren(counter, child, min, max));
      this.min = min;
      this.max = max;
    }

    public String name() {
      return format("%s{%s,%s}", super.name(), this.min, this.max);
    }

    @Override
    public String toString() {
      return name();
    }

    private static List<Expr> createChildren(AtomicInteger counter, Expr child, int min, int max) {
      List<Expr> ret = new LinkedList<Expr>();
      for (int i = min; i <= max; i++) {
        if (i == 0) {
          ret.add(EMPTY);
        } else {
          List<Expr> work = new LinkedList<Expr>();
          for (int j = 0; j < min; j++) {
            work.add(child);
          }
          for (int j = min; j < i; j++) {
            work.add(cloneIfAlt(counter, child));
          }
          ret.add(
              work.size() == 1 ?
                  work.get(0) :
                  new Cat(counter, work)
          );
        }
      }
      return ret;
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

    void visit(Empty empty);
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
