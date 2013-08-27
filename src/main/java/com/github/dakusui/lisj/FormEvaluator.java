package com.github.dakusui.lisj;

import com.github.dakusui.jcunit.exceptions.JCUnitException;

public class FormEvaluator {
	private Object params;
	private BaseForm form;

	private Object[] evaluatedResult;
	private Context context;
	private FormResult result;
	private boolean initialized;

	public FormEvaluator(Context context,  BaseForm func, Object params, FormResult formResult) {
		this.form = func;
		this.params = params;
		this.context = context;
		this.result = formResult;
		init();
	}

	public FormResult init() {
		this.evaluatedResult = new Object[Basic.length(this.params)];
		////
		// do i really want this System.arraycopy?
		// the evaluated result is calculated by 'next' method one by one, eventually.
		// right, if evaluateEach skips some element, the corresponding element in
		// evaluatedParams will remain null. but who cares?
		// it's the evaluated results in some meaning!
		//
		// ok, let's comment it out! (Aug/14)
		// System.arraycopy(this.params, 0, evaluatedResult, 0, evaluatedResult.length);
		
		FormResult ret = new FormResult(0, evaluatedResult.length, null);
		this.initialized = true;
		return ret;
	}

	public boolean hasNext(FormResult lastResult) {
		if (!this.initialized) throw new IllegalStateException();
		////
		// Since it's sure that this object is initialized, we can use evaluatedResult's
		// length instead of the actual length of params, which needs some calculation.
		return lastResult.nextPosition() < evaluatedResult.length;
	}

	public FormResult next(FormResult lastResult) throws CUT, JCUnitException {
		int nextPosition = lastResult.nextPosition();
		FormResult ret = form.evaluateEach(
				this.context, 
				Basic.get(params, nextPosition), lastResult
		);
		this.evaluatedResult[nextPosition] = ret.value();
		return ret;
	}

	public FormResult evaluateLast(FormResult lastResult) throws JCUnitException, CUT {
		return form.evaluateLast(this.context, this.evaluatedResult, lastResult);
	}

	public FormResult result() {
		return this.result;
	}

}