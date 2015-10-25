package com.github.dakusui.jcunit.tests.param;

import com.github.dakusui.jcunit.runners.standard.annotations.Value;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.exceptions.InvalidPluginException;
import com.github.dakusui.jcunit.exceptions.InvalidTestException;
import org.junit.Test;

import java.lang.annotation.Annotation;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

public class ValueTypeTest {
  @Test
  public void testBoolean() {
    assertEquals("boolean", Value.Type.Boolean.toString());
    whenValidValue$thenParsedValueReturned(Value.Type.Boolean, true, "true");
    whenValidValue$thenParsedValueReturned(Value.Type.BooleanArray, new Object[] { true, false }, "true", "false");
    whenInvalidValue$thenExceptionThrown(Value.Type.Boolean, InvalidTestException.class, "TRuE");
  }

  @Test
  public void testByte() {
    assertEquals("byte", Value.Type.Byte.toString());
    whenValidValue$thenParsedValueReturned(Value.Type.Byte, (byte) 3, "3");
    whenValidValue$thenParsedValueReturned(Value.Type.ByteArray, new Object[] { (byte) 1, (byte) 2 }, "1", "2");
    whenInvalidValue$thenExceptionThrown(Value.Type.Byte, InvalidTestException.class, "ZZZ");
  }

  @Test
  public void testChar() {
    assertEquals("char", Value.Type.Char.toString());
    whenValidValue$thenParsedValueReturned(Value.Type.Char, 't', "t");
    whenValidValue$thenParsedValueReturned(Value.Type.CharArray, new Object[] { 'a', 'b', 'c' }, "a", "b", "c");
    whenInvalidValue$thenExceptionThrown(Value.Type.Char, InvalidTestException.class, "ABC");
  }

  @Test
  public void testShort() {
    assertEquals("short", Value.Type.Short.toString());
    whenValidValue$thenParsedValueReturned(Value.Type.Short, (short) 3, "3");
    whenValidValue$thenParsedValueReturned(Value.Type.ShortArray, new Object[] { (short) 1, (short) 2 }, "1", "2");
    whenInvalidValue$thenExceptionThrown(Value.Type.Short, InvalidTestException.class, "ZZZ");
  }

  @Test
  public void testInt() {
    assertEquals("int", Value.Type.Int.toString());
    whenValidValue$thenParsedValueReturned(Value.Type.Int, 3, "3");
    whenValidValue$thenParsedValueReturned(Value.Type.IntArray, new Object[] { 1, 2 }, "1", "2");
    whenInvalidValue$thenExceptionThrown(Value.Type.Int, InvalidTestException.class, "ZZZ");
  }

  @Test
  public void testLong() {
    assertEquals("long", Value.Type.Long.toString());
    whenValidValue$thenParsedValueReturned(Value.Type.Long, (long) 3, "3");
    whenValidValue$thenParsedValueReturned(Value.Type.LongArray, new Object[] { (long) 1, (long) 2 }, "1", "2");
    whenInvalidValue$thenExceptionThrown(Value.Type.Long, InvalidTestException.class, "ZZZ");
  }

  @Test
  public void testFloat() {
    assertEquals("float", Value.Type.Float.toString());
    whenValidValue$thenParsedValueReturned(Value.Type.Float, (float) 3.1, "3.1");
    whenValidValue$thenParsedValueReturned(Value.Type.FloatArray, new Object[] { (float) 1.2, (float) 2.3 }, "1.2", "2.3");
    whenInvalidValue$thenExceptionThrown(Value.Type.Float, InvalidTestException.class, "ZZZ");
  }

  @Test
  public void testDouble() {
    assertEquals("double", Value.Type.Double.toString());
    whenValidValue$thenParsedValueReturned(Value.Type.Double, 3.1, "3.1");
    whenValidValue$thenParsedValueReturned(Value.Type.DoubleArray, new Object[] { 1.2, 2.3 }, "1.2", "2.3");
    whenInvalidValue$thenExceptionThrown(Value.Type.Double, InvalidTestException.class, "ZZZ");
  }

  @Test
  public void testString() {
    assertEquals("String", Value.Type.String.toString());
    whenValidValue$thenParsedValueReturned(Value.Type.String, "hello", "hello");
    whenValidValue$thenParsedValueReturned(Value.Type.StringArray, new Object[] { "Hello", "World" }, "Hello", "World");
  }

  public void whenValidValue$thenParsedValueReturned(Value.Type type, Object expected, String... value) {
    assertEquals(expected, type.parse(value));
  }

  public void whenValidValue$thenParsedValueReturned(Value.Type type, Object[] expected, String... value) {
    assertArrayEquals(expected, (Object[]) type.parse(value));
  }

  public void whenInvalidValue$thenExceptionThrown(Value.Type type, Class<? extends Throwable> expectedException, String... value) {
    try {
      type.parse(value);
    } catch (Throwable t) {
      assertTrue(
          String.format("Expected exception=%s, actual exception=%s\n\t%s",
              expectedException.getCanonicalName(),
              t.getClass().getCanonicalName(),
              Utils.join("\n\t", new Object[] { t.getStackTrace() })),
          expectedException.isAssignableFrom(t.getClass())
      );
      return;
    }
    assertTrue(
        String.format("%s should have been thrown, but not.", expectedException.getCanonicalName()),
        false);
  }

  public static Value p1 = new Value() {
    @Override
    public String[] value() {
      return new String[] { "true" };
    }

    @Override
    public Class<? extends Annotation> annotationType() {
      return Value.class;
    }
  };

  @Test
  public void processParamsTest1() {
    Object[] processed = Value.Type.processParams(new Value.Type[] { Value.Type.Boolean }, new Value[] { p1 });
    assertArrayEquals(new Object[] { Boolean.TRUE }, processed);
  }

  @Test
  public void processParamsTest2() {
    Object[] processed = Value.Type.processParams(new Value.Type[] { Value.Type.Boolean.withDefaultValue(true) }, new Value[] {});
    assertArrayEquals(new Object[] { Boolean.TRUE }, processed);
  }

  @Test
  public void processParamsTest3() {
    Object[] processed = Value.Type.processParams(new Value.Type[] { Value.Type.Boolean.withDefaultValue(true), Value.Type.Boolean.withDefaultValue(false) }, new Value[] {});
    assertArrayEquals(new Object[] { Boolean.TRUE, Boolean.FALSE }, processed);
  }

  @Test
  public void processParamsTest4() {
    Object[] processed = Value.Type.processParams(new Value.Type[] { Value.Type.Boolean.withVarArgsEnabled() }, new Value[] { p1 });
    assertArrayEquals(new Object[] { Boolean.TRUE }, processed);
  }

  @Test
  public void processParamsTest5() {
    Object[] processed = Value.Type.processParams(new Value.Type[] { Value.Type.Boolean.withVarArgsEnabled() }, new Value[] { p1, p1 });
    assertArrayEquals(new Object[] { Boolean.TRUE, Boolean.TRUE }, processed);
  }

  @Test(expected = InvalidTestException.class)
  public void processParamsTest6$tooLittleParameters() {
    Object[] processed = Value.Type.processParams(new Value.Type[] { Value.Type.Boolean, Value.Type.Boolean.withVarArgsEnabled() }, new Value[] {});
    assertArrayEquals(new Object[] { Boolean.TRUE, Boolean.TRUE }, processed);
  }

  @Test(expected = InvalidTestException.class)
  public void processParamsTest6$tooManyParameters() {
    Object[] processed = Value.Type.processParams(new Value.Type[] { Value.Type.Boolean, Value.Type.Boolean }, new Value[] { p1, p1, p1, p1 });
    assertArrayEquals(new Object[] { Boolean.TRUE, Boolean.TRUE }, processed);
  }

  @Test
  public void processParamsTest$defaultValueAndVarArgsInCombination() {
    Object[] processed = Value.Type.processParams(new Value.Type[] { Value.Type.Boolean.withDefaultValue(false), Value.Type.Boolean.withVarArgsEnabled() }, new Value[] {});
    assertArrayEquals(new Object[] { Boolean.FALSE }, processed);
  }

  @Test(expected = InvalidPluginException.class)
  public void processParamsTest$invalidDefaultNotAtLast() {
    Object[] processed = Value.Type.processParams(new Value.Type[] { Value.Type.Boolean.withDefaultValue(false), Value.Type.Boolean }, new Value[] {});
    assertArrayEquals(new Object[] { Boolean.FALSE }, processed);
  }

  @Test(expected = InvalidPluginException.class)
  public void processParamsTest$invalidVarargsNotAtLast() {
    Object[] processed = Value.Type.processParams(new Value.Type[] { Value.Type.Boolean.withVarArgsEnabled(), Value.Type.Boolean }, new Value[] { p1, p1 });
  }

}
