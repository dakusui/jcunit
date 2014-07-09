package com.github.dakusui.jcunit.core.factor;

import com.github.dakusui.jcunit.core.Utils;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class DefaultLevelsFactory implements LevelsFactory<Object> {
  private final Object values;
  private final int size;

  DefaultLevelsFactory(FactorField ann, Method m) {
    try {
      this.values = m.invoke(ann);
      this.size = Array.getLength(this.values);
    } catch (IllegalAccessException e) {
      Utils.rethrow(e);
      throw new RuntimeException(); // Will not be thrown actually. But necessary to make fields final.
    } catch (InvocationTargetException e) {
      Utils.rethrow(e);
      throw new RuntimeException(); // Will not be thrown actually. But necessary to make fields final.
    }
  }

  @Override public void init(String[] parameters) {
  }

  @Override public int size() {
    return this.size;
  }

  @Override public Object get(int index) {
    return Array.get(this.values, index);
  }
}
