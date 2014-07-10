package com.github.dakusui.lisj;

import com.github.dakusui.jcunit.exceptions.JCUnitException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
			throws JCUnitException {
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
			throw new JCUnitException(e.getMessage(), e);
		} catch (InvocationTargetException e) {
			throw new JCUnitException(e.getMessage(), e.getCause());
		} catch (SecurityException e) {
			throw new JCUnitException(e.getMessage(), e);
		} catch (NoSuchMethodException e) {
			throw new JCUnitException(e.getMessage(), e);
		}
	}

	private static Class<?>[] getParameterTypes(String methodId) {
		return new Class<?>[]{};
	}

	private static String getMethodName(String methodId) throws JCUnitException {
		Matcher m = methodPattern.matcher(methodId);
		if (m.find()) {
			return m.group(0);
		}
		throw new JCUnitException(String.format("Specified method wasn't found:%s",
				methodId), null);
	}
}
