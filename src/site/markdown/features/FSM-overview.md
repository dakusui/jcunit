# Background
## About FSMs
Why FSM?
Map ideas in Java to FSM's ones

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

# How to use JCUnit
## Simple FSM
* FSM Diagram
* Java sample code (SUT)
* Java sample code (JCUnit)
  Testing a state
  Define a field (enum member) for the state
  ```@StateSpec``` annotation
  Define a ```check``` method
  Testing an action
  ```@ActionSpec``` annotation


## Nested FSMs
In real world, things can happen repeatedly.
Recursive (nested) model is necessary.

* FSM Diagram
* Java sample code (SUT)
* Java sample code (JCUnit)


# Future works
* Multi-threading support
* Non-deterministic FSM support
