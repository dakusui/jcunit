package com.github.dakusui.jcunit.constraints.ccs;

import com.github.dakusui.jcunit.core.ValueTuple;
import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.lisj.Basic;
import com.github.dakusui.lisj.CUT;
import com.github.dakusui.lisj.Context;
import com.github.dakusui.lisj.Symbol;

public class Constraint<T, U> {
  private Object then;
  private Object when;
  private final Context context;

  public Constraint(Context context) {
    this.context = context;
  }

  public void when(Object when) {
    this.when = when;
  }

  public Object when() {
    return this.when;
  }

  public void then(Object then) {
    this.then = then;
  }

  public Object then() {
    return this.then;
  }

  public ConstraintValueTuple<T, U> evaluate(ValueTuple<T, U> values) throws JCUnitException, CUT {
    Context c = this.context.createChild();
    for (T key : values.keySet()) {
      c.bind(new Symbol(key.toString()), values.get(key));
    }
    if (Basic.evalp(c, this.when)) {
      if (Basic.evalp(c, this.then))
        return null;
    }
    ConstraintValueTuple<T, U> ret = new ConstraintValueTuple<T, U>();
    return ret;
  }
}
