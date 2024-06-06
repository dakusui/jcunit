package com.github.jcunit.annotations;

import com.github.jcunit.core.model.ParameterSpaceSpec;
import com.github.jcunit.core.model.ParameterSpec;
import com.github.jcunit.core.model.ValueResolver;
import com.github.jcunit.factorspace.Parameter;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.github.jcunit.core.model.ParameterSpec.Utils.createConstraints;
import static com.github.jcunit.core.model.ParameterSpec.Utils.isSeed;

@Retention(RetentionPolicy.RUNTIME)
public @interface JCUnitParameter {
  String value() default "";

  enum Type {
    SIMPLE {
      @Override
      public <T> Parameter<ValueResolver<T>> create(ParameterSpec<T> parameterSpec, ParameterSpaceSpec parameterSpaceSpec) {
        boolean isSeed = isSeed(parameterSpaceSpec, parameterSpec.name(), parameterSpaceSpec.parameterNames());
        return new Parameter.Simple.Impl<>(!isSeed,
                                           parameterSpec.name(),
                                           parameterSpec.valueResolvers(),
                                           createConstraints(isSeed,
                                                             parameterSpaceSpec,
                                                             parameterSpec.name()));
      }
    },
    REGEX {
      @Override
      public <T> Parameter<ValueResolver<T>> create(ParameterSpec<T> parameterSpec, ParameterSpaceSpec parameterSpaceSpec) {
        return null;
      }
    },
    ;

    public abstract <T> Parameter<ValueResolver<T>> create(ParameterSpec<T> parameterSpec, ParameterSpaceSpec parameterSpaceSpec);
  }
}
