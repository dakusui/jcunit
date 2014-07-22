# jcunit
JCUnit is a framework to perform combinatorial tests using 'pairwise' technique.

# First test with JCUnit
Below is jcunit's most basic example 'QuadraticEquationSolver.java'. Gist is also available at https://gist.github.com/dakusui/XYZ
Just by running QuadraticEquationSolverTest.java as a usual JUnit test, JCUnit will automatically generate test cases based on '@FactorLevels' annotations.

## QuadraticEquationSolver.java example
### QuadraticEquationSolver.java (Main class, SUT)
QuadraticEquationSolver is the SUT (Software under test) in this example.
The class provides a function to solve a quadratic equation using a quadratic formula and returns the solutions.

```
package com.github.dakusui.jcunit.framework.examples.quadraticequation.session1;

public class QuadraticEquationSolver {
  private final double a;
  private final double b;
  private final double c;

  public static class Solutions {
    public final double x1;
    public final double x2;

    public Solutions(double x1, double x2) {
      this.x1 = x1;
      this.x2 = x2;
    }

    public String toString() {
      return String.format("(%f,%f)", x1, x2);
    }
  }

  public QuadraticEquationSolver(double a, double b, double c) {
    this.a = a;
    this.b = b;
    this.c = c;
  }

  public Solutions solve() {
    return new Solutions(
        (-b + Math.sqrt(b * b - 4 * c * a)) / (2 * a),
        (-b - Math.sqrt(b * b - 4 * c * a)) / (2 * a)
    );
  }
}
```

### QuadraticEquationSolverTest.java (Test)
QuadraticEquationSolverTest is a test class for QuadraticEquationSolver class.

```
package com.github.dakusui.jcunit.framework.examples.quadraticequation.session1;

import com.github.dakusui.jcunit.core.JCUnit;
import com.github.dakusui.jcunit.core.factor.FactorField;
import com.github.dakusui.jcunit.framework.examples.quadraticequation.session1.QuadraticEquationSolver;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;

@RunWith(JCUnit.class)
public class QuadraticEquationSolverTest1 {
	@FactorField
	public int a;
	@FactorField
	public int b;
	@FactorField
	public int c;

	@Test
	public void test() {
		QuadraticEquationSolver.Solutions s = new QuadraticEquationSolver(a, b,
				c).solve();
		assertEquals(0.0, a * s.x1 * s.x1 + b * s.x1 + c);
		assertEquals(0.0, a * s.x2 * s.x2 + b * s.x2 + c);
	}
}
```

### Maven dependency
Below is a pom.xml fragment to describe jcunit's dependency.

```
    <dependency>
      <groupId>com.github.dakusui</groupId>
      <artifactId>jcunit</artifactId>
      <version>[0.3.0,]</version>
    </dependency>
```

## Tip 1: Customizing domains of @FactorField annotated fields
JCUnit creates combinations from values defined for each type by default.
For example, if a member is annotated with '@FactorField' and its type is int, jcunit will pick up values from a set
{1, 0, -1, 100, -100, Integer.MAX_VALUE, Integer.MIN_VALUE}.
But this set is just a 'default' and you can customize it by using 'levelsFactory' parameter of '@FactorField'
annotation and creating a static method whose name is the same as the field name's.
The method mustn't have any parameters and its return value must be an array of the field's type.

Below is the example for that sort of function.

```
    @FactorField(levelsFactory = MethodLevelsFactory.class)
	public int a;
	
	public static int[] a() {
		return new int[]{0, 1, 2};
	}
	
```

The values returned by the method will be picked up and assigned to the field 'a' by the framework one by one.

## Tip 2: Customizing the strength of all pair test's coverage.
Add annotation '@Generator(PairwiseTestArrayGenerator.class)' to CalcTest.java

```
@RunWith(JCUnit.class)
@TestCaseGeneration(
		generator = @Generator(
				value = IPO2TestCaseGenerator.class,
				params = {
						@Param(type = Param.Type.Int, array = false, value = {"3"})
				}))
public class QuadraticEquationSolverTest1 {
```

Since there are only two parameters in this test class now (members a and b only), the test will automatically be exhaustive.

You can also specify CartesianTestArrayGenerator, which does exhausitive combinatorial tests always and takes long (sometimes too long and not practical), or SimpleTestArrayGenerator, which is default and has less coverage than Pairwise.


# Copyright and license #

Copyright 2013 Hiroshi Ukai.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this work except in compliance with the License.
You may obtain a copy of the License in the LICENSE file, or at:

  [http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
