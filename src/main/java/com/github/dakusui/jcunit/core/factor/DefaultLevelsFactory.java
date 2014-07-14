package com.github.dakusui.jcunit.core.factor;

import com.github.dakusui.jcunit.core.Utils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class DefaultLevelsFactory extends LevelsFactoryBase<Object> {
  private final Method m;
  private       Object values;
  private       int    size;

  DefaultLevelsFactory(Method m) {
    this.m = m;
  }

  @Override protected void init(Field targetField,
      FactorField annotation, Object[] parameters) {
    try {
      Object values = m.invoke(annotation);
      Utils.checknotnull(values);
      if (values.getClass().isArray()) {
        this.values = values;
      } else if (Enum.class.isAssignableFrom((Class<?>)values)) {
        this.values = targetField.getType().getMethod("values").invoke(null);
      } else {
        throw new RuntimeException();
      }
      this.size = Array.getLength(this.values);
      return;
    } catch (IllegalAccessException e) {
      Utils.rethrow(e);
    } catch (InvocationTargetException e) {
      Utils.rethrow(e);
    } catch (NoSuchMethodException e) {
      Utils.rethrow(e);
    }
    throw new RuntimeException(); // Will not be thrown actually. But necessary to make fields final.
  }

  @Override public int size() {
    return this.size;
  }

  @Override public Object get(int index) {
    return Array.get(this.values, index);
  }
}
