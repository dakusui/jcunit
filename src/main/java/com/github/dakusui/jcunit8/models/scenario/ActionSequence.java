package com.github.dakusui.jcunit8.models.scenario;

import com.github.dakusui.actionunit.actions.Named;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.visitors.ActionPerformer;
import com.github.dakusui.actionunit.visitors.SimpleActionPerformer;

import java.util.LinkedList;
import java.util.List;

import static com.github.dakusui.actionunit.core.ActionSupport.sequential;
import static java.util.stream.Collectors.toList;

public interface ActionSequence extends List<ParameterizedAction> {
  default void perform() {
    this.perform(SimpleActionPerformer.create());
  }

  default void perform(ActionPerformer performer) {
    performer.visit(this.toAction());
  }

  default Action toAction() {
    return sequential(this.stream().map(each -> (Action) each).collect(toList()));
  }

  class Impl extends LinkedList<ParameterizedAction> implements ActionSequence {
    public Impl(List<ParameterizedAction> actions) {
      this.addAll(actions);
    }
  }
}
