package com.github.dakusui.petronia.examples;

import com.github.dakusui.jcunit.compat.core.JCUnit;
import com.github.dakusui.jcunit.compat.core.RuleSet;
import com.github.dakusui.jcunit.compat.core.annotations.Generator;
import com.github.dakusui.jcunit.compat.core.annotations.In;
import com.github.dakusui.jcunit.compat.core.annotations.In.Domain;
import com.github.dakusui.jcunit.core.JCUnitBase;
import com.github.dakusui.jcunit.compat.core.annotations.Out;
import com.github.dakusui.jcunit.compat.generators.SimpleTestArrayGenerator;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Method;

public class MethodFinderTest extends JCUnitBase {
  @RunWith(JCUnit.class)
  @Generator(SimpleTestArrayGenerator.class)
  public static class MethodFinderTest1 extends MethodFinderTest {
    @Rule
    public RuleSet verifier = ruleSet()
        .incase(
            and(isoneof(get("clazz"), String.class,
                    Object.class),
                is(get("methodName"), "toString"),
                eq(get("parameterTypes"),
                    new Object[0])
            ),
            not(is(get("method"), null))
        )
        .incase(
            and(is(get("methodName"), "equals"),
                eq(get("parameterTypes"),
                    new Object[0])
            ),
            isinstanceof(get("exception"),
                NoSuchMethodException.class)
        )
        .incase(
            and(is(get("methodName"), "matches"),
                eq(get("parameterTypes"),
                    new Object[0])
            ),
            isinstanceof(get("exception"),
                NoSuchMethodException.class)
        )
        .incase(
            is(get("clazz"), null),
            isinstanceof(get("exception"),
                NullPointerException.class)
        )
        .incase(
            is(get("methodName"), null),
            isinstanceof(get("exception"),
                NullPointerException.class)
        )
        .incase(
            is(get("parameterTypes"), null),
            isinstanceof(get("exception"),
                NullPointerException.class)
        )
        .incase(
            is(get("methodName"), "notFound"),
            isinstanceof(get("exception"),
                NoSuchMethodException.class)
        )
        .incase(
            and(is(get("methodName"), "toString"),
                not(or(
                    eq(get("parameterTypes"),
                        new Object[0]),
                    eq(get("parameterTypes"), null)
                ))
            ),
            isinstanceof(get("exception"),
                NoSuchMethodException.class)
        );

    @In(
        domain = Domain.Method)
    public Class<?>[] parameterTypes;

    public static Class<?>[][] parameterTypes() {
      return new Class<?>[][] { new Class[] { }, new Class[] { Object.class },
          new Class[] { String.class }, null };
    }

    @Test
    public void test() throws SecurityException, NoSuchMethodException {
      System.err.printf("clazz=%s, methodName=%s, paramTypes=%s\n", this.clazz,
          this.methodName, ArrayUtils.toString(this.parameterTypes));
      try {
        MethodFinder finder = new MethodFinder();
        finder.setClass(this.clazz);
        finder.setName(this.methodName);
        finder.setSignature(this.parameterTypes);
        this.method = finder.find();
      } catch (Exception e) {
        this.exception = e;
      }
    }
  }

  @RunWith(JCUnit.class)
  @Generator(SimpleTestArrayGenerator.class)
  public static class MethodFinderTest2 extends MethodFinderTest {
    @Rule
    public RuleSet verifier = ruleSet()
        .incase(
            and(isoneof(get("clazz"), String.class,
                    Object.class),
                is(get("methodName"), "toString"),
                eq(get("parameterTypes"), "")
            ),
            not(is(get("method"), null))
        )
        .incase(
            and(is(get("methodName"), "equals"),
                eq(get("parameterTypes"), "")),
            isinstanceof(get("exception"),
                NoSuchMethodException.class)
        )
        .incase(
            and(is(get("methodName"), "matches"),
                eq(get("parameterTypes"), "")),
            isinstanceof(get("exception"),
                NoSuchMethodException.class)
        )
        .incase(
            is(get("clazz"), null),
            isinstanceof(get("exception"),
                NullPointerException.class)
        )
        .incase(
            is(get("methodName"), null),
            isinstanceof(get("exception"),
                NullPointerException.class)
        )
        .incase(
            is(get("parameterTypes"), null),
            isinstanceof(get("exception"),
                NullPointerException.class)
        )
        .incase(
            isoneof(get("parameterTypes"), "Q",
                "Lnot.Found;"),
            isinstanceof(get("exception"),
                IllegalArgumentException.class)
        )
        .incase(
            is(get("methodName"), "notFound"),
            isinstanceof(get("exception"),
                NoSuchMethodException.class)
        )
        .incase(
            and(is(get("methodName"), "toString"),
                not(or(
                    eq(get("parameterTypes"),
                        "Lnot.Found;"),
                    eq(get("parameterTypes"), ""),
                    eq(get("parameterTypes"), "Q"),
                    eq(get("parameterTypes"), null)
                ))
            ),
            isinstanceof(get("exception"),
                NoSuchMethodException.class)
        );

    @In(
        domain = Domain.Method)
    public String parameterTypes;

    public static String[] parameterTypes() {
      return new String[] { "", "Ljava.lang.Object;", "Ljava.lang.String;",
          "Q", "Lnot.Found;", null };
    }

    @Test
    public void test() throws Exception {
      try {
        MethodFinder finder = new MethodFinder();
        finder.setClass(this.clazz);
        finder.setName(this.methodName);
        finder.setSignature(this.parameterTypes);
        this.method = finder.find();
      } catch (Exception e) {
        this.exception = e;
        if (e instanceof ClassNotFoundException) {
          throw e;
        }
      }
    }
  }

  @In(
      domain = Domain.Method)
  public Class<?> clazz;

  public static Class<?>[] clazz() {
    return new Class<?>[] { Object.class, String.class, null };
  }

  @In(
      domain = Domain.Method)
  public String methodName;

  public static String[] methodName() {
    return new String[] { "toString", "equals", "matches", "notFound", null };
  }

  @Out
  public Method method;

  @Out
  public Exception exception;

  public static void main(String[] args) throws ClassNotFoundException,
      SecurityException, NoSuchMethodException {
    System.out.println(Class.forName("[Ljava.lang.String;"));
    System.out.println(Class.forName("[[I"));
    System.out.println(Integer.TYPE);
    System.out.println(new MethodFinder().setClass(String.class)
        .setName("toString").find());
    System.out.println(new MethodFinder().setClass(String.class)
        .setName("charAt").setSignature("I").find());
    System.out.println(new MethodFinder().setClass(String.class)
        .setName("equals").setSignature(new Class[] { Object.class }).find());
    System.out.println(new MethodFinder().setClass(String.class)
        .setName("equals").setSignature(new Class[] { null }).find());
    System.out.println(new MethodFinder().setClass(String.class)
        .setName("charAt").find());
  }
}
