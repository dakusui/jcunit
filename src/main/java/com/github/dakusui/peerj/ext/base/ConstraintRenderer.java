package com.github.dakusui.peerj.ext.base;

import com.github.dakusui.jcunit8.factorspace.FactorSpace;

import java.util.List;

import static java.util.Collections.emptyList;
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
    if (!isTopLevel)
      b.append(" ) ");
  }

  protected void renderComparison(String leftTerm, String comparator, String rightTerm) {
    b.append(renderTerm(leftTerm));
    b.append(comparator);
    b.append(renderTerm(rightTerm));
  }

  protected abstract String renderTerm(String term);

  protected abstract S createChild();

  public static class DummyConstraintRenderer extends ConstraintRenderer<DummyConstraintRenderer> {
    public DummyConstraintRenderer() {
      this(true);
    }

    private DummyConstraintRenderer(boolean root) {
      super(new FactorSpaceNormalizer(FactorSpace.create(emptyList(), emptyList())), root);
    }

    @Override
    protected String renderTerm(String term) {
      return term;
    }

    @Override
    protected DummyConstraintRenderer createChild() {
      return new DummyConstraintRenderer(false);
    }

    @Override
    public void visit(NormalizableConstraint.Or constraint) {
      renderJunction(constraint.constraints(), " or ");
    }

    @Override
    public void visit(NormalizableConstraint.And constraint) {
      renderJunction(constraint.constraints(), " and ");
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
      renderComparison(constraint.leftTerm(), " == ", constraint.rightTerm());
    }

    @Override
    public void visit(NormalizableConstraint.NotEqualTo constraint) {
      renderComparison(constraint.leftTerm(), " != ", constraint.rightTerm());
    }
  }
}
