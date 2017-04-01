package com.github.dakusui.jcunit8.pipeline.stages;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.utils.Checks;
import com.github.dakusui.jcunit8.core.Utils;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.generators.Cartesian;

import java.util.List;

/**
 */
public interface Generator {
  List<Tuple> generate();

  abstract class Base implements Generator {
    protected final List<Tuple> seeds;
    protected final FactorSpace factorSpace;
    protected final Requirement requirement;

    protected Base(List<Tuple> seeds, FactorSpace factorSpace, Requirement requirement) {
      this.seeds = seeds;
      this.factorSpace = factorSpace;
      this.requirement = requirement;
    }

    public final List<Tuple> generate() {
      this.validate();
      return Utils.unique(generateCore());
    }

    @SuppressWarnings("WeakerAccess")
    protected void validate() {
      Checks.checkcond(this.factorSpace.getFactors().size() >= requirement.strength());
    }

    protected abstract List<Tuple> generateCore();
  }

  interface Factory {
    Generator create(List<Tuple> seeds, FactorSpace factorSpace, Requirement requirement);

    class Standard implements Factory {
      @Override
      public Generator create(List<Tuple> seeds, FactorSpace factorSpace, Requirement requirement) {
        ////
        // TODO
        //return new IpoGwithConstraints(seeds, requirement, factorSpace);
        return new Cartesian(seeds, factorSpace, requirement);
      }
    }
  }
}
