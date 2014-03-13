package com.github.dakusui.jcunit.auto;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;

import com.github.dakusui.jcunit.auto.encoder.ObjectEncoder;
import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.jcunit.exceptions.JCUnitRuntimeException;

/**
 * A function that stores a value of a field of a given object.
 * 
 * This function returns <code>true</code> as long as the storing procedure is
 * successful.
 * 
 * @see AutoBase
 * @author hiroshi
 */
public class Store extends AutoBase {
  /**
   * Serial version UID
   */
  private static final long serialVersionUID = -9189450774385787028L;

  @Override
  protected Object autoBaseExec(String testName, Object obj, String fieldName)
      throws JCUnitException {
    store(obj, fieldName, testName);
    return false;
  }

  private void store(Object obj, String fieldName, String testName)
      throws JCUnitException {
    Field field = field(obj.getClass(), fieldName);
    Object value;
    try {
      value = field.get(obj);
      saveObjectToFile(fileForField(baseDir(), testName, field),
          field.getDeclaringClass(), value);
    } catch (IllegalArgumentException e) {
      // //
      // This clause shouldn't be executed since method 'field' should return
      // an appropriate field only and throw a runtime exception if there is no
      // appropriate one.
      assert false;
    } catch (IllegalAccessException e) {
      // //
      // This clause shouldn't be executed since method 'field' should return
      // an appropriate field only and throw a runtime exception if there is no
      // appropriate one.
      assert false;
    }
  }

  private void saveObjectToFile(File f, Class<?> clazz, Object value) {
    try {
      if (!f.getParentFile().isDirectory() && !f.getParentFile().mkdirs()) {
        String msg = String.format("Failed to create a directory '%s'",
            f.getParentFile());
        throw new JCUnitRuntimeException(msg, null);
      }
      ObjectEncoder objEncoder = getObjectEncoder(clazz);
      OutputStream os = new BufferedOutputStream(new FileOutputStream(f));
      try {
        objEncoder.encodeObject(os, value);
      } finally {
        os.close();
      }
    } catch (FileNotFoundException e) {
      String msg = String.format("Failed to find a file (%s)", f);
      throw new JCUnitRuntimeException(msg, e);
    } catch (IOException e) {
      String msg = String.format("Failed to write object (%s) to a file (%s)",
          f, value);
      throw new JCUnitRuntimeException(msg, e);
    }
  }
}
