package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.FactorDef;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.utils.Utils;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;
import com.github.dakusui.jcunit.plugins.constraints.ConstraintChecker;

import java.util.*;

import static com.github.dakusui.jcunit.core.utils.Checks.checknotnull;

public class Parameters extends Factors {
  public static final Parameters EMPTY = new ParametersBuilder(new Object[][] {}).build();
  private final ConstraintChecker constraintChecker;

  public Parameters(ConstraintChecker constraintChecker, List<Factor> factors) {
    super(factors);
    this.constraintChecker = checknotnull(constraintChecker);
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

  public static class LocalConstraintChecker extends ConstraintChecker.Base {
    protected final ConstraintChecker   base;
    private final   List<String>        plainParameterNames;
    /**
     * A map from plain factor names used to declare parameters in Parameters.Builder
     * and inside constraint checker to flatten FSM tuple representation.
     */
    private final   Map<String, String> plainToFlattenFSM;

    /**
     * @param base                User defined constraint manager for parameters.
     * @param plainParameterNames User friendly parameter name.
     * @param fsmName             A name of a FSM. {@code Story} field name in standard JCUnit runner.
     * @param historyIndex        The current history index.
     */
    public LocalConstraintChecker(ConstraintChecker base, List<String> plainParameterNames, String fsmName, int historyIndex) {
      this.base = checknotnull(base);
      this.plainParameterNames = Collections.unmodifiableList(checknotnull(plainParameterNames));
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
        return this.base.check(translate(checknotnull(tuple)));
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
