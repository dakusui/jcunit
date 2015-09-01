# JCUnit FSM testing model
## 'Modified' Mealy machines
* In real world output matters
* Input symbols have parameters
* Why using modified Mealy machines in JCUnit?
  discussed in detail about the modification.
## JCUnit's FSM Factor model
factors and their levels
* states
* symbols (methods and parameters)
* path-length
* setup (routing) mechanism for paths which do not start with ```I```.
----

### Modified Mealy Machine
JCUnit's FSM support uses a 'modified' Mealy Machine as a basic model of SUT.
Mealy Machine is also known as a finite state transducer. [Wikipedia article](http://en.wikipedia.org/wiki/Mealy_machine)

We can consider a software system as a finite state machine, but in testing, outputs of it matter
and we must verify them.
So, simple FSM model, which doesn't have outputs is not enough powerful to describe the
specification of the system under test.

Since a Mealy machine has an output when a transition happens,
if we regard it an object, we can also regard the input alphabets Sigma as a set
of methods of the object.

But methods (or scenarios performed on a system) usually have some input parameters
and unless we have a way to define those parameters in the model, we cannot describe
an object to be tested.

The finite state machine discussed in this document is a bit different from a usual
Mealy machine. Each of its input alphabets are identified by a tuple of an action, which
 corresponds to a method name, and a list of arguments given to the method.

### FSM factors, their levels, and constraints
JCUnit's FSM support functionality maps states, actions, and parameters of actions
to factors and their levels in a manner described in this section.
Below is a state machine diagram of a sample FSM, ```monster```, and how elements
in an FSM are mapped to factors/levels will be discussed in this section using
this example.

```

    +-----+ cook(a1,a2) +--------+
    |     |------------>|        |-----+
    |  I  |             | Cooked |     |eat(b)
    |     |<------------|        |<----+
    +-----+   done      +--------+

```

Suppose that ```cook```'s parameter ```a1``` accepts arguments ```spaghetti``` and ```soup```,
```a2``` accepts ```tabasco``` and ```tomato```, and ```eat```'s parameter ```b``` only accepts ```spaghetti```.
Those are chosen by users as factor levels to be used in a test suite.

In this example, if ```historyLength``` is set to 1, the factors and levels below are automatically generated. (i = 0)


|FSM:monster:state:i|FSM:monster:action:i |FSM:monster:param:i:0|FSM:monster:param:i:1|
|:----------------- |:------------------- |:------------------- |:------------------- |
|```Si```           |```Ai```             |```p[i,0]```         |```p[i,1]```         |
|1. I,Cooked        |2. cook,eat,done     |3. spaghetti,soup    |4. tabasco,tomato    |

1. Levels for ```FSM:monster:state:0``` are all possible states, i.e., ```I``` and ```Cooked```.
2. Levels for ```FSM:monster:action:0``` are all possible actions, i.e., ```cook```, ```eat```, and ```done```
3. and 4. Levels for ```FSM:monster:parameter:0:j``` are a union of all possible arguments as the ```j```'th parameter of any action.

If ```historyLength``` is set to ```n``` ( > 1), this set of factors are generated for ```i``` from ```0``` to ```n``` - 1.


|FSM:monster:state:i|FSM:monster:action:i |FSM:monster:param:i:0|FSM:monster:param:i:1|FSM:monster:state:i+1| ... |
|:----------------- |:------------------- |:------------------- |:------------------- |:------------------- | --- |
|```Si```           |```Ai```             |```p[i,0]```         |```p[i,1]```         |```Si+1```           | ... |

This tuple defines  a partial path history on the FSM.

As described in the previous table, the first element in this partial path history can be any state, not only ```I```, which
is the initial one.
In order to execute a path history which starts with non-initial state, we need to make the FSM reach it.
The procedure for this is described in the sub-section "Set up procedure".

Once states, actions, and parameters are mapped to factors and their levels, by defining appropriate constraints, we
can finish modeling the FSM.

Let

1. ```actions(S)``` be a set of all possible actions that can be performed on state ```S```.
2. ```args(A, j)``` be a set of all possible ```j```'s argument value of an action ```A```.
3. ```state(S, A, p[x,0],p[x,1],...,p[x,m-1])``` be a state to which the machine moves from
 state ```S``` if action ```A``` is performed with arguments ```p[x,0],p[x,1],...,p[x,m]```.
 (```m``` is number of parameters of ```A```)

The constraints that define the FSM are like below,

If and only if all of following are satisfied for any ```i``` from 0 to ```historyLength``` - 1, the tuple is valid.

* ```Ai``` exists in ```actions(Si)```
* ```p[i,j]``` exists in ```args(A, j)``` for any ```j```
* if ```i``` < ```historyLength``` - 1, then ```Si+1``` == ```state(Si, A, p[i,0],p[i,1],...,p[i,m-1])```

This constraint set can be built using constraint functions of combinatorial test suite generator like PICT or JCUnit.

#### Set up procedure
As discussed already, partial path histories (test cases) JCUnit's FSM support creates can start with non-initial state.
In order to execute those test cases, JCUnit needs to make the SUT's state to the one from which the partial path starts.

This searches for the paths to reach each state at first with width-first algorithm and remember them at the beginning
of test generation.

And the path to reach the initial state will be set to each test case automatically.

If JCUnit cannot find a path reach the initial state, the partial path history starts with a non-reachable state.
In this case, JCUnit should simply discard the test case.


#### Coverage
If we can find any possible value assignments for ```Si```, ```Ai```, and ```p[i,j]```,
we can say that

_We have covered all sub-paths of ```historyLength```_

Of course this grows exponentially along the ```historyLength```.
But if we perform combinatorial test generation technique mentioned above using
these states, actions, arguments, and constraints, the number of test case should
grow much more slowly.
And about coverage, we can still state that

_We have covered all ```t```-wise transition combinations in ```historyLength```_

where ```t``` is a parameter to be given to combinatorial test generation engine as strength (2 for pairwise).

#### About guard conditions
If you want to handle 'guard conditions' of UML-like state machines, you can implement
a set of constraints to define them.

But this isn't a way in which JCUnit is intended to be used originally.
There are some confusing behaviour and please use this technique with caution as of now.

First, if the action ```cook``` has a guard condition in a diagram like below,

```
Fig. a

    +-----+ cook(a1<a2) +--------+
    |     |------------>|        |-----+
    |  I  |             | Cooked |     |eat(b)
    |     |<------------|        |<----+
    +-----+   done      +--------+

```

This guard condition 'a1 < a2' can be implemented as a constraint below

* If ```Si``` is ```I```, ```Ai``` is ```cook```, and ```p[i,0]``` < ```p[i,1]```, then ```Si+1``` is ```Cooked```

By using this with a set of basic constraints above mentioned, you can implement
the guard condition in Fig. a.
Combinatorial test case generation tools including PICT and JCUnit try to find such test cases.
As a result, test cases that satisfy the guard condition in the diagram will be found by the tools.

Similarly, we can model the guard conditions below by defining constraints below,

* If ```Si``` is ```I```, ```Ai``` is ```cook```, and ```p[i,0]``` < ```p[i,1]``` then ```Si+1``` is ```Cooked```
* If ```Si``` is ```I```, ```Ai``` is ```cook```, and ```p[i,0]``` >= ```p[i,1]``` then ```Si+1``` is ```I```

```
Fig. b

     cook(a1>=a2)
     +---+
     |   |
     |   V
    +-----+ cook(a1<a2) +--------+
    |     |------------>|        |-----+
    |  I  |             | Cooked |     |eat(b)
    |     |<------------|        |<----+
    +-----+   done      +--------+

```

The first thing to be careful is finding assignments to variables that satisfy a
certain condition that uses the variables is a problem called SAT, which is known
to be NP complete.

Trying to solve NP complete problem isn't a pragmatic effort and we need to find some approximate solution.

What JCUnit does for this is below,

* It tries to cover a certain value pair by changing other attribute values,
how these attributes are chosen is discussed in [IPO.md(t.b.d)]
* If it finds out tuples that satisfy the constraints, it chooses one which covers new value pairs the most.
* If it cannot find out any tuple after N tries, it gives up the value pair and mark it as impossible one.

By this mechanism JCUnit can generate test suite which executes ```cook(a1<a2)``` and ```cook(a1>=a2)```.
Because it tries to cover value pairs (S0, S1) = (```I```, ```Cooked```) and (```I```, ```I```).
But it is because there is only one action that is coming from ```I``` and going to ```Cooked``` (or ```I```).
If there is yet another arrow from ```I``` to ```Cooked``` and its name is ```cook```,

```
Fig. c

    +-----+ cook(cond1(a1,a2,a3)) +--------+
    |     |---------------------->|        |-----+
    |     |                       |        |     |
    |     | cook(cond2(a1,a2,a3)) |        |     |
    |     |---------------------->|        |     |
    |  I  |                       | Cooked |     |eat(b)
    |     |<----------------------|        |<----+
    +-----+   done                +--------+

```

Suppose cond1 and cond 2 are functions each of which returns a boolean value and they can't become true
at once. ```cook``` will be executed when cond1 or 2 becomes true.

JCUnit tries to cover the value pairs

* (S0,S1) = (```I```,```I```),(```I```,```Cooked```),(```Cooked```,```I```),(```Cooked```,```Cooked```)
* (S0,A0) = (```I```,```cook```),(```Cooked```,```eat```),(```Cooked```,```done```)
* (A0,S1) = (```cook```,```Cooked```),(```eat```,```Cooked```),(```done```,```I```)
* (A0,p[0,0]) = (```cook```, ```spaghetti```),...
* (A0,p[0,1]) = (```cook```, ```tomato```),...
...
* (p[0,0],p[1,0]) = (```spaghetti```, ```tomato```),...

Once it covers a pair (S0, A0) = (```I```, ```cook```), it does not try to cover both ```cook(cond1 = true)```,
```cook(cond2 = true)```. It just tries to cover all possible pairs and cond1/2 have 3 parameters.
So, it can miss the combinations which make cond2 false.

----
How can we determine which state the SUT is in?
No, we cannot.
Usually, SUTs do not expose their states directly.
Even if one does, is the state the SUT tells you really correct? Isn't it one thing we must 'test'?
We can only make sure that an SUT is meeting some conditions which should be satisfied in a certain state.


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

#### ```Story```, ```ScenarioSequence```, and ```Scenario```
(t.b.d.)
```Scenario``` component is named after an idea in [BDD](http://en.wikipedia.org/wiki/Behavior-driven_development) area.
A scenario consists of three items, which are listed below.

* given
* when
* then

```given``` represents a state on which the scenario should be performed.
```when``` represents an 'action' to be performed.
and ```then``` represents an expectation for the action represented by ```when```.

A ```ScenarioSequence``` is a sequence of ```Scenario```s.

### FSM factors, their levels, and constraints
(t.b.d.)

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