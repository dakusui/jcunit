package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.fsm.spec.ActionSpec;
import com.github.dakusui.jcunit.fsm.spec.FSMSpec;
import com.github.dakusui.jcunit.fsm.spec.ParametersSpec;
import com.github.dakusui.jcunit.fsm.spec.StateSpec;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * An interface that models a finite state machine.
 *
 * @param <SUT> A software under test.
 */
public interface FSM<SUT> {
  State<SUT> initialState();

  List<State<SUT>> states();

  List<Action<SUT>> actions();

  int historyLength();

  class Base<SUT> implements FSM<SUT> {
    private final int               historyLength;
    private       List<State<SUT>>  states;
    private       List<Action<SUT>> actions;
    private       State<SUT>        initialState;

    public Base(String fsmName, Class<? extends FSMSpec<SUT>> specClass, int historyLength) {
      Checks.checknotnull(fsmName);
      Checks.checknotnull(specClass);
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
              "A parameter field '%s' referred to by '%s' is not found in '%s'", paramsFieldName, each.getKey(), specClass
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
        states.add(createState(fsmName, this, each.getValue(), actionMethods));
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
      return (Action<SUT>) new Action.Base(actionMethod, parameters);
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

    private State<SUT> createState(String fsmName, FSM<SUT> fsm, final Field stateSpecField, final Map<String, Method> actionMethods) {
      final FSMSpec<SUT> stateSpec = getStateSpecValue(validateStateSpecField(stateSpecField));
      return new State.Base<SUT>(fsmName, fsm, stateSpec, actionMethods, stateSpecField);
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
        // * Bug: when spec isn't marked public, this can happen.
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

  }
}
