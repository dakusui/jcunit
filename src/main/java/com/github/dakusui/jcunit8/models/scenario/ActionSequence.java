package com.github.dakusui.jcunit8.models.scenario;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.jcunitx.core.KeyValuePair;

import java.util.LinkedList;
import java.util.List;

import static com.github.dakusui.actionunit.core.ActionSupport.sequential;
import static java.util.stream.Collectors.toList;

public interface ActionSequence<D> extends List<ParameterizedAction> {
  default Result perform() {
    return this.perform(DefaultActionPerformer.create(Context.create()));
  }

  default Result perform(ContextHoldingActionPerformer performer) {
    performer.visit(this.toAction());
    return new Result(performer.context());
  }

  default Action toAction() {
    return sequential(this.stream().map(each -> (Action) each).collect(toList()));
  }

  D driverObject();

  class Impl<D> extends LinkedList<ParameterizedAction> implements ActionSequence<D> {
    public Impl(List<ParameterizedAction> actions) {
      this.addAll(actions);
    }

    @Override
    public D driverObject() {
      return null;
    }
  }

  class Result {
    final Context context;

    public Result(Context context) {
      this.context = context;
    }

    @SuppressWarnings("unchecked")
    public <T> T lastValue() {
      List<KeyValuePair<?>> resultArray = resultArray();
      return (T) resultArray.get(resultArray.size() - 1).value();
    }

    public List<KeyValuePair<?>> resultArray() {
      return this.context.valueOf(ParameterizedAction.OUTPUT_VAR_NAME);
    }
  }
}
