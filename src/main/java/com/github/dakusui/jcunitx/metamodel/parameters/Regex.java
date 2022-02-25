package com.github.dakusui.jcunitx.metamodel.parameters;

import com.github.dakusui.jcunitx.core.AArray;
import com.github.dakusui.jcunitx.regex.Expr;
import com.github.dakusui.jcunitx.regex.Parser;
import com.github.dakusui.jcunitx.regex.RegexComposer;
import com.github.dakusui.jcunitx.utils.Utils;
import com.github.dakusui.jcunitx.factorspace.Constraint;
import com.github.dakusui.jcunitx.factorspace.Factor;
import com.github.dakusui.jcunitx.factorspace.FactorSpace;
import com.github.dakusui.jcunitx.metamodel.parameters.regex.RegexDecomposer;
import com.github.dakusui.jcunitx.metamodel.Parameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public interface Regex extends Parameter<List<String>> {
  static <V> Optional<AArray> _decomposeValue(V value, Stream<AArray> tuples, Function<AArray, V> valueComposer, Predicate<AArray> constraints) {
    return tuples.filter((AArray tuple) -> value.equals(valueComposer.apply(tuple)))
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

    public Optional<AArray> decomposeValue(List<String> value) {
      return _decomposeValue(
          value,
          this.factorSpace.stream(),
          this.regexComposer::compose,
          Utils.conjunct(this.factorSpace.getConstraints())
      );
    }

    @Override
    public List<String> composeValue(AArray tuple) {
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

    private List<String> composeStringValueFrom(AArray tuple) {
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
