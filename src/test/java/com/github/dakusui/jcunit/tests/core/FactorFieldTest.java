package com.github.dakusui.jcunit.tests.core;

import com.github.dakusui.jcunit.core.FactorField;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Since the default value of each typeLevels method is very self descriptive
 * in FactorField class, check only length of the returned array is larger than
 * zero.
 */
public class FactorFieldTest {
  @Test
  public void testBooleanLevels() {
    assertTrue(FactorField.DefaultValues.booleanLevels().length > 0);
  }

  @Test
  public void testByteLevels() {
    assertTrue(FactorField.DefaultValues.byteLevels().length > 0);
  }

  @Test
  public void testCharLevels() {
    assertTrue(FactorField.DefaultValues.charLevels().length > 0);
  }

  @Test
  public void testShortLevels() {
    assertTrue(FactorField.DefaultValues.shortLevels().length > 0);
  }

  @Test
  public void testIntLevels() {
    assertTrue(FactorField.DefaultValues.intLevels().length > 0);
  }

  @Test
  public void testLongLevels() {
    assertTrue(FactorField.DefaultValues.longLevels().length > 0);
  }

  @Test
  public void testFloatLevels() {
    assertTrue(FactorField.DefaultValues.floatLevels().length > 0);
  }

  @Test
  public void testDoubleLevels() {
    assertTrue(FactorField.DefaultValues.doubleLevels().length > 0);
  }

  @Test
  public void testStringLevels() {
    assertTrue(FactorField.DefaultValues.stringLevels().length > 0);
  }

  @Test
  public void testEnumLevels() {
    assertTrue(Enum.class.equals(FactorField.DefaultValues.enumLevels()));
  }

}
