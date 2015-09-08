# Introduction
* Theme: Model software and let JCUnit do the rest.
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
### Tsurumaki and Yanagida's method
* 
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


