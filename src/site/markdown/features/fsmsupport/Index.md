# Abstract
For the author of JCUnit, the goal of JCUnit is to implement a testing framework which allows him to



# Introduction
## About FSMs
A finite state machine (FSM) is everywhere in software.
And as discussed in a well known article about [BDD][1],
BDD scenarios can be considered as descriptions of FSM behaviours.

Then, once a software product/system is modeled as FSM, can't we generate a test
suite and execute it automatically? And doesn't it sound useful?

"Model the software under test as FSM and let your computer do the rest" is the goal of this feature.

If we can model an SUT as a finite state machine, we should simply be able to create
a test suite which consists of N times M test cases, where N is a number of possible
states and M is a number of event types.

Chapter 6 of [Introduction to combinatorial testings][5] discusses how we can design
a test suite for a state machine in detail.

But these N states and M events are the model of the software under test (SUT) or
we can say that it is just a tester's understanding about SUT. Not the FSM of SUT itself.

Since SUT might have internal, hidden, or unkintentional states, it can behave unexpectedly,
which means you find a bug, only when it experiences a certain state transition history.
In FSM support feature of JCUnit, the transition history (or path) is called a ```Story```.

If we want to test the state transition history exhaustively (all the possible paths on FSM),
the test cases we'll have to execute would be (N*M)^n, where n is the number of the states
the SUT goes through during one ```Story``` is being executed.

Obviously, this can easily be a too big number to finish within a practical amount of time.


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


## Is FSM a model powerful enough?
Actually not.
Mealy Machine will be necessary.
Input/output/parameters

But in this document, if we call a model FSM, it refers to a Mealy machine.


### Mealy machine


```
    
    <Sigma, Gamma, S, s0, delta, omega>
    
    Sigma ...
    Gamma ...
    S     ...
    s0    ...
    delta ...
    omega ...
    
```

## History of automated testing of FSM
### Tsurumaki and Yanagida's method
[Tsurumaki and Yanagida][2] discussed how a combinatorial test suite can be implemented
for an FSM using their open source tool PictMaster and Microsoft's PICT.
The method they presented in the material is like below,

1. For each state (S0, S1, ... Sn-1), generate k factors, where k is a length of a 'test scenario'. 
   So you will have n times k factors [S(0,0), S(1,0), ..., S(n-1,0)], ..., [S(0,k-1),S(1,k-1),...S(n-1,k-1)]) 
   in total.
2. For each factor , its levels are all possible transitions from the state to which 
   it belongs and special level which represents 'invalid'.
3. Constraints are defined by a user so that only valid transitions can happen.
3. Let pairwise engine (e.g. [PICT][4]) process the factors and their levels and generate a test suite.
4. By tracing the transitions in a test case from S(0,0), you can interpret a generated 
   test case into a sequence of state transitions.

The approach JCUnit took is inspired by this procedure but a different one.


### Spec Explorer
Spec Explorer supports both state modeling and combinatorial value selection.
But value selection is only applied to values given to functions.


Map ideas in Java to FSM's ones

## How this document is organized
(t.b.d.)
* [JCUnit FSM testing model](Model.md)
* [How to use JCUnit's FSM support feature](Usage.md)
* [Future works](FutureWorks.md)

# References
* [1] "The Truth about BDD(Uncle Bob)"
* [2] "Developing an open source combinatorial testing tool(Toshiro Tsurumaki and Yukihide Yanagida)"
* [3] "Wikipedia article about Mealy machine"
* [4] "Pairwise Testing & PICT Tool"
* [5] "Introduction to Combinatorial Testing" (Kuhn et al)
* [6] "Spec Explorer"

[1]: https://sites.google.com/site/unclebobconsultingllc/the-truth-about-bdd
[2]: http://www.jasst.jp/archives/jasst09e/pdf/D4-2.pdf
[3]: http://en.wikipedia.org/wiki/Mealy_machine
[4]: http://blogs.msdn.com/b/nagasatish/archive/2006/11/30/pairwise-testing-pict-tool.aspx
[5]: http://books.rakuten.co.jp/rk/bac16b7ae73b3e53b076cc479a7e870a/
[6]: https://msdn.microsoft.com/en-us/library/ee620411.aspx