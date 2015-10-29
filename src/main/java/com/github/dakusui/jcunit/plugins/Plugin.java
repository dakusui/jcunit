package com.github.dakusui.jcunit.plugins;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.SystemProperties;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.reflect.ReflectionUtils;
import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.jcunit.runners.core.RunnerContext;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * A common interface of all plugins of JCUnit that can be configured through
 * '{@literal @}Param' annotations.
 */
public interface Plugin {
  @Retention(RetentionPolicy.RUNTIME)
  @interface Param {
    enum Source {
      RUNNER,
      INSTANCE,
      SYSTEM_PROPERTY
    }

    SystemProperties.KEY propertyKey() default SystemProperties.KEY.DUMMY;

    RunnerContext.KEY contextKey() default RunnerContext.KEY.DUMMY;

    Source source() default Source.INSTANCE;

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
      /**
       * This resolver always pass through incoming value to target constructor.
       */
      public static final Resolver<Object> NULL = new PluginUtils.PassThroughResolver();

      private List<Converter<S>> converters;

      protected Resolver(List<Converter<S>> converters) {
        this.converters = new LinkedList<Converter<S>>();
        this.converters.addAll(converters);
      }

      public <T> T resolve(Desc<T> desc, S value) {
        Checks.checknotnull(desc);
        return Checks.cast(
            desc.parameterType,
            chooseConverter(
                desc.parameterType,
                findCompatibleConverters(desc.parameterType)
            ).convert(desc.parameterType, value));
      }

      protected <T> List<Converter<S>> findCompatibleConverters(Class<T> targetType) {
        Checks.checknotnull(targetType);
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
                return Utils.toString(in);
              }
            }), this.allConverters().size());
        return ret;
      }

      abstract protected <T> Converter<S> chooseConverter(Class<T> clazz, List<Converter<S>> from);

      public List<Converter<S>> allConverters() {
        return Collections.unmodifiableList(this.converters);
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
          this.requestedType = Checks.checknotnull(requestedType);
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

  class Factory<P extends Plugin, S> {
    private final Class<P>          pluginClass;
    private final Param.Resolver<S> resolver;
    private final RunnerContext     runnerContext;

    public Factory(Class<P> pluginClass, Param.Resolver<S> resolver) {
      this.pluginClass = Checks.checknotnull(pluginClass);
      this.resolver = Checks.checknotnull(resolver);
      this.runnerContext = RunnerContext.NULL;
    }

    public P create(S... args) {
      List<Object> resolvedArgs = new LinkedList<Object>();
      try {
        int i = 0;
        try {
          Constructor<P> constructor = getConstructor();
          for (Param.Desc each : getParameterDescs(getConstructor())) {
            Param.Source source = each.parameterRequirement.source();
            if (source == Param.Source.INSTANCE) {
              if (i < args.length) {
                resolvedArgs.add(resolver.resolve(each, args[i]));
              } else {
                resolvedArgs.add(PluginUtils.StringArrayResolver.INSTANCE.resolve(each, each.parameterRequirement.defaultValue()));
              }
            } else if (source == Param.Source.RUNNER) {
              Object value = this.runnerContext.get(each.parameterRequirement.contextKey());
              resolvedArgs.add(PluginUtils.StringResolver.INSTANCE.resolve(
                  each,
                  value == null
                      ? null
                      : value.toString()
              ));
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

            if (each.parameterRequirement.source() == Param.Source.INSTANCE) {
              i++;
            }
          }
          Checks.checktest(
              i >= args.length,
              "Too many arguments are given. %s are extra.",
              i < args.length
                  ? Arrays.toString(Arrays.copyOfRange(args, i, args.length, Object[].class))
                  : null);
          Checks.checktest(resolvedArgs.size() == constructor.getParameterTypes().length,
              "%s: Too few or to many arguments: required=%s, given=%s",
              constructor.getDeclaringClass(),
              constructor.getParameterTypes().length,
              args.length,
              i
          );
          return constructor.newInstance(resolvedArgs.toArray());
        } catch (JCUnitException e) {
          throw Checks.wrap(Checks.getRootCauseOf(e), "Failed to resolve args[%s] during instantiation of plugin '%s'", i, this.pluginClass);
        }
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
            "Failed to instantiate a plugin '%s' due to an illegal access",
            this.pluginClass
        );
      }
    }

    private Constructor<P> getConstructor() {
      Constructor[] constructors = Checks.cast(Constructor[].class, this.pluginClass.getConstructors());
      Checks.checkplugin(
          constructors.length == 1,
          "There must be 1 and only 1 constructor in order to use '%s' as a JCUnit plug-in. (%s found)",
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
      Checks.checknotnull(
          paramAnn,
          "@%s annotation is missing for a parameter whose type is %s",
          Param.class,
          parameterType
      );
      return new Param.Desc<T>(paramAnn, parameterType);
    }
  }
}
