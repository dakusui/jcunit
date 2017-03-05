package com.github.dakusui.jcunit.runners.standard;

import com.github.dakusui.combinatoradix.tuple.AttrValue;
import com.github.dakusui.jcunit.core.reflect.ReflectionUtils;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.tuples.TupleUtils;
import com.github.dakusui.jcunit.core.utils.Utils;
import com.github.dakusui.jcunit.runners.standard.annotations.FactorField;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.github.dakusui.jcunit.core.utils.Checks.checknotnull;
import static com.github.dakusui.jcunit.core.utils.Utils.*;
import static java.lang.Math.min;

/**
 */
public enum TestCaseUtils {
  ;

  public static AttrValue<String, Object> factor(String name, Object level) {
    return new AttrValue<String, Object>(name, level);
  }

  public static Tuple newTestCase(AttrValue<String, Object>... attrs) {
    Tuple.Builder b = new Tuple.Builder();
    for (AttrValue<String, Object> attrValue : attrs) {
      b.put(attrValue.attr(), attrValue.value());
    }
    return b.build();
  }

  public static void initializeObjectWithTuple(Object testObject, Tuple tuple) {
    for (String fieldName : tuple.keySet()) {
      Field f;

      //noinspection unchecked
      f = ReflectionUtils.getField(testObject, fieldName, FactorField.class);
      ReflectionUtils.setFieldValue(testObject, f, tuple.get(fieldName));
    }
  }

  public static <T> T toTestObject(Class<T> testClass, Tuple testCase) {
    checknotnull(testClass);
    checknotnull(testCase);
    T ret = ReflectionUtils.create(testClass);
    initializeObjectWithTuple(ret, testCase);
    return ret;
  }

  public static <T> Tuple toTestCase(T testObject) {
    checknotnull(testObject);
    Tuple.Builder b = new Tuple.Builder();
    for (Field each : ReflectionUtils.getAnnotatedFields(testObject.getClass(), FactorField.class)) {
      b.put(each.getName(), ReflectionUtils.getFieldValue(testObject, each));
    }
    return b.build();
  }

  public static List<Tuple> optimize(List<Tuple> tuples, int strength) {
    if (tuples.isEmpty()) {
      return tuples;
    }
    strength = min(strength, tuples.get(0).size());
    final int finalStrength = strength;
    return filter(tuples, new Utils.Predicate<Tuple>() {
      Set<Tuple> alreadyCovered = new HashSet<Tuple>();

      @Override
      public boolean apply(Tuple in) {
        Set<Tuple> currentSubtuples = TupleUtils.subtuplesOf(in, finalStrength);
        if (!alreadyCovered.containsAll(currentSubtuples)) {
          alreadyCovered.addAll(currentSubtuples);
          return true;
        }
        return false;
      }
    });
  }

  public static List<Tuple> unique(List<Tuple> tuples) {
    return toList(toLinkedHashSet(tuples));
  }
}
