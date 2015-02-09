package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.fsm.spec.ActionSpec;
import com.github.dakusui.jcunit.fsm.spec.FSMSpec;
import com.github.dakusui.jcunit.fsm.spec.ParametersSpec;
import com.github.dakusui.jcunit.fsm.spec.StateSpec;
import org.hamcrest.CoreMatchers;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A utility class for FSM (finite state machine) support of JCUnit.
 */
public class FSMUtils {
  private static final Class<? extends Object[][]> DOUBLE_ARRAYED_OBJECT_CLASS = Object[][].class;
  private static final Pattern                     fsmFactorPattern            = Pattern.compile("FSM:(main|setUp):([^:]+)");

  private FSMUtils() {
  }

  public static <SUT> void performScenarioSequence(ScenarioSequence<SUT> scenarioSeq, SUT sut, ScenarioSequence.Reporter<SUT> reporter) throws Throwable {
    Checks.checknotnull(scenarioSeq);
    Checks.checknotnull(reporter);
    reporter.startSequence(scenarioSeq);
    try {
      for (int i = 0; i < scenarioSeq.size(); i++) {
        Scenario<SUT> each = scenarioSeq.get(i);

        Expectation.Result result = null;
        reporter.run(each, sut);
        try {
          Object r = each.perform(sut);
          ////
          // each.perform(sut) didn't throw an exception
          //noinspection unchecked,ThrowableResultOfMethodCallIgnored
          result = each.then().checkReturnedValue(sut, r);
        } catch (Throwable t) {
          //noinspection unchecked,ThrowableResultOfMethodCallIgnored
          result = each.then().checkThrownException(sut, t);
        } finally {
          if (result != null) {
            if (result.isSuccessful())
              reporter.passed(each, sut);
            else
              reporter.failed(each, sut);
            result.throwIfFailed();
          }
        }
      }
    } finally {
      reporter.endSequence(scenarioSeq);
    }
  }

  public static <SUT> Expectation<SUT> invalid() {
    return invalid(IllegalStateException.class);
  }

  public static <SUT> Expectation<SUT> invalid(Class<? extends Throwable> klass) {
    //noinspection unchecked
    return new Expectation(Expectation.Type.EXCEPTION_THROWN, State.VOID, CoreMatchers.instanceOf(klass));
  }

  public static <SUT> Expectation<SUT> invalid(FSM<SUT> fsm, FSMSpec<SUT> state, Class<? extends Throwable> klass) {
    Checks.checknotnull(fsm);
    Checks.checknotnull(state);
    //noinspection unchecked
    return new Expectation(Expectation.Type.EXCEPTION_THROWN, chooseState(fsm, state), CoreMatchers.instanceOf(klass));
  }

  public static <SUT> Expectation<SUT> valid(FSM<SUT> fsm, FSMSpec<SUT> state) {
    return new Expectation<SUT>(Expectation.Type.VALUE_RETURNED, chooseState(fsm, state), CoreMatchers.anything());
  }

  public static <SUT> Expectation<SUT> valid(FSM<SUT> fsm, FSMSpec<SUT> state, Object returnedValue) {
    return valid(fsm, state, CoreMatchers.is(returnedValue));
  }

  public static <SUT> Expectation<SUT> valid(FSM<SUT> fsm, FSMSpec<SUT> state, org.hamcrest.Matcher matcher) {
    return new Expectation<SUT>(Expectation.Type.VALUE_RETURNED, chooseState(fsm, state), matcher);
  }

  public static <SUT> FSM<SUT> createFSM(Class<? extends FSMSpec<SUT>> fsmSpecClass) {
    return createFSM(fsmSpecClass, 2);
  }

  public static <SUT> FSM<SUT> createFSM(Class<? extends FSMSpec<SUT>> fsmSpecClass, int historyLength) {
    return new SimpleFSM<SUT>(fsmSpecClass, historyLength);
  }

  private static <SUT> State<SUT> chooseState(FSM<SUT> fsm, StateChecker<SUT> stateChecker) {
    Checks.checknotnull(fsm);
    Checks.checknotnull(stateChecker);
    for (State<SUT> each : fsm.states()) {
      if (((SimpleFSM.SimpleFSMState) each).stateSpec == stateChecker)
        return each;
    }
    Checks.checkcond(false, "No state for '%s' was found.", stateChecker);
    return null;
  }

  public static String composeMainScenarioName(String fsmName) {
    return String.format("FSM:main:%s", fsmName);
  }

  public static String composeSetUpScenarioName(String fsmName) {
    return String.format("FSM:setUp:%s", fsmName);
  }

  /**
   * Returns a name of FSM which is referred to be the given factor name.
   * Returns {@code null} if factorName doesn't appear to be an FSM factor's name.
   *
   * @param factorName A factor name to be examined.
   */
  public static String getFSMNameFromScenarioFactorName(String factorName) {
    Matcher m;
    if ((m = fsmFactorPattern.matcher(factorName)).matches()) {
      return m.group(2);
    }
    return null;
  }

  public static <SUT> String toString(ScenarioSequence<SUT> scenarioSequence) {
    Checks.checknotnull(scenarioSequence);
    Object[] scenarios = new Object[scenarioSequence.size()];
    for (int i = 0; i < scenarios.length; i++) {
      scenarios[i] = scenarioSequence.get(i);
    }
    return String.format("ScenarioSequence:[%s]", Utils.join(",", scenarios));
  }

  public static void main(String[] args) {
    System.out.println(getFSMNameFromScenarioFactorName("FSM:main:helloFSM"));
    System.out.println(getFSMNameFromScenarioFactorName("FSM:setUp:helloFSM"));
    System.out.println(getFSMNameFromScenarioFactorName("FSM:dummy:helloFSM"));
  }

  public static class SimpleFSM<SUT> implements FSM<SUT> {
    private final int               historyLength;
    private       List<State<SUT>>  states;
    private       List<Action<SUT>> actions;
    private       State<SUT>        initialState;
    private final String name;

    public SimpleFSM(Class<? extends FSMSpec<SUT>> specClass, int historyLength) {
      Checks.checknotnull(specClass);
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
      Checks.checktest(initialState != null, "A state whose name is 'I' couldn't be found in '%s'", specClass.getCanonicalName());
      this.states = Collections.unmodifiableList(states);
      this.initialState = initialState;
      this.historyLength = historyLength;
      this.name = specClass.getSimpleName();
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

    private Action<SUT> createAction(final Method actionMethod, final Field paramsField) {
      final Object[][] paramFactors;
      if (paramsField == null) {
        paramFactors = new Object[][] { };
      } else {
        paramFactors = getParamsFactors(validateParamsField(paramsField));
      }
      //noinspection unchecked
      return (Action<SUT>) new SimpleFSM.MethodAction(actionMethod, paramFactors);
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
          Modifier.isPublic(m) && Modifier.isStatic(m) && Modifier.isFinal(m) && DOUBLE_ARRAYED_OBJECT_CLASS.isAssignableFrom(fsmField.getType()),
          "Field '%s' of '%s' must be public, static, final, and of Object[][].",
          ret.getName(), ret.getType().getCanonicalName()
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

    private class SimpleFSMState implements State<SUT> {
      private final FSMSpec<SUT>        stateSpec;
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
      public Object perform(SUT o, Args args) throws Throwable {
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
}
