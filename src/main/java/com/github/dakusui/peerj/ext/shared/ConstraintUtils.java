package com.github.dakusui.peerj.ext.shared;

import com.github.dakusui.peerj.ext.shared.NormalizableConstraint;

import java.util.Arrays;
import java.util.function.Function;

import static java.util.stream.Collectors.joining;

public enum ConstraintUtils {
  ;

  public static NormalizableConstraint or(NormalizableConstraint... constraints) {
    return new NormalizableConstraint.Or.Base(constraints) {
      @Override
      public String toText(Function<String, String> termNormalizer) {
        return Arrays.stream(constraints)
            .map((NormalizableConstraint each) -> each.toText(termNormalizer))
            .collect(joining(" || "));
      }
    };
  }

  public static NormalizableConstraint and(NormalizableConstraint... constraints) {
    return new NormalizableConstraint.And.Base(constraints) {
      @Override
      public String toText(Function<String, String> termNormalizer) {
        return Arrays.stream(constraints)
            .map(each -> each.toText(termNormalizer))
            .collect(joining(" &amp;&amp; "));
      }
    };
  }

  public static NormalizableConstraint gt(String f, String g) {
    return new NormalizableConstraint.GreaterThan.Base(f, g) {
      @Override
      public String toText(Function<String, String> termNormalizer) {
        ////
        // Since ACTS seems not supporting > (&gt;), invert the comparator.
        return toText(termNormalizer.apply(g), termNormalizer.apply(f));
      }

      public String toText(String normalizedFactorNameForG, String normalizedFactorNameForF) {
        return normalizedFactorNameForG + " &lt; " + normalizedFactorNameForF;
      }
    };
  }

  public static NormalizableConstraint ge(String f, String g) {
    return new NormalizableConstraint.GreaterThanOrEqualTo.Base(f, g) {
      @Override
      public String toText(Function<String, String> termNormalizer) {
        ////
        // Since ACTS seems not supporting > (&gt;), invert the comparator.
        return termNormalizer.apply(g) + " &lt;= " + termNormalizer.apply(f);
      }
    };
  }

  public static NormalizableConstraint eq(String f, String g) {
    return new NormalizableConstraint.EqualTo.Base(f, g) {
      @Override
      public String toText(Function<String, String> termNormalizer) {
        return termNormalizer.apply(f) + " == " + termNormalizer.apply(g);
      }
    };
  }

  public static NormalizableConstraint neq(String f, String g) {
    return new NormalizableConstraint.NotEqualTo.Base(f, g) {
      @Override
      public String toText(Function<String, String> termNormalizer) {
        return termNormalizer.apply(f) + " != " + termNormalizer.apply(g);
      }
    };
  }
}
