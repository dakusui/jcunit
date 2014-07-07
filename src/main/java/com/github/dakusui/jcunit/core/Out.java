package com.github.dakusui.jcunit.core;

import com.github.dakusui.lisj.Basic;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation to tell JCUnit framework that a field is an output and should
 * be verified in tests.
 *
 * @author hiroshi
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Out {
  /**
   * Implementation of this interface must guarantee that there is a public
   * constructor without any parameter.
   *
   * @param <T>
   * @author hiroshi
   */
  public static interface Verifier {
    /**
     * Verifies <code>actual</code> object with the <code>expected</code> one.
     *
     * @param expected An expected object. Typically stored as local file by JCUnit
     *                 framework.
     * @param actual   An actual object.
     * @return true - actual object considered to be verified with expected
     * object / false - otherwise.
     */
    public boolean verify(Object expected, Object actual);
  }

  /**
   * Default implementation of <code>Verifier<code> interface.
   * This implementation uses <code>Basic.eq</code> method to verify the
   * <code>actual</code> object with <code>expected</code> one.
   *
   * @author hiroshi
   */
  public static class DefaultVerifier implements Verifier {
    /**
     * Creates an object of this class.
     */
    public DefaultVerifier() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean verify(Object obj, Object another) {
      return Basic.eq(obj, another);
    }
  }

  /**
   * Returns a Verifier class to be used for verifying loaded object. This
   * method is only used by 'automated regression framework'.
   *
   * @return the class to verify actual object.
   */
  public Class<? extends Verifier> verifier() default DefaultVerifier.class;
}
