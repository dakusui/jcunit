package com.github.dakusui.jcunit.extras.examples;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MethodFinder {
  private Class<?> clazz;
  private String   methodName;
  private Class<?>[] signature = new Class<?>[0];

  public MethodFinder setClass(Class<?> clazz) {
    if (clazz == null) {
      throw new NullPointerException();
    }
    this.clazz = clazz;
    return this;
  }

  public MethodFinder setSignature(String sig) throws ClassNotFoundException {
    if (sig == null) {
      throw new NullPointerException();
    }
    this.signature = parseSignature(sig);
    return this;
  }

  public MethodFinder setSignature(Class<?>... sig) {
    if (sig == null) {
      throw new NullPointerException();
    }
    this.signature = sig;
    return this;
  }

  private Class<?>[] parseSignature(String sig) throws ClassNotFoundException {
    List<Class<?>> paramTypeList = new ArrayList<Class<?>>();
    Reader r = new StringReader(sig);
    Class<?> cur;
    while ((cur = parseSignature(0, r)) != null) {
      paramTypeList.add(cur);
    }
    return paramTypeList.toArray(new Class[0]);
  }

  private Class<?> parseSignature(int arrayLevel, Reader r)
      throws ClassNotFoundException {
    int c = 0;
    try {
      c = r.read();
      if (c < 0) {
        return null;
      }
      if (c == 'Z') {
        return getClass(arrayLevel, Boolean.TYPE);
      }
      if (c == 'B') {
        return getClass(arrayLevel, Byte.TYPE);
      }
      if (c == 'C') {
        return getClass(arrayLevel, Character.TYPE);
      }
      if (c == 'S') {
        return getClass(arrayLevel, Short.TYPE);
      }
      if (c == 'I') {
        return getClass(arrayLevel, Integer.TYPE);
      }
      if (c == 'J') {
        return getClass(arrayLevel, Long.TYPE);
      }
      if (c == 'F') {
        return getClass(arrayLevel, Float.TYPE);
      }
      if (c == 'D') {
        return getClass(arrayLevel, Double.TYPE);
      }
      if (c == 'L') {
        return getClass(arrayLevel, readClassNameAndLoadTheClass(r));
      }
      if (c == '[') {
        return parseSignature(arrayLevel + 1, r);
      }
    } catch (IOException e) {
      // since the underlying data source is a string,
      // IO exception will never happen.
    }
    throw new IllegalArgumentException(String.format(
        "'%s': non-recognizable character.", c));
  }

  private Class<?> getClass(int arrayLevel, Class<?> type) {
    if (arrayLevel == 0) {
      return type;
    }
    int[] dimensions = new int[arrayLevel];
    // By Java language it is guaranteed that elements of int array
    // are all 0 after initialization.
    return Array.newInstance(type, dimensions).getClass();
  }

  private Class<?> readClassNameAndLoadTheClass(Reader r)
      throws ClassNotFoundException {
    StringBuilder builder = new StringBuilder(20);
    int c;
    String className = null;
    try {
      while ((c = r.read()) != -1) {
        if (c == ';') {
          return Class.forName(className = builder.toString());
        }
        builder.append((char) c);
      }
    } catch (ClassNotFoundException e) {
      String msg = String.format("Class '%s' isn't a valid class name.",
          className);
      throw new IllegalArgumentException(msg, e);
    } catch (IOException e) {
      // Since the underlying data source is a string,
      // IOException will never be thrown.
    }
    String msg = String.format("Unterminated class name is found (%s)",
        builder.toString());
    throw new IllegalArgumentException(msg);
  }

  public MethodFinder setName(String name) {
    if (name == null) {
      throw new NullPointerException();
    }
    this.methodName = name;
    return this;
  }

  public Method find() throws SecurityException, NoSuchMethodException {
    if (this.clazz == null || this.methodName == null
        || this.signature == null) {
      throw new IllegalStateException();
    }
    return this.clazz.getMethod(this.methodName, this.signature);
  }
}
