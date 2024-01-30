# StateMachine

Built for FTC robotics and Java programming for ease of autonomous programming.

## Introduction

### Enums

Enums are a way of creating a list of constants that can be used in a program.
For example you could create an Enum of fruits and have it contain apples, oranges, and bananas.

In this implementation, we use Enums commonly named states, or autonomousStates.

```
enum States {
    STATE1,
    STATE2,
    STATE3,
    STATE4,
    STATE5,
    STOP,
}
```

Later we will use these states to control the robot during autonomous.

### What is a State Machine?

More information on State Machines can be found [here](https://state-factory.gitbook.io/state-factory/essential-usage).

A state machine is a way of linearly programming an autonomous architecture.

It is a way of programming that allows you to program a sequence of events that will happen in
order.

It allows it to switch states and way things are moving during autonomous based on the conditions of
the robot.

### Why use a State Machine?

The architecture of the state machine allows for an easier and simplified way to move the robot
through autonomous using the conditions of the robot to control it.

Using a state machine allows for a more efficient way of programming autonomous.

### How does it work?

Once you code the builder function, you call it in your autonomous program and it will run through each state systematically with each function and condition you pass into it.

It will run through step by step, waiting for a condition to be true to move on to the next state.

## Installation

### Adding it to your project

In your ```build.gradle``` file in the TeamCode module, add the following line to the dependencies:

```
dependencies {
    implementation 'com.github.GramGra07:StateMachineFTC:1.0.1'
}
```

In your ```build.dependencies.gradle``` file, add the following to the repositories:

```
repositories {
    maven {url = 'https://jitpack.io'}
}
```

### Updating

git submodule update --remote

## How to use it

### Builder Function

```
StateMachine.Builder<States> builder = new StateMachine.Builder<>();
builder.state(States.STATE1)
        .onEnter(States.STATE1, () -> System.out.println("Entering STATE1"))
        .transition(States.STATE1, () -> true)
        .state(States.STATE2)
        .onEnter(States.STATE2, () -> System.out.println("Entering STATE2"))
        .transition(States.STATE2, () -> true)
        .stopRunning(States.STOP);
StateMachine<States> stateMachine = builder.build();
}
```

```.state()```

This builder is used to add a state to the state machine.

```.onEnter()```

This builder is used to add a function that will run when the state is entered.

```.whileState()```

This builder is used to add a function that will run while the supplied condition is active.

```.onExit()```

This builder is used to add a function that will run when the state is exited.

```.transition()```

This builder is used to add a condition that will be checked to see if the state should be switched.

```.stopRunning()```

This builder is required to add a state that will stop the state machine from running.

```.build()```

This builder is required to build the state machine.

```() -> {}```

This is a lambda function that is used to pass a function into the builder that will be run when it is called.

```() -> ```

This is a supplier for the transition or while state that will return a boolean value. You can make it be ```()-> true``` or ```()-> false``` or you can make it be ```()-> opModeIsActive()```.

### Other useful functions

### Requirements

```.stopRunning()```

## Examples

## Abstracting
