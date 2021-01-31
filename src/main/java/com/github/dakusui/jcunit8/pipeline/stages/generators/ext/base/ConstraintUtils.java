package com.github.dakusui.jcunit8.pipeline.stages.generators.ext.base;

public enum ConstraintUtils {
  ;

  public static NormalizableConstraint or(NormalizableConstraint... constraints) {
    return new NormalizableConstraint.Or.Base(constraints) {
    };
  }

  public static NormalizableConstraint and(NormalizableConstraint... constraints) {
    return new NormalizableConstraint.And.Impl(constraints);
  }

  public static NormalizableConstraint gt(String f, String g) {
    return new NormalizableConstraint.GreaterThan.Impl(f, g);
  }

  public static NormalizableConstraint ge(String f, String g) {
    return new NormalizableConstraint.GreaterThanOrEqualTo.Impl(f, g);
  }

  public static NormalizableConstraint eq(String f, String g) {
    return new NormalizableConstraint.EqualTo.Impl(f, g);
  }

  public static NormalizableConstraint neq(String f, String g) {
    return new NormalizableConstraint.NotEqualTo.Impl(f, g);
  }
}
