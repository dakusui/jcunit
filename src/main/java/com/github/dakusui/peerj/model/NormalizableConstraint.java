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

  interface Or extends NormalizableConstraint {

  }

  interface And extends NormalizableConstraint {

  }

  interface GreaterThan extends NormalizableConstraint {

  }

  interface GreaterThanOrEqualTo extends NormalizableConstraint {

  }

  interface EqualTo extends NormalizableConstraint {

  }

  interface NotEqualTo extends NormalizableConstraint {

  }
}
