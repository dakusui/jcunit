package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.utils.Checks;
import com.github.dakusui.jcunit.plugins.constraints.ConstraintChecker;

import java.util.LinkedList;
import java.util.List;

import static java.lang.String.format;

/**
 * Created by hiroshi.ukai on 4/11/17.
 */
public class ParametersBuilder {
  private       ConstraintChecker constraintChecker  = ConstraintChecker.DEFAULT_CONSTRAINT_CHECKER;
  private final List<Factor>      factors            = new LinkedList<Factor>();
  private final List<Object>      newParameterValues = new LinkedList<Object>();
  private String newParamName;

  public ParametersBuilder(Object[][] params) {
    super();
    int i = 0;
    for (Object[] each : params) {
      Checks.checktest(each.length > 0, "Invalid factor data found. Each array of this double-array must have at least one element");
      Factor.Builder b = new Factor.Builder(format("p%d", i++));
      for (Object o : each) {
        b.addLevel(o);
      }
      this.add(b.build());
    }
  }

  public ParametersBuilder() {
  }

  private void add(Factor factor) {
    this.factors.add(factor);
  }

  public ParametersBuilder setConstraintChecker(ConstraintChecker constraintChecker) {
    this.constraintChecker = constraintChecker;
    return this;
  }

  public ParametersBuilder addParameter() {
    return this.addParameter(format("p%d", this.factors.size()));
  }

  public ParametersBuilder addParameter(String name) {
    Checks.checkcond(this.newParamName == null);
    Checks.checknotnull(name);
    this.newParamName = name;
    this.newParameterValues.clear();
    return this;
  }

  public ParametersBuilder withValues(Object first, Object... rest) {
    Checks.checkcond(this.newParamName != null);
    this.newParameterValues.add(first);
    for (Object each : rest) {
      this.newParameterValues.add(each);
    }
    Factor.Builder b = new Factor.Builder(this.newParamName);
    for (Object each : this.newParameterValues) {
      b.addLevel(each);
    }
    this.factors.add(b.build());
    this.newParamName = null;
    this.newParameterValues.clear();
    return this;
  }

  public Parameters build() {
    Checks.checkcond(this.newParamName == null);
    Checks.checkcond(this.newParameterValues.isEmpty());
    return new Parameters(this.constraintChecker, this.factors);
  }
}
