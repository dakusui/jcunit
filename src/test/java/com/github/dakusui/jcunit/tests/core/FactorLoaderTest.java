package com.github.dakusui.jcunit.tests.core;

import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.FactorLoader;
import com.github.dakusui.jcunit.exceptions.InvalidTestException;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.*;

public class FactorLoaderTest {

  private final TestFactors testFactors = new TestFactors();

  @Test
  public void validIntFieldWithDefaultValues() throws NoSuchFieldException {
    FactorLoader factorLoader = new FactorLoader(
        this.testFactors.getClass().getField("validIntFieldWithDefaultValues"));
    Factor f = factorLoader.getFactor();
    assertArrayEquals(new Object[] { 1, 0, -1, 100, -100, Integer.MAX_VALUE,
        Integer.MIN_VALUE }, f.levels.toArray());
  }

  @Test
  public void validIntFieldWithExplicitValues() throws NoSuchFieldException {
    FactorLoader factorLoader = new FactorLoader(
        this.testFactors.getClass().getField("validIntFieldWithExplicitIntValues"));
    Factor f = factorLoader.getFactor();
    assertArrayEquals(new Object[] { 1, 2, 3 }, f.levels.toArray());
  }

  @Test(expected = InvalidTestException.class)
  public void invalidIntFieldWithExplicitLongValues() throws Exception {
    FactorLoader factorLoader = new FactorLoader(this.testFactors.getClass().getField("invalidIntFieldWithExplicitLongValues"));
    // Validation happens inside getFactor.
    factorLoader.getFactor();
  }

  @Test
  public void validateValidField1() throws Exception {
    FactorLoader factorLoader = new FactorLoader(this.testFactors.getClass().getField("validIntFieldWithDefaultValues"));
    Factor f = factorLoader.getFactor();
    assertEquals(1, f.levels.get(0));
    assertEquals(7, f.levels.size());
  }

  @Test
  public void validateValidField2() throws Exception {
    FactorLoader factorLoader = new FactorLoader(this.testFactors.getClass().getField("validIntFieldWithExplicitIntValues"));
    // Validation happens inside getFactor.
    Factor f = factorLoader.getFactor();
    assertEquals(1, f.levels.get(0));
    assertEquals(3, f.levels.size());
  }

  @Test(expected = InvalidTestException.class)
  public void validateInvalidField1() throws Exception {
    Field f = this.testFactors.getClass().getField("invalidIntFieldWithExplicitLongValues");
    FactorLoader factorLoader = new FactorLoader(f);
    // Validation happens inside getFactor.
    factorLoader.getFactor();
  }

  @Test(expected = InvalidTestException.class)
  public void validateInvalidField2() throws Exception {
    FactorLoader factorLoader = new FactorLoader(this.testFactors.getClass().getField("invalidIntFieldWithExplicitLongValues"));
    factorLoader.getFactor();
    fail();
  }
}
