package com.github.dakusui.jcunitx.pipeline.stages;

import com.github.dakusui.jcunitx.core.AArray;
import com.github.dakusui.jcunitx.utils.Utils;
import com.github.dakusui.jcunitx.factorspace.FactorSpace;
import com.github.dakusui.jcunitx.pipeline.Requirement;
import com.github.dakusui.jcunitx.pipeline.stages.generators.Cartesian;
import com.github.dakusui.jcunitx.pipeline.stages.generators.IpoGplus;

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

  List<AArray> generate();

  abstract class Base implements Generator {
    protected final FactorSpace factorSpace;
    protected final Requirement requirement;

    protected Base(FactorSpace factorSpace, Requirement requirement) {
      this.factorSpace = factorSpace;
      this.requirement = requirement;
    }

    public final List<AArray> generate() {
      this.validate();
      if (this.factorSpace.getFactors().stream().anyMatch(each -> each.getLevels().isEmpty()))
        return Collections.emptyList();
      return Utils.unique(generateCore());
    }

    protected void validate() {
    }

    protected abstract List<AArray> generateCore();
  }

  interface Factory {
    Generator create(FactorSpace factorSpace, Requirement requirement, List<AArray> encodedSeeds);

    class Standard implements Factory {
      @Override
      public Generator create(FactorSpace factorSpace, Requirement requirement, List<AArray> encodedSeeds) {
        if (requirement.strength() < factorSpace.getFactors().size()) {
          return new IpoGplus(factorSpace, requirement, encodedSeeds);
        }
        return new Cartesian(factorSpace, requirement);
      }
    }
  }
}
