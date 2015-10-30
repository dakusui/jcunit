package com.github.dakusui.jcunit.fsm;

/**
 * The idea 'State' JCUnit has for a software under test represents a tester's
 * understanding about the software built from design intention, functionality or
 * non-functionality requirements, common sense, etc. Not only state chart
 * diagram drawn by a designer.
 *
 * To model a certain SUT, it is possible to draw multiple diagrams to model it
 * from different perspective.
 *
 * States appear in those diagrams are sometimes not useful for the product itself
 * but only in testing efforts.
 * In this situation, test codes shouldn't push responsibility for modeling states
 * to production codes.
 * What they could do instead is to examine if the SUT is in the state in which
 * JCUnit assumes the SUT is from what it can observe. For instance, if the SUT
 * has a set of getters by which users cannot change its internal states, tests
 * can call them, observe their output, and determine if SUT is in an expected
 * state.
 *
 * @param <SUT> Type of Software Under Test.
 */
public interface StateChecker<SUT> {
  /**
   * Checks if the given {@code sut} (software under test) satisfies the conditions
   * for it to be in the state that this object represents.
   *
   * It is counter intuitive but if this method cannot examine the SUT at all,
   * this method will return {@code true}. {@code false} should be returned only
   * if the method finds something contradicting the state.
   *
   * @param sut An object that represents software under test.
   * @return false - This method finds that {@code sut} is not in the state / true - otherwise.
   */
  boolean check(SUT sut);
}
