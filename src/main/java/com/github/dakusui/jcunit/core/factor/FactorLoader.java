package com.github.dakusui.jcunit.core.factor;

import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.exceptions.JCUnitException;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class FactorLoader {
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

  private final Field field;
  private ValidationResult validationResult = null;

  public FactorLoader(Field f) {
    this.field = f;
  }

  private static Method getLevelsMethod(String methodName) {
    try {
      return FactorField.class.getMethod(methodName);
    } catch (NoSuchMethodException e) {
      Utils.rethrow(e, String.format(
          "Something went wrong. A method '%s' should be found in @FactorField",
          methodName));
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

  public ValidationResult validate() {
    return this.validate(this.field);
  }

  ValidationResult validate(Field f) {
    Utils.checknotnull(f);
    FactorField ann = f.getAnnotation(FactorField.class);
    List<String> errors = new LinkedList<String>();
    LevelsFactory<?> levelsFactory = InvalidLevelsFactory.INSTANCE;
    Method[] overridingLevelsMethods = getOverridingLevelsMethods(ann);
    if (overridingLevelsMethods.length > 1) {
      errors.add(String.format(
          "You can give at most one explicit value to FactorField annotation, but %d were given. [%s]",
          overridingLevelsMethods.length,
          Utils.join(",", new Utils.Formatter<Method>() {
            @Override
            public String format(Method m) {
              return m.getName();
            }
          })
      ));
    } else if (overridingLevelsMethods.length == 1) {
      Method method = overridingLevelsMethods[0];
      Class<?> levelType = Void.class; // In order to make this method explode in case explicit assignment isn't made, use Void.class.
      if ("levelsFactory".equals(method.getName())) {
        ////
        // 'levelsFactory' is the overridden method.
        Class<? extends LevelsFactory<?>> factoryClass = InvalidLevelsFactory.class;
        try {
          factoryClass = (Class<? extends LevelsFactory<?>>) method.invoke(ann);
          Utils.checknotnull(factoryClass);
          ////
          // We can safely cast it to LevelsFactory since 'levelsFactory' can only
          // return LevelsFactory and we've even already checked it.
          levelsFactory = (LevelsFactory<?>) factoryClass.newInstance();
        } catch (InstantiationException e) {
          errors.add(String.format(
              "A factory '%s' set to field '%s' couldn't be initialized. The constructor with no parameter of it must be implemented, be public, and successfully instantiate it. (failed):%s",
              factoryClass.getCanonicalName(), f, e.getMessage()));
        } catch (IllegalAccessException e) {
          errors.add(String.format(
              "A factory '%s' set to field '%s' couldn't be initialized. The constructor with no parameter of it must be implemented, be public, and successfully instantiate it. (not public):%s",
              factoryClass.getCanonicalName(), f, e.getMessage()));
        } catch (InvocationTargetException e) {
          errors.add(String.format(
              "A factory '%s' set to field '%s' couldn't be initialized. The constructor with no parameter of it must be implemented, be public, and successfully instantiate it. (failed):%s",
              factoryClass.getCanonicalName(), f, e.getMessage()));
        }
        try {
          ////
          // Get 'get(int)' method to check if it returns a value of appropriate type later.
          Method getMethod = levelsFactory.getClass().getMethod("get",
              Integer.TYPE);
          levelType = getMethod.getReturnType();
        } catch (NoSuchMethodException e) {
          ////
          // This can only happen due to JCUnit side's bugs, so simply an exception will be thrown.
          Utils.rethrow(e, String.format(
              "Something went wrong. Method 'get(int)' wasn't found in '%s':(%s)",
              levelsFactory.getClass(), e.getMessage()));
        }
      } else {
        ////
        // Other than 'levelsFactory' method (like 'intLevels') is overridden.
        levelsFactory = new DefaultLevelsFactory(method);
        levelType = method.getReturnType().getComponentType();
      }
      Utils.checknotnull(levelType);
    } else {
      Class<?> fieldType = f.getType();
      if (Enum.class.isAssignableFrom(fieldType)) {
        fieldType = Enum.class;
      }
      if (methodNameMappings.containsKey(fieldType)) {
        levelsFactory = new DefaultLevelsFactory(
            methodNameMappings.get(fieldType));
      } else {
        ////
        // In this case (Non-primitive, non-string typed fields),
        // levelsFactory must be provided, but not found (because no overriding
        // method was found).
        errors.add(String.format(
            "For the field '%s', 'levelsFactory' needs to be provided since there is no pre-defined xyzLevels method for it.",
            f));
      }
    }
    ValidationResult ret;
    if (errors.isEmpty()) {
      levelsFactory.setAnnotation(ann);
      levelsFactory.setTargetField(field);
      levelsFactory.init(Utils.processParams(ann.factoryParameters()));
      ret = new ValidationResult(true, levelsFactory, null);
    } else {
      ret = new ValidationResult(false, null,
          Utils.join("; ", errors.toArray()));
    }
    return ret;
  }

  private Method[] getOverridingLevelsMethods(
      FactorField factorFieldAnnotation) {
    Method[] methods = FactorField.class.getDeclaredMethods();
    List<Method> work = new ArrayList<Method>(methods.length);
    for (Method m : methods) {
      if (m.getName().endsWith("Levels") || "levelsFactory"
          .equals(m.getName())) {
        try {
          if (!areSame(m.getDefaultValue(), m.invoke(factorFieldAnnotation))) {
            work.add(m);
          }
        } catch (IllegalAccessException e) {
          Utils.rethrow(e, "Something went wrong:" + e.getMessage());
        } catch (InvocationTargetException e) {
          Utils.rethrow(e, "Something went wrong:" + e.getMessage());
        }
      }
    }
    return work.toArray(new Method[work.size()]);
  }

  public Factor getFactor() {
    if (this.validationResult == null) {
      this.validationResult = this.validate(this.field);
    }
    if (!this.validationResult.isValid()) {
      throw new FactorFieldValidationException(this.validationResult);
    }
    LevelsFactory<?> levelsFactory = this.validationResult
        .getLevelsFactory();

    Factor.Builder factorBuilder = new Factor.Builder();
    factorBuilder.setName(this.field.getName());
    int numLevels = levelsFactory.size();
    for (int i = 0; i < numLevels; i++) {
      factorBuilder.addLevel(levelsFactory.get(i));
    }
    return factorBuilder.build();
  }

  public static class ValidationResult {
    private final boolean          valid;
    private final String           errMessage;
    private final LevelsFactory<?> levelsFactory;

    public ValidationResult(boolean valid,
        LevelsFactory<?> levelsFactory, String errorMessage) {
      if (valid) {
        Utils.checknotnull(levelsFactory);
      } else {
        Utils.checknotnull(errorMessage);
      }
      this.valid = valid;
      this.levelsFactory = levelsFactory;
      this.errMessage = errorMessage;
    }

    public boolean isValid() {
      return this.valid;
    }

    /**
     * This method returns null, if this object represents a valid result.
     */
    public String getErrorMessage() {
      return this.errMessage;
    }

    public LevelsFactory<?> getLevelsFactory() {
      return levelsFactory;
    }

    public void check() {
      if (this.valid) {
        throw new FactorFieldValidationException(this);
      }
    }
  }

  public static class FactorFieldValidationException
      extends JCUnitException {
    private final ValidationResult validationResult;

    /**
     * Creates an object of this class.
     */
    public FactorFieldValidationException(
        ValidationResult result) {
      super(Utils.checknotnull(result).getErrorMessage(), null);
      this.validationResult = result;
    }

    public ValidationResult getValidationResult() {
      return this.validationResult;
    }
  }
}
