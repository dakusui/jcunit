package com.github.dakusui.jcunit.coverage;

import com.github.dakusui.jcunit.core.utils.Checks;
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
      @Param(source = Param.Source.CONTEXT, contextKey = RunnerContext.Key.FACTORS) Factors factors,
      @Param(source = Param.Source.CONTEXT, contextKey = RunnerContext.Key.CONSTRAINT_CHECKER) ConstraintChecker constraintChecker,
      @Param(source = Param.Source.CONFIG) int degree) {
    Checks.checknotnull(factors);
    Checks.checkcond(degree > 0 && factors.size() >= degree);
    this.factors = Checks.checknotnull(factors);
    this.cm = Checks.checknotnull(constraintChecker);
    this.degree = degree;
  }

  @Item
  public CoverageMetric<Tuple, Tuple> combinatorialCoverage() {
    return new CoverageMetric<Tuple, Tuple>(new HashSet<Tuple>(this.factors.generateAllPossibleTuples(degree))) {
      @Override
      public String name() {
        return "Combinatorial coverage";
      }

      @Override
      protected Set<Tuple> getCoveredItemsBy(Tuple tuple) {
        return TupleUtils.subtuplesOf(tuple, CombinatorialMetrics.this.degree);
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
