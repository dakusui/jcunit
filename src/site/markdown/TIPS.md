#Tips
## Tip 1: Customizing domains of @FactorField annotated fields (1)
JCUnit creates test cases by assigning a value, picked up from a hardcoded set of values defined for each type, to each '@FactorField' annotated field in a test class.
For example, if a member is annotated with '@FactorField' and its type is int, JCUnit will pick up a value from a set
{1, 0, -1, 100, -100, Integer.MAX_VALUE, Integer.MIN_VALUE}.
But this set is just a 'default' and you can customize it by using (overriding) an 'xyzLevels' attribute of a '@FactorField' annotation, 
where 'xyz' is a primitive types.

```java

    @FactorField(intLevels = { 0, 1, 2, -1, -2, 100, -100, 10000, -10000 })
    public int a;

```

Above is an example of the use the 'intLevels' attribute.


## Tip 2: Customizing domains of @FactorField annotated fields (2)
By using 'levelsProvider' parameter of '@FactorField' and creating a static method whose name is the same as the annotated field's name,
you can customize the domain of a certain field in a much more flexible way.

The method mustn't have any parameters and its return value must be an array of the field's type.

Below is the example for that sort of function.

```java

    @FactorField(levelsProvider = MethodLevelsProvider.class)
	public int a;
	
	public static int[] a() {
		return new int[]{0, 1, 2};
	}
	
```

The values returned by the method will be picked up and assigned to the field 'a' by the framework one by one.
And you need to use 'levelsProvider' attribute when you are going to use non-primitive, non-enum, nor non-string values as levels for a factor.

## Tip 3: Customizing the strength of t-wise testing.
Add parameter 'generator = @Generator(IPO2TestCaseGenerator.class)' explicitly to the '@TestCaseGeneration',
annotation for 'QuadraticEquationSolverTest1.java' and set the first parameter, which represents a 'strength'. 

```java

    @RunWith(JCUnit.class)
    @TupleGeneration(
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

## Tip 4: Defining constraints.
In testings, we sometimes want to exclude a certain pair (, a triple, or a tuple) from the test cases since there are constraints in the test domain.

For example, suppose there is a software system which has 100 parameters and doesn't accept any larger value than 1,000 for parameter x1.
And it validates all the parameters and immediately exits if any one of them is invalid at start up.

Combinatorial testing is an effort to cover pairs (or tuples) with test cases as less as possible to find bugs which cannot be found by testing any single input parameter.
If a test case, which can possibly contain a lot of meaningful pairs, is revoked by a single parameter, it means the coverage of the test suite will be damaged. 
Below are the links that would be helpful for understanding how much topLevelConstraint managements are important in combinatorial testing area. 

* [Combinatorial test cases with constraints in software systems](http://ieeexplore.ieee.org/xpl/articleDetails.jsp?reload=true&arnumber=6221818)
* [An Efficient Algorithm for Constraint Handling in Combinatorial Test Generation](http://ieeexplore.ieee.org/xpl/articleDetails.jsp?reload=true&arnumber=6569736)

(Of course we want to test the behaviors on such illegal inputs. It will be discussed in the next tip.) 

For this purpose, JCUnit has a mechanism called 'constraints manager'.
To use a topLevelConstraint manager, you can do below

```java

    @RunWith(JCUnit.class)
    @TupleGeneration(
        topLevelConstraint = @Constraint(
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
JCUnit avoids using tuples for which check method of the specified topLevelConstraint manager returns 'false'.

## Tip 5: Writing test cases for error handling (negative tests)
That being said, handling errors appropriately is another concern.
A program must complain of invalid parameters in an appropriate way.
And this characteristic is another aspect of software under test to be tested.

You can do it by overriding 'getViolations' method of a topLevelConstraint manager and switching the verification procedures based on a test's 'sub-identifier'.

First, by overriding method, return test cases that explicitly violate the topLevelConstraint represented by the topLevelConstraint manager class itself.

```java

    public static class CM extends ConstraintManagerBase {
        @Override
        public boolean check(Tuple tuple) throws JCUnitSymbolException {
            ...
        }
        
        @Override
        public List<Tuple> getViolations() {
            List<Tuple> ret = new LinkedList<Tuple>();
            ret.add(createTestCase("a=0", createTestCase(0, 1, 1)));
            ret.add(createTestCase("b*b-4ca<0", createTestCase(100, 1, 100)));
            ret.add(createTestCase("nonsense 1=0", createTestCase(0, 0, 1)));
            return ret;
        }
        
        private Tuple createTestCase(int a, int b, int c) {
            return new Tuple.Builder().put("a", a).put("b", b).put("c", c).build();
        }
    }
```

Now you enhance your test case so that it verifies the software under test behaves correctly in case invalid parameters are given.
Let's assume that the SUT should return null if one of the parameters 'a', 'b', or 'c' is not valid. 

```java

    @Test
    public void test() {
        if ("a=0".equals(desc.getSubIdentifier())) {
          assertEquals(null, s);
        } else if ("b*b-4ca<0".equals(desc.getSubIdentifier())) {
          assertEquals(null, s);
        } else if ("nonsense 1=0".equals(desc.getSubIdentifier())) {
          assertEquals(null, s);
        } else {
            QuadraticEquationSolver.Solutions s = new QuadraticEquationSolver(a, b,
                    c).solve();
            assertEquals(0.0, a * s.x1 * s.x1 + b * s.x1 + c);
            assertEquals(0.0, a * s.x2 * s.x2 + b * s.x2 + c);
        }
    }
```

## Tip 6: Filtering test cases

From time to time, you want to disable/enable test cases in order to concentrate some among all of them.
'@Precondition' notation is your friend in this use case.

You can define a method returning boolean and with no parameter like below,

```java

    @FactorField(intLevels = { 1, 2, 3 })
    public int a;
    
    ...
    
    @Precondition
    public boolean filterMethod() {
      return this.a == 3;
    }
```

By defining such a method, you can disable test cases which result in the method's value false.
In the example above, test cases whose factor 'a' is non 3 will no longer be executed.

## Tip 7: Executing test methods based on given conditions

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

## Tip 8: As a pairwise test generator

### Under JUnit execution (recommended)
By creating a test method below, which just prints test case definition, you can use JCUnit as a pairwise (or t-wise) test case generator.

```java

    @Test
    public void printTestCase() {
        System.out.println(TupleUtils.toString(TestCaseUtils.toTestCase(this)));
    }
```

The output will be a text whose lines are JSON objects and look like,

```

    {"browser":"IE","cpuClockInGHz":1.5,"edition":"Home Premium","gramInMB":128,"hddSizeInGB":100,"ramInGB":8}
    {"browser":"IE","cpuClockInGHz":2.5,"edition":"Enterprise","gramInMB":256,"hddSizeInGB":30,"ramInGB":1}
    {"browser":"IE","cpuClockInGHz":3.0,"edition":"Home Basic","gramInMB":512,"hddSizeInGB":50,"ramInGB":2}
    {"browser":"Opera","cpuClockInGHz":1.5,"edition":"Ultimate","gramInMB":512,"hddSizeInGB":20,"ramInGB":1}
    {"browser":"Opera","cpuClockInGHz":2.0,"edition":"Professional","gramInMB":256,"hddSizeInGB":50,"ramInGB":8}
    ...
```

You can refer to a following example for this use case. 
* [TestGen.java](https://github.com/dakusui/jcunit/blob/0.5.x/src/test/java/com/github/dakusui/jcunit/examples/testgen/TestGen.java)

### Without JUnit

Apparently people sometimes want to use JCUnit not as a JUnit runner but as a pairwise (t-wise)
test generator library.

You can do it by doing this.

```java

      public void moreFluentStyleRun(PrintStream ps) {
        TupleGenerator tg = new TupleGenerator.Builder().setFactors(
            new Factors.Builder()
                .add("OS", "Windows", "Linux")
                .add("Browser", "Chrome", "Firefox")
                .add("Bits", "32", "64").build()
        ).build();
        for (Tuple each : tg) {
          ps.println(each);
        }
      }
```

This should print out something like following.

```

    {Bits=32, Browser=Chrome, OS=Windows}
    {Bits=64, Browser=Firefox, OS=Windows}
    {Bits=64, Browser=Chrome, OS=Linux}
    {Bits=32, Browser=Firefox, OS=Linux}
```

You can refer to a following example for this use case. 

* [TestGenWithoutJUnit.java](https://github.com/dakusui/jcunit/blob/0.5.x/src/test/java/com/github/dakusui/jcunit/examples/testgen/TestGenWithoutJUnit.java)

Above mentioned code doesn't use JUnit's functionality at all and from JCUnit 0.5.4 on, 
dependency on JUnit is marked optional in pom.xml of JCUnit. Therefore it should even 
work without having JUnit library in your classpath at all.
But if you are using earlier version of JCUnit, or somehow unwanted version of JUnit 
is picked up, probably you need to 'exclude' the dependency. 
please refer to [Apache Maven Project page](https://maven.apache.org/guides/introduction/introduction-to-optional-and-excludes-dependencies.html)

## Tip 9: Modeling a finite state machine

Probably your SUT might have a specification where users need to follow a certain 
manner like "1. You will initialize the object, 2. Now you can do A or B, 3.
If you did A in step 2, you can do A1, A2, or B1. Otherwise you can only do B1.",
and A, B, etc can take a few parameters respectively.

You might be able to write a topLevelConstraint manager to describe this sort of specification
 but it would be very complicated, boring, and error prone task.
 
JCUnit has a feature called "FSM support" and the detail and how to use it are 
discussed in a separate document. Please refer to [FSM/JCUnit](FSM-README.md) 
