package com.github.dakusui.jcunit.core.factor;

import com.github.dakusui.jcunit.core.Utils;

import java.lang.reflect.*;

public class MethodLevelsFactory extends LevelsFactoryBase<Object> {
  private Object levels;
  private int    size;

  @Override protected void init(Field targetField,
      FactorField annotation, Object[] parameters) {
    Method levelsMethod = getFactorLevelsMethod(targetField.getDeclaringClass(),
        targetField);
    ////
    // It is guaranteed that levelsMethod is static by validation in 'getFactorLevelsMethod'.
    this.levels = Utils.invokeMethod(null, levelsMethod);
    this.size = Array.getLength(this.levels);
  }

  @Override public int size() {
    return this.size;
  }

  @Override public Object get(int index) {
    return Array.get(levels, index);
  }

  static Method getFactorLevelsMethod(Class<?> testClass, Field inField) {
    Method ret = null;
    try {
      try {
        ret = testClass.getMethod(inField.getName());
      } catch (NoSuchMethodException e) {
        ret = testClass.getDeclaredMethod(inField.getName());
      }
    } catch (SecurityException e) {
      String msg = String.format(
          "JCUnit cannot be run in this environment. (%s:%s)", e.getClass()
              .getName(), e.getMessage()
      );
      Utils.rethrow(e, msg);
    } catch (NoSuchMethodException e) {
      String msg = String
          .format(
              "Method to generate a domain for '%s' isn't defined in class '%s' or not visible.",
              inField, testClass);
      Utils.rethrow(e, msg);
    }
    if (!validateDomainMethod(inField, ret)) {
      String msg = String.format(
          "Domain method '%s' isn't compatible with field '%s'", ret, inField);
      throw new IllegalArgumentException(msg, null);
    }
    return ret;
  }

  private static boolean checkIfStatic(Method domainMethod) {
    return Modifier.isStatic(domainMethod.getModifiers());
  }

  private static boolean checkIfTypeCompatible(Field inField,
      Method domainMethod) {
    return domainMethod.getReturnType().isArray()
        && domainMethod.getReturnType().getComponentType() == inField.getType();
  }

  private static boolean checkIfAnyParameterExists(Method domainMethod) {
    return domainMethod.getParameterTypes().length != 0;
  }

  private static boolean checkIfReturnTypeIsArray(Method domainMethod) {
    return domainMethod.getReturnType().isArray();
  }

  private static boolean validateDomainMethod(Field inField,
      Method domainMethod) {
    boolean ret;
    ret = checkIfStatic(domainMethod);
    ret &= !checkIfAnyParameterExists(domainMethod);
    ret &= checkIfReturnTypeIsArray(domainMethod);
    ret &= checkIfTypeCompatible(inField, domainMethod);
    return ret;
  }
}
