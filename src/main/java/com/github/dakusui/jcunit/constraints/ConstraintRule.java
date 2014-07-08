package com.github.dakusui.jcunit.constraints;

import com.github.dakusui.jcunit.core.Tuple;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.jcunit.exceptions.SymbolNotFoundException;
import com.github.dakusui.lisj.BaseForm;
import com.github.dakusui.lisj.Basic;
import com.github.dakusui.lisj.CUT;
import com.github.dakusui.lisj.Context;
import com.github.dakusui.lisj.ContextObserver;
import com.github.dakusui.lisj.FormResult;
import com.github.dakusui.lisj.Symbol;
import com.github.dakusui.lisj.pred.And;
import com.github.dakusui.lisj.pred.Or;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ConstraintRule {
	public static class Builder {
		private Object when;
		private Object then;
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

	private final Object then;
	private final Object when;
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
	 * returned. If they violate this constraint, A <code>CCSValueTuple</code>
	 * whose entries represent the values used in the evaluation will be returned.
	 *
	 * @param given The values with which the evaluation is executed.
	 * @return The symbols and values that are used in the evaluation.
	 * @throws JCUnitException
	 * @throws SymbolNotFoundException A necessary field(s) is/are neither defined in the context nor
	 *                                 <code>values</code>
	 * @throws CUT                     Evaluation process is cut.
	 */
	public Tuple evaluate(final Tuple given) throws JCUnitException, CUT {
		final Tuple ret = new Tuple();
		Context c = this.context.createChild();
		final List<Symbol> involvedSymbols = new LinkedList<Symbol>();
		c.addObserver(new ContextObserver() {
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
				System.out.println("** BEGIN **" + form + Basic.tostr(params));
				if (form instanceof And || form instanceof Or) {
					mark(form);
				}
			}

			@Override
			public void eachEvaluation(BaseForm form, Object cur, FormResult ret) {
				System.out.println("** EACH **" + form);
				System.out.println("-->" + involvedSymbols.size());
				reset(form);
			}

			@Override
			public void endEvaluation(BaseForm form, FormResult ret) {
				System.out.println("** END **" + form);
				removeMarkedPosition(form);
			}

			@Override
			public void failEvaluation(BaseForm form, int index, JCUnitException e) {
				System.out.println("** FAIL **" + form);
			}

			@Override
			public void cutEvaluation(BaseForm form, int index, CUT e) {
				System.out.println("** CUT **" + form);
			}

			@Override
			public void symbolEvaluation(Symbol symbol, Object value) {
				System.out.println("** SYMBOL **" + symbol.name());
				involvedSymbols.add(symbol);
				// Pick up relevant parameters only.
				//if (given.containsKey(symbol.name()))
				//  ret.put(symbol.name(), value);
			}
		});
		for (String key : given.keySet()) {
			// Assumption behind here is the symbol names (keys of 'given' map) and
			// preset function names (e.g. concat, progn, and so on) do not collide
			// each other.
			c.bind(new Symbol(key), given.get(key));
		}
		if (Basic.evalp(c, this.when)) {
			if (!Basic.evalp(c, this.then)) {
				return null;
			}
		}
		for (Symbol s : involvedSymbols) {
			ret.put(s.name(), given.get(s.name()));
		}
		return ret;
	}
}
