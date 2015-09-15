# Introduction
In testing, it is very often to realize that there are some sort of 'sequence' to be executed like,
"In order to use function A, you must do function X with parameter x1 and x2, then execute function Y, which
will return object y. Pass the object as parameter a of function A to execute it."
"To execute function B, you can use the object y, mentioned in the procedure above,  as its first parameter."
If we are using JCUnit, "ConstraintManager" is a mechanism to handle this sort of situation, 
but it happens too often.
Writing custom constraint manager every time is boring, dependent on individual, and very error prone
procedure.
Model software and let JCUnit do the rest. This is the goal of JCUnit from the first place.
    
    
  * What model are we going to use?
  * Is your SUT stateless? -> Generally not.
    - In JCUnit, those can be considered as 'constraints'
    - I realized that I am creating a similar constraint manager over and over again for
      different SUT's. And it is because they generally have states inside it and
      my constraint managers are trying to eliminate invalid scenarios from generated 
      scenarios expressed as factors and levels.
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
  * (t.b.d.)
    * Modeling or documenting your software is essential to development. You cannot avoid it.
    * Without a sound mental model, what can define "how should it behave?", for your product, you shouldn't be able to test it.

## Modeling a system as FSM
* BDD (Behavior driven development)
* Turing machine
* Java object is also an FSM

## Challenges in FSM testing
* FSM testing looks very general and useful, if it is possible. But there are some challenges.
* Explosion
  By whatever a way we are going to take to test FSM, a large number of input values would involve.
  As in all the other tests, we'll need to limit number of test cases to avoid combinatorial explosion.
  And to avoid combinatorial explosion, a tool we have to solve it is 'Combinatorial testing' testing techniques
  including 'pairwise', 'all-pair', or 't-wise'. Those can be used through JCUnit.
* What to cover and how?
** 
* Coverage
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


