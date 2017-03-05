package com.github.dakusui.jcunit.plugins.constraints;

import com.github.dakusui.jcunit.core.utils.Checks;

import java.util.List;

/**
 * An interface that models constraints.
 *
 * @see ConstraintChecker
 * @see Constraint
 */
public interface ConstraintBundle {
  /**
   * Returns a new constraint checker instance.
   * In case constraint bundle can guarantee the returned value is stateless and thread safe,
   * this method may return the same object always.
   */
  ConstraintChecker newConstraintChecker();

  /**
   * Returns a list of constraints.
   *
   * This method may throw a runtime exception if an underlying constraint checker
   * does not allow to access individual constraints.
   *
   * To check if this method is supported or not, {@see allowsDecomposition}.
   */
  List<Constraint> getConstraints();

  /**
   * Checks if {@code getConstraints()} method is allowed or not.
   */
  boolean allowsDecomposition();

  enum Factory {
    ;
    public static ConstraintBundle createFromConstraintChecker(final ConstraintChecker checker) {
      Checks.checknotnull(checker);
      return new ConstraintBundle() {
        /**
         * TODO: Right now this method always returns the same object, but it should return a new one every time.
         */
        @Override
        public ConstraintChecker newConstraintChecker() {
          return checker.getFreshObject();
        }

        @Override
        public List<Constraint> getConstraints() {
          return checker.getConstraints();
        }

        @Override
        public boolean allowsDecomposition() {
          return checker instanceof SmartConstraintChecker;
        }
      };
    }
  }
}
