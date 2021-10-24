package com.github.dakusui.jcunit8.models.scenario;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;

public class DefaultActionPerformer extends ContextHoldingActionPerformer implements Action.Visitor {
  public DefaultActionPerformer(Context context) {
    super(context);
  }

  public static DefaultActionPerformer create(Context context) {
    return new DefaultActionPerformer(context);
  }

  public void callAccept(Action action, Action.Visitor visitor) {
    action.accept(visitor);
  }

  protected DefaultActionPerformer newInstance(Context context) {
    return new DefaultActionPerformer(context);
  }

  @Override
  public Context context() {
    return this.context;
  }
}
