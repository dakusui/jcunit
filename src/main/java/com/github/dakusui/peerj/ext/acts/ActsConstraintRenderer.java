package com.github.dakusui.peerj.ext.acts;

import com.github.dakusui.peerj.ext.base.ConstraintRenderer;
import com.github.dakusui.peerj.ext.base.FactorSpaceNormalizer;
import com.github.dakusui.peerj.ext.base.NormalizableConstraint;

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
    b.append(String.format("%s &lt; %s", renderTerm(constraint.rightTerm()), renderTerm(constraint.leftTerm())));
  }

  @Override
  public void visit(NormalizableConstraint.GreaterThanOrEqualTo constraint) {
    b.append(String.format("%s &lt;= %s", renderTerm(constraint.rightTerm()), renderTerm(constraint.leftTerm())));
  }

  @Override
  public void visit(NormalizableConstraint.EqualTo constraint) {
    b.append(String.format("%s == %s", renderTerm(constraint.rightTerm()), renderTerm(constraint.leftTerm())));
  }

  @Override
  public void visit(NormalizableConstraint.NotEqualTo constraint) {
    b.append(String.format("%s != %s", renderTerm(constraint.rightTerm()), renderTerm(constraint.leftTerm())));
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
