package com.github.dakusui.jcunit.core.factor;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.FactorField;
import com.github.dakusui.jcunit.core.Utils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultLevelsProvider extends LevelsProviderBase<Object> {
    private static final Map<Class<?>, Method> methodNameMappings;

    static {
        methodNameMappings = new HashMap<Class<?>, Method>();
        methodNameMappings.put(Boolean.TYPE, getLevelsMethod("booleanLevels"));
        methodNameMappings.put(Boolean.class, getLevelsMethod("booleanLevels"));
        methodNameMappings.put(Byte.TYPE, getLevelsMethod("byteLevels"));
        methodNameMappings.put(Byte.class, getLevelsMethod("byteLevels"));
        methodNameMappings.put(Character.TYPE, getLevelsMethod("charLevels"));
        methodNameMappings.put(Character.class, getLevelsMethod("charLevels"));
        methodNameMappings.put(Short.TYPE, getLevelsMethod("shortLevels"));
        methodNameMappings.put(Short.class, getLevelsMethod("shortLevels"));
        methodNameMappings.put(Integer.TYPE, getLevelsMethod("intLevels"));
        methodNameMappings.put(Integer.class, getLevelsMethod("intLevels"));
        methodNameMappings.put(Long.TYPE, getLevelsMethod("longLevels"));
        methodNameMappings.put(Long.class, getLevelsMethod("longLevels"));
        methodNameMappings.put(Float.TYPE, getLevelsMethod("floatLevels"));
        methodNameMappings.put(Float.class, getLevelsMethod("floatLevels"));
        methodNameMappings.put(Double.TYPE, getLevelsMethod("doubleLevels"));
        methodNameMappings.put(Double.class, getLevelsMethod("doubleLevels"));
        methodNameMappings.put(String.class, getLevelsMethod("stringLevels"));
        methodNameMappings.put(Enum.class, getLevelsMethod("enumLevels"));
    }


    private Object values;
    private int size;

    public DefaultLevelsProvider() {
    }

    @Override
    protected void init(Field targetField,
                        FactorField annotation,
                        Object[] parameters) {
        try {
            Method m = chooseMethodForThisField(targetField, annotation, errors);
            if (m == null) {
                Checks.checkcond(!errors.isEmpty());
                return;
            }
            Object values = m.invoke(annotation);
            Checks.checknotnull(values);
            if (values.getClass().isArray()) {
                this.values = values;
            } else if (Enum.class.isAssignableFrom((Class<?>) values)) {
                this.values = targetField.getType().getMethod("values").invoke(null);
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
                        String.format("Incompatible method '%s' is specified for field '%s' (%s)",
                                m.getName(), targetField.getName(), targetField.getType().getCanonicalName()
                        ));
            }
            return;
        } catch (IllegalAccessException e) {
            Checks.rethrow(e);
        } catch (InvocationTargetException e) {
            Checks.rethrow(e);
        } catch (NoSuchMethodException e) {
            Checks.rethrow(e);
        }
        throw new RuntimeException(); // Will not be thrown actually. But necessary to make fields of this class final.
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
            if (!methodNameMappings.containsKey(fieldType)) {
                ////
                // In this case (Non-primitive, non-string typed fields),
                // levelsProvider must be provided, but not found (because no overriding
                // method was found).
                errors.add(String.format(
                        "For the field '%s', 'levelsProvider' needs to be provided since there is no pre-defined xyzLevels method for it.",
                        targetField));
            }
            return methodNameMappings.get(fieldType);
        } else if (overridingLevelsMethods.length > 1) {
            errors.add(String.format(
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
                try {
                    if (!areSame(m.getDefaultValue(), m.invoke(factorFieldAnnotation))) {
                        work.add(m);
                    }
                } catch (IllegalAccessException e) {
                    Checks.rethrow(e, "Something went wrong:'%s'", e.getMessage());
                } catch (InvocationTargetException e) {
                    Checks.rethrow(e, "Something went wrong:%s", e.getMessage());
                }
            }
        }
        return work.toArray(new Method[work.size()]);
    }


    private static Method getLevelsMethod(String methodName) {
        try {
            return FactorField.class.getMethod(methodName);
        } catch (NoSuchMethodException e) {
            Checks.rethrow(e,
                "Something went wrong. A method '%s' should be found in @FactorField",
                methodName);
        }
        throw new RuntimeException(); // Will never be executed.
    }

    private static boolean areSame(Object arr1, Object arr2) {
        if (arr1 == null || arr2 == null) {
            return arr1 == arr2;
        }
        if (!arr1.getClass().isArray()) {
            return eq(arr1, arr2);
        }
        if (arr2.getClass().isArray() && Array.getLength(arr1) == Array
                .getLength(arr2)) {
            int len = Array.getLength(arr1);
            for (int i = 0; i < len; i++) {
                if (!eq(Array.get(arr1, i), Array.get(arr2, i))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private static boolean eq(Object a, Object b) {
        if (a == null) {
            return b == null;
        }
        return a.equals(b);
    }
}
