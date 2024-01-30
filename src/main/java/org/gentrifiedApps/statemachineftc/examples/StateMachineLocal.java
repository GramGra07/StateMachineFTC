package org.gentrifiedApps.statemachineftc.examples;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.gentrifiedApps.statemachineftc.StateMachine;


@Autonomous
@Disabled
public class StateMachineLocal extends LinearOpMode {
    public enum state {
        INIT,
        StateOne,
        END,
        STOP,
    }

    @Override
    public void runOpMode() {
        StateMachine<state> machine = new StateMachine.Builder<state>()
                .state(state.StateOne)
                .onEnter(state.StateOne, () -> {
                    // do something on enter
                })
                .whileState(state.StateOne, () -> {// escapeCondition
                    return true; // return true or false
                }, () -> {
                    // while a condition is false, do something
                })
                .transition(state.StateOne, () -> {
                    return true; // return true or false
                })
                .state(state.END)
                .onEnter(state.END, () -> {
                    // do something on enter
                })
                .whileState(state.END, () -> {
                    return true; // return true or false
                }, () -> {
                    // while a condition is false, do something
                })
                .transition(state.END, () -> {
                    return true; // return true or false
                })
                .stopRunning(state.STOP) // stop the state machine
                .build();
        waitForStart();
        machine.start();
        while (machine.mainLoop(this)) {
            machine.update();
        }
    }
}