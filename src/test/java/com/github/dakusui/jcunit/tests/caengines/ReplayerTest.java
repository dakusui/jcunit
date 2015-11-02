package com.github.dakusui.jcunit.tests.caengines;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.IOUtils;
import com.github.dakusui.jcunit.core.SystemProperties;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.exceptions.InvalidPluginException;
import com.github.dakusui.jcunit.plugins.caengines.CAEngine;
import com.github.dakusui.jcunit.plugins.caengines.CAEngineBase;
import com.github.dakusui.jcunit.runners.standard.JCUnit;
import com.github.dakusui.jcunit.runners.standard.annotations.FactorField;
import com.github.dakusui.jcunit.runners.standard.annotations.Generator;
import com.github.dakusui.jcunit.runners.standard.annotations.GenerateWith;
import com.github.dakusui.jcunit.runners.standard.annotations.Value;
import com.github.dakusui.jcunit.runners.standard.rules.Recorder;
import com.github.dakusui.jcunit.runners.standard.plugins.Replayer;
import com.github.dakusui.jcunit.ututils.Metatest;
import com.github.dakusui.jcunit.ututils.UTUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

public class ReplayerTest {
  @Before
  public void before() {
    System.setProperty(SystemProperties.KEY.RECORDER.key(), "false");
    System.setProperty(SystemProperties.KEY.REPLAYER.key(), "false");
  }

  @Before
  public void configureStdIOs() {
    UTUtils.configureStdIOs();
  }

  @RunWith(JCUnit.class)
  @GenerateWith(
      generator = @Generator(
          value = Replayer.class,
          args = {
              @Value({ "com.github.dakusui.jcunit.plugins.caengines.IPO2CAEngine", "2" }),
              @Value("Fallback")
          }
      )
  )
  public static class ReplayerOnly extends Metatest {
    public static int f1Threshold = 0;

    @FactorField(intLevels = { 100, 200 })
    public int f1;
    @FactorField(intLevels = 300)
    public int f2;

    public ReplayerOnly() {
      super(2, 0, 0);
    }

    public static Result run() {
      return JUnitCore.runClasses(TestClass.class);
    }

    @Test
    public void test() {
      UTUtils.stdout().println("f1=" + f1 + ", f2=" + f2);
      assertTrue(f1 > f1Threshold);
    }
  }

  @Test
  public void runReplayerOnlyTest() {
    new ReplayerOnly().runTests();
  }

  @RunWith(JCUnit.class)
  @GenerateWith(
      generator = @Generator(
          value = Replayer.class,
          args = {
              @Value({ "com.github.dakusui.jcunit.plugins.caengines.IPO2CAEngine", "2" }) }))
  public static class TestClass {
    public static int      f1Threshold = 0;
    @Rule
    public        Recorder recorder    = new Recorder();

    @FactorField(intLevels = { 100, 200 })
    public int f1;
    @FactorField(intLevels = 300)
    public int f2;

    public static Result run() {
      return JUnitCore.runClasses(TestClass.class);
    }

    @Test
    public void test() {
      UTUtils.stdout().println("f1=" + f1 + ", f2=" + f2);
      assertTrue(f1 > f1Threshold);
    }
  }

  @Test
  public void givenRecorderAndReplayerSetFalse$whenRunTests$thenTestsWillBeRunWithFallbackGenerator() {
    System.setProperty(SystemProperties.KEY.RECORDER.key(), "false");
    System.setProperty(SystemProperties.KEY.REPLAYER.key(), "false");

    Result testResult = runTests();

    assertEquals(2, testResult.getRunCount());
    assertEquals(true, testResult.wasSuccessful());
  }

  @Test
  public void givenRecorderIsFalseAndReplayerIsTrue$whenRunTests$thenTestsWillBeRunWithFallbackGenerator() {
    ////
    // Make sure the directory is empty.
    System.setProperty(SystemProperties.KEY.RECORDER.key(), "true");
    Recorder.initializeTestClassDataDir(TestClass.class);

    System.setProperty(SystemProperties.KEY.RECORDER.key(), "false");
    System.setProperty(SystemProperties.KEY.REPLAYER.key(), "true");

    Result testResult = runTests();

    assertEquals(2, testResult.getRunCount());
    assertEquals(true, testResult.wasSuccessful());
  }

  @Test
  public void givenRecorderAndReplayerSetTrue$whenRunTests$thenRecordedTuplesWillBeReplayed() {
    System.setProperty(SystemProperties.KEY.RECORDER.key(), "true");
    System.setProperty(SystemProperties.KEY.REPLAYER.key(), "true");

    Result testResult = runTests();

    assertEquals(2, testResult.getRunCount());
    assertEquals(true, testResult.wasSuccessful());
  }

  @Test
  public void givenRecorderWasAlreadyExecuted$whenRunTestsWithReplayerEnabled$thenRuplesWillBeReplayed() {
    ////
    // Given:
    {
      System.setProperty(SystemProperties.KEY.RECORDER.key(), "true");
      System.setProperty(SystemProperties.KEY.REPLAYER.key(), "false");
      Recorder.initializeTestClassDataDir(TestClass.class);
      Result testResult = runTests();
      assertEquals(2, testResult.getRunCount());
      assertEquals(true, testResult.wasSuccessful());
    }
    ////
    // When:
    {
      System.setProperty(SystemProperties.KEY.RECORDER.key(), "false");
      System.setProperty(SystemProperties.KEY.REPLAYER.key(), "true");
      Result testResult = runTestsWithoutCleanUp();
      ////
      // Then:
      assertEquals(2, testResult.getRunCount());
      assertEquals(true, testResult.wasSuccessful());
    }
  }

  @Test
  public void givenRecorderIsSetTrueAndReplayerFalse$whenRunTests$thenTestsWillBeRecorded() {
    System.setProperty(SystemProperties.KEY.RECORDER.key(), "true");
    System.setProperty(SystemProperties.KEY.REPLAYER.key(), "false");

    File testClassDataDir = ensureTestDataDirectoryForClassDoesntExist(TestClass.class);

    Result testResult1 = runTests();

    assertTrue(testClassDataDir.exists());

    assertEquals(2, testResult1.getRunCount());
    assertEquals(true, testResult1.wasSuccessful());

    ////
    // Run recorded tests
    System.setProperty(SystemProperties.KEY.RECORDER.key(), "true");
    System.setProperty(SystemProperties.KEY.REPLAYER.key(), "false");

    Result testResult2 = JUnitCore.runClasses(TestClass.class);

    assertEquals(2, testResult2.getRunCount());
    assertEquals(true, testResult2.wasSuccessful());
  }

  @Test
  public void testReplayer() {
    System.setProperty(SystemProperties.KEY.RECORDER.key(), "true");
    System.setProperty(SystemProperties.KEY.REPLAYER.key(), "false");

    File testClassDataDir = ensureTestDataDirectoryForClassDoesntExist(TestClass.class);
    Result testResult = runTests();

    assertTrue(testClassDataDir.exists());
    assertTrue(testResult.wasSuccessful());

    CAEngine caEngine = GenerateWith.CAEngineFactory.INSTANCE
        .createFromClass(TestClass.class);
    assertEquals(Replayer.class, caEngine.getClass());

    Replayer replayer = (Replayer) caEngine;
    assertEquals(2, replayer.size());
    assertEquals(100, replayer.getTuple(0).get("f1"));
    assertEquals(300, replayer.getTuple(0).get("f2"));
    assertEquals(200, replayer.getTuple(1).get("f1"));
    assertEquals(300, replayer.getTuple(1).get("f2"));

  }

  private File ensureTestDataDirectoryForClassDoesntExist(Class testClass) {
    File testClassDataDir = new File(
        String.format(
            "%s/%s",
            SystemProperties.get(SystemProperties.KEY.BASEDIR, ".jcunit"),
            TestClass.class.getCanonicalName()));
    UTUtils.stdout().println(testClassDataDir);
    Checks.checkcond(testClassDataDir.getAbsolutePath().contains(testClass.getCanonicalName()));
    if (testClassDataDir.exists()) {
      IOUtils.deleteRecursive(testClassDataDir);
    }
    assertTrue(!testClassDataDir.exists());
    return testClassDataDir;
  }


  private Result runTests() {
    Recorder.initializeTestClassDataDir(TestClass.class);
    return TestClass.run();
  }

  private Result runTestsWithoutCleanUp() {
    return TestClass.run();
  }

  @RunWith(JCUnit.class)
  @GenerateWith(
      generator = @Generator(
          value = Replayer.class,
          args = {
              @Value({ "com.github.dakusui.jcunit.plugins.caengines.IPO2CAEngine", "2" }),
              @Value("Fallback"),
              @Value("All")
          }
      ))
  public static class TestClass2 {
    @Rule
    public Recorder recorder = new Recorder();
    @SuppressWarnings("unused")
    @FactorField(intLevels = { 100, 200 })
    public int f1;
    @SuppressWarnings("unused")
    @FactorField(intLevels = 300)
    public int f2;

    @Test
    public void test() {
    }
  }

  @RunWith(JCUnit.class)
  @GenerateWith(
      generator = @Generator(
          value = Replayer.class,
          args = {
              @Value({ "com.github.dakusui.jcunit.plugins.caengines.IPO2CAEngine", "2" }),
              @Value("Replay"),
              @Value("All")
          }
      ))
  public static class TestClassNoFallBack extends Metatest {
    @Rule
    public Recorder recorder = new Recorder();
    @SuppressWarnings("unused")
    @FactorField(intLevels = { 100, 200 })
    public int f1;
    @SuppressWarnings("unused")
    @FactorField(intLevels = 300)
    public int f2;

    public TestClassNoFallBack() {
      super(1, 1, 0);
    }

    @Test
    public void test() {
    }
  }


  @Test
  public void givenRecorderIsFalseAndReplayerIsTrueButFallbackIsDisabled$whenRunTests$thenTestsWillBeRunWithFallbackGenerator() {
    ////
    // Make sure the directory is empty.
    System.setProperty(SystemProperties.KEY.RECORDER.key(), "true");
    Recorder.initializeTestClassDataDir(TestClass.class);

    System.setProperty(SystemProperties.KEY.RECORDER.key(), "false");
    System.setProperty(SystemProperties.KEY.REPLAYER.key(), "true");

    Result testResult = new TestClassNoFallBack().runTests();

    validateFailureForMissingPreviousRunRecord(testResult);
  }

  private void validateFailureForMissingPreviousRunRecord(Result testResult) {
    boolean validated = false;
    for (Failure failure : testResult.getFailures()) {
      assertEquals(
          String.format(
              "Test hasn't been run with 'JCUnitRecorder' rule yet. No tuple containing directory under '%s/%s' was found.",
              SystemProperties.get(SystemProperties.KEY.BASEDIR, ".jcunit"),
              TestClassNoFallBack.class.getCanonicalName()
          ),
          failure.getMessage()
      );
      validated = true;
    }
    assertTrue(validated);
  }


  @Test
  public void givenExplicitlyGeneratorClassNameAndStrengthAreSpecified$whenRunTests$thenTestsWillPass() throws Throwable {
    Result result = JUnitCore.runClasses(TestClass2.class);
    assertTrue(result.wasSuccessful());
  }

  @RunWith(JCUnit.class)
  @GenerateWith(
      generator = @Generator(
          value = Replayer.class,
          args = {
              @Value({ "WrongCAEngine", "2" }),
              @Value("Fallback"),
              @Value("All")
          }
      ))
  public static class TestClass3 {
    @Rule
    public Recorder recorder = new Recorder();
    @SuppressWarnings("unused")
    @FactorField(intLevels = { 100, 200 })
    public int f1;
    @SuppressWarnings("unused")
    @FactorField(intLevels = 300)
    public int f2;

    @Test
    public void test() {
    }
  }

  @Test(expected = ClassNotFoundException.class)
  public void givenWrongGeneratorClassNameIsSpecified$whenRunTests$thenClassNotFoundException() throws Throwable {
    Result result = JUnitCore.runClasses(TestClass3.class);
    assertFalse(result.wasSuccessful());
    assertEquals(1, result.getFailureCount());
    throw result.getFailures().get(0).getException();
  }

  @RunWith(JCUnit.class)
  @GenerateWith(
      generator = @Generator(
          value = Replayer.class,
          args = { @Value("com.github.dakusui.jcunit.tests.caengines.ReplayerTest$TestClass4$TG"), @Value("All") }
      ))
  public static class TestClass4 {
    public abstract static class TG extends CAEngineBase {
      public TG() throws IOException {
        throw new IOException("hello!!!");
      }

      @Override
      public Tuple getTuple(int tupleId) {
        return null;
      }

      @Override
      protected long initializeTuples() {
        return 0;
      }
    }

    @SuppressWarnings("unused")
    public static class TG2 extends TG {
      private TG2() throws IOException {
      }
    }

    @Rule
    public Recorder recorder = new Recorder();
    @SuppressWarnings("unused")
    @FactorField(intLevels = { 100, 200 })
    public int f1;
    @SuppressWarnings("unused")
    @FactorField(intLevels = 300)
    public int f2;

    @Test
    public void test() {
    }
  }

  @Test(expected = InstantiationException.class)
  public void givenAbstractGeneratorClassIsSpecified$whenRunTests$thenInstantiationExceptionWillBeThrown() throws Throwable {
    Result result = JUnitCore.runClasses(TestClass4.class);
    assertFalse(result.wasSuccessful());
    assertEquals(1, result.getFailureCount());
    throw result.getFailures().get(0).getException();
  }

  @RunWith(JCUnit.class)
  @GenerateWith(
      generator = @Generator(
          value = Replayer.class,
          args = { @Value("com.github.dakusui.jcunit.tests.caengines.ReplayerTest$TestClass5$TG2"), @Value("All") }
      ))
  public static class TestClass5 extends TestClass4 {
    @SuppressWarnings("unused")
    public static class TG2 extends TG {
      private TG2() throws IOException {
      }
    }
  }

  @Test(expected = InvalidPluginException.class)
  public void givenGeneratorClassWhoseNoParamConstructorIsPrivateIsSpecified$whenRunTests$thenInvalidPluginExceptionWillBeThrown() throws Throwable {
    Result result = JUnitCore.runClasses(TestClass5.class);
    assertFalse(result.wasSuccessful());
    assertEquals(1, result.getFailureCount());
    throw result.getFailures().get(0).getException();
  }

  @RunWith(JCUnit.class)
  @GenerateWith(
      generator = @Generator(
          value = Replayer.class,
          args = { @Value("com.github.dakusui.jcunit.tests.caengines.ReplayerTest$TestClass6$TG3"), @Value("All") }
      ))
  public static class TestClass6 extends TestClass4 {
    @SuppressWarnings("unused")
    public static class TG3 extends TG {
      public TG3() throws IOException {
      }
    }
  }

  @Test(expected = IOException.class)
  public void givenGeneratorClassWhoseNoParamConstructorFailsIsSpecified$whenRunTests$thenOriginalExceptionWillBeThrown() throws Throwable {
    Result result = JUnitCore.runClasses(TestClass6.class);
    throw result.getFailures().get(0).getException();
  }

}
