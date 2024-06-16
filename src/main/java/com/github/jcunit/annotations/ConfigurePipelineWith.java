package com.github.jcunit.annotations;

import com.github.jcunit.core.tuples.Tuple;
import com.github.jcunit.factorspace.ParameterSpace;
import com.github.jcunit.pipeline.ParseConfigWith;
import com.github.jcunit.pipeline.Pipeline;
import com.github.jcunit.pipeline.PipelineConfig;
import com.github.jcunit.pipeline.PipelineSpec;

import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.valid8j.classic.Requires.requireNonNull;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

@Retention(RUNTIME)
@Target(TYPE)
@Inherited
public @interface ConfigurePipelineWith {
  Class<? extends Pipeline.Standard> value() default Pipeline.Standard.class;

  Entry[] arguments() default {
      @Entry(name = "strength", value = "2"),
      @Entry(name = "negativeTestGeneration", value = "false"),
      @Entry(name = "seedGeneratorMethod", value = {})
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
  Class<?> parameterSpaceSpecClass() default Object.class;

  @interface Entry {
    String name();

    String[] value() default {};
  }

  interface PipelineConfigArgumentsParser {
    PipelineConfig parseConfig(Entry[] configEntries, Map<String, Supplier<List<Tuple>>> seedGenerators);
  }

  @ConfigurePipelineWith
  enum Utils {
    ;

    public static PipelineSpec createPipelineSpecFrom(ConfigurePipelineWith configuration, Entry[] arguments, Map<String, Supplier<List<Tuple>>> seedGenerators) {
      PipelineConfigArgumentsParser p = getArgumentsParser(configuration);
      PipelineConfig config = p.parseConfig(arguments, seedGenerators);
      return new PipelineSpec.Builder(config).build();
    }

    public static ConfigurePipelineWith configurationOf(Class<?> testClass) {
      ConfigurePipelineWith base = (requireNonNull(testClass).isAnnotationPresent(ConfigurePipelineWith.class)
                                    ? testClass
                                    : Utils.class).getAnnotation(ConfigurePipelineWith.class);
      List<Entry> configsFromInheritanceTree = collectEntriesFromInheritanceTree(testClass);
      return new ConfigurePipelineWith() {

        @Override
        public Class<? extends Annotation> annotationType() {
          return base.annotationType();
        }

        @Override
        public Class<? extends Pipeline.Standard> value() {
          return base.value();
        }

        @Override
        public Entry[] arguments() {
          return configsFromInheritanceTree.toArray(new Entry[0]);
        }

        @Override
        public Class<?> parameterSpaceSpecClass() {
          return base.parameterSpaceSpecClass();
        }
      };
    }

    private static List<Entry> collectEntriesFromInheritanceTree(Class<?> testClass) {
      List<Entry> ret = new LinkedList<>(entriesDirectlyDefinedIn(testClass));
      collectEntriesFromInheritanceTree(testClass, ret);
      return ret;
    }

    private static void collectEntriesFromInheritanceTree(Class<?> klass, List<Entry> work) {
      if (klass.equals(Object.class))
        return;
      for (Class<?> each : Stream.concat(Stream.of(klass.getSuperclass()), Stream.of(klass.getInterfaces())).collect(Collectors.toList())) {
        work.addAll(entriesDirectlyDefinedIn(each));
        collectEntriesFromInheritanceTree(each, work);
      }
    }

    private static List<Entry> entriesDirectlyDefinedIn(Class<?> klass) {
      if (klass.isAnnotationPresent(ConfigurePipelineWith.class)) {
        return asList(klass.getAnnotation(ConfigurePipelineWith.class).arguments());
      }
      return emptyList();
    }

    private static PipelineConfigArgumentsParser getArgumentsParser(ConfigurePipelineWith configuration) {
      try {
        return configuration.value()
                            .getAnnotation(ParseConfigWith.class)
                            .value()
                            .getConstructor()
                            .newInstance();
      } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
