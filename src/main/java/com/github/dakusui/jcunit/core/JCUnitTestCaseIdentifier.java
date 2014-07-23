package com.github.dakusui.jcunit.core;

import java.io.Serializable;
import java.util.List;

public class JCUnitTestCaseIdentifier implements Serializable{
  final JCUnitTestCaseType testType;
  final List<Serializable> labels;

  /**
   * Creates an object of this class.
   *
   * @param testType A type of test
   * @param labels An object that can identify a test case among the ones belonging to the same {@code testType}
   *                 in a test run.
   */
  public JCUnitTestCaseIdentifier(JCUnitTestCaseType testType, List<Serializable> labels) {
    Utils.checknotnull(testType);
    Utils.checknotnull(labels);
    this.testType = testType;
    this.labels = labels;
  }

  public int hashCode() {
    return this.labels.hashCode() + this.testType.hashCode();
  }

  public boolean equals(Object o) {
    if (!(o instanceof JCUnitTestCaseIdentifier)) return false;
    JCUnitTestCaseIdentifier another = (JCUnitTestCaseIdentifier) o;
    return this.testType == another.testType && this.labels.equals(another.labels);
  }

  public String toString() {
    return String.format("%s:%s", this.testType, this.labels);
  }
}
