package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.fsm.spec.FSMSpec;
import com.github.dakusui.jcunit.fsm.spec.ParametersSpec;
import com.github.dakusui.jcunit.fsm.spec.StateSpec;
import org.hamcrest.CoreMatchers;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * A utility class for FSM (finite state machine) support of JCUnit.
 */
public class FSMUtils {
  private static final Class<? extends Object[][]> doubleArrayedObjectClass = new Object[0][0].getClass();

  private FSMUtils() {
  }

  public static <SUT> void performScenarioSequence(ScenarioSequence<SUT> scenarioSeq, SUT sut) throws Throwable {
    Checks.checknotnull(scenarioSeq);
    for (int i = 0; i < scenarioSeq.size(); i++) {
      Scenario each = scenarioSeq.get(i);

      boolean checkState = false;
      try {
        Object r = each.perform(sut);
        if (each.then().returnedValue == null)
          fail(String.format("'%s' was expected to be thrown.", each.then().thrownException));
        each.then().returnedValue.matches(r);
        checkState = true;
      } catch (Throwable t) {
        if (each.then().thrownException == null)
          throw t;
        each.then().thrownException.matches(t);
        checkState = true;
      } finally {
        if (checkState) {
          assertTrue(
              String.format("Expected status of the SUT is '%s' but it was not satisfied.", each.then().state),
              each.then().state.check(sut)
          );
        }
      }
    }
  }

  public static <SUT> Expectation<SUT> invalid() {
    return invalid(IllegalStateException.class);
  }

  public static <SUT> Expectation<SUT> invalid(Class<? extends Throwable> klass) {
    return new Expectation(State.VOID, CoreMatchers.instanceOf(klass));
  }

  public static <SUT> Expectation<SUT> invalid(FSM<SUT> fsm, FSMSpec<SUT> state, Class<? extends Throwable> klass) {
    Checks.checknotnull(fsm);
    Checks.checknotnull(state);
    return new Expectation(chooseState(fsm, state), CoreMatchers.instanceOf(klass));
  }

  public static <SUT> Expectation<SUT> valid(FSM<SUT> fsm, FSMSpec<SUT> state) {
    return new Expectation<SUT>(chooseState(fsm, state), CoreMatchers.anything());
  }

  public static <SUT> Expectation<SUT> valid(FSM<SUT> fsm, FSMSpec<SUT> state, Object returnedValue) {
    return new Expectation<SUT>(chooseState(fsm, state), CoreMatchers.is(returnedValue));
  }

  public static <SUT> FSM<SUT> createFSM(Class<? extends FSMSpec<SUT>> fsmSpecClass) {
    return new SimpleFSM<SUT>(fsmSpecClass);
  }

  private static <SUT> State<SUT> chooseState(FSM<SUT> fsm, StateChecker<SUT> stateChecker) {
    return null;
  }

  public static class SimpleFSM<SUT> implements FSM<SUT> {
    private List<State<SUT>>  states;
    private List<Action<SUT>> actions;
    private State<SUT>        initialState;

    public SimpleFSM(Class<? extends FSMSpec<SUT>> specClass) {
      Checks.checknotnull(specClass);
      ////
      // Build 'actions'.
      Map<String, Method> actionMethods = Utils.toMap(getActionMethods(specClass), new Utils.Form<Method, String>() {
        @Override
        public String apply(Method in) {
          return in.toString();
        }
      });
      Map<String, Field> paramsFields = Utils.toMap(getParamsFields(specClass), new Utils.Form<Field, String>() {
        @Override
        public String apply(Field in) {
          return in.toString();
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
          return in.toString();
        }
      });
      List<State<SUT>> states = new LinkedList<State<SUT>>();
      State<SUT> initialState = null;
      for (Map.Entry<String, Field> each : stateFields.entrySet()) {
        states.add(createState(each.getValue(), actionMethods));
        if ("I".equals(each.getKey())) initialState = states.get(states.size() - 1);
      }
      Checks.checktest(initialState != null, "A state whose name is 'I' couldn't be found in '%s'", specClass.getCanonicalName());
      this.states = Collections.unmodifiableList(states);
      this.initialState = initialState;
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

    private List<Field> getStateFields(Class<? extends FSMSpec<SUT>> specClass) {
      List<Field> ret = new LinkedList<Field>();
      for (Field each : specClass.getFields()) {
        if (each.isAnnotationPresent(StateSpec.class) && !isAlreadyAddedIn(each, ret)) {
          ret.add(each);
        }
      }
      return ret;
    }

    private List< Field> getParamsFields(Class<? extends FSMSpec<SUT>> specClass) {
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
        if (each.isAnnotationPresent(StateSpec.class)) {
          ret.add(each);
        }
      }
      return ret;
    }

    private Action<SUT> createAction(final Method actionMethod, final Field paramsField) {
      final String name = actionMethod.getName();
      final Object[][] params = getParamsValue(validateParamsField(paramsField));
      return new Action<SUT>() {
        @Override
        public String toString() {
          return actionMethod.getName();
        }

        @Override
        public Object perform(SUT o, Args args) throws Throwable {
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
            // compatibility conflict in future, so I'm not supporting it for now.
            Checks.rethrowtesterror(e, "Non-public method testing isn't supported (%s#%s isn't public)");
          } catch (InvocationTargetException e) {
            throw e.getTargetException();
          }
          return ret;
        }

        private <SUT> Method chooseMethod(Class<SUT> klass, String name, int numArgs) {
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

        @Override
        public Object[] param(int i) {
          return params[i];
        }

        @Override
        public int numParams() {
          return params.length;
        }
      };
    }

    /**
     * {@code field} must be validated by {@code validateParamsField} in advance.
     *
     * @param field A field from which {@code params} values should be retrieved.
     */
    private Object[][] getParamsValue(Field field) {
      try {
        Object ret = field.get(null);
        Checks.checktest(ret != null, "The field '%s' of '%s' must be assigned a non-null value.", field.getName(), field.getType().getCanonicalName());
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
          Modifier.isPublic(m) && Modifier.isStatic(m) && Modifier.isFinal(m) && doubleArrayedObjectClass.getClass().isAssignableFrom(fsmField.getType()),
          "Field '%s' of '%s' must be public, static, final, and of Object[][].",
          ret.getName(), ret.getType().getCanonicalName()
      );
      return ret;
    }

    private State<SUT> createState(final Field stateSpecField, final Map<String, Method> actionMethods) {
      final FSMSpec<SUT> stateSpec = getStateSpecValue(validateStateSpecField(stateSpecField));
      return new State<SUT>() {
        @Override
        public boolean check(SUT sut) {
          return stateSpec.check(sut);
        }

        @Override
        public Expectation<SUT> expectation(Action<SUT> action, Args args) {
          Expectation<SUT> ret = null;
          try {
            Method m = Checks.checknotnull(actionMethods.get(action.toString()), "Unknown action '%s' was given.", action);
            Checks.checktest(Expectation.class.isAssignableFrom(m.getReturnType()), "");
            ret = (Expectation<SUT>) m.invoke(stateSpec, args.values());
          } catch (IllegalAccessException e) {
            Checks.rethrowtesterror(e, "");
          } catch (InvocationTargetException e) {
            Checks.rethrowtesterror(e, "");
          }
          return ret;
        }
      };
    }

    private FSMSpec<SUT> getStateSpecValue(Field field) {
      try {
        Object ret = field.get(null);
        Checks.checktest(ret != null, "The field '%s' of '%s' must be assigned a non-null value.", field.getName(), field.getType().getCanonicalName());
        ////
        // Casting to Object[][] is safe because validateParamsField checks it.
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
  }
}
