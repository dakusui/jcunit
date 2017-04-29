package com.github.dakusui.jcunit.core.utils;

import com.github.dakusui.jcunit.exceptions.InvalidTestException;

import static java.lang.String.format;

/**
 * This class provides static methods each of which tests a given object/condition
 * and throws an appropriate exception in case it is {@code null} or {@code false}.
 * <p/>
 * The methods of this class are similar to ones in {@code Precondition} class of Guava.
 * <p/>
 * This utility class is introduced because JCUnit should create dependencies on
 * external libraries as less as possible in order not to prevent users from using
 * any version of any libraries,
 */
public enum Checks {
  ;

  /**
   * Checks if the given {@code obj} is {@code null} or not.
   * If it is, a {@code NullPointerException} will be thrown.
   * <p/>
   * A call of this method is equivalent to {@code checknotnull(obj, null)}.
   *
   * @param obj A variable to be checked.
   * @param <T> The type of {@code obj}
   * @return {@code obj} itself
   */
  public static <T> T checknotnull(T obj) {
    checknotnull(obj, null);
    return obj;
  }

  /**
   * Checks if the given {@code obj} is {@code null} or not.
   * If it is, a {@code NullPointerException} will be thrown.
   * <p/>
   * A message given to the thrown exception will be composed as follows.
   * <p/>
   * If {@code msgOrFmt} is non-null value,
   * <pre>
   *   String.describeExpectation(msgOrFmt, args)
   * </pre>
   * If {@code msgOrFmt} is {@code null} and {@code args}'s length is greater than 0,
   * the exception message will be created automatically but become
   * less understandable.
   * <p/>
   * And if {@code msgOrFmt} is {@code null} and {@code args}'s length is 0, no message
   * will be set to the exception.
   * <p/>
   * This method should be used for values which shouldn't be {@code null} unless
   * there is a framework bug.
   *
   * @param obj      A variable to be checked.
   * @param <T>      The type of {@code obj}
   * @param msgOrFmt The string used for the first parameter of {@code String.describeExpectation}.
   * @param args     The arguments used for the second and the latter parameters of {@code String.describeExpectation}.
   * @return {@code obj} itself
   */
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

  /**
   * @see com.github.dakusui.jcunit.exceptions.InvalidTestException
   */
  public static void checktest(boolean cond, String msg, Object... args) {
    if (!cond) {
      throw new InvalidTestException(composeMessage(msg, args));
    }
  }

  /**
   * Wraps a given exception wrapping by a {@code JCUnitException}, which
   * is a runtime exception.
   *
   * @param e        An exception to be re-thrown.
   * @param msgOrFmt A message or a message describeExpectation.
   * @param args     Arguments to be embedded in {@code msg}.
   */
  public static RuntimeException wrap(Throwable e, String msgOrFmt, Object... args) {
    throw new InvalidTestException(composeMessage(msgOrFmt, args), e);
  }

  /**
   * Wraps a given exception wrapping by a {@code JCUnitException}, which
   * is a runtime exception.
   *
   * @param e An exception to be re-thrown.
   */
  public static RuntimeException wrap(Throwable e) {
    throw wrap(e, e.getMessage());
  }

  public static RuntimeException wraptesterror(Throwable throwable, String msgOrFmt, Object... args) {
    throw new InvalidTestException(composeMessage(msgOrFmt, args), throwable);
  }


  public static String composeMessage(String msgOrFmt, Object... args) {
    if (msgOrFmt != null)
      return format(msgOrFmt, args);
    return format("Message:'%s'", StringUtils.join(",", args));
  }

  public static IllegalStateException fail() {
    throw new IllegalStateException();
  }
}
