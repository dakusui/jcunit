package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.fsm.spec.FSMSpec;
import org.hamcrest.CoreMatchers;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class FSMUtils {
  private FSMUtils() {
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
    public SimpleFSM(Class<? extends FSMSpec<SUT>> specClass) {
    }

    @Override
    public State<SUT> initialState() {
      return null;
    }

    @Override
    public List<State<SUT>> states() {
      return null;
    }

    @Override
    public List<Action<SUT>> actions() {
      return null;
    }

    private Action<SUT> createAction(final Method actionMethod, final Field paramsField) throws IllegalAccessException {
      return new Action<SUT>() {
        final String name = actionMethod.getName();
        final Object[][] params = (Object[][]) paramsField.get(null);

        @Override
        public Object perform(SUT o, Args args) throws Throwable {
          Object ret = null;
          try {
            Method m = chooseMethod(o.getClass(), this.name, args.size());
            try {
              ret =  m.invoke(o, args.values());
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
            Checks.rethrowpluginerror(e, "Non-public method testing isn't supported (%s#%s isn't public)");
          } catch (InvocationTargetException e) {
            throw e.getTargetException();
          }
          return ret;
        }

        private <SUT> Method chooseMethod(Class<SUT> klass, String name, int numArgs) {
          Method ret = null;
          for (Method each : klass.getMethods()) {
            if (each.getName().equals(name) && each.getParameterTypes().length == numArgs) {
              Checks.checkplugin(ret == null, "There are more than 1 method '%s/%d' in '%s'", name, numArgs, klass.getCanonicalName());
              ret = each;
            }
          }
          Checks.checkplugin(ret != null, "No method '%s/%d' is found in '%s'", name, numArgs, klass.getCanonicalName());
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

    private State<SUT> createState(final FSMSpec<SUT> stateSpec, final Method actionMethod) {
      return new State<SUT>() {
        @Override
        public boolean check(SUT sut) {
          return stateSpec.check(sut);
        }

        @Override
        public Expectation<SUT> expectation(Action<SUT> action, Args args) {
          Expectation<SUT> ret = null;
          try {
            Object o = actionMethod.invoke(stateSpec, args.values());
            Checks.checkplugin(o instanceof Expectation, "");
            ret = (Expectation<SUT>)o;
          } catch (IllegalAccessException e) {
            Checks.rethrowpluginerror(e, "");
          } catch (InvocationTargetException e) {
            Checks.rethrowpluginerror(e, "");
          }
          return ret;
        }
      };
    }
  }

  public abstract static class SimpleFSMState<SUT> implements State<SUT> {
    private final StateChecker<SUT> stateChecker;

    SimpleFSMState(StateChecker<SUT> stateChecker) {
      Checks.checknotnull(stateChecker);
      this.stateChecker = stateChecker;
    }

    @Override
    public boolean check(SUT sut) {
      return this.stateChecker.check(sut);
    }
  }
}
