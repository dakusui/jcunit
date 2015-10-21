package com.github.dakusui.jcunit.core;

import com.github.dakusui.jcunit.core.factor.LevelsProvider;
import com.github.dakusui.jcunit.core.factor.LevelsProviderFactory;
import com.github.dakusui.jcunit.core.reflect.ReflectionUtils;
import com.github.dakusui.jcunit.exceptions.JCUnitException;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * A utility class of JCUnit.
 * <p/>
 * In case there is a good library and I want to use the functionality of it in JCUnit, I
 * usually mimic it here (except {@code Preconditions} of Guava, it's in {@code Checks})
 * instead of adding dependency on it.
 * <p/>
 * This is because JCUnit's nature which should be able to be used for any other software
 * (at least as much as possible, I want to make it so).
 */
public class Utils {
  private Utils() {
  }

  public static String format(String format, Object... args) {
    return String.format(
        Checks.checknotnull(format),
        Utils.transform(args, new Utils.Form<Object, Object>() {
          @Override
          public Object apply(Object in) {
            if (in == null)
              return null;
            Class<?> toStringDeclaringClass = ReflectionUtils.getMethod(in.getClass(), "toString").getDeclaringClass();
            if (Object.class.equals(toStringDeclaringClass)) {
              return getSimpleClassName(in) + "@" + System.identityHashCode(in);
            }
            return in;
          }
        }).toArray());
  }

  public static String getSimpleClassName(Object obj) {
    String className;
    className = "".equals(Checks.checknotnull(obj).getClass().getSimpleName())
        ? "(anonymous)"
        : obj.getClass().getSimpleName();
    return className;
  }

  public static Field getField(Object obj, String fieldName,
      Class<? extends Annotation>... expectedAnnotations) {
    Checks.checknotnull(obj);
    Checks.checknotnull(fieldName);
    Class<?> clazz = obj.getClass();
    return getFieldFromClass(clazz, fieldName, expectedAnnotations);
  }

  public static Field getFieldFromClass(Class<?> clazz, String fieldName,
      Class<? extends Annotation>... expectedAnnotations) {
    Checks.checknotnull(clazz);
    Checks.checknotnull(fieldName);
    Field ret = ReflectionUtils.getField(clazz, fieldName);
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
              Utils.join(",", new Formatter<Class<? extends Annotation>>() {
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

  /**
   * Joins given string objects with {@code sep} using {@code formatter}.
   * <p/>
   * This method is implemented in order to reduce dependencies on external libraries.
   *
   * @param sep       A separator to be used to join {@code elemes}.
   * @param formatter A formatter used to join strings.
   * @param elems     Elements to be joined.
   * @return A joined {@code String}
   */
  public static <T> String join(String sep, Formatter<T> formatter,
      T... elems) {
    Checks.checknotnull(sep);
    StringBuilder b = new StringBuilder();
    boolean firstOne = true;
    for (T s : elems) {
      if (!firstOne) {
        b.append(sep);
      }
      b.append(formatter.format(s));
      firstOne = false;
    }
    return b.toString();
  }

  /**
   * Joins given string objects with {@code sep} using {@code Formatter.INSTANCE}.
   * <p/>
   * This method is implemented in order to reduce dependencies on external libraries.
   *
   * @param sep   A separator to be used to join {@code elemes}
   * @param elems Elements to be joined.
   * @return A joined {@code String}
   */
  public static String join(String sep, Object... elems) {
    return join(sep, Formatter.INSTANCE, elems);
  }


  public static Field[] getAnnotatedFields(Class<?> clazz,
      Class<? extends Annotation> annClass) {
    Field[] fields = clazz.getFields();
    List<Field> ret = new ArrayList<Field>(fields.length);
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

  public static <T> T createNewInstanceUsingNoParameterConstructor(
      Class<? extends T> klazz) {
    T ret = null;
    try {
      ret = klazz.getDeclaredConstructor().newInstance();
    } catch (InstantiationException e) {
      Checks.rethrow(e,
          "'%s' is a class that cannot be instantiated directly.",
          klazz.getCanonicalName());
    } catch (IllegalAccessException e) {
      Checks.rethrow(e,
          "Failed to instantiate '%s'. The constructor with no parameter is not open enough.",
          klazz.getCanonicalName()
      );
    } catch (InvocationTargetException e) {
      Checks.rethrow(e.getTargetException(),
          "Failed to instantiate '%s'. An exception was thrown during instantiation.",
          klazz.getCanonicalName());
    } catch (NoSuchMethodException e) {
      Checks.rethrow(e,
          "Failed to instantiate '%s'. A constructor with no parameter is not found.",
          klazz.getCanonicalName()
      );
    }
    Checks.checknotnull(ret);
    return ret;
  }

  @SuppressWarnings("unchecked")
  public static <T> T getDefaultValueOfAnnotation(
      Class<? extends Annotation> klazz, String method) {
    Checks.checknotnull(klazz);
    Checks.checknotnull(method);
    try {
      return (T) klazz.getDeclaredMethod(method).getDefaultValue();
    } catch (NoSuchMethodException e) {
      Checks.rethrow(e);
    }
    Checks.checkcond(false, "Something went wrong. This line shouldn't be executed.");
    return null;
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

  /**
   * Creates a file using {@code java.io.File#createNewFile()} method.
   *
   * @param file A file to be created.
   * @return true - created / false - not created.
   * @see java.io.File
   */
  public static boolean createFile(File file) {
    Checks.checknotnull(file);
    try {
      return file.createNewFile();
    } catch (IOException e) {
      Checks.rethrow(e);
    }
    return false;
  }

  public static BufferedOutputStream openForWrite(File f) {
    BufferedOutputStream ret = null;
    try {
      ret = new BufferedOutputStream(new FileOutputStream(f));
    } catch (FileNotFoundException e) {
      Checks.rethrow(e);
    }
    return ret;
  }

  public static BufferedInputStream openForRead(File f) {
    Checks.checknotnull(f);
    BufferedInputStream ret = null;
    try {
      ret = new BufferedInputStream(new FileInputStream(f));
    } catch (FileNotFoundException e) {
      Checks.rethrow(e, "File not found: '%s'", f.getAbsolutePath());
    }
    return ret;
  }

  public static void close(Closeable stream) {
    try {
      stream.close();
    } catch (IOException e) {
      Checks.rethrow(e);
    }
  }

  /**
   * Saves a given object to a file.
   *
   * @param obj An object to be saved.
   * @param to  A file to which {@code obj} is saved.
   */
  public static void save(Object obj, File to) {
    BufferedOutputStream bos;
    bos = Utils.openForWrite(to);
    try {
      save(obj, bos);
    } finally {
      Utils.close(bos);
    }
  }

  public static void save(Object obj, OutputStream os) {
    Checks.checknotnull(obj);
    Checks.checknotnull(os);

    try {
      ObjectOutputStream oos = new ObjectOutputStream(os);
      try {
        oos.writeObject(obj);
      } finally {
        oos.close();
      }
    } catch (IOException e) {
      Checks.rethrow(e);
    }
  }

  public static Object load(File f) {
    Checks.checknotnull(f);
    BufferedInputStream bis;
    bis = Utils.openForRead(f);
    try {
      return load(bis);
    } finally {
      Utils.close(bis);
    }
  }

  public static Object load(InputStream is) {
    Checks.checknotnull(is);
    Object ret = null;
    try {
      ObjectInputStream ois = new ObjectInputStream(is);
      try {
        ret = ois.readObject();
      } catch (ClassNotFoundException e) {
        Checks.rethrow(e);
      } finally {
        ois.close();
      }
    } catch (IOException e) {
      Checks.rethrow(e);
    }
    return ret;
  }


  /**
   * By default File#delete fails for non-empty directories, it works like "rm".
   * We need something a little more brutal - this does the equivalent of "rm -r"
   * <p/>
   * This method is cited from the url indicated in the link.
   *
   * @param path Root File Path
   * @return true iff the file and all sub files/directories have been removed
   * @link "http://stackoverflow.com/questions/779519/delete-files-recursively-in-java"
   */
  public static boolean deleteRecursive(File path) {
    Checks.checknotnull(path);
    if (!path.exists()) {
      throw new JCUnitException(
          String.format("Path '%s' was not found.", path.getAbsolutePath()), null);
    }
    boolean ret = true;
    if (path.isDirectory()) {
      //noinspection ConstantConditions
      for (File f : path.listFiles()) {
        ret = ret && Utils.deleteRecursive(f);
      }
    }
    return ret && path.delete();
  }

  public static <T> List<T> dedup(Iterable<T> in) {
    List<T> ret = new LinkedList<T>();
    for (T each : Checks.checknotnull(in)) {
      if (!ret.contains(each))
        ret.add(each);
    }
    return ret;
  }

  /**
   * Returns a list whose members are coming from a parameter list {@code in}, but
   * each of them appears only once.
   * <p/>
   * Note that this method is not efficient if the size of {@code in} is very big.
   * it is implemented only for internal use of JCUnit.
   *
   * @param in List to be made a singleton.
   */
  public static <T> List<T> singleton(List<T> in) {
    Checks.checknotnull(in);
    List<T> ret = new ArrayList<T>(in.size());
    for (T each : in) {
      if (ret.contains(each))
        continue;
      ret.add(each);
    }
    return ret;
  }

  public static <I, O> List<O> transform(Iterable<I> in, Form<I, O> form) {
    List<O> ret = new ArrayList<O>();
    for (I each : in) {
      ret.add(form.apply(each));
    }
    return ret;
  }

  public static <I, O> List<O> transform(I[] in, Form<I, O> form) {
    return transform(
        Arrays.asList(in),
        Checks.checknotnull(form)
    );
  }

  public static <K, V> Map<K, V> toMap(List<V> in, Form<V, K> form) {
    Checks.checknotnull(in);
    Checks.checknotnull(form);
    ////
    // In most cases, it's better to use LinkedHashMap in JCUnit because
    // it needs to guarantee the test case generation result the same always.
    // So this method returns LinkedHashMap instead of HashMap.
    Map<K, V> ret = new LinkedHashMap<K, V>();
    for (V each : in) {
      ret.put(form.apply(each), each);
    }
    return ret;
  }

  public static <V> List filter(List<V> unfiltered, Predicate<V> predicate) {
    Checks.checknotnull(unfiltered);
    Checks.checknotnull(predicate);
    List<V> ret = new LinkedList<V>();
    for (V each : unfiltered) {
      if (predicate.apply(each))
        ret.add(each);
    }
    return ret;
  }

  public static <T> T[] concatenate(T[] a, T[] b) {
    int aLen = a.length;
    int bLen = b.length;

    @SuppressWarnings("unchecked")
    T[] c = (T[]) Array.newInstance(a.getClass().getComponentType(), aLen + bLen);
    System.arraycopy(a, 0, c, 0, aLen);
    System.arraycopy(b, 0, c, aLen, bLen);

    return c;
  }

  public static ValidationResult validateFactorField(Field f) {
    Checks.checknotnull(f);
    List<String> errors = new LinkedList<String>();
    ValidationResult ret;
    FactorField ann = f.getAnnotation(FactorField.class);
    LevelsProvider levelsProvider = null;
    if (ann == null) {
      errors.add(Utils.format(
          "%s annotation is present at %s#%s",
          FactorField.class,
          f.getType(),
          f.getName()));
    } else {
      levelsProvider = LevelsProviderFactory.INSTANCE.createLevelsProvider(
          f,
          ann,
          errors
      );
      errors.addAll(levelsProvider.getErrorsOnInitialization());
    }
    if (levelsProvider == null) {
      errors.add("LevelsProvider couldn't be created");
    } else {
      levelsProvider.setAnnotation(ann);
      levelsProvider.setTargetField(f);
      levelsProvider.init(ann.providerParams());
    }

    ret = new ValidationResult(
        errors.isEmpty(),
        levelsProvider,
        join("; ", errors.toArray()
        ));
    return ret;
  }

  public interface Formatter<T> {
    Formatter INSTANCE = new Formatter<Object>() {
      @Override
      public String format(Object elem) {
        if (elem == null) {
          return null;
        }
        return elem.toString();
      }
    };

    String format(T elem);
  }

  public interface Form<I, O> {
    O apply(I in);
  }

  public interface Predicate<I> {
    boolean apply(I in);
  }

  public static class ValidationResult {
    private final boolean        valid;
    private final String         errMessage;
    private final LevelsProvider levelsProvider;

    public ValidationResult(boolean valid,
        LevelsProvider levelsProvider, String errorMessage) {
      if (valid) {
        Checks.checknotnull(levelsProvider);
      } else {
        Checks.checknotnull(errorMessage);
      }
      this.valid = valid;
      this.levelsProvider = levelsProvider;
      this.errMessage = errorMessage;
    }

    public LevelsProvider getLevelsProvider() {
      return levelsProvider;
    }

    public void check() {
      Checks.checktest(this.valid, errMessage);
    }
  }
}
