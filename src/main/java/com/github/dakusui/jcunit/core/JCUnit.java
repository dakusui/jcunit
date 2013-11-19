package com.github.dakusui.jcunit.core;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.runner.Runner;
import org.junit.runners.Suite;
import org.junit.runners.model.TestClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dakusui.jcunit.exceptions.JCUnitEnvironmentException;
import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.jcunit.exceptions.JCUnitPluginException;
import com.github.dakusui.jcunit.exceptions.ObjectUnderFrameworkException;
import com.github.dakusui.jcunit.generators.SimpleTestArrayGenerator;
import com.github.dakusui.jcunit.generators.TestArrayGenerator;

public class JCUnit extends Suite {
	private static final Logger LOGGER = LoggerFactory.getLogger(JCUnit.class);
	
	private final ArrayList<Runner> runners= new ArrayList<Runner>();

	/**
	 * Only called reflectively. Do not use programmatically.
	 */
	public JCUnit(Class<?> klass) throws Throwable {
		super(klass, Collections.<Runner>emptyList());
		List<Object> parametersList= getParametersList(getTestClass());
		for (int i= 0; i < parametersList.size(); i++)
			runners.add(
					new JCUnitRunner(
							getTestClass().getJavaClass(),
							parametersList, 
							i)
					);
	}

	@SuppressWarnings("unchecked")
	static Map<Field, Object> cast(Object values) {
		return (Map<Field, Object>) values;
	}

	@Override
	protected List<Runner> getChildren() {
		return runners;
	}

	private List<Object> getParametersList(TestClass klass)
			throws Throwable {
		@SuppressWarnings("rawtypes")
		Class<? extends TestArrayGenerator> generatorClass = getTestArrayGeneratorClass(klass.getJavaClass());
		if (generatorClass == null) {
			generatorClass = SimpleTestArrayGenerator.class;
		}
		return JCUnit.composeTestArray(
				klass.getJavaClass(),
				generatorClass
				);
	}

	static Method domainMethod(Class<?> cut, Field inField) {
		Method ret;
		try {
			try {
				ret = cut.getMethod(inField.getName());
			} catch (NoSuchMethodException e) {
				ret = cut.getDeclaredMethod(inField.getName());
			}
		} catch (SecurityException e) {
			String msg = String.format("JCUnit cannot be run in this environment. (%s:%s)", e.getClass().getName(), e.getMessage());
			throw new JCUnitEnvironmentException(msg, e);
		} catch (NoSuchMethodException e) {
			String msg = String.format("Method to generate a domain for '%s' isn't defined in class '%s' or not visible.", inField, cut);
			throw new ObjectUnderFrameworkException(msg, e);
		}
		if (!validateDomainMethod(inField, ret)) {
			String msg = String.format("Domain method '%s' isn't compatible with field '%s'", ret, inField);
			throw new IllegalArgumentException(msg, null);
		}
		return ret;
	}

	static Method assertMethod(Class<?> cut, Field outField) {
		Method ret;
		try {
			try {
				ret = cut.getMethod(outField.getName(), outField.getType(), outField.getType());
			} catch (NoSuchMethodException e) {
				ret = cut.getDeclaredMethod(outField.getName());
			} 
		} catch (SecurityException e) {
			String msg = String.format("JCUnit cannot be run in this environment. (%s:%s)", e.getClass().getName(), e.getMessage());
			throw new JCUnitEnvironmentException(msg, e);
		} catch (NoSuchMethodException e) {
			String msg = String.format("Method to generate a domain for '%s' isn't defined in class '%s' or not visible.", outField, cut);
			throw new ObjectUnderFrameworkException(msg, e);
		}
		if (!validateAssertMethod(outField, ret)) {
			String msg = String.format("Assertion method '%s' isn't compatible with field '%s'", ret, outField);
			throw new IllegalArgumentException(msg, null);
		}
		
		return ret;
	}
	
	public static boolean checkIfStatic(Method domainMethod) {
		return Modifier.isStatic(domainMethod.getModifiers());
	}

	private static boolean checkIfTypeCompatible(Field inField,
			Method domainMethod) {
		if (!domainMethod.getReturnType().isArray()) {
			return false;
		}
		if (domainMethod.getReturnType().getComponentType() != inField.getType()) {
			return false;
		}
		return true;
	}

	private static boolean checkIfAnyParameterExists(Method domainMethod) {
		return domainMethod.getParameterTypes().length != 0;
	}

	private static boolean checkIfReturnTypeIsArray(Method domainMethod) {
		return domainMethod.getReturnType().isArray();
	}

	private static boolean checkIfReturnTypeIsBoolean(Method assertMethod) {
		return Boolean.TRUE.equals(assertMethod.getReturnType());
	}

	private static boolean validateAssertMethod(Field outField, Method assertMethod) {
		boolean ret = true;
		ret &= JCUnit.checkIfStatic(assertMethod);
		ret &= JCUnit.checkIfReturnTypeIsBoolean(assertMethod);
		return ret;
	}

	private static boolean validateDomainMethod(Field inField, Method domainMethod) {
		boolean ret = true;
		ret &= JCUnit.checkIfStatic(domainMethod);
		ret &= !JCUnit.checkIfAnyParameterExists(domainMethod);
		ret &= JCUnit.checkIfReturnTypeIsArray(domainMethod);
		ret &= JCUnit.checkIfTypeCompatible(inField, domainMethod);
		return ret;
	}

	public static DomainGenerator domainGenerator(Class<?> cut, Field inField) {
		DomainGenerator ret = null;
		In.Domain inType = inField.getAnnotation(In.class).domain();
		assert inType != null;
		
		if (inType  == In.Domain.Method) {
			final Method m = JCUnit.domainMethod(cut, inField);
			ret = new DomainGenerator() {
				@Override
				public Object[] domain() throws JCUnitException {
					return Utils.invokeDomainMethod(m);
				}
			};
		} else if (inType == In.Domain.Default) {
			Class<?> inFieldType = inField.getType();
			Object[] tmpvalues = null;
			if (inFieldType == Integer.TYPE || inFieldType == Integer.class) {
				tmpvalues = new Object[]{1, 0, -1, 100, -100, Integer.MAX_VALUE, Integer.MIN_VALUE};
			} else if (inFieldType == Long.TYPE || inFieldType == Long.class) {
				tmpvalues = new Object[]{1L, 0L, -1L, 100L, -100L, Long.MAX_VALUE, Long.MIN_VALUE};
			} else if (inFieldType == Short.TYPE || inFieldType == Short.class) {
				tmpvalues = new Object[]{(short)1, (short)0, (short)-1, (short)100, (short)-100, Short.MAX_VALUE, Short.MIN_VALUE};
			} else if (inFieldType == Byte.TYPE || inFieldType == Byte.class) {
				tmpvalues = new Object[]{(byte)1, (byte)0, (byte)-1, (byte)100, (byte)-100, Byte.MAX_VALUE, Byte.MIN_VALUE};
			} else if (inFieldType == Float.TYPE || inFieldType == Float.class) {
				tmpvalues = new Object[]{1.0f, 0f, -1.0f, 100.0f, -100.0f, Float.MAX_VALUE, Float.MIN_VALUE};
			} else if (inFieldType == Double.TYPE || inFieldType == Double.class) {
				tmpvalues = new Object[]{1.0d, 0d, -1.0d, 100.0d, -100.0d, Double.MAX_VALUE, Double.MIN_VALUE};
			} else if (inFieldType == Character.TYPE || inFieldType == Character.class) {
				tmpvalues = new Object[]{'a', 'あ', (char)1, Character.MAX_VALUE, Character.MIN_VALUE};
			} else if (inFieldType == Boolean.TYPE || inFieldType == Boolean.class) {
				tmpvalues = new Object[]{true, false};
			} else if (inFieldType == String.class) {
				tmpvalues = new Object[]{"Hello world", "こんにちは世界", ""};
			} else if (Enum.class.isAssignableFrom(inFieldType)) {
				try {
					tmpvalues = (Object[]) inFieldType.getMethod("values").invoke(null);
				} catch (IllegalArgumentException e) {
				} catch (SecurityException e) {
				} catch (IllegalAccessException e) {
				} catch (InvocationTargetException e) {
				} catch (NoSuchMethodException e) {
				}
			} else {
				String msg = String.format(
						"Only primitive type fields and fields whose class are wrappers " + 
						"of primitive types can use 'Default' domain type. (field:%s, type:%s)",
						inField.getName(),
						inFieldType.getName()
				);
				throw new IllegalArgumentException(msg);
			}
			assert tmpvalues != null;
			// if the field isn't a primitive, null is added as a possible value.
			if (!inFieldType.isPrimitive() && inField.getAnnotation(In.class).includeNull()) {
				Object[] values2 = new Object[tmpvalues.length + 1];
				System.arraycopy(tmpvalues, 0, values2, 0, tmpvalues.length);
				values2[tmpvalues.length] = null;
				tmpvalues = values2;
			}
			final Object[] values = tmpvalues; 
			ret = new DomainGenerator() {
				@Override
				public Object[] domain() {
					return values;
				}
			};
		} else if (inType == In.Domain.None) {
			ret = new DomainGenerator() {
				@Override
				public Object[] domain() throws JCUnitException {
					return new Object[]{};
				}
			};
		}
		assert ret != null;
		return ret;
	}

	@SuppressWarnings("unchecked")
	public
	static TestArrayGenerator<Field, Object> newTestArrayGenerator(
			@SuppressWarnings("rawtypes") Class<? extends TestArrayGenerator> generatorClass,
			Map<Field, Object[]> domains) {
		TestArrayGenerator<Field, Object> ret = null;
		try {
			ret = (TestArrayGenerator<Field, Object>) generatorClass.newInstance();
			ret.init(domains);
		} catch (InstantiationException e) {
			throw new JCUnitPluginException(e.getMessage(), e);
		} catch (IllegalAccessException e) {
			throw new JCUnitPluginException(e.getMessage(), e);
		}
		return ret;
	}

	public static List<Object> composeTestArray(
			Class<? extends Object> cut, 
			@SuppressWarnings("rawtypes") Class<? extends TestArrayGenerator> generatorClass) throws JCUnitException {
		if (generatorClass == null) throw new NullPointerException();
		
		Field[] fields = Utils.getInFieldsFromClassUnderTest(cut);
		
		Map<Field, Object[]> domains = new LinkedHashMap<Field, Object[]>();
		for (Field f : fields) {
			DomainGenerator domainGenerator = JCUnit.domainGenerator(cut, f);
			if (domainGenerator != null) {
				Object[] domain = domainGenerator.domain();
				domains.put(f, domain);
			}
		}
		
		List<Object> ret = new ArrayList<Object>();
		
		TestArrayGenerator<Field, Object> testArrayGenerator = JCUnit.newTestArrayGenerator(generatorClass, domains);
		for (Map<Field, Object> pattern : testArrayGenerator) {
			ret.add(pattern);
		}
		
		reportTestArray(testArrayGenerator);
		return ret;
	}

	private static void reportTestArray(
			TestArrayGenerator<Field, Object> testArrayGenerator) {
		writeHeader();
		LOGGER.info("");
		writeDomains(testArrayGenerator);
		LOGGER.info("");
		writeMatrix(testArrayGenerator);
		LOGGER.info("");
	}
	
	private static void writeHeader() {
		LOGGER.info("***********************************************");
		LOGGER.info("***                                         ***");
		LOGGER.info("***          T E S T   M A T R I X          ***");
		LOGGER.info("***                                         ***");
		LOGGER.info("***********************************************");
	}

	protected static void writeDomains(
			TestArrayGenerator<Field, Object> testArrayGenerator) {
		LOGGER.info("* DOMAINS *");
		char keyCode = 'A';
		for (Field key : testArrayGenerator.getKeys()) {
			////
			// print out header
			String domainHeader = String.format("%s:%s(%s)", keyCode, key.getName(), key.getType());
			LOGGER.info("  " + domainHeader);
			Object[] d = testArrayGenerator.getDomain(key);
			for (int i = 0; i < d.length; i++) {
				String l = String.format("%02d:'%s'", i, d[i]);
				LOGGER.info("    " + l);
			}
			keyCode++;
		}
	}

	protected static void writeMatrix(
			TestArrayGenerator<Field, Object> testArrayGenerator) {
		LOGGER.info("* MATRIX *");
		String header = String.format("%22s", "");
		int numKeys = testArrayGenerator.getKeys().size();
		boolean firstTime = true;
		char keyCode = 'A';
		for (int i = 0; i < numKeys; i++) {
			if (firstTime) firstTime = false; else header += ",";
			header += String.format("%-2s", keyCode);
			keyCode ++;
		}
		LOGGER.info(header);
		long size = testArrayGenerator.size();
		for (int i = 0; i < size; i++) {
			String line = String.format("%-20s", String.format("testrun[%d]:", i));
			firstTime = true;
			for (Field key : testArrayGenerator.getKeys()) {
				int valueCode = testArrayGenerator.getIndex(key, i);
				if (firstTime) firstTime = false; else line += ",";
				line += String.format("%02d", valueCode);
			}
			LOGGER.info("  "  + line);
		}
	}

	@SuppressWarnings("rawtypes")
	private static Class<? extends TestArrayGenerator> getTestArrayGeneratorClass(Class<? extends Object> cuf) {
		Generator an = cuf.getAnnotation(Generator.class);
		Class<? extends TestArrayGenerator> ret =  an != null ? an.value() : null;
		if (ret != null) {
			return ret;
		} else {
			Class<? extends Object> superClass = cuf.getSuperclass();
			if  (superClass == null) {
				return null;
			}
			return getTestArrayGeneratorClass(superClass);
		}
	}
}
