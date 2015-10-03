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
  private final String            fsmName;
  private final int               historyLength;
  private       List<State<SUT>>  states;
  private       List<Action<SUT>> actions;
  private       State<SUT>        initialState;

  public SimpleFSM(String fsmName, Class<? extends FSMSpec<SUT>> specClass, int historyLength) {
    Checks.checknotnull(fsmName);
    Checks.checknotnull(specClass);
    this.fsmName = fsmName;
    ////
    // Build 'actions'.
    Map<String, Method> actionMethods = Utils.toMap(getActionMethods(specClass), new Utils.Form<Method, String>() {
      @Override
      public String apply(Method in) {
        return generateMethodId(in);
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
      Field f = null;
      String paramsFieldName = each.getValue().getAnnotation(ActionSpec.class).parametersSpec();
      if (m.getParameterTypes().length > 1) {
        if (ActionSpec.DEFAULT_PARAMS_SPEC.equals(paramsFieldName)) {
          paramsFieldName = m.getName();
        }
        f = paramsFields.get(paramsFieldName);
        Checks.checktest(
            f != null,
            "A parameter field '%s' referred to by '%s' is not found in '%s'", paramsFieldName, each.getKey(), specClass.getCanonicalName()
        );
      } else {
        Checks.checktest(
            ActionSpec.DEFAULT_PARAMS_SPEC.equals(paramsFieldName),
            "An action without parameters but Expectation.Builder must not have 'parametersSpec' attribute."
        );
      }
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

  /**
   * {@code paramsField} can be numm if {@code actionMethod} doesn't have any parameter.
   */
  private Action<SUT> createAction(final Method actionMethod, final Field paramsField) {
    final Parameters parameters;
    if (paramsField == null) {
      parameters = Parameters.EMPTY;
    } else {
      parameters = getParamsFactors(validateParamsField(paramsField));
    }
    //noinspection unchecked
    return (Action<SUT>) new MethodAction(actionMethod, parameters);
  }

  /**
   * {@code field} must be validated by {@code validateParamsField} in advance.
   *
   * @param field A field from which {@code params} values should be retrieved.
   */
  private Parameters getParamsFactors(Field field) {
    try {
      Object ret = Checks.checknotnull(field).get(null);
      Checks.checktest(ret instanceof Parameters, "The field '%s' in %s must be typed %s", field.getName(), field.getDeclaringClass().getCanonicalName(), Parameters.class.getSimpleName());
      Checks.checktest((((Parameters) ret).values()).length > 0,
          "The field '%s' of '%s' must be assigned Object[][] value whose length is larget than 0.",
          field.getName(), field.getType().getCanonicalName());
      ////
      // Casting to Object[][] is safe because validateParamsField checks it.
      return ((Parameters) ret);
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
        Modifier.isPublic(m) && Modifier.isStatic(m) && Modifier.isFinal(m) && Parameters.class.isAssignableFrom(fsmField.getType()),
        "Field '%s' of '%s' must be public, static, final, and of %s.",
        ret.getName(), ret.getType().getSimpleName(), Parameters.class.getSimpleName()
    );
    return ret;
  }

  private State<SUT> createState(final Field stateSpecField, final Map<String, Method> actionMethods) {
    final FSMSpec<SUT> stateSpec = getStateSpecValue(validateStateSpecField(stateSpecField));
    return new SimpleFSMState(stateSpec, actionMethods, stateSpecField);
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

    public SimpleFSMState(FSMSpec<SUT> stateSpec, Map<String, Method> actionMethods, Field stateSpecField) {
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
      Method m = Checks.checknotnull(actionMethods.get(action.id()), "Unknown action '%s' was given.", action);
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
          new Object[] { new Expectation.Builder<SUT>(SimpleFSM.this.fsmName, SimpleFSM.this) },
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

  static String generateMethodId(Method m) {
    return Checks.checknotnull(m).getName() + "/" +
        Utils.join(
            ",",
            Utils.transform(Arrays.asList(m.getParameterTypes()).subList(1, m.getParameterTypes().length),
                new Utils.Form<Class<?>, String>() {
                  @Override
                  public String apply(Class in) {
                    return in.getCanonicalName();
                  }
                }).toArray());
  }

  private static class MethodAction<SUT> implements Action<SUT> {
    final         Method     method;
    final         String     name;
    private final Parameters parameters;

    /**
     * Creates an object of this class.
     *
     * @param method     An {@code ActionSpec}  annotated method in {@code FSMSpec}.
     * @param parameters A {@code ParametersSpec} annotated field's value in {@code FSMSpec}.
     */
    public MethodAction(Method method, Parameters parameters) {
      this.method = method;
      this.name = method.getName();
      this.parameters = parameters;
    }

    @Override
    public <T> Object perform(T context, SUT o, Args args) throws Throwable {
      Checks.checknotnull(o);
      Object ret = null;
      try {
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
        Checks.rethrowtesterror(
            e,
            "Non-public method testing isn't supported (%s#%s/%d isn't public)",
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
    public Object[] parameterFactorLevels(int i) {
      Object[][] paramFactors = this.parameters.values();
      Checks.checkcond(0 <= i && i < paramFactors.length, "i must be less than %d and greater than or equal to 0 but %d", paramFactors.length, i);
      return paramFactors[i];
    }

    @Override
    public int numParameterFactors() {
      Object[][] paramFactors = this.parameters.values();
      // It's safe to access the first parameter because it's already validated.
      return paramFactors.length;
    }

    @Override
    public String id() {
      return generateMethodId(this.method);
    }

    @Override
    public Class<?>[] parameterTypes() {
      return this.getParameterTypes();
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
        if (each.getName().equals(name) && equals(this.getParameterTypes(), each.getParameterTypes())) {
          ret = each;
          break;
        }
      }
      Checks.checktest(ret != null, "No method '%s/%d' is found in '%s'", name, numArgs, klass.getCanonicalName());
      return ret;
    }

    /**
     * Returns parameter types of a method that this action represents in SUT (not in Spec).
     */
    private Class<?>[] getParameterTypes() {
      Class<?>[] ret = this.method.getParameterTypes();
      ret = Arrays.asList(this.method.getParameterTypes()).subList(1, ret.length).toArray(new Class<?>[ret.length - 1]);
      return ret;
    }

    private static boolean equals(Class<?>[] parameterTypesA, Class<?>[] parameterTypesB) {
      return Arrays.equals(parameterTypesA, parameterTypesB);
    }
  }
}
