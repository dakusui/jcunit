package com.github.jcunit.annotations;

import com.github.jcunit.factorspace.ParameterSpace;
import com.github.jcunit.pipeline.ParseRequirementWith;
import com.github.jcunit.pipeline.Pipeline;
import com.github.jcunit.pipeline.PipelineConfig;
import com.github.jcunit.pipeline.Requirement;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.reflect.InvocationTargetException;

import static com.github.valid8j.classic.Requires.requireNonNull;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Inherited
public @interface ConfigureWith {
  Class<? extends Pipeline.Standard> value() default Pipeline.Standard.class;

  Entry[] pipelineArguments() default {
      @Entry(name = "strength", value = "2"),
      @Entry(name = "negativeTestGeneration", value = "false"),
      @Entry(name = "seedGeneratorMethod", value = "seeds")
  };

  /**
   * Specifies a class to define a parameter space, which has parameters, constraints
   * and non-constraint conditions. If this value is not used, (or {@code Object.class}
   * is specified, ) the same class to which {@code ConfigureWith} annotation is
   * attached is used to create a parameter space Object.
   *
   * @return A class that defines parameter space or {@code Object.class}.
   * @see ParameterSpace
   */
  Class<?> parameterSpace() default Object.class;

  @interface Entry {
    String name();

    String[] value() default {};
  }

  interface RequirementParser {
    Requirement parseRequirement(Entry[] requirement);
  }

  @ConfigureWith
  enum Utils {
    ;

    public static PipelineConfig createPipelineConfigFrom(ConfigureWith configuration) {
      Requirement r = getRequirement(configuration);
      return new PipelineConfig.Builder(r).build();

    }

    public static ConfigureWith configurationOf(Class<?> testClass) {
      return (requireNonNull(testClass).isAnnotationPresent(ConfigureWith.class) ? testClass
                                                                                 : Utils.class).getAnnotation(ConfigureWith.class);
    }

    public static Requirement getRequirement(ConfigureWith configuration) {
      RequirementParser p;
      try {
        p = configuration.value().getAnnotation(ParseRequirementWith.class).value().getConstructor().newInstance();
      } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
        throw new RuntimeException(e);
      }
      return p.parseRequirement(configuration.pipelineArguments());
    }
  }
}
