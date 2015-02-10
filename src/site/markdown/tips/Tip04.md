# Defining constraints
In testings, we sometimes want to exclude a certain pair (, a triple, or a tuple) from the test cases since there are constraints in the test domain.

For example, suppose there is a software system which has 100 parameters and doesn't accept any larger value than 1,000 for parameter x1.
And it validates all the parameters and immediately exits if any one of them is invalid at start up.

Combinatorial testing is an effort to cover pairs (or tuples) with test cases as less as possible to find bugs which cannot be found by testing any single input parameter.
If a test case, which can possibly contain a lot of meaningful pairs, is revoked by a single parameter, it means the coverage of the test suite will be damaged.
Below are the links that would be helpful for understanding how much constraint managements are important in combinatorial testing area.

* [Combinatorial test cases with constraints in software systems](http://ieeexplore.ieee.org/xpl/articleDetails.jsp?reload=true&arnumber=6221818)
* [An Efficient Algorithm for Constraint Handling in Combinatorial Test Generation](http://ieeexplore.ieee.org/xpl/articleDetails.jsp?reload=true&arnumber=6569736)

(Of course we want to test the behaviors on such illegal inputs. It will be discussed in the next tip.)

For this purpose, JCUnit has a mechanism called 'constraints manager'.
To use a constraint manager, you can do below

```java

  @RunWith(JCUnit.class)
  @TestCaseGeneration(
      constraint = @Constraint(
          value = QuadraticEquationSolverTestX.CM.class ))
  public class QuadraticEquationSolverTestX {
  ...
```

'CM' is a name of inner class which implements 'ConstraintManager' interface and checks if a given tuple violates any constraints in the test domain or not.
Unfortunately you cannot use anonymous classes because a value passed to an annotation as its parameter must be a constant and Java's anonymous classes cannot be one.

If you want to exclude test cases that violate a discriminant of the quadratic equation formula and also make sure the given equation is a quadratic,
not a linear, the definition of 'CM' would be

```java

  public static class CM extends ConstraintManagerBase {
      @Override
      public boolean check(Tuple tuple) throws JCUnitSymbolException {
        if (!tuple.containsKey("a") || !tuple.containsKey("b") || !tuple
            .containsKey("c")) {
          throw new UndefinedSymbol();
        }
        int a = (Integer) tuple.get("a");
        int b = (Integer) tuple.get("b");
        int c = (Integer) tuple.get("c");
        return a != 0 && b * b - 4 * c * a >= 0;
      }
  }
```

'ConstraintManagerBase' is a helper class that makes it easy to implement a ConstraintManager.
All that you need is overriding 'boolean check(Tuple)' method.
In case the passed tuple violates your constraints, make it return false.
A test case is passed as a tuple, you need to use 'get' method of it to retrieve the value (level) of it.

"a", "b", or "c" are the names of the fields annotated with '@FactorField'. JCUnit accesses them using 'reflection' techniques of Java.
JCUnit avoids using tuples for which check method of the specified constraint manager returns 'false'.
