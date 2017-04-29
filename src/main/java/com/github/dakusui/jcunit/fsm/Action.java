package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.utils.Checks;
import com.github.dakusui.jcunit8.exceptions.TestDefinitionException;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Arrays.asList;

/**
 * An interface that represents an action that can be performed on {@code SUT}.
 *
 * @param <SUT> A software under test.
 */
public interface Action<SUT> extends Serializable {
  /**
   * Performs this action with {@code args} on a given SUT {@code sut}.
   * An implementation of this method should usually represent and execute a method of
   * {@code sut} and return the value the method returns.
   * <p/>
   * {@code args} is composed from the returned value of {@code args} method.
   * The framework will pick up a value from a factor's levels returned by the method
   * one by one and creates an array of objects.
   * <p/>
   * The array will be passed to this method's second argument.
   */
  Object perform(SUT sut, Args args) throws Throwable;

  /**
   * Returns {@code Parameters} that belong to this object.
   */
  Parameters parameters();

  /**
   * Returns a number of parameters that this action takes.
   */
  int numParameterFactors();

  /**
   * Returns an identifier of this object.
   */
  String id();

  class Base<SUT> implements Action<SUT> {
    final         Method     method;
    final         String     name;
    private final Parameters parameters;

    /**
     * Creates an object of this class.
     *
     * @param method     An {@code ActionSpec}  annotated method in {@code FsmSpec}.
     * @param parameters A {@code ParametersSpec} annotated field's value in {@code FsmSpec}.
     */
    public Base(Method method, Parameters parameters) {
      this.method = method;
      this.name = method.getName();
      this.parameters = parameters;
    }

    @Override
    public Object perform(SUT o, Args args) throws Throwable {
      Checks.checknotnull(o);
      Object ret;
      try {
        Method m = chooseMethod(o.getClass(), name);
        try {
          ret = m.invoke(o, args.values());
        } catch (IllegalArgumentException e) {
          throw new IllegalArgumentException(format("Method '%s/%d' in '%s' expects %s, but %s are given.",
              name, args.size(),
              o.getClass().getCanonicalName(),
              Arrays.toString(m.getParameterTypes()),
              Arrays.toString(args.types())
          ));
        }
      } catch (IllegalAccessException e) {
        ////
        // I know it's possible to support non-public method test by accessing
        // security manager and it's easy. But I can't be sure it's useful
        // yet and a careless introduction of a new feature can create a
        // compatibility conflicts in future, so I'm not supporting it for now.
        throw Checks.wraptesterror(
            e,
            "Non-public method (or anonymous class method) testing isn't supported (%s#%s/%s isn't public)",
            o.getClass().getCanonicalName(),
            this.name,
            args.size()
        );
      } catch (InvocationTargetException e) {
        throw e.getTargetException();
      }
      return ret;
    }

    @Override
    public Parameters parameters() {
      return this.parameters;
    }

    @Override
    public int numParameterFactors() {
      List<List> paramFactors = this.parameters.values();
      // It's safe to access the first parameter because it's already validated.
      return paramFactors.size();
    }

    @Override
    public String id() {
      return FiniteStateMachine.Impl.generateMethodId(this.method);
    }

    @Override
    public String toString() {
      return method.getName();
    }

    @Override
    public int hashCode() {
      return method.hashCode();
    }

    @Override
    public boolean equals(Object anotherObject) {
      if (!(anotherObject instanceof Base))
        return false;
      // It's safe to cast to MethodAction because it's already checked.
      //noinspection unchecked
      Base another = (Base) anotherObject;
      return this.method.equals(another.method);
    }

    private Method chooseMethod(Class<?> klass, String name) {
      return Stream.of(klass.getMethods())
          .filter((Method eachMethod) -> eachMethod.getName().equals(name))
          .filter((Method eachMethod) -> getParameterTypes().equals(asList(eachMethod.getParameterTypes())))
          .findFirst()
          .orElseThrow(
              TestDefinitionException.sutDoesNotHaveSpecifiedMethod(klass, name, getParameterTypes())
          );
    }

    /**
     * Returns parameter types of a method that this action represents in SUT (not in Spec).
     */
    private List<Class<?>> getParameterTypes() {
      Class<?>[] parameterTypes = this.method.getParameterTypes();
      return asList(parameterTypes).subList(1, parameterTypes.length);
    }
  }
}
