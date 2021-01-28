package com.github.dakusui.peerj.model;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.Constraint;

import java.util.Arrays;
import java.util.Formattable;
import java.util.Formatter;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public interface NormalizableConstraint extends Constraint, Formattable {
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

    static abstract class ForJunction extends NormalizableConstraint.Base {
      final NormalizableConstraint[] constraints;

      protected ForJunction(NormalizableConstraint[] constraints) {
        this.constraints = constraints;
      }

      @Override
      public List<String> involvedKeys() {
        return Arrays.stream(constraints)
            .flatMap(each -> each.involvedKeys().stream())
            .distinct()
            .collect(toList());
      }
    }

    static abstract class ForComparison extends NormalizableConstraint.Base {
      final String f;
      final String g;

      protected ForComparison(String f, String g) {
        this.f = f;
        this.g = g;
      }

      @Override
      final public List<String> involvedKeys() {
        return Stream.of(f, g)
            .filter(n -> n.matches("^[A-Za-z]+.*"))
            .collect(toList());
      }
    }
  }

  interface Or extends NormalizableConstraint {
    abstract class Base extends NormalizableConstraint.Base.ForJunction implements Or {
      protected Base(NormalizableConstraint[] constraints) {
        super(constraints);
      }

      @Override
      public boolean test(Tuple tuple) {
        for (NormalizableConstraint each : constraints) {
          if (each.test(tuple))
            return true;
        }
        return false;
      }
    }
  }

  interface And extends NormalizableConstraint {
    abstract class Base extends NormalizableConstraint.Base.ForJunction implements And {
      protected Base(NormalizableConstraint[] constraints) {
        super(constraints);
      }

      @Override
      public boolean test(Tuple tuple) {
        for (NormalizableConstraint each : this.constraints) {
          if (each.test(tuple))
            return false;
        }
        return true;
      }
    }
  }

  interface GreaterThan extends NormalizableConstraint {
    abstract class Base extends NormalizableConstraint.Base.ForComparison implements GreaterThan {
      protected Base(String f, String g) {
        super(f, g);
      }
    }
  }

  interface GreaterThanOrEqualTo extends NormalizableConstraint {
    abstract class Base extends NormalizableConstraint.Base.ForComparison implements GreaterThanOrEqualTo {
      protected Base(String f, String g) {
        super(f, g);
      }
    }
  }

  interface EqualTo extends NormalizableConstraint {
    abstract class Base extends NormalizableConstraint.Base.ForComparison implements EqualTo {
      protected Base(String f, String g) {
        super(f, g);
      }
    }
  }

  interface NotEqualTo extends NormalizableConstraint {
    abstract class Base extends NormalizableConstraint.Base.ForComparison implements NotEqualTo {
      protected Base(String f, String g) {
        super(f, g);
      }
    }
  }
}
