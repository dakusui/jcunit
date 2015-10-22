package com.github.dakusui.jcunit.tests.factor;

import com.github.dakusui.jcunit.annotations.FactorField;
import com.github.dakusui.jcunit.core.JCUnit;
import com.github.dakusui.jcunit.core.factor.LevelsProviderBase;
import com.github.dakusui.jcunit.core.factor.MethodLevelsProvider;
import com.github.dakusui.jcunit.ututils.Metatest;
import com.github.dakusui.jcunit.ututils.UTUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.util.Arrays;

public class ArrayFactorLevelTest {
  @Test
  public void testNormal() {
    new Normal().runTests();
  }

  @Test
  public void testDuplicated() {
    new Duplicated().runTests();
  }


  @RunWith(JCUnit.class)
  public static class Normal extends Metatest {
    @FactorField(intLevels = { 1 })
    public int test;

    @FactorField(levelsProvider = MethodLevelsProvider.class, providerParams = {})
    public String[] factor1;

    public Normal() {
      super(2, 0, 0);
    }

    /**
     * This method is used to generate levels for  'factor1'
     */
    @SuppressWarnings("unused")
    public static String[][] factor1() {
      return new String[][] {
          new String[] { "Hello", "world" },
          new String[] { "Howdy" }
      };
    }

    @Before
    public void before() {
      UTUtils.configureStdIOs();
    }

    @org.junit.Test
    public void test() {
      UTUtils.stdout().println(Arrays.toString(factor1));
    }
  }

  @RunWith(JCUnit.class)
  public static class Duplicated extends Metatest {
    @FactorField(intLevels = { 1 })
    public int test;

    @FactorField(levelsProvider = MethodLevelsProvider.class, providerParams = {})
    public String[] factor1;

    static class P extends LevelsProviderBase {

      @Override
      protected void init(Field targetField, Object[] parameters) {
      }

      @Override
      public int size() {
        return 0;
      }

      @Override
      public Object get(int n) {
        return null;
      }
    }

    public Duplicated() {
      super(3, 0, 0);
    }

    /**
     * This method is used to generate levels for  'factor1'
     */
    @SuppressWarnings("unused")
    public static String[][] factor1() {
      return new String[][] {
          new String[] { "Hello", "world" },
          new String[] { "Hello", "world" },
          new String[] { "Howdy" }
      };
    }

    @Before
    public void before() {
      UTUtils.configureStdIOs();
    }

    @Test
    public void test() {
      UTUtils.stdout().println(Arrays.toString(factor1));
    }
  }
}
