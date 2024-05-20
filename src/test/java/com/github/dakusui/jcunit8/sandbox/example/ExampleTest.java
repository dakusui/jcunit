package com.github.dakusui.jcunit8.sandbox.example;


import com.github.jcunit.annotations.*;
import com.github.jcunit.runners.junit5.JCUnitTestExtension;
import com.github.valid8j.fluent.Expectations;
import com.github.valid8j.pcond.core.fluent.builtins.StringTransformer;
import com.github.valid8j.pcond.forms.Printables;
import jdk.nashorn.internal.ir.annotations.Ignore;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static com.github.valid8j.fluent.Expectations.assertStatement;
import static com.github.valid8j.fluent.Expectations.value;
import static org.junit.jupiter.api.Assertions.assertEquals;


@Ignore
@ExtendWith(JCUnitTestExtension.class)
@UsingParameterSpace(ExampleTest.FactorSpace.class)
public class ExampleTest {
  public static class ClassUnderTest {
    public String greeting(String name) {
      return "Hello, " + name + "!";
    }
  }

  @DefineParameterSpace(
      parameters = {
          @DefineParameter(name = "givenName", with = {"Yoshihiko", "Risa", "Hiroshi"}),
          @DefineParameter(name = "familyName", with = {"Naito", "Kitajima", "Ukai"}),
          @DefineParameter(name = "title", with = {"Mr.", "Ms.", "Dr."}),
          @DefineParameter(name = "fullName", with = {"familyNameFirst", "givenNameFirst", "withTitle"})
      },
      constraints = {
          @DefineConstraint("!isMister&&givenNameLooksFemale")
      })
  public static class FactorSpace {

    @Named
    public static String withTitle(@From("givenName") String given, @From("familyName") String family, @From("title") String title) {
      return String.format("%s %s %s", title, given, family);
    }

    @Named
    public static boolean isMister(@From("title") String title) {
      return "Mr.".equals(title);
    }

    @Named
    public static boolean givenNameLooksFemale(@From("givenName") String givenName) {
      return "Risa".equals(givenName);
    }
  }

  static class ClassUnderTestTransformer extends Expectations.CustomTransformer<ClassUnderTestTransformer, ClassUnderTest> {
    /**
     * Creates an instance of this class.
     *
     * @param baseValue The target value of this transformer.
     */
    public ClassUnderTestTransformer(ClassUnderTest baseValue) {
      super(baseValue);
    }

    public StringTransformer<ClassUnderTest> greeting(String fullName) {
      return this.function(Printables.function("greeting[" + fullName + "]", v -> v.greeting(fullName))).asString();
    }
  }

  /*
   NON-FACTOR
   fullName()        |fullName       |title|given    |family
   Mr. naito hiroshi |withTitle      |Mr.  |hiroshi  |naito
   naito hiroshi     |familyNameFirst|-    |hiroshi  |naito
   risa ukai         |givenNameFirst |-    |risa     |ukai
   yoshihiko kitajima|givenNameFirst |-    |yoshihiko|kitajima
   */
  @BeforeAll
  public static void beforeAll() {
    System.out.println("beforeAll is called:" + System.currentTimeMillis());
  }

  @JCUnitTest
  public void whenNameLooksFemale_thenTitleInGreetingIsNotMr(@From("fullName") String fullName) {
    System.out.println("testMethod1 is called:" + System.currentTimeMillis());
    ClassUnderTest cut = new ClassUnderTest();

    System.out.println(cut.greeting(fullName));

    assertStatement(value(cut, ClassUnderTestTransformer::new).greeting(fullName).satisfies().notNull().notEmpty());
  }

  @JCUnitTest
  public void whenNameLooksMale_thenTitleInGreetingIsNotMs(@From("fullName") String fullName) {
    System.out.println("testMethod2 is called:" + System.currentTimeMillis());
    ClassUnderTest cut = new ClassUnderTest();

    System.out.println("fullName:" + cut.greeting(fullName));
    assertStatement(value(cut, ClassUnderTestTransformer::new).greeting(fullName).satisfies().notNull().notEmpty());
  }

  /**
   * @formatter:off
   * @formatter:on
   */
  public static class ParamsExample {
    @BeforeAll
    public static void beforeAll() {
      System.out.println("before all.");
    }

    @ParameterizedTest
    @CsvSource({"test,TEST", "tEst,TEST", "Java,JAVA"})
    void toUpperCase_ShouldGenerateTheExpectedUppercaseValue(String input, String expected) {
      String actualValue = input.toUpperCase();
      assertEquals(expected, actualValue);
    }
  }
}
