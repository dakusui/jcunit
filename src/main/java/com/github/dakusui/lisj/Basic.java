package com.github.dakusui.lisj;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.lang3.ArrayUtils;

import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.jcunit.exceptions.ObjectUnderFrameworkException;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;

public class Basic {
  public static final Object NIL = new Object[0];

  public static boolean eq(Object a, Object b) {
    if (a == b)
      return true;
    if (a == null || b == null)
      return false;
    if (a.equals(b))
      return true;
    if (a instanceof Object[] && b instanceof Object[])
      return Arrays.deepEquals(((Object[]) a), ((Object[]) b));
    if (a instanceof Number && b instanceof Number)
      return Utils.bigDecimal((Number) a).equals(Utils.bigDecimal((Number) b));
    return a.equals(b);
  }

  public static Object cons(Object car, Object cdr) {
    if (eq(cdr, NIL))
      return new Object[] { car };
    return new Object[] { car, cdr };
  }

  public static Object car(Object var) {
    if (var == null)
      throw new NullPointerException();
    if (atom(var))
      throw new IllegalArgumentException();
    Object[] vars = (Object[]) var;
    if (vars.length == 0)
      return NIL;
    if (normalizeNIL(vars[0]) == NIL)
      return NIL;
    return vars[0];
  }

  public static Object cdr(Object var) {
    if (var == null)
      throw new NullPointerException();
    if (eq(NIL, var))
      return NIL;
    if (atom(var))
      throw new IllegalArgumentException();
    Object[] vars = (Object[]) var;
    if (vars.length == 1)
      return NIL;
    if (vars.length == 2) {
      if (atom(vars[1])) {
        if (eq(vars[1], NIL))
          return new Object[] { NIL };
        return new Object[] { vars[1] };
      } else
        return vars[1];
    }
    return ArrayUtils.subarray(vars, 1, vars.length);
  }

  /**
   * Returns the length of a given <code>obj</code> as an S-expression. If
   * <code>obj</code> is an atom and not NIL,
   * <code>IllegalArgumentException</code> will be thrown.
   */
  public static int length(Object obj) {
    if (obj == null)
      throw new NullPointerException();
    if (!(obj instanceof Object[]))
      throw new IllegalArgumentException();
    Object[] arr = ((Object[]) obj);
    int len;
    if ((len = arr.length) <= 1)
      return len;
    return length(arr[len - 1], len);
  }

  private static int length(Object obj, int len) {
    if (atom(obj))
      return len;
    Object[] arr = (Object[]) obj;
    if (arr.length <= 1)
      return len;
    return length(arr[arr.length - 1], len + arr.length - 1);
  }

  public static Object eval(Context context, Object var)
      throws JCUnitException, CUT {
    if (atom(var)) {
      if (var instanceof Symbol) {
        return context.lookup((Symbol) var);
      } else {
        return Utils.normalize(var);
      }
    }
    Object car = car(var);
    Object cdr = cdr(var);
    Form f = null;
    if (car instanceof Form) {
      f = (Form) car;
    } else if (car instanceof Symbol) {
      Object o = context.lookup((Symbol) car);
      if (!(o instanceof Form))
        throw new RuntimeException();
      f = (Form) o;
    } else {
      String msg = String.format(
          "car(%s) of var(%s) must be a form or a symbol.", tostr(car),
          tostr(var));
      throw new IllegalArgumentException(msg);
    }
    if (eq(NIL, cdr)) {
      return f.evaluate(context, NIL);
    }
    return f.evaluate(context, cdr);
  }

  private static Object normalizeNIL(Object var) {
    if (var instanceof Object[] && ((Object[]) var).length == 0)
      return NIL;
    return var;
  }

  public static Object quote(Object o) {
    return o;
  }

  public static Object[] quote(Object... o) {
    return o;
  }

  public static Object[] toarr(Object o) {
    if (eq(o, NIL))
      return (Object[]) NIL;
    if (atom(o))
      return new Object[] { o };
    Object[] arr = (Object[]) o;
    Object last = arr[arr.length - 1];
    if (atom(last))
      return arr;
    Object[] ret = new Object[length(arr)];
    for (int cur = 0;; arr = (Object[]) last, last = arr[arr.length - 1]) {
      System.arraycopy(arr, 0, ret, cur, arr.length - 1);
      cur += arr.length - 1;
      if (atom(last))
        break;
    }
    ret[ret.length - 1] = arr[arr.length - 1];
    return ret;
  }

  public static boolean atom(Object var) {
    if (var == null)
      return true;
    if (var instanceof Object[]) {
      return ((Object[]) var).length == 0;
    }
    return true;
  }

  public static Object arr(Object funcName, Object... args) {
    return new Object[] { new Symbol(funcName.toString()), args };
  }

  public static boolean evalp(Context context, Object predicate)
      throws JCUnitException, CUT {
    Object value = eval(context, predicate);
    if (value instanceof Boolean) {
      return ((Boolean) value);
    }
    String message = String.format(
        "'%s' returned non-boolean value or it is not a predicate", predicate);
    throw new ObjectUnderFrameworkException(message, null);
  }

  public static abstract class SexpIterator implements Iterator<Object>,
      Iterable<Object> {
  }

  public static class ConsIterator extends SexpIterator {
    private ConsIterator(Object target) {
      if (atom(target))
        throw new IllegalArgumentException();
      this.target = target;
    }

    @Override
    public Iterator<Object> iterator() {
      return this;
    }

    int    cur = 0;
    Object target;

    @Override
    public boolean hasNext() {
      if (cur < ((Object[]) target).length)
        return true;
      return false;
    }

    @Override
    public Object next() {
      int len = ((Object[]) target).length;
      if (cur >= len)
        throw new NoSuchElementException();
      if (cur == len - 1) {
        Object last;
        if (atom(last = ((Object[]) target)[cur])) {
          cur++;
          return last;
        } else {
          cur = 1;
          target = last;
          return ((Object[]) last)[0];
        }
      }
      return ((Object[]) target)[cur++];
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

  /*
   * obj_ must be a cons.
   */
  public static SexpIterator iterator(final Object obj) {
    if (atom(obj) || length(obj) == 1)
      return new SexpIterator() {
        boolean hasNext = true;

        @Override
        public boolean hasNext() {
          return this.hasNext;
        }

        @Override
        public Object next() {
          this.hasNext = false;
          return obj;
        }

        @Override
        public void remove() {
          throw new UnsupportedOperationException();
        }

        @Override
        public Iterator<Object> iterator() {
          return this;
        }
      };
    return new ConsIterator(obj);
  }

  /*
   * obj must be a cons.
   */
  public static Object get(Object obj, int index) {
    if (atom(obj))
      throw new IllegalArgumentException();
    int len = ((Object[]) obj).length;
    if (index < len - 1)
      return ((Object[]) obj)[index];
    else if (index == len - 1) {
      if (atom(((Object[]) obj)[index]) || len == 1) {
        return ((Object[]) obj)[index];
      }
    }
    Object last;
    if (atom(last = ((Object[]) obj)[len - 1])) {
      throw new ArrayIndexOutOfBoundsException();
    }
    return get(last, index - (len - 1));
  }

  public static String tostr(Object obj) {
    return tostr(obj, false);
  }

  public static String tostr(Object obj, boolean suppressObjectId) {
    if (obj == null)
      return "null";
    if (eq(obj, NIL))
      return "NIL";
    if (atom(obj))
      return toString(obj, suppressObjectId);

    StringBuilder builder = new StringBuilder(1024);
    Object[] cons = (Object[]) obj;
    tostr(cons, builder, true, suppressObjectId);
    return builder.toString();
  }

  /*
   * If 'obj''s toString is not overridden, this method returns a bit more
   * concise string than the one java.lang.Object#toString method returns, i.e.
   * package names will be omitted. And 'obj' is null, null itself will be
   * returned. Otherwise, the result of 'obj.toString()' will be returned.
   */
  private static String toString(Object obj, boolean suppressObjectId) {
    if (obj == null)
      return null;
    if (obj instanceof Class)
      return ((Class<?>) obj).getSimpleName() + ".class";
    try {
      if (obj instanceof String) {
        String str = (String) obj;
        // //
        // if str looks an FQCN, make it compact.
        return makeFqcnLikeStringCompact(str);
      } else {
        Method objToString = obj.getClass().getMethod("toString");
        // //
        // Checking if 'toString' method is overridden or not.
        Class<?> declaringClass = objToString.getDeclaringClass();
        if (declaringClass == Object.class) {
          String simpleName = obj.getClass().getSimpleName();
          if (suppressObjectId) {
            return simpleName + ".obj";
          }
          return simpleName + "@" + objectId(obj);
        }
      }
    } catch (SecurityException e) {
    } catch (NoSuchMethodException e) {
    }
    return obj.toString();
  }

  /**
   * If a given string looks like an FQCN, this method creates and returns a
   * compact version of it. Otherwise it return the original string itself.
   * 
   * @param str
   *          A string
   * @return A compact version of <code>str</code> or <code>str</code> itself.
   */
  private static String makeFqcnLikeStringCompact(String str) {
    int i;
    // //
    // If it doesn't start or end with a dot and two or more dots are found in
    // the
    // string, this method considers that it looks like an FQCN.
    if (str.endsWith("."))
      return str;
    // //
    // Also if it contains a white space, this method doesn't consider it is
    // an FQCN.
    if (str.contains(" "))
      return str;
    if ((i = str.indexOf('.')) > 0) {
      if ((str.indexOf('.', i + 1)) > i) {
        return str.substring(str.lastIndexOf('.') + 1);
      }
    }
    return str;
  }

  private static int                        nextObjectId = 1;

  private static final Map<Object, Integer> objectMap    = CacheBuilder
                                                             .newBuilder()
                                                             .weakKeys()
                                                             .build(
                                                                 new CacheLoader<Object, Integer>() {
                                                                   @Override
                                                                   public Integer load(
                                                                       Object key)
                                                                       throws Exception {
                                                                     return nextObjectId++;
                                                                   }
                                                                 }).asMap();

  private static int objectId(Object obj) {
    if (obj == null)
      return 0;
    if (!objectMap.containsKey(obj)) {
      objectMap.put(obj, nextObjectId++);
    }
    return objectMap.get(obj);
  }

  private static void tostr(Object[] cons, StringBuilder builder,
      boolean withParentheses, boolean suppressObjectId) {
    if (withParentheses)
      builder.append("(");
    try {
      for (int i = 0; i < cons.length; i++) {
        Object cur = cons[i];
        if (cons.length == 1 || i != cons.length - 1) {
          if (!atom(cur)) {
            tostr((Object[]) cur, builder, true, suppressObjectId);
          } else {
            builder.append(tostr(cur, suppressObjectId));
          }
          if (i != cons.length - 1)
            builder.append(",");
        } else if (atom(cur)) {
          if (eq(cur, NIL))
            builder.append("NIL");
          else
            builder.append(tostr(cur, suppressObjectId));
        } else {
          // cur is a cons at the end of the cons.
          tostr((Object[]) cur, builder, false, suppressObjectId);
        }
      }
    } finally {
      if (withParentheses)
        builder.append(")");
    }
  }
}
