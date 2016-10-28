package com.github.dakusui.jcunit.plugins.caengines.ipogc;

import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.tuples.TupleUtils;
import com.github.dakusui.jcunit.core.utils.StringUtils;
import com.github.dakusui.jcunit.core.utils.Utils;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;
import com.github.dakusui.jcunit.plugins.constraints.Constraint;
import com.github.dakusui.jcunit.runners.standard.TestCaseUtils;

import java.util.*;

import static com.github.dakusui.jcunit.core.tuples.TupleUtils.enumerateCartesianProduct;
import static com.github.dakusui.jcunit.core.tuples.TupleUtils.sortStably;
import static com.github.dakusui.jcunit.core.utils.Checks.wraptesterror;
import static com.github.dakusui.jcunit.core.utils.Utils.filter;
import static com.github.dakusui.jcunit.core.utils.Utils.transform;

class GroupedFactor extends Factor {
  private final List<Factor> subfactors;

  GroupedFactor(List<Factor> subfactors, List<Constraint> constraints, int strength) {
    super(
        composeFactorName(subfactors),
        sortStably(TestCaseUtils.optimize(composeLevels(subfactors, constraints), strength), subfactors));
    this.subfactors = Collections.unmodifiableList(subfactors);
  }

  List<Tuple> allPossibleTuples(int strength) {
    Set<Tuple> work = new LinkedHashSet<Tuple>();
    for (Object each : this.levels) {
      work.addAll(TupleUtils.subtuplesOf((Tuple) each, strength));
    }
    return sortStably(new ArrayList<Tuple>(work), getSubfactors());
  }

  List<Factor> getSubfactors() {
    return this.subfactors;
  }

  List<String> getSubfactorNames() {
    return transform(this.subfactors, new Utils.Form<Factor, String>() {
      @Override
      public String apply(Factor in) {
        return in.name;
      }
    });
  }

  private static List<Tuple> composeLevels(final List<Factor> subfactors, final List<Constraint> constraints) {
    return filter(enumerateCartesianProduct(new Tuple.Impl(), subfactors.toArray(new Factor[subfactors.size()])),
        new Utils.Predicate<Tuple>() {
          @Override
          public boolean apply(Tuple in) {
            for (Constraint each : constraints) {
              try {
                if (!each.check(in)) {
                  return false;
                }
              } catch (UndefinedSymbol undefinedSymbol) {
                throw wraptesterror(undefinedSymbol,
                    "A constraint '%s' threw '%s' (missing %s). Maybe it is not annotated appropriately.",
                    each, undefinedSymbol, undefinedSymbol.missingSymbols);
              }
            }
            return true;
          }
        });
  }

  private static String composeFactorName(List<Factor> subfactors) {
    return StringUtils.join("+", transform(subfactors, new Utils.Form<Factor, String>() {
      @Override
      public String apply(Factor in) {
        return in.name;
      }
    }));
  }
}
