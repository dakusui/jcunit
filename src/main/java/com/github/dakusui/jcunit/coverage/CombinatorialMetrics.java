package com.github.dakusui.jcunit.coverage;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.factor.FactorSpace;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.tuples.TupleUtils;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;
import com.github.dakusui.jcunit.plugins.constraints.ConstraintChecker;
import com.github.dakusui.jcunit.runners.core.RunnerContext;

import java.util.HashSet;
import java.util.Set;

public class CombinatorialMetrics extends Metrics.Base<Tuple> {
  private final Factors           factors;
  private final ConstraintChecker cm;
  private final int               degree;

  public CombinatorialMetrics(
      @Param(source = Param.Source.CONTEXT, contextKey = RunnerContext.Key.FACTOR_SPACE) FactorSpace factorSpace,
      @Param(source = Param.Source.CONFIG) int degree) {
    Checks.checknotnull(factorSpace);
    Checks.checkcond(degree > 0 && factorSpace.factors.size() >= degree);
    this.factors = factorSpace.factors;
    this.cm = factorSpace.constraintChecker;
    this.degree = degree;
  }

  @Item
  public CoverageMetric<Tuple, Tuple> combinatorialCoverage() {
    return new CoverageMetric<Tuple, Tuple>(new HashSet<Tuple>(this.factors.generateAllPossibleTuples(degree))) {
      @Override
      protected Set<Tuple> getCoveredItemsBy(Tuple tuple) {
        return TupleUtils.subtuplesOf(tuple, CombinatorialMetrics.this.degree);
      }

      @Override
      public String name() {
        return "Combinatorial coverage";
      }
    };
  }

  @Item
  public RatioMetric<Tuple> violationRatio() {
    return new CountMetric<Tuple>() {
      @Override
      public String name() {
        return "Violation ratio";
      }

      @Override
      protected boolean matches(Tuple each) {
        try {
          return !cm.check(each);
        } catch (UndefinedSymbol undefinedSymbol) {
          throw Checks.wrap(undefinedSymbol, "Unknown symbol '%s' was given. This should not happen under JCUnit.", undefinedSymbol);
        }
      }
    };
  }
}
