package com.github.dakusui.peerj.model;

import com.github.dakusui.jcunit8.factorspace.Constraint;

import java.util.Formattable;
import java.util.Formatter;
import java.util.function.Function;

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
  }

  interface Or extends NormalizableConstraint {
    abstract class Base extends NormalizableConstraint.Base implements Or {
    }
  }

  interface And extends NormalizableConstraint {
    abstract class Base extends NormalizableConstraint.Base implements And {
    }
  }

  interface GreaterThan extends NormalizableConstraint {
    abstract class Base extends NormalizableConstraint.Base implements GreaterThan {
    }
  }

  interface GreaterThanOrEqualTo extends NormalizableConstraint {
    abstract class Base extends NormalizableConstraint.Base implements GreaterThanOrEqualTo {
    }
  }

  interface EqualTo extends NormalizableConstraint {
    abstract class Base extends NormalizableConstraint.Base implements EqualTo {
    }
  }

  interface NotEqualTo extends NormalizableConstraint {
    abstract class Base extends NormalizableConstraint.Base implements NotEqualTo {
    }
  }
}
