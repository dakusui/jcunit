package com.github.dakusui.jcunit.standardrunner;

import com.github.dakusui.jcunit.core.*;
import com.github.dakusui.jcunit.core.reflect.ReflectionUtils;
import org.junit.runner.Description;

import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

/**
 * A 'recorder' class which stores test execution information in a local file system.
 * The default directory is {@code .jcunit} under the current directory.
 * This class records 'Generated' test cases and 'Custom' and 'Violation' test cases are ignored.
 * <p/>
 * This rule should be used in a test class annotated with {@literal @}{@code RunWith(JCUnit.class)}.
 * <p/>
 * An exception thrown while test execution, a test case, field annotated with {@literal @}Record in a test class are
 * saved in the directory structure below.
 * <p/>
 * <pre>
 *     {basedir}/                          ... baseDir
 *         {FQCN}/                         ... test class dataDir
 *             data-{testcase id}/         ... test case dataDir
 *                 testcase.ser
 *                 failed
 *                 {test method name}/     ... test dataDir
 *                   exception.ser
 *                   {field name}.ser
 * </pre>
 * <p/>
 * , where <ul>
 * <li>{basedir} is a base directory of JCUnit, which can be configured by a system property {@code "jcunit.basedir"}.</li>
 * <li>{FQCN} is an FQCN of the test class.</li>
 * <li>{testcase id} is an integer which identifies a test case tuple executed by a test class.</li>
 * <li>{test method name} is a name of a test method.</li>
 * <li>'testcase.ser' is a file which contains serialized test case (binary file).</li>
 * <li>'failed' is an empty file. If one or more test methods, this file will be created.</li>
 * <li>'exception.ser' is a file which contains serialized exception thrown during the execution of a test method.</li>
 * </ul>
 * This class doesn't do anything in case a system property {@code jcunit.recorder} isn't
 * set {@code true}.
 */
public class Recorder extends JCUnitRule {
  public static final String TESTCASE_FILENAME  = "testcase.ser";
  public static final String EXCEPTION_FILENAME = "exception.ser";
  public static final String FAILED_FILENAME    = "failed";
  private final String baseDir;

  private boolean initialized = false;
  private File testDataDir;

  /**
   * Creates an object of this class with {@code null} base directory, which makes
   * JCUnit use the value System.getProperty("jcunit.basedir")
   * as the base directory to store test execution data.
   */
  public Recorder() {
    this(SystemProperties.jcunitBaseDir().getAbsolutePath());
  }

  /**
   * Creates an object of this class with {@code baseDir}.
   *
   * @param baseDir base directory of test execution data.
   */
  public Recorder(String baseDir) {
    this.baseDir = baseDir;
  }

  protected static File testDataDirFor(String baseDir, int id, Description d) {
    return new File(testCaseDataDirFor(baseDir, id, d.getTestClass()), d.getMethodName());
  }

  protected static File testCaseDataDirFor(String baseDir, int id, Class<?> testClass) {
    return new File(testClassDataDirFor(baseDir, testClass),
        String.format("data-%d", id));
  }


  /**
   * Returns a data directory for a given test class.
   * If {@code parentDirectory} is null, the value set to the system property,
   * The value {@code SystemProperties.jcunitBaseDir()} returns will be used.
   */
  public static File testClassDataDirFor(String baseDir,
      Class<?> testClass) {
    Checks.checknotnull(testClass);
    File parentDir;
    if (baseDir == null) {
      parentDir = SystemProperties.jcunitBaseDir();
    } else {
      parentDir = new File(baseDir);
    }
    return new File(parentDir, testClass.getCanonicalName());
  }

  protected void setTestDataDir(File dir) {
    Checks.checknotnull(dir);
    this.testDataDir = dir;
  }

  public static void initializeTestClassDataDir(Class<?> testClass) {
    initializeTestClassDataDir(null, testClass);
  }

  public static void initializeTestClassDataDir(String baseDir,
      Class<?> testClass) {
    if (SystemProperties.isRecorderEnabled()) {
      Checks.checknotnull(testClass);
      File testClassBaseDir = testClassDataDirFor(baseDir, testClass);
      if (testClassBaseDir.exists()) {
        IOUtils.deleteRecursive(testClassBaseDir);
      }
    }
  }

  @Override
  protected void starting(Description d) {
    super.starting(d);
    if (SystemProperties.isRecorderEnabled() && this.getTestCaseType() == JCUnit.TestCaseType.Generated) {
      this.setTestDataDir(testDataDirFor(this.baseDir, this.getId(), d));

      synchronized (Recorder.class) {
        if (this.testDataDir.exists()) {
          IOUtils.deleteRecursive(this.testDataDir);
        }
        boolean dirCreated = this.testDataDir.mkdirs();
        Checks.checkcond(dirCreated);
        IOUtils.save(this.getTestCase(),
            new File(testDataDir.getParentFile(), TESTCASE_FILENAME));
      }
    }
    this.initialized = true;
  }

  @Override
  protected void failed(Throwable t, Description d) {
    Checks.checkcond(this.initialized);
    if (SystemProperties.isRecorderEnabled() && this.getTestCaseType() == JCUnit.TestCaseType.Generated) {
      Checks.checkcond(this.testDataDir != null);
      IOUtils.save(t, new File(testDataDir, EXCEPTION_FILENAME));
      ////
      // Create a file 'failed', which tells framework that this test case contains
      // at least one failed test.
      IOUtils.createFile(new File(testDataDir.getParentFile(), FAILED_FILENAME));
    }
    super.failed(t, d);
  }

  public <T> void save(T obj) {
    Checks.checkcond(this.initialized);
    if (SystemProperties.isRecorderEnabled() && this.getTestCaseType() == JCUnit.TestCaseType.Generated) {
      for (Field f : ReflectionUtils
          .getAnnotatedFields(obj.getClass(), Recorder.Record.class)) {
        IOUtils.save(ReflectionUtils.getFieldValue(obj, f), new File(testDataDir, f.getName()));
      }
    }
  }

  @SuppressWarnings("unchecked")
  public <T> T load() {
    Checks.checkcond(this.initialized);
    T ret = null;
    List<String> fieldsNotFoundInStore = new LinkedList<String>();
    if (SystemProperties.isRecorderEnabled() && this.getTestCaseType() == JCUnit.TestCaseType.Generated) {
      ret = (T) ReflectionUtils.create(getTestClass());
      for (Field f : ReflectionUtils
          .getAnnotatedFields(getTestClass(), Recorder.Record.class)) {
        File file = new File(testDataDir, f.getName());
        if (!file.exists()) {
          fieldsNotFoundInStore.add(f.getName());
          continue;
        }
        ReflectionUtils.setFieldValue(ret, f, IOUtils.load(file));
      }
    }
    Checks.checkcond(fieldsNotFoundInStore.isEmpty(),
        "%s: These field(s) are not stored. Maybe you should set system property '%s' true and re-run this test.",
        fieldsNotFoundInStore,
        SystemProperties.KEY.RECORDER.key()
    );
    return ret;
  }

  public String getBaseDir() {
    return baseDir;
  }

  @Target(ElementType.FIELD)
  @Retention(RetentionPolicy.RUNTIME)
  public @interface Record {
  }
}
