# Customizing the strength of t-wise testing.
  Add parameter 'generator = @Generator(IPO2TestCaseGenerator.class)' explicitly to the '@TestCaseGeneration',
  annotation for 'QuadraticEquationSolverTest1.java' and set the first parameter, which represents a 'strength'.

  ```java

      @RunWith(JCUnit.class)
      @TestCaseGeneration(
              generator = @Generator(
                      value = IPO2TestCaseGenerator.class,
                      params = { @Param("3") }))
      public class QuadraticEquationSolverTest1 {
  ```

  In this example, the line

  ```java

      params = { @Param("3") }))

  ```

  configures the strength of the t-wise tests performed by JCUnit.

  And '@Param' annotation is a standard way to give a parameter to JCUnit plugins (excepting '@FactorField', which requires more conciseness).
  It takes a string array as its 'value'(and remember that you can omit curly braces if there is only one element in the array).
  JCUnit internally translates those values accordingly.
