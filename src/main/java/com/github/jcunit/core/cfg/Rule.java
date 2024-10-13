package com.github.jcunit.core.cfg;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class Rule {
  final String identifier;
  final Element expression;

  public Rule(String identifier, Element expression) {
    this.identifier = requireNonNull(identifier);
    this.expression = requireNonNull(expression);
  }

  public String identifier() {
    return this.identifier;
  }

  public Element expression() {
    return this.expression;
  }

  /**
   * An exception will be thrown if `input` doesn't satisfy this `Rule`.
   *
   * @param input An input to be parsed.
   */
  public void parse(List<Object> input) {
    //ParsingResult parsingResult = this.expression.accept(, input);
    //      if (parsingResult.wasSuccessful())
    //        return;
    throw new RuntimeException();
  }

  public static class Builder {
    private final String identifier;
    private final List<Element> expressions = new ArrayList<>();

    public Builder(String identifier) {
      this.identifier = requireNonNull(identifier);
    }

    public Builder add(Object expression) {
      this.expressions.add(Bnf.toElement(requireNonNull(expression)));
      return this;
    }

    public Builder add(Object... expressions) {
      Builder ret = this;
      for (Object expression : expressions) {
        ret = this.add(expression);
      }
      return ret;
    }

    public Rule build() {
      if (this.expressions.isEmpty()) {
        throw new IllegalStateException();
      }
      if (this.expressions.size() == 1) {
        return new Rule(this.identifier, this.expressions.get(0));
      }
      return new Rule(this.identifier, ConcatenatedElement.create(this.expressions.toArray(new Element[0])));
    }


    public Rule expressions(Object... expressions) {
      return this.add(expressions).build();
    }
  }
}
