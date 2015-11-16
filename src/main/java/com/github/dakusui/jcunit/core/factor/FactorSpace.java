package com.github.dakusui.jcunit.core.factor;

import com.github.dakusui.jcunit.core.CoreBuilder;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;
import com.github.dakusui.jcunit.plugins.constraints.ConstraintChecker;

import java.util.List;

public class FactorSpace {
  public final Factors           factors;
  public final ConstraintChecker constraintChecker;

  public FactorSpace(Factors factors, ConstraintChecker constraintChecker) {
    this.factors = factors;
    this.constraintChecker = constraintChecker;
  }

  public static class Builder implements CoreBuilder<FactorSpace> {

    private final List<FactorDef> descs;

    private ConstraintChecker topLevelConstraintChecker;

    public Builder() {
      this.descs = Utils.newList();
    }

    public Builder addFactorDefs(List<FactorDef<?>> defs) {
      this.descs.addAll(defs);
      return this;
    }

    public Builder setTopLevelConstraintChecker(ConstraintChecker topLevelConstraintChecker) {
      this.topLevelConstraintChecker = topLevelConstraintChecker;
      return this;
    }

    @Override
    public FactorSpace build() {
      Factors.Builder b = new Factors.Builder();
      for (FactorDef<?> eachDesc : descs) {
        eachDesc.addTo(b);
      }
      Factors factors = b.build();
      ConstraintChecker constraintChecker = new ConstraintChecker.Base() {
        @Override
        public boolean check(Tuple tuple) throws UndefinedSymbol {
          if (!topLevelConstraintChecker.check(tuple))
            return false;
          for (FactorDef<?> each : descs) {
            if (each instanceof FactorDef.Fsm) {
              // TODO: FIXME
              /*
              if (!((FactorDef.Fsm) each).getConstraintChecker().check(tuple)) {
                return false;
              }
              */
            }
          }
          return true;
        }
      };
      return new FactorSpace(
          factors,
          constraintChecker
      );
    }
  }
}
