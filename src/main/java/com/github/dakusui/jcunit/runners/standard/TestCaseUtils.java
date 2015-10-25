package com.github.dakusui.jcunit.runners.standard;

import com.github.dakusui.combinatoradix.tuple.AttrValue;
import com.github.dakusui.jcunit.runners.standard.annotations.FactorField;
import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.reflect.ReflectionUtils;
import com.github.dakusui.jcunit.core.tuples.Tuple;

import java.lang.reflect.Field;

/**
 */
public class TestCaseUtils {
  private TestCaseUtils() {
  }

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

  public static void initializeObjectWithTuple(Object testObject,
      Tuple tuple) {
    for (String fieldName : tuple.keySet()) {
      Field f;

      //noinspection unchecked
      f = ReflectionUtils.getField(testObject, fieldName,
          FactorField.class);
      ReflectionUtils.setFieldValue(testObject, f, tuple.get(fieldName));
    }
  }

  public static <T> T toTestObject(Class<T> testClass, Tuple testCase) {
    Checks.checknotnull(testClass);
    Checks.checknotnull(testCase);
    T ret = ReflectionUtils.create(testClass);
    initializeObjectWithTuple(ret, testCase);
    return ret;
  }

  public static <T> Tuple toTestCase(T testObject) {
    Checks.checknotnull(testObject);
    Tuple.Builder b = new Tuple.Builder();
    for (Field each : ReflectionUtils.getAnnotatedFields(testObject.getClass(), FactorField.class)) {
      b.put(each.getName(), ReflectionUtils.getFieldValue(testObject, each));
    }
    return b.build();
  }
}
