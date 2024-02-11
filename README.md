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

FTC's basic autonomous using encoder drive uses just linear code and while loops waiting for the motors to complete running to their positions before starting the next step. The State Machine allows us to do this directly in the code, without using while loops much or at all.

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

This method is used to add a state to the state machine. It must be created in order to have it all work correctly. You must add a state for each step in the program or it will throw errors when you try to run it.

```.onEnter(state, ()->{ function to run })```

This builder is used to add a function that will run when the state is entered. This is mainly used to start your state function, this meaning that it will start the runToPosition in this step if you are using basic encoder drive. Specifically with Road Runner, it can be used to start an async trajectory. It is also useful in order to perform additional robot setup before running your program.

```.whileState(state, ()-> condition (when true, will break loop) , ()-> function to run)```

This builder is used to add a function that will run while the supplied condition is active. When the condition becomes true, it will move on. This is extremely useful when you are using PID, or Road Runner specifically because they require calls to them constantly in a while loop in order to run correctly. For younger teams, this could be useful to make sure an arm is set to the correct spot.

```.onExit(state, ()->{ function to run })```

This builder is used to add a function that will run when the state is exited. You would use this to open a claw, or raise your arm for instance. It is useful to make sure something is set up correctly before the next step if you can't add it into the next onEnter.

```.transition(state, ()-> condition)```

This builder is used to add a condition that will be checked to see if the state should be switched. If the condition is true, it will move on. You must add this builder at the end of the state method. Essentially the amount of states, must equal the amount of transitions.

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

This function contains the main loop of the StateMachine. It takes in your current opMode as a parameter and will run the while loop for you automatically. ```while(machine.mainLoop(this)){```. It will allow you to have easier use of the State Machine because it will control it all for you without any other work.

```machineName.start()```

This function starts the StateMachine and must go immediately before the while loop.

```machineName.update()```

This function tells the StateMachine to update the current state and check if it should switch states and what it should currently be doing. It will be placed in the main while loop, which tells the State Machine to run the next step, or keep doing what it is doing.

```machineName.stop()```

This function completely stops the State Machine, no matter what step it is on or where it is in the process.
