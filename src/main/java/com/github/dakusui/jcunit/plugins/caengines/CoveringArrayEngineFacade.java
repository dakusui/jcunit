package com.github.dakusui.jcunit.plugins.caengines;

import com.github.dakusui.jcunit.core.CoreBuilder;
import com.github.dakusui.jcunit.core.factor.FactorSource;
import com.github.dakusui.jcunit.core.factor.FactorSpace;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.fsm.FSM;
import com.github.dakusui.jcunit.fsm.FSMFactors;
import com.github.dakusui.jcunit.runners.core.RunnerContext;
import com.github.dakusui.jcunit.runners.standard.annotations.GenerateCoveringArrayWith;
import com.github.dakusui.jcunit.runners.standard.annotations.Generator;

import java.util.List;
import java.util.Map;

import static com.github.dakusui.jcunit.core.Checks.checknotnull;

public class CoveringArrayEngineFacade {
  private final FactorSpace               factorSpace;
  private final CoveringArrayEngine       engine;
  private final List<FactorSource.Fsm>    fsmFactorSources;

  private CoveringArrayEngineFacade(
      FactorSpace factorSpace,
      CoveringArrayEngine engine,
      List<FactorSource.Fsm> fsmFactorSources) {
    this.factorSpace = factorSpace;
    this.engine = engine;
    this.fsmFactorSources = fsmFactorSources;
  }

  public CoveringArray generate() {
    final CoveringArray inner;
    CoveringArray ret = new CoveringArray.Base(
        inner = this.engine.generate(this.factorSpace)
    ) {
      @Override
      public Tuple get(int elementId) {
        return inner.get(elementId);
      }
    };
    return ret;
  }

  public static class Builder implements CoreBuilder<CoveringArrayEngineFacade> {
    private final GenerateCoveringArrayWith annotation;
    private final RunnerContext runnerContext;

    public Builder(
        GenerateCoveringArrayWith ann,
        RunnerContext runnerContext
    ) {
      this.annotation = checknotnull(ann);
      this.runnerContext = checknotnull(runnerContext);
    }


    @Override
    public CoveringArrayEngineFacade build() {
      // Annotation
      // -> Config Values
      // -> Test Class
      //  -> Factors -> FSM Expansion
      // Constraint
      // Coverage report

      // expand FSMs to flatten factors.
      // construct constraint structure.
      //  = Base (user-defined) constraint + FSM constraint + local constraints

      // let underlying engine compute covering array.

      // collapse FSM factors into Story objects

      // construct FSM factors

      CoveringArrayEngine engine = new Generator.Base(this.annotation.engine(), this.runnerContext).build();

      return null;
    }

    private static FSMFactors buildFSMFactors(Factors baseFactors, Map<String, FSM> fsms) {
      FSMFactors.Builder b = new FSMFactors.Builder();
      for (Map.Entry<String, FSM> each : fsms.entrySet()) {
        b.addFSM(each.getKey(), each.getValue());
      }
      return b.setBaseFactors(baseFactors).build();
    }
  }
}
