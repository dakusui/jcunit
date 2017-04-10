# Preface: Renewal of JCUnit
The last month (Mar/2017) I attended and made a presentation about JCUnit at ICST 2017
(IEEE Conference on Software Testing 2017) and got new findings and feedbcks
in it. It was an exciting experience for me and I realized that it's time
to re-design JCUnit now based on new ideas.

Followings are new features that will be introduced to JCUnit in the next
version.

* Annotation Renewal
* Pipeline Renewal
    - New pipeline design
    - API
* New covering array engine
    - "IPO-G"

Also it will no longer be working with old Java SE6 and require Java 8.
The new version is called "JUnit 8" named after both Java 8 and the next
major version of JCUnit, 0.8.x.

Implementation of the features have almost finished already and I'm going
to walk through the features one by one in this post.

# Annotation Renewal

In JCUnit8, its annotation system was completely renewed. You can find an
example of the new annotation style [here](/src/java/test/com/github/dakusui/jcunit8/examples/BankAccountExample.java).

These days, people more and more tend to prefer ['@Theories' style](http://junit.org/junit4/javadoc/4.12/org/junit/experimental/theories/Theories.html),
where each test method takes parameters to each of whose actual value is
assigned by a test runner, over conventional JUnit style. Newly introduced
annotation system takes the approach.

Followings are the annotations used in the new style.

* Defining a test class
    * ```@Runwith(JCUnit8.class)```
    * ```@ConfigureWith```
* Defining a test method
    * ```@Test```
    * ```@Given```
    * ```@From```
* Defining a test space
    * ```@ParameterSource```
    * ```@Condition```

These will be explained in this section.

## Defining a test class
The new test runner is called **JCUnit8** (```com.github.dakusui.jcunit8.runners.junit4.JCUnit8```).
When you use the runner, a configuration factory for the test class
needs to be specified and you can do it with another annotation ```@ConfigureWith```.


```java

    @RunWith(JCUnit8.class)
    @ConfigureWith(BankAccountExample.BankAccountConfigFactory.class)
    public class BankAccountExample {
        ...

```


## Defining a test method

Methods annotated with ```@Test``` are considered to be test methods, but
unlike conventional JUnit test methods, they can take parameters. But each of
those parameters must be annotated with ```@From```, which specifies how
actual argument values of the parameter should be supplied.


```java

      @Test
      @Given("overdraftNotHappens")
      public void whenPerformScenario$thenBalanceIsCorrect(
          @From("scenario") List<String> scenario,
          @From("depositAmount") int amountOfDeposit,
          @From("withdrawAmount") int amountOfWithdraw,
          @From("transferAmount") int amountOfTransfer
      ) {
          ...
      }

```


```@From``` annotations specify a name of method defined from which actual parameter
values should be generated. Those methods must be defined in a class specified by
```@ConfigureWith``` annotation as mentioned in the chapter "Defining a test class".

### Associating test cases and test oracles

Expected behaviours of test cases can be different for inputs of them. E.g.,
when a sequence of bank account operations is executed, the expected outcome
will be different depending on whether an overdraft happens or not.

In JCUnit8, this can be expressed by using ```@Given``` annotation.

A ```@Given``` annotation specifies a condition on which this test method
should be executed. In the example above, the test method ```whenPerformScenario$thenBalanceIsCorrect```
will be invoked when (and only when) a method ```overdraftNotHappens``` defined
in the class specified by ```@ConfigureWith``` annotation returns ```true```.

Currently JCUnit allows you to create composite conditions using three
operators, AND, OR, and NOT from simple ones.

If you want to AND multiple conditions, you can do it by following.

```java

      @Test
      @Given("condition1&&condition2")
      public void aTestMethod(...) {
          ...
```

To OR conditions,

```java

      @Test
      @Given({"condition1", "condition2"})
      public void aTestMethod(...) {
          ...
```

And to negate a condition, you can do

```java
      @Test
      @Given({"!condition1", "!condition2&&condition3"})
      public void aTestMethod(...) {
          ...
```


```java

    @RunWith(JCUnit8.class)
    @ConfigureWith(BankAccountExample.BankAccountConfigFactory.class)
    public class BankAccountExample {
      public static class BankAccountConfigFactory extends ConfigFactory.Default {
        @ParameterSource
        public static Regex.Factory<String> scenario() {
          return Regex.Factory.of("open deposit(deposit|withdraw|transfer){0,3}getBalance");
        }

        @ParameterSource
        public static Simple.Factory<Integer> depositAmount() {
          return Simple.Factory.of(asList(100, 200, 300, 400, 500, 600, -1));
        }

        @ParameterSource
        public static Simple.Factory<Integer> withdrawAmount() {
          return Simple.Factory.of(asList(100, 200, 300, 400, 500, 600, - 1));
        }

        @ParameterSource
        public static Simple.Factory<Integer> transferAmount() {
          return Simple.Factory.of(asList(100, 200, 300, 400, 500, 600, -1));
        }

        @Condition(constraint = true)
        public static boolean depositUsed(
            @From("scenario") List<String> scenario,
            @From("depositAmount") int amount
        ) {
          //noinspection SimplifiableIfStatement
          if (!scenario.contains("deposit")) {
            return amount == -1;
          } else {
            return amount != -1;
          }
        }

        @Condition(constraint = true)
        public static boolean withdrawUsed(
            @From("scenario") List<String> scenario,
            @From("withdrawAmount") int amount
        ) {
          //noinspection SimplifiableIfStatement
          if (!scenario.contains("withdraw")) {
            return amount == -1;
          } else {
            return amount != -1;
          }
        }

        @Condition(constraint = true)
        public static boolean transferUsed(
            @From("scenario") List<String> scenario,
            @From("transferAmount") int amount
        ) {
          //noinspection SimplifiableIfStatement
          if (!scenario.contains("transfer")) {
            return amount == -1;
          } else {
            return amount != -1;
          }
        }

        @Condition(constraint = true)
        public static boolean overdraftNotHappens(
            @From("scenario") List<String> scenario,
            @From("depositAmount") int amountOfDeposit,
            @From("withdrawAmount") int amountOfWithdraw,
            @From("transferAmount") int amountOfTransfer
        ) {
          return calculateBalance(scenario, amountOfDeposit, amountOfWithdraw, amountOfTransfer) >= 0;
        }

        static int calculateBalance(List<String> scenario,
            int amountOfDeposit,
            int amountOfWithdraw,
            int amountOfTransfer) {
          int balance = 0;
          for (String op : scenario) {
            if ("deposit".equals(op)) {
              balance += amountOfDeposit;
            } else if ("withdraw".equals(op)) {
              balance -= amountOfWithdraw;
            } else if ("transfer".equals(op)) {
              balance -= amountOfTransfer;
            }
            if (balance < 0) {
              return balance;
            }
          }
          return balance;
        }
      }

      private BankAccount myAccount;
      private BankAccount anotherAccount = BankAccount.open();


      @Test
      @Given("overdraftNotHappens")
      public void performScenario(
          @From("scenario") List<String> scenario,
          @From("depositAmount") int amountOfDeposit,
          @From("withdrawAmount") int amountOfWithdraw,
          @From("transferAmount") int amountOfTransfer
      ) {
        int balance = -1;
        for (String operation : scenario) {
          balance = perform(operation, amountOfDeposit, amountOfWithdraw, amountOfTransfer);
        }
        assertEquals(calculateBalance(scenario, amountOfDeposit, amountOfWithdraw, amountOfTransfer), balance);
      }

```

# Pipeline Renewal
(t.b.d.)
## New pipeline design
<img src="src/site/docs/ThePipeline/Slide1.jpg" alt="Overview" style="width: 800px;"/>

<img src="src/site/docs/ThePipeline/Slide2.jpg" alt="Engine" style="width: 800px;"/>
(t.b.d.)

[]
## New covering array engine: "IPO-G+"
(t.b.d.)

# New capabilities
* Constraints that involve non-simple parameters
* Better constriant handlings by "IPO-G+"

# TODOs
* Validations
* Default value of '@ConfigureWith' annotation: If the test class is implementing
  ```Config.Factory``` interface, it might be good idea to use it as a value for
  '@ConfigureWith' annotation when it is absent.
* IPO-G+ performance improvements

# References
(t.b.d.)