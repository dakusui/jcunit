package com.github.dakusui.lisj;

import com.github.dakusui.jcunit.exceptions.JCUnitCheckedException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 */
public class LisjUtils {
	private static final Pattern methodPattern = Pattern
			.compile("[a-zA-Z$_][0-9a-zA-Z$_]*");

	private LisjUtils() {}

	@SuppressWarnings("unchecked")
	public static <T> T cast(Class<T> clazz, Object obj) {
		if (clazz == null) {
			throw new NullPointerException();
		}
		if (obj == null || clazz.isAssignableFrom(obj.getClass())) {
			return (T) obj;
		}
		throw new ClassCastException(msgClassCastException(clazz, obj));
	}

	private static String msgClassCastException(Class<?> clazz, Object obj) {
		return String.format(
				"An instance of '%s' class is expected, but '%s'(class=%s) was given.",
				clazz.getName(), obj, obj == null ? null : obj.getClass().getName());
	}

	public static Object invokeMethod(Object obj, String methodId,
	                                  Object[] params)
			throws JCUnitCheckedException {
		if (obj == null) {
			throw new NullPointerException();
		}
		try {
			Method m = obj.getClass().getMethod(getMethodName(methodId),
					getParameterTypes(methodId));
			return m.invoke(obj, params);
		} catch (IllegalArgumentException e) {
			throw e;
		} catch (IllegalAccessException e) {
			throw new JCUnitCheckedException(e.getMessage(), e);
		} catch (InvocationTargetException e) {
			throw new JCUnitCheckedException(e.getMessage(), e.getCause());
		} catch (SecurityException e) {
			throw new JCUnitCheckedException(e.getMessage(), e);
		} catch (NoSuchMethodException e) {
			throw new JCUnitCheckedException(e.getMessage(), e);
		}
	}

	private static Class<?>[] getParameterTypes(String methodId) {
		return new Class<?>[]{};
	}

	private static String getMethodName(String methodId) throws
      JCUnitCheckedException {
		Matcher m = methodPattern.matcher(methodId);
		if (m.find()) {
			return m.group(0);
		}
		throw new JCUnitCheckedException(String.format("Specified method wasn't found:%s",
				methodId), null);
	}

  public static BigDecimal bigDecimal(Number num) {
    if (num == null) {
      throw new NullPointerException();
    }
    if (num instanceof BigDecimal) {
      return (BigDecimal) num;
    }
    if (num instanceof BigInteger) {
      return new BigDecimal((BigInteger) num);
    }
    if (num instanceof Byte) {
      return new BigDecimal((Byte) num);
    }
    if (num instanceof Double) {
      return new BigDecimal((Double) num);
    }
    if (num instanceof Float) {
      return new BigDecimal((Float) num);
    }
    if (num instanceof Integer) {
      return new BigDecimal((Integer) num);
    }
    if (num instanceof Long) {
      return new BigDecimal((Long) num);
    }
    if (num instanceof Short) {
      return new BigDecimal((Short) num);
    }
    String message = String.format(
        "Unsupported number object %s(%s) is given.", num, num.getClass());
    throw new IllegalArgumentException(message);
  }

  public static Object normalize(Object v) {
		if (v == null) {
			return null;
		}
		if (v instanceof Number) {
			return bigDecimal((Number) v);
		}
		return v;
	}
}
