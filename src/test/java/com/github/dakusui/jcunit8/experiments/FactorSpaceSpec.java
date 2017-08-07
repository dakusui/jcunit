package com.github.dakusui.jcunit8.experiments;

import com.github.dakusui.jcunit8.factorspace.Factor;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;

import java.util.Collections;
import java.util.LinkedList;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FactorSpaceSpec {
  final SortedMap<Integer, Integer> factorSpecs = new TreeMap<>();
  private final String prefix;

  FactorSpaceSpec(String prefix) {
    this.prefix = prefix;
  }

  public FactorSpaceSpec addFactor(int numLevels, int numFactors) {
    this.factorSpecs.put(numLevels, numFactors);
    return this;
  }

  FactorSpace build() {
    AtomicInteger index = new AtomicInteger(0);
    return FactorSpace.create(
        new LinkedList<Factor>() {{
          factorSpecs.keySet().stream().flatMap(
              (Integer numLevels) -> IntStream.range(
                  0, factorSpecs.get(numLevels)
              ).mapToObj(
                  i -> Factor.create(
                      String.format("%s-%02d", prefix, index.getAndIncrement()),
                      IntStream.range(
                          0, numLevels
                      ).boxed(
                      ).collect(
                          Collectors.toList()
                      ).toArray()
                  )
              )
          ).forEach(
              this::add
          );
        }},
        Collections.emptyList()
    );
  }
}
