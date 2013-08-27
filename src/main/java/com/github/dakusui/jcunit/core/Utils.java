package com.github.dakusui.jcunit.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import com.github.dakusui.jcunit.exceptions.JCUnitEnvironmentException;
import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.jcunit.exceptions.ObjectUnderFrameworkException;

public class Utils {
	public static BigDecimal bigDecimal(Number num) {
		if (num == null) throw new NullPointerException();
		if (num instanceof BigDecimal) return (BigDecimal) num;
		if (num instanceof BigInteger) return new BigDecimal((BigInteger)num);
		if (num instanceof Byte) return new BigDecimal((Byte)num);
		if (num instanceof Double) return new BigDecimal((Double)num);
		if (num instanceof Float) return new BigDecimal((Float)num);
		if (num instanceof Integer) return new BigDecimal((Integer)num);
		if (num instanceof Long) return new BigDecimal((Long)num);
		if (num instanceof Short) return new BigDecimal((Short)num);
		String message = String.format("Unsupported number object %s(%s) is given.", num, num.getClass());
		throw new IllegalArgumentException(message);
	}
	
	public static Object normalize(Object v) {
		if (v == null) return null;
		if (v instanceof Number) {
			return bigDecimal((Number) v);
		}
		return v;
	}
	
	public static Field getField(Object obj, String name) {
		if (obj == null) throw new NullPointerException();
		Field ret;
		try {
			ret = obj.getClass().getField(name);
			if (!ret.isAnnotationPresent(In.class) && !ret.isAnnotationPresent(Out.class))
				throw new NoSuchFieldException();
		} catch (SecurityException e) {
			String msg = String.format("JCUnit cannot be run in this environment. (%s:%s)", e.getClass().getName(), e.getMessage());
			throw new JCUnitEnvironmentException(msg, e);
		} catch (NoSuchFieldException e) {
			String msg = String.format("Field '%s' isn't defined in class '%s' or not annotated.", name, obj.getClass());
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
			assert false; 
			throw e;
		} catch (IllegalAccessException e) {
			assert false;
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


	public static <T> T checknull(T obj) {
		if (obj == null) throw new NullPointerException();
		return obj;
	}
	
	public static void initializeObjectUnderTest(Object out, Map<Field, Object> values) {
		for (Field f : values.keySet()) {
			setFieldValue(out, f, values.get(f));
		}
	}

	public static Field[] getInFieldsFromClassUnderTest(Class<?> cut) {
		return getAnnotatedFieldsFromClassUnderTest(cut, In.class);
	}

	public static Field[] getOutFieldsFromClassUnderTest(Class<?> cut) {
		return getAnnotatedFieldsFromClassUnderTest(cut, Out.class);
	}

	public static Field[] getAnnotatedFieldsFromClassUnderTest(Class<?> cut, Class<? extends Annotation> annClass) {
		Field[] declaerdFields = cut.getFields();
		List<Field> ret = new ArrayList<Field>(declaerdFields.length);
		for (Field f : declaerdFields) {
			if (f.getAnnotation(annClass) != null) {
				ret.add(f);
			}
		}
		return ret.toArray(new Field[0]);
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
				String message = String.format("A domain method '%s' reported an internal exception (%s)", m, tt.getMessage());
				throw new ObjectUnderFrameworkException(message, tt);
			}
		} finally {
			m.setAccessible(accessible);
		}
		return ret;
	}

	@SuppressWarnings("unchecked")
	public static <T> T cast(Class<T> clazz, Object obj) {
		if (clazz == null) throw new NullPointerException();
		if (obj == null || clazz.isAssignableFrom(obj.getClass())) {
			return (T) obj;
		}
		throw new ClassCastException(msgClassCastException(clazz, obj));
	}
	
	private static String msgClassCastException(Class<?> clazz, Object obj) {
		return String.format(
				"An instance of '%s' class is expected, but '%s'(class=%s) was given.",
				clazz.getName(), 
				obj,
				obj == null ? null : obj.getClass().getName()
		);
	}
}