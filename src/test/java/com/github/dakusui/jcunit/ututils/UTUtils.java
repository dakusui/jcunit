package com.github.dakusui.jcunit.ututils;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;

import java.io.File;
import java.io.IOException;

public class UTUtils {
  public static final Factors defaultFactors = new Factors.Builder().add(
      new Factor.Builder().setName("A").addLevel("a1").addLevel("a2").build()
  ).add(
      new Factor.Builder().setName("B").addLevel("b1").addLevel("b2").build()
  ).build();

  private UTUtils() {
  }

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
}
