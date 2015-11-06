package com.github.dakusui.jcunit.plugins.caengines;

import com.github.dakusui.jcunit.core.CoreBuilder;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.fsm.FSM;
import com.github.dakusui.jcunit.fsm.FSMDescription;
import com.github.dakusui.jcunit.fsm.FSMFactors;
import com.github.dakusui.jcunit.plugins.constraints.Constraint;
import com.github.dakusui.jcunit.plugins.reporters.CoverageReporter;

import java.util.List;
import java.util.Map;

import static com.github.dakusui.jcunit.core.Checks.checknotnull;

public class CoveringArrayEngineFacade implements CoveringArrayEngine {
  private final Factors          factors;
  private final Constraint       constraint;
  private final CoverageReporter reporter;
  private final CoveringArray    coveringArray;

  @Override
  public void init() {
    // This method doesn't do anything.
  }

  @Override
  public Factors getFactors() {
    return this.factors;
  }

  @Override
  public void setFactors(Factors factors) {
    throw new UnsupportedOperationException("This method is not supported by this class.");
  }

  @Override
  public Constraint getConstraint() {
    return this.constraint;
  }

  @Override
  public void setConstraint(Constraint constraint) {
    throw new UnsupportedOperationException("This method is not supported by this class.");
  }

  @Override
  public CoverageReporter getCoverageReporter() {
    return this.reporter;
  }

  @Override
  public void setCoverageReporter(CoverageReporter coverageReporter) {
    throw new UnsupportedOperationException("This method is not supported by this class.");
  }

  @Override
  public CoveringArray getCoveringArray() {
    return this.coveringArray;
  }

  private CoveringArrayEngineFacade(
      Factors factors,
      Map<String, FSMDescription> fsms,
      Constraint constraint,
      CoveringArray coveringArray,
      CoverageReporter coverageReporter
  ) {
    this.factors = checknotnull(factors);
    this.constraint = checknotnull(constraint);
    this.coveringArray = checknotnull(coveringArray);
    this.reporter = checknotnull(coverageReporter);
  }


  public static class Builder implements CoreBuilder<CoveringArrayEngineFacade> {
    public Builder(
        Factors factors,
        Map<String, FSMDescription> fsms,
        List<Constraint> localConstraints,
        Constraint.Builder constraintBuilder,
        CoveringArrayEngine.Builder engineBuilder,
        CoverageReporter.Builder reporterBuilder
    ) {
    }


    @Override
    public CoveringArrayEngineFacade build() {
      // expand FSMs to flatten factors.
      // construct constraint structure.
      //  = Base (user-defined) constraint + FSM constraint + local constraints

      // let underlying engine compute covering array.

      // collapse FSM factors into Story objects

      // construct FSM factors

      return null;
    }

    private static FSMFactors buildFSMFactors(Factors baseFactors, Map<String, FSM> fsms) {
      FSMFactors.Builder b = new FSMFactors.Builder();
      for (Map.Entry<String, FSM> each : fsms.entrySet()) {
        b.addFSM(each.getKey(), each.getValue());
      }
      return b.setBaseFactors(baseFactors).build();
    }
  }
}
