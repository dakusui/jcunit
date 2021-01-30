package com.github.dakusui.peerj.ext.base;

public interface Term {
  enum Type {
    NUMBER,
    ENUM
  }

  Type type();

  interface Literal extends Term {
    Object value();

  }

  interface StringLiteral extends Literal {
    @Override
    String value();

    @Override
    default Type type() {
      return Type.ENUM;
    }
  }

  interface NumberLiteral extends Literal {
    @Override
    Number value();

    @Override
    default Type type() {
      return Type.NUMBER;
    }
  }

  interface Factor extends Term {
  }

  static Term.NumberLiteral numberLiteral(Number value) {
    return () -> value;
  }

  static Term.Literal stringLiteral(String value) {
    return (StringLiteral) () -> value;
  }
}
