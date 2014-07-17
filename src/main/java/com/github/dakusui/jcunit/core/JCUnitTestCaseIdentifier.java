package com.github.dakusui.jcunit.core;

import java.io.Serializable;

public class JCUnitTestCaseIdentifier implements Serializable{
  final JCUnitTestCaseType testType;
  final Serializable       idInType;

  /**
   * Creates an object of this class.
   *
   * @param testType A type of test
   * @param idInType An object that can identify a test case among the ones belonging to the same {@code testType}
   *                 in a test run.
   */
  public JCUnitTestCaseIdentifier(JCUnitTestCaseType testType, Serializable idInType) {
    Utils.checknotnull(testType);
    Utils.checknotnull(idInType);
    this.testType = testType;
    this.idInType = idInType;
  }

  public int hashCode() {
    return this.idInType.hashCode() + this.testType.hashCode();
  }

  public boolean equals(Object o) {
    if (!(o instanceof JCUnitTestCaseIdentifier)) return false;
    JCUnitTestCaseIdentifier another = (JCUnitTestCaseIdentifier) o;
    return this.testType == another.testType && this.idInType.equals(another.idInType);
  }

  public String toString() {
    return String.format("%s:%s", this.testType, this.idInType.toString());
  }
}
