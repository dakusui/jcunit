package com.github.dakusui.peerj.ext.pict;

import com.github.dakusui.peerj.ext.base.ConstraintRenderer;
import com.github.dakusui.peerj.ext.base.FactorSpaceNormalizer;
import com.github.dakusui.peerj.ext.base.NormalizableConstraint;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

class PictConstraintRenderer extends ConstraintRenderer<PictConstraintRenderer> {

  PictConstraintRenderer(FactorSpaceNormalizer factorSpaceNormalizer) {
    super(factorSpaceNormalizer);
  }

  @Override
  public void visit(NormalizableConstraint.Or constraint) {
    boolean isTopLevel = isTopLevel();
    if (!isTopLevel)
      b.append(" ( ");
    b.append(constraint.constraints().stream()
        .map(v -> newObject().render(v))
        .collect(joining(" OR ")));
    if (isTopLevel)
      b.append(" ) ");
  }

  @Override
  public void visit(NormalizableConstraint.And constraint) {
    boolean isTopLevel = isTopLevel();
    if (!isTopLevel)
      b.append(" ( ");
    b.append(constraint.constraints().stream()
        .map(v -> newObject().render(v))
        .collect(joining(" AND ")));
    if (isTopLevel)
      b.append(" ) ");
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
  protected PictConstraintRenderer newObject() {
    return new PictConstraintRenderer(factorSpaceNormalizer);
  }
}
