package com.github.dakusui.jcunit.core;

import com.github.dakusui.jcunit.compat.core.annotations.In;
import com.github.dakusui.jcunit.compat.core.annotations.Out;
import com.github.dakusui.jcunit.exceptions.JCUnitEnvironmentException;
import com.github.dakusui.jcunit.exceptions.JCUnitRuntimeException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class Utils {
	public static interface Formatter<T> {
		public static final Formatter INSTANCE = new Formatter<Object>() {
			@Override
			public String format(Object elem) {
				if (elem == null) return null;
				return elem.toString();
			}
		};

		String format(T elem);
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

	public static Field getField(Object obj, String fieldName) {
		if (obj == null) {
			throw new NullPointerException();
		}
		Class<?> clazz = obj.getClass();
		Field ret = getFieldFromClass(clazz, fieldName);
		return ret;
	}

	public static Field getFieldFromClass(Class<?> clazz, String fieldName) {
		Field ret;
		try {
			ret = clazz.getField(fieldName);
			if (!ret.isAnnotationPresent(In.class)
					&& !ret.isAnnotationPresent(Out.class)) {
				throw new NoSuchFieldException();
			}
		} catch (SecurityException e) {
			String msg = String.format(
					"JCUnit cannot be run in this environment. (%s:%s)", e.getClass()
					.getName(), e.getMessage()
			);
			throw new JCUnitEnvironmentException(msg, e);
		} catch (NoSuchFieldException e) {
			String msg = String.format(
					"Field '%s' isn't defined in class '%s' or not annotated.",
					fieldName, clazz);
			throw new IllegalArgumentException(msg, e);
		}
		return ret;
	}

	public static Object getFieldValue(Object obj, Field f) {
		Object ret = null;
		try {
			boolean accessible = f.isAccessible();
			try {
				f.setAccessible(true);
				ret = f.get(obj);
			} finally {
				f.setAccessible(accessible);
			}
		} catch (IllegalArgumentException e) {
			Utils.checkcond(false);
			throw e;
		} catch (IllegalAccessException e) {
			rethrow(e);
		}
		return ret;
	}

	public static void setFieldValue(Object obj, Field f, Object value) {
		try {
			boolean accessible = f.isAccessible();
			try {
				f.setAccessible(true);
				f.set(obj, value);
			} finally {
				f.setAccessible(accessible);
			}
		} catch (IllegalArgumentException e) {
			throw e;
		} catch (IllegalAccessException e) {
			assert false;
		}
	}

	/**
	 * This method is implemented in order to reduce dependencies on external libraries.
	 *
	 * @param sep   A separator to be used to join {@code elemes}
	 * @param elems Elements to be joined.
	 * @return A joined {@code String}
	 */
	public static <T> String join(String sep, Formatter formatter, T... elems) {
		Utils.checknotnull(sep);
		StringBuilder b = new StringBuilder();
		boolean firstOne = true;
		for (T s : elems) {
			if (!firstOne) b.append(formatter.format(sep));
			b.append(s);
			firstOne = false;
		}
		return b.toString();
	}

	public static <T> String join(String sep, T... elems) {
		return join(sep, Formatter.INSTANCE, elems);
	}

	public static <T> String join(T... elems) {
		return join("", elems);
	}

	/**
	 * Checks if the given {@code obj} is {@code null} or not.
	 * If it is, a {@code NullPointerException} will be thrown.
	 * <p/>
	 * This method is implemented in order to reduce dependencies on external libraries.
	 *
	 * @param obj A variable to be checked.
	 * @param <T> The type of {@code obj}
	 * @return {@code obj} itself
	 */
	public static <T> T checknotnull(T obj) {
		if (obj == null) {
			throw new NullPointerException();
		}
		return obj;
	}

	public static void checkcond(boolean b) {
		if (!b) {
			throw new IllegalArgumentException();
		}
	}

	public static void checkcond(boolean b, String msg) {
		if (!b) {
			throw new IllegalArgumentException(msg);
		}
	}

	public static void rethrow(String msg, Exception e) {
		throw new JCUnitRuntimeException(msg, e);
	}

	public static void rethrow(Exception e) {
		rethrow(e.getMessage(), e);
	}

	public static Field[] getAnnotatedFields(Class<?> cut,
	                                         Class<? extends Annotation> annClass) {
		Field[] declaerdFields = cut.getFields();
		List<Field> ret = new ArrayList<Field>(declaerdFields.length);
		for (Field f : declaerdFields) {
			if (f.getAnnotation(annClass) != null) {
				ret.add(f);
			}
		}
		return ret.toArray(new Field[0]);
	}
}
