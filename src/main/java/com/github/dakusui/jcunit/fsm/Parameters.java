package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.FactorDef;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;
import com.github.dakusui.jcunit.plugins.constraints.ConstraintChecker;

import java.util.*;

public class Parameters extends Factors {
  public static final Parameters EMPTY = new Builder().build();
  private final ConstraintChecker constraintChecker;

  public Parameters(ConstraintChecker constraintChecker, List<Factor> factors) {
    super(factors);
    this.constraintChecker = constraintChecker;
  }

  public ConstraintChecker getConstraintChecker() {
    return this.constraintChecker;
  }

  public Object[][] values() {
    Object[][] ret = new Object[this.size()][0];
    int i = 0;
    for (Factor each : this) {
      ret[i++] = each.levels.toArray(new Object[each.levels.size()]);
    }
    return ret;
  }

  public static class Builder extends Factors.Builder {
    private ConstraintChecker constraintChecker = ConstraintChecker.DEFAULT_CONSTRAINT_CHECKER;

    public Builder() {
      super();
    }

    public Builder(Object[][] params) {
      super();
      int i = 0;
      for (Object[] each : params) {
        Checks.checktest(each.length > 0, "Invalid factor data found. Each array of this double-array must have at least one element");
        Factor.Builder b = new Factor.Builder(String.format("p%d", i++));
        for (Object o : each) {
          b.addLevel(o);
        }
        this.add(b.build());
      }
    }

    public Builder add(String name, Object firstValue, Object... restValues) {
      Factor.Builder b = new Factor.Builder(Checks.checknotnull(name));
      b.addLevel(firstValue);
      for (Object each : restValues) {
        b.addLevel(each);
      }
      this.add(b.build());
      return this;
    }

    public Builder setConstraintChecker(ConstraintChecker constraintChecker) {
      this.constraintChecker = constraintChecker;
      return this;
    }

    public Parameters build() {
      return new Parameters(this.constraintChecker, this.factors);
    }
  }

  public static class LocalConstraintChecker extends ConstraintChecker.Base {
    protected final ConstraintChecker   target;
    private final   List<String>        plainParameterNames;
    /**
     * A map from plain factor names used to declare parameters in Parameters.Builder
     * and inside constraint checker to flatten FSM tuple representation.
     */
    private final   Map<String, String> plainToFlattenFSM;

    /**
     * @param target              User defined constraint manager for parameters.
     * @param plainParameterNames User friendly parameter name.
     * @param fsmName             A name of a FSM. {@code Story} field name in standard JCUnit runner.
     * @param historyIndex        The current history index.
     */
    public LocalConstraintChecker(ConstraintChecker target, List<String> plainParameterNames, String fsmName, int historyIndex) {
      this.target = Checks.checknotnull(target);
      this.plainParameterNames = Collections.unmodifiableList(Checks.checknotnull(plainParameterNames));
      this.plainToFlattenFSM = new HashMap<String, String>();
      int i = 0;
      for (String each : this.plainParameterNames) {
        this.plainToFlattenFSM.put(each, FactorDef.Fsm.paramName(fsmName, historyIndex, i));
        i++;
      }
    }

    @Override
    public boolean check(Tuple tuple) throws UndefinedSymbol {
      try {
        return this.target.check(translate(Checks.checknotnull(tuple)));
      } catch (UndefinedSymbol e) {
        ////
        // Translate back missing symbols into 'flatten FSM tuple' representation
        // based ones
        throw new UndefinedSymbol(Utils.transform(e.missingSymbols, new Utils.Form<String, String>() {
              @Override
              public String apply(String in) {
                if (LocalConstraintChecker.this.plainToFlattenFSM.containsKey(in)) {
                  return LocalConstraintChecker.this.plainToFlattenFSM.get(in);
                } else {
                  ////
                  // In case unknown symbol is reported, probably underlying constraint
                  // checker is complaining of its internal factor. Include it
                  // without translating it.
                  return in;
                }
              }
            }
        ).toArray(new String[e.missingSymbols.size()]));
      }
    }

    protected Tuple translate(Tuple tuple) throws UndefinedSymbol {
      Tuple.Builder b = new Tuple.Builder();
      List<String> missings = new ArrayList<String>(this.plainParameterNames.size());
      for (String each : this.plainParameterNames) {
        if (tuple.containsKey(this.plainToFlattenFSM.get(each))) {
          b.put(each, tuple.get(this.plainToFlattenFSM.get(each)));
        } else {
          missings.add(this.plainToFlattenFSM.get(each));
        }
      }
      if (!missings.isEmpty()) {
        throw new UndefinedSymbol(missings.toArray(new String[missings.size()]));
      }
      return b.build();
    }
  }
}
