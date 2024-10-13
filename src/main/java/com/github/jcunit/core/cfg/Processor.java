package com.github.jcunit.core.cfg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.github.valid8j.classic.Requires.requireNonNull;

public interface Processor<R extends ProcessingResult<R>> {
  default R before(Element element, R ongoingInput) {
    throw new UnsupportedOperationException();
  }

  default R pre(TermElement element, R ongoingInput) {
    return ongoingInput;
  }

  default R post(TermElement element, R ongoingInput) {
    return ongoingInput;
  }

  default R pre(ReferenceElement element, R ongoingInput) {
    return ongoingInput;
  }

  default R post(ReferenceElement element, R ongoingInput) {
    return ongoingInput;
  }

  default R pre(AlterationElement element, R ongoingInput) {
    return ongoingInput;
  }

  default R post(AlterationElement element, R ongoingInput) {
    return ongoingInput;
  }

  default R pre(ConcatenatedElement element, R ongoingInput) {
    return ongoingInput;
  }

  default R post(ConcatenatedElement element, R ongoingInput) {
    return ongoingInput;
  }

  Stream<Element> resolve(String identifier);

  R result();

  static <R extends ProcessingResult<R>> Processor<R> create(Bnf bnf, Function<Processor<R>, R> func) {
    return new Processor<R>() {
      final Map<String, List<Rule>> rules = indexRules(bnf.rules());


      @Override
      public R pre(TermElement element, R ongoingInput) {
        return null;
      }

      @Override
      public R post(TermElement element, R ongoingInput) {
        return null;
      }

      @Override
      public R pre(ReferenceElement element, R ongoingInput) {
        return null;
      }

      @Override
      public R post(ReferenceElement element, R ongoingInput) {
        return null;
      }

      @Override
      public R pre(AlterationElement element, R ongoingInput) {
        return null;
      }

      @Override
      public R post(AlterationElement element, R ongoingInput) {
        return null;
      }

      @Override
      public R pre(ConcatenatedElement element, R ongoingInput) {
        return null;
      }

      @Override
      public R post(ConcatenatedElement element, R ongoingInput) {
        return null;
      }

      @Override
      public Stream<Element> resolve(String identifier) {
        return rules.containsKey(requireNonNull(identifier)) ? rules.get(identifier).stream().map(Rule::expression)
                                                             : Stream.empty();
      }

      @Override
      public R result() {
        return func.apply(this);
      }

      private Map<String, List<Rule>> indexRules(List<Rule> rules) {
        Map<String, List<Rule>> ret = new HashMap<>();
        for (Rule rule : rules) {
          List<Rule> cur = ret.computeIfAbsent(rule.identifier(), k -> new ArrayList<>());
          cur.add(rule);
        }
        return ret;
      }
    };
  }
}
