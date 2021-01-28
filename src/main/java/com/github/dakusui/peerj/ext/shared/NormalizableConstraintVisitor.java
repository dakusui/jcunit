package com.github.dakusui.peerj.ext.shared;

public interface NormalizableConstraintVisitor {
  void visit(NormalizableConstraint.Or constraint);
  void visit(NormalizableConstraint.And constraint);
  void visit(NormalizableConstraint.GreaterThan constraint);
  void visit(NormalizableConstraint.GreaterThanOrEqualTo constraint);
  void visit(NormalizableConstraint.EqualTo constraint);
  void visit(NormalizableConstraint.NotEqualTo constraint);
}
