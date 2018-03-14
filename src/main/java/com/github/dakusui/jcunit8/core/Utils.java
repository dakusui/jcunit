package com.github.dakusui.jcunit8.core;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.utils.Checks;
import com.github.dakusui.jcunit8.exceptions.FrameworkException;
import org.junit.runners.Parameterized;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.LongPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static com.github.dakusui.jcunit.core.reflect.ReflectionUtils.getMethod;
import static com.github.dakusui.jcunit8.exceptions.FrameworkException.unexpectedByDesign;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public enum Utils {
  ;

  public static PrintStream out = System.out;

  public static <T> Function<T, T> printer() {
    return printer(Object::toString);
  }

  public static <T> Function<T, T> printer(Function<T, String> formatter) {
    return t -> {
      System.out.println(formatter.apply(t));
      return t;
    };
  }

  @SuppressWarnings("unchecked")
  public static <T> T print(T data) {
    return (T) printer().apply(data);
  }

  public static <T> List<T> unique(List<T> in) {
    return new ArrayList<>(new LinkedHashSet<>(in));
  }

  public static <T> int sizeOfIntersection(Set<T> a, Set<T> b) {
    Set<T> lhs;
    Set<T> rhs;
    if (a.size() > b.size()) {
      lhs = b;
      rhs = a;
    } else {
      lhs = a;
      rhs = b;
    }
    int ret = 0;
    for (T each : lhs) {
      if (rhs.contains(each))
        ret++;
    }
    return ret;
  }

  public static TestClass createTestClassMock(final TestClass testClass) {
    return new TestClass(testClass.getJavaClass()) {
      @Override
      public List<FrameworkMethod> getAnnotatedMethods(final Class<? extends Annotation> annClass) {
        if (Parameterized.Parameters.class.equals(annClass)) {
          return singletonList(createDummyFrameworkMethod());
        }
        return super.getAnnotatedMethods(annClass);
      }

      private FrameworkMethod createDummyFrameworkMethod() {
        return new FrameworkMethod(getDummyMethod()) {
          public boolean isStatic() {
            return true;
          }

          @Override
          public Object invokeExplosively(Object target, Object... params) {
            return new Object[] {};
          }

          @SuppressWarnings("unchecked")
          @Override
          public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
            FrameworkException.checkCondition(Parameterized.Parameters.class.equals(annotationType));
            return (T) new Parameterized.Parameters() {
              @Override
              public Class<? extends Annotation> annotationType() {
                return Parameterized.Parameters.class;
              }

              @Override
              public String name() {
                return "{index}";
              }
            };
          }
        };
      }

      private Method getDummyMethod() {
        return getToStringMethod(Object.class);
      }
    };
  }

  private static Method getToStringMethod(Class<?> klass) {
    return getMethod(klass, "toString");
  }

  public static String className(Class klass) {
    return className(klass, "");
  }

  private static String className(Class klass, String work) {
    String canonicalName = klass.getCanonicalName();
    if (canonicalName != null)
      return canonicalName;
    return className(klass.getEnclosingClass(), work + "$");
  }

  /**
   * Creates and returns an instance of a class represented by {@code TestClass}.
   *
   * @param testClass Must be validated beforehand.
   * @return created instance.
   */
  public static Object createInstanceOf(TestClass testClass) {
    try {
      return testClass.getOnlyConstructor().newInstance();
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
      throw unexpectedByDesign(e);
    }
  }

  public static <T extends Predicate<E>, E> Predicate<E> conjunct(Iterable<T> predicates) {
    Predicate<E> ret = tuple -> true;
    for (Predicate<E> each : predicates) {
      ret = ret.and(each);
    }
    return ret;
  }

  public static <T, R> Function<T, R> memoize(Function<T, R> function, Map<T, R> memo) {
    requireNonNull(memo);
    return t -> memo.computeIfAbsent(t, function);
  }

  public static <T, R> Function<T, R> memoize(Function<T, R> function) {
    return memoize(function, new ConcurrentHashMap<>());
  }

  public static <T> Stream<List<T>> combinations(List<T> elements, int k) {
    Checks.checkcond(k >= 0);
    return new StreamableCombinator<>(elements, k).stream();
  }

  public static long countCombinations(List<?> elements, int k) {
    return new StreamableCombinator<>(elements, k).size();
  }

  @SuppressWarnings("unchecked")
  public static <T> Stream<List<T>> cartesian(Stream<T>... streams) {
    return cartesian(Arrays.stream(streams).map((Function<Stream<T>, Supplier<Stream<T>>>) s -> {
      T[] arr = (T[]) s.toArray();
      return () -> Arrays.stream(arr);
    }).collect(toList()));
  }

  public static <T> Stream<List<T>> cartesian(List<Supplier<Stream<T>>> streams) {
    return streams.isEmpty() ?
        Stream.empty() :
        streams.size() == 1 ?
            streams.get(0).get().map(i -> new ArrayList<T>(1) {{
              add(i);
            }}) :
            cartesian(() -> cartesian(streams.subList(0, streams.size() - 1)), streams.get(streams.size() - 1));
  }

  private static <T> Stream<List<T>> cartesian(Supplier<Stream<List<T>>> i, Supplier<Stream<T>> j) {
    return i.get().flatMap(
        list -> j.get().map(t -> new LinkedList<T>(list) {{
          add(t);
        }}));
  }

  /**
   * Returns an element in a given stream which gives a maximum value when {@code f}
   * is applied to it.
   * <p>
   * {@code max} is the possible maximum value for {@code f} when it is applied to elements in {@code in}. If it is not known,
   * consider simply using {@code Stream#max} method, not this one.
   * <p>
   * This method cannot be used for a stream {@code in} which contains {@code null}.
   *
   * @param in  A stream.
   * @param max Known maximum value for
   * @param f   A function to evaluate each element in {@code in} as an integer.
   * @param <T> Type of each element in {@code in}.
   * @return an {@code Optional} describing the maximum element of this stream,
   * or an empty {@code Optional} if the stream is empty
   */
  public static <T> Optional<T> max(Stream<T> in, long max, Function<T, Long> f) {
    return find(in, max, f, r -> r <= 0);
  }

  /**
   * Returns an element in a given stream which gives a minimum value when {@code f}
   * is applied to it.
   * <p>
   * {@code max} is the possible minimum value for {@code f} when it is applied to elements in {@code in}.
   * If it is not known, consider simply using {@code Stream#max} method, not this one.
   * <p>
   * This method cannot be used for a stream {@code in} which contains {@code null}.
   *
   * @param in  A stream.
   * @param min Known minimum value for
   * @param f   A function to evaluate each element in {@code in} as an integer.
   * @param <T> Type of each element in {@code in}.
   * @return an {@code Optional} describing the maximum element of this stream,
   * or an empty {@code Optional} if the stream is empty
   */
  public static <T> Optional<T> min(Stream<T> in, long min, Function<T, Long> f) {
    return find(in, min, f, r -> r >= 0);
  }

  private static <T> Optional<T> find(Stream<T> in, long boundary, Function<T, Long> f, LongPredicate p) {
    AtomicReference<T> foundMax = new AtomicReference<>();
    Optional<T> v = in.peek(Objects::requireNonNull)
        .peek(t -> {
          T cur = foundMax.get();
          if (cur == null || p.test(f.apply(cur) - f.apply(t)))
            foundMax.set(t);
        })
        .filter(t -> p.test(boundary - f.apply(t)))
        .findFirst();
    return v.isPresent() ?
        v :
        Optional.ofNullable(foundMax.get());
  }

  public static <T> Set<T> intersection(Set<T> a, Set<T> b) {
    return a.size() <= b.size() ?
        _intersection(a, b) :
        _intersection(b, a);
  }

  private static <T> Set<T> _intersection(Set<T> a, Set<T> b) {
    return a.stream().filter(b::contains).collect(
        toLinkedHashSet()
    );
  }


  public static <T> Collector<T, Set<T>, Set<T>> toLinkedHashSet() {
    return Collector.of(
        LinkedHashSet::new,
        Set::add,
        (left, right) -> {
          left.addAll(right);
          return left;
        }
    );
  }

  public static <T> List<T> append(List<T> a, List<T> b) {
    return Stream.concat(a.stream(), b.stream()).collect(toList());
  }

  public static <T> List<T> sublist(List<T> list, int fromIndex, int toIndex) {
    return list.subList(
        fromIndex,
        toIndex < list.size() ?
            toIndex :
            list.size()
    );
  }
}
