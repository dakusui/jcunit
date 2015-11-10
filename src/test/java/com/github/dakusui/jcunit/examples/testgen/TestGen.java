package com.github.dakusui.jcunit.examples.testgen;

import com.github.dakusui.jcunit.runners.standard.annotations.FactorField;
import com.github.dakusui.jcunit.runners.standard.JCUnit;
import com.github.dakusui.jcunit.runners.standard.TestCaseUtils;
import com.github.dakusui.jcunit.core.tuples.TupleUtils;
import com.github.dakusui.jcunit.testutils.UTUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JCUnit.class)
public class TestGen {
  @FactorField(stringLevels = { "Starter", "Home Basic", "Home Premium", "Professional", "Enterprise", "Ultimate" })
  public String edition;
  @FactorField(intLevels = { 1, 2, 4, 8 })
  public int    ramInGB;
  @FactorField(intLevels = { 64, 128, 256, 512 })
  public int    gramInMB;
  @FactorField(intLevels = { 20, 30, 50, 100 })
  public int    hddSizeInGB;
  @FactorField(floatLevels = { 1.5f, 2, 2.5f, 3 })
  public float  cpuClockInGHz;
  @FactorField(stringLevels = { "IE", "Firefox", "Opera", "Safari", "Chrome" })
  public String browser;

  @Before
  public void configureStdIOs() {
    UTUtils.configureStdIOs();
  }

  @Test
  public void printTestCase() {
    UTUtils.stdout().println(TupleUtils.toString(TestCaseUtils.toTestCase(this)));
  }
}
