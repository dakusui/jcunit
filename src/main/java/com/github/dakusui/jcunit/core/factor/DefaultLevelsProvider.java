package com.github.dakusui.jcunit.core.factor;

import com.github.dakusui.jcunit.core.FactorField;
import com.github.dakusui.jcunit.core.Utils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class DefaultLevelsProvider extends LevelsProviderBase<Object> {
  private final Method m;
  private       Object values;
  private       int    size;

  DefaultLevelsProvider(Method m) {
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
      Class<?> compType = this.values.getClass().getComponentType();
      ////
      // If the type is String or Enum, and includeNull is set to true,
      // a null value will be included in the values.
      if (String.class.equals(compType) || Enum.class.isAssignableFrom(compType)) {
        if (annotation.includeNull()) {
          Object work = Array.newInstance(compType, size + 1);
          System.arraycopy(this.values, 0, work, 0, this.size);
          Array.set(work, this.size, null);
          this.values = work;
          this.size++;
        }
      }
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
