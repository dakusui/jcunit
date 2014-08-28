package com.github.dakusui.jcunit.core;

import com.github.dakusui.jcunit.exceptions.InvalidPluginException;
import com.github.dakusui.jcunit.exceptions.InvalidTestException;
import com.github.dakusui.jcunit.exceptions.JCUnitEnvironmentException;
import com.github.dakusui.jcunit.exceptions.JCUnitException;

public class Checks {
  /**
   * Checks if the given {@code obj} is {@code null} or not.
   * If it is, a {@code NullPointerException} will be thrown.
   * <p/>
   * This method is implemented in order to reduce dependencies on external libraries.
   *
   * @param obj A variable to be checked.
   * @param <T> The type of {@code obj}
   * @return {@code obj} itself
   */
  public static <T> T checknotnull(T obj) {
    if (obj == null) {
      throw new NullPointerException();
    }
    return obj;
  }

  public static <T> T checknotnull(T obj, String msgOrFmt, Object... args) {
    if (obj == null) {
      if (msgOrFmt != null) {
        throw new NullPointerException(String.format(msgOrFmt, args));
      } else {
        throw new NullPointerException(
            String.format("info(%s)", Utils.join(",", args)));
      }
    }
    return obj;
  }

  public static void checkcond(boolean b) {
    if (!b) {
      throw new IllegalStateException();
    }
  }

  public static void checkcond(boolean b, String msgOrFmt, Object... args) {
    if (!b) {
      if (msgOrFmt != null) {
        throw new IllegalStateException(String.format(msgOrFmt, args));
      } else {
        throw new IllegalStateException(
            String.format("info(%s)", Utils.join(",", args)));
      }
    }
  }

  public static void checkparam(boolean b) {
    if (!b) {
      throw new IllegalArgumentException();
    }
  }

  public static void checkparam(boolean b, String msgOrFmt, Object... args) {
    if (!b) {
      if (msgOrFmt != null) {
        throw new IllegalArgumentException(String.format(msgOrFmt, args));
      } else {
        throw new IllegalArgumentException(
            String.format("info(%s)", Utils.join(",", args)));
      }
    }
  }

  public static void checkplugin(boolean cond, String msg, Object... args) {
    if (!cond) {
      throw new InvalidPluginException(String.format(msg, args), null);
    }
  }

  public static void checkenv(boolean cond, String msg, Object... args) {
    if (!cond) {
      throw new JCUnitEnvironmentException(String.format(msg, args), null);
    }
  }

  public static void checktest(boolean cond, String msg, Object... args) {
    if (!cond) {
      throw new InvalidTestException(String.format(msg, args), null);
    }
  }

  /**
   * Rethrows a given exception wrapping by a {@code JCUnitException}, which
   * is a runtime exception.
   *
   * @param e        An exception to be re-thrown.
   * @param msgOrFmt A message or a message format.
   * @param args     Arguments to be embedded in {@code msg}.
   */
  public static void rethrow(Throwable e, String msgOrFmt, Object... args) {
    JCUnitException ee = new JCUnitException(String.format(msgOrFmt, args), e);
    ee.setStackTrace(e.getStackTrace());
    throw ee;
  }

  /**
   * Rethrows a given exception wrapping by a {@code JCUnitException}, which
   * is a runtime exception.
   *
   * @param e An exception to be re-thrown.
   */
  public static void rethrow(Throwable e) {
    rethrow(e, e.getMessage());
  }
}
