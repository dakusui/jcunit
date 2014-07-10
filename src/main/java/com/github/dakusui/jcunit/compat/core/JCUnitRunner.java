package com.github.dakusui.jcunit.compat.core;

import com.github.dakusui.jcunit.core.Utils;
import org.junit.Rule;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class JCUnitRunner extends BlockJUnit4ClassRunner {
  private final List<Object> fParameterList;

  private final int fParameterSetNumber;

  JCUnitRunner(Class<?> type, List<Object> parameterList, int i)
      throws InitializationError {
    super(type);
    fParameterList = parameterList;
    fParameterSetNumber = i;
  }

  @Override
  protected Statement classBlock(RunNotifier notifier) {
    return childrenInvoker(notifier);
  }

  private Object computeParams() throws Exception {
    return fParameterList.get(fParameterSetNumber);
  }

  @Override
  public Object createTest() throws Exception {
    TestClass klazz = getTestClass();
    Object ret = klazz.getJavaClass().newInstance();
    Map<Field, Object> values = JCUnit.cast(computeParams());
    CompatUtils.initializeTestObject(ret, values);
    for (RuleSet cur : getRuleSets(ret)) {
      cur.setInValues(values);
    }
    return ret;
  }

  private RuleSet[] getRuleSets(Object cut) {
    List<RuleSet> ret = new ArrayList<RuleSet>();
    for (Field f : cut.getClass().getFields()) {
      Object v = Utils.getFieldValue(cut, f);
      if (v != null && v instanceof RuleSet
          && f.getAnnotation(Rule.class) != null) {
        ret.add((RuleSet) v);
      }
    }
    return ret.toArray(new RuleSet[] { });
  }

  @Override
  protected String getName() {
    return String.format("[%s]", fParameterSetNumber);
  }

  @Override
  protected String testName(final FrameworkMethod method) {
    return String.format("%s[%s]", method.getName(), fParameterSetNumber);
  }

  @Override
  protected void validateConstructor(List<Throwable> errors) {
    validateZeroArgConstructor(errors);
  }

}