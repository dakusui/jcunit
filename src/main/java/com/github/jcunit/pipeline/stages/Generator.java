package com.github.jcunit.pipeline.stages;

import com.github.jcunit.core.tuples.Tuple;
import com.github.jcunit.utils.InternalUtils;
import com.github.jcunit.factorspace.FactorSpace;
import com.github.jcunit.pipeline.PipelineConfig;
import com.github.jcunit.pipeline.stages.generators.Cartesian;
import com.github.jcunit.pipeline.stages.generators.IpoGplus;

import java.util.Collections;
import java.util.List;

import static com.github.valid8j.classic.Requires.requireNonNull;

/**
 */
public interface Generator {
  Object DontCare = new Object() {
    @Override
    public final String toString() {
      return "D/C";
    }
  };
  
  List<Tuple> generate();

  abstract class Base implements Generator {
    protected final FactorSpace factorSpace;
    protected final PipelineConfig pipelineConfig;

    protected Base(FactorSpace factorSpace, PipelineConfig pipelineConfig) {
      this.factorSpace = factorSpace;
      this.pipelineConfig = pipelineConfig;
    }

    public final List<Tuple> generate() {
      this.validate();
      if (this.factorSpace.getFactors().stream().anyMatch(each -> each.getLevels().isEmpty()))
        return Collections.emptyList();
      return InternalUtils.unique(generateCore());
    }

    protected void validate() {
    }

    protected abstract List<Tuple> generateCore();
  }

  interface Factory {
    Generator create(FactorSpace factorSpace, List<Tuple> encodedSeeds);

    class Standard implements Factory {
      private final PipelineConfig pipelineConfig;

      public Standard(PipelineConfig pipelineConfig) {
        this.pipelineConfig = requireNonNull(pipelineConfig);
      }

      @Override
      public Generator create(FactorSpace factorSpace, List<Tuple> encodedSeeds) {
        if (pipelineConfig.strength() < factorSpace.getFactors().size()) {
          return new IpoGplus(factorSpace, this.pipelineConfig, encodedSeeds);
        }
        return new Cartesian(factorSpace, this.pipelineConfig);
      }
    }
  }
}
