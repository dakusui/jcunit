package com.github.jcunit.annotations;

import com.github.jcunit.core.model.ParameterSpaceSpec;
import com.github.jcunit.core.model.ParameterSpec;
import com.github.jcunit.core.model.ValueResolver;
import com.github.jcunit.factorspace.Parameter;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.*;
import java.util.function.Supplier;

import static com.github.jcunit.core.model.ParameterSpec.Utils.createConstraints;
import static com.github.jcunit.core.model.ParameterSpec.Utils.isSeed;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

@Retention(RetentionPolicy.RUNTIME)
public @interface JCUnitParameter {
  Type type() default Type.SIMPLE;

  String[] args() default {};

  enum Type {
    SIMPLE {
    },
    REGEX {
    };


    public static <T> Parameter<List<ValueResolver<T>>> createListSimple(ParameterSpec<T> parameterSpec, ParameterSpaceSpec parameterSpaceSpec) {
      boolean isSeed = isSeed(parameterSpaceSpec, parameterSpec.name(), parameterSpaceSpec.parameterNames());
      return new Parameter.Simple.Impl<>(!isSeed,
                                         parameterSpec.name(),
                                         parameterSpec.valueResolvers()
                                                      .stream()
                                                      .map(Collections::singletonList)
                                                      .collect(toList()),
                                         createConstraints(isSeed,
                                                           parameterSpaceSpec,
                                                           parameterSpec.name()));
    }

    public static <T> Parameter.Regex<ValueResolver<T>> createRegex(boolean isSeed,
                                                                    ParameterSpec<T> parameterSpec) {
      // List<String> tokens = tokensInRegex(regex); // This is only necessary for validation
      return new Parameter.Regex.Impl<>(!isSeed,
                                        parameterSpec.name(),
                                        parameterSpec.additionalArguments().toArray(new String[0]),
                                        emptyList(),
                                        name -> parameterSpec.valueResolvers()
                                                             .stream()
                                                             .filter(r -> r.name().isPresent() && Objects.equals(name, r.name().get()))
                                                             .findFirst()
                                                             .orElseThrow(noMatchingResolver(parameterSpec, name)));
    }

    private static <T> Supplier<NoSuchElementException> noMatchingResolver(ParameterSpec<T> parameterSpec, String name) {
      return () -> new NoSuchElementException("No matching value resolver is available for parameter: <" + name +
                                              ">, known value resolvers are defined for: " + parameterSpec.valueResolvers()
                                                                                                          .stream()
                                                                                                          .filter(r -> r.name().isPresent())
                                                                                                          .map(r -> r.name().orElseThrow(AssertionError::new))
                                                                                                          .collect(toList()));
    }

  }
}
