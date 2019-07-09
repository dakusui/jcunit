package com.github.dakusui.jcunit8.extras.abstracter;

import com.github.dakusui.jcunit8.extras.generators.ActsConstraint;
import com.github.dakusui.jcunit8.factorspace.Factor;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class FactorSpaceSpec {
  private final SortedMap<Integer, Integer>                  factorSpecs = new TreeMap<>((o1, o2) -> o2 - o1);
  private final List<Function<List<String>, ActsConstraint>> constraints = new LinkedList<>();

  public FactorSpaceSpec addFactors(int numLevels, int numFactors) {
    FactorSpaceSpec ret = this;
    for (int i = 0; i < numFactors; i++)
      ret = this.addFactor(numLevels);
    return ret;
  }

  public FactorSpaceSpec addFactor(int numLevels) {
    this.factorSpecs.putIfAbsent(numLevels, 0);
    this.factorSpecs.put(numLevels, factorSpecs.get(numLevels) + 1);
    return this;
  }

  public FactorSpaceSpec addConstraint(Function<List<String>, ActsConstraint> constraint) {
    this.constraints.add(constraint);
    return this;
  }

  public FactorSpace build() {
    AtomicInteger index = new AtomicInteger(0);
    LinkedList<Factor> factors = new LinkedList<Factor>() {{
      factorSpecs.keySet().stream()
          .flatMap((Integer numLevels) -> IntStream.range(0, factorSpecs.get(numLevels))
              .mapToObj(i -> Factor.create(
                  composeFactorName(index::getAndIncrement),
                  IntStream.range(0, numLevels)
                      .boxed().collect(toList())
                      .toArray())))
          .forEach(this::add);
    }};
    return FactorSpace.create(
        factors,
        constraints.stream()
            .map(each -> each.apply(factors.stream().map(Factor::getName).collect(toList())))
            .collect(toList())
    );
  }

  public String prefix() {
    return "p";
  }

  public String signature() {
    return this.factorSpecs.keySet().stream()
        .map(k -> format("%s^%s", k, this.factorSpecs.get(k)))
        .collect(joining(","));
  }

  public int numFactors() {
    return this.factorSpecs.values().stream().reduce(Integer::sum).orElse(0);
  }

  public Stream<Map.Entry<Integer, Integer>> factorSpecs() {
    return this.factorSpecs.entrySet().stream();
  }

  @Override
  public String toString() {
    return this.factorSpecs.keySet().stream()
        .map(k -> format("%s^%s", k, this.factorSpecs.get(k)))
        .collect(joining(" ", format("%s[", this.prefix()), "]"));
  }

  private String composeFactorName(IntSupplier factorId) {
    return format("%s-%02d", prefix(), factorId.getAsInt());
  }
}
