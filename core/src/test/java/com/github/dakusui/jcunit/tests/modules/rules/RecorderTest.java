package com.github.dakusui.jcunit.tests.modules.rules;

import com.github.dakusui.jcunit.core.utils.SystemProperties;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.framework.TestCase;
import com.github.dakusui.jcunit.runners.standard.InternalAnnotation;
import com.github.dakusui.jcunit.runners.standard.JCUnit;
import com.github.dakusui.jcunit.runners.standard.rules.Recorder;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RecorderTest extends Recorder implements Serializable {
  @SuppressWarnings("unused")
  @Record
  public int recordedField = -1024;

  @Mock
  public Description        description;
  @Mock
  public InternalAnnotation ann;

  Class<?>          testClass = RecorderTest.class;
  Factors           factors   = new Factors.Builder()
      .add(new Factor.Builder("f1").addLevel(1).build()).build();
  Tuple             tuple     = new Tuple.Builder().build();
  TestCase.Category category  = TestCase.Category.REGULAR;
  TestCase          testCase  = new JCUnit.NumberedTestCase(123, this.category, this.tuple);

  public RecorderTest() {
    super(baseDir().getAbsolutePath());
  }

  private static File baseDir() {
    return new File(System.getProperty("java.io.tmpdir"),
        "jcunittest-" + System.currentTimeMillis());
  }

  @Test
  public void givenRecorderAndReplayerAreSetTrue$whenInitializeDirSaveAndLoad$thenDirectoryIsInitializedObjectsAreSavedAndLoaded()
      throws IOException {
    System.setProperty(SystemProperties.Key.RECORDER.key(), "true");
    System.setProperty(SystemProperties.Key.REPLAYER.key(), "true");
    wireMocks();

    File baseDir = new File(this.getBaseDir());
    assertTrue(baseDir.mkdir() || baseDir.exists());
    baseDir.deleteOnExit();
    assertTrue(baseDir.exists());
    assertTrue(baseDir.isDirectory());

    ////
    // Create an extra file under baseDir.
    File extraFile = File.createTempFile("jcunit", "tmp", baseDir);

    /////////////////////////////////////
    // Perform initialization (1st time)
    Recorder.initializeTestClassDataDir(baseDir.getAbsolutePath(),
        this.getClass());

    ////
    // Make sure the working directory isn't removed.
    assertTrue(baseDir.exists());
    ////
    // Make sure an extra file in the directory isn't removed.
    assertTrue(extraFile.exists());

    ////
    // Perform 'starting' method.
    this.starting(this.description);
    File testDataDir = testDataDirFor(baseDir.getAbsolutePath(), this.getTestCase().getId(),
        this.description);

    /////////////////////////////////////
    // Make sure exception is saved.
    File exception = new File(testDataDir, "exception.ser");
    assertTrue(!exception.exists());
    this.failed(new Exception(), this.description);
    assertTrue(exception.exists());

    /////////////////////////////////////
    // Save '@Record' field.
    this.save(this);

    ////
    // Make sure file is created.
    File recordedFile = new File(testDataDir, "recordedField");
    assertTrue(recordedFile.exists());

    /////////////////////////////////////
    // Load '@Record' field.
    RecorderTest loaded = this.load();
    ////
    // Make sure the value 'recordedField' is restored.
    assertEquals(this.recordedField, loaded.recordedField);

    /////////////////////////////////////
    // Perform initialization (2nd time)
    Recorder.initializeTestClassDataDir(baseDir.getAbsolutePath(),
        this.getClass());
    ////
    // Make sure the working directory isn't removed.
    assertTrue(baseDir.exists());
    ////
    // Make sure an extra file in the directory isn't removed.
    assertTrue(extraFile.exists());
    ////
    // Make sure file is removed.
    assertTrue(!recordedFile.exists());
  }

  @Test
  public void givenRecorderAndReplayerAreSetFalse$whenInitializeDirSaveAndLoad$thenDoNotWriteAnything()
      throws IOException {
    System.setProperty(SystemProperties.Key.RECORDER.key(), "false");
    System.setProperty(SystemProperties.Key.REPLAYER.key(), "false");
    wireMocks();
    whenInitializeDirSaveAndLoad$thenDoNotWriteAnything(true);
  }

  @Test
  public void givenTestCaseTypeIsNotGenerated$whenInitializeDirSaveAndLoad$thenDoNotWriteAnything()
      throws IOException {
    System.setProperty(SystemProperties.Key.RECORDER.key(), "true");
    System.setProperty(SystemProperties.Key.REPLAYER.key(), "true");
    this.category = TestCase.Category.CUSTOM;
    this.testCase  = new JCUnit.NumberedTestCase(123, this.category, this.tuple);
    wireMocks();
    whenInitializeDirSaveAndLoad$thenDoNotWriteAnything(false);
  }

  @Test
  public void testDirectoryMethods() {
    wireMocks();
    assertEquals(
        new File(
            String.format("testbase/%s", this.getClass().getCanonicalName())),
        Recorder.testClassDataDirFor("testbase", this.getClass())
    );
    assertEquals(
        new File(String.format("testbase/%s/data-123",
            this.getClass().getCanonicalName())),
        Recorder.testCaseDataDirFor("testbase", 123, this.getClass())
    );
    assertEquals(
        new File(String.format("testbase/%s/data-123/methodName",
            this.getClass().getCanonicalName())),
        Recorder.testDataDirFor("testbase", 123, this.description)
    );
  }

  private void wireMocks() {
    //noinspection unchecked
    when(description.getTestClass()).thenReturn((Class) testClass);
    when(description.getAnnotation(InternalAnnotation.class))
        .thenReturn(ann);
    when(description.getMethodName()).thenReturn("methodName");
    when(ann.getTestCase()).thenReturn((JCUnit.NumberedTestCase) this.testCase);
    when(ann.getFactors()).thenReturn(factors);
  }

  private void whenInitializeDirSaveAndLoad$thenDoNotWriteAnything(
      boolean checkGarbage) throws IOException {
    File baseDir = new File(this.getBaseDir());
    this.starting(this.description);

    ////
    // Create a garbage file.
    File testDataDir = testDataDirFor(baseDir.getAbsolutePath(), this.getTestCase().getId(),
        this.description);
    assertTrue(testDataDir.mkdirs() || testDataDir.exists());
    File garbage = new File(testDataDir, "garbage");
    assertTrue(garbage.createNewFile());

    /////////////////////////////////////
    // Perform initialization (1st time)
    Recorder.initializeTestClassDataDir(baseDir.getAbsolutePath(),
        this.getClass());
    if (checkGarbage) {
      // Make sure garbage isn't removed if it is specified so.
      // Initialization should be skipped.
      assertTrue(garbage.exists());
    } else {
      // Make sure if garbage exists  initialization removes
      // the directory.
      assertFalse(garbage.exists());
    }
    ////
    // The expectation is NOT to write anything. Therefore even if the directory
    // doesn't exist, this line should succeed.
    this.save(this);
    ////
    // Make sure file is NOT created.
    File recordedFile = new File(testDataDir, "recordedField");
    assertTrue(!recordedFile.exists());
    ////
    // Make sure null is returned.
    assertNull(this.load());
  }

}
