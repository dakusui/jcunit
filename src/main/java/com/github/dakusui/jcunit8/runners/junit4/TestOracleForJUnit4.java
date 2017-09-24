package com.github.dakusui.jcunit8.runners.junit4;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.testsuite.TestOracle;
import org.junit.Test;
import org.junit.runners.model.FrameworkMethod;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

public interface TestOracleForJUnit4 extends TestOracle {
  Annotation[] annotations();

  class Builder {
    private final Map<String, Predicate<Tuple>> predicates;
    private       FrameworkMethod               testOrcleMethod;

    public Builder(Map<String, Predicate<Tuple>> predicates) {
      this.predicates = Objects.requireNonNull(predicates);
    }

    public Builder with(FrameworkMethod method) {
      this.testOrcleMethod = Objects.requireNonNull(method);
      return this;
    }

    public TestOracle build() {
      Test test = Builder.this.testOrcleMethod.getAnnotation(Test.class);
      Predicate<Result> then = expectsException(test) ?
          result -> result.exitedWith() == Result.Exit.THROWING_EXCEPTION &&
              getExpectedException(test).isAssignableFrom(result.value()) :
          result -> true;
      return new TestOracleForJUnit4() {
        @Override
        public Annotation[] annotations() {
          return Builder.this.testOrcleMethod.getAnnotations();
        }

        @Override
        public boolean shouldInvoke(Tuple tuple) {
          return false;
        }

        @Override
        public String getName() {
          return Builder.this.testOrcleMethod.getName();
        }

        @Override
        public Result apply(Tuple tuple) {
           return null;// TODO Builder.this.testOrcleMethod.invokeExplosively();
        }

        @Override
        public boolean test(Result result) {
          return then.test(result);
        }
      };
    }

    private boolean expectsException(Test annotation) {
      return getExpectedException(annotation) != null;
    }

    private long getTimeout(Test annotation) {
      if (annotation == null) {
        return 0;
      }
      return annotation.timeout();
    }

    private Class<? extends Throwable> getExpectedException(Test annotation) {
      if (annotation == null || annotation.expected() == Test.None.class) {
        return null;
      } else {
        return annotation.expected();
      }
    }
  }
}
