package com.github.dakusui.jcunit.core.rules;

import com.github.dakusui.jcunit.core.JCUnit;
import com.github.dakusui.jcunit.core.SystemProperties;
import com.github.dakusui.jcunit.core.Utils;
import org.junit.runner.Description;

import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A 'recorder' class which stores test execution information in a local file system.
 * The default directory is {@code .jcunit} under the current directory.
 *
 * This class doesn't do anything in case a system property {@code jcunit.recorder} isn't
 * set {@code true}.
 */
public class Recorder extends JCUnitRule {
  public static final String TESTCASE_FILENAME  = "testcase.ser";
  public static final String EXCEPTION_FILENAME = "exception.ser";
  public static final String FAILED_FILENAME    = "failed";
  private final String baseDir;

  private File dir;

  /**
   * Creates an object of this class with {@code null} base directory, which makes
   * JCUnit use the value System.getProperty("jcunit.basedir")
   * as the base directory to store test execution data.
   */
  public Recorder() {
    this(null);
  }

  /**
   * Creates an object of this class with {@code baseDir}.
   *
   * @param baseDir base directory of test execution data.
   */
  public Recorder(String baseDir) {
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
        synchronized (Recorder.class) {
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

  @Target(ElementType.FIELD)
  @Retention(RetentionPolicy.RUNTIME)
  public static @interface Record {
  }
}
