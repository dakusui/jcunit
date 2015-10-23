package com.github.dakusui.jcunit.tests.generators;

import com.github.dakusui.jcunit.standardrunner.annotations.FactorField;
import com.github.dakusui.jcunit.standardrunner.annotations.Generator;
import com.github.dakusui.jcunit.standardrunner.annotations.Arg;
import com.github.dakusui.jcunit.standardrunner.annotations.TupleGeneration;
import com.github.dakusui.jcunit.core.*;
import com.github.dakusui.jcunit.standardrunner.Recorder;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.exceptions.InvalidTestException;
import com.github.dakusui.jcunit.plugins.generators.Replayer;
import com.github.dakusui.jcunit.plugins.generators.TupleGenerator;
import com.github.dakusui.jcunit.plugins.generators.TupleGeneratorBase;
import com.github.dakusui.jcunit.standardrunner.JCUnit;
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
  @TupleGeneration(
      generator = @Generator(
          value = Replayer.class,
          params = { @Arg("All") }
      )
  )
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

    assertEquals(1, testResult.getRunCount());
    assertEquals(false, testResult.wasSuccessful());

    validateFailureForMissingPreviousRunRecord(testResult);
  }

  @Test
  public void givenRecorderAndReplayerSetTrue$whenRunTests$thenRecordedTuplesWillBeReplayed() {
    System.setProperty(SystemProperties.KEY.RECORDER.key(), "true");
    System.setProperty(SystemProperties.KEY.REPLAYER.key(), "true");

    Result testResult = runTests();

    ////
    // The test should fail since no recorded tests will be found.
    assertEquals(1, testResult.getRunCount());
    assertEquals(false, testResult.wasSuccessful());

    validateFailureForMissingPreviousRunRecord(testResult);
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

    Result testResult1 = runTests();

    File testClassDataDir = new File(
        String.format(".jcunit/%s", TestClass.class.getCanonicalName()));
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

    Result testResult = runTests();
    File testClassDataDir = new File(
        String.format(".jcunit/%s", TestClass.class.getCanonicalName()));
    assertTrue(testClassDataDir.exists());
    assertTrue(testResult.wasSuccessful());

    TupleGenerator tupleGenerator = TupleGeneration.TupleGeneratorFactory.INSTANCE
        .createFromClass(TestClass.class);
    assertEquals(Replayer.class, tupleGenerator.getClass());

    Replayer replayer = (Replayer) tupleGenerator;
    assertEquals(2, replayer.size());
    assertEquals(100, replayer.getTuple(0).get("f1"));
    assertEquals(300, replayer.getTuple(0).get("f2"));
    assertEquals(200, replayer.getTuple(1).get("f1"));
    assertEquals(300, replayer.getTuple(1).get("f2"));

  }


  private Result runTests() {
    Recorder.initializeTestClassDataDir(TestClass.class);
    return TestClass.run();
  }

  private Result runTestsWithoutCleanUp() {
    return TestClass.run();
  }

  private void validateFailureForMissingPreviousRunRecord(Result testResult) {
    boolean validated = false;
    for (Failure failure : testResult.getFailures()) {
      assertEquals(
          String.format(
              "Test hasn't been run with 'JCUnitRecorder' rule yet. No tuple containing directory under '.jcunit/%s' was found.",
              TestClass.class.getCanonicalName()
          ),
          failure.getMessage()
      );
      validated = true;
    }
    assertTrue(validated);
  }

  @RunWith(JCUnit.class)
  @TupleGeneration(
      generator = @Generator(
          value = Replayer.class,
          params = { @Arg("All"), @Arg(".jcunit"), @Arg("com.github.dakusui.jcunit.plugins.generators.IPO2TupleGenerator"), @Arg("2") }
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

  @Test
  public void givenExplicitlyGeneratorClassNameAndStrengthAreSpecified$whenRunTests$thenTestsWillPass() throws Throwable {
    Result result = JUnitCore.runClasses(TestClass2.class);
    assertTrue(result.wasSuccessful());
  }

  @RunWith(JCUnit.class)
  @TupleGeneration(
      generator = @Generator(
          value = Replayer.class,
          params = { @Arg("All"), @Arg(".jcunit"), @Arg("WrongTupleGenerator"), @Arg("2") }
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

  @Test(expected = InvalidTestException.class)
  public void givenWrongGeneratorClassNameIsSpecified$whenRunTests$thenTestsWillPass() throws Throwable {
    Result result = JUnitCore.runClasses(TestClass3.class);
    assertFalse(result.wasSuccessful());
    assertEquals(1, result.getFailureCount());
    throw result.getFailures().get(0).getException();
  }

  @RunWith(JCUnit.class)
  @TupleGeneration(
      generator = @Generator(
          value = Replayer.class,
          params = { @Arg("All"), @Arg(".jcunit"), @Arg("com.github.dakusui.jcunit.tests.generators.ReplayerTest$TestClass4$TG") }
      ))
  public static class TestClass4 {
    public abstract static class TG extends TupleGeneratorBase {
      public TG() throws IOException {
        throw new IOException("hello!!!");
      }

      @Override
      public Tuple getTuple(int tupleId) {
        return null;
      }

      @Override
      protected long initializeTuples(Object[] params) {
        return 0;
      }

      @Override
      public Arg.Type[] parameterTypes() {
        return new Arg.Type[0];
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
  @TupleGeneration(
      generator = @Generator(
          value = Replayer.class,
          params = { @Arg("All"), @Arg(".jcunit"), @Arg("com.github.dakusui.jcunit.tests.generators.ReplayerTest$TestClass5$TG2") }
      ))
  public static class TestClass5 extends TestClass4 {
    @SuppressWarnings("unused")
    public static class TG2 extends TG {
      private TG2() throws IOException {
      }
    }
  }

  @Test(expected = IllegalAccessException.class)
  public void givenGeneratorClassWhoseNoParamConstructorIsPrivateIsSpecified$whenRunTests$thenIllegalAccessExceptionWillBeThrown() throws Throwable {
    Result result = JUnitCore.runClasses(TestClass5.class);
    assertFalse(result.wasSuccessful());
    assertEquals(1, result.getFailureCount());
    throw result.getFailures().get(0).getException();
  }

  @RunWith(JCUnit.class)
  @TupleGeneration(
      generator = @Generator(
          value = Replayer.class,
          params = { @Arg("All"), @Arg(".jcunit"), @Arg("com.github.dakusui.jcunit.tests.generators.ReplayerTest$TestClass6$TG3") }
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
