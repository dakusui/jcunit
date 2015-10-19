package com.github.dakusui.jcunit.tests.plugins;

import com.github.dakusui.jcunit.core.FactorField;
import com.github.dakusui.jcunit.core.factor.DefaultLevelsProvider;
import com.github.dakusui.jcunit.exceptions.InvalidTestException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Spy;

import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;

public class DefaultLevelsProviderTest {
  private DefaultLevelsProvider levelsProvider;

  @Before
  public void before() {
    this.levelsProvider = new DefaultLevelsProvider();
  }

  @FactorField(intLevels = { 1, 2, 3 }, shortLevels = { 100, 200, 300 })
  public int f1;

  @SuppressWarnings("unused")
  @FactorField(includeNull = true, stringLevels = {"Hello"})
  public String f2;

  @SuppressWarnings("unused")
  @FactorField(includeNull = true)
  public RetentionPolicy f3;

  @Test(expected = InvalidTestException.class)
  public void test() throws NoSuchFieldException {
    Field f1 = this.getClass().getField("f1");
    FactorField ann = f1.getAnnotation(FactorField.class);
    this.levelsProvider.setAnnotation(ann);
    this.levelsProvider.setTargetField(f1);
    this.levelsProvider.init(new Object[] { });
  }

  @Test
  public void test2() throws NoSuchFieldException {
    Field f1 = this.getClass().getField("f2");
    FactorField ann = f1.getAnnotation(FactorField.class);
    this.levelsProvider.setAnnotation(ann);
    this.levelsProvider.setTargetField(f1);
    this.levelsProvider.init(new Object[] { });
    assertEquals(2, this.levelsProvider.size());
    assertEquals("Hello", this.levelsProvider.get(0));
    assertEquals(null, this.levelsProvider.get(1));
  }

  @Test
  public void test3() throws NoSuchFieldException {
    Field f1 = this.getClass().getField("f3");
    FactorField ann = f1.getAnnotation(FactorField.class);
    this.levelsProvider.setAnnotation(ann);
    this.levelsProvider.setTargetField(f1);
    this.levelsProvider.init(new Object[] { });
    assertEquals(RetentionPolicy.values().length + 1, this.levelsProvider.size());
    assertEquals(null, this.levelsProvider.get(RetentionPolicy.values().length));
  }
}
