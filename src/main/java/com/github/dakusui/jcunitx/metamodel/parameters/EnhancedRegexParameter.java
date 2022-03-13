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
import java.util.function.Function;

import static com.github.dakusui.jcunitx.utils.AssertionUtils.isKeyOf;
import static com.github.dakusui.pcond.Assertions.that;
import static com.github.dakusui.pcond.Preconditions.require;
import static com.github.dakusui.pcond.Preconditions.requireArgument;
import static com.github.dakusui.pcond.functions.Predicates.*;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * `ParameterizedRegex` is a meta-model designed to model a sequence of method calls.
 */
public interface EnhancedRegexParameter extends Parameter<List<EnhancedRegexParameter.MethodCallDescriptor>> {
  @SafeVarargs
  static <T> Parameter<Function<ExecutionContext, T>> parameter(String parameterName, Function<ExecutionContext, T>... args) {
    return ParameterUtils.simple(args).create(parameterName);
  }

  static <T extends Enum<T>> Function<ExecutionContext, T>[] immediateValuesFromEnum(Class<T> enumClass) {
    return immediateValues(enumClass.getEnumConstants());
  }

  @SuppressWarnings("unchecked")
  @SafeVarargs
  static <T> Function<ExecutionContext, T>[] immediateValues(T... values) {
    return Arrays.stream(values)
        .map(EnhancedRegexParameter::immediateValue)
        .toArray(Function[]::new);
  }

  static <T> Function<ExecutionContext, T> immediateValue(T value) {
    return Printables.function(() -> String.format("immediateValue:'%s'", value), c -> value);
  }

  static <T> Function<ExecutionContext, T> valueFrom(String methodName) {
    return valueFrom(methodName, -1);
  }

  static <T> Function<ExecutionContext, T> valueFrom(String methodName, int index) {
    return Printables.function(
        () -> String.format("valueFrom:%s[%s]", methodName, index),
        c -> c.<T>resultOf(methodName, index).orElseThrow(RuntimeException::new));
  }

  static <T> Function<ExecutionContext, T> argumentValueFrom(String methodName, int index, int argumentIndex) {
    return c -> null;
  }

  static MethodDescriptor.Builder method(String methodName) {
    return new MethodDescriptor.Builder(methodName);
  }

  /**
   * An interface to define how calls of a method should be.
   * That is, it specifies a name of a method and "parameters" of it.
   * Note that a parameter defines all the possible values for a variable passed to a method.
   */
  interface MethodDescriptor {
    String name();

    List<Parameter<?>> parameters();

    List<Constraint> constraints();

    class Builder {
      private final List<Parameter<?>> parameters  = new LinkedList<>();
      private final List<Constraint>   constraints = new LinkedList<>();
      private final String             methodName;

      public Builder(String methodName) {
        this.methodName = requireNonNull(methodName);
      }

      @SafeVarargs
      public final <T> Builder parameter(String parameterName, Function<ExecutionContext, T>... values) {
        return this.parameter(EnhancedRegexParameter.parameter(parameterName, values));
      }

      public <T> Builder parameter(Parameter<? extends Function<? extends ExecutionContext, T>> parameter) {
        parameters.add(requireNonNull(parameter));
        return this;
      }

      @SuppressWarnings({ "unchecked", "rawtypes" })
      @SafeVarargs
      public final Builder parameters(Parameter<Function<ExecutionContext, ?>>... parameters) {
        Builder ret = this;
        for (Parameter<Function<ExecutionContext, ?>> each : parameters) {
          ret = ret.parameter(((Parameter) each));
        }
        return ret;
      }

      public Builder constraint(Constraint constraint) {
        this.constraints.add(requireNonNull(constraint));
        return this;
      }

      public Builder constraints(Constraint... constraints) {
        Builder ret = this;
        for (Constraint each : constraints) {
          ret = ret.constraint(each);
        }
        return ret;
      }

      public MethodDescriptor build() {
        final String name = this.methodName;
        final List<Parameter<?>> parameters = unmodifiableList(this.parameters);
        return new MethodDescriptor() {
          @Override
          public String name() {
            return name;
          }

          @Override
          public List<Parameter<?>> parameters() {
            return parameters;
          }

          @Override
          public List<Constraint> constraints() {
            return constraints;
          }
        };
      }

      public MethodDescriptor $() {
        return build();
      }
    }
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
      FactorSpace decomposed = decomposer.decompose();
      this.factorSpace = decomposed.extend(
          descriptor.methodNames()
              .stream()
              .map(descriptor::parametersFor)
              .flatMap(
                  each -> each.stream()
                      .map(Parameter::toFactorSpace)
                      .map(p -> p.getFactors().get(0)))
              .collect(toList()),
          descriptor.methodNames()
              .stream().map(descriptor::constraintsFor)
              .flatMap(Collection::stream)
              .collect(toList()));
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

    /**
     * NAME:
     *     REGEX:regexExample:cat-8
     * LEVELS:
     *     (VOID)
     *     [[openForWrite], Reference:<REGEX:regexExample:rep-4>, [], [close], [openForWrite], Reference:<REGEX:regexExample:rep-4>, [], [close]]
     * NAME:
     *     REGEX:regexExample:rep-9
     * LEVELS:
     *     [(VOID), [Reference:<REGEX:regexExample:cat-7>], [Reference:<REGEX:regexExample:cat-8>]]
     * NAME:
     *     REGEX:regexExample:rep-14
     * LEVELS
     *     (VOID),
     *     [Reference:<REGEX:regexExample:empty-0>],
     *     [readLine],
     *     [Reference:<REGEX:regexExample:cat-13>]
     */
    private List<MethodCallDescriptor> composeMethodCallDescriptorSequenceFrom(AArray aarray) {
      // TODO regexComposer.compose(tuple);
      return emptyList();
    }

    private static List<Factor> expandParameterFor(Factor factor, String methodName, Parameter<?> parameter) {
      assert that(parameter, isInstanceOf(SimpleParameter.class));
      return null;
    }

    private static Parameter<?> renameParameterFor(Factor factor, String methodName, Parameter<?> parameter) {
      return parameter.descriptor().create(factor.getName() + ":" + methodName + "." + parameter.getName());
    }
  }

  /**
   * A factory class for `ParameterizedRegex` parameter.
   */
  class Descriptor extends Parameter.Descriptor.Base<List<MethodCallDescriptor>> {
    private final String                        regex;
    private final Map<String, MethodDescriptor> methodDescriptors = new HashMap<>();
    private       int                           asteriskMax;
    private       int                           plusMax;

    private Descriptor(String regex) {
      this.regex = requireNonNull(regex);
      this.asterisk(2).plus(2);
    }

    public static Descriptor of(String regex) {
      return new Descriptor(regex);
    }

    private static EnhancedRegexParameter create(String name, Descriptor descriptor) {
      return new Impl(name, descriptor);
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
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Descriptor describe(String methodName, List<Parameter<? extends Function<? extends ExecutionContext, ?>>> parameters) {
      MethodDescriptor.Builder b = new MethodDescriptor.Builder(methodName);
      for (Parameter<? extends Function<? extends ExecutionContext, ?>> each : parameters) {
        b.parameter((Parameter) each);
      }
      return this.describe(b.$());
    }

    public Descriptor describe(MethodDescriptor methodDescriptor) {
      this.methodDescriptors.put(methodDescriptor.name(), requireNonNull(methodDescriptor));
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

    public List<String> methodNames() {
      return this.methodDescriptors.keySet().stream().sorted().collect(toList());
    }

    public List<Parameter<?>> parametersFor(String methodName) {
      requireArgument(methodName, allOf(isNotNull(), isKeyOf(this.methodDescriptors)));
      return this.methodDescriptors.get(methodName).parameters();
    }


    public List<Constraint> constraintsFor(String methodName) {
      requireArgument(methodName, allOf(isNotNull(), isKeyOf(this.methodDescriptors)));
      return this.methodDescriptors.get(methodName).constraints();
    }
  }
}
