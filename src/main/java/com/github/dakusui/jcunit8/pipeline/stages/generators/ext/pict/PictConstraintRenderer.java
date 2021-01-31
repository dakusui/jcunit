package com.github.dakusui.jcunit8.pipeline.stages.generators.ext.pict;

import com.github.dakusui.jcunit8.pipeline.stages.generators.ext.base.ConstraintRenderer;
import com.github.dakusui.jcunit8.pipeline.stages.generators.ext.base.FactorSpaceNormalizer;
import com.github.dakusui.jcunit8.pipeline.stages.generators.ext.base.NormalizableConstraint;

import static java.lang.String.format;

class PictConstraintRenderer extends ConstraintRenderer<PictConstraintRenderer> {
  public PictConstraintRenderer(FactorSpaceNormalizer factorSpaceNormalizer) {
    this(factorSpaceNormalizer, true);
  }

  private PictConstraintRenderer(FactorSpaceNormalizer factorSpaceNormalizer, boolean root) {
    super(factorSpaceNormalizer, root);
  }

  @Override
  public void visit(NormalizableConstraint.Or constraint) {
    renderJunction(constraint.constraints(), " OR ");
  }

  @Override
  public void visit(NormalizableConstraint.And constraint) {
    renderJunction(constraint.constraints(), " AND ");
  }

  @Override
  public void visit(NormalizableConstraint.GreaterThan constraint) {
    renderComparison(constraint.leftTerm(), " > ", constraint.rightTerm());
  }

  @Override
  public void visit(NormalizableConstraint.GreaterThanOrEqualTo constraint) {
    renderComparison(constraint.leftTerm(), " >= ", constraint.rightTerm());
  }

  @Override
  public void visit(NormalizableConstraint.EqualTo constraint) {
    renderComparison(constraint.leftTerm(), " = ", constraint.rightTerm());
  }

  @Override
  public void visit(NormalizableConstraint.NotEqualTo constraint) {
    renderComparison(constraint.leftTerm(), " <> ", constraint.rightTerm());
  }

  @Override
  protected String renderTerm(String term) {
    return factorSpaceNormalizer.normalizedFactorName(term)
        .map(v -> format("[%s]", v))
        .orElse(term);
  }

  @Override
  protected PictConstraintRenderer createChild() {
    return new PictConstraintRenderer(factorSpaceNormalizer, false);
  }
}
