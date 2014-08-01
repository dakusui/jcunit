package com.github.dakusui.jcunit.core.rules;

import com.github.dakusui.jcunit.core.Utils;
import org.junit.runner.Description;

import java.io.File;

public class JCUnitRecorder extends JCUnitRule {
  public static final String TESTCASE_FILENAME = "testcase.ser";
  public static final String EXCEPTION_FILENAME = "exception.ser";

  private File dir;

  @Override
  protected void starting(Description d) {
    super.starting(d);
    this.dir = new File(Utils.baseDirFor(this.getTestClass()), this.getTestName());
    this.dir.mkdirs();
    Utils.save(this.getTestCase(), new File(dir, TESTCASE_FILENAME));
  }

  @Override
  protected void failed(Throwable t, Description d) {
    Utils.checkcond(this.dir != null);
    Utils.save(t, new File(dir, EXCEPTION_FILENAME));
    super.failed(t, d);
  }
}
