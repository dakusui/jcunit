package com.github.dakusui.jcunit.examples.bdd.lorry;

import com.github.dakusui.jcunit.core.Param;
import com.github.dakusui.jcunit.examples.bdd.JCBehavior;

public class ManageLorries {
  @JCBehavior.Given(@Param("I have gone to the new lorry page"))
  @JCBehavior.When({@Param("I fill in 'Name' with 'name 1'"), @Param("I press 'Create'")})
  @JCBehavior.Then(@Param("I should see 'name 1 - this is from before filter'"))
  public void registerNewLorry() {
  }
}
