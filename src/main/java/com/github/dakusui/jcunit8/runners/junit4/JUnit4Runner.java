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

  private JUnit4Runner(Class clazz, String methodName, int startInclusive, int endExclusive) {
    this.request = Request.classes(clazz).filterWith(
        createFilter(methodName, startInclusive, endExclusive)
    );
  }

  private Filter createFilter(String methodName, int startInclusive, int endExclusive) {
    return new Filter() {
      @Override
      public boolean shouldRun(Description description) {
        if (description.isTest()) {
          return createPredicate(methodName, startInclusive, endExclusive).test(description);
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
            startInclusive == ALL_TESTCASES ?
                "*" :
                String.format("%s-%s", startInclusive, endExclusive)
        );
      }
    };
  }

  private Predicate<Description> createPredicate(String methodName, int startInclusive, int endExclusive) {
    return description -> {
      Pattern pattern = Pattern.compile(
          Optional.ofNullable(methodName).orElse(".*") + "\\[([0-9]+)\\]"
      );
      Matcher matcher = pattern.matcher(description.getMethodName());

      if (!matcher.matches()) {
        return false;
      }

      int id = Integer.valueOf(matcher.group(1));
      return startInclusive == ALL_TESTCASES || id >= startInclusive && id < endExclusive;
    };
  }

  public static class Builder {

    private final Class clazz;
    private String methodName     = ANY_METHOD;
    private int    startInclusive = ALL_TESTCASES;
    private int    endInclusive   = ALL_TESTCASES;

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
      this.startInclusive = testCaseId;
      this.endInclusive = testCaseId + 1;
      return this;
    }

    public Builder testCasesInRange(int startTestCaseId, int endTestCaseId) {
      Checks.checkcond(startTestCaseId >= 0);
      Checks.checkcond(endTestCaseId >= 0);
      Checks.checkcond(startTestCaseId <= endTestCaseId);
      this.startInclusive = startTestCaseId;
      this.endInclusive = endTestCaseId;
      return this;
    }

    public Builder testCasesFrom(int startInclusive) {
      Checks.checkcond(startInclusive >= 0);
      this.startInclusive = startInclusive;
      this.endInclusive = Integer.MAX_VALUE;
      return this;
    }

    public Builder testCasesUntil(int endInclusive) {
      Checks.checkcond(endInclusive >= 0);
      this.startInclusive = 0;
      this.endInclusive = endInclusive;
      return this;
    }

    public Builder allTestCases() {
      this.startInclusive = ALL_TESTCASES;
      return this;
    }

    public JUnit4Runner build() {
      return new JUnit4Runner(
          this.clazz,
          this.methodName,
          this.startInclusive,
          this.endInclusive
      );
    }
  }
}
