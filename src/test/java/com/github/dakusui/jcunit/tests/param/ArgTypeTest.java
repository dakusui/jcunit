package com.github.dakusui.jcunit.tests.param;

import com.github.dakusui.jcunit.standardrunner.annotations.Arg;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.exceptions.InvalidPluginException;
import com.github.dakusui.jcunit.exceptions.InvalidTestException;
import org.junit.Test;

import java.lang.annotation.Annotation;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

public class ArgTypeTest {
  @Test
  public void testBoolean() {
    assertEquals("boolean", Arg.Type.Boolean.toString());
    whenValidValue$thenParsedValueReturned(Arg.Type.Boolean, true, "true");
    whenValidValue$thenParsedValueReturned(Arg.Type.BooleanArray, new Object[] { true, false }, "true", "false");
    whenInvalidValue$thenExceptionThrown(Arg.Type.Boolean, InvalidTestException.class, "TRuE");
  }

  @Test
  public void testByte() {
    assertEquals("byte", Arg.Type.Byte.toString());
    whenValidValue$thenParsedValueReturned(Arg.Type.Byte, (byte) 3, "3");
    whenValidValue$thenParsedValueReturned(Arg.Type.ByteArray, new Object[] { (byte) 1, (byte) 2 }, "1", "2");
    whenInvalidValue$thenExceptionThrown(Arg.Type.Byte, InvalidTestException.class, "ZZZ");
  }

  @Test
  public void testChar() {
    assertEquals("char", Arg.Type.Char.toString());
    whenValidValue$thenParsedValueReturned(Arg.Type.Char, 't', "t");
    whenValidValue$thenParsedValueReturned(Arg.Type.CharArray, new Object[] { 'a', 'b', 'c' }, "a", "b", "c");
    whenInvalidValue$thenExceptionThrown(Arg.Type.Char, InvalidTestException.class, "ABC");
  }

  @Test
  public void testShort() {
    assertEquals("short", Arg.Type.Short.toString());
    whenValidValue$thenParsedValueReturned(Arg.Type.Short, (short) 3, "3");
    whenValidValue$thenParsedValueReturned(Arg.Type.ShortArray, new Object[] { (short) 1, (short) 2 }, "1", "2");
    whenInvalidValue$thenExceptionThrown(Arg.Type.Short, InvalidTestException.class, "ZZZ");
  }

  @Test
  public void testInt() {
    assertEquals("int", Arg.Type.Int.toString());
    whenValidValue$thenParsedValueReturned(Arg.Type.Int, 3, "3");
    whenValidValue$thenParsedValueReturned(Arg.Type.IntArray, new Object[] { 1, 2 }, "1", "2");
    whenInvalidValue$thenExceptionThrown(Arg.Type.Int, InvalidTestException.class, "ZZZ");
  }

  @Test
  public void testLong() {
    assertEquals("long", Arg.Type.Long.toString());
    whenValidValue$thenParsedValueReturned(Arg.Type.Long, (long) 3, "3");
    whenValidValue$thenParsedValueReturned(Arg.Type.LongArray, new Object[] { (long) 1, (long) 2 }, "1", "2");
    whenInvalidValue$thenExceptionThrown(Arg.Type.Long, InvalidTestException.class, "ZZZ");
  }

  @Test
  public void testFloat() {
    assertEquals("float", Arg.Type.Float.toString());
    whenValidValue$thenParsedValueReturned(Arg.Type.Float, (float) 3.1, "3.1");
    whenValidValue$thenParsedValueReturned(Arg.Type.FloatArray, new Object[] { (float) 1.2, (float) 2.3 }, "1.2", "2.3");
    whenInvalidValue$thenExceptionThrown(Arg.Type.Float, InvalidTestException.class, "ZZZ");
  }

  @Test
  public void testDouble() {
    assertEquals("double", Arg.Type.Double.toString());
    whenValidValue$thenParsedValueReturned(Arg.Type.Double, 3.1, "3.1");
    whenValidValue$thenParsedValueReturned(Arg.Type.DoubleArray, new Object[] { 1.2, 2.3 }, "1.2", "2.3");
    whenInvalidValue$thenExceptionThrown(Arg.Type.Double, InvalidTestException.class, "ZZZ");
  }

  @Test
  public void testString() {
    assertEquals("String", Arg.Type.String.toString());
    whenValidValue$thenParsedValueReturned(Arg.Type.String, "hello", "hello");
    whenValidValue$thenParsedValueReturned(Arg.Type.StringArray, new Object[] { "Hello", "World" }, "Hello", "World");
  }

  public void whenValidValue$thenParsedValueReturned(Arg.Type type, Object expected, String... value) {
    assertEquals(expected, type.parse(value));
  }

  public void whenValidValue$thenParsedValueReturned(Arg.Type type, Object[] expected, String... value) {
    assertArrayEquals(expected, (Object[]) type.parse(value));
  }

  public void whenInvalidValue$thenExceptionThrown(Arg.Type type, Class<? extends Throwable> expectedException, String... value) {
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

  public static Arg p1 = new Arg() {
    @Override
    public String[] value() {
      return new String[] { "true" };
    }

    @Override
    public Class<? extends Annotation> annotationType() {
      return Arg.class;
    }
  };

  @Test
  public void processParamsTest1() {
    Object[] processed = Arg.Type.processParams(new Arg.Type[] { Arg.Type.Boolean }, new Arg[] { p1 });
    assertArrayEquals(new Object[] { Boolean.TRUE }, processed);
  }

  @Test
  public void processParamsTest2() {
    Object[] processed = Arg.Type.processParams(new Arg.Type[] { Arg.Type.Boolean.withDefaultValue(true) }, new Arg[] {});
    assertArrayEquals(new Object[] { Boolean.TRUE }, processed);
  }

  @Test
  public void processParamsTest3() {
    Object[] processed = Arg.Type.processParams(new Arg.Type[] { Arg.Type.Boolean.withDefaultValue(true), Arg.Type.Boolean.withDefaultValue(false) }, new Arg[] {});
    assertArrayEquals(new Object[] { Boolean.TRUE, Boolean.FALSE }, processed);
  }

  @Test
  public void processParamsTest4() {
    Object[] processed = Arg.Type.processParams(new Arg.Type[] { Arg.Type.Boolean.withVarArgsEnabled() }, new Arg[] { p1 });
    assertArrayEquals(new Object[] { Boolean.TRUE }, processed);
  }

  @Test
  public void processParamsTest5() {
    Object[] processed = Arg.Type.processParams(new Arg.Type[] { Arg.Type.Boolean.withVarArgsEnabled() }, new Arg[] { p1, p1 });
    assertArrayEquals(new Object[] { Boolean.TRUE, Boolean.TRUE }, processed);
  }

  @Test(expected = InvalidTestException.class)
  public void processParamsTest6$tooLittleParameters() {
    Object[] processed = Arg.Type.processParams(new Arg.Type[] { Arg.Type.Boolean, Arg.Type.Boolean.withVarArgsEnabled() }, new Arg[] {});
    assertArrayEquals(new Object[] { Boolean.TRUE, Boolean.TRUE }, processed);
  }

  @Test(expected = InvalidTestException.class)
  public void processParamsTest6$tooManyParameters() {
    Object[] processed = Arg.Type.processParams(new Arg.Type[] { Arg.Type.Boolean, Arg.Type.Boolean }, new Arg[] { p1, p1, p1, p1 });
    assertArrayEquals(new Object[] { Boolean.TRUE, Boolean.TRUE }, processed);
  }

  @Test
  public void processParamsTest$defaultValueAndVarArgsInCombination() {
    Object[] processed = Arg.Type.processParams(new Arg.Type[] { Arg.Type.Boolean.withDefaultValue(false), Arg.Type.Boolean.withVarArgsEnabled() }, new Arg[] {});
    assertArrayEquals(new Object[] { Boolean.FALSE }, processed);
  }

  @Test(expected = InvalidPluginException.class)
  public void processParamsTest$invalidDefaultNotAtLast() {
    Object[] processed = Arg.Type.processParams(new Arg.Type[] { Arg.Type.Boolean.withDefaultValue(false), Arg.Type.Boolean }, new Arg[] {});
    assertArrayEquals(new Object[] { Boolean.FALSE }, processed);
  }

  @Test(expected = InvalidPluginException.class)
  public void processParamsTest$invalidVarargsNotAtLast() {
    Object[] processed = Arg.Type.processParams(new Arg.Type[] { Arg.Type.Boolean.withVarArgsEnabled(), Arg.Type.Boolean }, new Arg[] { p1, p1 });
  }

}
