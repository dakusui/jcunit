# Background

FSM is everywhere.
A software system can be considered as an FSM.

Combinatorial explosion.


## Combinatorial tests for FSMs
What are factors and what are levels?
(t.b.d.)


# Design
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


### Scenario Sequence (Story)

history length

(t.b.d.)

### main and setUp scenario sequences
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
### Simple FSM
(t.b.d.)

#### Annotations
- StateSpec
- ActionSpec
- ParametersSpec

# References
* (t.b.d.) Combinatorial FSM tests, Tsurumaki, Toshiro
* (t.b.d.)
* (t.b.d.) http://en.wikipedia.org/wiki/Mealy_machine

