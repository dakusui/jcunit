package com.github.dakusui.peerj.testbases;

import com.github.dakusui.jcunit8.factorspace.Factor;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.peerj.model.ConstraintSet;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public abstract class PeerJIncremental extends PeerJBase {
  public PeerJIncremental(Spec spec) {
    super(spec);
  }

  /**
   * Creates and returns a factor space from a given factor space by reducing the
   * last ten factors.
   *
   * @param factorSpace A factor space from which a new and smaller factor space
   *                    is created.
   * @return A created factor space.
   */
  public static FactorSpace baseFactorSpaceFrom(FactorSpace factorSpace) {
    int baseDegree = factorSpace.getFactors().size() - 10;
    List<Factor> baseFactors = factorSpace.getFactors().subList(0, baseDegree);
    Set<String> baseFactorNames = baseFactors.stream().map(Factor::getName).collect(toSet());
    return FactorSpace.create(
        baseFactors,
        factorSpace.getConstraints()
            .stream()
            .filter(each -> each.involvedKeys().stream().anyMatch(baseFactorNames::contains))
            .peek(crossingConstraintFound(baseFactorNames))
            .collect(toList()));
  }
}
