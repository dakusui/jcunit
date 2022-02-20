package com.github.dakusui.jcunit8.metamodel.parameters;

import com.github.dakusui.jcunit.core.tuples.Aarray;
import com.github.dakusui.jcunit.regex.Expr;
import com.github.dakusui.jcunit.regex.Parser;
import com.github.dakusui.jcunit.regex.RegexComposer;
import com.github.dakusui.jcunit8.core.Utils;
import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.factorspace.Factor;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.jcunit8.factorspace.regex.RegexDecomposer;
import com.github.dakusui.jcunit8.metamodel.Parameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public interface Regex extends Parameter<List<String>> {
  static <V> Optional<Aarray> _decomposeValue(V value, Stream<Aarray> tuples, Function<Aarray, V> valueComposer, Predicate<Aarray> constraints) {
    return tuples.filter((Aarray tuple) -> value.equals(valueComposer.apply(tuple)))
        .filter(constraints)
        .findFirst();
  }

  class Impl extends Base<List<String>> implements Regex {

    private final FactorSpace   factorSpace;
    private final RegexComposer regexComposer;

    public Impl(String name, String regex, List<List<String>> knownValues) {
      super(name, knownValues);
      Expr expr = new Parser().parse(regex);
      RegexDecomposer decomposer = new RegexDecomposer(name, expr);
      this.regexComposer = new RegexComposer(name, expr);
      this.factorSpace = decomposer.decompose();
    }

    public Optional<Aarray> decomposeValue(List<String> value) {
      return _decomposeValue(
          value,
          this.factorSpace.stream(),
          this.regexComposer::compose,
          Utils.conjunct(this.factorSpace.getConstraints())
      );
    }

    @Override
    public List<String> composeValue(Aarray tuple) {
      return new ArrayList<>(composeStringValueFrom(tuple));
    }

    @Override
    protected List<Factor> decompose() {
      return factorSpace.getFactors();
    }

    @Override
    protected List<Constraint> generateConstraints() {
      return factorSpace.getConstraints();
    }

    private List<String> composeStringValueFrom(Aarray tuple) {
      return regexComposer.compose(tuple);
    }
  }

  class Factory extends Parameter.Factory.Base<List<String>> {
    private final String regex;

    private Factory(String regex) {
      this.regex = requireNonNull(regex);
    }

    public static Factory of(String regex) {
      return new Factory(regex);
    }

    private static Regex create(String name, String regex, List<List<String>> knownValues) {
      return new Impl(name, regex, knownValues);
    }

    @Override
    public Regex create(String name) {
      return create(name, regex, knownValues);
    }
  }
}
