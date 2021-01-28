package com.github.dakusui.peerj.utils;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.peerj.model.NormalizableConstraint;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.github.dakusui.jcunit.core.utils.Checks.checkcond;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public enum ConstraintUtils {
  ;

  public static NormalizableConstraint or(NormalizableConstraint... constraints) {
    return new NormalizableConstraint.Or.Base() {
      @Override
      public String toText(Function<String, String> termNormalizer) {
        return Arrays.stream(constraints)
            .map((NormalizableConstraint each) -> each.toText(termNormalizer))
            .collect(joining(" || "));
      }

      @Override
      public boolean test(Tuple tuple) {
        for (NormalizableConstraint each : constraints) {
          if (each.test(tuple))
            return true;
        }
        return false;
      }

      @Override
      public List<String> involvedKeys() {
        return Arrays.stream(constraints)
            .flatMap(each -> each.involvedKeys().stream())
            .distinct()
            .collect(toList());
      }
    };
  }

  public static NormalizableConstraint and(NormalizableConstraint... constraints) {
    return new NormalizableConstraint.And.Base() {
      @Override
      public String toText(Function<String, String> termNormalizer) {
        return Arrays.stream(constraints)
            .map(each -> each.toText(termNormalizer))
            .collect(joining(" &amp;&amp; "));
      }

      @Override
      public boolean test(Tuple tuple) {
        for (NormalizableConstraint each : constraints) {
          if (each.test(tuple))
            return false;
        }
        return true;
      }

      @Override
      public List<String> involvedKeys() {
        return Arrays.stream(constraints)
            .flatMap(each -> each.involvedKeys().stream())
            .distinct()
            .collect(toList());
      }
    };
  }

  public static NormalizableConstraint gt(String f, String g) {
    return new NormalizableConstraint.GreaterThan.Base() {
      @Override
      public String toText(Function<String, String> termNormalizer) {
        ////
        // Since ACTS seems not supporting > (&gt;), invert the comparator.
        return toText(termNormalizer.apply(g), termNormalizer.apply(f));
      }

      public String toText(String normalizedFactorNameForG, String normalizedFactorNameForF) {
        return normalizedFactorNameForG + " &lt; " + normalizedFactorNameForF;
      }

      @Override
      public boolean test(Tuple tuple) {
        checkcond(tuple.get(f) instanceof Comparable);
        checkcond(tuple.get(g) instanceof Comparable);
        return compare(f, g);
      }

      @SuppressWarnings({ "rawtypes", "unchecked" })
      private boolean compare(Comparable f, Comparable g) {
        return f.compareTo(g) > 0;
      }

      @Override
      public List<String> involvedKeys() {
        return Stream.of(f, g)
            .filter(n -> n.matches("^[A-Za-z]+.*"))
            .collect(toList());
      }
    };
  }

  public static NormalizableConstraint ge(String f, String g) {
    return new NormalizableConstraint.GreaterThanOrEqualTo.Base() {
      @Override
      public String toText(Function<String, String> termNormalizer) {
        ////
        // Since ACTS seems not supporting > (&gt;), invert the comparator.
        return termNormalizer.apply(g) + " &lt;= " + termNormalizer.apply(f);
      }

      @SuppressWarnings({ "unchecked", "rawtypes" })
      @Override
      public boolean test(Tuple tuple) {
        checkcond(tuple.get(f) instanceof Comparable);
        checkcond(tuple.get(g) instanceof Comparable);
        return ((Comparable) f).compareTo(g) >= 0;
      }

      @Override
      public List<String> involvedKeys() {
        return Stream.of(f, g)
            .filter(n -> n.matches("^[A-Za-z]+.*"))
            .collect(toList());
      }
    };
  }

  public static NormalizableConstraint eq(String f, String g) {
    return new NormalizableConstraint.EqualTo.Base() {
      @Override
      public String toText(Function<String, String> termNormalizer) {
        return termNormalizer.apply(f) + " == " + termNormalizer.apply(g);
      }

      @SuppressWarnings({ "unchecked", "rawtypes" })
      @Override
      public boolean test(Tuple tuple) {
        checkcond(tuple.get(f) instanceof Comparable);
        checkcond(tuple.get(g) instanceof Comparable);
        return ((Comparable) f).compareTo(g) == 0;
      }

      @Override
      public List<String> involvedKeys() {
        return Stream.of(f, g)
            .filter(n -> n.matches("^[A-Za-z]+.*"))
            .collect(toList());
      }
    };
  }

  public static NormalizableConstraint neq(String f, String g) {
    return new NormalizableConstraint.NotEqualTo.Base() {
      @Override
      public String toText(Function<String, String> termNormalizer) {
        return termNormalizer.apply(f) + " != " + termNormalizer.apply(g);
      }

      @SuppressWarnings({ "unchecked", "rawtypes" })
      @Override
      public boolean test(Tuple tuple) {
        checkcond(tuple.get(f) instanceof Comparable);
        checkcond(tuple.get(g) instanceof Comparable);
        return ((Comparable) f).compareTo(g) == 0;
      }

      @Override
      public List<String> involvedKeys() {
        return Stream.of(f, g)
            .filter(n -> n.matches("^[A-Za-z]+.*"))
            .collect(toList());
      }
    };
  }
}
