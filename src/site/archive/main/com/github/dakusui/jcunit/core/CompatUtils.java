package com.github.dakusui.jcunit.core;

import com.github.dakusui.jcunit.core.reflect.ReflectionUtils;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.utils.Checks;
import com.github.dakusui.jcunit.exceptions.InvalidPluginException;
import com.github.dakusui.jcunit.exceptions.JCUnitEnvironmentException;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;

import java.util.LinkedList;
import java.util.List;

public class CompatUtils {
  private static final Class<?>[][] primitivesAndWrappers = new Class<?>[][] {
      new Class[] { boolean.class, Boolean.class },
      new Class[] { byte.class, Byte.class },
      new Class[] { char.class, Character.class },
      new Class[] { short.class, Short.class },
      new Class[] { int.class, Integer.class },
      new Class[] { long.class, Long.class },
      new Class[] { float.class, Float.class },
      new Class[] { double.class, Double.class },
  };

  /**
   * @see com.github.dakusui.jcunit.exceptions.InvalidPluginException
   */
  public static void checkplugin(boolean cond, String msg, Object... args) {
    if (!cond) {
      throw new InvalidPluginException(Checks.composeMessage(msg, args));
    }
  }

  public static void checksymbols(Tuple tuple, String... factorNames) throws UndefinedSymbol {
    List<String> missings = new LinkedList<>();
    for (String each : factorNames) {
      if (!Checks.checknotnull(tuple).containsKey(each)) {
        missings.add(each);
      }
    }
    if (!missings.isEmpty()) {
      throw new UndefinedSymbol(missings.toArray(new String[missings.size()]));
    }
  }

  /**
   * A message set to the exception will be composed in the same manner as {@code checknotnull} method.
   *
   * @see Checks#checknotnull(Object, String, Object...)
   */
  public static void checkenv(boolean cond, String msg, Object... args) {
    if (!cond) {
      throw new JCUnitEnvironmentException(Checks.composeMessage(msg, args), null);
    }
  }

  public static RuntimeException wrappluginerror(Throwable throwable, String msgOrFmt, Object... args) {
    throw new InvalidPluginException(Checks.composeMessage(msgOrFmt, args), throwable);
  }

  public static <T> T cast(Class<T> clazz, Object parameter) {
    Checks.checkcond(
        ReflectionUtils.isAssignable(Checks.checknotnull(clazz), parameter),
        "Type mismatch. Required:%s Found:%s",
        clazz,
        parameter
    );
    //noinspection unchecked
    return (T) parameter;
  }

  public static File determineTestSuiteFile(Class<?> testClass) {
    String fqcn = Checks.checknotnull(testClass).getCanonicalName();
    String filename = String.format("%s/testsuites/%s/testsuite.dat", SystemProperties.jcunitBaseDir(), fqcn);
    return new File(filename);
  }

  public static Tuple loadTuple(InputStream is) {
    Object obj;
    try {
      obj = load(is);
    } catch (JCUnitException e) {
      throw new SavedObjectBrokenException("Saved object was broken.", e);
    }
    if (obj instanceof Tuple) {
      return (Tuple) obj;
    }
    throw new SavedObjectBrokenException(String.format("Saved object wasn't a tuple (%s)", obj.getClass().getCanonicalName()), null);
  }

  public static Method[] getAnnotatedMethods(Class<?> clazz, Class<? extends Annotation> annClass) {
    List<Method> methods = getMethods(clazz);
    List<Method> ret = new ArrayList<Method>(methods.size());
    for (Method m : methods) {
      if (m.getAnnotation(annClass) != null) {
        ret.add(m);
      }
    }
    Collections.sort(ret, new Comparator<Method>() {
      @Override
      public int compare(Method o1, Method o2) {
        return o1.getName().compareTo(o2.getName());
      }
    });
    return ret.toArray(new Method[ret.size()]);
  }

  /**
   * Internally does {@code Class#getDeclaredField}. To be used with {@code getFieldValueForcibly}.
   */
  public static Field getFieldDeclaredIn(Class<?> clazz, String name) {
    try {
      return Checks.checknotnull(clazz).getDeclaredField(Checks.checknotnull(name));
    } catch (NoSuchFieldException e) {
      String msg = String.format(
          "Field '%s' isn't defined in class '%s' or not public: canonical name='%s'",
          name,
          clazz.getSimpleName(),
          clazz.getCanonicalName());
      throw new IllegalArgumentException(msg, e);
    }
  }

  public static <T> T getFieldValueForcibly(Object obj, Field f) {
    Checks.checknotnull(f).setAccessible(true);
    try {
      return getFieldValue(obj, f);
    } finally {
      f.setAccessible(false);
    }
  }

  public static <T> T invokeForcibly(Object obj, Method method, Object... args) {
    method.setAccessible(true);
    try {
      return invoke(obj, method, args);
    } finally {
      method.setAccessible(false);
    }
  }

  public static void setFieldValue(Object obj, Field f, Object value) {
    Checks.checknotnull(obj);
    Checks.checknotnull(f);
    boolean accessible = f.isAccessible();
    try {
      f.setAccessible(true);
      f.set(obj, value);
    } catch (IllegalAccessException e) {
      // This path should never be executed since the field is set accessible.
      throw Checks.wrap(e, "Something went wrong.");
    } finally {
      f.setAccessible(accessible);
    }
  }

  public static Class<?>[] primitiveClasses() {
    Class<?>[] ret = new Class[primitivesAndWrappers.length];
    for (int i = 0; i < ret.length; i++) {
      ret[i] = primitivesAndWrappers[i][0];
    }
    return ret;
  }

  public static Class<?> wrapperToPrimitive(Class<?> c) {
    Checks.checknotnull(c);
    Checks.checkcond(isWrapper(c));
    for (Class<?>[] each : primitivesAndWrappers) {
      if (each[1].equals(c))
        return each[0];
    }
    assert false : "c=" + c;
    throw new RuntimeException();
  }


  public static boolean isWrapper(Class<?> c) {
    Checks.checknotnull(c);
    for (Class<?>[] each : primitivesAndWrappers) {
      if (each[1].equals(c))
        return true;
    }
    return false;
  }

  public static boolean isAssignable(Class<?> to, Object value) {
    Checks.checknotnull(to);
    if (value == null) {
      return !to.isPrimitive();
    }
    return isAssignable(to, value.getClass());
  }

  public static boolean isAssignable(Class<?> to, Class<?> from) {
    Checks.checknotnull(to);
    Checks.checknotnull(from);
    return to.isAssignableFrom(from) || isWrapperOf(to, from) || isPrimitiveOf(to, from);
  }

  /**
   * Invokes a {@code method } on {@code obj} with {@code args}.
   * Caller must be responsible for checking the returned value's type.
   *
   * @param obj    An object on which {@code method} is invoked.
   * @param method A {@code method} to be invoked.
   * @param args   Arguments given to {@code method}.
   * @param <T>    Category of returned value from {@code method}.
   */
  public static <T> T invoke(Object obj, Method method, Object... args) {
    try {
      //noinspection unchecked
      return (T) Checks.checknotnull(method).invoke(obj, args);
    } catch (InvocationTargetException e) {
      throw Checks.wrap(e.getTargetException(), "Failed to execute method '%s' with ", method, args);
    } catch (IllegalAccessException e) {
      throw Checks.wrap(e, "A method '%s' is too less open. Make it public.", method);
    }
  }


  private static boolean isWrapperOf(Class<?> a, Class<?> b) {
    for (Class<?>[] each : primitivesAndWrappers) {
      if (Arrays.equals(each, new Class<?>[] { b, a }))
        return true;
    }
    return false;
  }

  private static boolean isPrimitiveOf(Class<?> a, Class<?> b) {
    for (Class<?>[] each : primitivesAndWrappers) {
      if (Arrays.equals(each, new Class<?>[] { a, b }))
        return true;
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  public static <T> T getDefaultValueOfAnnotation(
      Class<? extends Annotation> klazz, String method) {
    Checks.checknotnull(klazz);
    Checks.checknotnull(method);
    try {
      return (T) klazz.getDeclaredMethod(method).getDefaultValue();
    } catch (NoSuchMethodException e) {
      throw Checks.wrap(e);
    }
  }


  public static Field getField(Object obj, String fieldName,
      Class<? extends Annotation>... expectedAnnotations) {
    Checks.checknotnull(obj);
    Checks.checknotnull(fieldName);
    Class<?> clazz = obj.getClass();
    return getFieldFromClass(clazz, fieldName, expectedAnnotations);
  }


  public static Field[] getAnnotatedFields(Class<?> clazz,
      Class<? extends Annotation> annClass) {
    List<Field> fields = getFields(clazz);
    List<Field> ret = new ArrayList<Field>(fields.size());
    for (Field f : fields) {
      if (f.getAnnotation(annClass) != null) {
        ret.add(f);
      }
    }
    Collections.sort(ret, new Comparator<Field>() {
      @Override
      public int compare(Field o1, Field o2) {
        return o1.getName().compareTo(o2.getName());
      }
    });
    return ret.toArray(new Field[ret.size()]);
  }

  public static boolean hasField(Class<?> clazz, String fieldName, FieldChecker... checkers ) {
    Checks.checknotnull(clazz);
    Checks.checknotnull(fieldName);
    Field field = ReflectionUtils.getField(clazz, fieldName);
    for (FieldChecker each : checkers) {
      if (!each.check(field)) return false;
    }
    return true;
  }

  public static Field getFieldFromClass(Class<?> clazz, String fieldName,
      Class<? extends Annotation>... expectedAnnotations) {
    Checks.checknotnull(clazz);
    Checks.checknotnull(fieldName);
    Field ret = getField(clazz, fieldName);
    if (expectedAnnotations.length > 0) {
      for (Class<? extends Annotation> expectedAnnotation : expectedAnnotations) {
        Checks.checknotnull(expectedAnnotation);
        if (ret.isAnnotationPresent(expectedAnnotation)) {
          return ret;
        }
      }
      Checks.checkparam(false,
          String.format(
              "Field '%s' is found in '%s, but not annotated with none of [%s]",
              fieldName,
              clazz,
              StringUtils.join(",", new StringUtils.Formatter<Class<? extends Annotation>>() {
                    @Override
                    public String format(Class<? extends Annotation> elem) {
                      return elem.getSimpleName();
                    }
                  },
                  expectedAnnotations)
          )
      );
    }
    return ret;
  }

  public static Field getField(Class<?> clazz, String name) {
    try {
      return Checks.checknotnull(clazz).getField(Checks.checknotnull(name));
    } catch (NoSuchFieldException e) {
      String msg = String.format(
          "Field '%s' isn't defined in class '%s' or not public: canonical name='%s'",
          name,
          clazz.getSimpleName(),
          clazz.getCanonicalName());
      throw new IllegalArgumentException(msg, e);
    }
  }


  public interface FieldChecker {
    boolean check(Field field);

    enum Basic implements FieldChecker {
      IS_PUBLIC {
        @Override
        public boolean check(Field field) {
          return Modifier.isPublic(Checks.checknotnull(field).getModifiers());
        }
      };
      public static FieldChecker typeOf(final Class<?> target) {
        return new FieldChecker() {
          @Override
          public boolean check(Field field) {
            return target.isAssignableFrom(Checks.checknotnull(field).getType());
          }
        };
      }

      public static FieldChecker hasAnnotation(final Class<? extends Annotation> annotationClass) {
        return new FieldChecker() {
          @Override
          public boolean check(Field field) {
            return Checks.checknotnull(field).getAnnotation(annotationClass) != null;
          }
        };
      }
    }
  }

  public static void save(Tuple tuple, OutputStream os) {
    IOUtils.save(tuple, os);
  }

  public static <T> boolean containsAny(List<T> a, List<T> b) {
    boolean ret = false;
    for (T outer : a) {
      for (T inner : b) {
        if (Utils.eq(outer, inner))
          return true;
      }
    }
    return false;
  }

  /**
   * Returns a new list whose elements are coming from a parameter list {@code in}, but
   * each of them appears only once.
   * <p/>
   * Note that this method is not efficient if the size of {@code in} is very big.
   * it is implemented only for internal use of JCUnit.
   *
   * @param in List whose elements to be made  unique.
   */
  public static <T> List<T> dedup(List<T> in) {
    checknotnull(in);
    List<T> ret = new ArrayList<T>(in.size());
    for (T each : in) {
      if (ret.contains(each))
        continue;
      ret.add(each);
    }
    return ret;
  }

  public static boolean deepEq(Object a, Object b) {
    if (a == null || b == null) {
      return b == a;
    }
    if (!a.getClass().isArray() || !b.getClass().isArray()) {
      return a.equals(b);
    }

    int lena = Array.getLength(a);
    if (lena != Array.getLength(b)) {
      return false;
    }
    if (!a.getClass().equals(b.getClass())) {
      return false;
    }
    for (int i = 0; i < lena; i++) {
      if (!deepEq(Array.get(a, i), Array.get(b, i)))
        return false;
    }
    return true;
  }

  public static <T> Predicate<T> alwaysTrue() {
    //noinspection unchecked
    return (Predicate<T>) ALWAYS_TRUE;
  }

  private static final Predicate ALWAYS_TRUE = new Predicate() {
    @Override
    public boolean apply(Object in) {
      return true;
    }
  };


  public static <K, V> Map<K, V> newMap() {
    return newMap(Collections.<K, V>emptyMap());
  }

  public static <K, V> Map<K, V> newMap(Map<K, V> from) {
    return new LinkedHashMap<K, V>(from);
  }

  public static <E> Set<E> newSet() {
    return new LinkedHashSet<E>();
  }

  public static <T> List<T> newList(T... elements) {
    List<T> ret = new ArrayList<T>(elements.length);
    ret.addAll(asList(elements));
    return ret;
  }

  public static <T> List<T> newUnmodifiableList(List<? extends T> elements) {
    //noinspection RedundantTypeArguments
    return Collections.<T>unmodifiableList(newList(elements));
  }

  public static <T> List<T> newList() {
    return newList(Collections.<T>emptyList());
  }

  public static <T> LinkedHashSet<T> toLinkedHashSet(List<T> list) {
    return new LinkedHashSet<T>(list);
  }

  public interface Consumer<I> {
    void accept(I t);
  }


  public static <T> List<T> newList(List<T> elements) {
    return new ArrayList<T>(elements);
  }

  public static <T> List<T> toList(LinkedHashSet<T> set) {
    return new LinkedList<T>(set);
  }

  /**
   * Returns {@code true} if {@code v} and {@code} are equal,
   * {@code false} otherwise.
   */
  public static boolean eq(Object v, Object o) {
    if (v == null) {
      return o == null;
    }
    return v.equals(o);
  }


  /**
   * If a list {@code in} is immutable and efficient at index access, such as
   * {@code ArrayList}, which is recommended to use in JCUnit, consider using
   * this method.
   *
   * @param in   input list.
   * @param form A form to translate input to output
   * @param <I>  Input type
   * @param <O>  Output type
   */
  public static <I, O> List<O> transformLazily(final List<I> in, final Form<I, O> form) {
    return new AbstractList<O>() {
      @Override
      public O get(int index) {
        return form.apply(in.get(index));
      }

      @Override
      public int size() {
        return in.size();
      }
    };
  }

  public static <T> Predicate<T> not(final Predicate<T> predicate) {
    checknotnull(predicate);
    return new Predicate<T>() {
      @Override
      public boolean apply(T in) {
        return !predicate.apply(in);
      }
    };
  }

  public static <I, O> List<O> transform(Iterable<? extends I> in, Form<I, O> form) {
    List<O> ret = new ArrayList<O>();
    for (I each : in) {
      ret.add(form.apply(each));
    }
    return ret;
  }

  public static <V> List<V> filter(Iterable<V> unfiltered, Predicate<V> predicate) {
    checknotnull(unfiltered);
    checknotnull(predicate);
    List<V> ret = new LinkedList<V>();
    for (V each : unfiltered) {
      if (predicate.apply(each))
        ret.add(each);
    }
    return ret;
  }

  public static <T> T debug(T value) {
    return debug(value, t -> t);
  }

  public static <T> T debug(T value, Function<T, Object> formatter) {
    System.out.println(formatter.apply(value));
    return value;
  }
}
