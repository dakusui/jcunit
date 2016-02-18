package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.Utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public interface SUTFactory<SUT> {
  SUT create(InputHistory inputHistory);

  class Dummy<SUT> implements SUTFactory<SUT> {
    public final SUT sut;

    public Dummy(SUT sut) {
      this.sut = sut;
    }

    @Override
    public SUT create(InputHistory inputHistory) {
      return this.sut;
    }
  }

  abstract class Base<B extends Base, SUT> extends InputHistory.CollectorHolder<B> implements SUTFactory<SUT> {
    protected final Class<SUT> clazz;
    protected final List<Arg>  args;

    public Base(Class<SUT> clazz, Arg... args) {
      this.clazz = Checks.checknotnull(clazz);
      this.args = Collections.unmodifiableList(Arrays.asList(args));
    }

    @Override
    public SUT create(InputHistory inputHistory) {
      Object[] argValues = Utils.transform(args,
          new Utils.Form<Arg, Object>() {
            @Override
            public Object apply(Arg in) {
              return in.arg;
            }
          }
      ).toArray(new Object[args.size()]);
      for (InputHistory.Collector each : this.collectors) {
        each.apply(inputHistory, argValues);
      }
      return createSUT(argValues);
    }

    abstract protected SUT createSUT(Object... args);

    public static class Arg<T> {
      public final Class<T> type;
      public final T        arg;

      public Arg(Class<T> type, T arg) {
        this.type = type;
        this.arg = arg;
      }
    }

    public static class AliasedArg<T> extends Arg<T> {
      public final String alias;

      public AliasedArg(Class<T> type, T arg, String alias) {
        super(type, arg);
        this.alias = alias;
      }
    }

    /**
     * A utility method to create {@code Arg} object.
     * {@code static import} this method to reduce number of key-types.
     */
    public static <T> Arg<T> $(Class<T> type, T arg) {
      return new Arg<T>(Checks.checknotnull(type), arg);
    }

    /**
     * A utility method to create {@code Arg} object.
     * {@code static import} this method to reduce number of key-types.
     */
    public static <T> Arg<T> $(Class<T> type, T arg, String alias) {
      return new AliasedArg<T>(Checks.checknotnull(type), arg, alias);
    }

    public static final Class<boolean[]> BOOLEAN_ARRAY_TYPE = boolean[].class;
    public static final Class<byte[]>    BYTE_ARRAY_TYPE    = byte[].class;
    public static final Class<char[]>    CHAR_ARRAY_TYPE    = char[].class;
    public static final Class<short[]>   SHORT_ARRAY_TYPE   = short[].class;
    public static final Class<int[]>     INT_ARRAY_TYPE     = int[].class;
    public static final Class<long[]>    LONG_ARRAY_TYPE    = long[].class;
    public static final Class<float[]>   FLOAT_ARRAY_TYPE   = float[].class;
    public static final Class<double[]>  DOUBLE_ARRRAY_TYPE = double[].class;
  }

  class Simple<SUT> extends Base<Simple, SUT> {

    public Simple(Class<SUT> type, Arg<?>... args) {
      super(type, args);
    }

    @Override
    protected SUT createSUT(Object... args) {
      try {
        return chooseConstructor(this.clazz, Utils.transform(this.args,
            new Utils.Form<Arg, Class>() {
              @Override
              public Class apply(Arg in) {
                return in.type;
              }
            }
        ).toArray(new Class[this.args.size()])).newInstance(args);
      } catch (InstantiationException e) {
        throw Checks.wrappluginerror(e, "Failed to instantiate %s", this.clazz);
      } catch (IllegalAccessException e) {
        throw Checks.wrappluginerror(e, "Illegal access. Constructor of %s is not public enough", this.clazz);
      } catch (InvocationTargetException e) {
        throw Checks.wrappluginerror(
            e.getTargetException(),
            "Exception thrown during instantiation. (%s)", e.getTargetException().getMessage());
      }
    }

    private Constructor<SUT> chooseConstructor(Class<SUT> clazz, Class<?>[] parameterTypes) {
      try {
        return clazz.getConstructor(parameterTypes);
      } catch (NoSuchMethodException e) {
        throw Checks.wrappluginerror(e, "No mathing constructor found %s(%s)", clazz, parameterTypes);
      }
    }
  }
}
