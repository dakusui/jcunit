package com.github.dakusui.peerj.utils;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.peerj.model.FormalizableConstraint;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.github.dakusui.jcunit.core.utils.Checks.checkcond;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public enum ConstraintUtils {
  ;

  public static FormalizableConstraint or(FormalizableConstraint... constraints) {
    return new FormalizableConstraint() {
      @Override
      public String toText(Function<String, String> termNormalizer) {
        return Arrays.stream(constraints)
            .map((FormalizableConstraint each) -> each.toText(termNormalizer))
            .collect(joining(" || "));
      }

      @Override
      public String getName() {
        throw new UnsupportedOperationException();
      }

      @Override
      public boolean test(Tuple tuple) {
        for (FormalizableConstraint each : constraints) {
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

      @Override
      public String toString() {
        return String.format("%s", this);
      }
    };
  }

  public static FormalizableConstraint and(FormalizableConstraint... constraints) {
    return new FormalizableConstraint() {
      @Override
      public String toText(Function<String, String> termNormalizer) {
        return Arrays.stream(constraints)
            .map(each -> each.toText(termNormalizer))
            .collect(joining(" &amp;&amp; "));
      }

      @Override
      public String getName() {
        throw new UnsupportedOperationException();
      }

      @Override
      public boolean test(Tuple tuple) {
        for (FormalizableConstraint each : constraints) {
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

      @Override
      public String toString() {
        return String.format("%s", this);
      }
    };
  }

  public static FormalizableConstraint gt(String f, String g) {
    return new Comp(f, g) {

      @Override
      public String getName() {
        throw new UnsupportedOperationException();
      }
    };
  }

  public static FormalizableConstraint ge(String f, String g) {
    return new FormalizableConstraint() {
      @Override
      public String toText(Function<String, String> termNormalizer) {
        ////
        // Since ACTS seems not supporting > (&gt;), invert the comparator.
        return termNormalizer.apply(g) + " &lt;= " + termNormalizer.apply(f);
      }

      @Override
      public String getName() {
        throw new UnsupportedOperationException();
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

      @Override
      public String toString() {
        return String.format("%s", this);
      }
    };
  }

  public static FormalizableConstraint eq(String f, String g) {
    return new FormalizableConstraint() {
      @Override
      public String toText(Function<String, String> termNormalizer) {
        return termNormalizer.apply(f) + " == " + termNormalizer.apply(g);
      }

      @Override
      public String getName() {
        throw new UnsupportedOperationException();
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

      @Override
      public String toString() {
        return String.format("%s", this);
      }
    };
  }

  public static FormalizableConstraint neq(String f, String g) {
    return new FormalizableConstraint() {
      @Override
      public String toText(Function<String, String> termNormalizer) {
        return termNormalizer.apply(f) + " != " + termNormalizer.apply(g);
      }

      @Override
      public String getName() {
        throw new UnsupportedOperationException();
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

      @Override
      public String toString() {
        return String.format("%s", this);
      }
    };
  }

  abstract static class Comp implements FormalizableConstraint {
    private final String g;
    private final String f;

    public Comp(String f, String g) {
      this.g = g;
      this.f = f;
    }

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
    private static boolean compare(Comparable f, Comparable g) {
      return f.compareTo(g) > 0;
    }

    @Override
    public List<String> involvedKeys() {
      return Stream.of(f, g)
          .filter(n -> n.matches("^[A-Za-z]+.*"))
          .collect(toList());
    }

    @Override
    public String toString() {
      return String.format("%s", this);
    }
  }
}
