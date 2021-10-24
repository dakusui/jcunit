package com.github.dakusui.jcunit8.models.scenario;

import com.github.dakusui.actionunit.actions.Named;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.jcunitx.core.KeyValuePair;

import java.util.LinkedList;
import java.util.List;

import static com.github.dakusui.actionunit.core.ActionSupport.leaf;

public interface ParameterizedAction extends Named {
  String OUTPUT_VAR_NAME = "out";

  <T> T arg(int argIndex);

  int numArgs();

  class Impl implements ParameterizedAction {
    private final String   name;
    private final Action   action;
    private final Object[] args;

    public Impl(String name, DriverMethodInvoker driverMethodInvoker, Object... args) {
      this.name = name;
      this.action = leaf(context -> {
        if (!context.defined(OUTPUT_VAR_NAME))
          context.assignTo(OUTPUT_VAR_NAME, new LinkedList<>());
        context.<List<KeyValuePair<?>>>valueOf(OUTPUT_VAR_NAME)
            .add(KeyValuePair.create(name, driverMethodInvoker.invoke()));
      });
      this.args = args;
    }

    @Override
    public String name() {
      return this.name;
    }

    @Override
    public Action action() {
      return action;
    }

    @SuppressWarnings("unchecked")
    public <T> T arg(int argIndex) {
      return (T) args[argIndex];
    }

    @Override
    public int numArgs() {
      return args.length;
    }
  }
}
