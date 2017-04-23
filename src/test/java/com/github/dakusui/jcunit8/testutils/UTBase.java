package com.github.dakusui.jcunit8.testutils;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

public class UTBase {
  /**
   * Names a predicate and returns it.
   *
   * @param name      Name of a predicate
   * @param predicate Predicate to be named.
   * @param <T>       A type of value given to {@code predicate}.
   */
  public static <T> Predicate<T> name(String name, Predicate<T> predicate) {
    return new Predicate<T>() {
      @Override
      public boolean test(T t) {
        return predicate.test(t);
      }

      @Override
      public String toString() {
        return name;
      }
    };
  }

  @SafeVarargs
  public static <T> Matcher<T> matcher(Predicate<T>... predicates) {
    return matcher(asList(predicates));
  }

  private static <T> Matcher<T> matcher(List<Predicate<T>> predicates) {
    return new BaseMatcher<T>() {
      @Override
      public boolean matches(Object o) {
        //noinspection unchecked
        T target = (T) o;
        for (Predicate<T> each : predicates) {
          if (!each.test(target))
            return false;
        }
        return true;
      }

      public void describeMismatch(Object item, Description description) {
        description.appendText("was ").appendValue(item).appendText(" ");
        description.appendText("that does not satisfy ");
        //noinspection unchecked
        description.appendText(
            predicates.stream()
                .filter((Predicate<T> target) -> !target.test((T) item))
                .map(Predicate::toString)
                .collect(Collectors.joining(", ", "[", "]"))
        );
      }

      @Override
      public void describeTo(Description description) {
        description.appendText(
            predicates.stream()
                .map(Predicate::toString)
                .collect(Collectors.joining(", "))
        );
      }
    };
  }
}
