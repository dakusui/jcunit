package com.github.dakusui.jcunit8.models.scenario;

import com.github.dakusui.actionunit.actions.Named;
import com.github.dakusui.actionunit.core.ActionSupport;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.models.Parameter;

import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

public interface Scenario extends Parameter.Regex<ParameterizedAction> {
  @Override
  ActionSequence composeValue(Tuple tuple);

  class Impl extends Regex.Impl<ParameterizedAction> implements Scenario {
    public Impl(String name, String regex, List<List<ParameterizedAction>> knownValues, Function<String, ParameterizedAction> func) {
      super(name, regex, knownValues, func);
    }

    @Override
    public ActionSequence composeValue(Tuple tuple) {
      return new ActionSequence.Impl(composeStringValueFrom(tuple).stream().map(func).collect(toList()));
    }
  }

  class Factory extends Regex.Factory<Named> {
    public Factory(String regex) {
      this(regex, s -> (Named) ActionSupport.simple(s, c -> {
      }));
    }

    private Factory(String regex, Function<String, Named> func) {
      super(regex, func);
    }

    public <T> Factory addParameter(String actionName, Simple.Factory<T> parameter) {
      return this;
    }
  }
}
