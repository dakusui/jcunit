# jcunit
JCUnit is a framework to perform combinatorial tests using 'pairwise' technique.

# First test with JCUnit
Below is jcunit's most basic example 'Calc.java'. Gist is also available at https://gist.github.com/dakusui/8980728

Just by running CalcTest.java as a usual JUnit test, JCUnit will automatically generate test cases based on '@In' annotations, and will store the output of Calc based on '@Out' annotation at the first time. 
From then on, in other words from the second run, you will be able to verify if Calc#calc's output is unchanged just by running CalcTest as a JUnit test.

And the values of '@Out' annotated fields are stored in '.jcunit/' under current directory. You can remove it when you want to record new values of your SUT.

## Calc.java example
### Calc.java (Main class, SUT)
Calc is the SUT (Software under test) in this example.
The class provides a function to perform a calculation based on given two numbers and returns the result.

```
package com.github.dakusui.jcunit.tutorial.session01;
 
public class Calc {
	public int calc(int a, int b) {
		return a + b;
	}
}
```

### CalcTest.java (Test)
CalcTest is a test class for Calc class. '@Rule' and '@ClassRule' in this example is kind of boilerplate.

```
package com.github.dakusui.jcunit.tutorial.session01;
 
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
 
import com.github.dakusui.jcunit.core.BasicSummarizer;
import com.github.dakusui.jcunit.core.DefaultRuleSetBuilder;
import com.github.dakusui.jcunit.core.In;
import com.github.dakusui.jcunit.core.JCUnit;
import com.github.dakusui.jcunit.core.Out;
import com.github.dakusui.jcunit.core.RuleSet;
import com.github.dakusui.jcunit.core.Summarizer;
 
@RunWith(JCUnit.class)
public class CalcTest {
	@In
	public int a;
	@In
	public int b;
	@Out
	public int c;
	
	@Rule
	public RuleSet rules = new DefaultRuleSetBuilder().autoRuleSet(this).summarizer(summarizer);
	@ClassRule
	public static Summarizer summarizer = new BasicSummarizer();
	
	@Test
	public void test() {
		this.c = new Calc().calc(this.a, this.b);
	}
}
```

### Maven dependency
Below is a pom.xml fragment to describe jcunit's dependency.

```
    <dependency>
      <groupId>com.github.dakusui</groupId>
      <artifactId>jcunit</artifactId>
      <version>0.1.4</version>
    </dependency>
```

## Tip 1: Customizing domains of @In fields
JCUnit creates combinations based on types. For example, if a memeber is annotated with '@In' and its type is int, jcunit will pick up values from a set {0, -1, 100, -100, 2147483647, -2147483648, 1}.
But this set is just a 'default' and you can customize it by using 'domain' paramter of '@In' annotation and creating a static method whose name is the same as the input field name's.
The method mustn't have any parameters and its return value must be an array of the field's type.

Below is the example for this function.

```
	@In(domain=Domain.Method)
	public int a;
	
	public static int[] a() {
		return new int[]{0, 1, 2};
	}
	
```

The values returned by the method will be picked up and assigned to the field 'a' by the framework one by one.

## Tip 2: Doing pairwise tests or customizing coverage.
Add annotation '@Generator(PairwiseTestArrayGenerator.class)' to CalcTest.java

```
@RunWith(JCUnit.class)
@Generator(PairwiseTestArrayGenerator.class)
public class CalcTest {
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
