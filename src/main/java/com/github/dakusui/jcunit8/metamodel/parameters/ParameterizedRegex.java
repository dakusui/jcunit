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

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;

/**
 * `ParameterizedRegex` is a meta-model designed to model a sequence of method calls.
 */
public interface ParameterizedRegex extends Parameter<List<ParameterizedRegex.Element>> {
  /**
   * A class to model a single method call.
   */
  interface Element {
    String name();

    List<Object> arguments();
  }

  class Impl extends Base<List<Element>> implements ParameterizedRegex {

    private final FactorSpace   factorSpace;
    private final RegexComposer regexComposer;

    public Impl(String name, String regex, List<List<Element>> knownValues) {
      super(name, knownValues);
      Expr expr = new Parser().parse(regex);
      RegexDecomposer decomposer = new RegexDecomposer(name, expr);
      this.regexComposer = new RegexComposer(name, expr);
      this.factorSpace = decomposer.decompose();
    }

    public Optional<Aarray> decomposeValue(List<Element> value) {
      return _decomposeValue(
          value,
          this.factorSpace.stream(),
          this.regexComposer::compose,
          Utils.conjunct(this.factorSpace.getConstraints())
      );
    }

    @Override
    public List<Element> composeValue(Aarray tuple) {
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

    private List<Element> composeStringValueFrom(Aarray tuple) {
      // TODO regexComposer.compose(tuple);
      return emptyList();
    }
  }

  class Factory extends Parameter.Factory.Base<List<Element>> {
    private final String regex;

    private Factory(String regex) {
      this.regex = requireNonNull(regex);
    }

    /**
     * A method to specify parameter factories for a method call specified by `element`.
     * `element` can be in the following format.
     * <p>
     * - `elementName`: All the occurrences of the element whose name is `elementName`.
     * - `elementName[0]`, `elementName[1]`, ..., `elementName[i]` ,... `elementName[n-1]`: The *i*th occurrence of the element whose name is `elementName`.
     *
     * @param element    A string to specify element. By default, used as a name of method.
     * @param parameters Parameters passed to a method specified by `element`.
     * @return This object
     */
    public Factory parameters(String element, Parameter.Factory<?>... parameters) {
      return this;
    }

    public Factory constraints(Constraint... constraints) {
      return this;
    }

    public static Factory of(String regex) {
      return new Factory(regex);
    }


    private static ParameterizedRegex create(String name, String regex, List<List<Element>> knownValues) {
      return new Impl(name, regex, knownValues);
    }

    @Override
    public ParameterizedRegex create(String name) {
      return create(name, regex, knownValues);
    }
  }
}
