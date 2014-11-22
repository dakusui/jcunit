package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.tuples.Tuple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 */
public class Args {
  private final Object[] values;

  Args(Object[] values) {
    Checks.checknotnull(values);
    this.values = values;
  }

  public Object[] values() {
    return this.values;
  }

  public int size() {
    return this.values.length;
  }

  public static class Factory {
    final Map<Action, Factor[]> paramsMap = new HashMap<Action, Factor[]>();

    public void register(Action action, Factor[] params) {
      Checks.checknotnull(action);
      Checks.checknotnull(params);
      this.paramsMap.put(action, params);
    }

    public Args createArgs(Action action, Tuple testCase) {
      Checks.checknotnull(testCase);
      Checks.checkcond(paramsMap.containsKey(action));
      Factor[] params = paramsMap.get(action);
      List<Object> values = new ArrayList<Object>(params.length);
      for (Factor each : params) {
        Checks.checkcond(testCase.containsKey(each.name));
        Object v = testCase.get(each.name);
        values.add(v);
      }
      return new Args(values.toArray());
    }
  }
}
