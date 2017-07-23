package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.reflect.ReflectionUtils;
import com.github.dakusui.jcunit.core.utils.Checks;
import com.github.dakusui.jcunit.core.utils.StringUtils;
import com.github.dakusui.jcunit.core.utils.Utils;
import com.github.dakusui.jcunit.fsm.spec.ActionSpec;
import com.github.dakusui.jcunit.fsm.spec.FsmSpec;
import com.github.dakusui.jcunit.fsm.spec.ParametersSpec;
import com.github.dakusui.jcunit.fsm.spec.StateSpec;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

/**
 * An interface that models a finite state machine.
 *
 * @param <SUT> A software under test.
 */
public interface FiniteStateMachine<SUT> {
  State<SUT> initialState();

  List<State<SUT>> states();

  List<Action<SUT>> actions();

  static <SUT> FiniteStateMachine<SUT> create(String fsmName, Class<? extends FsmSpec<SUT>> specClass) {
    return new Impl<>(fsmName, specClass);
  }

  class Impl<SUT> implements FiniteStateMachine<SUT> {
    private List<State<SUT>>  states;
    private List<Action<SUT>> actions;
    private State<SUT>        initialState;

    public Impl(String fsmName, Class<? extends FsmSpec<SUT>> specClass) {
      Checks.checknotnull(fsmName);
      Checks.checknotnull(specClass);
      ////
      // Build 'actions'.
      Map<String, Method> actionMethods = Utils.toMap(getActionMethods(specClass), Impl::generateMethodId);
      Map<String, Field> paramsFields = Utils.toMap(getParamsFields(specClass), Field::getName);
      List<Action<SUT>> actions = new LinkedList<>();
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
      Map<String, Field> stateFields = Utils.toMap(getStateFields(specClass), Field::getName);
      List<State<SUT>> states = new LinkedList<>();
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

    private List<Field> getStateFields(Class<? extends FsmSpec<SUT>> specClass) {
      List<Field> ret = new LinkedList<>();
      for (Field each : ReflectionUtils.getFields(specClass)) {
        if (each.isAnnotationPresent(StateSpec.class) && !isAlreadyAddedIn(each, ret)) {
          ret.add(each);
        }
      }
      return ret;
    }

    private List<Field> getParamsFields(Class<? extends FsmSpec<SUT>> specClass) {
      List<Field> ret = new LinkedList<>();
      for (final Field each : ReflectionUtils.getFields(specClass)) {
        if (each.isAnnotationPresent(ParametersSpec.class) && !isAlreadyAddedIn(each, ret))
          ret.add(each);
      }
      return ret;
    }

    private boolean isAlreadyAddedIn(final Field each, List<Field> list) {
      return list.stream().anyMatch(in -> in.getName().equals(each.getName()) && each.getType().isAssignableFrom(in.getType()));
    }

    private List<Method> getActionMethods(Class<? extends FsmSpec<SUT>> specClass) {
      List<Method> ret = new LinkedList<>();
      for (Method each : ReflectionUtils.getMethods(specClass)) {
        if (each.isAnnotationPresent(ActionSpec.class)) {
          ret.add(each);
        }
      }
      return ret;
    }

    /**
     * {@code paramsField} can be numm if {@code actionMethod} doesn't have any parameter.
     */
    @SuppressWarnings("unchecked")
    private Action<SUT> createAction(final Method actionMethod, final Field paramsField) {
      final Parameters parameters;
      if (paramsField == null) {
        parameters = Parameters.EMPTY;
      } else {
        parameters = getParamsFactors(validateParamsField(paramsField));
      }
      return new Action.Base<>(actionMethod, parameters);
    }

    /**
     * {@code field} must be validated by {@code validateParamsField} in advance.
     *
     * @param field A field from which {@code args} values should be retrieved.
     */
    private Parameters getParamsFactors(Field field) {
      ////
      // The field should be static.
      Object ret = ReflectionUtils.getFieldValue(null, Checks.checknotnull(field));
      Checks.checktest(
          ret instanceof Parameters,
          "The field '%s' in %s must be typed %s",
          field.getName(),
          field.getDeclaringClass().getCanonicalName(),
          Parameters.class.getSimpleName()
      );
      Checks.checktest(
          (((Parameters) ret).values()).size() > 0,
          "The field '%s' of '%s' must be assigned Object[][] value whose length is larget than 0.",
          field.getName(),
          field.getType().getCanonicalName());
      ////
      // Casting to Object[][] is safe because validateParamsField checks it.
      return ((Parameters) ret);
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

    private State<SUT> createState(String fsmName, FiniteStateMachine<SUT> fsm, final Field stateSpecField, final Map<String, Method> actionMethods) {
      final FsmSpec<SUT> stateSpec = getStateSpecValue(validateStateSpecField(stateSpecField));
      return new State.Base<>(fsmName, fsm, stateSpec, actionMethods, stateSpecField);
    }

    @SuppressWarnings("unchecked")
    private FsmSpec<SUT> getStateSpecValue(Field field) {
      Object ret = ReflectionUtils.getFieldValue(null, field);
      Checks.checktest(ret != null, "The field '%s' of '%s' must be assigned a non-null value.", field.getName(), field.getType().getCanonicalName());
      ////
      // Casting to (FsmSpec<SUT>) is safe because validateParamsField checks it already.
      return FsmSpec.class.<SUT>cast(ret);
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
          StringUtils.join(
              ",",
              asList(m.getParameterTypes())
                  .subList(1, m.getParameterTypes().length).stream()
                  .map(Class::getCanonicalName)
                  .toArray());
    }

  }
}
