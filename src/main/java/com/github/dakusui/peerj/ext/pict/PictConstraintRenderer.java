package com.github.dakusui.peerj.ext.pict;

import com.github.dakusui.peerj.ext.base.ConstraintRenderer;
import com.github.dakusui.peerj.ext.base.FactorSpaceNormalizer;
import com.github.dakusui.peerj.ext.base.NormalizableConstraint;

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
    b.append(renderTerm(constraint.leftTerm()));
    b.append(" > ");
    b.append(renderTerm(constraint.rightTerm()));
  }

  @Override
  public void visit(NormalizableConstraint.GreaterThanOrEqualTo constraint) {
    b.append(renderTerm(constraint.leftTerm()));
    b.append(" >= ");
    b.append(renderTerm(constraint.rightTerm()));
  }

  @Override
  public void visit(NormalizableConstraint.EqualTo constraint) {
    b.append(renderTerm(constraint.leftTerm()));
    b.append(" = ");
    b.append(renderTerm(constraint.rightTerm()));
  }

  @Override
  public void visit(NormalizableConstraint.NotEqualTo constraint) {
    b.append(renderTerm(constraint.leftTerm()));
    b.append(" <> ");
    b.append(renderTerm(constraint.rightTerm()));
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
