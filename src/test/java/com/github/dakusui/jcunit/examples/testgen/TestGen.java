package com.github.dakusui.jcunit.examples.testgen;

import com.github.dakusui.jcunit.core.FactorField;
import com.github.dakusui.jcunit.core.JCUnit;
import com.github.dakusui.jcunit.core.TestCaseUtils;
import com.github.dakusui.jcunit.core.tuples.TupleUtils;
import com.github.dakusui.jcunit.ututils.UTUtils;
import org.junit.After;
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
  public void setSilent() {
    UTUtils.setSilent();
  }

  @After
  public void setVerbose() {
    UTUtils.setVerbose();
  }

  @Test
  public void printTestCase() {
    UTUtils.out.println(TupleUtils.toString(TestCaseUtils.toTestCase(this)));
  }
}
