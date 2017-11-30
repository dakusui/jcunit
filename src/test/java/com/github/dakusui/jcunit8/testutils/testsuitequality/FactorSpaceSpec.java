package com.github.dakusui.jcunit8.testutils.testsuitequality;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.factorspace.Factor;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Arrays.asList;

public class FactorSpaceSpec {
  private final SortedMap<Integer, Integer> factorSpecs = new TreeMap<>();
  private final String prefix;
  private List<Constraint> constraints = new LinkedList<>();

  public FactorSpaceSpec(String prefix) {
    this.prefix = prefix;
  }

  public FactorSpaceSpec addFactors(int numLevels, int numFactors) {
    this.factorSpecs.put(numLevels, numFactors);
    return this;
  }

  public FactorSpaceSpec addConstraint(int pi, int pj) {
    this.constraints.add(new Constraint() {
      @Override
      public String getName() {
        return String.format("%s<=%s", key(pi), key(pj));
      }

      @Override
      public boolean test(Tuple tuple) {
        return (int) tuple.get(key(pi)) <= (int) tuple.get(key(pj));
      }

      @Override
      public List<String> involvedKeys() {
        return asList(key(pi), key(pj));
      }
    });
    return this;
  }

  public FactorSpace build() {
    AtomicInteger index = new AtomicInteger(0);
    return FactorSpace.create(
        new LinkedList<Factor>() {{
          factorSpecs.keySet().stream().flatMap(
              (Integer numLevels) -> IntStream.range(
                  0, factorSpecs.get(numLevels)
              ).mapToObj(
                  i -> Factor.create(
                      key(index.getAndIncrement()),
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
        this.constraints
    );
  }

  private String key(int index) {
    return String.format("%s-%02d", prefix, index);
  }

  @Override
  public String toString() {
    return this.factorSpecs.keySet().stream(
    ).map(
        k -> String.format("%s^%s", k, this.factorSpecs.get(k))
    ).collect(
        Collectors.joining(" ", String.format("%s[", this.prefix), "]")
    );
  }
}
