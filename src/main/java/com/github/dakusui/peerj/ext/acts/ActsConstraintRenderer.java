package com.github.dakusui.peerj.ext.acts;

import com.github.dakusui.peerj.ext.base.ConstraintRenderer;
import com.github.dakusui.peerj.ext.base.FactorSpaceNormalizer;
import com.github.dakusui.peerj.ext.base.NormalizableConstraint;

import static java.util.stream.Collectors.joining;

public class ActsConstraintRenderer extends ConstraintRenderer<ActsConstraintRenderer> {
  public ActsConstraintRenderer(FactorSpaceNormalizer factorSpaceNormalizer) {
    super(factorSpaceNormalizer);
  }

  @Override
  public void visit(NormalizableConstraint.Or constraint) {
    boolean isTopLevel = isTopLevel();
    if (!isTopLevel)
      b.append(" ( ");
    b.append(constraint.constraints()
        .stream()
        .map(v -> newObject().render(v))
        .collect(joining(" || ")));
    if (isTopLevel)
      b.append(" ) ");
  }

  @Override
  public void visit(NormalizableConstraint.And constraint) {
    boolean isTopLevel = isTopLevel();
    if (!isTopLevel)
      b.append(" ( ");
    b.append(constraint.constraints()
        .stream()
        .map(v -> newObject().render(v))
        .collect(joining(" &amp;&amp; ")));
    if (isTopLevel)
      b.append(" ) ");
  }

  @Override
  public void visit(NormalizableConstraint.GreaterThan constraint) {

  }

  @Override
  public void visit(NormalizableConstraint.GreaterThanOrEqualTo constraint) {

  }

  @Override
  public void visit(NormalizableConstraint.EqualTo constraint) {

  }

  @Override
  public void visit(NormalizableConstraint.NotEqualTo constraint) {

  }

  @Override
  protected String renderTerm(String term) {
    return this.factorSpaceNormalizer.normalizedFactorName(term).orElse(term);
  }

  @Override
  protected ActsConstraintRenderer newObject() {
    return new ActsConstraintRenderer(this.factorSpaceNormalizer);
  }
}
