package com.github.dakusui.jcunit.tests.core;

import com.github.dakusui.jcunit.core.FactorField;
import com.github.dakusui.jcunit.core.factor.DefaultLevelsProvider;
import com.github.dakusui.jcunit.exceptions.InvalidTestException;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;

public class DefaultLevelsProviderTest {
  private DefaultLevelsProvider levelsProvider;

  @Before
  public void before() {
    this.levelsProvider = new DefaultLevelsProvider();
  }

  @FactorField(intLevels = { 1, 2, 3 }, shortLevels = { 100, 200, 300 })
  public int f1;

  @Test(expected = InvalidTestException.class)
  public void test() throws NoSuchFieldException {
    Field f1 = this.getClass().getField("f1");
    FactorField ann = f1.getAnnotation(FactorField.class);
    this.levelsProvider.setAnnotation(ann);
    this.levelsProvider.setTargetField(f1);
    this.levelsProvider.init(new Object[] { });
  }
}
