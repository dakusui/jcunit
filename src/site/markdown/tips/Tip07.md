# Executing test methods based on given conditions
Also you sometimes want to let JCUnit decide a certain test method should be executed
based on some conditions automatically.

E.g, in case you are testing quadratic equation solver program, if a discriminant of a given
equation is negative, you want to check if error handling is working correct,
but probably you don't want to perform a test to check if the solutions are precise.
Of course the value is non-negative, you don't need a test for error handling and
do need a test for solutions' precision.

For this purpose, JCUnit provides another annotation '@When'.
Below is an example to illustrate how this feature should be used.

```java

  @Test
  @When( "discriminantIsNonNegative" )
  public void thenSolveQuadraticEquation() {
    ...
  }
```

JCUnit will invoke a method whose name is 'discriminantIsNonNegative' and iff its
return value is true, this test method will be executed.

If the coefficients of an equation, i.e. factors of this test class, are a, b, and c,
the method would look like below.

```java

  public boolean discriminantIsNonNegative() {
      return b * b - 4 * c * a >= 0;
  }
```

If you want to add another condition you can also do below.

```java

  @Test
  @When( "aIsNonZero&&discriminantIsNonNegative" )
  public void thenSolveQuadraticEquation() {
      ...
  }
```

In this example, you are making sure that a is not zero AND the discriminant is non-negative
before this method is executed.

Also you can do

```java

  @Test
  @When( "!discriminantIsNonNegative" )
  public void thenAnErrorWillBeReported() {
      ....
  }
```

And, to express 'OR', do

```java

  @Test
  @When({"!discriminantIsNonNegative", "!aIsNonZero"})
  public void thenAnErrorWillBeReported() {
      ....
  }
```

Unfortunately you cannot use parentheses as of now.
