package com.github.jcunit.core.cfg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

public interface Bnf {
  List<Rule> rules();

  void parse(Processor<ProcessingResult.Default> parser, Object... input);

  static Element seq(Object... args) {
    return ConcatenatedElement.create(toElements(args));
  }

  static Element or(Object... args) {
    return AlterationElement.create(toElements(args));
  }

  static Element ref(String reference) {
    return ReferenceElement.create(reference);
  }

  static Element term(Object term) {
    return TermElement.create(term);
  }

  /**
   * A reference element (`ReferenceElement`) created from `reference` will be returned.
   * A synonym for `ref(String reference)`.
   *
   * @param reference A name of a rule to be referenced.
   * @return A reference element.
   */
  static Element $(String reference) {
    requireNonNull(reference);
    return ref(reference);
  }

  static Element[] toElements(Object[] elements) {
    return Arrays.stream(elements).map(Bnf::toElement).toArray(Element[]::new);
  }

  static Element toElement(Object e) {
    return e instanceof Element ? (Element) e : TermElement.create(e);
  }

  static Rule.Builder rule(String identifier) {
    return new Rule.Builder(identifier);
  }

  class Builder {
    List<Rule> rules = new ArrayList<>();
    private final String startingSymbol;

    public Builder(String startingSymbol) {
      this.startingSymbol = requireNonNull(startingSymbol);
    }

    public Builder add(Rule rule) {
      rules.add(requireNonNull(rule));
      return this;
    }

    public Builder add(String identifier, Object... args) {
      this.add(new Rule.Builder(identifier).add(args).build());
      return this;
    }

    public Builder rule(String identifier, Object... elements) {
      return this.add(Bnf.rule(identifier).expressions(elements));
    }

    public Bnf build() {
      final List<Rule> rules = unmodifiableList(Builder.this.rules);
      return new Bnf() {
        public String startingSymbol() {
          return Builder.this.startingSymbol;
        }

        @Override
        public List<Rule> rules() {
          return rules;
        }

        @Override
        public void parse(Processor<ProcessingResult.Default> processor, Object... input) {
          ProcessingResult.Default processingResult = processor.resolve(startingSymbol())
                                                               .map(s -> s.accept(processor, ProcessingResult.Default.from(input)))
                                                               .filter(r -> r.wasSuccessful() && r.remainingInput().isEmpty())
                                                               .findFirst()
                                                               .orElseThrow(RuntimeException::new);
          System.out.println(processingResult);
        }
      };
    }
  }

  /**
   * <statements> ::= <statement> | <statements> <statement>
   * <statement> ::= <assignment> | <for_loop> | <if_statement> | <function_call>
   * <for_loop> ::= "for" "(" <assignment> ";" <condition> ";" <update> ")" "{" <statements> "}"
   * <condition> ::= <identifier> "<" <value>
   * <update> ::= <identifier> "++"
   */
  static void main(String... args) {
    Bnf bnf = new Builder("statements")
        .rule("statements", or($("statement"), seq($("statements"), $("statement"))))
        .rule("statement", or($("assignment"), $("for_loop")))
        .rule("for_loop", "for", "(", $("assignment"), ";", $("condition"), ";", $("update"), ")", "{", $("statement"), "}")
        .rule("assignment", $("identifier"), "=", $("value"))
        .rule("condition", $("identifier"), "<", $("value"))
        .rule("update", $("identifier"), "++")
        .rule("identifier", or("i", "j", "k"))
        .rule("value", or("0", "1", "10", "100"))
        .build();

    bnf.parse(Processor.create(bnf, null),
              "for", "(", "i", "=", "0", ";", "i", "<", "10", ";", "i", "++", ")", "{",
              "j", "=", "100",
              "}");
  }
}
