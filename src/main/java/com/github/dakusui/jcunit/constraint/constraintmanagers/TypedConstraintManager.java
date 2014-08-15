package com.github.dakusui.jcunit.constraint.constraintmanagers;

import com.github.dakusui.jcunit.core.TestCaseUtils;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.exceptions.JCUnitSymbolException;

import java.lang.reflect.ParameterizedType;
import java.util.LinkedList;
import java.util.List;

public abstract class TypedConstraintManager<T extends Object>
    extends ConstraintManagerBase {
  @Override public final boolean check(Tuple tuple) throws JCUnitSymbolException {
    return check(TestCaseUtils.toTestObject(getTestClass(), tuple), tuple);
  }

  /**
   * {@code tuple} is used to check if required attributes are actually present
   * in the test case. This is necessary because user codes can't tell if a
   * certain field's value is assigned by JCUnit or just a default value.
   *
   * @param o A test object.
   * @param tuple A tuple fromm which {@code o} is generated.
   * @return true - constraint check is passed / false - otherwise.
   */
  protected abstract boolean check(T o, Tuple tuple) throws JCUnitSymbolException;

  @Override public final List<Tuple> getViolations() {
    List<Tuple> ret = new LinkedList<Tuple>();
    for (T testObject : getViolationTestObjects()) {
      ret.add(TestCaseUtils.toTestCase(testObject));
    }
    return ret;
  }

  protected abstract List<T> getViolationTestObjects();

  protected Class<T> getTestClass() {
    return (Class<T>) ((ParameterizedType) this.getClass()
        .getGenericSuperclass()).getActualTypeArguments()[0];
  }

  protected T toTestObject(Tuple t) {
    return TestCaseUtils.toTestObject(getTestClass(), t);
  }
}
