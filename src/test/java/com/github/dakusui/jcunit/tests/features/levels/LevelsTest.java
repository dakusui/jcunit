package com.github.dakusui.jcunit.tests.features.levels;

import com.github.dakusui.jcunit.runners.standard.JCUnit;
import com.github.dakusui.jcunit.runners.standard.annotations.FactorField;
import com.github.dakusui.jcunit.runners.standard.annotations.FactorField.Source;
import com.github.dakusui.jcunit.testutils.UTUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;

import java.util.Date;

import static org.junit.Assert.assertEquals;

public class LevelsTest {
  @RunWith(JCUnit.class)
  public static class LevelsTest1 {
    /**
     * This method is accessed by JCUnit reflectively.
     */
    public static Date[] dateSource() {
      return new Date[] {
          new Date(1)
      };
    }

    /**
     * This field is accessed by JCUnit reflectively.
     */
    @SuppressWarnings("unused")
    public static Date[] dateSource = new Date[] { new Date(2), new Date(3) };

    @FactorField(from = { @Source("dateSource"), @Source(type = Source.Type.STATIC_FIELD, value = "dateSource") })
    public Date date;

    @BeforeClass
    static public void beforeAll() {
      UTUtils.configureStdIOs();
    }
    @Test
    public void test() {
      System.out.println(date);
    }
  }

  @Test
  public void testConfiguredLevelsProvider() {
    Result result = JUnitCore.runClasses(LevelsTest1.class);
    assertEquals(true, result.wasSuccessful());
    assertEquals(3, result.getRunCount());
    assertEquals(0, result.getIgnoreCount());
    assertEquals(0, result.getFailureCount());
  }
}
