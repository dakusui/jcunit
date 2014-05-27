package com.github.dakusui.lisj.pred;

import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.jcunit.exceptions.SymbolNotFoundException;
import com.github.dakusui.lisj.CUT;
import com.github.dakusui.lisj.Context;
import com.github.dakusui.lisj.FormResult;

/**
 * Created by hiroshi on 5/28/14.
 */
public abstract class LogicalMultinominalPredicate extends LogicalPredicate {
  protected final FormResult evaluateEach(boolean shortCuttingValue, Context context, Object currentParam,
      FormResult lastResult) throws JCUnitException, CUT {
    FormResult ret = super.evaluateEach(context, currentParam, lastResult);
    if (ret.value() instanceof Boolean) {
      if (Boolean.valueOf(shortCuttingValue).equals(ret.value())) {
        ret.clearIgnoredExceptions();
        cut(shortCuttingValue);
      }
    } else {
      throw new IllegalArgumentException(msgReturnedTypeMismatch(Boolean.class,
          ret.value()));
    }
    return ret;
  }

  @Override
  protected FormResult handleException(Context context,
      JCUnitException e, FormResult result)
      throws JCUnitException {
    if (context.allowsUnboundSymbols() && e instanceof SymbolNotFoundException) {
      result.addIgnoredException(e);
      return result;
    }
    else
      return super.handleException(context, e, result);
  }

  @Override
  protected FormResult evaluateLast(Context context,
      Object[] evaluatedParams,
      FormResult lastResult) throws JCUnitException {
    if (context.allowsUnboundSymbols() && lastResult.ignoredExceptions().size() > 0) {
      StringBuilder symbolNames = new StringBuilder();
      for (JCUnitException e : lastResult.ignoredExceptions()) {
        if (e instanceof SymbolNotFoundException) {
          SymbolNotFoundException ee = (SymbolNotFoundException) e;
          if (symbolNames.length() > 0) symbolNames.append(",");
          symbolNames.append(ee.getSymbolNames());
        } else {
          ////
          // This shouldn't happen since only symbol not found exceptions are added
          // to result object by 'handleException' method.
          throw e;
        }
      }
      throw new SymbolNotFoundException(symbolNames.toString(), null);
    }
    return super.evaluateLast(context, evaluatedParams, lastResult);
  }
}
