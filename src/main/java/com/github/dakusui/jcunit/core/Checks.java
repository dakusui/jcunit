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
    checknotnull(obj, null);
    return obj;
  }

  public static <T> T checknotnull(T obj, String msgOrFmt, Object... args) {
    if (obj == null) {
      throw new NullPointerException(composeMessage(msgOrFmt, args));
    }
    return obj;
  }

  public static void checkcond(boolean b) {
    checkcond(b, null);
  }

  public static void checkcond(boolean b, String msgOrFmt, Object... args) {
    if (!b) {
      throw new IllegalStateException(composeMessage(msgOrFmt, args));
    }
  }

  public static void checkparam(boolean b) {
    checkparam(b, null);
  }

  public static void checkparam(@SuppressWarnings("SameParameterValue") boolean b, String msgOrFmt, Object... args) {
    if (!b) {
      throw new IllegalArgumentException(composeMessage(msgOrFmt, args));
    }
  }

  public static void checkplugin(boolean cond, String msg, Object... args) {
    if (!cond) {
      throw new InvalidPluginException(composeMessage(msg, args), null);
    }
  }

  public static void checkenv(boolean cond, String msg, Object... args) {
    if (!cond) {
      throw new JCUnitEnvironmentException(composeMessage(msg, args), null);
    }
  }

  public static void checktest(boolean cond, String msg, Object... args) {
    if (!cond) {
      throw new InvalidTestException(composeMessage(msg, args), null);
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
    throw new JCUnitException(composeMessage(msgOrFmt, args), e);
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

  public static void rethrowpluginerror(Throwable throwable, String msgOrFmt, Object... args) {
    throw new InvalidPluginException(composeMessage(msgOrFmt, args), throwable);
  }

  public static void rethrowtesterror(Throwable throwable, String msgOrFmt, Object... args) {
    throw new InvalidTestException(composeMessage(msgOrFmt, args), throwable);
  }


  private static String composeMessage(String msgOrFmt, Object... args) {
    if (msgOrFmt != null) return String.format(msgOrFmt, args);
    return String.format("Message:'%s'", Utils.join(",", args));
  }
}
