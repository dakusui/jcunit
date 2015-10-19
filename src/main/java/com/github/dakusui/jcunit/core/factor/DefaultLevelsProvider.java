package com.github.dakusui.jcunit.core.factor;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.FactorField;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.reflect.ReflectionUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultLevelsProvider extends LevelsProviderBase<Object> {
  private Object values;
  private int    size;

  public DefaultLevelsProvider() {
  }

  @Override
  protected void init(Field targetField,
      FactorField annotation,
      Object[] parameters) {
    Method m = chooseMethodForThisField(targetField, annotation, errors);
    if (m == null) {
      Checks.checkcond(!errors.isEmpty());
      return;
    }
    Object values = ReflectionUtils.invokeMethod(annotation, m);
    Checks.checknotnull(values);
    // values is already checked not null.
    //noinspection ConstantConditions
    if (values.getClass().isArray()) {
      this.values = values;
    } else if (Enum.class.isAssignableFrom((Class<?>) values)) {
      this.values = ReflectionUtils.invokeMethod(
          null,
          ReflectionUtils.getMethod(
              targetField.getType(),
              "values"));
    } else {
      throw new RuntimeException();
    }
    this.size = Array.getLength(this.values);
    Class<?> compType = this.values.getClass().getComponentType();
    ////
    // If the type is String or Enum, and includeNull is set to true,
    // a null value will be included in the values.
    if (String.class.equals(compType) || Enum.class.isAssignableFrom(compType)) {
      if (annotation.includeNull()) {
        Object work = Array.newInstance(compType, size + 1);
        //noinspection SuspiciousSystemArraycopy
        System.arraycopy(this.values, 0, work, 0, this.size);
        Array.set(work, this.size, null);
        this.values = work;
        this.size++;
      }
    }
    if (!targetField.getType().isAssignableFrom(compType)) {
      errors.add(
          Utils.format("Incompatible method '%s' is specified for field '%s' (%s)",
              m.getName(), targetField.getName(), targetField.getType().getCanonicalName()
          ));
    }
  }

  @Override
  public int size() {
    return this.size;
  }

  @Override
  public Object get(int index) {
    return Array.get(this.values, index);
  }

  private Method chooseMethodForThisField(Field targetField, FactorField ann, List<String> errors) {
    Method[] overridingLevelsMethods = getOverridingLevelsMethods(ann);
    Class<?> fieldType = targetField.getType();
    if (overridingLevelsMethods.length == 0) {
      if (Enum.class.isAssignableFrom(fieldType)) {
        fieldType = Enum.class;
      }
      if (!FactorField.Utils.INSTANCE.hasMethodFor(fieldType)) {
        ////
        // In this case (Non-primitive, non-string typed fields),
        // levelsProvider must be provided, but not found (because no overriding
        // method was found).
        errors.add(Utils.format(
            "For the field '%s', 'levelsProvider' needs to be provided since there is no pre-defined xyzLevels method for it.",
            targetField));
      }
      return FactorField.Utils.INSTANCE.getMethodFor(fieldType);
    } else if (overridingLevelsMethods.length > 1) {
      errors.add(Utils.format(
          "You can give at most one explicit value to FactorField annotation, but %d were given. [%s]",
          overridingLevelsMethods.length,
          Utils.join(",", new Utils.Formatter<Method>() {
            @Override
            public String format(Method m) {
              return m.getName();
            }
          }, overridingLevelsMethods)
      ));
      return null;
    }
    return overridingLevelsMethods[0];
  }

  private Method[] getOverridingLevelsMethods(
      FactorField factorFieldAnnotation) {
    Method[] methods = FactorField.class.getDeclaredMethods();
    List<Method> work = new ArrayList<Method>(methods.length);
    for (Method m : methods) {
      if (m.getName().endsWith("Levels") || "levelsProvider"
          .equals(m.getName())) {
        if (!Utils.deepEq(
            m.getDefaultValue(),
            ReflectionUtils.invokeMethod(factorFieldAnnotation, m))) {
          work.add(m);
        }
      }
    }
    return work.toArray(new Method[work.size()]);
  }

}
