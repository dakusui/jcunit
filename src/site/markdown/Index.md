# Introduction
JCUnit is a framework to perform combinatorial tests using 'pairwise'(or more generally 't-wise') technique.
About what combinatorial testings are, articles below are useful as a starting point.

* [All-pairs testing](http://en.wikipedia.org/wiki/All-pairs_testing)
* [Introduction to Combinatorial Testing](http://mse.isri.cmu.edu/software-engineering/documents/faculty-publications/miranda/kuhnintroductioncombinatorialtesting.pdf)

Very roughly to say, it's a technique to generate test cases with good 'coverage' without making the number of test cases explode.

## How JCUnit's documents are organized


# Design
## Design policy
(t.b.d.)
Java 6
Dependencies as less as possible

## Software structure
(t.b.d.)
# Features
1. Annotation based test suite generation.
2. Pluggable software architecture
    1. Test suite generator
    2. Constraint handler
    3. Factor level provider
3. IPO algorithm based pairwise/t-wise combinatorial test suite generation
4. [Constraint support](features/ConstraintSupport.md)
    * You can write your own constraints by implementing 'ConstraintManager' interface.
5. [FSM support](features/fsmsupport/Index.md)
    1. JCUnit provides an easy way to define FSM models
    2. Based on the user defined FSM model, JCUnit automatically generates a test suite.

# Tips and examples
* [Tips](tips/Index.md)
* [Exmples](examples/Index.md)

# Limitations and future works
## General
1. Enhance constraint support
  - JCUnit's constraint support is still in initial phase. It tries 50 different tuples to find
  values that satisfy constraints given by a constraint manager. This can result in reducing
  the pairwise/t-wise coverage. This may especially happen when you are using IPO algorithm
   for test suite generation.
2. AETG support
  - Currently JCUnit supports only IPO algorithm as its pairwise/t-wise test suite generation engine.
  but there is another well-known algorithm called AETG, which seems easier to handle complex constraints.
## FSM
For limitations and future works in FSM (finite state machine) support area, 
refer to [this](features/FSM/FutureWoks.md)

# FAQ
Frequently asked questions are found [here](FAQ.md)
