package com.github.dakusui.jcunitx.runners.core;

import com.github.dakusui.jcunitx.core.AArray;
import com.github.dakusui.jcunitx.exceptions.FrameworkException;
import com.github.dakusui.jcunitx.factorspace.Constraint;
import com.github.dakusui.jcunitx.runners.junit4.annotations.Condition;
import com.github.dakusui.jcunitx.runners.junit4.annotations.ConfigureWith;
import com.github.dakusui.jcunitx.runners.junit4.annotations.From;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.dakusui.jcunitx.utils.Utils.createInstanceOf;
import static com.github.dakusui.jcunitx.exceptions.FrameworkException.unexpectedByDesign;
import static java.util.stream.Collectors.toList;

public enum NodeUtils {
  ;

  public static TestInputPredicate buildPredicate(String[] values, SortedMap<String, TestInputPredicate> predicates_) {
    class Builder implements Node.Visitor {
      private final SortedMap<String, TestInputPredicate> predicates   = predicates_;
      private Predicate<AArray>                           result;
      private final SortedSet<String>                     involvedKeys = new TreeSet<>();

      @Override
      public void visitLeaf(Node.Leaf leaf) {
        TestInputPredicate predicate = lookupTestPredicate(leaf.id()).orElseThrow(FrameworkException::unexpectedByDesign);
        involvedKeys.addAll(predicate.involvedKeys());
        if (leaf.args().length == 0)
          result = predicate;
        else
          result = tuple -> predicate.test(appendArgs(tuple, leaf));
      }

      private AArray appendArgs(AArray tuple, Node.Leaf leaf) {
        return new AArray.Builder() {{
          putAll(tuple);
          for (int i = 0; i < leaf.args().length; i++) {
            put(String.format("@arg[%s]", i), expandFactorValueIfNecessary(tuple, leaf.args()[i]));
          }
        }}.build();
      }

      private Object expandFactorValueIfNecessary(AArray tuple, String arg) {
        if (arg.startsWith("@"))
          return tuple.get(arg.substring(1));
        return arg;
      }

      @Override
      public void visitAnd(Node.And and) {
        and.children().forEach(
            node -> {
              Predicate<AArray> previous = result;
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
              Predicate<AArray> previous = result;
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

      private Optional<TestInputPredicate> lookupTestPredicate(String name) {
        return this.predicates.containsKey(name) ?
            Optional.of(this.predicates.get(name)) :
            Optional.empty();
      }
    }
    Builder builder = new Builder();
    parse(values).accept(builder);
    return TestInputPredicate.of(
        Arrays.toString(values),
        new ArrayList<>(builder.involvedKeys),
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

  public static SortedMap<String, TestInputPredicate> allTestPredicates(TestClass testClass) {
    ////
    // TestClass <>--------------> parameterSpace class
    //                               constraints
    //   non-constraint-condition    non-constraint-condition?
    // TestClass
    //   constraints
    //   non-constraint-condition
    return new TreeMap<>((Objects.equals(testClass.getJavaClass(), getParameterSpaceDefinitionClass(testClass)) ?
        streamTestPredicatesIn(testClass.getJavaClass()) :
        Stream.concat(
            streamTestPredicatesIn(getParameterSpaceDefinitionClass(testClass)),
            streamTestPredicatesIn(testClass.getJavaClass()).filter(
                each -> !(each instanceof Constraint)
            )
        )
    ).collect(Collectors.toMap(
        TestInputPredicate::getName,
        each -> each
    )));
  }

  private static Class<?> getParameterSpaceDefinitionClass(TestClass testClass) {
    ConfigureWith configureWith = testClass.getAnnotation(ConfigureWith.class);
    configureWith = configureWith == null ?
        ConfigureWith.DEFAULT_INSTANCE :
        configureWith;
    return Objects.equals(configureWith.parameterSpace(), Object.class) ?
        testClass.getJavaClass() :
        configureWith.parameterSpace();
  }

  private static Stream<TestInputPredicate> streamTestPredicatesIn(Class<?> parameterSpaceDefinitionClass) {
    TestClass wrapper = new TestClass(parameterSpaceDefinitionClass);
    Object testObject = createInstanceOf(wrapper);
    return wrapper.getAnnotatedMethods(Condition.class).stream(
    ).map(
        frameworkMethod -> createTestPredicate(testObject, frameworkMethod)
    );
  }

  public static TestInputPredicate createTestPredicate(Object testObject, FrameworkMethod frameworkMethod) {
    Method method = frameworkMethod.getMethod();
    //noinspection RedundantTypeArguments (to suppress a compilation error)
    List<String> involvedKeys = Stream.of(method.getParameterAnnotations())
        .map(annotations -> Stream.of(annotations)
            .filter(annotation -> annotation instanceof From)
            .map(From.class::cast)
            .map(From::value)
            .findFirst()
            .<FrameworkException>orElseThrow(FrameworkException::unexpectedByDesign))
        .collect(toList());
    int varargsIndex = method.isVarArgs() ?
        frameworkMethod.getMethod().getParameterCount() - 1 :
        -1;
    Predicate<AArray> predicate = (AArray tuple) -> {
      try {
        return (boolean) frameworkMethod.invokeExplosively(
            testObject,
            involvedKeys.stream()
                .map(new Function<String, Object>() {
                  final AtomicInteger cur = new AtomicInteger(0);

                  @Override
                  public Object apply(String key) {
                    if (key.equals("@arg"))
                      return isVarArgs(cur.get()) ?
                          getVarArgs() :
                          getArg();
                    return tuple.get(key);
                  }

                  private Object getArg() {
                    return tuple.get(key(cur.getAndIncrement()));
                  }

                  private Object getVarArgs() {
                    List<Object> work = new LinkedList<>();
                    while (tuple.containsKey(key(cur.get()))) {
                      work.add(getArg());
                    }
                    return work.toArray();
                  }

                  private boolean isVarArgs(int argIndex) {
                    return argIndex == varargsIndex;
                  }

                  private String key(int i) {
                    return String.format("@arg[%d]", i);
                  }
                })
                .toArray());
      } catch (Throwable e) {
        throw unexpectedByDesign(e);
      }
    };
    return frameworkMethod.getAnnotation(Condition.class).constraint() ?
        Constraint.create(frameworkMethod.getName(), predicate, involvedKeys) :
        new TestInputPredicate() {
          @Override
          public String getName() {
            return frameworkMethod.getName();
          }

          @Override
          public boolean test(AArray tuple) {
            return predicate.test(tuple);
          }

          @Override
          public List<String> involvedKeys() {
            return involvedKeys;
          }
        };
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
