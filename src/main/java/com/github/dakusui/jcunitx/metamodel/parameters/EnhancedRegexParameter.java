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
import com.github.dakusui.jcunitx.runners.helpers.ParameterUtils;
import com.github.dakusui.jcunitx.utils.Utils;
import com.github.dakusui.pcond.functions.Printables;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static com.github.dakusui.jcunitx.utils.AssertionUtils.isKeyOf;
import static com.github.dakusui.pcond.Preconditions.require;
import static com.github.dakusui.pcond.Preconditions.requireArgument;
import static com.github.dakusui.pcond.functions.Predicates.*;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * `ParameterizedRegex` is a meta-model designed to model a sequence of method calls.
 */
public interface EnhancedRegexParameter extends Parameter<List<EnhancedRegexParameter.MethodCallDescriptor>> {
  @SafeVarargs
  static <T> Parameter<Function<Context, T>> parameter(String parameterName, Function<Context, T>... args) {
    return ParameterUtils.simple(args).create(parameterName);
  }

  static <T extends Enum<T>> Function<Context, T>[] immediateValuesFromEnum(Class<T> enumClass) {
    return immediateValues(enumClass.getEnumConstants());
  }

  @SuppressWarnings("unchecked")
  @SafeVarargs
  static <T> Function<Context, T>[] immediateValues(T... values) {
    return Arrays.stream(values)
        .map(EnhancedRegexParameter::immediateValue)
        .toArray(Function[]::new);
  }

  static <T> Function<Context, T> immediateValue(T value) {
    return Printables.function(() -> String.format("immediateValue:'%s'", value), c -> value);
  }

  static <T> Function<Context, T> valueFrom(String methodName) {
    return valueFrom(methodName, -1);
  }

  static <T> Function<Context, T> valueFrom(String methodName, int index) {
    return Printables.function(
        () -> String.format("valueFrom:%s[%s]", methodName, index),
        c -> c.<T>resultOf(methodName, index).orElseThrow(RuntimeException::new));
  }

  static <T> Function<Context, T> argumentValueFrom(String methodName, int index, int argumentIndex) {
    return c -> null;
  }

  interface Context {
    <T> Optional<T> resultOf(String methodName, int index);
  }

  /**
   * A class to model a single method call.
   */
  interface MethodCallDescriptor {
    String methodName();

    List<Object> arguments();
  }

  class Impl extends Base<List<MethodCallDescriptor>> implements EnhancedRegexParameter {

    private final FactorSpace   factorSpace;
    private final RegexComposer regexComposer;

    public Impl(String name, EnhancedRegexParameter.Descriptor descriptor) {
      super(name, descriptor);
      Expr expr = new Parser().parse(descriptor.regex());
      RegexDecomposer decomposer = new RegexDecomposer(name, expr);
      this.regexComposer = new RegexComposer(name, expr);
      AtomicInteger i = new AtomicInteger(0);
      this.factorSpace = decomposer.decompose().extend(
          descriptor.methodName()
              .stream()
              .map(descriptor::parametersFor)
              .flatMap(
                  each -> each.stream()
                      .map(Parameter::toFactorSpace)
                      .map(p -> p.getFactors().get(0)))
              .collect(toList()),
          descriptor.constraints());
    }

    public Optional<AArray> decomposeValue(List<MethodCallDescriptor> value) {
      return RegexParameter._decomposeValue(
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
  class Descriptor extends Parameter.Descriptor.Base<List<MethodCallDescriptor>> {
    private final String                          regex;
    private final Map<String, List<Parameter<?>>> parameterDefinitions = new HashMap<>();
    private final List<Constraint>                constraints          = new LinkedList<>();
    private       int                             asteriskMax;
    private       int                             plusMax;

    private Descriptor(String regex) {
      this.regex = requireNonNull(regex);
      this.asterisk(2).plus(2);
    }

    public static Descriptor of(String regex) {
      return new Descriptor(regex);
    }

    @Override
    public EnhancedRegexParameter create(String name) {
      return create(name, this);
    }

    /**
     * A method to specify parameters for a method call specified by `element`.
     * `element` can be in the following format.
     *
     * - `elementName`: All the occurrences of the element whose name is `elementName`.
     * - `elementName[0]`, `elementName[1]`, ..., `elementName[i]` ,... `elementName[n-1]`: The *i*th occurrence of the element whose name is `elementName`.
     *
     * @param methodName A string to specify element. By default, used as a name of method.
     * @param parameters Parameters passed to a method specified by `element`.
     * @return This object
     */
    public Descriptor call(String methodName, Parameter<?>... parameters) {
      this.parameterDefinitions.put(methodName, unmodifiableList(asList(parameters)));
      return this;
    }

    public Descriptor constraints(Constraint... constraints) {
      this.constraints.addAll(asList(constraints));
      return this;
    }

    public Descriptor plus(int max) {
      this.plusMax = require(max, greaterThanOrEqualTo(1));
      return this;
    }

    public Descriptor asterisk(int max) {
      this.asteriskMax = require(max, greaterThanOrEqualTo(0));
      return this;
    }

    public String regex() {
      return this.regex;
    }

    public int plusMax() {
      return this.plusMax;
    }

    public int asteriskMax() {
      return this.asteriskMax;
    }

    public List<Parameter<?>> parametersFor(String methodName) {
      requireArgument(methodName, allOf(isNotNull(), isKeyOf(this.parameterDefinitions)));
      return this.parameterDefinitions.get(methodName);
    }

    public List<String> methodName() {
      return this.parameterDefinitions.keySet().stream().sorted().collect(toList());
    }

    public List<Constraint> constraints() {
      return unmodifiableList(this.constraints);
    }

    private static EnhancedRegexParameter create(String name, Descriptor descriptor) {
      return new Impl(name, descriptor);
    }
  }
}
