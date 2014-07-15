package com.github.dakusui.jcunit.framework.utils;

import java.io.File;
import java.io.IOException;

public class TestUtils {
  private TestUtils() {
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
}
