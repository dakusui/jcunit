package com.github.dakusui.jcunit8.runners.core;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.exceptions.FrameworkException;
import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.factorspace.TestPredicate;
import com.github.dakusui.jcunit8.runners.junit4.annotations.Condition;
import com.github.dakusui.jcunit8.runners.junit4.annotations.From;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.github.dakusui.jcunit8.core.Utils.createInstanceOf;
import static com.github.dakusui.jcunit8.exceptions.FrameworkException.unexpectedByDesign;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public enum NodeUtils {
  ;

  public static TestPredicate buildPredicate(String[] values, TestClass parameterSpaceDefinitionClass) {
    class Builder implements Node.Visitor {
      private final SortedMap<String, TestPredicate> predicates = allTestPredicates(parameterSpaceDefinitionClass);
      private Predicate<Tuple> result;
      private final SortedSet<String> involvedKeys = new TreeSet<>();

      @Override
      public void visitLeaf(Node.Leaf leaf) {
        TestPredicate predicate = lookupTestPredicate(leaf.id()).orElseThrow(FrameworkException::unexpectedByDesign);
        involvedKeys.addAll(predicate.involvedKeys());
        result = predicate;
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

      private Optional<TestPredicate> lookupTestPredicate(String name) {
        return this.predicates.containsKey(name) ?
            Optional.of(this.predicates.get(name)) :
            Optional.empty();
      }
    }
    Builder builder = new Builder();
    parse(values).accept(builder);
    return TestPredicate.of(
        builder.involvedKeys.stream().collect(toList()),
        builder.result
    );
  }

  public static List<String> allLeaves(String[] values) {
    return new LinkedList<String>() {
      {
        parse(values).accept(
            new Node.Visitor.Base() {
              @Override
              public void visitLeaf(Node.Leaf leaf) {
                add(leaf.id());
              }
            });
      }
    };
  }

  public static SortedMap<String, TestPredicate> allTestPredicates(TestClass parameterSpaceDefinitionClass) {
    Object testObject = createInstanceOf(parameterSpaceDefinitionClass);
    return new TreeMap<>(parameterSpaceDefinitionClass.getAnnotatedMethods(Condition.class).stream()
        .collect(
            toMap(FrameworkMethod::getName,
                frameworkMethod -> createTestPredicate(testObject, frameworkMethod)
            )));
  }

  public static TestPredicate createTestPredicate(Object testObject, FrameworkMethod method) {
    //noinspection RedundantTypeArguments (to suppress a compilation error)
    List<String> involvedKeys = Stream.of(method.getMethod().getParameterAnnotations())
        .map(annotations -> Stream.of(annotations)
            .filter(annotation -> annotation instanceof From)
            .map(From.class::cast)
            .map(From::value)
            .findFirst()
            .<FrameworkException>orElseThrow(FrameworkException::unexpectedByDesign))
        .collect(toList());
    Predicate<Tuple> predicate = tuple -> {
      try {
        return (boolean) method.invokeExplosively(
            testObject,
            involvedKeys.stream()
                .map(tuple::get)
                .toArray());
      } catch (Throwable e) {
        throw unexpectedByDesign(e);
      }
    };
    return method.getAnnotation(Condition.class).constraint() ?
        Constraint.create(predicate, involvedKeys) :
        new TestPredicate() {
          @Override
          public boolean test(Tuple tuple) {
            return predicate.test(tuple);
          }

          @Override
          public List<String> involvedKeys() {
            return involvedKeys;
          }
        }
        ;
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
