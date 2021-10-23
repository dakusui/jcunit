package com.github.dakusui.jcunit8.models.scenario;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public interface DriverMethodInvoker {
  <T> T invoke(Object... args);

  class Impl implements DriverMethodInvoker {
    final         Method method;
    private final Object driverObject;

    public Impl(Object driverObject, Method method) {
      this.method = method;
      this.driverObject = driverObject;
    }

    @SuppressWarnings("unchecked")
    public <T> T invoke(Object... args) {
      try {
        return (T) this.method.invoke(this.driverObject, args);
      } catch (IllegalAccessException | InvocationTargetException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
