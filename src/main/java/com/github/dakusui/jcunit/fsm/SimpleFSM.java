package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.fsm.spec.ActionSpec;
import com.github.dakusui.jcunit.fsm.spec.FSMSpec;
import com.github.dakusui.jcunit.fsm.spec.ParametersSpec;
import com.github.dakusui.jcunit.fsm.spec.StateSpec;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class SimpleFSM<SUT> implements FSM<SUT> {
  private final String            name;
  private final int               historyLength;
  private       List<State<SUT>>  states;
  private       List<Action<SUT>> actions;
  private       State<SUT>        initialState;

  public <T> SimpleFSM(String name, Class<? extends FSMSpec<SUT>> specClass, int historyLength) {
    Checks.checknotnull(name);
    Checks.checknotnull(specClass);
    this.name = name;
    ////
    // Build 'actions'.
    Map<String, Method> actionMethods = Utils.toMap(getActionMethods(specClass), new Utils.Form<Method, String>() {
      @Override
      public String apply(Method in) {
        return in.getName();
      }
    });
    Map<String, Field> paramsFields = Utils.toMap(getParamsFields(specClass), new Utils.Form<Field, String>() {
      @Override
      public String apply(Field in) {
        return in.getName();
      }
    });
    List<Action<SUT>> actions = new LinkedList<Action<SUT>>();
    for (Map.Entry<String, Method> each : actionMethods.entrySet()) {
      Method m = each.getValue();
      Field f = paramsFields.get(each.getKey());
      actions.add(createAction(m, f));
    }
    this.actions = Collections.unmodifiableList(actions);
    ////
    // Build states and initialState.
    Map<String, Field> stateFields = Utils.toMap(getStateFields(specClass), new Utils.Form<Field, String>() {
      @Override
      public String apply(Field in) {
        return in.getName();
      }
    });
    List<State<SUT>> states = new LinkedList<State<SUT>>();
    State<SUT> initialState = null;
    for (Map.Entry<String, Field> each : stateFields.entrySet()) {
      states.add(createState(each.getValue(), actionMethods));
      if ("I".equals(each.getKey()))
        initialState = states.get(states.size() - 1);
    }
    Checks.checktest(initialState != null,
        "A state whose name is 'I'(annotated with %s) couldn't be found in '%s'",
        StateSpec.class.getSimpleName(),
        specClass.getCanonicalName()
    );
    this.states = Collections.unmodifiableList(states);
    this.initialState = initialState;
    this.historyLength = historyLength;
  }

  @Override
  public State<SUT> initialState() {
    return this.initialState;
  }

  @Override
  public List<State<SUT>> states() {
    return this.states;
  }

  @Override
  public List<Action<SUT>> actions() {
    return this.actions;
  }

  @Override
  public int historyLength() {
    return historyLength;
  }

  @Override
  public String name() {
    return this.name;
  }

  private List<Field> getStateFields(Class<? extends FSMSpec<SUT>> specClass) {
    List<Field> ret = new LinkedList<Field>();
    for (Field each : specClass.getFields()) {
      if (each.isAnnotationPresent(StateSpec.class) && !isAlreadyAddedIn(each, ret)) {
        ret.add(each);
      }
    }
    return ret;
  }

  private List<Field> getParamsFields(Class<? extends FSMSpec<SUT>> specClass) {
    List<Field> ret = new LinkedList<Field>();
    for (final Field each : specClass.getFields()) {
      if (each.isAnnotationPresent(ParametersSpec.class) && !isAlreadyAddedIn(each, ret))
        ret.add(each);
    }
    return ret;
  }

  private boolean isAlreadyAddedIn(final Field each, List<Field> list) {
    return !Utils.filter(list, new Utils.Predicate<Field>() {
      @Override
      public boolean apply(Field in) {
        return each.getName().equals(in.getName()) && each.getType().isAssignableFrom(in.getType());
      }
    }).isEmpty();
  }

  private List<Method> getActionMethods(Class<? extends FSMSpec<SUT>> specClass) {
    List<Method> ret = new LinkedList<Method>();
    for (Method each : specClass.getMethods()) {
      if (each.isAnnotationPresent(ActionSpec.class)) {
        ret.add(each);
      }
    }
    return ret;
  }

  private Action<SUT> createAction(final Method actionMethod, final Field paramsField) {
    final Object[][] paramFactors;
    if (paramsField == null) {
      paramFactors = new Object[][] {};
    } else {
      paramFactors = getParamsFactors(validateParamsField(paramsField));
    }
    //noinspection unchecked
    return (Action<SUT>) new MethodAction(actionMethod, paramFactors);
  }

  /**
   * {@code field} must be validated by {@code validateParamsField} in advance.
   *
   * @param field A field from which {@code params} values should be retrieved.
   */
  private Object[][] getParamsFactors(Field field) {
    try {
      Object ret = field.get(null);
      Checks.checktest(
          ret != null && ((Object[][]) ret).length > 0,
          "The field '%s' of '%s' must be assigned Object[][] value whose length is larget than 0.",
          field.getName(), field.getType().getCanonicalName());
      ////
      // Casting to Object[][] is safe because validateParamsField checks it.
      return (Object[][]) ret;
    } catch (IllegalAccessException e) {
      ////
      // This will never happen because filed should be validated in advance.
      throw new RuntimeException();
    }
  }

  private Field validateParamsField(Field fsmField) {
    Field ret = Checks.checknotnull(fsmField);
    int m = ret.getModifiers();
    Checks.checktest(
        Modifier.isPublic(m) && Modifier.isStatic(m) && Modifier.isFinal(m) && FSMUtils.DOUBLE_ARRAYED_OBJECT_CLASS.isAssignableFrom(fsmField.getType()),
        "Field '%s' of '%s' must be public, static, final, and of Object[][].",
        ret.getName(), ret.getType().getCanonicalName()
    );
    return ret;
  }

  private State<SUT> createState(final Field stateSpecField, final Map<String, Method> actionMethods) {
    final FSMSpec<SUT> stateSpec = getStateSpecValue(validateStateSpecField(stateSpecField));
    return new SimpleFSMState(stateSpecField.getName(), stateSpec, actionMethods, stateSpecField);
  }

  private FSMSpec<SUT> getStateSpecValue(Field field) {
    try {
      Object ret = field.get(null);
      Checks.checktest(ret != null, "The field '%s' of '%s' must be assigned a non-null value.", field.getName(), field.getType().getCanonicalName());
      ////
      // Casting to Object[][] is safe because validateParamsField checks it.
      //noinspection unchecked
      return (FSMSpec<SUT>) ret;
    } catch (IllegalAccessException e) {
      ////
      // This will never happen because filed should be validated in advance.
      throw new RuntimeException();
    }
  }

  private Field validateStateSpecField(Field fsmField) {
    Field ret = Checks.checknotnull(fsmField);
    int m = ret.getModifiers();
    Checks.checktest(
        Modifier.isPublic(m) && Modifier.isStatic(m) && Modifier.isFinal(m) && fsmField.getType().isAssignableFrom(fsmField.getType()),
        "Field '%s' of '%s' must be public, static, final, and of '%s'.",
        ret.getName(), ret.getType().getCanonicalName(), fsmField.getType().getCanonicalName()
    );
    return ret;
  }

  class SimpleFSMState implements State<SUT> {
    final         FSMSpec<SUT>        stateSpec;
    private final Map<String, Method> actionMethods;
    private final Field               stateSpecField;
    private final String              fsmName;

    public SimpleFSMState(String fsmName, FSMSpec<SUT> stateSpec, Map<String, Method> actionMethods, Field stateSpecField) {
      this.fsmName = fsmName;
      this.stateSpec = stateSpec;
      this.actionMethods = actionMethods;
      this.stateSpecField = stateSpecField;
    }

    @Override
    public boolean check(SUT sut) {
      return stateSpec.check(sut);
    }

    @Override
    public Expectation<SUT> expectation(Action<SUT> action, Args args) {
      Expectation<SUT> ret = null;
      Method m = Checks.checknotnull(actionMethods.get(action.toString()), "Unknown action '%s' was given.", action);
      Checks.checktest(
          Expectation.class.isAssignableFrom(m.getReturnType()),
          "Method '%s/%d' of '%s' must return an '%s' object (but '%s' was returned).",
          m.getName(),
          m.getParameterTypes().length,
          m.getDeclaringClass().getCanonicalName(),
          Expectation.class.getCanonicalName(),
          m.getReturnType().getCanonicalName()
      );
      Object[] argsToMethod = Utils.concatenate(
          new Object[] { SimpleFSM.this },
          args.values()
      );
      try {
        //noinspection unchecked
        ret = (Expectation<SUT>) m.invoke(stateSpec, argsToMethod);
      } catch (IllegalArgumentException e) {
        Checks.rethrowtesterror(
            e,
            "Wrong types: '%s/%s' of '%s' can't be executed with %s",
            m.getName(),
            m.getParameterTypes().length,
            m.getDeclaringClass(),
            Arrays.toString(argsToMethod)
        );
      } catch (IllegalAccessException e) {
        // Since the method is validated in advance, this path should never be executed.
        Checks.fail();
      } catch (InvocationTargetException e) {
        Checks.rethrowtesterror(
            e,
            "Method '%s/%s' of '%s' must always succeed and return an object of '%s'.",
            m.getName(), args.values().length, stateSpec.getClass().getCanonicalName(), Expectation.class.getCanonicalName()
        );
      }
      return ret;
    }

    @Override
    public String toString() {
      StateSpec ann = this.stateSpecField.getAnnotation(StateSpec.class);
      if (ann.value().length() > 0) {
        return String.format("%s(%s)", stateSpecField.getName(), ann.value());
      }
      return String.format("%s", stateSpecField.getName());
    }
  }

  private static class MethodAction<SUT> implements Action<SUT> {
    final         Method     method;
    final         String     name;
    private final Object[][] paramFactors;

    public MethodAction(Method method, Object[][] paramFactors) {
      this.method = method;
      this.name = method.getName();
      this.paramFactors = paramFactors;
    }

    @Override
    public <T> Object perform(T context, SUT o, Args args) throws Throwable {
      Checks.checknotnull(o);
      Object ret = null;
      try {
        // +1 is for a context object to ba passed to the method on invocation.
        Method m = chooseMethod(o.getClass(), name, args.size());
        try {
          ret = m.invoke(o, args.values());
        } catch (IllegalArgumentException e) {
          throw new IllegalArgumentException(String.format("Method '%s/%d' in '%s' expects %s, but %s are given.",
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
        Checks.rethrowtesterror(e, "Non-public method testing isn't supported (%s#%s isn't public)");
      } catch (InvocationTargetException e) {
        throw e.getTargetException();
      }
      return ret;
    }

    @Override
    public Object[] parameterFactorLevels(int i) {
      Checks.checkcond(0 <= i && i < paramFactors.length, "i must be less than %d and greater than or equal to 0 but %d", paramFactors.length, i);
      return paramFactors[i];
    }

    @Override
    public int numParameterFactors() {
      // It's safe to access the first parameter because it's already validated.
      return paramFactors.length;
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
      if (!(anotherObject instanceof MethodAction))
        return false;
      // It's safe to cast to MethodAction because it's already checked.
      //noinspection unchecked
      MethodAction another = (MethodAction) anotherObject;
      return this.method.equals(another.method);
    }

    private Method chooseMethod(Class<?> klass, String name, int numArgs) {
      Method ret = null;
      for (Method each : klass.getMethods()) {
        if (each.getName().equals(name) && each.getParameterTypes().length == numArgs) {
          Checks.checktest(ret == null, "There are more than 1 method '%s/%d' in '%s'", name, numArgs, klass.getCanonicalName());
          ret = each;
        }
      }
      Checks.checktest(ret != null, "No method '%s/%d' is found in '%s'", name, numArgs, klass.getCanonicalName());
      return ret;
    }
  }
}
