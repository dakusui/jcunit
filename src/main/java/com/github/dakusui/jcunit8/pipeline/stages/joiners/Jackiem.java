package com.github.dakusui.jcunit8.pipeline.stages.joiners;

import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.Joiner;
import com.github.dakusui.jcunit8.testsuite.SchemafulTupleSet;

public class Jackiem extends Joiner.Base {
  private final Requirement requirement;

  public Jackiem(Requirement requirement) {
    this.requirement = requirement;
  }

  @Override
  protected SchemafulTupleSet doJoin(SchemafulTupleSet lhs, SchemafulTupleSet rhs) {
    return null;
  }
}
