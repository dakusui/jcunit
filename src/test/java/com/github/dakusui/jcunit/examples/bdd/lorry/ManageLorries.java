package com.github.dakusui.jcunit.examples.bdd.lorry;

import com.github.dakusui.jcunit.core.When;
import com.github.dakusui.jcunit.examples.bdd.Given;
import com.github.dakusui.jcunit.examples.bdd.Then;

public class ManageLorries {
  @Given("I have gone to the new lorry page")
  @When({"I fill in 'Name' with 'name 1'", "I press 'Create'"})
  @Then("I should see 'name 1 - this is from before filter'")
  public void registerNewLorry() {
  }
}
