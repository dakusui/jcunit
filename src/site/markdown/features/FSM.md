(This documentation is a draft)

* [Motivations](#motivations)
* [Background](#background)
* [Design](#design)
    1. Finite state machine representation
      1. Mealy machine
      2. JCUnit object model
        1. FSM and related objects
        2. ```Story``` and ```Scenario```
    2. Test suite generation
      1. Flat FSM tuple
      2. setUp and main story
      3. Generating constraints
      4. ```SimpleFSM``` mechanism
* [Examples](#examples)
* [References](#references)

<a name="motivations"/>
# Motivations

A finite state machine (FSM) is everywhere in software.
And as discussed in a well known article about [BDD][1],
BDD scenarios can be considered as descriptions of FSM behaviours.

Then, once a software product/system is modeled as FSM, can't we generate a test
suite and execute it automatically? And doesn't it sound useful?

"Model the software under test as FSM and let your computer do the rest" is the goal of this feature.

<a name="background"/>
# Background
If we can model an SUT as a finite state machine, we should simply be able to create
a test suite which consists of N times M test cases, where N is a number of possible
states and M is a number of event types.

Chapter 6 of [Introduction to combinatorial testings][5] discusses how we can design
a test suite for a state machine in detail.

## Path
But these N states and M events are the model of the software under test (SUT) or
we can say that it is just a tester's understanding about SUT. Not the FSM of SUT itself.

Since SUT might have internal, hidden or unknown states, it can behave unexpectedly,
which means you find a bug, only when it experiences a certain state transition history.
In FSM support feature of JCUnit, the transition history (or path) is called a ```Story```.

If we want to test the state transition history exhaustively (all the possible paths on FSM),
the test cases we'll have to execute would be (N*M)^n, where n is the number of the states
the SUT goes through during one ```Story``` is being executed.

Obviously, this can easily be a too big number to finish within a practical amount of time.


(draft)
How can we determine which state the SUT is in?
No, we cannot.
Usually, SUTs do not expose their states directly.
Even if one does, is the state the SUT tells you really correct? Isn't it one thing we must 'test'?
We can only make sure that an SUT is meeting some conditions which should be satisfied in a certain state.

(draft)

## Tsurumaki and Yanagida's method
[Tsurumaki and Yanagida][2] discussed how a combinatorial test suite can be implemented
for an FSM using their open source tool PictMaster and Microsoft's PICT.
The method they presented in the material is like below,

1. For each state (S0, S1, ... Sn-1), generate k factors. So you will have n times k factors ([S(0,0), S(1,0), ..., S(n-1,0)], ..., [S(0,k-1),S(1,k-1),...S(n-1,k-1)]) in total.
2. For each factor , its levels are all possible transitions from the state to which it belongs and special level which represents 'invalid'.
3. Constraints are defined by a user so that only valid transitions can happen.
3. Let pairwise engine (e.g. [PICT][4]) process the factors and their levels and generate a test suite.
4. By tracing the transitions in a test case from S(0,0), you can interpret a generated test case into a sequence of state transitions.

The approach JCUnit took is inspired by this procedure but a different one.

## Spec explorer
(t.b.d.)

<a name="design">
# Design
## Finite state machine representation
### Mealy machine
Unlike an acceptor nor recognizer, a usual software product gives outputs on input which need to be tested.
In order to allow users to test not only state transitions but them, FSM support of JCUnit
provides a way to define a [Mealy machine][3], which is also known as a finite state transducer.

A 6 tuple below defines a Mealy machine.

```
(S, S0, Sigma, Lambda, T, G)
```

They respectively represent the following

1. a finite set of states S
2. a start state (also called initial state) S0 which is an element of S
3. a finite set called the input alphabet Sigma
4. a finite set called the output alphabet Lambda
5. a transition function T : S x Sigma -> S mapping pairs of a state and an input symbol to the corresponding next state.
6. an output function G : S x Sigma -> Lambda mapping pairs of a state and an input symbol to the corresponding output symbol.

### JCUnit's object model
#### FSM and related objects
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

Then the next question would be what State and Action, which should be returned by the implementation.

Below is a class diagram of FSM and its related classes.

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
       +---------------+                      |                        +------------+
                                              |                        |    Args    |
                                              V                        +------------+
                                     +------------------+
                                     | Expectation<SUT> |
                                     +------------------+


```

For the reasons discussed later and to keep the software simple, the items listed above
are not necessarily corresponding to the classes in the diagram directly.

S is represented by `State` and an element in the list returned by `states()` method of `FSM` is S0.
Sigma is represented by `Action` and `Args`.
And Lambda, T, and G are represented by `Expectation`.

#### ```Story``` and ```Scenario```
(t.b.d.)
```Scenario``` component is named after an idea in [BDD](http://en.wikipedia.org/wiki/Behavior-driven_development) area.
A scenario consists of three items, which are listed below.

* given
* when
* then

```given``` represents a state on which the scenario should be performed.
```when``` represents an 'action' to be performed.
and ```then``` represents an expectation for the action represented by ```when```.

A ```Story``` is a sequence of ```Scenario```s.

    |abc  |def |
    | --- | --- |



## Test suite generation
(t.b.d.)
### Generating constraints
(t.b.d.)

### Flat FSM tuple
By collecting values in a flat FSM tuple, JCUnit can define a scenario sequence for a test case.
(t.b.d.)
### setUp and main story
(t.b.d.)


### ```SimpleFSM``` mechanism
(t.b.d.)

Implementing FSM interface is a cumbersome task and in order to ease the pain JCUnit provides an easy way to
define an FSM using annotations.

* Nesting state machines

What you cannot do with ```SimpleFSM```
* You cannot test overloaded methods, which have same names and different parameter sets.
* Negative tests

(t.b.d.)

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

```FlyingSpaghettiMonster```'s State machine

```

          +---+eat / ERR         +---+eat / "yummy"
          |   |                  |   |
          |   V                  |   V
       +---------+            +----------+
       |    I    |----------->|  COOKED  |
       +---------+    cook    +----------+
                                 A   |
                                 |   |
                                 +---+cook
```


#### Annotations
- StateSpec
- ActionSpec
- ParametersSpec

<a name="references"/>
# References
* [1] "The Truth about BDD(Uncle Bob)"
* [2] "Developing an open source combinatorial testing tool(Toshiro Tsurumaki and Yukihide Yanagida)"
* [3] "Wikipedia article about Mealy machine"
* [4] "Pairwise Testing & PICT Tool"
* [5] "Introduction to Combinatorial Testing" (Kuhn et al)

[1]: https://sites.google.com/site/unclebobconsultingllc/the-truth-about-bdd
[2]: http://www.jasst.jp/archives/jasst09e/pdf/D4-2.pdf
[3]: http://en.wikipedia.org/wiki/Mealy_machine
[4]: http://blogs.msdn.com/b/nagasatish/archive/2006/11/30/pairwise-testing-pict-tool.aspx
[5]: http://kobo.rakuten.com/