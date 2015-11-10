package com.github.dakusui.jcunit.plugins.caengines;

import com.github.dakusui.jcunit.core.CoreBuilder;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.factor.FactorDef;
import com.github.dakusui.jcunit.core.factor.FactorSpace;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.plugins.constraints.ConstraintChecker;
import com.github.dakusui.jcunit.runners.core.RunnerContext;
import com.github.dakusui.jcunit.runners.standard.annotations.Checker;
import com.github.dakusui.jcunit.runners.standard.annotations.GenerateCoveringArrayWith;
import com.github.dakusui.jcunit.runners.standard.annotations.Generator;

import java.util.List;

import static com.github.dakusui.jcunit.core.Checks.checknotnull;

public class CoveringArrayEngineFacade {
  private final FactorSpace         factorSpace;
  private final CoveringArrayEngine engine;
  private final List<FactorDef<?>>  factorDefs;

  private CoveringArrayEngineFacade(
      FactorSpace factorSpace,
      CoveringArrayEngine engine,
      List<FactorDef<?>> factorDefs) {
    this.factorSpace = factorSpace;
    this.engine = engine;
    this.factorDefs = factorDefs;
  }

  public CoveringArray generate() {
    final CoveringArray inner;
    return new CoveringArray.Base(
        inner = CoveringArrayEngineFacade.this.engine.generate(CoveringArrayEngineFacade.this.factorSpace)
    ) {
      @Override
      public Tuple get(int elementId) {
        Tuple.Builder b = new Tuple.Builder();
        for (FactorDef<?> each : factorDefs) {
          b.put(each.name, each.getValueFrom(inner.get(elementId)));
        }
        return b.build();
      }
    };
  }

  public static class Builder implements CoreBuilder<CoveringArrayEngineFacade> {
    private final GenerateCoveringArrayWith annotation;
    private final RunnerContext             runnerContext;
    private       List<FactorDef<?>>        factorDefs;

    public Builder(
        GenerateCoveringArrayWith ann,
        RunnerContext runnerContext
    ) {
      this.annotation = checknotnull(ann);
      this.runnerContext = checknotnull(runnerContext);
      this.factorDefs = Utils.newList();
    }

    public Builder add(FactorDef<?> factorDef) {
      this.factorDefs.add(factorDef);
      return this;
    }


    @Override
    public CoveringArrayEngineFacade build() {
      CoveringArrayEngine engine = new Generator.Base(this.annotation.engine(), this.runnerContext).build();
      ConstraintChecker checker = new Checker.Base(this.annotation.checker(), this.runnerContext).build();

      FactorSpace.Builder fsBuilder = new FactorSpace.Builder();
      fsBuilder.setTopLevelConstraintChecker(checker);
      fsBuilder.addFactorDefs(this.factorDefs);

      return new CoveringArrayEngineFacade(fsBuilder.build(), engine, factorDefs);
    }
  }
}
