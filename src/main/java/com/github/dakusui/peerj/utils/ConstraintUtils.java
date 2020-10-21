package com.github.dakusui.peerj.utils;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.peerj.model.NormalizedConstraint;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.github.dakusui.jcunit.core.utils.Checks.checkcond;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;

public enum ConstraintUtils {
  ;

  public static NormalizedConstraint or(NormalizedConstraint... constraints) {
    return new NormalizedConstraint() {
      @Override
      public String toText(Function<String, String> factorNameToParameterName) {
        return Arrays.stream(constraints)
            .map(each -> each.toText(factorNameToParameterName))
            .collect(joining(" || "));
      }

      @Override
      public String getName() {
        throw new UnsupportedOperationException();
      }

      @Override
      public boolean test(Tuple tuple) {
        for (NormalizedConstraint each : constraints) {
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
            .collect(Collectors.toList());
      }

      @Override
      public String toString() {
        return String.format("%s", this);
      }
    };
  }

  public static NormalizedConstraint and(NormalizedConstraint... constraints) {
    return new NormalizedConstraint() {
      @Override
      public String toText(Function<String, String> factorNameToParameterName) {
        return Arrays.stream(constraints)
            .map(each -> each.toText(factorNameToParameterName))
            .collect(joining(" &amp;&amp; "));
      }

      @Override
      public String getName() {
        throw new UnsupportedOperationException();
      }

      @Override
      public boolean test(Tuple tuple) {
        for (NormalizedConstraint each : constraints) {
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
            .collect(Collectors.toList());
      }

      @Override
      public String toString() {
        return String.format("%s", this);
      }
    };
  }

  public static NormalizedConstraint gt(String f, String g) {
    return new Comp(f, g) {

      @Override
      public String getName() {
        throw new UnsupportedOperationException();
      }
    };
  }

  public static NormalizedConstraint ge(String f, String g) {
    return new NormalizedConstraint() {
      @Override
      public String toText(Function<String, String> factorNameNormalizer) {
        ////
        // Since ACTS seems not supporting > (&gt;), invert the comparator.
        return factorNameNormalizer.apply(g) + " &lt;= " + factorNameNormalizer.apply(f);
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
        return asList(f, g);
      }

      @Override
      public String toString() {
        return String.format("%s", this);
      }
    };
  }

  public static NormalizedConstraint eq(String f, String g) {
    return new NormalizedConstraint() {
      @Override
      public String toText(Function<String, String> factorNameNormalizer) {
        ////
        // Since ACTS seems not supporting > (&gt;), invert the comparator.
        return factorNameNormalizer.apply(g) + " == " + factorNameNormalizer.apply(f);
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
        return asList(f, g);
      }

      @Override
      public String toString() {
        return String.format("%s", this);
      }
    };
  }

  public static NormalizedConstraint neq(String f, String g) {
    return new NormalizedConstraint() {
      @Override
      public String toText(Function<String, String> factorNameNormalizer) {
        ////
        // Since ACTS seems not supporting > (&gt;), invert the comparator.
        return factorNameNormalizer.apply(g) + " != " + factorNameNormalizer.apply(f);
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
        return asList(f, g);
      }

      @Override
      public String toString() {
        return String.format("%s", this);
      }
    };
  }

  abstract static class Comp implements NormalizedConstraint {
    private final String g;
    private final String f;

    public Comp(String f, String g) {
      this.g = g;
      this.f = f;
    }

    @Override
    public String toText(Function<String, String> factorNameNormalizer) {
      ////
      // Since ACTS seems not supporting > (&gt;), invert the comparator.
      return toText(factorNameNormalizer.apply(g), factorNameNormalizer.apply(f));
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
      return asList(f, g);
    }

    @Override
    public String toString() {
      return String.format("%s", this);
    }
  }
}
