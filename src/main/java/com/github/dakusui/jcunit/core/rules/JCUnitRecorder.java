package com.github.dakusui.jcunit.core.rules;

import com.github.dakusui.jcunit.core.JCUnit;
import com.github.dakusui.jcunit.core.Utils;
import org.junit.runner.Description;

import java.io.File;

public class JCUnitRecorder extends JCUnitRule {
  public static final String TESTCASE_FILENAME  = "testcase.ser";
  public static final String EXCEPTION_FILENAME = "exception.ser";
  private final String baseDir;

  private File dir;

  public JCUnitRecorder() {
    this(null);
  }

  public JCUnitRecorder(String baseDir) {
    this.baseDir = baseDir;
  }

  @Override
  protected void starting(Description d) {
    super.starting(d);
    if (this.getTestCaseType() == JCUnit.TestCaseType.Generated) {
      this.dir = new File(Utils.baseDirFor(this.baseDir, this.getTestClass()),
          String.format("test-%d", this.getId()));
      if (this.dir.exists()) {
        Utils.deleteRecursive(this.dir);
      }
      boolean dirCreated = this.dir.mkdirs();
      Utils.checkcond(dirCreated);
      Utils.save(this.getTestCase(), new File(dir, TESTCASE_FILENAME));
    }
  }

  @Override
  protected void failed(Throwable t, Description d) {
    if (this.getTestCaseType() == JCUnit.TestCaseType.Generated) {
      Utils.checkcond(this.dir != null);
      Utils.save(t, new File(dir, EXCEPTION_FILENAME));
    }
    super.failed(t, d);
  }
}
