package com.github.dakusui.jcunit8.sandbox.example;


import com.github.jcunit.annotations.DefineParameter;
import com.github.jcunit.annotations.DefineParameterSpace;
import com.github.jcunit.annotations.From;
import com.github.jcunit.annotations.MethodName;
import com.github.jcunit.core.model.FactorFactory.ValueResolvingMethodNames;
import org.junit.jupiter.api.Test;

@DefineParameterSpace(
    factors = {
        @DefineParameter(name = "givenName", with = {"Yoshihiko", "Risa", "Hiroshi"}),
        @DefineParameter(name = "familyName", with = {"Naito", "Kitajima", "Ukai"}),
        @DefineParameter(name = "title", with = {"Mr.", "Ms.", "Dr."}),
        @DefineParameter(name = "fullName", with = {"familyNameFirst", "givenNameFirst", "withTitle"}, as = ValueResolvingMethodNames.class)
    },
    constraints = {
        "!isMister&&givenNameLooksFemale"
    }
)
public abstract class ExampleFactorSpace {
  @MethodName
  public static String familyNameFirst(@From("givenName") String given, @From("familyName") String family) {
    return String.format("%s, %s", family, given);
  }
  
  @MethodName
  public static String givenNameFirst(@From("givenName") String given, @From("familyName") String family) {
    return String.format("%s %s", given, family);
  }
  
  @MethodName
  public static String withTitle(@From("givenName") String given, @From("familyName") String family, @From("title") String title) {
    return String.format("%s %s %s", title, given, family);
  }
  
  @MethodName
  public static boolean isMister(@From("title") String title) {
    return false;
  }
  
  @MethodName
  public static boolean givenNameLooksFemale(@From("givenName") String givenName) {
    return false;
  }
  
  
  public String greeting(String name) {
    return "Hello, " + name + "!";
  }
  

  @MethodName
  public void whenNameLooksFemale_thenTitleInGreetingIsNotMr(@From("fullName") String fullName) {
    System.out.println(greeting(fullName));
  }

  /*
   NON-FACTOR
   fullName()        |fullName       |title|given    |family
   Mr. naito hiroshi |withTitle      |Mr.  |hiroshi  |naito
   naito hiroshi     |familyNameFirst|-    |hiroshi  |naito
   risa ukai         |givenNameFirst |-    |risa     |ukai
   yoshihiko kitajima|givenNameFirst |-    |yoshihiko|kitajima
   */
}
