# Let's model your FSM.
If you are creating a finite state machine which has two actions, 'cook' and 'eat',
and only after 'cook' is done, the machine can 'eat' a thing, a state transition
diagram for it would be like following.

(Figure 1. Simplest finite state machine)
```
                               
                                +--+
                                |  |eat 
                                |  V 
    +-----+                   +------+
    |  I  |------------------>|COOKED|
    +-----+       cook        +------+


```

The machine has 2 states, which are 'I' and 'COOKED'. 'I' is the initial state in which
the machine is right after its creation. And 'COOKED' is the state to which it
moves after an action 'cook' is performed.

If you are going to implement this state machine as a Java program, it might become like this,

(Code 1. Implementation of Figure 1 - with a bug.)
```java
    
    public class FSMonster {
        boolean cooked = false;
        public void cook() {
            // Don't we need to check the value of cooked before assigning?
            this.cooked = true;
        }
       
        public void eat() {
            if (this.cooked) {
                System.out.println("Yummy!");
            } else {
                throw new IllegalStateException("Not yet cooked!");
            }
        }
    }

```

What should happen if 'cook' is attempted when the machine is already in 'COOKED' state?
Unless explicitly described in the state machine diagram, shouldn't it be rejected?

Yes, it should be rejected. This is an intentional bug for explanation of "FSM support
feature". This bug itself might be easy to be found, but if you have some experience in
software developments, finding/debugging this sort of bugs is sometimes a time consuming,
cumbersome, boring task.

How to detect this sort of bugs in your SUT using "FSM feature" of JCUnit will be
discussed in this document.


## Listing states and actions
Let's go back to the diagram (Fig.1), as we already saw, there are 2 states and 2 actions,

* States: 'I', 'COOKED'
* Actions(Input symbols): 'cook', 'eat'

In JCUnit, to model a finite state machine, you need to implement 'FSMSpec<SUT>'
interface. 'SUT' is a class name of your software under test.
And inside your implementation, you will use a few annotations, @StateSpec, @ActionSpec,
and @ParameterSpec. Only first two will be necessary to model the machine and @ParameterSpec
will be discussed later to model an action with parameters.

'public static final' fields annotated with '@StateSpec' will be treated as states
by JCUnit.
Among those fields, a field named 'I' (capital I) has a special semantics, where
it is considered 'initial state' of the finite state machine. And you must define
it always.

Methods annotated with '@ActionSpec' are treated as definitions of actions.
They must return 'Expectation<SUT>' and their first parameter must be 'Expectation.Builder<SUT>' always.
JCUnit validates the types and complain of it if they do not meet the requirements.

Following is a skeleton of the spec of the FSM.


(Code 2. Model in JCUnit of Figure 1 - skeleton)
```java

    public enum Spec implements FSMSpec<FSMonster> {
      @StateSpec I {
        ... },
      @StateSpec COOKED {
        ... },; 

      @ActionSpec public Expectation<FSMonster> cook(Expectation.Builder<FSMonster> b) {
        ... }
      @ActionSpec public Expectation<FSMonster> eat(Expectation.Builder<FSMonster> b) {
        ... }
    }

```

## Modeling states and actions
### Modeling states
First, FSMSpec<SUT> interface requires you to implement 'check(SUT): boolean' method.
The method is responsible for checking if the SUT is in the specified state.
Suppose that 'FSMonster' has a method 'isReady()', which returns true iff it's
in 'COOKED' state, you can do following

```java

    public enum Spec implements FSMSpec<FSMonster> {
      @StateSpec I {
        @Override public boolean check(FlyingSpaghettiMonster fsm) {
          return !fsm.isReady();
        }
      },
      @StateSpec COOKED {
        @Override public boolean check(FlyingSpaghettiMonster fsm) {
          return fsm.isReady();
        }
      },;
      ...
    }

```

If there is not an easy (and safe) way to check it, simply you can return 'true'
always like following.

```java

    public enum Spec implements FSMSpec<FSMonster> {
      ...
      public boolean check(FSMonster fsm) {
          return true.
      }
    }

```

Without making sure the SUT is in expected state, should we really return OK?
Yes, it's inevitable.

If the SUT has 'getState()', it would be good to write something like following,

```java

    @StateSpec I {
      @Override public boolean check(FlyingSpaghettiMonster fsm) {
        return "Initial".equals(fsm.getState().getName());
      }
    },
    @StateSpec COOKED {
      @Override public boolean check(FlyingSpaghettiMonster fsm) {
        return "Cooked".equals(fsm.getState().getName());
      }
    },;

```

But it is not necessarily the case always, and more importantly the FSM we modeled
first (fig. 1) is independent of its actual implementation. Even if "getState()"
method is provided, is the method really giving a correct state always? Isn't it
what we are very testing?
Yes, if it is giving a state different from expectation, the SUT might be actually
in wrong state. Or it might be a bug where it is not giving a correct state. Either
way, we can say that it's a bug. Therefore, it's a good idea to check if the returned
state is correct.
But even if it is giving an expected state, it might be just deceiving us. What
we can do here is to check SUT's state if it violates any known constraints derived
from its specification at most.
Of course, if there is a very good way to make sure if and only if the SUT is in the state,
you should implement it in the 'check' method.


## Modeling actions
As we already mentioned, if it is not explicitly allowed in a state machine diagram,
we should think that an operation isn't allowed.

In the figure 1., operations allowed are 'cook' on state 'I', and 'eat' on 'COOKED'.
Therefore, we should test if 'eat' on 'I' and 'cook' on 'COOKED' result in errors.

(Figure 1. Simplest finite state machine)
```
                               
                                +--+
                                |  |eat 
                                |  V 
    +-----+                   +------+
    |  I  |------------------>|COOKED|
    +-----+       cook        +------+


```

The idea that 'unless it is explicitly allowed, it should result in an error' can
be expressed in a following way.

```java

    public enum Spec implements FSMSpec<FSMonster> {
      ...
      @ActionSpec public Expectation<FSMonster> cook(Expectation.Builder<FSMonster> b) {
        return b.invalid().build();
      }
      @ActionSpec public Expectation<FSMonster> eat(Expectation.Builder<FSMonster> b) {
        return b.invalid().build();
      }
    }
    
```

The parameter ```b``` is an instance of ```Expectation.Builder<SUT>```, by which
you can instantiate ```Expectation<SUT>```. In this case you are creating an expectation
where this operation should fail (```invalid```).

And then you will override these methods in the states accordingly.

```java

    public enum Spec implements FSMSpec<FSMonster> {
      @StateSpec I {
        @Override public boolean check(FlyingSpaghettiMonster fsm) {
          return !fsm.isReady();
        }
        @Override public Expectation<FSMonster> cook(Expectation.Builder<FSMonster> b) {
          return b.valid(COOKED).build();
        }
      },
      @StateSpec COOKED {
        @Override public boolean check(FlyingSpaghettiMonster fsm) {
          return fsm.isReady();
        }
        @Override public Expectation<FSMonster> eat(Expectation.Builder<FSMonster> b) {
          return b.valid(COOKED).build();
        }
      },;
     
      @ActionSpec public Expectation<FSMonster> cook(Expectation.Builder<FSMonster> b) {
        return b.invalid().build();
      }
      @ActionSpec public Expectation<FSMonster> eat(Expectation.Builder<FSMonster> b) {
        return b.invalid().build();
      }
    }

```

### Testing values returned by methods
In the example above, we are only able to test SUT's states. But methods can return 
values. And they must be tested. 

To test a returned value by a method, you need to describe your expectation for SUT.
You can do it by giving it to ```Expectation.Builder```.

If a method ```cook()``` of ```FSMonster``` should be returning a string ```"Cooking spaghetti"```,
then you can do this.

```java


    public enum Spec implements FSMSpec<FSMonster> {
      @StateSpec I {
        ...
        @Override public Expectation<FSMonster> cook(Expectation.Builder<FSMonster> b) {
          return b.valid(COOKED, CoreMatchers.startsWith("Cooking")).build();
        }
      },
      ...

```

The method ```valid(FSMSpec<SUT>, Matcher)``` of the builder sets expected status 
of the SUT and a condition to be satisfied by the value returned by the method ```cook``` of SUT,
 in this example ```FSMonster```.
The ```Matcher``` and ```CoreMatchers``` in this example are from ```org.hamcrest``` 
library, which is used in JUnit itself.

### About the finite state machine model we are using
As you may noticed, you can test a method which returns a different value when the
state machine is in a different state.

```java


    public enum Spec implements FSMSpec<FSMonster> {
      @StateSpec I {
        ...
        @Override public Expectation<FSMonster> cook(Expectation.Builder<FSMonster> b) {
          return b.valid(COOKED, CoreMatchers.startsWith("Cooking a dish")).build();
        }
      },
      @StateSpec COOKED {
        ...
        @Override public Expectation<FSMonster> cook(Expectation.Builder<FSMonster> b) {
          return b.valid(COOKED, CoreMatchers.startsWith("Cooking another dish")).build();
        }
      },
      ...

```

This is one sort of transducers called 'Mealy machine'. Its mathematical model can be
formalized as follows.

```

      Sigma: input symbols
      Gamma: output symbols
      S:     states
      s0:    initial state
      delta: state transition function. delta: S x Sigma -> S
      omega: output function. omega: S x Sigma -> Gamma (Mealy machine)
```

You can refer to Wikipedia articles for definitions of the models ([Mealy Machine][1]
and [Finite state transducer][2]).


### Testing a method with parameters
Methods have parameters. 

```
                               
                                +--+
                                |  |eat 
                                |  V 
    +-----+                   +------+
    |  I  |------------------>|COOKED|
    +-----+ cook(pasta,sauce) +------+


```

```java

    public enum Spec implements FSMSpec<FSMonster> {
      @StateSpec I {...},
      @StateSpec COOKED {...},;
    
      @ActionSpec public Expectation<FSMonster> cook(Expectation.Builder<FSMonster> b,
          String pasta,
          String sauce) { ... }
      @ParametersSpec
      public static final Object[][] cook = new Object[][] {
          { "spaghetti", "spaghettini", "penne" },
          { "peperoncino", "carbonara", "meat sauce" },
      };
      @ActionSpec public Expectation<FSMonster> eat(Expectation.Builder<FSMonster> b) {
        ... }
    }

```


 
### Testing exceptions thrown by methods
Same as returned values, we want to test if a method is throwing an appropriate 
exception. You can do it by writing code as follows,

```java

    public enum Spec implements FSMSpec<FSMonster> {
      @StateSpec I {
        ...
        @Override public Expectation<FSMonster> cook(Expectation.Builder<FSMonster> b) {
          return b.invalid(NullPointerException.class).build();
        }
      },
      ...

```

You can even test if the SUT is in intended state after an exception is thrown.

```java

    public enum Spec implements FSMSpec<FSMonster> {
      @StateSpec I {
        ...
        @Override public Expectation<FSMonster> cook(Expectation.Builder<FSMonster> b) {
          return b.invalid(I, NullPointerException.class).build();
        }
      },
      ...

```

In the example above, JCUnit will test if the method ```cook``` throws ```NullPointerException```
and then test if the SUT (```FSMonster``` object) is in state ```I```.

# References
* [1] "Wikipedia article about Mealy machine"
* [2] "Wikipedia article about Finite state transducer"
[1]: http://en.wikipedia.org/wiki/Mealy_machine
[2]: https://en.wikipedia.org/wiki/Finite_state_transducer
