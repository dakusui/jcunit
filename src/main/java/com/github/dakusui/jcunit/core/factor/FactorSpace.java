package com.github.dakusui.jcunit.core.factor;

import com.github.dakusui.jcunit.core.CoreBuilder;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;
import com.github.dakusui.jcunit.plugins.constraints.ConstraintChecker;
import com.github.dakusui.jcunit.plugins.levelsproviders.SimpleLevelsProvider;

import java.util.Collections;
import java.util.List;

public class FactorSpace {
  public final Factors           factors;
  public final ConstraintChecker constraintChecker;
  private final List<FactorDef> factorDefs;

  public FactorSpace(List<FactorDef> factorDefs, ConstraintChecker constraintChecker) {
    this.factorDefs = Collections.unmodifiableList(factorDefs);
    Factors.Builder b = new Factors.Builder();
    for (FactorDef eachDesc : factorDefs) {
      eachDesc.addTo(b);
    }
    this.factors = b.build();
    this.constraintChecker = constraintChecker;
  }

  public Tuple convert(Tuple tuple) {
    Tuple.Builder b = new Tuple.Builder();
    for (FactorDef each : factorDefs) {
      each.compose(b, tuple);
    }
    return b.build();
  }

  public static List<FactorDef> convertFactorsIntoSimpleFactorDefs(Factors factors) {
    List<FactorDef> ret = Utils.newList();
    for (final Factor each : factors) {
      ret.add(new FactorDef.Simple(each.name, new SimpleLevelsProvider() {
        @Override
        protected Object[] values() {
          return each.levels.toArray();
        }
      }));
    }
    return ret;
  }

  public static class Builder implements CoreBuilder<FactorSpace> {

    private final List<FactorDef> factorDefs;

    private ConstraintChecker topLevelConstraintChecker;

    public Builder() {
      this.factorDefs = Utils.newList();
    }

    public Builder addFactorDefs(List<FactorDef> defs) {
      this.factorDefs.addAll(defs);
      return this;
    }

    public Builder setTopLevelConstraintChecker(ConstraintChecker topLevelConstraintChecker) {
      this.topLevelConstraintChecker = topLevelConstraintChecker;
      return this;
    }

    @Override
    public FactorSpace build() {
      final List<ConstraintChecker> constraintCheckers = Utils.transform(
          factorDefs,
          new Utils.Form<FactorDef, ConstraintChecker>() {
            @Override
            public ConstraintChecker apply(FactorDef in) {
              return in.createConstraintChecker();
            }
          }
      );
      ConstraintChecker constraintChecker = new ConstraintChecker.Base() {
        @Override
        public boolean check(Tuple tuple) throws UndefinedSymbol {
          if (!topLevelConstraintChecker.check(tuple))
            return false;
          for (ConstraintChecker each : constraintCheckers) {
            if (!each.check(tuple))
              return false;
          }
          return true;
        }
      };
      return new FactorSpace(
          this.factorDefs,
          constraintChecker
      );
    }
  }
}
