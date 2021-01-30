package com.github.dakusui.peerj.ext.base;

import java.util.List;

import static java.util.stream.Collectors.joining;

public abstract class ConstraintRenderer<S extends ConstraintRenderer<S>> implements NormalizableConstraint.Visitor {
  protected final FactorSpaceNormalizer factorSpaceNormalizer;
  protected final StringBuilder         b = new StringBuilder();
  private final   boolean               root;

  public ConstraintRenderer(FactorSpaceNormalizer factorSpaceNormalizer, boolean root) {
    this.factorSpaceNormalizer = factorSpaceNormalizer;
    this.root = root;
  }

  public boolean isRoot() {
    return this.root;
  }

  public String render(NormalizableConstraint constraint) {
    constraint.accept(this);
    return this.render();
  }

  public String render() {
    return this.b.toString();
  }

  protected void renderJunction(List<NormalizableConstraint> constraints, String junction) {
    boolean isTopLevel = isRoot();
    if (!isTopLevel)
      b.append(" ( ");
    b.append(constraints
        .stream()
        .map(v -> createChild().render(v))
        .collect(joining(junction)));
    if (isTopLevel)
      b.append(" ) ");
  }

  protected abstract String renderTerm(String term);

  protected abstract S createChild();
}
