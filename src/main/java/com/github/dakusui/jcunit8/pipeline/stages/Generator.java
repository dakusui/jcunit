package com.github.dakusui.jcunit8.pipeline.stages;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;

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
  }
  interface Factory {
    Generator create(List<Tuple> seeds, FactorSpace factorSpace, Requirement requirement);

    class Standard implements Factory {
      @Override
      public Generator create(List<Tuple> seeds, FactorSpace factorSpace, Requirement requirement) {
        return () -> {
          // TODO
          throw new UnsupportedOperationException();
        };
      }
    }
  }
}
