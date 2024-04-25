package com.github.dakusui.jcunit8.sandbox.example;


import com.github.dakusui.jcunit8.sandbox.annotations.DefineFactor;
import com.github.dakusui.jcunit8.sandbox.annotations.DefineFactorSpace;
import com.github.dakusui.jcunit8.sandbox.annotations.From;

@DefineFactorSpace(
    factors = {
        @DefineFactor(name = "givenName", args = {"Yoshihiko", "Risa", "Hiroshi"}),
        @DefineFactor(name = "familyName", args = {"Naito", "Kitajima", "Ukai"}),
        @DefineFactor(name = "title", args = {"Mr.", "Ms.", "Dr."}),
        @DefineFactor(name= "fullName", args = {"familyNameFirst", "givenNameFirst", "withTitle"})
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

  /*
   NON-FACTOR
   fullName()        |fullName       |title|given    |family
   Mr. naito hiroshi |withTitle      |Mr.  |hiroshi  |naito
   naito hiroshi     |familyNameFirst|-    |hiroshi  |naito
   risa ukai         |givenNameFirst |-    |risa     |ukai
   yoshihiko kitajima|givenNameFirst |-    |yoshihiko|kitajima
   */
}
