package com.github.dakusui.jcunit8.testutils.testsuitequality;

import com.github.dakusui.jcunit8.experiments.join.acts.ActsConstraint;
import com.github.dakusui.jcunit8.factorspace.Factor;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class FactorSpaceSpec {
  private final SortedMap<Integer, Integer>                  factorSpecs = new TreeMap<>((o1, o2) -> o2 - o1);
  private final List<Function<List<String>, ActsConstraint>> constraints = new LinkedList<>();
  private final String                                       prefix;

  public FactorSpaceSpec(String prefix) {
    this.prefix = prefix;
  }

  public FactorSpaceSpec addFactors(int numLevels, int numFactors) {
    this.factorSpecs.put(numLevels, numFactors);
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
                  format("%s-%02d", prefix, index.getAndIncrement()),
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

  @Override
  public String toString() {
    return this.factorSpecs.keySet().stream()
        .map(k -> format("%s^%s", k, this.factorSpecs.get(k)))
        .collect(joining(" ", format("%s[", this.prefix), "]"));
  }

  public String signature() {
    return this.factorSpecs.keySet().stream()
        .map(k -> format("%s^%s", k, this.factorSpecs.get(k)))
        .collect(joining(","));
  }

  public static void main(String... args) {
    System.out.println(new FactorSpaceSpec("F").addFactors(2, 3).addFactors(4, 2).toString());
  }

  public int numFactors() {
    return this.factorSpecs.values().stream().reduce(Integer::sum).orElse(0);
  }

  public String prefix() {
    return prefix;
  }

  public Stream<Map.Entry<Integer, Integer>> factorSpecs() {
    return this.factorSpecs.entrySet().stream();
  }
}
