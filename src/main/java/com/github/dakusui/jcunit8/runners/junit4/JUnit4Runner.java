package com.github.dakusui.jcunit8.runners.junit4;

import com.github.dakusui.jcunit.core.utils.Checks;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.manipulation.Filter;

import java.util.Objects;
import java.util.function.Predicate;

public class JUnit4Runner {
  static final int    ALL_TESTCASES = -1;
  static final String ANY_METHOD    = null;
  private final Request request;

  public Result run() {
    return new JUnitCore().run(this.getRequest());
  }

  public Request getRequest() {
    return this.request;
  }

  private JUnit4Runner(Class clazz, String methodName, int testCaseId) {
    this.request = Request.classes(clazz).filterWith(createFilter(methodName, testCaseId));
  }

  private Filter createFilter(String methodName, int testCaseId) {
    Predicate<Description> predicate = createPredicate(methodName, testCaseId);
    return new Filter() {
      @Override
      public boolean shouldRun(Description description) {
        if (description.isTest()) {
          return predicate.test(description);
        }

        // explicitly check if any children want to run
        for (Description each : description.getChildren()) {
          if (shouldRun(each)) {
            return true;
          }
        }
        return false;
      }

      @Override
      public String describe() {
        return String.format(
            "Method %s[%s]",
            methodName,
            testCaseId == ALL_TESTCASES ?
                "*" :
                Integer.toString(testCaseId)
        );
      }
    };
  }

  private Predicate<Description> createPredicate(String methodName, int testCaseId) {
    return description -> description.getMethodName().matches(
        (methodName == ANY_METHOD ?
            ".*" :
            methodName) +
            "\\[" +
            (testCaseId == ALL_TESTCASES ?
                "[0-9]+" :
                Integer.toString(testCaseId)
            ) +
            "\\]"

    );
  }

  public static class Builder {

    private final Class clazz;
    private String methodName = ANY_METHOD;
    private int    testCaseId = ALL_TESTCASES;

    public Builder(Class clazz) {
      this.clazz = Objects.requireNonNull(clazz);
    }

    public Builder methodName(String methodName) {
      this.methodName = Objects.requireNonNull(methodName);
      return this;
    }

    public Builder allMethods() {
      this.methodName = ANY_METHOD;
      return this;
    }

    public Builder testCase(int testCaseId) {
      Checks.checkcond(testCaseId >= 0);
      this.testCaseId = testCaseId;
      return this;
    }

    public Builder allTestCases() {
      this.testCaseId = ALL_TESTCASES;
      return this;
    }

    public JUnit4Runner build() {
      return new JUnit4Runner(
          this.clazz,
          this.methodName,
          this.testCaseId
      );
    }
  }
}
