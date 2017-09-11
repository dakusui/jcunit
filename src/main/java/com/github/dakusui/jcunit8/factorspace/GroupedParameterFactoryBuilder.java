package com.github.dakusui.jcunit8.factorspace;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.Generator;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.github.dakusui.jcunit.core.utils.Checks.checknotnull;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

public class GroupedParameterFactoryBuilder<T> {
  private final Function<Tuple, T> translator;
  private       int                strength;
  private List<Constraint>  constraints      = new LinkedList<>();
  private List<Factor>      factors          = new LinkedList<>();
  private Generator.Factory generatorFactory = new Generator.Factory.Standard();
  private List<Tuple>       seeds            = Collections.emptyList();

  public GroupedParameterFactoryBuilder(Function<Tuple, T> translator) {
    this.translator = checknotnull(translator);
  }

  public GroupedParameterFactoryBuilder<T> factor(String name, Object first, Object... rest) {
    factors.add(
        Factor.create(checknotnull(name),
            new LinkedList<Object>() {{
              add(first);
              addAll(asList(rest));
            }}.toArray())
    );
    return this;
  }

  public GroupedParameterFactoryBuilder<T> constraint(String name, Predicate<Tuple> predicate, String firstInvolvedKey, String... restInvolvedKeys) {
    constraints.add(Constraint.create(name, predicate, new LinkedList<String>() {{
      add(firstInvolvedKey);
      addAll(asList(restInvolvedKeys));
    }}));
    return this;
  }

  public GroupedParameterFactoryBuilder<T> strength(int strength) {
    this.strength = strength;
    return this;
  }

  public Parameter.Simple.Factory<T> build() {
    return Parameter.Simple.Factory.of(
        generatorFactory.create(
            FactorSpace.create(this.factors, this.constraints),
            new Requirement.Builder().withStrength(strength).withNegativeTestGeneration(false).build(),
            this.seeds)
            .generate()
            .stream()
            .map(translator)
            .collect(toList())
    );
  }
}
