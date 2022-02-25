package com.github.dakusui.jcunitx.testutils;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public interface TestOracle<T> extends Predicate<T> {
  Matcher<T> toMatcher();

  class Builder<T, U> {
    Function<T, U> transformer;
    private Predicate<U> tester;
    private String       transformerName;
    private String       testerName;

    public Builder<T, U> withTransformer(Function<T, U> transformer) {
      return this.withTransformer(
          UTUtils.isToStringOverridden(transformer.getClass()) ?
              transformer.toString() :
              "(unknown function)({x})",
          transformer
      );
    }

    public Builder<T, U> withTransformer(String name, Function<T, U> transformer) {
      this.transformerName = name;
      this.transformer = transformer;
      return this;
    }

    public Builder<T, U> withTester(Predicate<U> tester) {
      return this.withTester(
          UTUtils.isToStringOverridden(tester.getClass()) ?
              tester.toString() :
              "(unknown predicate)({x})",
          tester
      );
    }

    public Builder<T, U> withTester(String name, Predicate<U> tester) {
      this.testerName = name;
      this.tester = tester;
      return this;
    }

    public TestOracle<T> build() {
      Objects.requireNonNull(this.transformerName);
      Objects.requireNonNull(this.transformer);
      Objects.requireNonNull(this.testerName);
      Objects.requireNonNull(this.tester);
      return new TestOracle<T>() {
        @Override
        public boolean test(T t) {
          return tester.test(transformer.apply(t));
        }

        @Override
        public Matcher<T> toMatcher() {
          return new BaseMatcher<T>() {
            @SuppressWarnings("unchecked")
            @Override
            public boolean matches(Object item) {
              return test((T) item);
            }

            @Override
            public void describeTo(Description description) {
              description.appendText(
                  String.format(
                      "'%s' should be '%s'",
                      transformerName,
                      testerName
                  )
              );
            }

            @SuppressWarnings("unchecked")
            @Override
            public void describeMismatch(Object item, Description description) {
              description
                  .appendText("but {x} was ")
                  .appendValue(Objects.toString(item))
                  .appendText(" and '")
                  .appendText(transformerName)
                  .appendText("' became ")
                  .appendValue(transformer.apply((T) item));
            }
          };
        }
      };
    }
  }
}
