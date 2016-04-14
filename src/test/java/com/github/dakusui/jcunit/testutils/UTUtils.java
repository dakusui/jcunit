package com.github.dakusui.jcunit.testutils;

import com.github.dakusui.jcunit.core.utils.Checks;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.Map;

public enum UTUtils {
  ;
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
    return s != null && s.contains("surefire");
  }

  /**
   * Returns a new map created from {@code entries}.
   * This method should only be used only when you now the types of objects
   * given through elements.
   *
   * Each element of the argument
   * <ol>
   * <li>must have 2 and only 2 elements.</li>
   * <li>The first element must be an instance of {@code K}.</li>
   * <li>The second element must be an instance of {@code V}.</li>
   * </ol>
   * If {@code K} is {@code String} and {@code V} is {@code Integer}, following
   * object is valid for this method for {@code entries}.
   * <p/>
   * <pre>
   * Object[] entries = new Object[]{"Hello", 256, "World", 1};
   *
   * </pre>
   *
   * If there are more than one entry whose first element is {@code equals} each other,
   * The last one wins.
   * If no entry is given, this method returns a new map of the specified key-value types.
   *
   * @param <K> A key type of returned map.
   * @param <V> A value type of returned map.
   * @see UTUtils#entry(Object, Object)
   */
  public static <K, V> Map<K, V> toMap(Object[]... entries) {
    Map<K, V> ret = new LinkedHashMap<K,V>();
    for (Object[] eachEntry : entries) {
      Checks.checknotnull(eachEntry);
      Checks.checkcond(eachEntry.length == 2, "Invalid entry is found. '%s'", eachEntry);
      //noinspection unchecked
      ret.put((K)eachEntry[0], (V)eachEntry[1]);
    }
    return ret;
  }

  /**
   * A helper method for {@code toMap}.
   * <pre>
   *   Map<String, Integer> map = toMap(entry("Hello", 1), entry("World"));
   * </pre>
   */
  public static <K, V> Object[] entry(K key, V value) {
    return new Object[] { key, value };
  }

  public static class MapBuilder<K,V> {
    private Map<K, V> map;
    public MapBuilder() {
      this.map = new LinkedHashMap<K, V>();
    }

    public MapBuilder<K, V> add(K k, V v) {
      this.map.put(k, v);
      return this;
    }

    public Map<K,V> build() {
      return this.map;
    }
  }
}
