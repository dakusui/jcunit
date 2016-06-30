package com.github.dakusui.jcunit.tests.modules.core;

import com.github.dakusui.jcunit.runners.standard.annotations.FactorField;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Since the default value of each typeLevels method is very self descriptive
 * in FactorField class, safeCheck only length of the returned array is larger than
 * zero.
 */
public class FactorFieldTest {
  @Test
  public void testBooleanLevels() {
    assertTrue(FactorField.DefaultLevels.booleanLevels().length > 0);
  }

  @Test
  public void testByteLevels() {
    assertTrue(FactorField.DefaultLevels.byteLevels().length > 0);
  }

  @Test
  public void testCharLevels() {
    assertTrue(FactorField.DefaultLevels.charLevels().length > 0);
  }

  @Test
  public void testShortLevels() {
    assertTrue(FactorField.DefaultLevels.shortLevels().length > 0);
  }

  @Test
  public void testIntLevels() {
    assertTrue(FactorField.DefaultLevels.intLevels().length > 0);
  }

  @Test
  public void testLongLevels() {
    assertTrue(FactorField.DefaultLevels.longLevels().length > 0);
  }

  @Test
  public void testFloatLevels() {
    assertTrue(FactorField.DefaultLevels.floatLevels().length > 0);
  }

  @Test
  public void testDoubleLevels() {
    assertTrue(FactorField.DefaultLevels.doubleLevels().length > 0);
  }

  @Test
  public void testStringLevels() {
    assertTrue(FactorField.DefaultLevels.stringLevels().length > 0);
  }
}
