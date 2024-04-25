package com.github.dakusui.jcunit8.sandbox.example;


import com.github.dakusui.jcunit8.sandbox.annotations.DefineFactor;
import com.github.dakusui.jcunit8.sandbox.annotations.DefineFactorSpace;
import com.github.dakusui.jcunit8.sandbox.annotations.From;
import com.github.dakusui.jcunit8.sandbox.core.FactorFactory.StringLevels;
import com.github.dakusui.jcunit8.sandbox.core.FactorFactory.MethodNames;

@DefineFactorSpace(
    factors = {
        @DefineFactor(name = "givenName", with = {"Yoshihiko", "Risa", "Hiroshi"}, as = StringLevels.class),
        @DefineFactor(name = "familyName", with = {"Naito", "Kitajima", "Ukai"}),
        @DefineFactor(name = "title", with = {"Mr.", "Ms.", "Dr."}),
        @DefineFactor(name= "fullName", with = {"familyNameFirst", "givenNameFirst", "withTitle"}, as = MethodNames.class)
    },
    constraints = {
    
    }
)
public abstract class ExampleFactorSpace {
  public static String familyNameFirst(@From("givenName") String given, @From("familyName") String family) {
    return String.format("%s, %s", family, given);
  }

  public static String givenNameFirst(@From("givenName") String given, @From("familyName") String family) {
    return String.format("%s %s", given, family);
  }

  public static String withTitle(@From("givenName") String given, @From("familyName") String family, @From("title") String title) {
    return String.format("%s %s %s", title, given, family);
  }
  
  public static boolean test() {
    return false;
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
