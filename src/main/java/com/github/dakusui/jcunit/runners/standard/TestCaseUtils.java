package com.github.dakusui.jcunit.runners.standard;

import com.github.dakusui.combinatoradix.tuple.AttrValue;
import com.github.dakusui.jcunit.core.SystemProperties;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.factor.FactorSpace;
import com.github.dakusui.jcunit.fsm.ScenarioSequence;
import com.github.dakusui.jcunit.runners.standard.annotations.FactorField;
import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.reflect.ReflectionUtils;
import com.github.dakusui.jcunit.core.tuples.Tuple;

import java.lang.reflect.Field;
import java.util.Map;

import static com.github.dakusui.jcunit.core.Checks.checknotnull;

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

  public static void initializeObjectWithTuple(Object testObject, Tuple tuple) {
    for (String fieldName : tuple.keySet()) {
      Field f;

      //noinspection unchecked
      f = ReflectionUtils.getField(testObject, fieldName, FactorField.class);
      ReflectionUtils.setFieldValue(testObject, f, tuple.get(fieldName));
    }
  }

  /**
   * Some attributes in a tuple may or may not be able to assign to fields in a test object
   * in its original form.
   *
   * This method make those attributes assignable to those fields.
   *
   * In other words, attributes like "FSM:simpleFSM:action:0" are aggregated into one "ScenarioSequence"
   * attribute "simpleFSM".
   *
   */
  private static Tuple convertSpecialAttributesIntoAssignable(FactorSpace factorSpace, Tuple tuple) {
    return factorSpace.convert(tuple);
  }

  private static String extractFSMName(String attributeName) {
    return attributeName.split(":")[1];
  }

  private static int extractPositionInScenarioSequence(String attributeName) {
    return Integer.parseInt(attributeName.split(":")[3]);
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
}
