package com.github.dakusui.jcunit8.sandbox.example;

import com.github.jcunit.annotations.From;
import com.github.jcunit.annotations.JCUnitParameter;
import com.github.jcunit.annotations.JCUnitParameterValue;
import com.github.jcunit.annotations.JCUnitTest;
import com.github.jcunit.core.model.ParameterSpaceSpec;
import com.github.jcunit.core.model.ParameterSpec;
import com.github.jcunit.core.model.ValueResolver;
import com.github.jcunit.runners.junit5.JCUnitTestExtension;
import com.github.valid8j.pcond.core.refl.MethodQuery;
import com.github.valid8j.pcond.core.refl.ReflUtils;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

/**
 * // @formatter:off 
 * // @formatter:on 
 */
@ExtendWith(JCUnitTestExtension.class)
public class TestExample2 {
  @JCUnitParameter
  public static List<ValueResolver<String>> param1() {
    return asList(ValueResolver.fromValue("hello").$(),
                  ValueResolver.fromValue("world").$(),
                  ValueResolver.with(t -> (String) t.get("param3")).$("param3"),
                  ValueResolver.fromStaticMethod(findMethod(MethodQuery.classMethod(TestExample2.class, "param1Value1", "String")))
    );
  }

  @JCUnitParameterValue("param1Value1")
  public static String param1Value1(@From("param3") String param3) {
    return "Hello, " + param3;
  }

  public static ParameterSpaceSpec parameterSpaceSpec() {
    return ParameterSpaceSpec.create(
        ParameterSpec.create("",
                             ValueResolver.fromValue("hello").$(),
                             ValueResolver.fromValue("world").$(),
                             ValueResolver.with(t -> (String) t.get("param3")).$("param3"),
                             ValueResolver.fromStaticMethod(findMethod(MethodQuery.classMethod(TestExample2.class, "param1Value1", "String")))),
        ParameterSpec.create("")
    );
  }


  @JCUnitTest
  public void test(@From("param1") String param1, @From("param2") Map<String, List<String>> param2) {

  }


  static Method findMethod(MethodQuery methodQuery) {
    return ReflUtils.findMethod(methodQuery.targetClass(), methodQuery.methodName(), methodQuery.arguments());
  }
}
