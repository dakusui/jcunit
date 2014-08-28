package com.github.dakusui.jcunit.generators;

import com.github.dakusui.jcunit.core.*;
import com.github.dakusui.jcunit.core.rules.Recorder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ReplayerTest {
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

    TupleGenerator tupleGenerator = TupleGeneratorFactory.INSTANCE
        .createTupleGeneratorFromClass(TestClass.class);
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

  @RunWith(JCUnit.class)
  @TupleGeneration(
      generator = @Generator(
          value = Replayer.class,
          params = { @Param("All") }
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
      System.out.println("f1=" + f1 + ", f2=" + f2);
      assertTrue(f1 > f1Threshold);
    }
  }
}
