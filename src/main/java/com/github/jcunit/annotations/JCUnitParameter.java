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
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.github.jcunit.core.model.ParameterSpec.Utils.createConstraints;
import static com.github.jcunit.core.model.ParameterSpec.Utils.isSeed;
import static com.github.jcunit.runners.junit5.JCUnitTestExtensionUtils.nameOf;
import static java.util.stream.Collectors.toMap;

@Retention(RetentionPolicy.RUNTIME)
public @interface JCUnitParameter {
  String value() default "";

  enum Type {
    ;

    public static <T> Parameter<ValueResolver<T>> createSimple(ParameterSpec<T> parameterSpec, ParameterSpaceSpec parameterSpaceSpec) {
      boolean isSeed = isSeed(parameterSpaceSpec, parameterSpec.name(), parameterSpaceSpec.parameterNames());
      return new Parameter.Simple.Impl<>(!isSeed,
                                         parameterSpec.name(),
                                         parameterSpec.valueResolvers(),
                                         createConstraints(isSeed,
                                                           parameterSpaceSpec,
                                                           parameterSpec.name()));
    }


    public static <T> Parameter<List<ValueResolver<T>>> createRegex(String regex,
                                                                    boolean isSeed,
                                                                    Class<T> valueResolverType,
                                                                    Class<?> parameterSpaceClass, String parameterName) {
      // List<String> tokens = tokensInRegex(regex); // This is only necessary for validation
      return createRegex(regex,
                         isSeed,
                         parameterName,
                         valueResolvers(valueResolverType, parameterSpaceClass)::get);
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
                   .collect(Collectors.toList());
    }

    public static <T> Parameter<List<ValueResolver<T>>> createRegex(String regex, boolean isSeed1, String name, Function<String, ValueResolver<T>> stringValueResolverFunction1) {
      boolean isSeed = isSeed1;
      return new Parameter.Regex.Impl<>(name,
                                        regex,
                                        Collections.emptyList(),
                                        stringValueResolverFunction1

      );
    }
  }
}
