package com.github.dakusui.jcunit.refined;

import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.FactorField;
import com.github.dakusui.jcunit.core.factor.FactorLoader;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

public class FactorLoaderTest {
  @FactorField
  public int intField;
  @FactorField(intLevels = {1,2,3})
  public int intField2;

  @Test
  public void loadFactor() throws NoSuchFieldException {
    FactorLoader factorLoader = new FactorLoader(this.getClass().getField("intField"));
    Factor f = factorLoader.getFactor();
    assertArrayEquals(new Object[] { 1, 0, -1, 100, -100, Integer.MAX_VALUE,
            Integer.MIN_VALUE }, f.levels.toArray());
  }

  @Test
  public void loadFactor2() throws NoSuchFieldException {
    FactorLoader factorLoader = new FactorLoader(this.getClass().getField("intField2"));
    Factor f = factorLoader.getFactor();
    assertArrayEquals(new Object[] { 1, 2, 3 }, f.levels.toArray());
  }

  @Test
  public void validate() throws Exception {
    FactorLoader factorLoader = new FactorLoader(this.getClass().getField("intField"));
    FactorLoader.ValidationResult result = factorLoader.validate();
    assertTrue(result.isValid());
  }
}
