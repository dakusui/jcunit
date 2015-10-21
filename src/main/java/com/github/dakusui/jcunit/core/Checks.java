package com.github.dakusui.jcunit.core;

import com.github.dakusui.jcunit.core.reflect.ReflectionUtils;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.exceptions.*;

import java.util.LinkedList;
import java.util.List;

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
public class Checks {
  private Checks() {
  }

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
   * Checks if the parameters satisfy a certain condition which should be {@code true}
   * unless there is a framework bug.
   * <p/>
   * This method has the same effect as the call of {@code checkparam(b, null)}.
   */
  public static void checkparam(boolean b) {
    checkparam(b, null);
  }

  /**
   * Checks if the parameters satisfy a certain condition which should be {@code true}
   * unless there is a framework bug.
   * <p/>
   * If {@code b} becomes {@code false}, an {@code IllegalArgumentException} will be thrown.
   * <p/>
   * A message set to the exception will be composed in the same manner as {@code checknotnull} method.
   *
   * @see com.github.dakusui.jcunit.core.Checks#checknotnull(Object, String, Object...)
   */
  public static void checkparam(@SuppressWarnings("SameParameterValue") boolean b, String msgOrFmt, Object... args) {
    if (!b) {
      throw new IllegalArgumentException(composeMessage(msgOrFmt, args));
    }
  }

  /**
   * @see com.github.dakusui.jcunit.exceptions.InvalidPluginException
   */
  public static void checkplugin(boolean cond, String msg, Object... args) {
    if (!cond) {
      throw new InvalidPluginException(composeMessage(msg, args));
    }
  }

  /**
   * A message set to the exception will be composed in the same manner as {@code checknotnull} method.
   *
   * @see com.github.dakusui.jcunit.core.Checks#checknotnull(Object, String, Object...)
   */
  public static void checkenv(boolean cond, String msg, Object... args) {
    if (!cond) {
      throw new JCUnitEnvironmentException(composeMessage(msg, args), null);
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

  public static void checksymbols(Tuple tuple, String... factorNames) throws UndefinedSymbol {
    List<String> missings = new LinkedList<String>();
    for (String each : factorNames) {
      if (!Checks.checknotnull(tuple).containsKey(each)) {
        missings.add(each);
      }
    }
    if (!missings.isEmpty()) {
      throw new UndefinedSymbol(missings.toArray(new String[missings.size()]));
    }
  }

  /**
   * Rethrows a given exception wrapping by a {@code JCUnitException}, which
   * is a runtime exception.
   *
   * @param e        An exception to be re-thrown.
   * @param msgOrFmt A message or a message describeExpectation.
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
    if (msgOrFmt != null)
      return Utils.format(msgOrFmt, args);
    return Utils.format("Message:'%s'", Utils.join(",", args));
  }

  public static void fail() {
    throw new IllegalStateException();
  }

  public static <T> T cast(Class<T> clazz, Object parameter) {
    Checks.checkcond(
        ReflectionUtils.isAssignable(Checks.checknotnull(clazz), parameter),
        "Type mismatch. Requiret:%s Found:%s",
        clazz,
        parameter
    );
    //noinspection unchecked
    return (T) parameter;
  }
}
