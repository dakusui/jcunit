package com.github.dakusui.jcunit8.runners.junit4;

import com.github.dakusui.jcunit.core.utils.Checks;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.manipulation.Filter;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JUnit4Runner {
  static final int ALL_TESTCASES = -1;
  static final String ANY_METHOD = null;
  private final Request request;

  public Result run() {
    return new JUnitCore().run(this.getRequest());
  }

  public Request getRequest() {
    return this.request;
  }

  private JUnit4Runner(Class clazz, String methodName, int startTestCaseId, int endTestCaseId) {
    this.request = Request.classes(clazz).filterWith(
        createFilter(methodName, startTestCaseId, endTestCaseId)
    );
  }

  private Filter createFilter(String methodName, int startTestCaseId, int endTestCaseId) {
    return new Filter() {
      @Override
      public boolean shouldRun(Description description) {
        if (description.isTest()) {
          return createPredicate(methodName, startTestCaseId, endTestCaseId).test(description);
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
            startTestCaseId == ALL_TESTCASES ?
                "*" :
                String.format("%s-%s", startTestCaseId, endTestCaseId)
        );
      }
    };
  }

  private Predicate<Description> createPredicate(String methodName, int startTestCaseId, int endTestCaseId) {
    return description -> {
      Pattern pattern = Pattern.compile(
          Optional.ofNullable(methodName).orElse(".*") + "\\[([0-9]+)\\]"
      );
      Matcher matcher = pattern.matcher(description.getMethodName());

      if (!matcher.matches()) {
        return false;
      }

      int id = Integer.valueOf(matcher.group(1));
      return startTestCaseId == ALL_TESTCASES || id >= startTestCaseId && id <= endTestCaseId;
    };
  }

  public static class Builder {

    private final Class clazz;
    private String methodName = ANY_METHOD;
    private int startTestCaseId = ALL_TESTCASES;
    private int endTestCaseId = ALL_TESTCASES;

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
      this.startTestCaseId = testCaseId;
      this.endTestCaseId = testCaseId;
      return this;
    }

    public Builder testCasesInRange(int startTestCaseId, int endTestCaseId) {
      Checks.checkcond(startTestCaseId >= 0);
      Checks.checkcond(endTestCaseId >= 0);
      Checks.checkcond(startTestCaseId <= endTestCaseId);
      this.startTestCaseId = startTestCaseId;
      this.endTestCaseId = endTestCaseId;
      return this;
    }

    public Builder testCasesFrom(int startTestCaseId) {
      Checks.checkcond(startTestCaseId >= 0);
      this.startTestCaseId = startTestCaseId;
      this.endTestCaseId = Integer.MAX_VALUE;
      return this;
    }

    public Builder testCasesUntil(int endTestCaseId) {
      Checks.checkcond(endTestCaseId >= 0);
      this.startTestCaseId= 0;
      this.endTestCaseId = endTestCaseId;
      return this;
    }

    public Builder allTestCases() {
      this.startTestCaseId = ALL_TESTCASES;
      return this;
    }

    public JUnit4Runner build() {
      return new JUnit4Runner(
          this.clazz,
          this.methodName,
          this.startTestCaseId,
          this.endTestCaseId
      );
    }
  }
}
