package com.github.jcunit.annotations;

import com.github.jcunit.core.Invokable;
import com.github.jcunit.core.model.ParameterSpaceSpec;
import com.github.jcunit.core.model.ParameterSpec;
import com.github.jcunit.core.model.ValueResolver;
import com.github.jcunit.factorspace.Parameter;
import com.github.jcunit.regex.Expr;
import com.github.jcunit.regex.Parser;
import com.github.jcunit.runners.junit5.JCUnitTestExtensionUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.*;

import static com.github.jcunit.core.model.ParameterSpec.Utils.createConstraints;
import static com.github.jcunit.core.model.ParameterSpec.Utils.isSeed;
import static com.github.jcunit.runners.junit5.JCUnitTestExtensionUtils.nameOf;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

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
                                         parameterSpec.valueResolvers().stream().map(each -> singletonList(each)).collect(toList()),
                                         createConstraints(isSeed,
                                                           parameterSpaceSpec,
                                                           parameterSpec.name()));
    }

    public static <T> Parameter<List<ValueResolver<T>>> createRegex(String[] regexes,
                                                                    boolean isSeed,
                                                                    String parameterName,
                                                                    Class<T> valueType,
                                                                    Class<?> parameterSpaceClass) {
      // List<String> tokens = tokensInRegex(regex); // This is only necessary for validation
      return new Parameter.Regex.Impl<>(!isSeed,
                                        parameterName,
                                        regexes,
                                        emptyList(),
                                        valueResolvers(valueType, parameterSpaceClass)::get

      );
    }

    public static <T> Map<String, ValueResolver<T>> valueResolvers(Class<T> valueResolverType, Class<?> parameterSpaceClass) {
      return Arrays.stream(parameterSpaceClass.getMethods())
                   .filter(m -> m.isAnnotationPresent(JCUnitParameterValue.class))
                   .filter(m -> valueResolverType.isAssignableFrom(m.getReturnType()))
                   .collect(toMap(
                       JCUnitTestExtensionUtils::nameOf,
                       m -> ValueResolver.fromInvokable(Invokable.fromClassMethodNamed(parameterSpaceClass,
                                                                                       nameOf(m)))));
    }

    // Only used for validation
    private static List<String> tokensInRegex(String regex) {
      Set<String> tokens = new HashSet<>();
      Expr expr = new Parser().parse(regex);
      expr.accept(new Expr.Visitor() {
        @Override
        public void visit(Expr.Alt exp) {
          for (Expr each : exp.getChildren()) {
            each.accept(this);
          }
        }

        @Override
        public void visit(Expr.Cat exp) {
          for (Expr each : exp.getChildren()) {
            each.accept(this);
          }
        }

        @Override
        public void visit(Expr.Leaf exp) {
          tokens.add(exp.toString());
        }

        @Override
        public void visit(Expr.Empty exp) {
        }
      });
      return tokens.stream()
                   .sorted()
                   .collect(toList());
    }
  }
}
