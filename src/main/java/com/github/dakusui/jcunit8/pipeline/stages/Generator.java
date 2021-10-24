package com.github.dakusui.jcunit8.pipeline.stages;

import com.github.dakusui.jcunit.core.tuples.KeyValuePairs;
import com.github.dakusui.jcunit.core.tuples.Row;
import com.github.dakusui.jcunit8.core.Utils;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.generators.Cartesian;
import com.github.dakusui.jcunit8.pipeline.stages.generators.IpoGplus;

import java.util.Collections;
import java.util.List;

/**
 */
public interface Generator {
  Object DontCare = new Object() {
    @Override
    public final String toString() {
      return "D/C";
    }
  };
  Object VOID     = new Object() {
    @Override
    public final String toString() {
      return "(VOID)";
    }
  };

  List<Row> generate();

  abstract class Base implements Generator {
    protected final FactorSpace factorSpace;
    protected final Requirement requirement;

    protected Base(FactorSpace factorSpace, Requirement requirement) {
      this.factorSpace = factorSpace;
      this.requirement = requirement;
    }

    public final List<Row> generate() {
      this.validate();
      if (this.factorSpace.getFactors().stream().anyMatch(each -> each.getLevels().isEmpty()))
        return Collections.emptyList();
      return Utils.unique(generateCore());
    }

    protected void validate() {
    }

    protected abstract List<Row> generateCore();
  }

  interface Factory {
    Generator create(FactorSpace factorSpace, Requirement requirement, List<Row> encodedSeeds);

    class Standard implements Factory {
      @Override
      public Generator create(FactorSpace factorSpace, Requirement requirement, List<Row> encodedSeeds) {
        if (requirement.strength() < factorSpace.getFactors().size()) {
          return new IpoGplus(factorSpace, requirement, encodedSeeds);
        }
        return new Cartesian(factorSpace, requirement);
      }
    }
  }
}
