package com.github.dakusui.jcunit8.pipeline.stages.generators.ext.acts;

import com.github.dakusui.jcunit8.pipeline.stages.generators.ext.base.ConstraintRenderer;
import com.github.dakusui.jcunit8.pipeline.stages.generators.ext.base.FactorSpaceNormalizer;
import com.github.dakusui.jcunit8.pipeline.stages.generators.ext.base.NormalizableConstraint;

public class ActsConstraintRenderer extends ConstraintRenderer<ActsConstraintRenderer> {
  public ActsConstraintRenderer(FactorSpaceNormalizer factorSpaceNormalizer) {
    this(factorSpaceNormalizer, true);
  }

  private ActsConstraintRenderer(FactorSpaceNormalizer factorSpaceNormalizer, boolean root) {
    super(factorSpaceNormalizer, root);
  }

  @Override
  public void visit(NormalizableConstraint.Or constraint) {
    renderJunction(constraint.constraints(), " || ");
  }

  @Override
  public void visit(NormalizableConstraint.And constraint) {
    renderJunction(constraint.constraints(), " &amp;&amp; ");
  }

  @Override
  public void visit(NormalizableConstraint.GreaterThan constraint) {
    renderComparison(constraint.rightTerm(), " &lt; ", constraint.leftTerm());
  }

  @Override
  public void visit(NormalizableConstraint.GreaterThanOrEqualTo constraint) {
    renderComparison(constraint.rightTerm(), " &lt;= ", constraint.leftTerm());
  }

  @Override
  public void visit(NormalizableConstraint.EqualTo constraint) {
    renderComparison(constraint.leftTerm(), " == ", constraint.rightTerm());
  }

  @Override
  public void visit(NormalizableConstraint.NotEqualTo constraint) {
    renderComparison(constraint.leftTerm(), " != ", constraint.rightTerm());
  }

  @Override
  protected String renderTerm(String term) {
    return this.factorSpaceNormalizer.normalizedFactorName(term).orElse(term);
  }

  @Override
  protected ActsConstraintRenderer createChild() {
    return new ActsConstraintRenderer(this.factorSpaceNormalizer, false);
  }
}
