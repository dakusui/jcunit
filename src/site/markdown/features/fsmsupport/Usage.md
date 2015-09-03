# How to use JCUnit
## Simple FSM
* Sample FSM Diagram
* Java sample code (SUT)
* Java sample code (JCUnit)
  Testing a state
  Define a field (enum member) for the state
  ```@StateSpec``` annotation
  Define a ```check``` method
  Testing an action
  ```@ActionSpec``` annotation


## Nested FSMs
In real world, things can happen repeatedly.
Recursive (nested) model is necessary.

* Sample FSM Diagram
* Java sample code (SUT)
* Java sample code (JCUnit)
* Java sample code with 'FSMTestBase'

# Writing a test suite

Defining a specification of SUT.


## Defining states
To define states in FSM, the first step would be

```java

    public enum Spec implements FSMSpec<FlyingSpaghettiMonster> {
      @StateSpec I {
        @Override
        public boolean check(FlyingSpaghettiMonster flyingSpaghettiMonster) {
          boolean result= ...;
          return result;
        }
      },
      @StateSpec COOKED
    }

```

Actually, you do not need to use ```enum``` to define a FSM spec, but just use normal class like this.
 
```java

    public class Spec implements FSMSpec<FlyingSpaghettiMonster> {
      @StateSpec public static final Spec I = new Spec() {},
      @StateSpec public static final Spec COOKED = new Spec() {}
    }


```

## Defining actions

```java

      @ActionSpec
      public Expectation<FlyingSpaghettiMonster> cook(FSM<FlyingSpaghettiMonster> fsm, String pasta, String sauce) {
        return FSMUtils.invalid();
      }
  
      @ActionSpec
      public Expectation<FlyingSpaghettiMonster> eat(FSM<FlyingSpaghettiMonster> fsm) {
        return FSMUtils.invalid();
      }

```

  /**
   * Fields annotated with {@code StateSpec} will be considered states of the FSM.
   * And they must be public, static, final fields, and typed by an enclosing class.
   * Otherwise errors will be reported by JCUnit framework.
   * In this example, {@code Spec} is an enclosing class of {@code I} and {@code COOKED}
   * and they are typed with {@code Spec} because it is a Java {@code enum}, whose
   * members are typed with it.
   * <p/>
   * Methods annotated with {@code ActionSpec} will be considered actions of the FSM.
   * And they must be public, returning {@code Expectation<SUT>}, taking arguments
   * which define the signature of the methods to be tested in the SUT.
   */
   
```java

    public enum Spec implements FSMSpec<FlyingSpaghettiMonster> {
       @StateSpec I {
        @Override
        public boolean check(FlyingSpaghettiMonster flyingSpaghettiMonster) {
          return flyingSpaghettiMonster.isReady();
        }
  
        @Override
        public Expectation<FlyingSpaghettiMonster> cook(FSM<FlyingSpaghettiMonster> fsm, String dish, String sauce) {
          Checks.checknotnull(fsm);
          return FSMUtils.valid(fsm, COOKED, CoreMatchers.startsWith("Cooking"));
        }
      },
      @StateSpec COOKED {
        @Override
        public boolean check(FlyingSpaghettiMonster flyingSpaghettiMonster) {
          return flyingSpaghettiMonster.isReady();
        }
  
        @Override
        public Expectation<FlyingSpaghettiMonster> eat(FSM<FlyingSpaghettiMonster> fsm) {
          return FSMUtils.valid(fsm, COOKED, CoreMatchers.containsString("yummy"));
        }
  
        @Override
        public Expectation<FlyingSpaghettiMonster> cook(FSM<FlyingSpaghettiMonster> fsm, String dish, String sauce) {
          Checks.checknotnull(fsm);
          return FSMUtils.valid(fsm, COOKED, CoreMatchers.startsWith("Cooking"));
        }
      },;
  
  
      @ParametersSpec
      public static final Object[][] cook = new Object[][] {
          { "spaghetti", "spaghettini" },
          { "peperoncino", "carbonara", "meat sauce" },
      };
  
      @ActionSpec
      public Expectation<FlyingSpaghettiMonster> cook(FSM<FlyingSpaghettiMonster> fsm, String pasta, String sauce) {
        return FSMUtils.invalid();
      }
  
      @ActionSpec
      public Expectation<FlyingSpaghettiMonster> eat(FSM<FlyingSpaghettiMonster> fsm) {
        return FSMUtils.invalid();
      }
    }
```
