package com.github.dakusui.jcunit8.models.scenario;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.visitors.ActionPerformer;

public abstract class ContextHoldingActionPerformer extends ActionPerformer implements ContextHolder, Action.Visitor {
  protected ContextHoldingActionPerformer(Context context) {
    super(context);
  }
}
