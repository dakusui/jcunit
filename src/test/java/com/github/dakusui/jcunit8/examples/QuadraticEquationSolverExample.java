package com.github.dakusui.jcunit8.examples;

import com.github.dakusui.jcunit.runners.standard.annotations.Given;
import com.github.dakusui.jcunit8.runners.junit4.annotations.From;
import org.junit.Test;

public class QuadraticEquationSolverExample {
  @Given
  @Test
  public void whenSolve$thenSolved(
      @From("a") int a,
      @From("b") int b,
      @From("c") int c
  ) {

  }
}
