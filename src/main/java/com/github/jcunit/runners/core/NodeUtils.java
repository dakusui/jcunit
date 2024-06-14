package com.github.jcunit.runners.core;

import com.github.jcunit.core.tuples.Tuple;
import com.github.jcunit.exceptions.FrameworkException;
import com.github.jcunit.factorspace.TuplePredicate;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public enum NodeUtils {
  ;
  public static TuplePredicate buildPredicate(String[] values, SortedMap<String, TuplePredicate> predicates_) {
    class Builder implements Node.Visitor {
      private final SortedMap<String, TuplePredicate> predicates = predicates_;
      private Predicate<Tuple> result;
      private final SortedSet<String> involvedKeys = new TreeSet<>();
      
      @Override
      public void visitLeaf(Node.Leaf leaf) {
        TuplePredicate predicate = lookupTestPredicate(leaf.id()).orElseThrow(FrameworkException::unexpectedByDesign);
        involvedKeys.addAll(predicate.involvedKeys());
        if (leaf.args().length == 0)
          result = predicate;
        else
          result = tuple -> predicate.test(appendArgs(tuple, leaf));
      }
      
      private Tuple appendArgs(Tuple tuple, Node.Leaf leaf) {
        return new Tuple.Builder() {{
          putAll(tuple);
          for (int i = 0; i < leaf.args().length; i++) {
            put(String.format("@arg[%s]", i), expandFactorValueIfNecessary(tuple, leaf.args()[i]));
          }
        }}.build();
      }
      
      private Object expandFactorValueIfNecessary(Tuple tuple, String arg) {
        if (arg.startsWith("@"))
          return tuple.get(arg.substring(1));
        return arg;
      }
      
      @Override
      public void visitAnd(Node.And and) {
        and.children().forEach(
            node -> {
              Predicate<Tuple> previous = result;
              node.accept(this);
              if (previous != null) {
                result = previous.and(result);
              }
            });
      }
      
      @Override
      public void visitOr(Node.Or or) {
        or.children().forEach(
            node -> {
              Predicate<Tuple> previous = result;
              node.accept(this);
              if (previous != null) {
                result = previous.or(result);
              }
            });
      }
      
      @Override
      public void visitNot(Node.Not not) {
        not.target().accept(this);
        result = result.negate();
      }
      
      private Optional<TuplePredicate> lookupTestPredicate(String name) {
        return this.predicates.containsKey(name) ?
            Optional.of(this.predicates.get(name)) :
            Optional.empty();
      }
    }
    Builder builder = new Builder();
    parse(values).accept(builder);
    return TuplePredicate.of(
        Arrays.toString(values),
        new ArrayList<>(builder.involvedKeys),
        builder.result
    );
  }
  
  public static Node parse(String[] values) {
    return new Node.Or.Impl(Stream.of(values)
        .map(NodeUtils::parseLine)
        .collect(toList()));
  }
  
  public static Node parseLine(String value) {
    return new Node.And.Impl(
        Stream.of(value.split("&&"))
            .map(s -> s.startsWith("!") ?
                new Node.Not.Impl(new Node.Leaf.Impl(s.substring(1))) :
                new Node.Leaf.Impl(s)
            )
            .collect(toList())
    );
  }
}
