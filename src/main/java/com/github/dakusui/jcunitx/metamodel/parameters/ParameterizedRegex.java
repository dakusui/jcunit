package com.github.dakusui.jcunitx.metamodel.parameters;

import com.github.dakusui.jcunitx.core.AArray;
import com.github.dakusui.jcunitx.factorspace.Constraint;
import com.github.dakusui.jcunitx.factorspace.Factor;
import com.github.dakusui.jcunitx.factorspace.FactorSpace;
import com.github.dakusui.jcunitx.metamodel.Parameter;
import com.github.dakusui.jcunitx.metamodel.parameters.regex.RegexComposer;
import com.github.dakusui.jcunitx.metamodel.parameters.regex.RegexDecomposer;
import com.github.dakusui.jcunitx.regex.Expr;
import com.github.dakusui.jcunitx.regex.Parser;
import com.github.dakusui.jcunitx.utils.Utils;
import com.github.dakusui.pcond.functions.Functions;
import com.github.dakusui.pcond.functions.Predicates;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.github.dakusui.pcond.Preconditions.require;
import static com.github.dakusui.pcond.functions.Functions.size;
import static com.github.dakusui.pcond.functions.Predicates.isEqualTo;
import static com.github.dakusui.pcond.functions.Predicates.transform;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * `ParameterizedRegex` is a meta-model designed to model a sequence of method calls.
 */
public interface ParameterizedRegex extends Parameter<List<ParameterizedRegex.MethodCallDescriptor>> {
  /**
   * A class to model a single method call.
   */
  interface MethodCallDescriptor {
    String methodName();

    List<Object> arguments();
  }

  class Impl extends Base<List<MethodCallDescriptor>> implements ParameterizedRegex {

    private final FactorSpace   factorSpace;
    private final RegexComposer regexComposer;

    public Impl(String name, String regex, List<List<MethodCallDescriptor>> knownValues, Map<Parameter.Factory<?>, List<String>> arguments) {
      super(name, knownValues);
      Expr expr = new Parser().parse(regex);
      RegexDecomposer decomposer = new RegexDecomposer(name, expr);
      this.regexComposer = new RegexComposer(name, expr);
      AtomicInteger i = new AtomicInteger(0);
      this.factorSpace = decomposer.decompose().extend(
          arguments.keySet()
              .stream()
              .map(each -> each.create(format("p%02d", i.getAndIncrement())))
              .map(Parameter::toFactorSpace)
              .peek(each -> require(each.getFactors(), transform(size()).check(isEqualTo(1))))
              .map(each -> each.getFactors().get(0))
              .collect(toList()),
          emptyList()
      );
    }

    public Optional<AArray> decomposeValue(List<MethodCallDescriptor> value) {
      return Regex._decomposeValue(
          value,
          this.factorSpace.streamAllPossibleRows(),
          this.regexComposer::compose,
          Utils.conjunct(this.factorSpace.getConstraints())
      );
    }

    @Override
    public List<MethodCallDescriptor> composeValue(AArray tuple) {
      return new ArrayList<>(composeMethodCallDescriptorSequenceFrom(tuple));
    }

    @Override
    protected List<Factor> decompose() {
      return factorSpace.getFactors();
    }

    @Override
    protected List<Constraint> generateConstraints() {
      return factorSpace.getConstraints();
    }

    private List<MethodCallDescriptor> composeMethodCallDescriptorSequenceFrom(AArray aarray) {
      // TODO regexComposer.compose(tuple);
      return emptyList();
    }
  }

  /**
   * A factory class for `ParameterizedRegex` parameter.
   */
  class Factory extends Parameter.Factory.Base<List<MethodCallDescriptor>> {
    private final String                                  regex;
    /**
     * Stores parameters for arguments as keys.
     * Method names that are referencing a method is stored as a list in the value side.
     */
    private final Map<Parameter.Factory<?>, List<String>> arguments = new HashMap<>();

    private Factory(String regex) {
      this.regex = requireNonNull(regex);
    }

    public static Factory of(String regex) {
      return new Factory(regex);
    }

    private static ParameterizedRegex create(String name, String regex, List<List<MethodCallDescriptor>> knownValues, Map<Parameter.Factory<?>, List<String>> arguments) {
      return new Impl(name, regex, knownValues, arguments);
    }

    /**
     * A method to specify parameter factories for a method call specified by `element`.
     * `element` can be in the following format.
     *
     * - `elementName`: All the occurrences of the element whose name is `elementName`.
     * - `elementName[0]`, `elementName[1]`, ..., `elementName[i]` ,... `elementName[n-1]`: The *i*th occurrence of the element whose name is `elementName`.
     *
     * @param methodName A string to specify element. By default, used as a name of method.
     * @param parameters Parameters passed to a method specified by `element`.
     * @return This object
     */
    public Factory parameters(String methodName, Parameter.Factory<?>... parameters) {
      for (Parameter.Factory<?> each : parameters) {
        this.arguments.putIfAbsent(each, new LinkedList<>());
        this.arguments.get(each).add(methodName);
      }
      return this;
    }

    public Factory constraints(Constraint... constraints) {
      return this;
    }

    @Override
    public ParameterizedRegex create(String name) {
      return create(name, this.regex, this.knownValues, this.arguments);
    }
  }
}
