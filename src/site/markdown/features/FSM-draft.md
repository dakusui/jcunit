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

But this isn't a way in which I intended to use this feature.
You may be confused with the output of JCUnit in some conditions.

If the action ```cook``` has a guard condition in a diagram like below,

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

