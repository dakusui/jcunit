package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.Param;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScenarioSequence<SUT> {
  private final List<Scenario<SUT>> seq;

  public ScenarioSequence(List<Scenario<SUT>> seq) {
    Checks.checknotnull(seq);
    this.seq = Collections.unmodifiableList(seq);
  }

  public Scenario<SUT> get(int i) {
    return seq.get(i);
  }

  public int size() {
    return seq.size();
  }


  public static class Builder<SUT> {
    private FactorNameResolver resolver;
    private Tuple              tuple;
    private Factors            factors;
    private int                size;

    public Builder() {
    }

    public Builder setFactorNameResolver(FactorNameResolver resolver) {
      this.resolver = resolver;
      return this;
    }

    public Builder setTuple(Tuple tuple) {
      this.tuple = tuple;
      return this;
    }

    public Builder setFactors(Factors factors) {
      this.factors = factors;
      return this;
    }

    public Builder setSize(int size) {
      this.size = size;
      return this;
    }

    public ScenarioSequence<SUT> build() {
      Checks.checknotnull(resolver);
      Checks.checknotnull(tuple);
      Checks.checknotnull(factors);
      Checks.checkcond(size > 0);
      List<Scenario<SUT>> work = new ArrayList<Scenario<SUT>>(this.size);
      for (int i = 0; i < this.size; i++) {
        String stateName = this.resolver.stateName(i);
        String actionName = this.resolver.actionName(i);
        int numParams = this.resolver.numParams(actionName);
        String[] paramNames = new String[numParams];
        for (int j = 0; j < numParams; j++) {
          paramNames[j] = this.resolver.paramName(actionName, j);
        }
        State<SUT> given = (State<SUT>) this.factors.get(stateName);
        Action<SUT> when = (Action<SUT>) this.factors.get(actionName);
        Args with = createArgs(paramNames, this.tuple);
        Scenario<SUT> cur = new Scenario<SUT>(given, when, with);
        work.add(cur);
      }
      return new ScenarioSequence<SUT>(work);
    }

    private Args createArgs(String[] paramNames, Tuple tuple) {
      Object[] values = new Object[paramNames.length];
      for (int i = 0; i < paramNames.length; i++) {
        values[i] = tuple.get(paramNames[i]);
      }
      return new Args(values);
    }
  }
}
