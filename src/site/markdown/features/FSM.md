# Motivations

A finite state machine (FSM) is everywhere in software.
And as discussed in a well known article about [BDD][1],
a software system can be considered as an FSM in some sense.

Then, in order to test a software product/system, the most important thing how we model it as an FSM.
Once it is modeled, can't we generate a test suite and run it automatically?

"Model the software under test and let the computer do the rest" is the goal of this feature.


# Background
What type of FSM?
Mealy machine


we can simply create a test suite
consists of N times M test cases, where N is a number of possible states and M is
number of events, if we can model it as an FSM.

But these N states and M events are just the model of the software under test (SUT).
and SUT might have hidden states and it can behave unexpectedly when it experiences a
certain state transition history.
If we want to test the state transition history exhaustively, the test cases we'll
have to execute would be (N*M)^n, where n is the number of the states the SUT goes
through during one test case is being executed.

Reducing the number of test cases by applying combinatorial testing technique is
the first motivation of JCUnit's FSM support.

# Design
The 6 tuple below defines a [Mealy machine][3].

```
(S, S0, Sigma, Lambda, T, G)
```

They respectively represent the following

1. a finite set of states S
2. a start state (also called initial state) S0 which is an element of S
3. a finite set called the input alphabet Sigma
4. a finite set called the output alphabet Lambda
5. a transition function T : S x Sbigma -> S mapping pairs of a state and an input symbol to the corresponding next state.
6. an output function G : S x Sigma -> Lambda mapping pairs of a state and an input symbol to the corresponding output symbol.

To model a Mealy machine in JCUnit, you can implement an interface 'FSM.java' below.

```java

    /**
     * An interface that represents a finite state machine's (FSM) specification.
     *
     * @param <SUT> A software under test.
     */
    public interface FSM<SUT> {
      State<SUT> initialState();

      List<State<SUT>> states();

      List<Action<SUT>> actions();

      int historyLength();
    }
```

By creating an implementation of this interface, a user can model the SUT and pass
 it to JCUnit to let it generate a test suite and execute the suite.

```

                +-----------------------------+------------------------------+
                |                             |                              |
                |1                            V*                             V*
       +---------------+       +-----------------------------+      +-----------------+
       |    FSM<SUT>   |       |          State<SUT>         |      |   Action<SUT>   |
       +---------------+       +-----------------------------+      +-----------------+
       |initialState() |       |expectation(Action<SUT>,Args)|      |perform(SUT,Args)|
       |states()       |       +-----------------------------+      +-----------------+
       |historyLength()|                      |
       +---------------+                      |
                                              |
                                              V
                                     +------------------+
                                     | Expectation<SUT> |
                                     +------------------+


```

(t.b.d.)

[1] discusses how an FSM can be modeled in combinatorial testing.

## Combinatorial tests for FSMs
What are factors and what are levels?
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
[1]: https://sites.google.com/site/unclebobconsultingllc/the-truth-about-bdd "The Truth about BDD(Uncle Bob)"
[2]: http://www.jasst.jp/archives/jasst09e/pdf/D4-2.pdf "Developing an open source combinatorial testing tool(Toshiro Tsurumaki and Yukihide Yanagida)"
[3]: http://en.wikipedia.org/wiki/Mealy_machine "Wikipedia article about Mealy machine"

