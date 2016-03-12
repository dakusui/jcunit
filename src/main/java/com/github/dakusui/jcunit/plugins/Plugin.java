package com.github.dakusui.jcunit.plugins;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.StringUtils;
import com.github.dakusui.jcunit.core.SystemProperties;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.reflect.ReflectionUtils;
import com.github.dakusui.jcunit.runners.core.RunnerContext;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static com.github.dakusui.jcunit.core.Checks.checknotnull;

/**
 * A common interface of all plugins of JCUnit that can be configured through
 * '{@literal @}Param' annotations.
 */
public interface Plugin {
  @Retention(RetentionPolicy.RUNTIME)
  @interface Param {
    enum Source {
      /**
       * The parameter's value should be assigned from a runner context item
       * specified by the value of {@code contextKey}.
       */
      CONTEXT,
      /**
       * The parameter should be read from a configuration (typically annotation's
       * value).
       * Following is an example of this type of parameter source.
       * <p/>
       * If a plugin's first constructor parameter is annotated with {@literal @}{@code Param},
       * and its {@code source} has this element as its value,
       * <code>
       * public IPO2CoveringArrayEngine(
       * <p/>
       * {@literal @}(source = Param.Source.CONFIG, defaultValue = "2") int strength) {
       * ...
       * }
       * </code>
       * the first {@literal @}{@code Value} will be passed as the value for it after the
       * string is processed by {@code Plugin.Param.Resolver} by a following code fragment.
       * <p/>
       * <code>
       * Factory<CoveringArrayEngine, Value> pluginFactory = Factory.newFactory(engineClass, new Value.Resolver(), this.runnerContext);
       * return pluginFactory.create(this.args);
       * </code>
       * <p/>
       * In an example below, the first element of {@code args} is {@literal @}{@code Value("1")}.
       * This will be processed by
       * <code>
       * {@literal @}GenerateCoveringArrayWith( engine = @Generator(value = IPO2CoveringArrayEngine.class, args = {@literal @}Value("1")
       * ))
       * </code>
       * <p/>
       */
      CONFIG,
      /**
       * The parameter's value should be assigned from a system property specified by the value of
       * {@code propertyKey}.
       */
      SYSTEM_PROPERTY,
    }

    SystemProperties.Key propertyKey() default SystemProperties.Key.DUMMY;

    RunnerContext.Key contextKey() default RunnerContext.Key.DUMMY;

    Source source() default Source.CONFIG;

    String[] defaultValue() default {};


    class Desc<R> {
      public final Param    parameterRequirement;
      public final Class<R> parameterType;

      public Desc(Param parameterRequirement, Class<R> parameterType) {
        this.parameterRequirement = parameterRequirement;
        this.parameterType = parameterType;
      }
    }

    abstract class Resolver<S> implements Cloneable {
      public static <S> Resolver<S> passThroughResolver() {
        // safe cast for null object pattern.
        //noinspection unchecked
        return (Resolver<S>) PassThroughResolver.INSTANCE;
      }

      private List<Converter<S>> converters;

      protected Resolver(List<Converter<S>> converters) {
        this.converters = new LinkedList<Converter<S>>();
        this.converters.addAll(converters);
      }

      public <T> T resolve(Desc<T> desc, S value) {
        checknotnull(desc);
        return Checks.cast(desc.parameterType, chooseConverter(
            desc.parameterType,
            findCompatibleConverters(desc.parameterType)
        ).convert(desc.parameterType, value));
      }

      protected <T> List<Converter<S>> findCompatibleConverters(Class<T> targetType) {
        checknotnull(targetType);
        List<Converter<S>> ret = new ArrayList<Converter<S>>(this.allConverters().size());

        for (Converter<S> each : this.allConverters()) {
          if (each.supports(targetType)) {
            ret.add(each);
          }
        }
        Checks.checkcond(
            ret.size() > 0,
            "No compatible converter is found for target type '%s' in %s (all known converters:%s;%s)",
            targetType,
            this,
            Utils.transform(this.allConverters(), new Utils.Form<Converter<S>, String>() {
              @Override
              public String apply(Converter<S> in) {
                return StringUtils.toString(in);
              }
            }), this.allConverters().size());
        return ret;
      }

      abstract protected <T> Converter<S> chooseConverter(Class<T> clazz, List<Converter<S>> from);

      public List<Converter<S>> allConverters() {
        return Collections.unmodifiableList(this.converters);
      }

      public static class PassThroughResolver extends Plugin.Param.Resolver<Object> {
        /**
         * This resolver always pass through incoming value to base constructor.
         */
        private static final PassThroughResolver INSTANCE = new PassThroughResolver();

        protected PassThroughResolver() {
          super(createConverters());
        }

        private static List<Converter<Object>> createConverters() {
          List<Converter<Object>> converters = new ArrayList<Converter<Object>>(1);
          converters.add(Converter.NULL);
          return Collections.unmodifiableList(converters);
        }

        @Override
        protected <T> Converter<Object> chooseConverter(Class<T> clazz, List<Converter<Object>> from) {
          return from.get(0);
        }
      }
    }

    /**
     * @param <I> Input type. E.g., {@literal @}{@code Param}.
     */
    interface Converter<I> {
      Converter<Object> NULL = new Converter<Object>() {
        @Override
        public Object convert(Class requested, Object in) {
          return Checks.cast(requested, in);
        }

        @Override
        public boolean supports(Class<?> target) {
          return true;
        }
      };

      Object convert(Class requested, I in);

      boolean supports(Class<?> target);

      abstract class Simple<I> implements Converter<I> {
        private final Class requestedType;

        public Simple(Class requestedType) {
          this.requestedType = checknotnull(requestedType);
        }

        @Override
        public Object convert(Class requested, I in) {
          //noinspection unchecked
          return Checks.cast(this.requestedType, convert(in));
        }

        @Override
        public boolean supports(Class<?> target) {
          return ReflectionUtils.isAssignable(target, this.outputType());
        }

        protected abstract Object convert(I in);

        protected Class<?> outputType() {
          return this.requestedType;
        }
      }
    }
  }

  abstract class Base implements Plugin {
  }

  class Factory<P extends Plugin, S> {
    private final Class<? super P>  pluginClass;
    private final Param.Resolver<S> resolver;
    private final RunnerContext     runnerContext;

    public Factory(Class<? super P> pluginClass, Param.Resolver<S> resolver, RunnerContext runnerContext) {
      this.pluginClass = checknotnull(pluginClass);
      this.resolver = checknotnull(resolver);
      this.runnerContext = checknotnull(runnerContext);
    }

    public P create(List<S> args) {
      List<Object> resolvedArgs = new LinkedList<Object>();
      try {
        int i = 0;
        Constructor<P> constructor = getConstructor();
        for (Param.Desc each : getParameterDescs(getConstructor())) {
          Param.Source source = each.parameterRequirement.source();
          if (source == Param.Source.CONFIG) {
            if (i < args.size()) {
              resolvedArgs.add(resolver.resolve(each, args.get(i)));
            } else {
              resolvedArgs.add(PluginUtils.StringArrayResolver.INSTANCE.resolve(each, each.parameterRequirement.defaultValue()));
            }
          } else if (source == Param.Source.CONTEXT) {
            Object value = this.runnerContext.get(each.parameterRequirement.contextKey());
            resolvedArgs.add(Checks.cast(each.parameterType, value));
          } else if (source == Param.Source.SYSTEM_PROPERTY) {
            String defaultValue = null;
            if (each.parameterRequirement.defaultValue().length > 0) {
              defaultValue = each.parameterRequirement.defaultValue()[0];
            }
            String value = SystemProperties.get(
                each.parameterRequirement.propertyKey(),
                defaultValue
            );
            resolvedArgs.add(PluginUtils.StringResolver.INSTANCE.resolve(each, value));
          } else {
            Checks.checkcond(false,
                "Unknown source: '%s' is given.",
                source
            );
          }

          if (each.parameterRequirement.source() == Param.Source.CONFIG) {
            i++;
          }
        }
        Checks.checktest(
            i >= args.size(),
            "Too many arguments are given. %s are extra.",
            i < args.size()
                ? args.subList(i, args.size())
                : null);
        Checks.checktest(resolvedArgs.size() == constructor.getParameterTypes().length,
            "%s: Too few or to many arguments: required=%s, given=%s",
            constructor.getDeclaringClass(),
            constructor.getParameterTypes().length,
            args.size(),
            i
        );
        P ret =   constructor.newInstance(resolvedArgs.toArray());
        return ret;
      } catch (InstantiationException e) {
        throw Checks.wrap(
            e,
            "Failed to instantiate a plugin '%s'",
            this.pluginClass
        );
      } catch (IllegalAccessException e) {
        throw Checks.wrap(
            e,
            "Failed to instantiate a plugin '%s' due to an illegal access",
            this.pluginClass
        );
      } catch (InvocationTargetException e) {
        throw Checks.wrap(
            e.getTargetException(),
            "Failed to instantiate a plugin '%s' due to an error",
            this.pluginClass
        );
      }
    }

    private Constructor<P> getConstructor() {
      Constructor[] constructors = Checks.cast(Constructor[].class, this.pluginClass.getConstructors());
      Checks.checkplugin(
          constructors.length == 1,
          "There must be 1 and only 1 public constructor in order to use '%s' as a JCUnit plug-in(%s found). Also please make sure the class is public and static.",
          this.pluginClass,
          constructors.length);
      //noinspection unchecked
      return (Constructor<P>) constructors[0];
    }

    private static <P> List<Param.Desc> getParameterDescs(Constructor<P> constructor) {
      List<Param.Desc> ret = new LinkedList<Param.Desc>();
      Class<?>[] parameterTypes = constructor.getParameterTypes();
      int i = 0;
      for (Annotation[] each : constructor.getParameterAnnotations()) {
        ret.add(createDesc(parameterTypes[i], each));
        i++;
      }
      return ret;
    }

    private static <T> Param.Desc createDesc(Class<T> parameterType, Annotation[] annotationsToParameter) {
      Param paramAnn = null;
      for (Annotation each : annotationsToParameter) {
        if (each instanceof Param) {
          paramAnn = Checks.cast(Param.class, each);
          break;
        }
      }
      checknotnull(
          paramAnn,
          "@%s annotation is missing for a parameter whose type is %s",
          Param.class,
          parameterType
      );
      return new Param.Desc<T>(paramAnn, parameterType);
    }

    public static <P extends Plugin, S>
    Factory<P, S> newFactory(Class<? extends P> pluginClass, Param.Resolver<S> resolver, RunnerContext runnerContext) {
      //noinspection unchecked
      return new Factory<P, S>((Class<? super P>) pluginClass, resolver, runnerContext);
    }
  }
}
