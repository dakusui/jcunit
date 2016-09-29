package com.github.dakusui.jcunit.plugins.caengines.ipo3;

import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.utils.Checks;
import com.github.dakusui.jcunit.core.utils.StringUtils;
import com.github.dakusui.jcunit.core.utils.Utils;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;
import com.github.dakusui.jcunit.plugins.caengines.ipo2.Ipo;
import com.github.dakusui.jcunit.plugins.constraints.Constraint;
import com.github.dakusui.jcunit.plugins.constraints.ConstraintChecker;

import java.util.*;

import static com.github.dakusui.jcunit.core.utils.Utils.transformLazily;

public class Ipo3 extends Ipo {
  static class AggregatedFactor extends Factor {

    private final List<Factor> subfactors;

    private AggregatedFactor(List<Factor> subfactors, List<Constraint> constraints) {
      super(composeFactorName(subfactors), composeLevels(subfactors, constraints));
      this.subfactors = Collections.unmodifiableList(subfactors);
    }

    private static List<Tuple> composeLevels(List<Factor> subfactors, List<Constraint> constraints) {
      // FIXME: 9/30/16 Implement this
      return null;
    }

    private static String composeFactorName(List<Factor> subfactors) {
      // FIXME: 9/30/16 Implement this
      return null;
    }

    List<Factor> getSubfactors() {
      return this.subfactors;
    }
  }

  public Ipo3(int strength, ConstraintChecker constraintChecker, Factors factors) {
    super(strength, constraintChecker, factors);
  }

  @Override
  public Result ipo() {
    if (factors.size() == this.strength) {
      return new Result(
          Collections.<Tuple>emptyList(),
          generateAllPossibleTuples(factors.asFactorList(), filterViolations(constraintChecker)));
    }
    ////
    // Group factors used by constraints so that each group disjoints each other.
    LinkedHashMap<Factor, List<Factor>> aggregatedFactors = new LinkedHashMap<Factor, List<Factor>>();
    for (List<Factor> eachFactorGroup : transformLazily(this.groupFactorNames(constraintChecker.getConstraints()), transformFactorNamesToFactors())) {
      aggregatedFactors.put(
          aggregateFactors(constraintChecker).apply(eachFactorGroup),
          eachFactorGroup);
    }
    return null;
  }

  protected static Utils.Predicate<Tuple> filterViolations(final ConstraintChecker constraintChecker) {
    return new Utils.Predicate<Tuple>() {
      @Override
      public boolean apply(Tuple in) {
        try {
          return constraintChecker.check(in);
        } catch (UndefinedSymbol undefinedSymbol) {
          // This should not happen through usual usage scenario.
          throw Checks.wrap(undefinedSymbol);
        }
      }
    };
  }

  private static Utils.Form<List<Factor>, Factor> aggregateFactors(final ConstraintChecker constraintChecker) {
    return new Utils.Form<List<Factor>, Factor>() {
      /**
       *
       * @param in Already grouped factors
       */
      @Override
      public Factor apply(List<Factor> in) {
        String factorName = StringUtils.join("+", in);
        Factor.Builder builder = new Factor.Builder(factorName);
        for (Tuple eachTuple : optimizeSubtuples(generateAllPossibleTuples(in, filterViolations(constraintChecker)))) {
          builder.addLevel(eachTuple);
        }
        return builder.build();
      }

      private List<Tuple> optimizeSubtuples(List<Tuple> tuples) {
        ////
        // fixme: Remove subtuples that do not contribute to combinatorial coverage.
        return tuples;
      }
    };
  }

  Utils.Form<List<String>, List<Factor>> transformFactorNamesToFactors() {
    return new Utils.Form<List<String>, List<Factor>>() {
      @Override
      public List<Factor> apply(List<String> in) {
        return transformLazily(in, new Utils.Form<String, Factor>() {
          @Override
          public Factor apply(String in) {
            return factors.get(in);
          }
        });
      }
    };
  }

  List<List<String>> groupFactorNames(List<Constraint> constraints) {
    ////
    // fixme: Implement this method.
    List<Set<String>> ret = new LinkedList<Set<String>>();
    for (Constraint each : constraints) {
      each.getFactorNamesInUse();
    }
    return null;
  }
}
