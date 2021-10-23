package com.github.dakusui.jcunit8.models.scenario;

import com.github.dakusui.actionunit.actions.Named;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.ActionSupport;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.core.context.ContextConsumer;

public interface ParameterizedAction extends Named {
  <T> T arg(int argIndex);

  int numArgs();

  class Impl implements ParameterizedAction {
    private final String   name;
    private final Action   action;
    private final Object[] args;

    public Impl(String name, DriverMethodInvoker driverMethodInvoker, Object... args) {
      this.name = name;
      this.action = ActionSupport.leaf(new ContextConsumer() {
        @Override
        public void accept(Context context) {
          context.assignTo("out", driverMethodInvoker.invoke());
        }
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
