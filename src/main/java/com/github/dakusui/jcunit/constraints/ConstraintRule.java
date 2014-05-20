package com.github.dakusui.jcunit.constraints;

import com.github.dakusui.jcunit.constraints.ccs.CCSValueTuple;
import com.github.dakusui.jcunit.core.ValueTuple;
import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.jcunit.exceptions.SymbolNotFoundException;
import com.github.dakusui.lisj.BaseForm;
import com.github.dakusui.lisj.Basic;
import com.github.dakusui.lisj.CUT;
import com.github.dakusui.lisj.Context;
import com.github.dakusui.lisj.ContextObserver;
import com.github.dakusui.lisj.FormResult;
import com.github.dakusui.lisj.Symbol;

public class ConstraintRule {
  private Object then;
  private Object when;
  private final Context context;

  public ConstraintRule(Context context) {
    this.context = context;
  }

  public ConstraintRule when(Object when) {
    this.when = when;
    return this;
  }

  public Object when() {
    return this.when;
  }

  public ConstraintRule then(Object then) {
    this.then = then;
    return this;
  }

  public Object then() {
    return this.then;
  }

  /**
   * Evaluates this <code>ConstraintRule</code> with <code>given</code> values.
   * If all the necessary values are found in the parameter <code>values</code>
   * and they do not violate this constraint, <code>null</code> will be
   * returned. If they violate this constraint, A <code>CCSValueTuple</code>
   * whose entries represent the values used in the evaluation will be returned.
   * 
   * @param given
   *          The values with which the evaluation is executed.
   * @return The values that are used in the evaluation.
   * @throws JCUnitException
   * @throws SymbolNotFoundException
   *           A necessary field(s) is/are neither defined in the context nor
   *           <code>values</code>
   * @throws CUT
   *           Evaluation process is cut.
   */
  public CCSValueTuple<String, Object> evaluate(final ValueTuple<String, Object> given) throws JCUnitException, CUT {
    final CCSValueTuple<String, Object> ret = new CCSValueTuple<String, Object>();
    Context c = this.context.createChild();
    c.addObserver(new ContextObserver() {
      @Override
      public void beginEvaluation(BaseForm form, Object params) {
      }

      @Override
      public void eachEvaluation(BaseForm form, Object cur, FormResult ret) {
      }

      @Override
      public void endEvaluation(BaseForm form, FormResult ret) {
      }

      @Override
      public void failEvaluation(BaseForm form, int index, JCUnitException e) {
      }

      @Override
      public void cutEvaluation(BaseForm form, int index, CUT e) {
      }

      @Override
      public void symbolEvaluation(Symbol symbol, Object value) {
        // Pick up relevant parameters only.
        if (given.containsKey(symbol.name()))
          ret.put(symbol.name(), value);
      }
    });
    for (String key : given.keySet()) {
      // Assumption behind here is the symbol names (keys of 'given' map) and
      // preset function names (e.g. concat, progn, and so on) do not collide
      // each other.
      c.bind(new Symbol(key), given.get(key));
    }
    if (Basic.evalp(c, this.when)) {
      if (Basic.evalp(c, this.then))
        return null;
    }
    return ret;
  }
}
