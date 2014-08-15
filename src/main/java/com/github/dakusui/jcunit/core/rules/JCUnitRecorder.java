package com.github.dakusui.jcunit.core.rules;

import com.github.dakusui.jcunit.core.JCUnit;
import com.github.dakusui.jcunit.core.SystemProperties;
import com.github.dakusui.jcunit.core.Utils;
import org.junit.runner.Description;

import java.io.File;

/**
 * A 'recorder' class which stores test execution information in a local file system.
 * The default directory is {@code .jcunit} under the current directory.
 *
 * This class doesn't do anything in case a system property {@code jcunit.recorder} isn't
 * set {@codde true}.
 */
public class JCUnitRecorder extends JCUnitRule {
  public static final String TESTCASE_FILENAME  = "testcase.ser";
  public static final String EXCEPTION_FILENAME = "exception.ser";
  public static final String FAILED_FILENAME    = "failed";
  private final String baseDir;

  private File dir;

  public JCUnitRecorder() {
    this(null);
  }

  public JCUnitRecorder(String baseDir) {
    this.baseDir = baseDir;
  }

  private static File getDir(String baseDir, int id, Description d) {
    return new File(Utils.baseDirFor(baseDir, d.getTestClass()),
        String.format("data-%d/%s", id, d.getMethodName()));
  }

  public static void initializeDir(Class<?> testClass) {
    initializeDir(null, testClass);
  }

  public static void initializeDir(String baseDir, Class<?> testClass) {
    if (SystemProperties.isRecorderEnabled()) {
      Utils.checknotnull(testClass);
      File testClassBaseDir = Utils.baseDirFor(baseDir, testClass);
      if (testClassBaseDir.exists()) {
        Utils.deleteRecursive(testClassBaseDir);
      }
    }
  }

  @Override
  protected void starting(Description d) {
    super.starting(d);
    if (SystemProperties.isRecorderEnabled()) {
      if (this.getTestCaseType() == JCUnit.TestCaseType.Generated) {
        this.dir = getDir(this.baseDir, this.getId(), d);
        if (this.dir.exists()) {
          Utils.deleteRecursive(this.dir);
        }
        synchronized (JCUnitRecorder.class) {
          boolean dirCreated = this.dir.mkdirs();
          Utils.checkcond(dirCreated);
          Utils.save(this.getTestCase(),
              new File(dir.getParentFile(), TESTCASE_FILENAME));
        }
      }
    }
  }

  @Override
  protected void failed(Throwable t, Description d) {
    if (SystemProperties.isRecorderEnabled()) {
      if (this.getTestCaseType() == JCUnit.TestCaseType.Generated) {
        Utils.checkcond(this.dir != null);
        Utils.save(t, new File(dir, EXCEPTION_FILENAME));
      }
      ////
      // Create a file 'failed', which tells framework that this test case contains
      // at least one failed test.
      Utils.createFile(new File(dir.getParentFile(), FAILED_FILENAME));
    }
    super.failed(t, d);
  }
}
