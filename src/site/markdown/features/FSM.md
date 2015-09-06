# Introduction
* Model software and let JCUnit do the rest. This is the goal of JCUnit.
** Modeling or documenting your software is essential to development. You cannot avoid it.
** Without a sound mental model, which can define "how should it behave?", for your product, you shouldn't be able to test it.

* What model are we going to use?
* Is your SUT stateless? -> Generally not.
  - Those can be considered as 'constraints'
* What are tests
* We want to achieve a methodology where developers only need to do
  - Write software
  - Model software spec
  - And let computers do the rest
  The first two are essential to a software product.
* FSM looks useful to model a software product.
* We are going to allow users to model their SUT's as state machines. In 
  whatever manner it is achieved, the number of factors and their levels would be
  big. This results in unmanageable huge test suite.
  To limit number of test cases, combinatorial testing technique will be used.

## Modeling a system as FSM
* BDD (Behavior driven development)
* A Turing machine
* Java object
  

## Challenges in FSM testing
* What to cover
* Coverage
* Explosion of test cases

### Normal FSM
* Not enough powerful to model usual software
* Methods return values, and they need to be verified.
### Mealy Machine (FST)
* Slight modification (what was it?)
* If we consider a method something like 'event' in FSM model, how should we handle its parameters?
## Known solutions
### Spec Explorer
* It doesn't treat states and actions as factors. 
* It just applies combinatorial testing idea to function parameters
### Tsurumaki and Yanagida's methody 
* Forcing PICT to put a FSM in a certain state.
* All states are exercised as long as it is possible.

## How this document is organized 
# Design of FSM support
## History
* Factors and their levels
States, actions, their parameters
## Constraint
## Set up scenario

# Usage
## Simple FSM

## Nested FSM
# Future works
* Concurrency
# References
* (t.b.d.)
*

How FSM is useful and its limit
* It's generally used even if you don't this you are using it.
Challenges
* If we want to test FSM thoroughly, a number of test cases explodes.
* Let's apply Combinatorial testing technique
** How to map states and event to factors and levels
Other works that test FSM and their limit
* Spec Explorer
* Tsurumaki's method
** No transition history


