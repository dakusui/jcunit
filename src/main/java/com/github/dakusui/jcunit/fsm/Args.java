package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.Checks;
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

    public static class Factory {
        final Map<Action, Param[]> paramsMap = new HashMap<Action, Param[]>();

        public void register(Action action, Param[] params) {
            Checks.checknotnull(action);
            Checks.checknotnull(params);
            this.paramsMap.put(action, params);
        }

        public Args createArgs(Action action, Tuple testCase) {
            Checks.checknotnull(testCase);
            Checks.checkcond(paramsMap.containsKey(action));
            Param[] params = paramsMap.get(action);
            List<Object> values = new ArrayList<Object>(params.length);
            for (Param each : params) {
                Checks.checkcond(testCase.containsKey(each.name));
                Object v = testCase.get(each.name);
                values.add(v);
            }
            return new Args(values.toArray());
        }
    }
}
