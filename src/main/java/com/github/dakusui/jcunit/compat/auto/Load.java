package com.github.dakusui.jcunit.compat.auto;

import com.github.dakusui.jcunit.core.encoders.ObjectEncoder;
import com.github.dakusui.jcunit.exceptions.JCUnitCheckedException;
import com.github.dakusui.jcunit.exceptions.JCUnitException;

import java.io.*;
import java.lang.reflect.Field;

public class Load extends AutoBase {
  /**
   * Serial version UID
   */
  private static final long serialVersionUID = -6545578051675203857L;

  @Override
  protected Object autoBaseExec(String testName, Object obj, String fieldName)
      throws JCUnitCheckedException {
    return load(obj.getClass(), fieldName, testName);
  }

  private Object load(Class<?> clazz, String fieldName, String testName)
      throws JCUnitCheckedException {
    Field f = field(clazz, fieldName);
    return readObjectFromFile(fileForField(baseDir(), testName, f), clazz);
  }

  private Object readObjectFromFile(File f, Class<?> clazz) {
    ObjectEncoder encoder = getObjectEncoder(clazz);
    Object ret;

    try {
      InputStream is = new BufferedInputStream(new FileInputStream(f));
      try {
        ret = encoder.decodeObject(is);
      } finally {
        is.close();
      }
    } catch (FileNotFoundException e) {
      String msg = String.format("Failed to find a file (%s)", f);
      throw new JCUnitException(msg, e);
    } catch (IOException e) {
      String msg = String.format("Failed to read object from a file (%s)", f);
      throw new JCUnitException(msg, e);
    }
    return ret;
  }

}
