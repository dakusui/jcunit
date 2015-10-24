package com.github.dakusui.jcunit.tests.core;

import com.github.dakusui.jcunit.runners.standard.annotations.FactorField;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.reflect.ReflectionUtils;
import com.github.dakusui.jcunit.exceptions.InvalidTestException;
import org.junit.Test;

import static org.junit.Assert.*;

public class FactorLoaderTest {

  private final TestFactors testFactors = new TestFactors();

  @Test
  public void validIntFieldWithDefaultValues() throws NoSuchFieldException {
    String fieldName = "validIntFieldWithDefaultValues";
    Factor f = createFactor(fieldName);
    assertArrayEquals(new Object[] { 1, 0, -1, 100, -100, Integer.MAX_VALUE,
        Integer.MIN_VALUE }, f.levels.toArray());
  }

  @Test
  public void validIntFieldWithExplicitValues() throws NoSuchFieldException {
    Factor f = createFactor("validIntFieldWithExplicitIntValues");
    assertArrayEquals(new Object[] { 1, 2, 3 }, f.levels.toArray());
  }

  @Test(expected = InvalidTestException.class)
  public void invalidIntFieldWithExplicitLongValues() throws Exception {
    // Validation happens inside createFactor.
    createFactor("invalidIntFieldWithExplicitLongValues");
  }

  @Test
  public void validateValidField1() throws Exception {
    Factor f = createFactor("validIntFieldWithDefaultValues");
    assertEquals(1, f.levels.get(0));
    assertEquals(7, f.levels.size());
  }

  @Test
  public void validateValidField2() throws Exception {
    Factor f = createFactor("validIntFieldWithExplicitIntValues");
    assertEquals(1, f.levels.get(0));
    assertEquals(3, f.levels.size());
  }

  @Test(expected = InvalidTestException.class)
  public void validateInvalidField1() throws Exception {
    // Validation happens inside createFactor.
    createFactor("invalidIntFieldWithExplicitLongValues");
  }

  @Test(expected = InvalidTestException.class)
  public void validateInvalidField2() throws Exception {
    // This must fail because assignment is incompatible.
    FactorField.FactorFactory.INSTANCE.createFromField(this.testFactors.getClass().getField("invalidIntFieldWithExplicitLongValues"));
    fail();
  }


  @Test
  public void validateValidBooleanFieldIncludingNull()  {
    Factor f = createFactor("validBooleanFieldIncludingNull");
    assertEquals(3, f.levels.size());
    assertEquals(true, f.levels.get(0));
    assertEquals(false, f.levels.get(1));
    assertEquals(null, f.levels.get(2));
  }

  @Test(expected = InvalidTestException.class)
  public void validateInvalidBooleanFieldIncludingNull()  {
    createFactor("invalidBooleanFieldIncludingNull");
  }
  private Factor createFactor(String fieldName) {
    return FactorField.FactorFactory.INSTANCE.createFromField(ReflectionUtils.getField(this.testFactors.getClass(), fieldName));
  }

}
