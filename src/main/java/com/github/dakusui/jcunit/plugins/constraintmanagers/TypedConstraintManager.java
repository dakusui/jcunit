package com.github.dakusui.jcunit.plugins.constraintmanagers;

import com.github.dakusui.jcunit.standardrunner.TestCaseUtils;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;

import java.lang.reflect.ParameterizedType;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public abstract class TypedConstraintManager<T>
    extends ConstraintManagerBase {
  @Override public final boolean check(Tuple tuple)
      throws UndefinedSymbol {
    return check(toTestObject(tuple), tuple);
  }

  /**
   * Checks if a given object {@code o} comply with the constraints defined by this class or not.
   *
   * The second parameter {@code tuple} is used for checking if an attribute in {@code o} is
   * assigned or not.

   * {@code tuple} is used to check if required attributes are actually present
   * in the test case. This is necessary because user codes can't tell if a
   * certain field's value is assigned by JCUnit or just a default value.
   *
   * If {@code false} is returned, a tuple generator may give up
   * covering pairs contained in {@tuple}. The pair coverage will potentially be
   * damaged in case {@code false} is returned incorrectly.
   *
   * It is strongly recommended to check if factors involved in the constraints are
   * already assigned by JCUnit by reading {@code tuple}.
   *
   * @param o     A test object.
   * @param tuple A tuple from which {@code o} is generated.
   * @return true - constraint check is passed / false - otherwise.
   */
  protected abstract boolean check(T o, Tuple tuple)
      throws UndefinedSymbol;

  @Override public final List<Tuple> getViolations() {
    List<Tuple> ret = new LinkedList<Tuple>();
    for (T testObject : getViolationTestObjects()) {
      ret.add(TestCaseUtils.toTestCase(testObject));
    }
    return ret;
  }

  /**
   * By default this method returns an empty list.
   */
  protected List<T> getViolationTestObjects() {
    return Collections.emptyList();
  }

  @SuppressWarnings("unchecked")
  protected Class<T> getTestClass() {
    return (Class<T>) ((ParameterizedType) this.getClass()
        .getGenericSuperclass()).getActualTypeArguments()[0];
  }

  protected T toTestObject(Tuple t) {
    return TestCaseUtils.toTestObject(getTestClass(), t);
  }
}
