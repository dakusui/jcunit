package com.github.dakusui.jcunit.compat.core;

import com.github.dakusui.jcunit.compat.core.annotations.In;
import com.github.dakusui.jcunit.compat.core.annotations.Out;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.jcunit.exceptions.ObjectUnderFrameworkException;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 */
public class CompatUtils {
	private CompatUtils() {}

	public static void initializeTestObject(Object out,
	    Map<Field, Object> values) {
	  for (Field f : values.keySet()) {
	    Utils.setFieldValue(out, f, values.get(f));
	  }
	}

	public static Field[] getInFieldsFromClassUnderTest(Class<?> cut) {
	  return Utils.getAnnotatedFields(cut, In.class);
	}

	public static Field[] getOutFieldsFromClassUnderTest(Class<?> cut) {
	  return Utils.getAnnotatedFields(cut, Out.class);
	}

	public static Object[] invokeDomainMethod(Method m) throws JCUnitException {
	  Object[] ret = null;
	  boolean accessible = m.isAccessible();
	  try {
	    m.setAccessible(true);
	    Object tmp = m.invoke(null);
	    int arrayLength = Array.getLength(tmp);
	    ret = new Object[arrayLength];
	    for (int i = 0; i < arrayLength; i++) {
	      ret[i] = Array.get(tmp, i);
	    }
	  } catch (IllegalArgumentException e) {
	    assert false;
	  } catch (IllegalAccessException e) {
	    assert false;
	  } catch (InvocationTargetException e) {
	    Throwable t = e.getTargetException();
	    try {
	      throw t;
	    } catch (Error ee) {
	      throw ee;
	    } catch (RuntimeException ee) {
	      throw ee;
	    } catch (JCUnitException ee) {
	      throw ee;
	    } catch (Throwable tt) {
	      String message = String.format(
	          "A domain method '%s' reported an internal exception (%s)", m,
	          tt.getMessage());
	      throw new ObjectUnderFrameworkException(message, tt);
	    }
	  } finally {
	    m.setAccessible(accessible);
	  }
	  return ret;
	}
}
