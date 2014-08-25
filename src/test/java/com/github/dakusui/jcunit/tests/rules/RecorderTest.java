package com.github.dakusui.jcunit.framework.rules;

import com.github.dakusui.jcunit.core.JCUnit;
import com.github.dakusui.jcunit.core.SystemProperties;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.rules.Recorder;
import com.github.dakusui.jcunit.core.tuples.Tuple;
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
  public Description               description;
  @Mock
  public JCUnit.InternalAnnotation ann;

  Class<?>            testClass = RecorderTest.class;
  Factors             factors   = new Factors.Builder()
      .add(new Factor.Builder().setName("f1").addLevel(1).build()).build();
  Tuple               tuple     = new Tuple.Builder().build();
  JCUnit.TestCaseType type      = JCUnit.TestCaseType.Generated;

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
    System.setProperty(SystemProperties.KEY.RECORDER.key(), "true");
    System.setProperty(SystemProperties.KEY.REPLAYER.key(), "true");
    wireMocks();

    File baseDir = new File(this.getBaseDir());
    baseDir.mkdir();
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
    File testDataDir = testDataDirFor(baseDir.getAbsolutePath(), this.getId(),
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
    System.setProperty(SystemProperties.KEY.RECORDER.key(), "false");
    System.setProperty(SystemProperties.KEY.REPLAYER.key(), "false");
    wireMocks();
    whenInitializeDirSaveAndLoad$thenDoNotWriteAnything(true);
  }

  @Test
  public void givenTestCaseTypeIsNotGenerated$whenInitializeDirSaveAndLoad$thenDoNotWriteAnything()
      throws IOException {
    System.setProperty(SystemProperties.KEY.RECORDER.key(), "true");
    System.setProperty(SystemProperties.KEY.REPLAYER.key(), "true");
    this.type = JCUnit.TestCaseType.Custom;
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
    when(description.getTestClass()).thenReturn((Class) testClass);
    when(description.getAnnotation(JCUnit.InternalAnnotation.class))
        .thenReturn(ann);
    when(description.getMethodName()).thenReturn("methodName");
    when(ann.getTestCase()).thenReturn(tuple);
    when(ann.getTestCaseType()).thenReturn(type);
    when(ann.getId()).thenReturn(123);
    when(ann.getFactors()).thenReturn(factors);
  }

  private void whenInitializeDirSaveAndLoad$thenDoNotWriteAnything(
      boolean checkGarbage) throws IOException {
    File baseDir = new File(this.getBaseDir());
    this.starting(this.description);

    ////
    // Create a garbage file.
    File testDataDir = testDataDirFor(baseDir.getAbsolutePath(), this.getId(),
        this.description);
    testDataDir.mkdirs();
    File garbage = new File(testDataDir, "garbage");
    garbage.createNewFile();

    /////////////////////////////////////
    // Perform initialization (1st time)
    Recorder.initializeTestClassDataDir(baseDir.getAbsolutePath(),
        this.getClass());
    ////
    // Make sure garbage isn't removed.
    if (checkGarbage) {
      assertTrue(garbage.exists());
    }

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
