package com.github.dakusui.jcunit8.pipeline.stages;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.utils.Checks;
import com.github.dakusui.jcunit8.core.Utils;
import com.github.dakusui.jcunit8.factorspace.Factor;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.generators.Cartesian;
import com.github.dakusui.jcunit8.pipeline.stages.generators.IpoGplus;

import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 */
public interface Generator {
  List<Tuple> generate();

  abstract class Base implements Generator {
    protected final FactorSpace factorSpace;
    protected final Requirement requirement;

    protected Base(FactorSpace factorSpace, Requirement requirement) {
      this.factorSpace = factorSpace;
      this.requirement = requirement;
    }

    public final List<Tuple> generate() {
      this.validate();
      if (this.factorSpace.getFactors().stream().anyMatch(each -> each.getLevels().isEmpty()))
        return Collections.emptyList();
      return Utils.unique(generateCore());
    }

    protected void validate() {
    }

    protected abstract List<Tuple> generateCore();
  }

  interface Factory {
    Generator create(FactorSpace factorSpace, Requirement requirement);

    class Standard implements Factory {
      @Override
      public Generator create(FactorSpace factorSpace, Requirement requirement) {
        validate(requirement, factorSpace);
        if (requirement.strength() < factorSpace.getFactors().size()) {
          return new IpoGplus(factorSpace, requirement);
        }
        return new Cartesian(factorSpace, requirement);
      }

      private void validate(Requirement requirement, FactorSpace factorSpace) {
        List<Tuple> violations = requirement.seeds(
        ).stream(
        ).filter(
            (Tuple tuple) -> !isComplete(tuple, factorSpace)
        ).collect(
            toList()
        );
        Checks.checkcond(
            violations.isEmpty(),
            "Following seed(s) are incomplete. A seed must have all of %s. :%s",
            allFactorNames(factorSpace),
            violations
        );
      }

      private static boolean isComplete(Tuple tuple, FactorSpace factorSpace) {
        return tuple.keySet().containsAll(allFactorNames(factorSpace));
      }

      private static List<String> allFactorNames(FactorSpace factorSpace) {
        return factorSpace.getFactors().stream().map(Factor::getName).collect(toList());
      }
    }
  }
}
