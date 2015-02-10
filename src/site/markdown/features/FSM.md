# Background

FSM is everywhere.
A software system can be considered as an FSM.

Combinatorial explosion.


## Combinatorial tests for FSMs
What are factors and what are levels?
(t.b.d.)


# Design
(t.b.d.)

## Software components and basic ideas
### FSM interface
Defines an FSM to be tested.
Implementation of this interface represents an understanding of the tester (a JCUnit's user)
about the software under test.

In other words, SUT will be modeled as an FSM and JCUnit generates a test suite using it.

It must define the things below

* states the FSM has
* initial state
* actions
* history length

### Scenario
This component is named after an idea in [BDD](http://en.wikipedia.org/wiki/Behavior-driven_development) area.
A scenario consists of three items, which are listed below.

* given
* when
* then

```given``` represents a state on which the scenario should be performed.
```when``` represents an 'action' to be performed.
and ```then``` represents an expectation for the action represented by ```when```.


### Story

history length

(t.b.d.)

### main and setUp stories
(t.b.d.)


### Flat FSM tuple
(t.b.d)
By collecting values in a flat FSM tuple, JCUnit can define a scenario sequence for a test case.

### FSMConstraintManager
(t.b.d.)

## FSM support
### Flat FSM tuple generation
(t.b.d.)

## Scenario Sequence generation
(t.b.d.)

# Extras
## Simple FSM
Implementing FSM interface is a cumbersome task and in order to ease the pain JCUnit provides an easy way to
define an FSM using annotations.

(t.b.d.)

### Annotations
- StateSpec
- ActionSpec
- ParametersSpec

Below is an example.
```java

    @RunWith(JCUnit.class)
    public class FlyingSpaghettiMonsterTest {
      public static enum Spec implements FSMSpec<FlyingSpaghettiMonster> {
        @StateSpec I {
          @Override public boolean check(FlyingSpaghettiMonster flyingSpaghettiMonster) {
            return flyingSpaghettiMonster.isReady();
          }

          @Override public Expectation<FlyingSpaghettiMonster> cook(FSM<FlyingSpaghettiMonster> fsm, String dish, String sauce) {
            Checks.checknotnull(fsm);
            return FSMUtils.valid(fsm, COOKED, CoreMatchers.startsWith("Cooking"));
          }
        },
        @StateSpec COOKED {
          @Override public boolean check(FlyingSpaghettiMonster flyingSpaghettiMonster) {
            return flyingSpaghettiMonster.isReady();
          }

          @Override public Expectation<FlyingSpaghettiMonster> eat(FSM<FlyingSpaghettiMonster> fsm) {
            return FSMUtils.valid(fsm, COOKED, CoreMatchers.containsString("yummy"));
          }

          @Override public Expectation<FlyingSpaghettiMonster> cook(FSM<FlyingSpaghettiMonster> fsm, String dish, String sauce) {
            Checks.checknotnull(fsm);
            return FSMUtils.valid(fsm, COOKED, CoreMatchers.startsWith("Cooking"));
          }
        },;


        @ActionSpec public Expectation<FlyingSpaghettiMonster> cook(FSM<FlyingSpaghettiMonster> fsm, String pasta, String sauce) {
          return FSMUtils.invalid();
        }

        @ParametersSpec public static final Object[][] cook = new Object[][] {
            { "spaghetti", "spaghettini" },
            { "peperoncino", "carbonara", "meat sauce" },
        };

        @ActionSpec public Expectation<FlyingSpaghettiMonster> eat(FSM<FlyingSpaghettiMonster> fsm) {
          return FSMUtils.invalid();
        }
      }

      @FactorField(
          levelsProvider = FSMLevelsProvider.class,
          providerParams = {
              @Param("flyingSpaghettiMonster"),
              @Param("setUp")
          })
      public Story<FlyingSpaghettiMonster> setUp;

      @FactorField(
          levelsProvider = FSMLevelsProvider.class,
          providerParams = {
              @Param("flyingSpaghettiMonster"),
              @Param("main")
          })
      public Story<FlyingSpaghettiMonster> main;

      public FlyingSpaghettiMonster sut = new FlyingSpaghettiMonster();

      public static FSM flyingSpaghettiMonster() {
        return FSMUtils.createFSM(Spec.class);
      }

      @Before
      public void before() throws Throwable {
        FSMUtils.performStory(this.setUp, this.sut, Story.SIMPLE_REPORTER);
      }

      @Test
      public void test() throws Throwable {
        FSMUtils.performStory(this.main, this.sut, Story.SIMPLE_REPORTER);
      }
    }
```

# References
* (t.b.d.) Combinatorial FSM tests, Tsurumaki, Toshiro
* (t.b.d.)
* (t.b.d.) http://en.wikipedia.org/wiki/Mealy_machine

