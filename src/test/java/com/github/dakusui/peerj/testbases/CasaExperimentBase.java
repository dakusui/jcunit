package com.github.dakusui.peerj.testbases;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.Partitioner;
import com.github.dakusui.peerj.PeerJUtils2;
import com.github.dakusui.peerj.utils.CasaDataSet;
import com.github.dakusui.peerj.utils.CasaUtils;

import java.io.File;
import java.util.List;

import static java.lang.String.format;

public abstract class CasaExperimentBase extends PeerJExperimentBase {
  public static class Spec extends PeerJExperimentBase.Spec {
    final CasaDataSet def;

    public Spec(CasaDataSet def, int strength, Algorithm algorithm, ConstraintHandlingMethod constraintHandlingMethod) {
      super(strength, algorithm, constraintHandlingMethod);
      this.def = def;
    }

    @Override
    public String toString() {
      return format("%s:t=%s:algorithm=%s:constraintHandling=%s", def, strength, algorithm, constraintHandlingMethod);
    }

    public static class Builder extends PeerJExperimentBase.Spec.Builder<Builder> {
      CasaDataSet def;

      public Builder def(CasaDataSet def) {
        this.def = def;
        return this;
      }

      @Override
      public Spec build() {
        return new Spec(def, strength, algorithm, constraintHandlingMethod);
      }
    }
  }

  public List<Tuple> conductActsExperimentForCasa(CasaDataSet def) {
    Requirement requirement = PeerJUtils2.requirement(strength());
    CasaDataSet.CasaModel casaModel = CasaUtils.readCasaModel(
        def,
        "prefix",
        requirement.strength()
    );
    return PeerJExperimentBase.generateWithActs(
        CasaUtils.baseDirFor(def, casaModel.strength, "acts", "none"),
        casaModel.factorSpace,
        casaModel.strength,
        algorithm(),
        constraintHandlingMethod());
  }

  public List<Tuple> conductJoinExperimentForCasa(CasaDataSet def, Partitioner partitioner) {
    Requirement requirement = PeerJUtils2.requirement(strength());
    CasaDataSet.CasaModel casaModel = CasaUtils.readCasaModel(
        def,
        "prefix",
        requirement.strength());
    int strength = casaModel.strength;
    File baseDir = CasaUtils.baseDirFor(def, strength, "join", partitioner.name());
    String messageOnFailure = def.toString();
    FactorSpace factorSpace = casaModel.factorSpace;
    return generateWithCombinatorialJoin(requirement, baseDir, partitioner, factorSpace, algorithm(), constraintHandlingMethod(), messageOnFailure);
  }

}
