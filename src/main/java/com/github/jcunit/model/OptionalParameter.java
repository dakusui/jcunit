package com.github.jcunit.model;

import com.github.jcunit.core.tuples.Tuple;
import com.github.jcunit.factorspace.Constraint;
import com.github.jcunit.factorspace.Factor;
import com.github.jcunit.factorspace.FactorSpace;
import com.github.jcunit.factorspace.Parameter;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * // @formatter:off 
 * // @formatter:on 
 */
public class OptionalParameter<T> extends Parameter.Base<T> implements Parameter<T> {
  private final Parameter<T> parameter;

  /**
   *
   */
  public OptionalParameter(Parameter<T> parameter) {
    super(parameter.getName(), parameter.getKnownValues());
    this.parameter = parameter;
  }

  @Override
  protected List<Factor> decompose() {
    return Stream.concat(parameter.toFactorSpace()
                                  .getFactors()
                                  .stream()
                                  .map(this::ensureVoidValue),
                         Stream.of(Factor.create(presentKey(parameter), new Object[]{true, false})))
                 .collect(toList());
  }

  private Factor ensureVoidValue(Factor f) {
    if (f.getLevels().contains(Factor.VOID))
      return f;
    return Factor.create(f.getName(), Stream.concat(f.getLevels().stream(), Stream.of(Factor.VOID)).toArray());
  }

  @Override
  protected List<Constraint> generateConstraints() {
    FactorSpace factorSpace = parameter.toFactorSpace();
    return Stream.concat(factorSpace.getConstraints().stream(),
                         Stream.of(createConstraint(parameter, factorSpace)))
                 .collect(toList());
  }

  @Override
  public T composeValue(Tuple tuple) {
    return parameter.composeValue(tuple);
  }

  @Override
  public Optional<Tuple> decomposeValue(T value) {
    return parameter.decomposeValue(value);
  }

  private static <T> String presentKey(Parameter<T> p) {
    return p.getName() + "#present";
  }

  private static <T> Constraint createConstraint(final Parameter<T> parameter, FactorSpace factorSpace) {
    return new Constraint() {
      private final List<String> involvedKeys = Stream.concat(Stream.of(presentKey(parameter)),
                                                              factorSpace.getFactorNames().stream())
                                                      .collect(toList());

      @Override
      public boolean isExplicit() {
        return false;
      }

      @Override
      public String getName() {
        return String.format("%s=VOID->ANY in %s=VOID", presentKey(parameter), parameter.getName());
      }

      @Override
      public boolean test(Tuple tuple) {
        if (!Factor.VOID.equals(tuple.get(presentKey(parameter))))
          return true;
        for (Factor factor : factorSpace.getFactors()) {
          if (!Factor.VOID.equals(tuple.get(factor.getName())))
            return false;
        }
        return true;
      }

      @Override
      public List<String> involvedKeys() {
        return involvedKeys;
      }
    };
  }
}
