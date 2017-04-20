package com.github.dakusui.jcunit8.tests.features.pipeline.testbase;

import com.github.dakusui.jcunit8.tests.features.UTBase;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.function.Predicate;

import static java.lang.String.format;

public enum UTUtils {
  ;

  public static <C extends Collection> Predicate<C> sizeIs(Predicate<Integer> predicate) {
    return UTBase.name(
        format("Size should be '%s'", predicate),
        (C tupleSet) -> predicate.test(tupleSet.size())
    );
  }

  public static <E, C extends Collection<E>> Predicate<C> allSatisfy(Predicate<E> predicate) {
    return UTBase.name(
        format("All elements should satisfy '%s'", predicate),
        (C collection) -> collection.stream().allMatch(predicate)
    );
  }

  public static boolean isRunByMaven() {
    final String s = System.getProperty("sun.java.command");
    return s != null && s.contains("surefire");
  }

  public synchronized static void configureStdIOs() {
    if (UTUtils.isRunByMaven()) {
      setSilent();
      System.setOut(DUMMY_PRINTSTREAM);
      System.setErr(DUMMY_PRINTSTREAM);
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

  public static final PrintStream DUMMY_PRINTSTREAM = new PrintStream(new OutputStream() {
    @Override
    public void write(int b) throws IOException {
    }
  });

  private static      PrintStream out               = System.out;

}
