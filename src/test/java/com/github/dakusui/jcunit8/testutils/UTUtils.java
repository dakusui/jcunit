package com.github.dakusui.jcunit8.testutils;

import com.github.dakusui.jcunit8.exceptions.FrameworkException;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

public enum UTUtils {
  ;

  private static final Logger LOGGER = LoggerFactory.getLogger(UTUtils.class);

  @SuppressWarnings("unchecked")
  public static boolean isToStringOverridden(Class klass) {
    try {
      return !Objects.equals(klass.getMethod("toString"), Object.class.getMethod("toString"));
    } catch (NoSuchMethodException e) {
      throw FrameworkException.unexpectedByDesign(e);
    }
  }

  public static <C extends Collection> TestOracle<C, Integer> sizeIs(String description, Predicate<Integer> predicate) {
    return oracle(
        "Size",
        Collection::size,
        format("should be '%s'", description),
        predicate
    );
  }

  public static <E, C extends Collection<E>> Predicate<C> allSatisfy(Predicate<E> predicate) {
    return oracle(
        format("All elements should satisfy '%s'", predicate),
        (C collection) -> collection.stream().allMatch(predicate)
    );
  }

  public static boolean isRunByMaven() {
    final String s = System.getProperty("sun.java.command");
    return s != null && s.contains("surefire");
  }

  public synchronized static void configureStdIOs() {
    if (UTUtils.isRunByMaven()) {
      setSilent();
      System.setOut(DUMMY_PRINTSTREAM);
      System.setErr(DUMMY_PRINTSTREAM);
    } else {
      setVerbose();
    }
  }

  public synchronized static void setSilent() {
    out = DUMMY_PRINTSTREAM;
  }

  public synchronized static void setVerbose() {
    out = System.out;
  }

  public static final PrintStream DUMMY_PRINTSTREAM = new PrintStream(new OutputStream() {
    @Override
    public void write(int b) throws IOException {
    }
  });

  public static <T> T print(T value) {
    System.out.println(value);
    return value;
  }

  private static PrintStream out = System.out;

  /**
   * Names a predicate and returns it.
   *
   * @param descriptionOfTest Name of a predicate
   * @param predicate         Predicate to be named.
   * @param <T>               A type of value given to {@code predicate}.
   */
  public static <T> TestOracle<T, T> oracle(String descriptionOfTest, Predicate<T> predicate) {
    return oracle("{x}", t -> t, descriptionOfTest, predicate);
  }

  public static <T, U> TestOracle<T, U> oracle(String descriptionOfTransform, Function<T, U> transform, String descriptionOfTest, Predicate<U> tester) {
    return new TestOracle.Builder<T, U>()
        .withTester(descriptionOfTest, tester)
        .withTransformer(descriptionOfTransform, transform)
        .build();
  }

  @SafeVarargs
  public static <T> Matcher<T> matcherFromPredicates(Predicate<T>... predicates) {
    return matcher(Stream.of(predicates)
        .map(each -> new TestOracle.Builder<T, T>()
            .withTransformer(v -> v)
            .withTester(each)
            .build()
        )
        .collect(toList()));
  }

  @SafeVarargs
  public static <T> Matcher<T> matcher(TestOracle<T, ?>... testOracles) {
    return matcher(asList(testOracles));
  }

  private static <T> Matcher<T> matcher(List<TestOracle<T, ?>> testOracles) {
    return CoreMatchers.allOf(
        testOracles.stream()
            .map(TestOracle::toMatcher)
            .collect(toList())
    );
  }

  public static File createTempDirectory(String pathname) {
    try {
      File dir = new File(pathname);
      LOGGER.debug("{} was created={}", dir, dir.mkdirs());
      return Files.createTempDirectory(dir.toPath(), "jcunit-").toFile();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
