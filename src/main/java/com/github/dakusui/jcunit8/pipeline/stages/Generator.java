package com.github.dakusui.jcunit8.pipeline.stages;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.core.Utils;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.generators.Cartesian;
import com.github.dakusui.jcunit8.pipeline.stages.generators.IpoG;

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
      System.out.println(this.getClass().getCanonicalName());
      System.out.println("factors");
      factorSpace.getFactors().forEach(factor -> System.out.println("  " + factor));
      System.out.println("constraints");
      factorSpace.getConstraints().forEach(constraint -> System.out.println("  " + constraint));
      System.out.println("--");
    }

    public final List<Tuple> generate() {
      this.validate();
      return Utils.unique(generateCore());
    }

    protected void validate() {
    }

    protected abstract List<Tuple> generateCore();
  }

  interface Factory {
    Generator create(List<Tuple> seeds, FactorSpace factorSpace, Requirement requirement);

    class Standard implements Factory {
      @Override
      public Generator create(List<Tuple> seeds, FactorSpace factorSpace, Requirement requirement) {
        if (requirement.strength() < factorSpace.getFactors().size()) {
          return new IpoG(seeds, factorSpace, requirement);
        }
        return new Cartesian(seeds, factorSpace, requirement);
      }
    }
  }
}
