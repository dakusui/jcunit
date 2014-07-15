package com.github.dakusui.jcunit.constraints;

import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.tuples.TupleImpl;
import com.github.dakusui.lisj.*;
import com.github.dakusui.lisj.exceptions.LisjCheckedException;
import com.github.dakusui.lisj.exceptions.SymbolNotFoundException;
import com.github.dakusui.lisj.pred.And;
import com.github.dakusui.lisj.pred.Or;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ConstraintRule {
  private final Object  then;
  private final Object  when;
  private final Context context;

  public ConstraintRule(Context context, Object when, Object then) {
    Utils.checknotnull(context);
    Utils.checknotnull(when);
    Utils.checknotnull(then);
    this.context = context;
    this.when = when;
    this.then = then;
  }

  public Object when() {
    return this.when;
  }

  public Object then() {
    return this.then;
  }

  /**
   * Evaluates this <code>ConstraintRule</code> with <code>given</code> values.
   * If all the necessary values are found in the parameter <code>values</code>
   * and they do not violate this constraint, <code>null</code> will be
   * returned. If they violate this constraint, A <code>Tuple</code>
   * whose entries represent the values used in the evaluation will be returned.
   * <p/>
   * When an attribute not defined in the tuple and context is necessary
   * for the evaluation, a {@code SymbolNotFoundException} will be thrown.
   *
   * @param given The values with which the evaluation is executed.
   * @return A sub tuple that doesn't satisfy this constraint.
   * @throws com.github.dakusui.jcunit.exceptions.JCUnitException Failed for other failures than undefined symbols.
   * @throws SymbolNotFoundException                              A necessary field(s) is/are neither defined in the context nor
   *                                                              <code>values</code>
   * @throws CUT                                                  Evaluation process is cut.
   */
  public Tuple evaluate(final Tuple given) throws SymbolNotFoundException,
      CUT {
    final Tuple ret = new TupleImpl();
    Context c = this.context.createChild();
    final List<Symbol> involvedSymbols = new LinkedList<Symbol>();
    c.addObserver(createContextObserver(involvedSymbols));
    for (String key : given.keySet()) {
      // Assumption behind here is the symbol names (keys of 'given' map) and
      // preset function names (e.g. concat, progn, and so on) do not collide
      // each other.
      c.bind(new Symbol(key), given.get(key));
    }
    try {
      if (Basic.evalp(c, this.when())) {
        if (Basic.evalp(c, this.then())) {
          ////
          // If and only if both 'when' and 'then' are satisfied, {@code null} will
          // be returned.
          return null;
        }
      }
    } catch (SymbolNotFoundException e) {
      throw e;
    } catch (LisjCheckedException e) {
      Utils.rethrow(e, "Something went wrong.:%s", e.getMessage());
    }
    for (Symbol s : involvedSymbols) {
      ret.put(s.name(), given.get(s.name()));
    }
    return ret;
  }

  private ContextObserver createContextObserver(
      final List<Symbol> involvedSymbols) {
    return new ContextObserver() {
      Map<BaseForm, Integer> markedPositions = new HashMap<BaseForm, Integer>();

      private void mark(BaseForm form) {
        markedPositions.put(form, involvedSymbols.size());
      }

      private void reset(BaseForm form) {
        if (markedPositions.containsKey(form)) {
          int pos = markedPositions.get(form);
          involvedSymbols.subList(pos, involvedSymbols.size()).clear();
        }
      }

      private void removeMarkedPosition(BaseForm form) {
        if (markedPositions.containsKey(form)) {
          markedPositions.remove(form);
        }
      }

      @Override
      public void beginEvaluation(BaseForm form, Object params) {
        if (form instanceof And || form instanceof Or) {
          mark(form);
        }
      }

      @Override
      public void eachEvaluation(BaseForm form, Object cur, FormResult ret) {
        reset(form);
      }

      @Override
      public void endEvaluation(BaseForm form, FormResult ret) {
        removeMarkedPosition(form);
      }

      @Override
      public void failEvaluation(BaseForm form, int index,
          LisjCheckedException e) {
      }

      @Override
      public void cutEvaluation(BaseForm form, int index, CUT e) {
      }

      @Override
      public void symbolEvaluation(Symbol symbol, Object value) {
        involvedSymbols.add(symbol);
        // Pick up relevant parameters only.
        //if (given.containsKey(symbol.name()))
        //  ret.put(symbol.name(), value);
      }
    };
  }

  public static class Builder {
    private Object  when;
    private Object  then;
    private Context context;

    public Builder setContext(Context context) {
      this.context = context;
      return this;
    }

    public Builder when(Object when) {
      this.when = when;
      return this;
    }

    public Builder then(Object then) {
      this.then = then;
      return this;
    }

    public ConstraintRule build() {
      return new ConstraintRule(context, when, then);
    }

  }
}
