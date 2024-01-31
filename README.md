# StateMachine

Built for FTC robotics and Java programming for ease of autonomous programming.

[![](https://jitpack.io/v/GramGra07/StateMachineFTC.svg)](https://jitpack.io/#GramGra07/StateMachineFTC)

## Introduction

TeleOp mode is pretty straightforward for new teams, you use the gamepad to control the robot.  Autonomous is not as easy to program but it isn't all the much different than TeleOp, you just have to pre-program what movements you want the robot to perform. StateMachine can help with this and make it easier to understand.

### Enums

Enums are a way of creating a list of constants that can be used in a program.
For example you could create an Enum of fruits and have it contain apples, oranges, and bananas. Another example could be types of cars, you could have a list of cars that are sedans, trucks, and SUVs.

Enums are a variable that is defined by the user, and has a list of constants the user defines.

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

In the case of autonomous, states could be things like drive forward, turn left, turn right, raise arm, etc.

These enums provide an advantage because they are easy to read and understand in comparison to just telling the robot exactly what to do when. It can also wait for specific input from the robot, which might be harder to implement with just hard coding autonomous.

Later we will use these states to control the robot during autonomous.

### What is a State Machine?

More information on State Machines can be found [here](https://state-factory.gitbook.io/state-factory/essential-usage).

A state machine is a way of linearly programming an autonomous architecture. This meaning that it will go step by step through the code and run each function in order. Autonomous architecture refers to the way you structure and build your autonomous.

It allows it to switch states and way things are moving during autonomous based on the conditions of
the robot. This is useful because the robot will only do the "next step" when it is ready to do so based on a condition you provide to it.

Specifically for FTC, things like PIDF control and other motor controls require the loop to constantly be running. FTC sample code almost never uses the while(opModeIsActive()) loop, but instead just programs it linearly. For younger teams as well, this is a way to make it easier to understand and program the autonomous architecture.

### Why use a State Machine?

The architecture of the state machine allows for an easier and simplified way to move the robot
through autonomous using the conditions of the robot to control it.

Using a state machine allows for a more efficient way of programming autonomous.

FTC Sample code programs the autonomous very linearly and one by one. As soon as you need other motor control or PIDF or a reason to use the while(opModeIsActive()) loop, it becomes much more difficult to program because it is linear. The StateMachine helps solve this problem and makes it easier to program.

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

Inside your ```build.common.gradle``` file, add the following to your packaging options:

```
packagingOptions {
    exclude 'META-INF/LICENSE-notice.md'
    exclude 'META-INF/LICENSE.md'
}
```

## How to use it

### Builder Function

There are a total of five builders you can choose from.

It is required to have both a state and transition builder for each state and the first state **must** have a onEnter command.

#### Inside the <> brackets, you **must** put the enum you created earlier.

```
StateMachine.Builder<name> builder = new StateMachine.Builder<>();
builder.state(name.STATE1)
        .onEnter(name.STATE1, () -> System.out.println("Entering STATE1"))
        .name(name.STATE1, () -> true)
        .state(name.STATE2)
        .onEnter(name.STATE2, () -> System.out.println("Entering STATE2"))
        .transition(name.STATE2, () -> true)
        .stopRunning(name.STOP);
StateMachine<name> stateMachine = builder.build();
```

The builder function builds the StateMachine. It is a function that returns a built StateMachine with all of the states and transitions you have added to it.

```.state()```

This method is used to add a state to the state machine.

```.onEnter(state, ()->{ function to run })```

This builder is used to add a function that will run when the state is entered.

```.whileState(state, ()-> condition (when true, will break loop) , ()-> function to run)```

This builder is used to add a function that will run while the supplied condition is active. When the condition becomes true, it will move on.

```.onExit(state, ()->{ function to run })```

This builder is used to add a function that will run when the state is exited.

```.transition(state, ()-> condition)```

This builder is used to add a condition that will be checked to see if the state should be switched. If the condition is true, it will move on.

```.stopRunning(state)```

This builder is required to add a state that will stop the state machine from running.

```.build()```

This builder is required to build the state machine.

```() -> {}```

This is a lambda function that is used to pass a function into the builder that will be run when it is called. A lambda function is a function that the user will pass in, and then it will be used and called by the state machine while running.

```() -> ```

This is a supplier for the transition or while state that will return a boolean value. You can make it be ```()-> true``` or ```()-> false``` or you can make it be ```()-> opModeIsActive()```.

### Other useful functions

```machineName.mainLoop(this)```

This function contains the main loop of the StateMachine. It takes in your current opMode as a parameter and will run the while loop for you automatically. ```while(machine.mainLoop(this)){```.

```machineName.start()```

This function starts the StateMachine and will go immediately before the while loop.

```machineName.update()```

This function tells the StateMachine to update the current state and check if it should switch states and what it should currently be doing.

## Examples


## Abstracting

