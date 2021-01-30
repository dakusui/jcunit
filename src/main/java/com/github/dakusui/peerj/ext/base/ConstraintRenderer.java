package com.github.dakusui.peerj.ext.base;

public abstract class ConstraintRenderer<S extends ConstraintRenderer<S>> implements NormalizableConstraint.Visitor {
  protected final FactorSpaceNormalizer factorSpaceNormalizer;
  protected final StringBuilder         b = new StringBuilder();

  public ConstraintRenderer(FactorSpaceNormalizer factorSpaceNormalizer) {
    this.factorSpaceNormalizer = factorSpaceNormalizer;
  }

  public boolean isTopLevel() {
    return b.length() == 0;
  }

  public String render(NormalizableConstraint constraint) {
    constraint.accept(this);
    return this.render();
  }

  public String render() {
    return this.b.toString();
  }

  protected abstract String renderTerm(String term);

  protected abstract S newObject();
}
