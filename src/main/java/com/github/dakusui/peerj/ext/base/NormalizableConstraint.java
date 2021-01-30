package com.github.dakusui.peerj.ext.base;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.Constraint;

import java.util.Formattable;
import java.util.Formatter;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.github.dakusui.pcond.Preconditions.require;
import static com.github.dakusui.pcond.functions.Predicates.isInstanceOf;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

public interface NormalizableConstraint extends Constraint, Formattable {
  void accept(Visitor formatter);

  String toText(Function<String, String> termNormalizer);

  @Override
  default void formatTo(Formatter formatter, int flags, int width, int precision) {
    formatter.format("%s", this.toText(Function.identity()));
  }

  abstract class Base implements NormalizableConstraint {
    @Override
    final public String getName() {
      throw new UnsupportedOperationException();
    }

    @Override
    final public String toString() {
      return String.format("%s", this);
    }
  }

  interface ForJunction extends NormalizableConstraint {
    List<NormalizableConstraint> constraints();

    abstract class Base extends NormalizableConstraint.Base implements ForJunction {
      private final NormalizableConstraint[] constraints;

      protected Base(NormalizableConstraint[] constraints) {
        this.constraints = constraints;
      }

      @Override
      public List<NormalizableConstraint> constraints() {
        return asList(this.constraints);
      }

      @Override
      public List<String> involvedKeys() {
        return constraints()
            .stream()
            .flatMap(each -> each.involvedKeys().stream())
            .distinct()
            .collect(toList());
      }
    }
  }

  interface ForComparison extends NormalizableConstraint {
    String leftTerm();
    String rightTerm();

    abstract class Base extends NormalizableConstraint.Base implements ForComparison {
      private final String leftTerm;
      private final String rightTerm;

      protected Base(String leftTerm, String rightTerm) {
        this.leftTerm = require(leftTerm, isInstanceOf(Comparable.class));
        this.rightTerm = require(rightTerm, isInstanceOf(Comparable.class));
      }

      @Override
      public String leftTerm() {
        return this.leftTerm;
      }

      @Override
      public String rightTerm() {
        return this.rightTerm;
      }

      @Override
      final public List<String> involvedKeys() {
        return Stream.of(leftTerm, rightTerm)
            .filter(n -> n.matches("^[A-Za-z]+.*"))
            .collect(toList());
      }
    }
  }

  interface Or extends NormalizableConstraint.ForJunction {
    @Override
    default void accept(Visitor formatter) {
      formatter.visit(this);
    }

    abstract class Base extends ForJunction.Base implements Or {
      protected Base(NormalizableConstraint[] constraints) {
        super(constraints);
      }

      @Override
      public boolean test(Tuple tuple) {
        for (NormalizableConstraint each : constraints()) {
          if (each.test(tuple))
            return true;
        }
        return false;
      }
    }
  }

  interface And extends NormalizableConstraint.ForJunction {
    @Override
    default void accept(Visitor formatter) {
      formatter.visit(this);
    }

    abstract class Base extends ForJunction.Base implements And {
      protected Base(NormalizableConstraint[] constraints) {
        super(constraints);
      }

      @Override
      public boolean test(Tuple tuple) {
        for (NormalizableConstraint each : this.constraints()) {
          if (each.test(tuple))
            return false;
        }
        return true;
      }
    }
  }

  interface GreaterThan extends NormalizableConstraint.ForComparison {
    @Override
    default void accept(Visitor formatter) {
      formatter.visit(this);
    }

    abstract class Base extends ForComparison.Base implements GreaterThan {
      protected Base(String f, String g) {
        super(f, g);
      }

      @Override
      public boolean test(Tuple tuple) {
        return compare(leftTerm(), rightTerm());
      }

      @SuppressWarnings({ "rawtypes", "unchecked" })
      private boolean compare(Comparable f, Comparable g) {
        return f.compareTo(g) > 0;
      }
    }
  }

  interface GreaterThanOrEqualTo extends NormalizableConstraint.ForComparison {
    @Override
    default void accept(Visitor formatter) {
      formatter.visit(this);
    }

    abstract class Base extends ForComparison.Base implements GreaterThanOrEqualTo {
      protected Base(String f, String g) {
        super(f, g);
      }

      @SuppressWarnings({ "unchecked", "rawtypes" })
      @Override
      public boolean test(Tuple tuple) {
        return ((Comparable) leftTerm()).compareTo(rightTerm()) >= 0;
      }
    }
  }

  interface EqualTo extends NormalizableConstraint.ForComparison {
    @Override
    default void accept(Visitor formatter) {
      formatter.visit(this);
    }

    abstract class Base extends ForComparison.Base implements EqualTo {
      protected Base(String f, String g) {
        super(f, g);
      }

      @SuppressWarnings({ "unchecked", "rawtypes" })
      @Override
      public boolean test(Tuple tuple) {
        return ((Comparable) leftTerm()).compareTo(rightTerm()) == 0;
      }
    }
  }

  interface NotEqualTo extends NormalizableConstraint.ForComparison {
    @Override
    default void accept(Visitor formatter) {
      formatter.visit(this);
    }

    abstract class Base extends ForComparison.Base implements NotEqualTo {
      protected Base(String f, String g) {
        super(f, g);
      }

      @SuppressWarnings({ "unchecked", "rawtypes" })
      @Override
      public boolean test(Tuple tuple) {
        return ((Comparable) leftTerm()).compareTo(rightTerm()) == 0;
      }
    }
  }

  interface Visitor {
    void visit(Or constraint);
    void visit(And constraint);
    void visit(GreaterThan constraint);
    void visit(GreaterThanOrEqualTo constraint);
    void visit(EqualTo constraint);
    void visit(NotEqualTo constraint);
  }
}
