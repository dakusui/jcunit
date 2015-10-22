package com.github.dakusui.jcunit.tests.param;

import com.github.dakusui.jcunit.standardrunner.annotations.Param;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.exceptions.InvalidPluginException;
import com.github.dakusui.jcunit.exceptions.InvalidTestException;
import org.junit.Test;

import java.lang.annotation.Annotation;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

public class ParamTypeTest {
  @Test
  public void testBoolean() {
    assertEquals("boolean", Param.Type.Boolean.toString());
    whenValidValue$thenParsedValueReturned(Param.Type.Boolean, true, "true");
    whenValidValue$thenParsedValueReturned(Param.Type.BooleanArray, new Object[] { true, false }, "true", "false");
    whenInvalidValue$thenExceptionThrown(Param.Type.Boolean, InvalidTestException.class, "TRuE");
  }

  @Test
  public void testByte() {
    assertEquals("byte", Param.Type.Byte.toString());
    whenValidValue$thenParsedValueReturned(Param.Type.Byte, (byte) 3, "3");
    whenValidValue$thenParsedValueReturned(Param.Type.ByteArray, new Object[] { (byte) 1, (byte) 2 }, "1", "2");
    whenInvalidValue$thenExceptionThrown(Param.Type.Byte, InvalidTestException.class, "ZZZ");
  }

  @Test
  public void testChar() {
    assertEquals("char", Param.Type.Char.toString());
    whenValidValue$thenParsedValueReturned(Param.Type.Char, 't', "t");
    whenValidValue$thenParsedValueReturned(Param.Type.CharArray, new Object[] { 'a', 'b', 'c' }, "a", "b", "c");
    whenInvalidValue$thenExceptionThrown(Param.Type.Char, InvalidTestException.class, "ABC");
  }

  @Test
  public void testShort() {
    assertEquals("short", Param.Type.Short.toString());
    whenValidValue$thenParsedValueReturned(Param.Type.Short, (short) 3, "3");
    whenValidValue$thenParsedValueReturned(Param.Type.ShortArray, new Object[] { (short) 1, (short) 2 }, "1", "2");
    whenInvalidValue$thenExceptionThrown(Param.Type.Short, InvalidTestException.class, "ZZZ");
  }

  @Test
  public void testInt() {
    assertEquals("int", Param.Type.Int.toString());
    whenValidValue$thenParsedValueReturned(Param.Type.Int, 3, "3");
    whenValidValue$thenParsedValueReturned(Param.Type.IntArray, new Object[] { 1, 2 }, "1", "2");
    whenInvalidValue$thenExceptionThrown(Param.Type.Int, InvalidTestException.class, "ZZZ");
  }

  @Test
  public void testLong() {
    assertEquals("long", Param.Type.Long.toString());
    whenValidValue$thenParsedValueReturned(Param.Type.Long, (long) 3, "3");
    whenValidValue$thenParsedValueReturned(Param.Type.LongArray, new Object[] { (long) 1, (long) 2 }, "1", "2");
    whenInvalidValue$thenExceptionThrown(Param.Type.Long, InvalidTestException.class, "ZZZ");
  }

  @Test
  public void testFloat() {
    assertEquals("float", Param.Type.Float.toString());
    whenValidValue$thenParsedValueReturned(Param.Type.Float, (float) 3.1, "3.1");
    whenValidValue$thenParsedValueReturned(Param.Type.FloatArray, new Object[] { (float) 1.2, (float) 2.3 }, "1.2", "2.3");
    whenInvalidValue$thenExceptionThrown(Param.Type.Float, InvalidTestException.class, "ZZZ");
  }

  @Test
  public void testDouble() {
    assertEquals("double", Param.Type.Double.toString());
    whenValidValue$thenParsedValueReturned(Param.Type.Double, 3.1, "3.1");
    whenValidValue$thenParsedValueReturned(Param.Type.DoubleArray, new Object[] { 1.2, 2.3 }, "1.2", "2.3");
    whenInvalidValue$thenExceptionThrown(Param.Type.Double, InvalidTestException.class, "ZZZ");
  }

  @Test
  public void testString() {
    assertEquals("String", Param.Type.String.toString());
    whenValidValue$thenParsedValueReturned(Param.Type.String, "hello", "hello");
    whenValidValue$thenParsedValueReturned(Param.Type.StringArray, new Object[] { "Hello", "World" }, "Hello", "World");
  }

  public void whenValidValue$thenParsedValueReturned(Param.Type type, Object expected, String... value) {
    assertEquals(expected, type.parse(value));
  }

  public void whenValidValue$thenParsedValueReturned(Param.Type type, Object[] expected, String... value) {
    assertArrayEquals(expected, (Object[]) type.parse(value));
  }

  public void whenInvalidValue$thenExceptionThrown(Param.Type type, Class<? extends Throwable> expectedException, String... value) {
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

  public static Param p1 = new Param() {
    @Override
    public String[] value() {
      return new String[]{"true"};
    }

    @Override
    public Class<? extends Annotation> annotationType() {
      return Param.class;
    }
  };

  @Test
  public void processParamsTest1() {
    Object[] processed = Param.Type.processParams(new Param.Type[] { Param.Type.Boolean }, new Param[] { p1 });
    assertArrayEquals(new Object[]{Boolean.TRUE}, processed);
  }

  @Test
  public void processParamsTest2() {
    Object[] processed = Param.Type.processParams(new Param.Type[] { Param.Type.Boolean.withDefaultValue(true) }, new Param[] {});
    assertArrayEquals(new Object[]{Boolean.TRUE}, processed);
  }

  @Test
  public void processParamsTest3() {
    Object[] processed = Param.Type.processParams(new Param.Type[] { Param.Type.Boolean.withDefaultValue(true), Param.Type.Boolean.withDefaultValue(false) }, new Param[] {});
    assertArrayEquals(new Object[]{Boolean.TRUE, Boolean.FALSE}, processed);
  }

  @Test
  public void processParamsTest4() {
    Object[] processed = Param.Type.processParams(new Param.Type[] { Param.Type.Boolean.withVarArgsEnabled() }, new Param[] { p1 });
    assertArrayEquals(new Object[]{Boolean.TRUE}, processed);
  }

  @Test
  public void processParamsTest5() {
    Object[] processed = Param.Type.processParams(new Param.Type[] { Param.Type.Boolean.withVarArgsEnabled() }, new Param[] { p1, p1 });
    assertArrayEquals(new Object[]{Boolean.TRUE, Boolean.TRUE}, processed);
  }

  @Test(expected = InvalidTestException.class)
  public void processParamsTest6$tooLittleParameters() {
    Object[] processed = Param.Type.processParams(new Param.Type[] { Param.Type.Boolean, Param.Type.Boolean.withVarArgsEnabled() }, new Param[] {});
    assertArrayEquals(new Object[]{Boolean.TRUE, Boolean.TRUE}, processed);
  }
  @Test(expected = InvalidTestException.class)
  public void processParamsTest6$tooManyParameters() {
    Object[] processed = Param.Type.processParams(new Param.Type[] { Param.Type.Boolean, Param.Type.Boolean }, new Param[] { p1, p1, p1, p1 });
    assertArrayEquals(new Object[]{Boolean.TRUE, Boolean.TRUE}, processed);
  }

  @Test
  public void processParamsTest$defaultValueAndVarArgsInCombination() {
    Object[] processed = Param.Type.processParams(new Param.Type[] { Param.Type.Boolean.withDefaultValue(false), Param.Type.Boolean.withVarArgsEnabled() }, new Param[] {});
    assertArrayEquals(new Object[]{Boolean.FALSE}, processed);
  }

  @Test(expected = InvalidPluginException.class)
  public void processParamsTest$invalidDefaultNotAtLast() {
    Object[] processed = Param.Type.processParams(new Param.Type[] { Param.Type.Boolean.withDefaultValue(false), Param.Type.Boolean }, new Param[] {});
    assertArrayEquals(new Object[]{Boolean.FALSE}, processed);
  }

  @Test(expected = InvalidPluginException.class)
  public void processParamsTest$invalidVarargsNotAtLast() {
    Object[] processed = Param.Type.processParams(new Param.Type[] { Param.Type.Boolean.withVarArgsEnabled(), Param.Type.Boolean }, new Param[] { p1, p1 });
  }

}
