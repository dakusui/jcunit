package com.github.dakusui.jcunit.ututils;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;

public class UTUtils {
  public static final Factors defaultFactors = new Factors.Builder().add(
      new Factor.Builder("A").addLevel("a1").addLevel("a2").build()
  ).add(
      new Factor.Builder("B").addLevel("b1").addLevel("b2").build()
  ).build();

  public static final PrintStream DUMMY_PRINTSTREAM = new PrintStream(new OutputStream() {
    @Override
    public void write(int b) throws IOException {
    }
  });
  private static      PrintStream out               = System.out;

  private UTUtils() {
  }

  public synchronized static void configureStdIOs() {
    if (UTUtils.isRunByMaven()) {
      setSilent();
    } else {
      setVerbose();
    }
  }

  public synchronized static void setSilent() {
    out = DUMMY_PRINTSTREAM;
  }

  public synchronized static void setVerbose() {
    out = System.out;
  }

  public static PrintStream stdout() {
    return out;
  }

  @SuppressWarnings("unused")
  public static File createTempDirectory() throws IOException {
    final File temp = File.createTempFile("temp",
        Long.toString(System.nanoTime()));
    if (!(temp.delete())) {
      throw new IOException("Could not delete temp file: "
          + temp.getAbsolutePath());
    }
    if (!(temp.mkdir())) {
      throw new IOException("Could not create temp directory: "
          + temp.getAbsolutePath());
    }
    return (temp);
  }

  public static Tuple.Builder tupleBuilder() {
    return new Tuple.Builder();
  }

  public static Tuple[] tuples(Tuple... tuples) {
    return Checks.checknotnull(tuples);
  }

  public static boolean isRunByMaven() {
    final String s = System.getProperty("sun.java.command");
    if (s == null)
      return false;
    return s.contains("surefire");
  }

  public static Result runTests(
      Class<?> testClass,
      int expectedRunCount,
      int expectedFailureCount,
      int expectedIgnoreCount) {
    Result result = JUnitCore.runClasses(Checks.checknotnull(testClass));
    assertEquals(expectedRunCount, result.getRunCount());
    assertEquals(expectedFailureCount, result.getFailureCount());
    assertEquals(expectedIgnoreCount, result.getIgnoreCount());
    return result;
  }
}
