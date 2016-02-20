package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.Utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public interface SUTFactory<SUT> {
  SUT create(InteractionHistory interactionHistory);

  class Dummy<SUT> implements SUTFactory<SUT> {
    public final SUT sut;

    public Dummy(SUT sut) {
      this.sut = sut;
    }

    @Override
    public SUT create(InteractionHistory interactionHistory) {
      return this.sut;
    }
  }

  abstract class Base<SUT> implements SUTFactory<SUT> {
    protected final Class<SUT> clazz;
    protected final List<Arg>  args;

    public Base(Class<SUT> clazz, Arg... args) {
      this.clazz = Checks.checknotnull(clazz);
      this.args = Collections.unmodifiableList(Arrays.asList(args));
    }

    @Override
    public SUT create(final InteractionHistory interactionHistory) {
      Object[] argValues = Utils.transform(args,
          new Utils.Form<Arg, Object>() {
            @Override
            public Object apply(Arg in) {
              if (in.alias != null) {
                interactionHistory.add(in.alias, in.value);
              }
              return in.value;
            }
          }
      ).toArray(new Object[args.size()]);
      if (this.getAlias() != null) {
        interactionHistory.add(this.getAlias(), argValues);
      }
      return createSUT(argValues);
    }

    abstract protected SUT createSUT(Object... args);

    abstract public SUTFactory<SUT> as(String alias);

    abstract public String getAlias();

    public static class Arg<T> {
      public final Class<T> type;
      public final T        value;
      public final String   alias;

      private Arg(Class<T> type, T value) {
        this(null, type, value);
      }

      private Arg(String alias, Class<T> type, T value) {
        this.alias = alias;
        this.type = type;
        this.value = value;
      }

      public Arg<T> as(String alias) {
        return new Arg<T>(Checks.checknotnull(alias), this.type, value);
      }
    }

    /**
     * A utility method to create {@code Arg} object.
     * {@code static import} this method to reduce number of key-types.
     */
    public static <T> Arg<T> $(Class<T> type, T arg) {
      return new Arg<T>(Checks.checknotnull(type), arg);
    }
  }

  class Simple<SUT> extends Base<SUT> {
    private final String alias;

    public Simple(Class<SUT> type, Arg<?>... args) {
      this(null, type, args);
    }

    private Simple(String alias, Class<SUT> type, Arg<?>... args) {
      super(type, args);
      this.alias = alias;
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

    @Override
    public SUTFactory<SUT> as(String alias) {
      return new SUTFactory.Simple<SUT>(
          Checks.checknotnull(alias),
          this.clazz,
          this.args.toArray(new Arg[this.args.size()])
      );
    }

    @Override
    public String getAlias() {
      return this.alias;
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
